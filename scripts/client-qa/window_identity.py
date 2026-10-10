#!/usr/bin/env python3
"""Bounded-worker X11 facts for one published SDL XID, never a window search.

ABI: Xlib XWindowAttributes and libXRes 1.2 XRes.h. The server's local
peer PID is authoritative; _NET_WM_PID is only a client-supplied hint.
Run this module in a subprocess with a timeout: native replies may block.
"""
from __future__ import annotations
import argparse
import ctypes as C
import json
import os
from pathlib import Path
import re
import select
import subprocess
import sys
import time


class WindowError(RuntimeError):
    pass


class Attributes(C.Structure):
    _fields_ = [(name, C.c_int) for name in ('x', 'y', 'width', 'height', 'border_width', 'depth')] + [
        ('visual', C.c_void_p), ('root', C.c_ulong), ('window_class', C.c_int),
        ('bit_gravity', C.c_int), ('win_gravity', C.c_int), ('backing_store', C.c_int),
        ('backing_planes', C.c_ulong), ('backing_pixel', C.c_ulong), ('save_under', C.c_int),
        ('colormap', C.c_ulong), ('map_installed', C.c_int), ('map_state', C.c_int),
        ('all_event_masks', C.c_long), ('your_event_mask', C.c_long), ('do_not_propagate_mask', C.c_long),
        ('override_redirect', C.c_int), ('screen', C.c_void_p)]


class ClientRange(C.Structure):
    _fields_ = [('resource_base', C.c_ulong), ('resource_mask', C.c_ulong)]


class ClientSpec(C.Structure):
    _fields_ = [('client', C.c_ulong), ('mask', C.c_uint)]


class ClientValue(C.Structure):
    _fields_ = [('spec', ClientSpec), ('length', C.c_long), ('value', C.c_void_p)]


class XError(C.Structure):
    _fields_ = [('type', C.c_int), ('display', C.c_void_p), ('resourceid', C.c_ulong),
               ('serial', C.c_ulong), ('error_code', C.c_ubyte), ('request_code', C.c_ubyte),
               ('minor_code', C.c_ubyte)]


class NativeDisplay:
    def __init__(self):
        self.name = os.environ.get('DISPLAY', '')
        if not re.fullmatch(r':\d+(?:\.\d+)?', self.name):
            raise WindowError('A local UNIX X11 display is required')
        self.x = C.CDLL('libX11.so.6')
        self.res = C.CDLL('libXRes.so.1')
        def api(lib, name, result, *arguments):
            fn = getattr(lib, name); fn.restype = result; fn.argtypes = arguments
        D, X, I, P = C.c_void_p, C.c_ulong, C.c_int, C.POINTER
        api(self.x, 'XOpenDisplay', D, C.c_char_p)
        api(self.x, 'XCloseDisplay', I, D)
        api(self.x, 'XDefaultRootWindow', X, D)
        api(self.x, 'XGetWindowAttributes', I, D, X, P(Attributes))
        api(self.x, 'XTranslateCoordinates', I, D, X, X, I, I, P(I), P(I), P(X))
        api(self.x, 'XQueryTree', I, D, X, P(X), P(X), P(P(X)), P(C.c_uint))
        api(self.x, 'XGetInputFocus', I, D, P(X), P(I))
        api(self.x, 'XQueryPointer', I, D, X, P(X), P(X), P(I), P(I), P(I), P(I), P(C.c_uint))
        api(self.x, 'XInternAtom', X, D, C.c_char_p, I)
        api(self.x, 'XGetWindowProperty', I, D, X, X, C.c_long, C.c_long, I, X,
            P(X), P(I), P(X), P(X), P(D))
        api(self.x, 'XFree', I, D)
        api(self.x, 'XSync', I, D, I)
        api(self.x, 'XGrabServer', I, D)
        api(self.x, 'XUngrabServer', I, D)
        api(self.x, 'XSetErrorHandler', D, D)
        api(self.x, 'XCreateSimpleWindow', X, D, X, I, I, C.c_uint, C.c_uint, C.c_uint, X, X)
        api(self.x, 'XStoreName', I, D, X, C.c_char_p)
        api(self.x, 'XChangeProperty', I, D, X, X, X, I, I, D, I)
        api(self.x, 'XDeleteProperty', I, D, X, X)
        api(self.x, 'XMapWindow', I, D, X)
        api(self.x, 'XUnmapWindow', I, D, X)
        api(self.x, 'XDestroyWindow', I, D, X)
        api(self.x, 'XRaiseWindow', I, D, X)
        api(self.x, 'XSetInputFocus', I, D, X, I, X)
        api(self.res, 'XResQueryVersion', I, D, P(I), P(I))
        api(self.res, 'XResQueryClients', I, D, P(I), P(P(ClientRange)))
        api(self.res, 'XResQueryClientIds', I, D, C.c_long, P(ClientSpec), P(C.c_long), P(P(ClientValue)))
        api(self.res, 'XResGetClientPid', I, P(ClientValue))
        api(self.res, 'XResClientIdsDestroy', None, C.c_long, P(ClientValue))
        self.errors = []
        self.handler = C.CFUNCTYPE(I, D, P(XError))(self.on_error)
        self.previous_handler = self.x.XSetErrorHandler(C.cast(self.handler, D))
        self.display = self.x.XOpenDisplay(self.name.encode())
        if not self.display: raise WindowError('Cannot open the requested local X11 display')

    def on_error(self, display, event):
        if len(self.errors) < 8:
            self.errors.append({'resource_id': event.contents.resourceid,
                                'code': event.contents.error_code,
                                'request': event.contents.request_code})
        return 0

    def close(self):
        if self.display:
            self.x.XCloseDisplay(self.display); self.display = None
        self.x.XSetErrorHandler(self.previous_handler)

    def property(self, xid, name):
        atom = self.x.XInternAtom(self.display, name.encode(), 1)
        if not atom: return None
        kind, count, remaining, form, pointer = C.c_ulong(), C.c_ulong(), C.c_ulong(), C.c_int(), C.c_void_p()
        result = self.x.XGetWindowProperty(self.display, xid, atom, 0, 1024, 0, 0,
                    C.byref(kind), C.byref(form), C.byref(count), C.byref(remaining), C.byref(pointer))
        try:
            if result != 0 or not kind.value: return None
            if remaining.value: return {'invalid': True}
            if name == '_NET_WM_PID':
                if kind.value != 6 or form.value != 32 or count.value != 1 or not pointer.value:
                    return {'invalid': True}
                return int(C.cast(pointer, C.POINTER(C.c_ulong))[0])
            if form.value != 8 or count.value > 256 or not pointer.value: return {'invalid': True}
            return C.string_at(pointer, count.value).decode('utf-8', errors='replace')
        finally:
            if pointer.value: self.x.XFree(pointer)

    def facts(self, xid):
        if type(xid) is not int or not 0 < xid <= 0xffffffff:
            raise WindowError('Invalid published XID')
        self.errors.clear()
        value = {'schema_version': 1, 'display': self.name, 'xid': xid,
                 'written_at_ms': int(time.time() * 1000), 'x_errors': self.errors}
        self.last_facts = value
        # Keep this observation internally coherent even if another client resizes/destroys a window.
        self.x.XGrabServer(self.display)
        try:
            root = self.x.XDefaultRootWindow(self.display)
            attrs, root_attrs = Attributes(), Attributes()
            value['default_root'] = root
            value['exists'] = bool(self.x.XGetWindowAttributes(self.display, xid, C.byref(attrs)))
            if value['exists']:
                value.update(root=attrs.root, map_state=attrs.map_state, window_class=attrs.window_class,
                             width=attrs.width, height=attrs.height, border_width=attrs.border_width,
                             override_redirect=bool(attrs.override_redirect))
                dx, dy, child = C.c_int(), C.c_int(), C.c_ulong()
                value['translated'] = bool(self.x.XTranslateCoordinates(self.display, xid, root, 0, 0,
                                                        C.byref(dx), C.byref(dy), C.byref(child)))
                value.update(x=dx.value, y=dy.value)
                if self.x.XGetWindowAttributes(self.display, root, C.byref(root_attrs)):
                    value.update(root_width=root_attrs.width, root_height=root_attrs.height)
                value['title'] = self.property(xid, '_NET_WM_NAME')
                if value['title'] is None: value['title'] = self.property(xid, 'WM_NAME')
                value['net_wm_pid'] = self.property(xid, '_NET_WM_PID')
                focus, revert = C.c_ulong(), C.c_int()
                self.x.XGetInputFocus(self.display, C.byref(focus), C.byref(revert))
                value['focused'] = focus.value == xid
                pointer_root, pointer_child, rx, ry, wx, wy, buttons = C.c_ulong(), C.c_ulong(), C.c_int(), C.c_int(), C.c_int(), C.c_int(), C.c_uint()
                value['pointer_same_screen'] = bool(self.x.XQueryPointer(self.display, root,
                    C.byref(pointer_root), C.byref(pointer_child), C.byref(rx), C.byref(ry),
                    C.byref(wx), C.byref(wy), C.byref(buttons)))
                value.update(pointer_on_window=pointer_root.value == root and pointer_child.value == xid,
                             pointer_x=rx.value - dx.value, pointer_y=ry.value - dy.value)
                tree_root, parent, windows, total = C.c_ulong(), C.c_ulong(), C.POINTER(C.c_ulong)(), C.c_uint()
                if not self.x.XQueryTree(self.display, root, C.byref(tree_root), C.byref(parent), C.byref(windows), C.byref(total)):
                    raise WindowError('Cannot inspect exact-window foreground condition')
                try:
                    if total.value > 65536 or (total.value and not windows):
                        raise WindowError('Invalid native root child count')
                    stacking = list(windows[:total.value])
                    value['root_child'] = xid in stacking
                    occluders = 0
                    if value['root_child']:
                        for other in stacking[stacking.index(xid) + 1:]:
                            other_attrs = Attributes()
                            if self.x.XGetWindowAttributes(self.display, other, C.byref(other_attrs)) \
                                    and other_attrs.window_class == 1 and other_attrs.map_state == 2 \
                                    and other_attrs.x < dx.value + attrs.width and other_attrs.y < dy.value + attrs.height \
                                    and other_attrs.x + other_attrs.width + 2 * other_attrs.border_width > dx.value \
                                    and other_attrs.y + other_attrs.height + 2 * other_attrs.border_width > dy.value:
                                occluders += 1
                    value['overlapping_windows_above'] = occluders
                finally:
                    if windows: self.x.XFree(windows)
            major, minor = C.c_int(), C.c_int()
            if not self.res.XResQueryVersion(self.display, C.byref(major), C.byref(minor)):
                raise WindowError('XRes extension is unavailable')
            value['xres_version'] = [major.value, minor.value]
            if major.value != 1 or minor.value < 2: raise WindowError('Compatible XRes 1.2 is required')
            count, ranges = C.c_int(), C.POINTER(ClientRange)()
            if not self.res.XResQueryClients(self.display, C.byref(count), C.byref(ranges)):
                raise WindowError('XRes client ranges are unavailable')
            try:
                if not 0 <= count.value <= 65536 or (count.value and not ranges):
                    raise WindowError('Invalid native XRes client range count')
                matches = [(item.resource_base, item.resource_mask) for item in ranges[:count.value]
                           if xid & (~item.resource_mask & 0xffffffff) == item.resource_base]
            finally:
                if ranges: self.x.XFree(ranges)
            value['matching_client_ranges'] = len(matches)
            if len(matches) == 1:
                value['resource_base'], value['resource_mask'] = matches[0]
            spec, number, identities = ClientSpec(xid, 2), C.c_long(), C.POINTER(ClientValue)()
            # Unlike XResQueryVersion/Clients, QueryClientIds returns Xlib Success (zero).
            status = self.res.XResQueryClientIds(self.display, 1, C.byref(spec), C.byref(number), C.byref(identities))
            try:
                value['xres_query_success'] = status == 0
                value['server_ids'] = [{'resource_base': item.spec.client, 'mask': item.spec.mask,
                    'length_bytes': item.length,
                    'pid': self.res.XResGetClientPid(C.byref(item)) if item.value else -1}
                    for item in identities[:number.value]] if identities and 0 <= number.value <= 16 else []
            finally:
                if identities and 0 <= number.value <= 16:
                    self.res.XResClientIdsDestroy(number.value, identities)
        finally:
            self.x.XUngrabServer(self.display)
            self.x.XSync(self.display, 0)
            value['x_errors'] = [dict(error) for error in self.errors]
        return value


def validate_owner(facts, *, xid, pid, display):
    if not isinstance(facts, dict) or type(facts.get('schema_version')) is not int or facts.get('schema_version') != 1 \
            or facts.get('display') != display or not isinstance(display, str) \
            or not re.fullmatch(r':\d+(?:\.\d+)?', display) \
            or type(xid) is not int or not 0 < xid <= 0xffffffff \
            or type(facts.get('xid')) is not int or facts.get('xid') != xid \
            or type(pid) is not int or pid <= 0:
        raise WindowError('Native display/XID identity differs')
    version = facts.get('xres_version')
    if not isinstance(version, list) or len(version) != 2 or any(type(n) is not int for n in version) \
            or version[0] != 1 or version[1] < 2 or type(facts.get('matching_client_ranges')) is not int \
            or facts.get('matching_client_ranges') != 1 \
            or facts.get('xres_query_success') is not True:
        raise WindowError('Missing unambiguous XRes 1.2 resource-owner proof')
    identities = facts.get('server_ids')
    if type(facts.get('resource_base')) is not int or type(facts.get('resource_mask')) is not int \
            or not 0 <= facts['resource_base'] <= 0xffffffff or not 0 <= facts['resource_mask'] <= 0xffffffff \
            or xid & (~facts['resource_mask'] & 0xffffffff) != facts['resource_base'] \
            or not isinstance(identities, list) or len(identities) != 1 \
            or not isinstance(identities[0], dict) or any(type(n) is not int for n in identities[0].values()) \
            or identities[0] != {
            'resource_base': facts.get('resource_base'), 'mask': 2, 'length_bytes': 4, 'pid': pid}:
        raise WindowError('Exact XID server peer PID differs from the verified client')
    if facts.get('exists') is not True or not isinstance(facts.get('x_errors'), list) or facts['x_errors']:
        raise WindowError('Published XID is absent or generated native X errors')
    if type(facts.get('root')) is not int or not 0 < facts['root'] <= 0xffffffff \
            or type(facts.get('default_root')) is not int or facts.get('root') != facts.get('default_root') \
            or type(facts.get('window_class')) is not int or facts.get('window_class') != 1:
        raise WindowError('Published XID is not an input/output window on this display root')
    if 'net_wm_pid' not in facts or (facts['net_wm_pid'] is not None \
            and (type(facts['net_wm_pid']) is not int or facts['net_wm_pid'] != pid)):
        raise WindowError('Published XID has a conflicting or malformed PID hint')
    return facts


def drawable(facts, *, title, width, height):
    integers = ('map_state', 'width', 'height', 'border_width', 'root_width', 'root_height',
                'x', 'y', 'overlapping_windows_above')
    return all(type(facts.get(key)) is int for key in integers) \
        and facts.get('map_state') == 2 and facts.get('translated') is True \
        and facts.get('root_child') is True and facts.get('overlapping_windows_above') == 0 \
        and facts.get('title') == title and facts.get('width') == width and facts.get('height') == height \
        and facts.get('border_width') == 0 and facts.get('override_redirect') is False \
        and facts.get('root_width') == width and facts.get('root_height') == height \
        and type(facts.get('x')) is int and type(facts.get('y')) is int \
        and 0 <= facts['x'] <= facts['root_width'] - width \
        and 0 <= facts['y'] <= facts['root_height'] - height


def fixture_child():
    native = NativeDisplay()
    try:
        xid = native.x.XCreateSimpleWindow(native.display, native.x.XDefaultRootWindow(native.display),
                                         0, 0, 1280, 960, 0, 0, 0)
        native.x.XStoreName(native.display, xid, b'RAS X11 infrastructure probe')
        native.x.XMapWindow(native.display, xid); native.x.XSync(native.display, 0)
        print(json.dumps({'xid': xid, 'pid': os.getpid()}), flush=True)
        end = time.monotonic() + 15
        while time.monotonic() < end:
            if not select.select([sys.stdin], [], [], .2)[0]: continue
            command = sys.stdin.readline().strip()
            if command == 'exit' or not command: break
            if command == 'unmap': native.x.XUnmapWindow(native.display, xid)
            elif command == 'map': native.x.XMapWindow(native.display, xid)
            elif command.startswith('pid '):
                hint = C.c_ulong(int(command.split()[1]))
                atom = native.x.XInternAtom(native.display, b'_NET_WM_PID', 0)
                native.x.XChangeProperty(native.display, xid, atom, 6, 32, 0, C.byref(hint), 1)
            else: raise WindowError('Unexpected controlled probe command')
            native.x.XSync(native.display, 0); print('{}', flush=True)
        native.x.XDestroyWindow(native.display, xid)
    finally: native.close()


def self_test(output, source_sha):
    if not re.fullmatch(r'[0-9a-f]{40}', source_sha or ''): raise WindowError('Missing preflight source identity')
    report = {'schema_version': 1, 'source_sha': source_sha,
              'kind': 'x11-infrastructure-only-not-minecraft-acceptance', 'status': 'FAIL', 'checks': {}}
    children = []
    native = None
    def response(child):
        if not select.select([child.stdout], [], [], 3)[0]: raise WindowError('Controlled child timed out')
        return json.loads(child.stdout.readline())
    def command(child, text):
        child.stdin.write(text + '\n'); child.stdin.flush(); response(child)
    try:
        native = NativeDisplay()
        for _ in range(2):
            child = subprocess.Popen([sys.executable, str(Path(__file__).resolve()), '--fixture-child'],
                                     stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL,
                                     text=True, env=os.environ.copy())
            children.append(child)
            identity = response(child)
            if identity.get('pid') != child.pid: raise WindowError('Controlled child identity differs')
            child.identity = identity
        foreign, owned = children  # The exact owned test window is topmost.
        xid, pid = owned.identity['xid'], owned.pid
        def observe(): return native.facts(xid)
        facts = observe(); validate_owner(facts, xid=xid, pid=pid, display=native.name)
        if not drawable(facts, title='RAS X11 infrastructure probe', width=1280, height=960):
            raise WindowError('Controlled owned window is not drawable')
        report['owned_window'] = facts
        report['checks']['owned_child_server_pid_and_absent_hint'] = True
        native.x.XSetInputFocus(native.display, xid, 0, 0); native.x.XSync(native.display, 0)
        if observe().get('focused') is not True: raise WindowError('Controlled focus proof failed')
        report['checks']['selected_window_focus_proven'] = True
        native.x.XRaiseWindow(native.display, foreign.identity['xid']); native.x.XSync(native.display, 0)
        if drawable(observe(), title='RAS X11 infrastructure probe', width=1280, height=960):
            raise WindowError('Occluded controlled window accepted as drawable')
        report['checks']['overlapping_foreign_window_not_drawable'] = True
        native.x.XRaiseWindow(native.display, xid); native.x.XSync(native.display, 0)
        for label, current, target in [('foreign_owner', native.facts(foreign.identity['xid']), foreign.identity['xid'])]:
            try: validate_owner(current, xid=target, pid=pid, display=native.name)
            except WindowError: report['checks'][label + '_rejected'] = True
            else: raise WindowError('Foreign controlled owner accepted')
        command(foreign, f'pid {pid}')
        try: validate_owner(native.facts(foreign.identity['xid']), xid=foreign.identity['xid'], pid=pid, display=native.name)
        except WindowError: report['checks']['spoofed_foreign_hint_rejected'] = True
        else: raise WindowError('Spoofed foreign owner accepted')
        command(owned, f'pid {foreign.pid}')
        try: validate_owner(observe(), xid=xid, pid=pid, display=native.name)
        except WindowError: report['checks']['conflicting_owned_hint_rejected'] = True
        else: raise WindowError('Conflicting owned hint accepted')
        command(owned, f'pid {pid}'); command(owned, 'unmap')
        facts = observe(); validate_owner(facts, xid=xid, pid=pid, display=native.name)
        if drawable(facts, title='RAS X11 infrastructure probe', width=1280, height=960):
            raise WindowError('Unmapped controlled window accepted as drawable')
        report['unmapped_window'] = facts; report['checks']['unmapped_window_not_drawable'] = True
        command(owned, 'map')
        native.x.XDestroyWindow(native.display, xid); native.x.XSync(native.display, 0)
        try: validate_owner(observe(), xid=xid, pid=pid, display=native.name)
        except WindowError: report['checks']['destroyed_xid_rejected'] = True
        else: raise WindowError('Destroyed XID accepted')
        report['status'] = 'PASS'
    except Exception as exc:
        report['error_type'] = type(exc).__name__
        raise
    finally:
        if native is not None: native.close()
        for child in children:
            if child.poll() is None:
                child.terminate()
                try: child.wait(timeout=2)
                except subprocess.TimeoutExpired: child.kill(); child.wait(timeout=2)
            child.stdin.close(); child.stdout.close()
        report['explicit_children_reaped'] = all(child.poll() is not None for child in children)
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(report, indent=2) + '\n')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--xid', type=int)
    parser.add_argument('--self-test', action='store_true')
    parser.add_argument('--fixture-child', action='store_true')
    parser.add_argument('--output', type=Path)
    parser.add_argument('--source-sha')
    args = parser.parse_args()
    if args.fixture_child: fixture_child(); return
    if args.self_test:
        if args.output is None: parser.error('--self-test needs --output')
        self_test(args.output, args.source_sha); return
    native = None
    try:
        native = NativeDisplay(); facts = native.facts(args.xid)
        print(json.dumps(facts))
    except Exception as exc:
        facts = dict(getattr(native, 'last_facts', {'schema_version': 1, 'xid': args.xid}))
        facts['error_type'] = type(exc).__name__
        print(json.dumps(facts))
        raise SystemExit(1)
    finally:
        if native is not None: native.close()


if __name__ == '__main__': main()
