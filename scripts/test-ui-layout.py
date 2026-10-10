#!/usr/bin/env python3
"""Run actual native-pixel layout/allocation-preview/number regressions for all five roots.

These are geometry/source regressions, not screenshot or client rendering tests.
"""
import argparse
import os
from pathlib import Path
import subprocess
import tempfile

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--javac',default='javac')
parser.add_argument('--java',default='java')
args=parser.parse_args()
repo=Path(__file__).resolve().parents[1]
for root in ['1.20.1','1.21.1','26.1.2','26.2','26.3']:
    source=repo/root/'common/src/main/java'
    if root=='26.2':source/='java'
    package=source/'tn/nightbeam/ras/client/gui'
    test=repo/root/'common/src/test/java/tn/nightbeam/ras/client/gui/RasGuiLayoutTest.java'
    # Exercise the actual helper below, and guard the screen's presentation wiring.
    style=(package/'RasGuiStyle.java').read_text()
    screen=(package/'PlayerStatsGUIScreen.java').read_text()
    overview=(package/'PlayerStatsOverviewScreen.java').read_text()
    assert 'new DecimalFormat("0.##").format(value)' in style, f'{root}: compact row precision changed'
    assert 'preciseNumber(double value) { return RasGuiNumbers.tooltip(value); }' in style
    assert '"Next value: " + RasGuiStyle.preciseNumber(preview.value())' in screen
    assert 'RasGuiStyle.preciseNumber(value(button.attributeId))' in screen
    assert 'lines.add(attributeName(id) + ": " + RasGuiStyle.preciseNumber(value(id)))' in screen
    assert 'String value = RasGuiStyle.ellipsis(font, RasGuiStyle.number(value(id)), 60)' in screen
    assert 'lines.add(name(data, id) + ": " + RasGuiStyle.preciseNumber(current))' in overview
    with tempfile.TemporaryDirectory(prefix='ras-ui-') as output:
        subprocess.run([args.javac,'--release','17','-d',output,str(package/'PixelRpgBookLayout.java'),
                        str(package/'AttributeAllocationPreview.java'),str(package/'RasGuiNumbers.java'),str(test)],check=True)
        subprocess.run([args.java,'-cp',output,'tn.nightbeam.ras.client.gui.RasGuiLayoutTest'],check=True)
        print(f'PASS {root}: actual layout/allocation preview geometry and tooltip numbers',flush=True)
