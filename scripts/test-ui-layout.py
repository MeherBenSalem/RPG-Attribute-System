#!/usr/bin/env python3
"""Run actual native-pixel layout, staging, preview, number and motion regressions for all five roots.

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
    # Exercise the actual helpers below, and guard the screen's presentation wiring.
    style=(package/'RasGuiStyle.java').read_text()
    screen=(package/'PlayerStatsGUIScreen.java').read_text()
    pages=(package/'RasPages.java').read_text()
    model=(package/'RasStatsModel.java').read_text()
    assert 'new DecimalFormat("0.##").format(value)' in style, f'{root}: compact row precision changed'
    assert 'preciseNumber(double value) { return RasGuiNumbers.tooltip(value); }' in style
    assert '"Next value: " + RasGuiStyle.preciseNumber(PendingAllocation.valueAfter(row.model(), staged + 1))' in screen
    assert 'tip.add(row.name() + ": " + RasGuiStyle.preciseNumber(row.current()))' in screen
    assert 'String[] values = RasGuiNumbers.pair(row.current(), after);' in screen, f'{root}: rows must show visible changes'
    assert 'new AttributeAllocationPacket(inFlight)' in screen, f'{root}: Confirm must send one atomic request'
    assert '"Initial value: " + RasGuiStyle.preciseNumber(row.data().initValue)' in pages
    assert 'tooltip.add(label + ": " + RasGuiStyle.preciseNumber(current));' in model
    network=source/'tn/nightbeam/ras/network'
    with tempfile.TemporaryDirectory(prefix='ras-ui-') as output:
        subprocess.run([args.javac,'--release','17','-d',output,*(str(package/name) for name in
                        ['SoulsLayout.java','AttributeAllocationPreview.java','RasGuiNumbers.java','PendingAllocation.java','RasMotion.java']),
                        str(network/'StatPreview.java'),str(test)],check=True)
        subprocess.run([args.java,'-cp',output,'tn.nightbeam.ras.client.gui.RasGuiLayoutTest'],check=True)
        print(f'PASS {root}: actual layout/staging/preview geometry, numbers and motion',flush=True)
