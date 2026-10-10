import re,json,pathlib
s=pathlib.Path('source-reference/ConfigInitializer.java').read_text()
pack={}
methods=['createGlobalSettings','createRespecConfig','createTemplatesConfig','createStatsDisplayConfig','createAttributeSettings','createDropRateConfig','createItemsLockConfig','createBlocksLockConfig','createLevelUpRewardsConfig','createDisplaySettings','createOverlayConfig']
for method in methods:
 chunk=s.split('private static void '+method+'() {',1)[1].split('\n    private static ',1)[0]
 directory=re.search(r'String dir = "([^"]+)"',chunk).group(1)
 file=re.search(r'String file = "([^"]+)"',chunk).group(1)
 obj={}
 for m in re.finditer(r'Services.CONFIG.(setNumberValue|setBooleanValue|setStringValue|addStringToArray)\(dir, file, "([^"]+)",\s*("(?:[^"\\]|\\.)*"|true|false|[-\d.]+)\s*\)',chunk):
  op,key,val=m.groups();val=json.loads(val)
  if op=='addStringToArray':obj.setdefault(key,[]).append(val)
  else:obj[key]=val
 if method=='createGlobalSettings':
  obj['exp_curve_first_level_xp']=obj['first_level_vp'];obj['exp_curve_default_scale']=obj['levels_scale_default'];obj['exp_curve_scale_intervals']=obj['levels_scale_interval'].copy()
  obj['exp_required_per_level']=['[level]%d[levelEnd][xp]%d[xpEnd]'%(i,100+35*i+5*i*i) for i in range(1,101)]
 pack['config/'+directory+'/'+file+'.json']=obj

def switch(method,i):
 c=s.split('private static ',1)[0] if False else s.split(method+'(int id) {',1)[1].split('\n    private static ',1)[0]
 matches=list(re.finditer(r'case ([\d, ]+) ->\s*("(?:[^"\\]|\\.)*"|[-\d.]+)',c))
 for m in matches:
  if i in map(int,m.group(1).split(',')):return json.loads(m.group(2))
 m=re.search(r'default ->\s*("(?:[^"\\]|\\.)*"|[-\d.]+)',c)
 return json.loads(m.group(1)) if m else None
for i in range(1,9):
 obj={k:switch(fn,i) for k,fn in {'display_name':'getDefaultDisplayName','description':'getAttributeDescription','init_val_attribute':'getDefaultInitValue','base_value_per_point':'getDefaultValuePerPoint','tip_to_display':'getDefaultTip'}.items()}
 obj.update(max_level=500,cmd_to_exc=[switch('getDefaultCommand',i)],on_level_event='effect give @s minecraft:instant_health 2 3' if i==1 else '',lock=False,icon_path=f'screens/att_{i}.png')
 pack[f'config/ras/attributes/attribute_{i}.json']=obj
 pack[f'config/ras/display/attribute_{i}.json']={'enable':True,'display_name':switch('getDefaultDisplayDisplay',i),'attribute_namespace':'minecraft','attribute_name':switch('getDefaultAttributeName',i),'display_modifer':1}
pathlib.Path('dist/defaults.mjs').write_text('export const SOURCE_SHA="9900a048e41b10f8bba0e7fdffbbc8e95f50a186";\nexport const DEFAULTS='+json.dumps(pack,ensure_ascii=False,indent=2)+';\n')
print('Default JSON files:',len(pack))
