#!/usr/bin/env python3
import os

path = r"D:\software\V07-Android\app\src\main\assets\v3\index.html"
with open(path, "r", encoding="utf-8") as f:
    c = f.read()

# Define callCoreBridge helper
helper = """    function callCoreBridge(method, params) {
      const paramsStr = typeof params === 'string' ? params : JSON.stringify(params || {});
      const bridge = window.V3NativeBridge || (window.parent && window.parent.V3NativeBridge);
      if (bridge && typeof bridge.invoke === 'function') {
        try {
          const resStr = bridge.invoke('core', method, paramsStr);
          try { return JSON.parse(resStr); } catch (_) { return { success: true, result: resStr }; }
        } catch (e) {
          console.warn('V3NativeBridge invoke error:', e);
        }
      }
      return null;
    }
    window.callCoreBridge = callCoreBridge;
"""

# Replace in promptRemovePlugin
old_remove = """        if (window.V07Platform && typeof window.V07Platform.invoke === 'function') {
          try {
            window.V07Platform.invoke('core', 'plugin:uninstall', JSON.stringify({ pluginId: pluginId }));
          } catch(e) {
            console.warn('Backend plugin uninstall invoke warning:', e);
          }
        }"""

new_remove = """        callCoreBridge('plugin:uninstall', { pluginId: pluginId });"""

c = c.replace(old_remove.replace('\r\n', '\n'), new_remove)

# Replace overlay functions
old_overlay = """    function toggleOverlayFromJs() {
      if (window.V07Platform && typeof window.V07Platform.invoke === 'function') {
        try {
          const resStr = window.V07Platform.invoke('core', 'overlay:toggle', '{}');
          const res = JSON.parse(resStr);
          if (res && res.error === 'OVERLAY_PERMISSION_REQUIRED') {
            window.V07Platform.invoke('core', 'overlay:requestPermission', '{}');
          } else {
            refreshOverlayState();
          }
        } catch(e) {
          console.error('Error toggling overlay:', e);
        }
      }
    }

    function openUsagePermissionFromJs() {
      if (window.V07Platform && typeof window.V07Platform.invoke === 'function') {
        try {
          window.V07Platform.invoke('core', 'telemetry:requestPermission', '{}');
        } catch(e) {
          console.error('Error requesting usage permission:', e);
        }
      }
    }

    function refreshOverlayState() {
      if (window.V07Platform && typeof window.V07Platform.invoke === 'function') {
        try {
          const resStr = window.V07Platform.invoke('core', 'overlay:getStatus', '{}');
          const res = JSON.parse(resStr);"""

new_overlay = helper + """
    function toggleOverlayFromJs() {
      const res = callCoreBridge('overlay:toggle', {});
      if (res && res.error === 'OVERLAY_PERMISSION_REQUIRED') {
        callCoreBridge('overlay:requestPermission', {});
      } else {
        refreshOverlayState();
      }
    }

    function openUsagePermissionFromJs() {
      callCoreBridge('telemetry:requestPermission', {});
    }

    function refreshOverlayState() {
      const res = callCoreBridge('overlay:getStatus', {});"""

c_norm = c.replace('\r\n', '\n')
old_overlay_norm = old_overlay.replace('\r\n', '\n')

if old_overlay_norm in c_norm:
    c_norm = c_norm.replace(old_overlay_norm, new_overlay)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(c_norm)
    print("Updated bridge calls in app/src/main/assets/v3/index.html")
else:
    print("old_overlay_norm not found")
