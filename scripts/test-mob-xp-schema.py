#!/usr/bin/env python3
"""Check the published mob VP schema and studio/runtime parity fixtures."""
import json
import math
from pathlib import Path
from jsonschema import Draft202012Validator

root=Path(__file__).resolve().parents[1]
schema=json.loads((root/'docs/configuration/mob-xp.schema.json').read_text())
Draft202012Validator.check_schema(schema)
validator=Draft202012Validator(schema)
fixtures=json.loads((root/'tests/fixtures/mob-xp-contract.json').read_text())
for case in fixtures['cases']:
    validator.validate(case['config'])
    ids=[rule['id'] for rule in case['config'].get('rules',[])]
    assert len(ids)==len(set(ids)),case['name']
    assert math.isfinite(case['expected_vp']) and case['expected_vp']>=0,case['name']
validator.validate(json.loads((root/'tests/fixtures/mob-xp-builder-export.json').read_text()))
print(f"PASS schema and {len(fixtures['cases'])} shared runtime/studio fixtures + actual builder export")
