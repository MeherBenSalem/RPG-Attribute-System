#!/usr/bin/env python3
"""Bounded GET-only catalog evidence. Existing credentials are used only in CI.

Official schemas:
https://support.curseforge.com/support/solutions/articles/9000197321-curseforge-upload-api
https://docs.curseforge.com/rest-api/#get-specific-minecraft-version
https://docs.curseforge.com/rest-api/#get-version-types
https://docs.curseforge.com/rest-api/#get-versions---v2
No raw responses, credentials, headers, download URLs or account facts are emitted.
This diagnostic never selects upload IDs, edits a journal or performs a store write.
"""
import json
import os
import re
import urllib.error
import urllib.request
from pathlib import Path

MINECRAFT = ['1.20.1', '1.21.1', '26.1.2', '26.2', '26.3']
REQUESTED = MINECRAFT + ['Fabric', 'Forge', 'NeoForge', 'Client', 'Server']
PROJECT_ID = 1079687
GAME_ID = 432
MAX_BYTES = 10 * 1024 * 1024
MAX_ROWS = 100000
MAX_TYPES = 512
MAX_MATCHES = 256


class DiagnosticError(Exception):
    """Only static error codes may reach the report."""


def positive_id(value):
    return type(value) is int and 0 < value <= 0xffffffff


def safe_name(value, secrets=()):
    if not isinstance(value, str) or not re.fullmatch(r'[A-Za-z0-9 ._+()/:-]{1,128}', value):
        return None
    if any(secret and secret in value for secret in secrets):
        return None
    return value


def read(url, headers):
    # All callers use fixed official endpoints. Redirects are rejected so a token
    # cannot be forwarded outside its intended provider or into a login flow.
    class NoRedirect(urllib.request.HTTPRedirectHandler):
        def redirect_request(self, req, fp, code, msg, response_headers, newurl):
            return None
    request = urllib.request.Request(url, method='GET', headers={**headers,
        'Accept': 'application/json', 'User-Agent': 'NightBeam-RAS-catalog-diagnostic'})
    try:
        with urllib.request.build_opener(NoRedirect).open(request, timeout=20) as response:
            body = response.read(MAX_BYTES + 1)
            if len(body) > MAX_BYTES:
                raise DiagnosticError('response-size-bound')
            try:
                return response.status, json.loads(body)
            except (ValueError, UnicodeError):
                raise DiagnosticError('malformed-json') from None
    except urllib.error.HTTPError as error:
        return error.code, None
    except DiagnosticError:
        raise
    except Exception:
        raise DiagnosticError('network-error') from None


def rows(payload, *, upload=False, maximum=MAX_ROWS):
    result = payload if upload and isinstance(payload, list) else payload.get('data') if isinstance(payload, dict) else None
    if not isinstance(result, list) or len(result) > maximum:
        raise DiagnosticError('malformed-or-oversized-catalog')
    return result


def check(environment, reader=read):
    token, key = environment.get('CURSEFORGE_TOKEN', ''), environment.get('CURSEFORGE_API_KEY', '')
    secrets = (token, key)
    sha = environment.get('GITHUB_SHA', '')
    report = {'schema_version': 1, 'read_only': True, 'selection_performed': False,
              'helper_commit': sha if re.fullmatch(r'[0-9a-f]{40}', sha) else None,
              'requested_names': REQUESTED, 'endpoints': {}, 'upload_candidates': {},
              'core_minecraft': {}, 'namespace_facts': {}, 'core_candidates': {},
              'complete': False}

    def fetch(label, url, headers, present):
        if not present:
            report['endpoints'][label] = {'error': 'credential-missing'}
            return None
        try:
            status, payload = reader(url, headers)
            if type(status) is not int or not 100 <= status <= 599:
                raise DiagnosticError('malformed-status')
            report['endpoints'][label] = {'http_status': status}
            return payload if status == 200 else None
        except DiagnosticError as error:
            code = str(error)
            report['endpoints'][label] = {'error': code if code in [
                'response-size-bound', 'malformed-json', 'network-error'] else 'diagnostic-read-error'}
            return None
        except Exception:
            report['endpoints'][label] = {'error': 'diagnostic-read-error'}
            return None

    def malformed(label, code='malformed-schema'):
        report['endpoints'][label]['error'] = code

    payload = fetch('upload_versions', 'https://minecraft.curseforge.com/api/game/versions',
                    {'X-Api-Token': token}, bool(token))
    if payload is not None:
        try:
            catalog = rows(payload, upload=True)
            matches = [row for row in catalog if isinstance(row, dict) and row.get('name') in REQUESTED]
            if len(matches) > MAX_MATCHES:
                raise DiagnosticError('candidate-bound')
            for name in REQUESTED:
                selected = [row for row in matches if row.get('name') == name]
                if any(not positive_id(row.get('id')) or not positive_id(row.get('gameVersionTypeID')) for row in selected):
                    raise DiagnosticError('malformed-schema')
                report['upload_candidates'][name] = [
                    {'id': row['id'], 'gameVersionTypeID': row['gameVersionTypeID'], 'name': name,
                     'slug': safe_name(row.get('slug'), secrets)} for row in selected]
        except DiagnosticError:
            malformed('upload_versions')

    payload = fetch('project_namespace', f'https://api.curseforge.com/v1/mods/{PROJECT_ID}', {'x-api-key': key}, bool(key))
    project = payload.get('data') if isinstance(payload, dict) else None
    if payload is not None:
        if isinstance(project, dict) and project.get('id') == PROJECT_ID and project.get('gameId') == GAME_ID:
            report['namespace_facts']['project'] = {'id': PROJECT_ID, 'gameId': GAME_ID}
        else:
            malformed('project_namespace')

    payload = fetch('game_namespace', f'https://api.curseforge.com/v1/games/{GAME_ID}', {'x-api-key': key}, bool(key))
    game = payload.get('data') if isinstance(payload, dict) else None
    if payload is not None:
        if isinstance(game, dict) and game.get('id') == GAME_ID and game.get('slug') == 'minecraft':
            report['namespace_facts']['game'] = {'id': GAME_ID, 'slug': 'minecraft'}
        else:
            malformed('game_namespace')

    payload = fetch('version_types', f'https://api.curseforge.com/v1/games/{GAME_ID}/version-types', {'x-api-key': key}, bool(key))
    if payload is not None:
        try:
            types = rows(payload, maximum=MAX_TYPES)
            if any(not isinstance(row, dict) or not positive_id(row.get('id')) or row.get('gameId') != GAME_ID
                   or type(row.get('status')) is not int or row['status'] not in [1, 2] for row in types):
                raise DiagnosticError('malformed-schema')
            if len({row['id'] for row in types}) != len(types):
                raise DiagnosticError('malformed-schema')
            report['namespace_facts']['version_types'] = [
                {'id': row['id'], 'gameId': GAME_ID, 'name': safe_name(row.get('name'), secrets),
                 'slug': safe_name(row.get('slug'), secrets), 'status': row['status']} for row in types]
        except DiagnosticError:
            malformed('version_types')

    payload = fetch('core_versions_v2', f'https://api.curseforge.com/v2/games/{GAME_ID}/versions', {'x-api-key': key}, bool(key))
    if payload is not None:
        try:
            groups = rows(payload, maximum=MAX_TYPES)
            candidates = []
            total = 0
            for group in groups:
                if not isinstance(group, dict) or not positive_id(group.get('type')) or not isinstance(group.get('versions'), list):
                    raise DiagnosticError('malformed-schema')
                total += len(group['versions'])
                if total > MAX_ROWS:
                    raise DiagnosticError('catalog-bound')
                for row in group['versions']:
                    if isinstance(row, dict) and row.get('name') in REQUESTED:
                        if not positive_id(row.get('id')):
                            raise DiagnosticError('malformed-schema')
                        candidates.append({'id': row['id'], 'gameVersionTypeId': group['type'], 'name': row['name'],
                                           'slug': safe_name(row.get('slug'), secrets)})
            if len(candidates) > MAX_MATCHES:
                raise DiagnosticError('candidate-bound')
            report['core_candidates'] = {name: [row for row in candidates if row['name'] == name] for name in REQUESTED}
        except DiagnosticError:
            malformed('core_versions_v2')

    for name in MINECRAFT:
        label = 'minecraft_' + name
        payload = fetch(label, 'https://api.curseforge.com/v1/minecraft/version/' + name, {'x-api-key': key}, bool(key))
        data = payload.get('data') if isinstance(payload, dict) else None
        if payload is None:
            continue
        if not isinstance(data, dict) or data.get('versionString') != name or not all(
                positive_id(data.get(field)) for field in ['id', 'gameVersionId', 'gameVersionTypeId']) \
                or type(data.get('approved')) is not bool or type(data.get('gameVersionStatus')) is not int \
                or data['gameVersionStatus'] not in [1, 2, 3] or type(data.get('gameVersionTypeStatus')) is not int \
                or data['gameVersionTypeStatus'] not in [1, 2]:
            malformed(label)
            continue
        report['core_minecraft'][name] = {field: data[field] for field in [
            'id', 'gameVersionId', 'versionString', 'gameVersionTypeId', 'approved', 'gameVersionStatus', 'gameVersionTypeStatus']}

    # Retain namespace descriptions only for IDs relevant to exact requested names.
    referenced = {row['gameVersionTypeID'] for matches in report['upload_candidates'].values() for row in matches}
    referenced.update(row['gameVersionTypeId'] for matches in report['core_candidates'].values() for row in matches)
    referenced.update(row['gameVersionTypeId'] for row in report['core_minecraft'].values())
    if 'version_types' in report['namespace_facts']:
        report['namespace_facts']['version_types'] = [row for row in report['namespace_facts']['version_types'] if row['id'] in referenced]

    report['complete'] = len(report['endpoints']) == 10 and all(
        row.get('http_status') == 200 and 'error' not in row for row in report['endpoints'].values())
    return report


def main():
    report = check(os.environ)
    Path('curseforge-catalog-diagnostic.json').write_text(json.dumps(report, indent=2) + '\n')
    print(json.dumps(report, sort_keys=True))
    return 0 if report['complete'] else 1


if __name__ == '__main__':
    raise SystemExit(main())
