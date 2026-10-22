#!/usr/bin/env python3
"""Read-only provenance for CurseForge-only recovery of the immutable v5.0.0 release.

Reuses the reviewed 4.3.0 recovery checks with the 5.0.0 lock and helper workflow:
exact tag, source run, artifact digests and the latest attempted journal.
"""
import importlib.util
import json
from pathlib import Path

spec = importlib.util.spec_from_file_location('recovery_430', Path(__file__).with_name('validate-430-publisher-recovery.py'))
recovery = importlib.util.module_from_spec(spec)
spec.loader.exec_module(recovery)
recovery.HELPER_WORKFLOW = '.github/workflows/recover-500-publication.yml'
recovery.HELPER_NAME = 'Recover RAS 5.0.0 publication'


def load_lock():
    lock = json.loads(Path(__file__).with_name('release-500-recovery-lock.json').read_text())
    if lock.get('schema_version') != 1 or lock.get('repository') != recovery.original.REPOSITORY \
            or lock.get('source_commit') != 'c8e06b8412c7e4ee6c7985411c14a73f032549af' \
            or lock.get('release_version') != '5.0.0' or lock.get('tag') != 'v5.0.0' \
            or lock.get('source_run_id') != 39739461491 or lock.get('minimum_receipt_run_id') != 39739461491:
        raise ValueError('Recovery lock identity differs')
    return lock


recovery.load_lock = load_lock

if __name__ == '__main__':
    try:
        recovery.main()
    except Exception:
        print('Publisher recovery provenance failed; no store write is permitted.')
        raise SystemExit(1)
