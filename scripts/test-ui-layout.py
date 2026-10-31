#!/usr/bin/env python3
"""Run actual native-pixel layout, staging, preview, number, motion, ui.json/texts.json and banner regressions
for all five roots.

These are geometry/source regressions, not screenshot or client rendering tests. The settings and texts classes
read JSON, so a cached Gson jar is required; Services and Constants are minimal doubles.
"""
import argparse
import os
from pathlib import Path
import subprocess
import tempfile

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--gson-jar',default=os.environ.get('GSON_JAR'))
parser.add_argument('--javac',default='javac')
parser.add_argument('--java',default='java')
args=parser.parse_args()
if not args.gson_jar or not Path(args.gson_jar).is_file(): parser.error('Supply --gson-jar PATH or GSON_JAR')
repo=Path(__file__).resolve().parents[1]
STUBS={
 'tn/nightbeam/ras/platform/Services.java':
  'package tn.nightbeam.ras.platform; public final class Services { public static final Config CONFIG=new Config();'
  ' public static final class Config { public java.nio.file.Path getConfigDirectory()'
  '{return java.nio.file.Path.of(System.getProperty("java.io.tmpdir"));} } }',
 'tn/nightbeam/ras/Constants.java':
  'package tn.nightbeam.ras; public final class Constants { public static final Log LOG=new Log();'
  ' public static final class Log { public void warn(String message,Object... args){} } }',
}
UI_SOURCES=['SoulsLayout.java','AttributeAllocationPreview.java','RasGuiNumbers.java','PendingAllocation.java','RasMotion.java',
            'RasPalette.java','RasUiSettings.java','RasTexts.java','RasLiveConfig.java','BannerLayout.java']
for root in ['1.20.1','1.21.1','26.1.2','26.2','26.3']:
    source=repo/root/'common/src/main/java'
    if root=='26.2':source/='java'
    package=source/'tn/nightbeam/ras/client/gui'
    test=repo/root/'common/src/test/java/tn/nightbeam/ras/client/gui/RasGuiLayoutTest.java'
    # Exercise the actual helpers below, and guard the screen's presentation wiring.
    style=(package/'RasGuiStyle.java').read_text()
    screen=(package/'PlayerStatsGUIScreen.java').read_text()
    pages=(package/'RasPages.java').read_text()
    model=(package/'RasStatsModel.java').read_text()
    assert 'new DecimalFormat("0.##").format(value)' in style, f'{root}: compact row precision changed'
    assert 'preciseNumber(double value) { return RasGuiNumbers.tooltip(value); }' in style
    # Texts come from texts.json since 5.1; the values filled into them must stay precise.
    assert '"value", RasGuiStyle.preciseNumber(PendingAllocation.valueAfter(row.model(), staged + 1))' in screen
    assert 'tip.add(RasTexts.format("tooltip.value", "name", row.name(), "value", RasGuiStyle.preciseNumber(row.current())))' in screen
    assert 'String[] values = RasGuiNumbers.pair(row.current(), after);' in screen, f'{root}: rows must show visible changes'
    assert 'new AttributeAllocationPacket(inFlight)' in screen, f'{root}: Confirm must send one atomic request'
    assert 'RasTexts.format("tooltip.initial_value", "value", RasGuiStyle.preciseNumber(row.data().initValue))' in pages
    assert 'tooltip.add(RasTexts.format("tooltip.value", "name", label, "value", RasGuiStyle.preciseNumber(current)));' in model
    network=source/'tn/nightbeam/ras/network'
    with tempfile.TemporaryDirectory(prefix='ras-ui-') as output, tempfile.TemporaryDirectory(prefix='ras-ui-stubs-') as stubs:
        for name,body in STUBS.items():
            path=Path(stubs)/name
            path.parent.mkdir(parents=True,exist_ok=True)
            path.write_text(body)
        subprocess.run([args.javac,'--release','17','-encoding','UTF-8','-cp',args.gson_jar,'-d',output,
                        *(str(package/name) for name in UI_SOURCES),
                        str(source/'tn/nightbeam/ras/config/ConfigFiles.java'),str(source/'tn/nightbeam/ras/config/UiConfigDefaults.java'),
                        *(str(Path(stubs)/name) for name in STUBS),
                        str(network/'StatPreview.java'),str(test)],check=True)
        subprocess.run([args.java,'-cp',os.pathsep.join([output,args.gson_jar]),'tn.nightbeam.ras.client.gui.RasGuiLayoutTest'],check=True)
        print(f'PASS {root}: actual layout/staging/preview geometry, numbers, motion, settings, texts and banner',flush=True)
