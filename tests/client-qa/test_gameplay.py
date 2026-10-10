"""Offline evidence validation only. These tests never claim a Minecraft runtime pass."""
import copy
import importlib.util
from pathlib import Path
import time
import unittest
from unittest.mock import Mock

ROOT = Path(__file__).resolve().parents[2]
spec = importlib.util.spec_from_file_location('qa_gameplay', ROOT / 'scripts/client-qa/run.py')
qa = importlib.util.module_from_spec(spec)
spec.loader.exec_module(qa)


class GameplayEvidenceTests(unittest.TestCase):
    def setUp(self):
        self.now = int(time.time() * 1000)
        self.snapshot = {'total_xp': 0, 'level': 0, 'spare_points': 5, 'modifier': 1,
                         'attributes': {'attribute_5': .1025}, 'attribute_points': {'attribute_5': 1}}
        self.data = {'schema_version': 1, 'status': 'PASS', 'run_id': 'real-run-123',
                     'source_sha': 'a' * 40, 'request_id': 'request-123', 'action': 'kill-entity',
                     'written_at_ms': self.now, 'before': copy.deepcopy(self.snapshot),
                     'after': {**copy.deepcopy(self.snapshot), 'total_xp': 31.5},
                     'mob': 'minecraft:skeleton', 'real_tag_match': True,
                     'difficulty': 'hard', 'armor_points': 2, 'expected_reward': 31.5,
                     'observed_reward': 31.5, 'normal_loader_death_event_verified': True}

    def validate(self, data, action='kill-entity'):
        return qa.validate_gameplay(data, run_id='real-run-123', sha='a' * 40,
                                   request_id='request-123', action=action, not_before_ms=self.now - 1)

    def test_complete_death_evidence(self):
        self.validate(self.data)

    def test_false_positive_identity_reward_and_snapshot_fields_fail(self):
        mutations = {'schema_version': 2, 'status': 'FAIL', 'run_id': 'old-run-123',
                     'source_sha': 'b' * 40, 'request_id': 'old-request', 'action': 'kill-tag',
                     'written_at_ms': self.now - 1000, 'before': None, 'after': {},
                     'mob': 'minecraft:cow', 'real_tag_match': False, 'difficulty': 'normal',
                     'armor_points': 0, 'expected_reward': 30, 'observed_reward': 30,
                     'normal_loader_death_event_verified': False}
        for field, value in mutations.items():
            with self.subTest(field=field):
                data = copy.deepcopy(self.data)
                data[field] = value
                with self.assertRaises(qa.QaError):
                    self.validate(data)

    def test_snapshot_delta_must_match_actual_reward(self):
        data = copy.deepcopy(self.data)
        data['after']['total_xp'] = 0
        with self.assertRaises(qa.QaError):
            self.validate(data)

    def test_nonfinite_boolean_missing_and_extra_values_fail(self):
        for value in (float('nan'), float('inf'), None, '31.5', True):
            with self.subTest(value=value):
                data = copy.deepcopy(self.data)
                data['after']['total_xp'] = value
                with self.assertRaises(qa.QaError):
                    self.validate(data)
        data = copy.deepcopy(self.data)
        data['after']['synthetic_field'] = True
        with self.assertRaises(qa.QaError):
            self.validate(data)

    def test_tag_invalid_reload_and_legacy_rewards(self):
        for action, expected in [('kill-tag', 33.75), ('invalid-reload', 33.75), ('kill-legacy', 20)]:
            with self.subTest(action=action):
                data = copy.deepcopy(self.data)
                data.update(action=action, expected_reward=expected, observed_reward=expected)
                data['after']['total_xp'] = expected
                data['invalid_reload_retained_previous_rules'] = True
                self.validate(data, action)
                if action == 'invalid-reload':
                    data.pop('invalid_reload_retained_previous_rules')
                    with self.assertRaises(qa.QaError):
                        self.validate(data, action)

    def test_real_ui_allocation_requires_server_point_value_and_spare_count(self):
        data = copy.deepcopy(self.data)
        data.update(action='verify-allocation', normal_ui_packet_allocation_verified=True, expected_agility=.1025, server_movement_speed_base=.1025, server_movement_speed_value=.1025)
        self.validate(data, 'verify-allocation')
        for field in ('server_movement_speed_base', 'server_movement_speed_value'):
            broken = copy.deepcopy(data)
            broken[field] = .1
            with self.assertRaises(qa.QaError):
                self.validate(broken, 'verify-allocation')
        for field, value in [('spare_points', 6), ('attributes', {'attribute_5': .1}), ('attribute_points', {'attribute_5': 0})]:
            broken = copy.deepcopy(data)
            broken['after'][field] = value
            with self.assertRaises(qa.QaError):
                self.validate(broken, 'verify-allocation')

    def test_seed_is_explicit_fixture_setup(self):
        data = copy.deepcopy(self.data)
        data.update(action='seed', fixture_setup_only=True)
        self.validate(data, 'seed')
        data.pop('fixture_setup_only')
        with self.assertRaises(qa.QaError):
            self.validate(data, 'seed')

    def test_synced_client_snapshot_must_match_exact_server_snapshot(self):
        self.assertTrue(qa.snapshots_equal(self.snapshot, copy.deepcopy(self.snapshot)))
        for mutation in ({'total_xp': 1}, {'attributes': {'attribute_5': .1}}, {'attribute_points': {}}, {'modifier': None}):
            self.assertFalse(qa.snapshots_equal({**self.snapshot, **mutation}, self.snapshot))

    def test_seed_preview_wait_rejects_real_reset_before_widget_refresh(self):
        ready = {'state': 'ALLOCATION', 'player_variables': {
            'attributes': {'attribute_5': .1}, 'attribute_points': {'attribute_5': 0}, 'spare_points': 6},
            'buttons': [{'label': 'Allocate Agility. Next value: 0.105 · 1 point (max: 500)', 'active': True}]}
        self.assertFalse(qa.seeded_agility_preview_ready(ready))
        ready['buttons'][0]['label'] = 'Allocate Agility. Next value: 0.1025 · 1 point (max: 500)'
        self.assertTrue(qa.seeded_agility_preview_ready(ready))
        for label in ('Allocate Agility. Next value: 0.1 · 1 point',
                      'Allocate Agility. Next value: 0.10256 · 1 point'):
            broken = copy.deepcopy(ready); broken['buttons'][0]['label'] = label
            self.assertFalse(qa.seeded_agility_preview_ready(broken))

    def test_seed_preview_requires_active_unique_control_and_actual_reset_values(self):
        ready = {'state': 'ALLOCATION', 'player_variables': {
            'attributes': {'attribute_5': .1}, 'attribute_points': {'attribute_5': 0}, 'spare_points': 6},
            'buttons': [{'label': 'Allocate Agility. Next value: 0.1025 · 1 point (max: 500)', 'active': True}]}
        for field, value in (('attributes', {'attribute_5': .1025}), ('attribute_points', {'attribute_5': 1}), ('spare_points', 5)):
            broken = copy.deepcopy(ready); broken['player_variables'][field] = value
            self.assertFalse(qa.seeded_agility_preview_ready(broken))
        for field, value in (('state', 'COMBAT'), ('player_variables', None), ('buttons', None)):
            self.assertFalse(qa.seeded_agility_preview_ready({**ready, field: value}))
        broken = copy.deepcopy(ready); broken['buttons'][0]['active'] = False
        self.assertFalse(qa.seeded_agility_preview_ready(broken))
        broken = copy.deepcopy(ready); broken['buttons'] *= 2
        self.assertFalse(qa.seeded_agility_preview_ready(broken))

    def test_physical_page_pair_awaits_both_exact_pages_and_records_only_observed_transitions(self):
        driver = qa.Driver.__new__(qa.Driver)
        driver.paging_exercised = []
        ready = {'state': 'COMBAT', 'page': 0, 'sequence': 10,
                 'buttons': [{'label': 'Next combat-stat page', 'active': True}]}
        paged = {**ready, 'page': 1, 'sequence': 11}
        returned = {**ready, 'sequence': 12}
        driver.click = Mock(); driver.stable_capture = Mock()
        driver.wait_ready = Mock(side_effect=[paged, returned])
        self.assertIs(driver.exercise_page_pair(ready, 'Next combat-stat page', 'Previous combat-stat page', 'combat'), returned)
        self.assertEqual(driver.click.call_args_list[0].args, (ready, 'Next combat-stat page'))
        self.assertEqual(driver.click.call_args_list[1].args, (paged, 'Previous combat-stat page'))
        self.assertEqual(driver.wait_ready.call_args_list[0].kwargs, {'since': 10, 'page': 1})
        self.assertEqual(driver.wait_ready.call_args_list[1].kwargs, {'since': 11, 'page': 0})
        self.assertEqual(driver.paging_exercised, [{'state': 'COMBAT', 'from_page': 0, 'next_page': 1, 'returned_page': 0}])
        ready['buttons'][0]['active'] = False
        self.assertIs(driver.exercise_page_pair(ready, 'Next combat-stat page', 'Previous combat-stat page', 'combat'), ready)
        self.assertEqual(driver.click.call_count, 2)

    def test_source_hooks_are_bounded_default_off_real_death_not_direct_procedure(self):
        for version in ('1.21.1', '26.3'):
            source = (ROOT / version / 'common/src/main/java/tn/nightbeam/ras/client/RasClientGameplayQa.java').read_text()
            self.assertIn('!Boolean.getBoolean("ras.clientQa")', source)
            self.assertIn('directory.getParent().getFileName().toString().equals("client-qa")', source)
            self.assertIn('config.equals(directory.resolve("config"))', source)
            self.assertIn('server.getPlayerList().getPlayers().size() != 1', source)
            self.assertIn('level.addFreshEntity(victim)', source)
            self.assertIn('player.damageSources().playerAttack(player)', source)
            self.assertNotIn('GameplayRulesProcedure.handleEntityKill', source)
            self.assertNotIn('AddPointsAttributeGenericProcedure.execute', source)
            self.assertNotIn('LevelingService.addXp', source)
            self.assertNotIn('eula=true', source)
        calendar = (ROOT / '26.3/common/src/main/java/tn/nightbeam/ras/client/RasClientGameplayQa.java').read_text()
        self.assertNotIn('player.serverLevel()', calendar)
        self.assertNotIn('EntityType.SKELETON', calendar)
        self.assertIn('BuiltInRegistries.ENTITY_TYPE.getKey(type)', calendar)
        self.assertIn('skeleton.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND)', calendar)
        driver = (ROOT / 'scripts/client-qa/run.py').read_text()
        self.assertIn('self.click(seeded, label)', driver)
        self.assertIn('snapshots_equal(data.get("player_variables"), result["after"])', driver)
        self.assertIn('numeric_close(data.get("movement_speed_value"), .1025)', driver)
        self.assertIn('self.capture("10-agility-precise-preview", seeded)', driver)


if __name__ == '__main__':
    unittest.main()
