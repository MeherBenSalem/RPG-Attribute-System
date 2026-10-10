#!/usr/bin/env python3
"""Check the real studio importer/exporter and preview against all five production parsers.

Requires Node >=22, a JDK with Java 17 compilation support, and --gson-jar (or
GSON_JAR). This focused regression is not rendered-browser QA or a loader build.
All cross-language exports and compiled classes are disposable temporary files.
"""
import argparse
import os
from pathlib import Path
import subprocess
import sys
import tempfile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--gson-jar', default=os.environ.get('GSON_JAR'))
    parser.add_argument('--javac', default='javac')
    parser.add_argument('--java', default='java')
    parser.add_argument('--node', default='node')
    args = parser.parse_args()
    if not args.gson_jar or not Path(args.gson_jar).is_file():
        parser.error('Supply --gson-jar PATH or GSON_JAR pointing to a com.google.code.gson jar')
    gson = str(Path(args.gson_jar).resolve())
    repo = Path(__file__).resolve().parents[1]
    studio = repo / 'tools/config-studio'
    version = subprocess.check_output([args.node, '--version'], text=True).strip()
    if int(version.lstrip('v').split('.')[0]) < 22:
        parser.error('Node 22 or newer is required')
    for source in sorted((studio / 'dist').glob('*.mjs')):
        subprocess.run([args.node, '--check', str(source)], check=True)
    for test in ['check.mjs', 'edge-cases.mjs', 'zip-safety.mjs']:
        subprocess.run([args.node, str(studio / 'tests' / test)], cwd=studio, check=True)
    # Reuse the original fixture/cache/error/reload checks without changing their source.
    subprocess.run([sys.executable, str(repo / 'scripts/test-mob-xp.py'),
                    '--gson-jar', gson, '--javac', args.javac, '--java', args.java], check=True)
    with tempfile.TemporaryDirectory(prefix='ras-studio-parity-') as temporary:
        output = Path(temporary)
        subprocess.run([args.node, str(studio / 'tests/export-parity.mjs'),
                        str(repo / 'tests/fixtures/mob-xp-contract.json'), str(output)], check=True)
        for root in ['1.20.1', '1.21.1', '26.1.2', '26.2', '26.3']:
            source = repo / root / 'common/src/main/java'
            if root == '26.2':
                source /= 'java'
            production = source / 'tn/nightbeam/ras/config/MobXpRules.java'
            classes = output / root
            classes.mkdir()
            subprocess.run([args.javac, '--release', '17', '-cp', gson, '-d', str(classes),
                            str(production), str(studio / 'tests/MobXpStudioParityTest.java')], check=True)
            subprocess.run([args.java, '-cp', str(classes) + os.pathsep + gson,
                            'tn.nightbeam.ras.config.MobXpStudioParityTest',
                            str(output / 'studio-export-contract.json')], check=True)
            print(f'PASS {root}: fresh studio ZIP exports / actual MobXpRules / preview parity', flush=True)
    print('PASS configuration studio focused regression; rendered UI and loader builds are separate')


if __name__ == '__main__':
    main()
