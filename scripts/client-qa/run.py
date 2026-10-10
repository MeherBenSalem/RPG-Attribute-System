#!/usr/bin/env python3
"""Drive real Fabric clients on an existing isolated X11 display. Never render a GUI mock."""
from __future__ import annotations
import argparse
import hashlib
import json
import math
import os
from pathlib import Path
import platform
import re
import shutil
import signal
import subprocess
import time
import uuid

EXPECTED_IDS = {f"attribute_{i}" for i in range(1, 9)}
WIDTH, HEIGHT = 1280, 960


class QaError(RuntimeError):
    pass


def validate_ready(data, *, run_id, sha, mc, scale, launched_ms, expected_state=None, since=0, not_before_ms=0):
    """Fail closed on old files, invented state, missing sync, or a clamped GUI scale."""
    if not isinstance(data, dict) or data.get("schema_version") != 1:
        raise QaError("Unsupported readiness schema")
    if data.get("run_id") != run_id or data.get("source_sha") != sha:
        raise QaError("Readiness run identity/source SHA mismatch")
    if data.get("status") != "READY" or data.get("mc") != mc or data.get("loader") != "fabric":
        raise QaError("Unexpected readiness status/version/loader")
    written = data.get("written_at_ms", 0)
    if not isinstance(written, (int, float)) or not math.isfinite(written) or written < max(launched_ms, not_before_ms) or written > time.time() * 1000 + 5000:
        raise QaError("Stale/future readiness result")
    if data.get("requested_gui_scale") != scale or data.get("actual_gui_scale") != scale:
        raise QaError("Requested/actual GUI scale mismatch")
    if data.get("window_width") != WIDTH or data.get("window_height") != HEIGHT:
        raise QaError("Actual framebuffer size differs from requested 1280x960")
    if data.get("gui_width") != math.ceil(WIDTH / scale) or data.get("gui_height") != math.ceil(HEIGHT / scale):
        raise QaError("Actual GUI dimensions do not match framebuffer/scale")
    for field in ("server_player_bound", "client_player_present", "client_level_present", "expected_attributes_synced"):
        if data.get(field) is not True:
            raise QaError(f"Real player/server/sync evidence missing: {field}")
    ids = data.get("synced_attribute_ids", [])
    if not isinstance(ids, list) or len(ids) != 8 or set(ids) != EXPECTED_IDS:
        raise QaError("Fresh-default synced attribute IDs are not exactly 1 through 8")
    for field, minimum in (("level", 0), ("spare_points", 0), ("next_level_xp", .0000001)):
        value = data.get(field)
        if not isinstance(value, (int, float)) or not math.isfinite(value) or value < minimum:
            raise QaError(f"Expected actual player variable missing: {field}")
    if not isinstance(data.get("sequence"), int) or data["sequence"] <= since:
        raise QaError("Readiness has not advanced after the action")
    states = {"ALLOCATION", "COMBAT", "OVERVIEW_ATTRIBUTES", "OVERVIEW_TOTALS", "WORLD"}
    if data.get("state") not in states or (expected_state and data["state"] != expected_state):
        raise QaError("Expected real screen state has not appeared")
    buttons = data.get("buttons")
    if not isinstance(buttons, list):
        raise QaError("Missing actual control geometry")
    if data["state"] != "WORLD":
        panel = data.get("panel", {})
        if panel.get("native_scale") != 1 or panel.get("rows", 0) < 1:
            raise QaError("Non-native or missing actual panel geometry")
        for key in ("x", "y", "width", "height"):
            if not isinstance(panel.get(key), int):
                raise QaError("Invalid actual panel rectangle")
        if panel["x"] < 0 or panel["y"] < 0 or panel["width"] < 1 or panel["height"] < 1:
            raise QaError("Invalid actual panel bounds")
        if panel["x"] + panel["width"] > data["gui_width"] or panel["y"] + panel["height"] > data["gui_height"]:
            raise QaError("Actual panel exceeds GUI viewport")
        if len(buttons) < 3:
            raise QaError("Missing real screen controls")
    for button in buttons:
        if not isinstance(button, dict) or not isinstance(button.get("label"), str) or not button["label"].strip():
            raise QaError("Unlabeled real control")
        for key in ("x", "y", "width", "height"):
            if not isinstance(button.get(key), int):
                raise QaError("Invalid control rectangle")
        if button["width"] < 20 or button["height"] < 20 or button["x"] < 0 or button["y"] < 0:
            raise QaError("Small/outside real control")
        if button["x"] + button["width"] > data["gui_width"] or button["y"] + button["height"] > data["gui_height"]:
            raise QaError("Control exceeds actual GUI viewport")
    return data


def numeric_close(actual, expected):
    return type(actual) in (int, float) and type(expected) in (int, float) and math.isfinite(actual) and math.isfinite(expected) and abs(actual - expected) <= 1e-8


def seeded_agility_preview_ready(data):
    """Observe the real post-reset widget too: variables can sync before its next render."""
    if not isinstance(data, dict) or data.get("state") != "ALLOCATION":
        return False
    variables = data.get("player_variables")
    if not isinstance(variables, dict) or not isinstance(variables.get("attributes"), dict) \
            or not isinstance(variables.get("attribute_points"), dict) \
            or not numeric_close(variables["attributes"].get("attribute_5"), .1) \
            or not numeric_close(variables["attribute_points"].get("attribute_5"), 0) \
            or not numeric_close(variables.get("spare_points"), 6):
        return False
    buttons = data.get("buttons")
    if not isinstance(buttons, list):
        return False
    agility = [button for button in buttons if isinstance(button, dict)
               and isinstance(button.get("label"), str) and button["label"].startswith("Allocate Agility.")]
    return len(agility) == 1 and agility[0].get("active") is True \
        and agility[0]["label"].startswith("Allocate Agility. Next value: 0.1025 · ")


def snapshots_equal(actual, expected):
    if not isinstance(actual, dict) or not isinstance(expected, dict):
        return False
    if set(actual) != {"total_xp", "level", "spare_points", "modifier", "attributes", "attribute_points"}:
        return False
    for key in ("total_xp", "level", "spare_points", "modifier"):
        if not numeric_close(actual.get(key), expected.get(key, float("nan"))):
            return False
    for key in ("attributes", "attribute_points"):
        if not isinstance(actual.get(key), dict) or not isinstance(expected.get(key), dict) or set(actual[key]) != set(expected[key]):
            return False
        if any(not numeric_close(value, expected[key][name]) for name, value in actual[key].items()):
            return False
    return True


def validate_gameplay(data, *, run_id, sha, request_id, action, not_before_ms):
    if not isinstance(data, dict) or data.get("schema_version") != 1:
        raise QaError("Missing real gameplay evidence schema")
    if any(data.get(key) != value for key, value in (("run_id", run_id), ("source_sha", sha),
            ("request_id", request_id), ("action", action))):
        raise QaError("Gameplay request/source/run identity differs")
    written = data.get("written_at_ms")
    if type(written) not in (int, float) or not math.isfinite(written) or written < not_before_ms or written > time.time() * 1000 + 5000:
        raise QaError("Gameplay evidence is stale or future-dated")
    if data.get("status") != "PASS":
        raise QaError(f"Actual bound-server gameplay failed: {data.get('reason', 'missing pass')}")
    for key in ("before", "after"):
        if not snapshots_equal(data.get(key), data.get(key)):
            raise QaError("Gameplay requires actual finite server variable snapshots")
    if action == "seed" and data.get("fixture_setup_only") is not True:
        raise QaError("Fixture seed lacks explicit setup-only boundary")
    if action == "verify-allocation":
        if data.get("normal_ui_packet_allocation_verified") is not True or not numeric_close(data.get("expected_agility"), .1025) \
                or not numeric_close(data["after"]["attributes"].get("attribute_5"), .1025) \
                or not numeric_close(data["after"]["attribute_points"].get("attribute_5"), 1) \
                or not numeric_close(data["after"].get("spare_points"), 5) \
                or not numeric_close(data.get("server_movement_speed_base"), .1025) \
                or not numeric_close(data.get("server_movement_speed_value"), .1025):
            raise QaError("Actual server-authoritative Agility allocation was not verified")
    if action in {"kill-entity", "kill-tag", "invalid-reload", "kill-legacy"}:
        expected = {"kill-entity": 31.5, "kill-tag": 33.75, "invalid-reload": 33.75, "kill-legacy": 20}[action]
        if data.get("normal_loader_death_event_verified") is not True or data.get("real_tag_match") is not True \
                or data.get("mob") != "minecraft:skeleton" or data.get("difficulty") != "hard" \
                or not numeric_close(data.get("armor_points"), 2) or not numeric_close(data.get("expected_reward"), expected) \
                or not numeric_close(data.get("observed_reward"), expected) \
                or not numeric_close(data["after"]["total_xp"] - data["before"]["total_xp"], expected):
            raise QaError("Actual player damage/loader death reward evidence is incomplete or wrong")
        if action == "invalid-reload" and data.get("invalid_reload_retained_previous_rules") is not True:
            raise QaError("Last-good rules were not preserved after malformed reload")
    return data


def checked(command, **kwargs):
    return subprocess.run(command, check=True, text=True, timeout=kwargs.pop("timeout", 20), **kwargs)


def read_json(path):
    try:
        return json.loads(path.read_text())
    except (FileNotFoundError, json.JSONDecodeError):
        return None


def renderer_environment(mc, base=None, icd_directory=Path("/usr/share/vulkan/icd.d")):
    """Choose genuine packaged software drivers, never advertise invented GL capabilities."""
    environment = dict(os.environ if base is None else base)
    if any(key.startswith("MESA_") and key.endswith("VERSION_OVERRIDE") for key in environment):
        raise QaError("GL/GLSL version overrides are prohibited in real-client QA")
    environment.update({"LIBGL_ALWAYS_SOFTWARE": "true", "GALLIUM_DRIVER": "llvmpipe"})
    if mc == "1.21.1":
        environment.pop("RAS_GRAPHICS_BACKEND", None)  # This client only has the OpenGL path.
    if mc == "26.3":
        # SDL3 must use the same X11 display that our native captures observe.
        environment["SDL_VIDEO_DRIVER"] = "x11"
        backend = environment.setdefault("RAS_GRAPHICS_BACKEND", "vulkan")
        if backend not in {"opengl", "vulkan"}:
            raise QaError("26.3 QA requires an explicit OpenGL or Vulkan backend")
        if backend == "vulkan":
            # Ubuntu Mesa has used both architecture-qualified and plain manifest names.
            names = (f"lvp_icd.{platform.machine()}.json", "lvp_icd.json")
            manifest = next((icd_directory / name for name in names if (icd_directory / name).is_file()), None)
            if manifest is None:
                raise QaError("Missing packaged Mesa lavapipe ICD; install mesa-vulkan-drivers")
            data = read_json(manifest)
            icd = data.get("ICD") if isinstance(data, dict) else None
            library = icd.get("library_path", "") if isinstance(icd, dict) else ""
            if not isinstance(library, str) or not Path(library).name.startswith("libvulkan_lvp.so"):
                raise QaError("Packaged lavapipe manifest does not identify the real Mesa driver")
            # VK_DRIVER_FILES is the supported Vulkan-loader selector, not a capability override.
            environment["VK_DRIVER_FILES"] = str(manifest.resolve())
            environment.pop("VK_ICD_FILENAMES", None)
    return environment


def validate_vulkan_renderer(renderer):
    """Fail before the client starts when a software device or its X11 WSI is missing."""
    extensions = set(re.findall(r"^\s*(VK_[A-Za-z0-9_]+)\s*:", renderer, re.MULTILINE))
    if not {"VK_KHR_surface", "VK_KHR_xlib_surface"}.issubset(extensions):
        raise QaError("Mesa Vulkan is missing VK_KHR_surface/VK_KHR_xlib_surface for the actual X11 display")
    devices = re.findall(r"^\s*deviceName\s*=\s*(.+)$", renderer, re.MULTILINE)
    types = re.findall(r"^\s*deviceType\s*=\s*(\S+)", renderer, re.MULTILINE)
    drivers = re.findall(r"^\s*driverID\s*=\s*(\S+)", renderer, re.MULTILINE)
    if len(devices) != 1 or len(types) != 1 or len(drivers) != 1 or not devices[0].lower().startswith(("llvmpipe", "lavapipe")) \
            or types[0] != "PHYSICAL_DEVICE_TYPE_CPU" or drivers[0] != "DRIVER_ID_MESA_LLVMPIPE":
        raise QaError("Exactly one verified Mesa lavapipe CPU Vulkan device is required")


def prepare_renderer(mc, output):
    environment = renderer_environment(mc)
    vulkan = mc == "26.3" and environment["RAS_GRAPHICS_BACKEND"] == "vulkan"
    tool = "vulkaninfo" if vulkan else "glxinfo"
    if not shutil.which(tool):
        raise QaError(f"Missing real-client QA renderer probe: {tool}")
    command = [tool, "--summary" if vulkan else "-B"]
    result = subprocess.run(command, text=True, capture_output=True, timeout=30, env=environment)
    (output / "renderer.txt").write_text(result.stdout)
    (output / "renderer-stderr.txt").write_text(result.stderr)
    if result.returncode != 0:
        raise QaError(f"Actual {tool} renderer setup failed (exit {result.returncode}); see renderer-stderr.txt")
    if vulkan:
        validate_vulkan_renderer(result.stdout)
    elif "llvmpipe" not in result.stdout.lower():
        raise QaError("A verified Mesa software OpenGL renderer is required")
    selected = {key: environment[key] for key in ("LIBGL_ALWAYS_SOFTWARE", "GALLIUM_DRIVER", "RAS_GRAPHICS_BACKEND",
                "SDL_VIDEO_DRIVER", "VK_DRIVER_FILES") if key in environment}
    (output / "renderer-environment.json").write_text(json.dumps(selected, indent=2) + "\n")
    return environment


def renderer_failure(log, backend):
    """Recognize renderer-creation failures, not harmless audio/authentication diagnostics."""
    markers = ["No available video device", "Failed to initialize SDL"]
    if backend == "vulkan":
        markers += ["Failed to create backend Vulkan", "Vulkan is not supported:"]
    elif backend == "opengl":
        markers += ["Failed to create backend OpenGL", "Couldn't find matching GLX visual",
                    "Failed to create window for OpenGL context"]
    for line in log.splitlines():
        if any(marker in line for marker in markers):
            return line.strip()
    return None


def neutral_pointer(ready):
    """Find a native framebuffer corner away from every actual interactive control."""
    scale = ready["actual_gui_scale"]
    for x, y in ((1, 1), (WIDTH-2, 1), (1, HEIGHT-2), (WIDTH-2, HEIGHT-2)):
        if all(not (button["x"]*scale <= x < (button["x"]+button["width"])*scale
                    and button["y"]*scale <= y < (button["y"]+button["height"])*scale)
               for button in ready["buttons"]):
            return x, y
    raise QaError("No neutral native pointer position outside actual controls")


def world_inventory(world):
    if not world.is_dir() or not (world / "level.dat").is_file():
        raise QaError("No genuinely generated world level.dat")
    inventory, total = {}, 0
    for path in sorted(world.rglob("*")):
        if path.is_symlink():
            raise QaError("Disposable fixture contains a symlink")
        if path.is_file():
            size = path.stat().st_size
            total += size
            if total > 256 * 1024 * 1024:
                raise QaError("Disposable world fixture exceeds 256 MiB")
            inventory[path.relative_to(world).as_posix()] = {"bytes": size, "sha256": hashlib.sha256(path.read_bytes()).hexdigest()}
    if (world / "level.dat").stat().st_size < 100:
        raise QaError("Generated world level.dat is incomplete")
    return inventory


def validate_fixture(directory, sha):
    provenance = read_json(directory / "provenance.json")
    if not provenance or provenance.get("schema_version") != 1 or provenance.get("source_sha") != sha:
        raise QaError("Disposable-world provenance does not match this source SHA")
    if provenance.get("fixture_kind") != "live-client-generated-disposable-demo" or provenance.get("producer_mc") != "1.21.1":
        raise QaError("Only this workflow's genuine disposable demo fixture is allowed")
    if provenance.get("world_files") != world_inventory(directory / "world"):
        raise QaError("Disposable-world inventory/hash mismatch")
    return directory / "world"


class Driver:
    def __init__(self, repo, mc, output, sha, fixture, environment=None):
        self.repo, self.mc, self.output, self.sha, self.fixture = repo, mc, output, sha, fixture
        self.process = None
        self.pgid = None
        self.game = None
        self.case = None
        self.window_id = None
        self.run_id = ""
        self.scale = 0
        self.launched_ms = 0
        self.deadline = 0
        self.stdout = None
        self.last_action_ms = 0
        self.last_action_sequence = 0
        self.environment = dict(os.environ if environment is None else environment)

    def check_renderer_failure(self):
        backend = self.environment.get("RAS_GRAPHICS_BACKEND", "opengl")
        for path in (self.case / "gradle-client.log", self.game / "logs/latest.log"):
            try:
                with path.open("rb") as handle:
                    handle.seek(max(0, path.stat().st_size - 128 * 1024))
                    failure = renderer_failure(handle.read().decode("utf-8", errors="replace"), backend)
            except FileNotFoundError:
                continue
            if failure:
                raise QaError(f"Actual {backend} renderer creation failed: {failure}")

    def wait_ready(self, state, since=0, seconds=60, page=None, predicate=None):
        limit = min(self.deadline, time.monotonic() + seconds)
        last = "No fresh readiness file"
        while time.monotonic() < limit:
            self.check_renderer_failure()
            failure = read_json(self.game / "ras-client-qa-final.json")
            if failure and failure.get("run_id") == self.run_id and failure.get("source_sha") == self.sha and failure.get("status") == "FAIL":
                raise QaError(f"Actual client failed: {failure.get('reason')}")
            data = read_json(self.game / "ras-client-qa-ready.json")
            if data:
                try:
                    ready = validate_ready(data, run_id=self.run_id, sha=self.sha, mc=self.mc, scale=self.scale,
                                           launched_ms=self.launched_ms, expected_state=state, since=max(since,self.last_action_sequence),
                                           not_before_ms=self.last_action_ms)
                    if predicate is not None and not predicate(ready):
                        raise QaError("Actual post-input state predicate has not appeared")
                    if page is not None and ready.get("page") != page:
                        raise QaError("Expected real page has not appeared")
                    return ready
                except QaError as exc:
                    last = str(exc)
            if self.process.poll() is not None:
                raise QaError(f"Client/Gradle exited {self.process.returncode} without fresh required state {state}: {last}")
            time.sleep(.2)
        raise QaError(f"Timed out awaiting real {state}: {last}")

    def find_window(self):
        result = checked(["xdotool", "search", "--onlyvisible", "--name", "^Minecraft"], capture_output=True)
        windows = result.stdout.split()
        if len(windows) != 1:
            raise QaError(f"Expected exactly one real Minecraft X11 window; found {len(windows)}")
        self.window_id = windows[0]
        checked(["xdotool", "windowfocus", "--sync", self.window_id])
        geometry = checked(["xdotool", "getwindowgeometry", "--shell", self.window_id], capture_output=True).stdout
        rectangle = dict(line.split("=", 1) for line in geometry.splitlines() if "=" in line)
        if int(rectangle["WIDTH"]) != WIDTH or int(rectangle["HEIGHT"]) != HEIGHT:
            raise QaError("Actual X11 client window size differs from readiness framebuffer")
        return int(rectangle["X"]), int(rectangle["Y"])

    def capture(self, name, ready=None, verify=True):
        from PIL import Image
        x, y = self.find_window()
        path = self.case / f"{name}.png"
        checked(["ffmpeg", "-nostdin", "-loglevel", "error", "-y", "-f", "x11grab", "-video_size", f"{WIDTH}x{HEIGHT}",
                 "-draw_mouse", "0", "-i", f"{os.environ['DISPLAY']}+{x},{y}", "-frames:v", "1", "-update", "1", str(path)], capture_output=True)
        with Image.open(path) as image:
            image = image.convert("RGB")
            if image.size != (WIDTH, HEIGHT) or sum(high-low for low, high in image.getextrema()) < 20:
                raise QaError("Actual X11 frame is missing, blank, or has wrong dimensions")
            if verify and ready and ready["state"] != "WORLD":
                panel, scale = ready["panel"], ready["actual_gui_scale"]
                # These samples prove the live panel was rasterized. They do not replace human text/layout review.
                for dy, expected in ((6, (99,58,77)), (30, (241,232,204))):
                    pixel = image.getpixel((round((panel['x']+5)*scale), round((panel['y']+dy)*scale)))
                    if any(abs(actual-wanted) > 12 for actual,wanted in zip(pixel,expected)):
                        raise QaError(f"Expected real panel pixels absent in {name}: {pixel} vs {expected}")
        if ready:
            (self.case / f"{name}.json").write_text(json.dumps(ready, indent=2)+"\n")
        return path

    def stable_capture(self, name, ready):
        if ready["state"] != "WORLD":
            # Move the real pointer, leaving intentional keyboard focus unchanged. No image edits/crops.
            self.find_window()
            x, y = neutral_pointer(ready)
            checked(["xdotool", "mousemove", "--sync", "--window", self.window_id, str(x), str(y)])
        # Readiness already requires twelve live client ticks. Capture two actual subsequent frames.
        time.sleep(.5)
        self.capture(name+"-frame1", ready)
        time.sleep(.5)
        self.capture(name, ready)

    def gameplay_action(self, action, ready):
        self.before_input()
        request_id = "action-" + uuid.uuid4().hex
        request = {"schema_version": 1, "run_id": self.run_id, "source_sha": self.sha,
                   "request_id": request_id, "action": action}
        path = self.game / "ras-client-qa-action.json"
        temporary = path.with_suffix(".tmp")
        temporary.write_text(json.dumps(request) + "\n")
        self.last_action_ms = int(time.time() * 1000)
        temporary.replace(path)
        deadline = min(self.deadline, time.monotonic() + 60)
        while time.monotonic() < deadline:
            self.check_renderer_failure()
            result = read_json(self.game / "ras-client-qa-gameplay.json")
            if isinstance(result, dict) and result.get("request_id") == request_id:
                validate_gameplay(result, run_id=self.run_id, sha=self.sha, request_id=request_id,
                                  action=action, not_before_ms=self.last_action_ms)
                (self.case / ("gameplay-" + action + ".json")).write_text(json.dumps(result, indent=2) + "\n")
                synchronized = self.wait_ready(ready["state"], since=ready["sequence"],
                    predicate=lambda data: snapshots_equal(data.get("player_variables"), result["after"])
                        and (action != "verify-allocation" or (numeric_close(data.get("movement_speed_base"), .1025)
                             and numeric_close(data.get("movement_speed_value"), .1025))))
                return synchronized
            if self.process.poll() is not None:
                raise QaError(f"Client exited before real gameplay evidence: {action}")
            time.sleep(.2)
        raise QaError(f"Timed out waiting for actual bound-server gameplay action: {action}")

    def exercise_gameplay(self, ready):
        seeded = self.gameplay_action("seed", ready)
        # The normal renderWidget refresh follows packet sync. Do not accept a
        # prior-world 0.105 label merely because the reset variables already arrived.
        seeded = self.wait_ready("ALLOCATION", since=seeded["sequence"], predicate=seeded_agility_preview_ready)
        buttons = [button for button in seeded["buttons"] if button["label"].startswith("Allocate Agility.")]
        if len(buttons) != 1:
            raise QaError("Exactly one actual Agility allocation control is required")
        label = buttons[0]["label"]
        if not seeded_agility_preview_ready(seeded):
            raise QaError("Actual Agility preview must retain its 0.1 to 0.1025 precision")
        button, scale = buttons[0], seeded["actual_gui_scale"]
        self.find_window()
        checked(["xdotool", "mousemove", "--sync", "--window", self.window_id,
                 str(round((button["x"] + button["width"] / 2) * scale)),
                 str(round((button["y"] + button["height"] / 2) * scale))])
        time.sleep(.5)
        self.capture("10-agility-precise-preview", seeded)
        seeded = self.wait_ready("ALLOCATION", since=seeded["sequence"], predicate=seeded_agility_preview_ready)
        label = next(button["label"] for button in seeded["buttons"] if button["label"].startswith("Allocate Agility."))
        self.click(seeded, label)
        allocated = self.wait_ready("ALLOCATION", since=seeded["sequence"],
            predicate=lambda data: isinstance(data.get("player_variables"), dict)
                and numeric_close(data["player_variables"].get("spare_points"), 5)
                and numeric_close(data["player_variables"].get("attribute_points", {}).get("attribute_5"), 1)
                and numeric_close(data["player_variables"].get("attributes", {}).get("attribute_5"), .1025))
        verified = self.gameplay_action("verify-allocation", allocated)
        self.stable_capture("11-real-agility-allocation", verified)
        for index, action in enumerate(("kill-entity", "kill-tag", "invalid-reload", "kill-legacy"), 12):
            verified = self.gameplay_action(action, verified)
            self.stable_capture(f"{index:02d}-{action}", verified)
        return verified

    def exercise_page_pair(self, ready, next_label, previous_label, name):
        """Use real physical next/back controls when this fresh-default view is paged."""
        if not any(button["label"] == next_label and button.get("active") is True for button in ready["buttons"]):
            return ready
        initial_page = ready["page"]
        self.click(ready, next_label)
        paged = self.wait_ready(ready["state"], since=ready["sequence"], page=initial_page + 1)
        self.stable_capture(name + "-next-page", paged)
        self.click(paged, previous_label)
        returned = self.wait_ready(ready["state"], since=paged["sequence"], page=initial_page)
        self.stable_capture(name + "-previous-page", returned)
        self.paging_exercised.append({"state": ready["state"], "from_page": initial_page,
                                     "next_page": paged["page"], "returned_page": returned["page"]})
        return returned

    def before_input(self):
        latest = read_json(self.game / "ras-client-qa-ready.json")
        validate_ready(latest,run_id=self.run_id,sha=self.sha,mc=self.mc,scale=self.scale,launched_ms=self.launched_ms)
        self.last_action_sequence = latest["sequence"]
        return {button["label"] for button in latest["buttons"] if button.get("focused") is True}

    def click(self, ready, label, width=None):
        self.before_input()
        candidates = [button for button in ready["buttons"] if button["label"] == label and button.get("active") is True
                      and (width is None or button["width"] == width)]
        if len(candidates) != 1:
            raise QaError(f"Expected one active actual button: {label}")
        button, scale = candidates[0], ready["actual_gui_scale"]
        self.find_window()
        checked(["xdotool", "mousemove", "--window", self.window_id,
                 str(round((button["x"]+button["width"]/2)*scale)), str(round((button["y"]+button["height"]/2)*scale))])
        checked(["xdotool", "click", "1"])
        self.last_action_ms = int(time.time()*1000)

    def key(self, key):
        previous_focus = self.before_input()
        self.find_window()
        checked(["xdotool", "key", key])
        self.last_action_ms = int(time.time()*1000)
        return previous_focus

    def stop(self):
        (self.game / "ras-client-qa-stop.txt").write_text(self.run_id+" "+self.sha+"\n")
        limit = min(self.deadline, time.monotonic()+90)
        while self.process.poll() is None and time.monotonic() < limit:
            time.sleep(.2)
        if self.process.poll() is None:
            raise QaError("Client failed to shut down and save disposable world within 90 seconds")
        final = read_json(self.game / "ras-client-qa-final.json")
        if self.process.returncode != 0 or not final or final.get("status") != "STOPPED" or final.get("run_id") != self.run_id or final.get("source_sha") != self.sha:
            raise QaError(f"Missing clean run-specific stop evidence (exit {self.process.returncode})")

    def cleanup(self):
        if self.game and self.case:
            for relative in ("logs/latest.log", "ras-client-qa-ready.json", "ras-client-qa-final.json", "qa-world-join.json", "options.txt", "ras-client-qa-action.json", "ras-client-qa-gameplay.json"):
                source = self.game / relative
                if source.is_file():
                    target = self.case / relative.replace("/", "-")
                    shutil.copyfile(source, target)
            crashes = self.game / "crash-reports"
            if crashes.is_dir():
                shutil.copytree(crashes, self.case / "crash-reports", dirs_exist_ok=True)
        # The launcher may have exited while its client survives. Track and terminate only our own session group.
        if self.pgid is not None:
            try:
                os.killpg(self.pgid, signal.SIGTERM)
                limit = time.monotonic()+20
                while time.monotonic() < limit:
                    listing = checked(["ps", "-eo", "pgid=,stat="], capture_output=True, timeout=2).stdout
                    live = any(int(parts[0]) == self.pgid and not parts[1].startswith("Z")
                               for line in listing.splitlines() if len(parts := line.split()) == 2)
                    if not live: break
                    time.sleep(.2)
                else:
                    os.killpg(self.pgid, signal.SIGKILL)
                if self.process and self.process.poll() is None: self.process.wait(timeout=10)
            except ProcessLookupError:
                pass
            finally:
                self.pgid = None
        if self.stdout:
            self.stdout.close()
            self.stdout = None

    def launch(self, scale):
        self.scale = scale
        self.run_id = f"qa-{self.mc.replace('.','_')}-s{scale}-{uuid.uuid4().hex}"
        self.game = self.repo / self.mc / "fabric/runs/client-qa" / self.run_id
        self.case = self.output / f"{self.mc}-fabric-scale{scale}"
        self.game.mkdir(parents=True, exist_ok=False)
        self.case.mkdir(parents=True, exist_ok=False)
        self.window_id = None
        self.last_action_ms = 0
        self.last_action_sequence = 0
        self.paging_exercised = []
        # Every run is disposable. No existing player options/world/result directory is read or overwritten.
        (self.game / "options.txt").write_text(f"guiScale:{scale}\nlang:en_us\nonboardAccessibility:false\ntutorialStep:none\nfullscreen:false\nrenderDistance:2\nsimulationDistance:5\nmaxFps:30\n")
        if self.fixture:
            destination = self.game / "saves" / ("world" if self.mc == "26.3" else "Demo_World")
            destination.parent.mkdir(parents=True)
            shutil.copytree(self.fixture, destination)
        environment = self.environment.copy()
        environment.update({"RAS_CLIENT_QA":"true", "RAS_QA_RUN_ID":self.run_id, "RAS_QA_SOURCE_SHA":self.sha,
                            "RAS_QA_SCALE":str(scale), "LIBGL_ALWAYS_SOFTWARE":"true", "GALLIUM_DRIVER":"llvmpipe"})
        if any(key.startswith("MESA_") and key.endswith("VERSION_OVERRIDE") for key in environment):
            raise QaError("GL/GLSL version overrides are prohibited in real-client QA")
        self.stdout = (self.case / "gradle-client.log").open("w")
        self.launched_ms = int(time.time()*1000)
        self.deadline = time.monotonic()+1200
        (self.case / "launch.json").write_text(json.dumps({"run_id":self.run_id,"source_sha":self.sha,"mc":self.mc,
            "loader":"fabric","requested_gui_scale":scale,"launched_at_ms":self.launched_ms,"physical_size":[WIDTH,HEIGHT],
            "graphics_backend":environment.get("RAS_GRAPHICS_BACKEND", "opengl"),
            "capture":"native-x11grab-without-pointer-overlay","tutorial_step":"none"},indent=2)+"\n")
        self.process = subprocess.Popen(["bash","./gradlew",":fabric:runClientSelfTest","--no-daemon"], cwd=self.repo/self.mc,
                                        env=environment, stdout=self.stdout, stderr=subprocess.STDOUT, start_new_session=True)
        self.pgid = self.process.pid
        try:
            ready = self.wait_ready("ALLOCATION", seconds=1000)
            self.stable_capture("01-allocation", ready)
            previous_focus = self.key("Tab")
            focused = self.wait_ready("ALLOCATION", since=ready["sequence"],
                predicate=lambda data: bool(current := {button["label"] for button in data["buttons"] if button.get("focused") is True})
                                       and current != previous_focus)
            self.stable_capture("02-keyboard-focus", focused)
            if any(button["label"] == "Next attribute page" and button.get("active") is True for button in focused["buttons"]):
                self.click(focused, "Next attribute page")
                paged = self.wait_ready("ALLOCATION", since=focused["sequence"], page=1)
                self.stable_capture("02b-allocation-next-page", paged)
                self.click(paged, "Previous attribute page")
                focused = self.wait_ready("ALLOCATION", since=paged["sequence"], page=0)
            self.click(focused, "View actual combat statistics")
            combat = self.wait_ready("COMBAT", since=focused["sequence"])
            self.stable_capture("03-combat", combat)
            combat = self.exercise_page_pair(combat, "Next combat-stat page", "Previous combat-stat page", "03b-combat")
            self.click(combat, "View player statistics and configured totals")
            overview = self.wait_ready("OVERVIEW_ATTRIBUTES", since=combat["sequence"])
            self.stable_capture("04-overview-attributes", overview)
            overview = self.exercise_page_pair(overview, "Next statistics page", "Previous statistics page", "04b-overview-attributes")
            self.click(overview, "View all configured totals")
            totals = self.wait_ready("OVERVIEW_TOTALS", since=overview["sequence"])
            self.stable_capture("05-overview-totals", totals)
            totals = self.exercise_page_pair(totals, "Next statistics page", "Previous statistics page", "05b-overview-totals")
            self.click(totals, "Return to previous screen", width=52)
            returned = self.wait_ready("COMBAT", since=totals["sequence"])
            self.stable_capture("06-back-to-combat", returned)
            self.click(returned, "View and allocate attributes")
            allocation = self.wait_ready("ALLOCATION", since=returned["sequence"])
            self.stable_capture("07-back-to-allocation", allocation)
            self.key("Escape")
            world = self.wait_ready("WORLD", since=allocation["sequence"])
            self.stable_capture("08-escape-to-real-world", world)
            self.key("k")
            reopened = self.wait_ready("ALLOCATION", since=world["sequence"])
            self.stable_capture("09-keybind-reopened", reopened)
            self.exercise_gameplay(reopened)
            self.stop()
            (self.case / "driver-result.json").write_text(json.dumps({"status":"PASS", "run_id":self.run_id,
                "source_sha":self.sha,"mc":self.mc,"loader":"fabric","gui_scale":scale,
                "paging_exercised":self.paging_exercised,
                "human_pixel_review":"required","coverage":["allocation","keyboard_focus","combat","overview_attributes",
                "overview_totals","available_page_next_back","back","escape","keybind_reopen", "real_ui_allocation",
                "entity_rule_precedence", "builtin_entity_tag", "difficulty_weighting", "armor_weighting",
                "invalid_reload_last_good_rules", "legacy_disabled_reward"]},indent=2)+"\n")
        except Exception:
            raise
        return self.game / "saves" / ("world" if self.mc == "26.3" else "Demo_World")


def export_fixture(destination, world, sha, run_id):
    inventory = world_inventory(world)
    destination.mkdir(parents=True, exist_ok=False)
    shutil.copytree(world, destination/"world")
    (destination/"provenance.json").write_text(json.dumps({"schema_version":1,"source_sha":sha,
        "fixture_kind":"live-client-generated-disposable-demo","producer_mc":"1.21.1","producer_run_id":run_id,
        "world_files":inventory},indent=2)+"\n")
    validate_fixture(destination,sha)


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--workspace", choices=["1.21.1","26.3"], required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--source-sha", required=True)
    parser.add_argument("--fixture-input", type=Path)
    parser.add_argument("--fixture-output", type=Path)
    args=parser.parse_args()
    repo=Path(__file__).resolve().parents[2]
    sha=checked(["git","rev-parse","HEAD"],cwd=repo,capture_output=True).stdout.strip()
    if args.source_sha != sha:
        raise QaError("Checked-out source SHA differs from requested evidence SHA")
    if not os.environ.get("DISPLAY"):
        raise QaError("No actual X11 display provided")
    for tool in ("xdotool","ffmpeg"):
        if not shutil.which(tool): raise QaError(f"Missing real-client QA tool: {tool}")
    args.output.mkdir(parents=True,exist_ok=False)
    try:
        environment = prepare_renderer(args.workspace, args.output)
    except Exception as exc:
        (args.output / "renderer-failure.txt").write_text(str(exc) + "\n")
        raise
    if args.workspace == "1.21.1" and args.fixture_input:
        raise QaError("The bootstrap run must generate its own fresh demo world")
    if args.workspace == "26.3" and args.fixture_output:
        raise QaError("Only the actual 1.21.1 bootstrap may export the demo fixture")
    fixture=validate_fixture(args.fixture_input,args.source_sha) if args.fixture_input else None
    if args.workspace=="26.3" and fixture is None: raise QaError("26.3 requires this workflow's live-client-generated fixture")
    driver=Driver(repo,args.workspace,args.output,args.source_sha,fixture,environment)
    for scale in (2,3,4):
        try:
            generated = driver.launch(scale)
            if scale == 2 and args.workspace == "1.21.1":
                if (generated/"level.dat").stat().st_mtime_ns < driver.launched_ms*1_000_000:
                    raise QaError("Generated/saved demo world is stale")
                if args.fixture_output:
                    export_fixture(args.fixture_output,generated,args.source_sha,driver.run_id)
                    driver.fixture=validate_fixture(args.fixture_output,args.source_sha)
                else:
                    driver.fixture=generated
        except Exception as exc:
            if driver.case:
                (driver.case/"driver-failure.txt").write_text(str(exc)+"\n")
                try: driver.capture("failure-actual-screen",verify=False)
                except Exception as capture_error: (driver.case/"failure-capture-error.txt").write_text(str(capture_error)+"\n")
            raise
        finally:
            driver.cleanup()
    print("PASS actual client navigation/screenshots (human pixel review remains required)")


if __name__ == "__main__":
    main()
