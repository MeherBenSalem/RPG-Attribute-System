#!/usr/bin/env python3
"""Compile and run actual pure rules/cache source for every isolated version root.

Use --gson-jar PATH (a Minecraft/Gradle cached Gson jar) or GSON_JAR.
This focused check does not replace complete loader builds or in-game tests.
"""
import argparse
import os
from pathlib import Path
import subprocess
import tempfile

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--gson-jar', default=os.environ.get('GSON_JAR'))
parser.add_argument('--javac', default='javac')
parser.add_argument('--java', default='java')
args = parser.parse_args()
if not args.gson_jar or not Path(args.gson_jar).is_file():
    parser.error('Supply --gson-jar PATH or GSON_JAR pointing to a cached com.google.code.gson jar')
repo = Path(__file__).resolve().parents[1]
for root in ['1.20.1', '1.21.1', '26.1.2', '26.2', '26.3']:
    source = repo / root / 'common/src/main/java'
    if root == '26.2':
        source /= 'java'
    package = source / 'tn/nightbeam/ras/config'
    test = repo / root / 'common/src/test/java/tn/nightbeam/ras/config/MobXpRulesTest.java'
    with tempfile.TemporaryDirectory(prefix='ras-xp-') as output:
        subprocess.run([args.javac, '--release', '17', '-cp', args.gson_jar, '-d', output,
                        str(package / 'MobXpRules.java'), str(package / 'MobXpRuleStore.java'), str(test)], check=True)
        subprocess.run([args.java, '-cp', output + os.pathsep + args.gson_jar,
                        'tn.nightbeam.ras.config.MobXpRulesTest',
                        str(repo / 'tests/fixtures/mob-xp-contract.json')], check=True)
        print(f'PASS {root}: actual rules/cache source', flush=True)
