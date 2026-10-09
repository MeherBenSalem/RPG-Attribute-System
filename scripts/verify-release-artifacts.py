#!/usr/bin/env python3
"""Fail-closed release inventory and internal JAR provenance verification.

Only a complete ten-loader set bound to the requested source commit is accepted.
No filenames alone, partial prebuilt sets, store writes, or credentials are trusted.
"""
import argparse
import hashlib
import json
from pathlib import Path
import re
import struct
import tomllib
import zipfile

MOD_ID = 'rpg_attribute_system'
MATRIX = [('1.20.1','fabric',17),('1.20.1','forge',17),('1.21.1','fabric',21),('1.21.1','neoforge',21),
          ('26.1.2','fabric',25),('26.1.2','neoforge',25),('26.2','fabric',25),('26.2','neoforge',25),
          ('26.3','fabric',25),('26.3','neoforge',25)]
RUNTIME_DEPENDENCIES = {'minecraft','java','fabricloader','forge','neoforge'}
MOD_ALIASES = {'fabric':'fabric-api','fabric-api':'fabric-api','jauml':'jauml'}
FEATURE_CLASSES = ['tn/nightbeam/ras/config/MobXpRules.class','tn/nightbeam/ras/config/MobXpConfig.class',
                   'tn/nightbeam/ras/client/gui/PixelRpgBookLayout.class']


def manifest_attributes(text):
    unfolded=[]
    for line in text.replace('\r\n','\n').split('\n'):
        if line.startswith(' ') and unfolded:
            unfolded[-1] += line[1:]
        else:
            unfolded.append(line)
    result={}
    seen=set()
    for line in unfolded:
        if not line: break
        if ': ' not in line: raise ValueError('Malformed manifest attribute')
        key,value=line.split(': ',1)
        if key.lower() in seen: raise ValueError('Duplicate manifest attribute: '+key)
        seen.add(key.lower())
        result[key]=value
    return result


def strict_json(text):
    def object_pairs(pairs):
        result={}
        for key,value in pairs:
            if key in result: raise ValueError('Duplicate JSON key: '+key)
            result[key]=value
        return result
    value=json.loads(text,object_pairs_hook=object_pairs)
    if not isinstance(value,dict): raise ValueError('Loader metadata must be an object')
    return value


def source_metadata(minecraft,loader):
    root=Path(__file__).resolve().parents[1]/minecraft
    properties={}
    for line in (root/'gradle.properties').read_text().splitlines():
        if line and not line.lstrip().startswith('#') and '=' in line:
            key,value=line.split('=',1);properties[key.strip()]=value.strip()
    name='fabric.mod.json' if loader=='fabric' else ('META-INF/mods.toml' if loader=='forge' else 'META-INF/neoforge.mods.toml')
    text=(root/loader/'src/main/resources'/name).read_text()
    text=re.sub(r'\$\{([a-zA-Z_]+)\}',lambda match:properties[match.group(1)],text)
    return strict_json(text) if loader=='fabric' else tomllib.loads(text)


def dependency_rows(meta):
    all_rows=meta.get('dependencies',{})
    if not isinstance(all_rows,dict): raise ValueError('Loader dependencies must be an object')
    rows=all_rows.get(MOD_ID,[])
    if not isinstance(rows,list) or not all(isinstance(item,dict) for item in rows):
        raise ValueError('Loader dependency rows must be objects')
    return rows


def check_declared_metadata(meta,expected,loader):
    if loader=='fabric':
        actual_deps=meta.get('depends')
        if not isinstance(actual_deps,dict) or actual_deps!=expected.get('depends'):
            raise ValueError('Fabric required dependency declarations differ from reviewed source')
    else:
        if meta.get('modLoader')!=expected.get('modLoader') or meta.get('loaderVersion')!=expected.get('loaderVersion'):
            raise ValueError('Loader/container version differs from reviewed source')
        def normalize(rows):
            normalized=[]
            for row in rows:
                copy=dict(row)
                if not isinstance(copy.get('versionRange'),str): raise ValueError('Invalid dependency version range')
                copy['versionRange']=re.sub(r'\s+','',copy['versionRange'])
                normalized.append(copy)
            return sorted(normalized,key=lambda row:row.get('modId',''))
        if normalize(dependency_rows(meta))!=normalize(dependency_rows(expected)):
            raise ValueError('Loader dependency declarations differ from reviewed source')


def required_dependencies(meta,loader):
    if loader=='fabric':
        dependencies=meta.get('depends',{})
        if not isinstance(dependencies,dict): raise ValueError('Fabric depends must be an object')
        required=list(dependencies)
    else:
        dependencies=dependency_rows(meta)
        required=[item.get('modId') for item in dependencies
                  if item.get('mandatory') is True or item.get('type')=='required']
    normalized=[]
    for dep in required:
        if dep in RUNTIME_DEPENDENCIES: continue
        if dep not in MOD_ALIASES: raise ValueError('Unmapped required dependency: '+str(dep))
        normalized.append(MOD_ALIASES[dep])
    return sorted(set(normalized))


def verify_jar(path,version,commit,minecraft,loader,java):
    if path.stat().st_size > 128*1024*1024: raise ValueError('Unexpectedly large mod JAR: '+path.name)
    try:
        with zipfile.ZipFile(path) as jar:
            names=jar.namelist()
            if len(names)>10000 or len(names)!=len(set(names)): raise ValueError('Duplicate or excessive ZIP entries')
            if sum(item.file_size for item in jar.infolist())>256*1024*1024: raise ValueError('Excessive uncompressed JAR size')
            if any(name.startswith('/') or '..' in Path(name).parts or '\\' in name for name in names):
                raise ValueError('Unsafe ZIP entry path')
            attributes=manifest_attributes(jar.read('META-INF/MANIFEST.MF').decode('utf-8'))
            expected={'RAS-Source-Commit':commit,'Built-On-Minecraft':minecraft,
                      'Implementation-Title':loader,'Implementation-Version':version}
            for key,value in expected.items():
                if attributes.get(key)!=value: raise ValueError(f'{path.name}: {key} must be {value}')
            for name in FEATURE_CLASSES:
                binary=jar.read(name)
                if len(binary)<8 or binary[:4]!=b'\xca\xfe\xba\xbe' or struct.unpack('>H',binary[6:8])[0]!=java+44:
                    raise ValueError(f'{path.name}: missing/wrong Java{java} feature class {name}')
            if loader=='fabric':
                metadata_path='fabric.mod.json'
                raw=jar.read(metadata_path).decode('utf-8')
                meta=strict_json(raw)
                if meta.get('id')!=MOD_ID or meta.get('version')!=version:
                    raise ValueError('Wrong Fabric mod ID/version')
                dependencies=meta.get('depends',{})
                if not isinstance(dependencies,dict): raise ValueError('Fabric depends must be an object')
                if dependencies.get('minecraft') not in [minecraft,'~'+minecraft]:
                    raise ValueError('Wrong Fabric Minecraft dependency')
                if 'fabricloader' not in dependencies or 'java' not in dependencies:
                    raise ValueError('Missing Fabric runtime dependency')
            else:
                metadata_path='META-INF/mods.toml' if loader=='forge' else 'META-INF/neoforge.mods.toml'
                raw=jar.read(metadata_path).decode('utf-8')
                meta=tomllib.loads(raw)
                mod_rows=meta.get('mods',[])
                if not isinstance(mod_rows,list) or not all(isinstance(item,dict) for item in mod_rows):
                    raise ValueError('Loader mods must be an array of objects')
                mods=[mod for mod in mod_rows if mod.get('modId')==MOD_ID]
                if len(mods)!=1 or mods[0].get('version')!=version: raise ValueError('Wrong loader mod ID/version')
                dependencies=dependency_rows(meta)
                required={item.get('modId'):item for item in dependencies
                          if item.get('mandatory') is True or item.get('type')=='required'}
                if loader not in required or 'minecraft' not in required: raise ValueError('Missing loader/runtime dependency')
                mc_range=required['minecraft'].get('versionRange','')
                if not mc_range.startswith('['+minecraft+','): raise ValueError('Wrong loader Minecraft range')
            incompatible={'fabric.mod.json','META-INF/mods.toml','META-INF/neoforge.mods.toml'}-{metadata_path}
            if incompatible.intersection(names): raise ValueError('Wrong/multiple loader descriptor present')
            if '${' in raw: raise ValueError('Unexpanded loader metadata placeholder')
            check_declared_metadata(meta,source_metadata(minecraft,loader),loader)
            dependencies=required_dependencies(meta,loader)
            expected_deps=(['fabric-api','jauml'] if loader=='fabric' and minecraft in ['1.20.1','1.21.1']
                           else ['fabric-api'] if loader=='fabric' else ['jauml'] if minecraft in ['1.20.1','1.21.1'] else [])
            if dependencies!=expected_deps: raise ValueError(f'{path.name}: required dependency set changed: {dependencies}')
    except (KeyError,zipfile.BadZipFile,UnicodeDecodeError,json.JSONDecodeError,tomllib.TOMLDecodeError) as error:
        raise ValueError(f'{path.name}: invalid/incomplete internal JAR metadata: {error}') from error
    binary=path.read_bytes()
    return dict(file_name=path.name,loader=loader,minecraft=minecraft,java=java,version=version,
                source_commit=commit,size_bytes=len(binary),sha256=hashlib.sha256(binary).hexdigest(),
                sha512=hashlib.sha512(binary).hexdigest(),sha1=hashlib.sha1(binary).hexdigest(),
                required_mod_ids=dependencies,mod_metadata_path=metadata_path)


def verify(directory,version,commit):
    directory=Path(directory).resolve()
    if not re.fullmatch(r'\d+\.\d+\.\d+',version): raise ValueError('Release version must be x.y.z')
    if not re.fullmatch(r'[0-9a-f]{40}',commit): raise ValueError('Source commit must be a full lowercase40-hex SHA')
    if not directory.is_dir(): raise ValueError('Artifact directory missing')
    expected={f'{MOD_ID}-{loader}-{mc}-{version}.jar':(mc,loader,java) for mc,loader,java in MATRIX}
    found={}
    for path in directory.rglob('*'):
        if path.suffix.lower()!='.jar': continue
        if path.is_symlink() or not path.resolve().is_relative_to(directory): raise ValueError('Symlink/outside artifact path')
        if path.name not in expected: raise ValueError('Unexpected release JAR: '+path.name)
        if path.name in found: raise ValueError('Duplicate release JAR: '+path.name)
        found[path.name]=path
    missing=sorted(set(expected)-set(found))
    if missing: raise ValueError('Incomplete ten-JAR inventory: '+', '.join(missing))
    files=[]
    for name,(mc,loader,java) in expected.items():
        result=verify_jar(found[name],version,commit,mc,loader,java)
        result['path']=found[name].relative_to(directory).as_posix()
        files.append(result)
    return dict(schema_version=1,mod_id=MOD_ID,release_version=version,source_commit=commit,
                complete_inventory=True,artifact_count=len(files),files=files)


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--directory',required=True,type=Path)
    parser.add_argument('--version',required=True)
    parser.add_argument('--commit',required=True)
    parser.add_argument('--output',required=True,type=Path)
    args=parser.parse_args()
    result=verify(args.directory,args.version,args.commit)
    args.output.write_text(json.dumps(result,indent=2)+'\n')
    print(f'PASS verified complete{len(result["files"])}-JAR set for {args.version} at {args.commit}')

if __name__=='__main__': main()
