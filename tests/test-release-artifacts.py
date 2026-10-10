#!/usr/bin/env python3
"""Offline regression tests for the complete RAS release-artifact inventory.

All JARs are synthetic ZIP files created in temporary directories. These checks
exercise release attestation and loader metadata, not compiled mod behavior.
Run: python3 tests/test-release-artifacts.py (Python 3.11+).
Optional Node preflight checks run when node is available; fetch is disabled.
"""

from __future__ import annotations

import copy
import hashlib
import importlib.util
import json
from pathlib import Path
import shutil
import subprocess
import struct
import sys
import tempfile
import unittest
import warnings
import zipfile


REPO = Path(__file__).resolve().parents[1]
VERIFIER_PATH = REPO / "scripts" / "verify-release-artifacts.py"
VERSION = "4.3.0"
COMMIT = "0123456789abcdef0123456789abcdef01234567"
OTHER_COMMIT = "abcdef0123456789abcdef0123456789abcdef01"
MOD_ID = "rpg_attribute_system"
FEATURE_CLASSES = ("tn/nightbeam/ras/config/MobXpRules.class",
                   "tn/nightbeam/ras/config/MobXpConfig.class",
                   "tn/nightbeam/ras/client/gui/PixelRpgBookLayout.class")

# Independent expectations from the checked-in metadata templates/properties.
# Do not import the verifier's matrix: an accidentally removed target must fail.
TARGETS = (
    ("1.20.1", "fabric"),
    ("1.20.1", "forge"),
    ("1.21.1", "fabric"),
    ("1.21.1", "neoforge"),
    ("26.1.2", "fabric"),
    ("26.1.2", "neoforge"),
    ("26.2", "fabric"),
    ("26.2", "neoforge"),
    ("26.3", "fabric"),
    ("26.3", "neoforge"),
)
METADATA = {
    "1.20.1": {"java": "17", "minecraft": "[1.20.1, 1.22)",
               "fabricloader": ">=0.14", "forge": "[47.2.30,)",
               "loaderVersion": "[47,)"},
    "1.21.1": {"java": "21", "minecraft": "[1.21.1, 1.22)",
               "fabricloader": ">=0.14", "neoforge": "[21.1.229,)",
               "loaderVersion": "[4,)"},
    "26.1.2": {"java": "25", "minecraft": "[26.1.2, 26.2)",
               "fabricloader": ">=0.18.6", "neoforge": "[26.1.2.94,)",
               "loaderVersion": "[4,)"},
    "26.2": {"java": "25", "minecraft": "[26.2, 26.3)",
             "fabricloader": ">=0.19.3", "neoforge": "[26.2.0.1-beta,)",
             "loaderVersion": "[4,)"},
    "26.3": {"java": "25", "minecraft": "[26.3, 26.4)",
             "fabricloader": ">=0.19.5", "neoforge": "[26.3.0.16-beta,)",
             "loaderVersion": "[4,)"},
}


def load_verifier():
    spec = importlib.util.spec_from_file_location("ras_release_verifier", VERIFIER_PATH)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"Cannot load release verifier: {VERIFIER_PATH}")
    module = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return module


# Do not leave imported verifier bytecode in the release checkout.
sys.dont_write_bytecode = True
VERIFIER = load_verifier()
NODE = shutil.which("node")
PUBLISHER_PATH = REPO / "scripts" / "publish-verified-release.mjs"


def filename(mc: str, loader: str, version: str = VERSION) -> str:
    return f"{MOD_ID}-{loader}-{mc}-{version}.jar"


def descriptor_path(loader: str) -> str:
    return {"fabric": "fabric.mod.json", "forge": "META-INF/mods.toml",
            "neoforge": "META-INF/neoforge.mods.toml"}[loader]


def fabric_metadata(mc: str) -> dict:
    classic = mc.startswith("1.")
    depends = {
        "fabricloader": METADATA[mc]["fabricloader"],
        "fabric" if classic else "fabric-api": "*",
        "minecraft": mc if classic else f"~{mc}",
        "java": f">={METADATA[mc]['java']}",
    }
    if classic:
        depends["jauml"] = "*"
    return {"schemaVersion": 1, "id": MOD_ID, "version": VERSION,
            "name": "RPG Attribute System", "environment": "*",
            "depends": depends}


def toml_metadata(mc: str, loader: str) -> str:
    required = 'mandatory = true' if loader == "forge" else 'type = "required"'
    dependencies = [(loader, METADATA[mc][loader]),
                    ("minecraft", METADATA[mc]["minecraft"])]
    if mc.startswith("1."):
        dependencies.append(("jauml", "*"))
    parts = [
        'modLoader = "javafml"',
        f'loaderVersion = "{METADATA[mc]["loaderVersion"]}"',
        'license = "Apache-2.0"',
        "[[mods]]", f'modId = "{MOD_ID}"', f'version = "{VERSION}"',
        'displayName = "RPG Attribute System"',
    ]
    for dependency, version_range in dependencies:
        parts.extend([f"[[dependencies.{MOD_ID}]]", f'modId = "{dependency}"',
                      required, f'versionRange = "{version_range}"',
                      'ordering = "NONE"', 'side = "BOTH"'])
    return "\n".join(parts) + "\n"


def manifest(mc: str, loader: str) -> str:
    return ("Manifest-Version: 1.0\r\n"
            f"Implementation-Title: {loader}\r\n"
            f"Implementation-Version: {VERSION}\r\n"
            f"Built-On-Minecraft: {mc}\r\n"
            f"RAS-Source-Commit: {COMMIT}\r\n\r\n")


def jar_entries(mc: str, loader: str) -> dict[str, bytes]:
    metadata = json.dumps(fabric_metadata(mc)) if loader == "fabric" else toml_metadata(mc, loader)
    return {"META-INF/MANIFEST.MF": manifest(mc, loader).encode(),
            descriptor_path(loader): metadata.encode(),
            **{name: b"\xca\xfe\xba\xbe" + struct.pack(">HH", 0, int(METADATA[mc]["java"]) + 44)
               for name in FEATURE_CLASSES}}


def write_jar(path: Path, entries: dict[str, bytes]) -> None:
    # Fixed timestamp/order makes the fixtures and hash assertions reproducible.
    with zipfile.ZipFile(path, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        for name, data in sorted(entries.items()):
            info = zipfile.ZipInfo(name, date_time=(2026, 1, 1, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            archive.writestr(info, data)


class ReleaseArtifactsTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix="ras-release-test-")
        self.addCleanup(self.temp.cleanup)
        self.directory = Path(self.temp.name)
        for mc, loader in TARGETS:
            write_jar(self.path(mc, loader), jar_entries(mc, loader))

    def path(self, mc="26.3", loader="fabric") -> Path:
        return self.directory / filename(mc, loader)

    def reset(self, mc="26.3", loader="fabric") -> None:
        write_jar(self.path(mc, loader), jar_entries(mc, loader))

    def rewrite(self, entry: str, data: bytes | str | None,
                mc="26.3", loader="fabric") -> None:
        path = self.path(mc, loader)
        with zipfile.ZipFile(path) as archive:
            entries = {name: archive.read(name) for name in archive.namelist()}
        if data is None:
            entries.pop(entry, None)
        else:
            entries[entry] = data.encode() if isinstance(data, str) else data
        write_jar(path, entries)

    def mutate_fabric(self, callback, mc="26.3") -> None:
        data = copy.deepcopy(fabric_metadata(mc))
        callback(data)
        self.rewrite("fabric.mod.json", json.dumps(data), mc, "fabric")

    def verify(self, version=VERSION, commit=COMMIT):
        return VERIFIER.verify(self.directory, version, commit)

    def assert_rejected(self, *, version=VERSION, commit=COMMIT):
        with self.assertRaises(ValueError):
            self.verify(version=version, commit=commit)

    def cli(self, output: Path | None = None, **kwargs):
        command = [sys.executable, str(VERIFIER_PATH), "--directory", str(self.directory),
                   "--version", VERSION, "--commit", COMMIT]
        if output is not None:
            command.extend(["--output", str(output)])
        return subprocess.run(command, capture_output=True, text=True, check=False, **kwargs)

    def publisher_preflight(self, inventory: dict):
        # Import the pure preflight export, never main(), with network disabled.
        code = """
            import fs from 'node:fs';
            globalThis.fetch = () => { throw new Error('Network forbidden in fixture tests'); };
            const {validatePlan} = await import(process.argv[1]);
            const manifest = JSON.parse(fs.readFileSync(0, 'utf8'));
            try {
              const entries = validatePlan(manifest, process.argv[2]);
              process.stdout.write(JSON.stringify(entries));
            } catch (error) {
              process.stderr.write(error.message);
              process.exitCode = 1;
            }
        """
        return subprocess.run([NODE, "--input-type=module", "-e", code,
                               PUBLISHER_PATH.as_uri(), str(self.directory)],
                              input=json.dumps(inventory), capture_output=True,
                              text=True, check=False, timeout=15)

    @unittest.skipUnless(NODE, "Node is required for publisher preflight smoke tests")
    def test_publisher_accepts_exact_unchanged_inventory(self):
        result = self.publisher_preflight(self.verify())
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(len(json.loads(result.stdout)), 10)

    @unittest.skipUnless(NODE, "Node is required for publisher preflight smoke tests")
    def test_publisher_rejects_each_mismatched_hash(self):
        inventory = self.verify()
        for algorithm in ("sha1", "sha256", "sha512"):
            with self.subTest(algorithm=algorithm):
                tampered = copy.deepcopy(inventory)
                original = tampered["files"][0][algorithm]
                tampered["files"][0][algorithm] = ("0" if original[0] != "0" else "1") + original[1:]
                result = self.publisher_preflight(tampered)
                self.assertNotEqual(result.returncode, 0)
                self.assertIn("Artifact changed after verification", result.stderr)

    @unittest.skipUnless(NODE, "Node is required for publisher preflight smoke tests")
    def test_publisher_rejects_mismatched_byte_length(self):
        inventory = self.verify()
        inventory["files"][0]["size_bytes"] += 1
        result = self.publisher_preflight(inventory)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("Artifact changed after verification", result.stderr)

    @unittest.skipUnless(NODE, "Node is required for publisher preflight smoke tests")
    def test_publisher_rejects_bytes_changed_after_verification(self):
        inventory = self.verify()
        self.rewrite("synthetic-payload.txt", b"bytes changed after manifest generation")
        result = self.publisher_preflight(inventory)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("Artifact changed after verification", result.stderr)

    @unittest.skipUnless(NODE, "Node is required for publisher preflight smoke tests")
    def test_publisher_rejects_incomplete_duplicate_or_extra_plans(self):
        inventory = self.verify()
        for mutation in ("missing", "duplicate", "extra", "partial"):
            with self.subTest(mutation=mutation):
                changed = copy.deepcopy(inventory)
                if mutation == "missing":
                    changed["files"].pop()
                elif mutation == "duplicate":
                    changed["files"][0] = copy.deepcopy(changed["files"][1])
                elif mutation == "extra":
                    changed["files"].append(copy.deepcopy(changed["files"][0]))
                else:
                    changed["complete_inventory"] = False
                result = self.publisher_preflight(changed)
                self.assertNotEqual(result.returncode, 0)

    @unittest.skipUnless(NODE, "Node is required for publisher preflight smoke tests")
    def test_publisher_rejects_cross_release_and_source_entries(self):
        inventory = self.verify()
        for field, value in (("version", "4.2.9"), ("source_commit", OTHER_COMMIT)):
            with self.subTest(field=field):
                changed = copy.deepcopy(inventory)
                changed["files"][0][field] = value
                result = self.publisher_preflight(changed)
                self.assertNotEqual(result.returncode, 0)

    def test_complete_ten_target_release_is_valid(self):
        inventory = self.verify()
        self.assertIsInstance(inventory, dict)
        self.assertEqual(self.verify(), inventory, "Inventory must be deterministic")
        self.assertEqual(inventory["schema_version"], 1)
        self.assertEqual(inventory["mod_id"], MOD_ID)
        self.assertEqual(inventory["release_version"], VERSION)
        self.assertEqual(inventory["source_commit"], COMMIT)
        self.assertIs(inventory["complete_inventory"], True)
        self.assertEqual(inventory["artifact_count"], 10)
        self.assertEqual(len(inventory["files"]), 10)
        self.assertEqual({(row["minecraft"], row["loader"]) for row in inventory["files"]}, set(TARGETS))
        for row in inventory["files"]:
            mc, loader = row["minecraft"], row["loader"]
            with self.subTest(mc=mc, loader=loader):
                self.assertEqual(row["file_name"], filename(mc, loader))
                self.assertEqual(row["path"], filename(mc, loader))
                self.assertEqual(row["version"], VERSION)
                self.assertEqual(row["source_commit"], COMMIT)
                self.assertEqual(row["java"], int(METADATA[mc]["java"]))
                self.assertEqual(row["mod_metadata_path"], descriptor_path(loader))
                required = (["fabric-api", "jauml"] if loader == "fabric" and mc.startswith("1.")
                            else ["fabric-api"] if loader == "fabric"
                            else ["jauml"] if mc.startswith("1.") else [])
                self.assertEqual(row["required_mod_ids"], required)
                binary = self.path(mc, loader).read_bytes()
                self.assertEqual(row["size_bytes"], len(binary))
                for algorithm in ("sha1", "sha256", "sha512"):
                    self.assertEqual(row[algorithm], hashlib.new(algorithm, binary).hexdigest())

    def test_changed_jar_bytes_change_all_inventory_hashes(self):
        before = {row["file_name"]: row for row in self.verify()["files"]}
        self.rewrite("synthetic-payload.txt", b"changed bytes")
        after = {row["file_name"]: row for row in self.verify()["files"]}
        for name in before:
            for algorithm in ("sha1", "sha256", "sha512"):
                with self.subTest(filename=name, algorithm=algorithm):
                    if name == self.path().name:
                        self.assertNotEqual(before[name][algorithm], after[name][algorithm])
                    else:
                        self.assertEqual(before[name][algorithm], after[name][algorithm])

    def test_duplicate_target_in_nested_directory_is_rejected(self):
        nested = self.directory / "another-build"
        nested.mkdir()
        shutil.copyfile(self.path(), nested / self.path().name)
        self.assert_rejected()

    def test_nested_complete_inventory_preserves_relative_paths(self):
        nested = self.directory / "artifacts"
        nested.mkdir()
        for jar in self.directory.glob("*.jar"):
            jar.rename(nested / jar.name)
        inventory = self.verify()
        self.assertEqual({row["path"] for row in inventory["files"]},
                         {"artifacts/" + filename(mc, loader) for mc, loader in TARGETS})

    def test_cli_writes_valid_json_inventory(self):
        output = self.directory / "release-manifest.json"
        result = self.cli(output)
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(json.loads(output.read_text()), self.verify())

    def test_cli_failure_does_not_create_inventory(self):
        self.path().unlink()
        output = self.directory / "release-manifest.json"
        result = self.cli(output)
        self.assertNotEqual(result.returncode, 0)
        self.assertFalse(output.exists())

    def test_missing_each_target_is_rejected(self):
        for mc, loader in TARGETS:
            with self.subTest(mc=mc, loader=loader):
                self.path(mc, loader).unlink()
                self.assert_rejected()
                self.reset(mc, loader)

    def test_empty_release_is_rejected(self):
        for jar in self.directory.glob("*.jar"):
            jar.unlink()
        self.assert_rejected()

    def test_extra_jar_names_are_rejected(self):
        for name in ("unrelated.jar", filename("1.21.1", "forge"),
                     filename("26.4", "fabric"), filename("26.3", "quilt"),
                     filename("26.3", "fabric", "4.2.9"),
                     "rpg_attribute_system-fabric-26.3-4.3.0-sources.jar",
                     "rpg_attribute_system-common-26.3-4.3.0.jar"):
            with self.subTest(filename=name):
                extra = self.directory / name
                shutil.copyfile(self.path(), extra)
                self.assert_rejected()
                extra.unlink()

    def test_missing_release_directory_is_rejected(self):
        with self.assertRaises(ValueError):
            VERIFIER.verify(self.directory / "missing", VERSION, COMMIT)

    def test_symlink_artifact_is_rejected(self):
        path = self.path()
        real = self.directory / "elsewhere.bin"
        path.rename(real)
        path.symlink_to(real)
        self.assert_rejected()

    def test_invalid_version_arguments_are_rejected(self):
        for version in ("", "v4.3.0", "4.3", "4.3.0\n", "../4.3.0", "4.3.0+fabric"):
            with self.subTest(version=version):
                self.assert_rejected(version=version)

    def test_source_commit_must_be_complete_hex_sha(self):
        for commit in ("", "0123456", "0" * 39, "0" * 41, "g" * 40,
                       COMMIT + "\n", COMMIT.upper(), "${GITHUB_SHA}"):
            with self.subTest(commit=commit):
                self.assert_rejected(commit=commit)

    def test_requested_source_commit_must_match_every_jar(self):
        self.assert_rejected(commit=OTHER_COMMIT)

    def test_manifest_required_fields_and_values(self):
        fields = {"Implementation-Title": "common", "Implementation-Version": "4.2.9",
                  "Built-On-Minecraft": "999.0", "RAS-Source-Commit": OTHER_COMMIT}
        for mc, loader in TARGETS:
            for key, wrong_value in fields.items():
                for replacement in (None, wrong_value, "${unexpanded}"):
                    with self.subTest(mc=mc, loader=loader, key=key, value=replacement):
                        lines = manifest(mc, loader).splitlines()
                        lines = [(f"{key}: {replacement}" if replacement is not None else "")
                                 if line.startswith(key + ":") else line for line in lines]
                        self.rewrite("META-INF/MANIFEST.MF", "\r\n".join(lines) + "\r\n", mc, loader)
                        self.assert_rejected()
                        self.reset(mc, loader)

    def test_missing_manifest_is_rejected(self):
        self.rewrite("META-INF/MANIFEST.MF", None)
        self.assert_rejected()

    def test_folded_manifest_source_commit_is_valid(self):
        text = manifest("26.3", "fabric").replace(
            f"RAS-Source-Commit: {COMMIT}",
            f"RAS-Source-Commit: {COMMIT[:20]}\r\n {COMMIT[20:]}")
        self.rewrite("META-INF/MANIFEST.MF", text)
        self.assertIsInstance(self.verify(), dict)

    def test_duplicate_manifest_attestation_is_rejected(self):
        text = manifest("26.3", "fabric").replace("\r\n\r\n", f"\r\nRAS-Source-Commit: {COMMIT}\r\n\r\n")
        self.rewrite("META-INF/MANIFEST.MF", text)
        self.assert_rejected()

    def test_manifest_named_section_cannot_supply_main_attestation(self):
        text = manifest("26.3", "fabric").replace(f"RAS-Source-Commit: {COMMIT}\r\n", "")
        text += f"Name: fixture.class\r\nRAS-Source-Commit: {COMMIT}\r\n\r\n"
        self.rewrite("META-INF/MANIFEST.MF", text)
        self.assert_rejected()

    def test_missing_loader_descriptor_is_rejected_for_every_target(self):
        for mc, loader in TARGETS:
            with self.subTest(mc=mc, loader=loader):
                self.rewrite(descriptor_path(loader), None, mc, loader)
                self.assert_rejected()
                self.reset(mc, loader)

    def test_wrong_loader_descriptor_is_rejected(self):
        for mc, loader in TARGETS:
            with self.subTest(mc=mc, loader=loader):
                self.rewrite("fabric.mod.json" if loader != "fabric" else "META-INF/neoforge.mods.toml",
                             json.dumps(fabric_metadata(mc)) if loader != "fabric" else toml_metadata("1.21.1", "neoforge"),
                             mc, loader)
                self.assert_rejected()
                self.reset(mc, loader)

    def test_malformed_metadata_is_rejected_for_every_loader(self):
        for mc, loader in TARGETS:
            for data in (b"not JSON or TOML", b"\xff\xfeinvalid UTF-8", b""):
                with self.subTest(mc=mc, loader=loader, data=data):
                    self.rewrite(descriptor_path(loader), data, mc, loader)
                    self.assert_rejected()
                    self.reset(mc, loader)

    def test_fabric_mod_id_and_version_must_match(self):
        for mc, loader in TARGETS:
            if loader != "fabric":
                continue
            for key, value in (("id", "other_mod"), ("version", "4.2.9"),
                               ("id", "${mod_id}"), ("version", "${version}")):
                with self.subTest(mc=mc, key=key, value=value):
                    self.mutate_fabric(lambda data: data.__setitem__(key, value), mc)
                    self.assert_rejected()
                    self.reset(mc, loader)

    def test_fabric_required_dependency_values_and_presence(self):
        for mc, loader in TARGETS:
            if loader != "fabric":
                continue
            for dependency in fabric_metadata(mc)["depends"]:
                for replacement in (None, "${unexpanded}", ">=999"):
                    with self.subTest(mc=mc, dependency=dependency, value=replacement):
                        def change(data):
                            if replacement is None:
                                del data["depends"][dependency]
                            else:
                                data["depends"][dependency] = replacement
                        self.mutate_fabric(change, mc)
                        self.assert_rejected()
                        self.reset(mc, loader)

    def test_fabric_missing_depends_object_is_rejected(self):
        for value in (None, [], "*"):
            with self.subTest(depends=value):
                self.mutate_fabric(lambda data: data.__setitem__("depends", value))
                self.assert_rejected()
                self.reset()

    def test_forge_and_neoforge_mod_id_and_version_must_match(self):
        for mc, loader in TARGETS:
            if loader == "fabric":
                continue
            for original, replacement in ((f'modId = "{MOD_ID}"', 'modId = "other_mod"'),
                                          (f'version = "{VERSION}"', 'version = "4.2.9"'),
                                          (f'version = "{VERSION}"', 'version = "${version}"')):
                with self.subTest(mc=mc, loader=loader, replacement=replacement):
                    self.rewrite(descriptor_path(loader), toml_metadata(mc, loader).replace(original, replacement, 1), mc, loader)
                    self.assert_rejected()
                    self.reset(mc, loader)

    def test_forge_and_neoforge_mc_ranges_and_loader_requirements(self):
        for mc, loader in TARGETS:
            if loader == "fabric":
                continue
            for original, replacement in ((f'loaderVersion = "{METADATA[mc]["loaderVersion"]}"', 'loaderVersion = "[999,)"'),
                                          (f'versionRange = "{METADATA[mc]["minecraft"]}"', 'versionRange = "[1.0,999)"'),
                                          (f'versionRange = "{METADATA[mc][loader]}"', 'versionRange = "[999,)"'),
                                          ('modLoader = "javafml"', 'modLoader = "other_loader"')):
                with self.subTest(mc=mc, loader=loader, replacement=replacement):
                    self.rewrite(descriptor_path(loader), toml_metadata(mc, loader).replace(original, replacement, 1), mc, loader)
                    self.assert_rejected()
                    self.reset(mc, loader)

    def test_forge_and_neoforge_dependencies_must_be_required(self):
        for mc, loader in TARGETS:
            if loader == "fabric":
                continue
            original = 'mandatory = true' if loader == "forge" else 'type = "required"'
            for replacement in ("", "mandatory = false" if loader == "forge" else 'type = "optional"'):
                with self.subTest(mc=mc, loader=loader, replacement=replacement):
                    self.rewrite(descriptor_path(loader), toml_metadata(mc, loader).replace(original, replacement), mc, loader)
                    self.assert_rejected()
                    self.reset(mc, loader)

    def test_forge_and_neoforge_missing_dependency_is_rejected(self):
        for mc, loader in TARGETS:
            if loader == "fabric":
                continue
            parts = toml_metadata(mc, loader).split(f"[[dependencies.{MOD_ID}]]")
            for index in range(1, len(parts)):
                with self.subTest(mc=mc, loader=loader, index=index):
                    text = parts[0] + "".join(f"[[dependencies.{MOD_ID}]]" + part
                                               for i, part in enumerate(parts[1:], 1) if i != index)
                    self.rewrite(descriptor_path(loader), text, mc, loader)
                    self.assert_rejected()
                    self.reset(mc, loader)

    def test_duplicate_jar_metadata_entries_are_rejected(self):
        for mc, loader in TARGETS:
            with self.subTest(mc=mc, loader=loader):
                entry = descriptor_path(loader)
                with warnings.catch_warnings():
                    warnings.simplefilter("ignore", UserWarning)
                    with zipfile.ZipFile(self.path(mc, loader), "a") as archive:
                        archive.writestr(entry, archive.read(entry))
                self.assert_rejected()
                self.reset(mc, loader)

    def test_feature_classes_must_exist_with_correct_java_header(self):
        for mc, loader in TARGETS:
            for name in FEATURE_CLASSES:
                for content in (None, b"", b"not a Java class",
                                b"\xca\xfe\xba\xbe" + struct.pack(">HH", 0, 52)):
                    with self.subTest(mc=mc, loader=loader, name=name, content=content):
                        self.rewrite(name, content, mc, loader)
                        self.assert_rejected()
                        self.reset(mc, loader)

    def test_extra_runtime_mod_dependency_is_rejected(self):
        for mc, loader in TARGETS:
            with self.subTest(mc=mc, loader=loader):
                if loader == "fabric":
                    self.mutate_fabric(lambda data: data["depends"].__setitem__("unexpected_mod", "*"), mc)
                else:
                    required = 'mandatory = true' if loader == "forge" else 'type = "required"'
                    extra = (f"[[dependencies.{MOD_ID}]]\nmodId = \"unexpected_mod\"\n"
                             + required + '\nversionRange = "*"\n')
                    self.rewrite(descriptor_path(loader), toml_metadata(mc, loader) + extra, mc, loader)
                self.assert_rejected()
                self.reset(mc, loader)

    def test_unsafe_zip_paths_are_rejected(self):
        for path in ("../escape.class", "/absolute.class", "META-INF\\escape.class"):
            with self.subTest(path=path):
                self.rewrite(path, b"untrusted")
                self.assert_rejected()
                self.reset()

    def test_fabric_duplicate_json_keys_are_rejected(self):
        raw = json.dumps(fabric_metadata("26.3"))
        raw = raw.replace(f'"version": "{VERSION}"', f'"version": "4.2.9", "version": "{VERSION}"')
        self.rewrite("fabric.mod.json", raw)
        self.assert_rejected()

    def test_fabric_metadata_root_must_be_an_object(self):
        for value in (None, [], "metadata", 1, True):
            with self.subTest(metadata=value):
                self.rewrite("fabric.mod.json", json.dumps(value))
                self.assert_rejected()
                self.reset()

    def test_loader_metadata_containers_must_have_valid_types(self):
        for mc, loader in TARGETS:
            if loader == "fabric":
                continue
            for text in ('mods = "not an array"\n',
                         '[mods]\nmodId = "rpg_attribute_system"\n',
                         'dependencies = "not a table"\n',
                         '[dependencies]\nrpg_attribute_system = "not an array"\n'):
                with self.subTest(mc=mc, loader=loader, text=text):
                    self.rewrite(descriptor_path(loader), text, mc, loader)
                    self.assert_rejected()
                    self.reset(mc, loader)

    def test_uppercase_jar_extension_does_not_hide_extra_artifact(self):
        extra = self.directory / "unexpected.JAR"
        shutil.copyfile(self.path(), extra)
        self.assert_rejected()

    def test_manifest_case_variant_duplicate_is_rejected(self):
        text = manifest("26.3", "fabric").replace(
            "\r\n\r\n", f"\r\nras-source-commit: {OTHER_COMMIT}\r\n\r\n")
        self.rewrite("META-INF/MANIFEST.MF", text)
        self.assert_rejected()

    def test_loader_mc_range_harmless_spacing_is_valid(self):
        for mc, loader in TARGETS:
            if loader != "fabric":
                self.rewrite(descriptor_path(loader), toml_metadata(mc, loader).replace(
                    METADATA[mc]["minecraft"], METADATA[mc]["minecraft"].replace(" ", "")), mc, loader)
        self.assertEqual(self.verify()["artifact_count"], 10)

    def test_loader_mc_range_cannot_broaden_its_upper_bound(self):
        for mc, loader in TARGETS:
            if loader == "fabric":
                continue
            with self.subTest(mc=mc, loader=loader):
                text = toml_metadata(mc, loader).replace(
                    METADATA[mc]["minecraft"], f"[{mc}, 999)")
                self.rewrite(descriptor_path(loader), text, mc, loader)
                self.assert_rejected()
                self.reset(mc, loader)

    def test_corrupt_zip_is_rejected(self):
        for content in (b"not a JAR", self.path().read_bytes()[:40]):
            with self.subTest(content_length=len(content)):
                self.path().write_bytes(content)
                self.assert_rejected()


if __name__ == "__main__":
    unittest.main(verbosity=2)
