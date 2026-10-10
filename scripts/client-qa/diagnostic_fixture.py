"""Pinned PR48-only diagnostic fixture reuse. Never a native acceptance path."""
from __future__ import annotations

from datetime import datetime
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import stat
import subprocess
import urllib.error
import urllib.parse
import urllib.request
import zipfile

REPOSITORY = "MeherBenSalem/RPG-Attribute-System"
REPOSITORY_ID = 896773299
BRANCH = "feat/ras-4.3.0-progression-studio"
PULL_REQUEST_ID = 4802378941
WORKFLOW_ID = 379809394
WORKFLOW_PATH = ".github/workflows/client-qa.yml"
WORKFLOW_NAME = "Real client GUI evidence"
RUN_ID = 38042988005
JOB_ID = 114186793149
PRODUCER_SHA = "8913087ca9484147539a3d608a82aa0e51bbeed7"
PRODUCER_TREE = "bf9c9e84c532310bd7c05445a88253630acb7a62"
PRODUCER_NONCE = "qa-1_21_1-s2-d35b6780fd2d47d8ab9499cc12dab89a"
ARTIFACT_ID = 11667095566
ARTIFACT_NAME = "generated-disposable-demo-fixture"
ARCHIVE_BYTES = 1449114
ARCHIVE_SHA256 = "82bca02d7a0e036b18a03b38df94e5cb12c4450dcc8e27804271f59b1cd1a7f7"
API = f"https://api.github.com/repos/{REPOSITORY}"


class DiagnosticError(RuntimeError):
    pass


def require(condition, message):
    if not condition:
        raise DiagnosticError(message)


def check_pr48_context(environment, target_sha):
    """Both the workflow gate and runtime reject dispatches, forks and other PRs."""
    require(environment.get("GITHUB_ACTIONS") == "true"
            and environment.get("GITHUB_REPOSITORY") == REPOSITORY
            and environment.get("GITHUB_EVENT_NAME") == "pull_request"
            and environment.get("GITHUB_JOB") == "client-263-diagnostic",
            "Diagnostic fixture reuse is restricted to the same-repository PR48 diagnostic job")
    try:
        event = json.loads(Path(environment["GITHUB_EVENT_PATH"]).read_text())
        pr = event["pull_request"]
        require(event["number"] == 48 and pr["number"] == 48 and pr["id"] == PULL_REQUEST_ID
                and pr["head"]["repo"]["full_name"] == REPOSITORY
                and pr["head"]["repo"]["id"] == REPOSITORY_ID
                and pr["base"]["repo"]["full_name"] == REPOSITORY
                and pr["base"]["repo"]["id"] == REPOSITORY_ID
                and pr["head"]["ref"] == BRANCH and pr["head"]["sha"] == target_sha,
                "Diagnostic fixture reuse requires exact PR48 repository, branch and target SHA")
    except (KeyError, TypeError, ValueError, OSError) as exc:
        raise DiagnosticError("Missing or malformed GitHub PR48 event context") from exc


def git(repo, *arguments):
    result = subprocess.run(["git", *arguments], cwd=repo, capture_output=True, text=True, timeout=20)
    require(result.returncode == 0, "Diagnostic source ancestry/tree verification failed: git " + " ".join(arguments))
    return result.stdout.strip()


def check_source(repo, target_sha):
    require(git(repo, "rev-parse", "HEAD") == target_sha, "Diagnostic target SHA differs from the checkout")
    git(repo, "merge-base", "--is-ancestor", PRODUCER_SHA, target_sha)
    producer_tree = git(repo, "rev-parse", f"{PRODUCER_SHA}:1.21.1")
    target_tree = git(repo, "rev-parse", f"{target_sha}:1.21.1")
    require(producer_tree == target_tree == PRODUCER_TREE,
            "The entire 1.21.1 source subtree must be unchanged from the pinned producer")
    require(not git(repo, "status", "--porcelain", "--untracked-files=all", "--", "1.21.1"),
            "The checked-out 1.21.1 source subtree has local changes")
    return {"producer_is_ancestor": True, "producer_1211_tree": producer_tree, "target_1211_tree": target_tree}


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


class GitHubReader:
    """An ephemeral token goes only to api.github.com, never to artifact storage."""
    def __init__(self, token):
        require(bool(token), "Missing ephemeral GITHUB_TOKEN for the pinned GitHub artifact read")
        self.token = token
        self.opener = urllib.request.build_opener(NoRedirect())

    def request(self, url, *, api=True):
        parsed = urllib.parse.urlsplit(url)
        require(parsed.scheme == "https" and not parsed.username and not parsed.password
                and parsed.port in (None, 443) and not parsed.fragment, "Unsafe diagnostic artifact URL")
        if api:
            require(url.startswith(API + "/") or url == API,
                    "Diagnostic token may only be sent to the exact producing GitHub repository API")
            headers = {"Accept": "application/vnd.github+json", "X-GitHub-Api-Version": "2022-11-28",
                       "Authorization": f"Bearer {self.token}"}
        else:
            require(bool(parsed.hostname) and (parsed.hostname.endswith(".blob.core.windows.net")
                    or parsed.hostname.endswith(".actions.githubusercontent.com")),
                    "Unexpected GitHub artifact storage host")
            headers = {}  # No Authorization/cookies are forwarded to the signed storage URL.
        request = urllib.request.Request(url, headers=headers)
        try:
            return self.opener.open(request, timeout=30)
        except urllib.error.HTTPError as exc:
            if api and exc.code == 302:
                return exc
            location = url if api else f"https://{parsed.hostname}/[signed-artifact-path]"
            raise DiagnosticError(f"Pinned artifact read blocked: GET {location} returned HTTP {exc.code}. "
                                  "Only the authorized diagnostic read scope is used; no added credential or permission fallback.") from None
        except (urllib.error.URLError, TimeoutError, OSError):
            location = url if api else f"https://{parsed.hostname}/[signed-artifact-path]"
            raise DiagnosticError(f"Pinned artifact read failed: GET {location}; no credential or permission fallback.") from None

    def json(self, path):
        with self.request(API + path) as response:
            require(response.status == 200, "Unexpected redirect while reading pinned GitHub metadata")
            data = response.read(2 * 1024 * 1024 + 1)
            require(len(data) <= 2 * 1024 * 1024, "Pinned GitHub metadata exceeds the read bound")
        try:
            return json.loads(data)
        except (ValueError, UnicodeDecodeError) as exc:
            raise DiagnosticError("Malformed pinned GitHub metadata") from exc

    def archive(self, destination):
        url = f"{API}/actions/artifacts/{ARTIFACT_ID}/zip"
        with self.request(url) as response:
            require(response.status == 302, "GitHub artifact API did not provide its expected signed redirect")
            location = response.headers.get("Location")
            require(bool(location), "GitHub artifact API returned no signed redirect")
        # The second request is new, unauthenticated, bounded, and also forbids redirects.
        with self.request(location, api=False) as response:
            require(response.status == 200, "Unexpected artifact storage response")
            data = response.read(ARCHIVE_BYTES + 1)
        require(len(data) == ARCHIVE_BYTES and hashlib.sha256(data).hexdigest() == ARCHIVE_SHA256,
                "Downloaded pinned artifact ZIP size/SHA-256 mismatch")
        with destination.open("xb") as handle:
            handle.write(data)


def timestamp(value):
    try:
        return datetime.fromisoformat(value.replace("Z", "+00:00"))
    except (AttributeError, TypeError, ValueError) as exc:
        raise DiagnosticError("Invalid producer metadata timestamp") from exc


def validate_metadata(metadata):
    try:
        repo, workflow, run, job, artifact = (metadata[key] for key in ("repository", "workflow", "run", "job", "artifact"))
        require(repo["id"] == REPOSITORY_ID and repo["full_name"] == REPOSITORY and repo["private"] is False,
                "Pinned producing repository metadata mismatch")
        require(workflow["id"] == WORKFLOW_ID and workflow["path"] == WORKFLOW_PATH
                and workflow["name"] == WORKFLOW_NAME and workflow["state"] == "active",
                "Pinned producing workflow metadata mismatch")
        require(run["id"] == RUN_ID and run["workflow_id"] == WORKFLOW_ID and run["path"] == WORKFLOW_PATH
                and run["head_sha"] == PRODUCER_SHA and run["head_branch"] == BRANCH
                and run["event"] == "pull_request" and run["status"] == "completed" and run["run_attempt"] == 1
                and all(run[key]["id"] == REPOSITORY_ID and run[key]["full_name"] == REPOSITORY
                        for key in ("repository", "head_repository")), "Pinned producing run/head metadata mismatch")
        # Linked PR heads are mutable in historical run responses. Only the run,
        # successful job and artifact identify the producer SHA; preserve the raw
        # linked PR head honestly as descriptive metadata in the evidence receipt.
        require(any(pr["id"] == PULL_REQUEST_ID and pr["number"] == 48
                    and pr["head"]["ref"] == BRANCH
                    and pr["head"]["repo"]["id"] == pr["base"]["repo"]["id"] == REPOSITORY_ID
                    for pr in run["pull_requests"]), "Pinned producer run is not bound to same-repository PR48")
        require(job["id"] == JOB_ID and job["run_id"] == RUN_ID and job["run_attempt"] == 1
                and job["head_sha"] == PRODUCER_SHA and job["head_branch"] == BRANCH
                and job["workflow_name"] == WORKFLOW_NAME and job["name"] == "client-1211"
                and job["status"] == "completed" and job["conclusion"] == "success",
                "Pinned client-1211 producer job was not successful for the exact source/run")
        require(any(step["name"] == "Generate genuine demo world and exercise scales 2 through 4"
                    and step["status"] == "completed" and step["conclusion"] == "success" for step in job["steps"]),
                "Pinned live-client fixture generation step was not successful")
        producer = artifact["workflow_run"]
        require(artifact["id"] == ARTIFACT_ID and artifact["name"] == ARTIFACT_NAME
                and artifact["size_in_bytes"] == ARCHIVE_BYTES and artifact["expired"] is False
                and artifact["digest"] == "sha256:" + ARCHIVE_SHA256
                and artifact["archive_download_url"] == f"{API}/actions/artifacts/{ARTIFACT_ID}/zip"
                and producer["id"] == RUN_ID and producer["head_sha"] == PRODUCER_SHA
                and producer["head_branch"] == BRANCH
                and producer["repository_id"] == producer["head_repository_id"] == REPOSITORY_ID,
                "Pinned fixture artifact identity/digest/producer mismatch")
        created = timestamp(artifact["created_at"])
        require(timestamp(job["started_at"]) <= created <= timestamp(job["completed_at"])
                and any(step["name"] == "Run actions/upload-artifact@v4" and step["number"] == 9
                        and step["status"] == "completed" and step["conclusion"] == "success"
                        and timestamp(step["started_at"]) <= created <= timestamp(step["completed_at"])
                        for step in job["steps"]), "Pinned fixture does not match the successful producer upload step")
    except (KeyError, TypeError) as exc:
        raise DiagnosticError("Incomplete pinned GitHub producer metadata") from exc


def fetch_metadata(reader):
    metadata = {"repository": reader.json(""), "workflow": reader.json(f"/actions/workflows/{WORKFLOW_ID}"),
                "run": reader.json(f"/actions/runs/{RUN_ID}"), "job": reader.json(f"/actions/jobs/{JOB_ID}"),
                "artifact": reader.json(f"/actions/artifacts/{ARTIFACT_ID}")}
    validate_metadata(metadata)
    return metadata


def extract_archive(archive, destination):
    require(archive.is_file() and not archive.is_symlink() and archive.stat().st_size == ARCHIVE_BYTES
            and hashlib.sha256(archive.read_bytes()).hexdigest() == ARCHIVE_SHA256,
            "Pinned fixture archive size/SHA-256 mismatch")
    destination.mkdir(parents=True, exist_ok=False)
    try:
        with zipfile.ZipFile(archive) as bundle:
            seen, total = set(), 0
            for info in bundle.infolist():
                path = PurePosixPath(info.filename)
                mode = info.external_attr >> 16
                require(info.filename not in seen and bool(path.parts) and not path.is_absolute() and ".." not in path.parts
                        and "\\" not in info.filename and path.as_posix() == info.filename.rstrip("/")
                        and (info.filename == "provenance.json" or path.parts[0] == "world")
                        and not stat.S_ISLNK(mode) and (not mode or stat.S_IFMT(mode) in (0, stat.S_IFREG, stat.S_IFDIR)),
                        "Unsafe or duplicate entry in pinned fixture archive")
                seen.add(info.filename)
                total += info.file_size
                require(total <= 256 * 1024 * 1024, "Pinned fixture ZIP exceeds the full-world size bound")
            bundle.extractall(destination)
    except (zipfile.BadZipFile, OSError) as exc:
        raise DiagnosticError("Cannot extract the pinned fixture archive") from exc


def prepare_fixture(repo, target_sha, output, validate_fixture, environment=None):
    environment = dict(os.environ if environment is None else environment)
    check_pr48_context(environment, target_sha)
    source_proof = check_source(repo, target_sha)
    reader = GitHubReader(environment.get("GITHUB_TOKEN"))
    metadata = fetch_metadata(reader)
    archive = output / "diagnostic-producer-fixture.zip"
    reader.archive(archive)
    fixture = output / "diagnostic-producer-fixture"
    extract_archive(archive, fixture)
    world = validate_fixture(fixture, PRODUCER_SHA)  # Original source identity is never rewritten.
    raw = (fixture / "provenance.json").read_bytes()
    provenance = json.loads(raw)
    require(provenance.get("producer_run_id") == PRODUCER_NONCE, "Pinned live-client producer nonce mismatch")
    proof = {"schema_version": 1, "evidence_mode": "pr48-263-diagnostic-only", "native_acceptance": False,
             "source_sha": target_sha, "producer_source_sha": PRODUCER_SHA, "source_proof": source_proof,
             "archive": {"artifact_id": ARTIFACT_ID, "bytes": ARCHIVE_BYTES, "sha256": ARCHIVE_SHA256},
             "github_producer_metadata": metadata, "original_fixture_provenance": provenance,
             "original_fixture_provenance_sha256": hashlib.sha256(raw).hexdigest()}
    (output / "diagnostic-fixture-verification.json").write_text(json.dumps(proof, indent=2) + "\n")
    return world, proof


def evidence_context(proof):
    if proof is None:
        return {}
    return {"evidence_mode": "pr48-263-diagnostic-only", "native_acceptance": False,
            "fixture_producer": proof}


def write_evidence_manifest(case, target_sha, run_id, proof):
    """Bind all diagnostic files, including failure logs/screens, to honest provenance."""
    files = {}
    for path in sorted(case.rglob("*")):
        require(not path.is_symlink(), "Diagnostic evidence contains a symlink")
        if path.is_file() and path.name != "diagnostic-evidence-manifest.json":
            files[path.relative_to(case).as_posix()] = {"bytes": path.stat().st_size,
                "sha256": hashlib.sha256(path.read_bytes()).hexdigest()}
    manifest = {"schema_version": 1, "run_id": run_id, "source_sha": target_sha,
                **evidence_context(proof), "evidence_files": files}
    (case / "diagnostic-evidence-manifest.json").write_text(json.dumps(manifest, indent=2) + "\n")
