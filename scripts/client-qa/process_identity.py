"""Linux pidfd-bound ownership for one explicitly launched disposable QA task.

Gradle's single-use daemon detaches its session. Ownership follows observed
PID/start-time ancestry, never process-group membership or a name-only search.
Only selected nonsecret facts are exported; argv is used in memory for checks.
"""
from __future__ import annotations
import os
from pathlib import Path
import select
import signal
import time


class OwnershipError(RuntimeError):
    pass


def process_stat(pid):
    text = (Path('/proc') / str(pid) / 'stat').read_text()
    # comm may itself contain spaces or parentheses; fields after its final ')' are fixed.
    fields = text[text.rfind(')') + 2:].split()
    return {'pid': pid, 'state': fields[0], 'ppid': int(fields[1]),
            'pgid': int(fields[2]), 'session': int(fields[3]), 'start_ticks': int(fields[19])}


def process_details(pid):
    directory = Path('/proc') / str(pid)
    value = process_stat(pid)
    uid_line = next(line for line in (directory / 'status').read_text().splitlines() if line.startswith('Uid:'))
    value['uids'] = [int(uid) for uid in uid_line.split()[1:]]
    value['executable'] = str((directory / 'exe').resolve(strict=True))
    value['cwd'] = str((directory / 'cwd').resolve(strict=True))
    value['argv'] = [arg.decode('utf-8') for arg in (directory / 'cmdline').read_bytes().split(b'\0') if arg]
    if process_stat(pid)['start_ticks'] != value['start_ticks']:
        raise OwnershipError('Process identity changed while reading its proof')
    return value


def boot_ticks():
    return int(time.clock_gettime(time.CLOCK_BOOTTIME) * os.sysconf('SC_CLK_TCK'))


class Binding:
    def __init__(self, pid, minimum_start, parent_pid=None):
        self.pid, self.parent_pid = pid, parent_pid
        self.fd = os.pidfd_open(pid, 0)
        try:
            first = process_stat(pid)
            self.start_ticks = first['start_ticks']
            if not self.alive() or first['state'] == 'Z' or self.start_ticks < minimum_start:
                raise OwnershipError('Process is exited or predates this explicit launch')
            self.facts = {key: first[key] for key in ('pid', 'ppid', 'pgid', 'session', 'start_ticks')}
        except Exception:
            os.close(self.fd)
            raise

    def alive(self):
        if self.fd < 0: return False
        if select.select([self.fd], [], [], 0)[0]:
            return False
        try:
            current = process_stat(self.pid)
            return current['start_ticks'] == self.start_ticks and current['state'] != 'Z'
        except (FileNotFoundError, ProcessLookupError):
            return False

    def send(self, sig):
        if not self.alive(): return False
        try:
            # The kernel handle refers to this exact process even if the numeric PID is reused.
            signal.pidfd_send_signal(self.fd, sig, None, 0)
            return True
        except ProcessLookupError:
            return False

    def close(self):
        if self.fd >= 0:
            fd, self.fd = self.fd, -1
            os.close(fd)


class ProcessTracker:
    def __init__(self):
        if not hasattr(os, 'pidfd_open') or not hasattr(signal, 'pidfd_send_signal'):
            raise OwnershipError('Real-client QA requires Linux pidfd process-identity support')
        self.minimum_start = boot_ticks()
        self.bindings = {}
        self.launcher_pid = None
        self.client_pid = None

    def bind_launcher(self, pid):
        binding = Binding(pid, self.minimum_start)
        self.bindings[pid] = binding
        self.launcher_pid = pid
        return dict(binding.facts)

    def refresh(self):
        """Bind only children of a still-live, already bound identity, including detached daemons."""
        candidates = []
        for directory in Path('/proc').iterdir():
            if directory.name.isdecimal():
                try: candidates.append(process_stat(int(directory.name)))
                except (FileNotFoundError, ProcessLookupError, PermissionError): pass
        for _ in range(len(candidates) + 1):
            added = False
            for observed in candidates:
                pid, parent_pid = observed['pid'], observed['ppid']
                if pid in self.bindings or parent_pid not in self.bindings: continue
                parent = self.bindings[parent_pid]
                if not parent.alive(): continue
                child = None
                try:
                    child = Binding(pid, max(self.minimum_start, parent.start_ticks), parent_pid)
                    current = process_details(pid)
                    if current['ppid'] != parent_pid or current['start_ticks'] != observed['start_ticks'] \
                            or current['uids'][:2] != [os.getuid(), os.geteuid()] or not parent.alive() or not child.alive():
                        child.close(); continue
                    self.bindings[pid] = child
                    added = True
                except Exception as exc:
                    if child is not None: child.close()
                    if not isinstance(exc, (FileNotFoundError, ProcessLookupError, PermissionError, OwnershipError)):
                        raise
            if not added: break

    def verify_client(self, pid, *, executable, cwd, properties):
        self.refresh()
        binding = self.bindings.get(pid)
        if binding is None or pid == self.launcher_pid or not binding.alive():
            raise OwnershipError('Client PID is not a live observed descendant of this explicit launcher')
        if self.client_pid is not None and pid != self.client_pid:
            raise OwnershipError('Actual client PID changed after its initial binding')
        current = process_details(pid)
        if current['start_ticks'] != binding.start_ticks or current['uids'][:2] != [os.getuid(), os.geteuid()] \
                or current['executable'] != str(Path(executable).resolve(strict=True)) \
                or current['cwd'] != str(Path(cwd).resolve(strict=True)):
            raise OwnershipError('Client start-time, user, Java executable or disposable cwd differs')
        for key, expected in properties.items():
            values = [arg for arg in current['argv'] if arg.startswith('-D' + key + '=')]
            if values != ['-D' + key + '=' + str(expected)]:
                raise OwnershipError('Actual client JVM argument does not uniquely match: ' + key)
        if not binding.alive(): raise OwnershipError('Client exited or changed identity during verification')
        self.client_pid = pid
        ancestry, seen = [], set()
        node = binding
        while node is not None and node.pid not in seen:
            seen.add(node.pid); ancestry.append(dict(node.facts))
            node = self.bindings.get(node.parent_pid)
        if not ancestry or ancestry[-1]['pid'] != self.launcher_pid:
            raise OwnershipError('Client retained ancestry does not end at this explicit launcher')
        # Deliberately never persist raw command lines or environment variables.
        return {'pid': pid, 'start_ticks': binding.start_ticks, 'executable': current['executable'],
                'cwd': current['cwd'], 'verified_qa_properties': properties, 'observed_ancestry': ancestry,
                'pidfd_bound': True}

    def cleanup(self, seconds=20):
        # Children first. Every signal targets a retained kernel process identity.
        def depth(binding):
            result, seen = 0, set()
            while binding.parent_pid in self.bindings and binding.pid not in seen:
                seen.add(binding.pid); result += 1; binding = self.bindings[binding.parent_pid]
            return result
        report = {'method': 'verified-pidfd-descendants', 'term_pids': [], 'kill_pids': []}
        try:
            try: self.refresh()
            except Exception as exc:
                # Failed discovery cannot authorize any new process or skip already retained identities.
                report['refresh_error_type'] = type(exc).__name__
            bindings = sorted(self.bindings.values(), key=depth, reverse=True)
            for binding in bindings:
                if binding.send(signal.SIGTERM): report['term_pids'].append(binding.pid)
            limit = time.monotonic() + seconds
            while any(binding.alive() for binding in bindings) and time.monotonic() < limit:
                time.sleep(.1)
            for binding in bindings:
                if binding.send(signal.SIGKILL): report['kill_pids'].append(binding.pid)
            return report
        finally:
            for binding in self.bindings.values():
                try: binding.close()
                except OSError: pass
            self.bindings.clear()
