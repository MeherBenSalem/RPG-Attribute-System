#!/usr/bin/env python3
"""Run actual native-pixel layout/allocation-preview regressions for all five roots.

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
    with tempfile.TemporaryDirectory(prefix='ras-ui-') as output:
        subprocess.run([args.javac,'--release','17','-d',output,str(package/'PixelRpgBookLayout.java'),
                        str(package/'AttributeAllocationPreview.java'),str(test)],check=True)
        subprocess.run([args.java,'-cp',output,'tn.nightbeam.ras.client.gui.RasGuiLayoutTest'],check=True)
        print(f'PASS {root}: actual layout/allocation preview geometry',flush=True)
