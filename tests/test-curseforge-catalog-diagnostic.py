#!/usr/bin/env python3
"""No network or real credentials. Only explicit public catalog facts may escape."""
import importlib.util
import json
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location('catalog', Path(__file__).parents[1] / 'scripts/diagnose-curseforge-catalog.py')
catalog = importlib.util.module_from_spec(spec)
spec.loader.exec_module(catalog)


class DiagnosticTests(unittest.TestCase):
    def fixture(self):
        ids = {name: index + 100 for index, name in enumerate(catalog.REQUESTED)}
        upload = [{'id': ids[name], 'name': name, 'slug': name, 'gameVersionTypeID': 10} for name in catalog.REQUESTED]
        # A same-name foreign namespace is evidence, never silently selected.
        upload.append({'id': 200, 'name': '1.20.1', 'slug': '1-20-1', 'gameVersionTypeID': 20})
        payloads = {
            'https://minecraft.curseforge.com/api/game/versions': upload,
            'https://api.curseforge.com/v1/mods/1079687': {'data': {'id': 1079687, 'gameId': 432, 'private': 'DO-NOT-EMIT'}},
            'https://api.curseforge.com/v1/games/432': {'data': {'id': 432, 'slug': 'minecraft', 'private': 'DO-NOT-EMIT'}},
            'https://api.curseforge.com/v1/games/432/version-types': {'data': [
                {'id': 10, 'gameId': 432, 'name': 'Minecraft', 'slug': 'minecraft', 'status': 1, 'private': 'DO-NOT-EMIT'},
                {'id': 20, 'gameId': 432, 'name': 'Foreign namespace', 'slug': 'foreign', 'status': 1}]},
            'https://api.curseforge.com/v2/games/432/versions': {'data': [
                {'type': 10, 'versions': [{'id': ids[name], 'name': name, 'slug': name, 'private': 'DO-NOT-EMIT'} for name in catalog.REQUESTED]}]},
        }
        for name in catalog.MINECRAFT:
            payloads['https://api.curseforge.com/v1/minecraft/version/' + name] = {'data': {
                'id': ids[name] + 500, 'gameVersionId': ids[name], 'gameVersionTypeId': 10,
                'versionString': name, 'approved': True, 'gameVersionStatus': 1, 'gameVersionTypeStatus': 1,
                'jarDownloadUrl': 'DO-NOT-EMIT', 'jsonDownloadUrl': 'DO-NOT-EMIT'}}
        calls = []
        def reader(url, headers):
            calls.append((url, headers))
            self.assertIn(url, payloads)
            return 200, payloads[url]
        return payloads, calls, reader

    def test_exact_public_facts_only_without_selection(self):
        _, calls, reader = self.fixture()
        result = catalog.check({'CURSEFORGE_TOKEN': 'fake-upload-token', 'CURSEFORGE_API_KEY': 'fake-core-key', 'GITHUB_SHA': 'a' * 40}, reader)
        self.assertTrue(result['complete'])
        self.assertTrue(result['read_only'])
        self.assertFalse(result['selection_performed'])
        self.assertEqual(len(calls), 10)
        self.assertEqual(len(result['upload_candidates']['1.20.1']), 2)
        self.assertEqual(result['core_minecraft']['1.20.1']['id'], 600)
        self.assertEqual(result['core_minecraft']['1.20.1']['gameVersionId'], 100)
        output = json.dumps(result)
        for forbidden in ['DO-NOT-EMIT', 'fake-upload-token', 'fake-core-key', 'jarDownloadUrl', 'Authorization']:
            self.assertNotIn(forbidden, output)
        for url, headers in calls:
            if 'minecraft.curseforge.com' in url:
                self.assertEqual(headers, {'X-Api-Token': 'fake-upload-token'})
            else:
                self.assertEqual(headers, {'x-api-key': 'fake-core-key'})

    def test_no_credentials_means_no_network(self):
        def forbidden(*args):
            self.fail('No credential means no request')
        result = catalog.check({}, forbidden)
        self.assertFalse(result['complete'])
        self.assertEqual(len(result['endpoints']), 10)
        self.assertTrue(all(row == {'error': 'credential-missing'} for row in result['endpoints'].values()))

    def test_http_and_exception_bodies_are_not_disclosed(self):
        for failure in ['http', 'exception']:
            def reader(*args):
                if failure == 'http':
                    return 403, {'secret': 'DO-NOT-EMIT'}
                raise RuntimeError('DO-NOT-EMIT')
            result = catalog.check({'CURSEFORGE_TOKEN': 't', 'CURSEFORGE_API_KEY': 'k'}, reader)
            self.assertFalse(result['complete'])
            self.assertNotIn('DO-NOT-EMIT', json.dumps(result))

    def test_malformed_ids_canonical_names_and_bounds_fail_closed(self):
        for mutation in ['bool id', 'missing type', 'wrong canonical', 'oversized', 'wrong game']:
            payloads, _, reader = self.fixture()
            if mutation == 'bool id':
                payloads['https://minecraft.curseforge.com/api/game/versions'][0]['id'] = True
            elif mutation == 'missing type':
                del payloads['https://minecraft.curseforge.com/api/game/versions'][0]['gameVersionTypeID']
            elif mutation == 'wrong canonical':
                payloads['https://api.curseforge.com/v1/minecraft/version/1.20.1']['data']['versionString'] = '1.20.2'
            elif mutation == 'wrong game':
                payloads['https://api.curseforge.com/v1/mods/1079687']['data']['gameId'] = 999
            else:
                payloads['https://minecraft.curseforge.com/api/game/versions'] *= catalog.MAX_ROWS
            result = catalog.check({'CURSEFORGE_TOKEN': 'fake', 'CURSEFORGE_API_KEY': 'fake'}, reader)
            self.assertFalse(result['complete'], mutation)

    def test_workflow_is_owner_only_and_separate_from_publication(self):
        source = (Path(__file__).parents[1] / '.github/workflows/curseforge-catalog-diagnostic.yml').read_text()
        self.assertIn("github.actor_id == '74470806'", source)
        self.assertIn("github.repository == 'MeherBenSalem/RPG-Attribute-System'", source)
        self.assertIn('timeout-minutes: 5', source)
        self.assertIn('persist-credentials: false', source)
        for forbidden in ['publish-verified-release', 'upload-file', 'MODRINTH_TOKEN', 'contents: write', 'gradlew']:
            self.assertNotIn(forbidden, source)

    def test_arbitrary_strings_and_echoed_credentials_are_removed(self):
        payloads, _, reader = self.fixture()
        payloads['https://api.curseforge.com/v1/games/432/version-types']['data'][0]['name'] = 'fake-core-key'
        payloads['https://minecraft.curseforge.com/api/game/versions'][0]['slug'] = 'DO-NOT-EMIT\n<unsafe>'
        result = catalog.check({'CURSEFORGE_TOKEN': 'fake-upload-token', 'CURSEFORGE_API_KEY': 'fake-core-key'}, reader)
        self.assertIsNone(result['namespace_facts']['version_types'][0]['name'])
        self.assertIsNone(result['upload_candidates']['1.20.1'][0]['slug'])
        self.assertNotIn('fake-core-key', json.dumps(result))
        self.assertNotIn('DO-NOT-EMIT', json.dumps(result))


if __name__ == '__main__':
    unittest.main()
