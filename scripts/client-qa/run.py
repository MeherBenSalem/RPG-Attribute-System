#!/usr/bin/env python3
"""Drive real Fabric clients on an existing isolated X11 display. Never render a GUI mock."""
from __future__ import annotations
import argparse
import hashlib
import json
import math
import os
from pathlib import Path
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


def checked(command, **kwargs):
    return subprocess.run(command, check=True, text=True, timeout=kwargs.pop("timeout", 20), **kwargs)


def read_json(path):
    try:
        return json.loads(path.read_text())
    except (FileNotFoundError, json.JSONDecodeError):
        return None


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
    def __init__(self, repo, mc, output, sha, fixture):
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

    def wait_ready(self, state, since=0, seconds=60, page=None, predicate=None):
        limit = min(self.deadline, time.monotonic() + seconds)
        last = "No fresh readiness file"
        while time.monotonic() < limit:
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
                 "-i", f"{os.environ['DISPLAY']}+{x},{y}", "-frames:v", "1", "-update", "1", str(path)], capture_output=True)
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
        # Readiness already requires twelve live client ticks. Capture two actual subsequent frames.
        time.sleep(.5)
        self.capture(name+"-frame1", ready)
        time.sleep(.5)
        self.capture(name, ready)

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
            for relative in ("logs/latest.log", "ras-client-qa-ready.json", "ras-client-qa-final.json", "qa-world-join.json", "options.txt"):
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
        # Every run is disposable. No existing player options/world/result directory is read or overwritten.
        (self.game / "options.txt").write_text(f"guiScale:{scale}\nlang:en_us\nonboardAccessibility:false\nfullscreen:false\nrenderDistance:2\nsimulationDistance:5\nmaxFps:30\n")
        if self.fixture:
            destination = self.game / "saves" / ("world" if self.mc == "26.3" else "Demo_World")
            destination.parent.mkdir(parents=True)
            shutil.copytree(self.fixture, destination)
        environment = os.environ.copy()
        environment.update({"RAS_CLIENT_QA":"true", "RAS_QA_RUN_ID":self.run_id, "RAS_QA_SOURCE_SHA":self.sha,
                            "RAS_QA_SCALE":str(scale), "LIBGL_ALWAYS_SOFTWARE":"true", "GALLIUM_DRIVER":"llvmpipe"})
        if any(key.startswith("MESA_") and key.endswith("VERSION_OVERRIDE") for key in environment):
            raise QaError("GL/GLSL version overrides are prohibited in real-client QA")
        self.stdout = (self.case / "gradle-client.log").open("w")
        self.launched_ms = int(time.time()*1000)
        self.deadline = time.monotonic()+1200
        (self.case / "launch.json").write_text(json.dumps({"run_id":self.run_id,"source_sha":self.sha,"mc":self.mc,
            "loader":"fabric","requested_gui_scale":scale,"launched_at_ms":self.launched_ms,"physical_size":[WIDTH,HEIGHT]},indent=2)+"\n")
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
            self.click(combat, "View player statistics and configured totals")
            overview = self.wait_ready("OVERVIEW_ATTRIBUTES", since=combat["sequence"])
            self.stable_capture("04-overview-attributes", overview)
            self.click(overview, "View all configured totals")
            totals = self.wait_ready("OVERVIEW_TOTALS", since=overview["sequence"])
            self.stable_capture("05-overview-totals", totals)
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
            self.stop()
            (self.case / "driver-result.json").write_text(json.dumps({"status":"PASS", "run_id":self.run_id,
                "source_sha":self.sha,"mc":self.mc,"loader":"fabric","gui_scale":scale,
                "human_pixel_review":"required","coverage":["allocation","keyboard_focus","combat","overview_attributes",
                "overview_totals","back","escape","keybind_reopen"]},indent=2)+"\n")
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
    for tool in ("xdotool","ffmpeg","glxinfo"):
        if not shutil.which(tool): raise QaError(f"Missing real-client QA tool: {tool}")
    args.output.mkdir(parents=True,exist_ok=False)
    renderer=checked(["glxinfo","-B"],capture_output=True,env={**os.environ,"LIBGL_ALWAYS_SOFTWARE":"true","GALLIUM_DRIVER":"llvmpipe"}).stdout
    (args.output/"renderer.txt").write_text(renderer)
    if "llvmpipe" not in renderer.lower(): raise QaError("A verified Mesa software renderer is required")
    if args.workspace == "1.21.1" and args.fixture_input:
        raise QaError("The bootstrap run must generate its own fresh demo world")
    if args.workspace == "26.3" and args.fixture_output:
        raise QaError("Only the actual 1.21.1 bootstrap may export the demo fixture")
    fixture=validate_fixture(args.fixture_input,args.source_sha) if args.fixture_input else None
    if args.workspace=="26.3" and fixture is None: raise QaError("26.3 requires this workflow's live-client-generated fixture")
    driver=Driver(repo,args.workspace,args.output,args.source_sha,fixture)
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
