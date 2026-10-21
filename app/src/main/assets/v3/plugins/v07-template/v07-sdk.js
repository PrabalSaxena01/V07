/**
 * V07 Sanctuary Plugin SDK (v3.0)
 * Unified interface for modular companion plugins to interact with the host platform.
 * Supports direct WebView injection, iframe same-origin bridge, and browser local simulation.
 */
(function(window) {
  'use strict';

  // Read plugin ID from manifest or URL
  const pathParts = window.location.pathname.split('/');
  const pluginIndex = pathParts.indexOf('plugins');
  const detectedPluginId = (pluginIndex !== -1 && pathParts[pluginIndex + 1]) ? pathParts[pluginIndex + 1] : 'com.v07.template';

  const v07 = {
    pluginId: detectedPluginId,
    version: '3.0.0',

    // Low-level bridge invoke
    invoke: async function(method, params = {}) {
      // 1. Direct native bridge in current window
      if (window.V3NativeBridge && typeof window.V3NativeBridge.invoke === 'function') {
        try {
          const resStr = window.V3NativeBridge.invoke(this.pluginId, method, JSON.stringify(params));
          const res = JSON.parse(resStr);
          if (res.ok) return res.result;
          throw new Error(res.error || 'Native bridge error');
        } catch (e) {
          console.warn('[V07 SDK] Direct bridge error:', e);
        }
      }

      // 2. Parent window native bridge (when inside iframe)
      if (window.parent && window.parent !== window) {
        if (typeof window.parent.nativeInvoke === 'function') {
          return await window.parent.nativeInvoke(method, params);
        }
        if (window.parent.V3NativeBridge && typeof window.parent.V3NativeBridge.invoke === 'function') {
          const resStr = window.parent.V3NativeBridge.invoke(this.pluginId, method, JSON.stringify(params));
          const res = JSON.parse(resStr);
          if (res.ok) return res.result;
          throw new Error(res.error || 'Native bridge error');
        }
      }

      // 3. Fallback to HTTP API if hosted on local daemon
      try {
        const token = (window.parent && window.parent.V3PlatformHost) ? window.parent.V3PlatformHost.sessionToken : '';
        const resp = await fetch(`/api/v3/plugins/${this.pluginId}/data?method=${encodeURIComponent(method)}`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', 'X-V07-Token': token },
          body: JSON.stringify(params)
        });
        if (resp.ok) return await resp.json();
      } catch (_) {}

      // 4. Standalone browser simulation fallback (for easy offline development)
      return this._simulate(method, params);
    },

    // Persistent Key-Value Storage (Isolated SQLite plugin_storage partition)
    storage: {
      get: async function(key) {
        try {
          const res = await v07.invoke('storage:get', { key });
          if (!res || !res.found || res.value === null) return null;
          try { return JSON.parse(res.value); } catch (_) { return res.value; }
        } catch (_) {
          return null;
        }
      },
      set: async function(key, value) {
        const valStr = typeof value === 'string' ? value : JSON.stringify(value);
        return await v07.invoke('storage:set', { key, value: valStr });
      },
      delete: async function(key) {
        return await v07.invoke('storage:delete', { key });
      },
      keys: async function() {
        const res = await v07.invoke('storage:keys', {});
        return (res && res.keys) ? res.keys : [];
      }
    },

    // Sanctuary Ledger Events API
    ledger: {
      addEntry: async function(entry) {
        const payload = {
          category: entry.category || 'plugin',
          title: entry.title || 'Plugin Activity',
          content: entry.content || '',
          timestamp: entry.timestamp || Date.now()
        };
        return await v07.invoke('ledger:save', payload);
      }
    },

    // UI & Shell Interactions
    ui: {
      showToast: function(title, message = '', type = 'info') {
        if (window.parent && typeof window.parent.showToast === 'function') {
          window.parent.showToast(title, message, type);
        } else {
          console.log(`[Toast ${type.toUpperCase()}] ${title}: ${message}`);
          alert(`${title}\n${message}`);
        }
      },
      close: function() {
        if (window.parent && typeof window.parent.closeCustomModal === 'function') {
          window.parent.closeCustomModal();
        } else {
          window.history.back();
        }
      },
      getTheme: function() {
        try {
          var params = new URLSearchParams(window.location.search);
          var qTheme = params.get('theme');
          if (qTheme === 'light' || qTheme === 'dark') return qTheme;
          if (window.AndroidTheme && typeof window.AndroidTheme.isDark === 'function') {
            return window.AndroidTheme.isDark() ? 'dark' : 'light';
          }
          var local = localStorage.getItem('v07_theme') || localStorage.getItem('v07_app_theme');
          if (local === 'light' || local === 'dark') return local;
          if (window.parent && window.parent !== window && window.parent.document && window.parent.document.documentElement.dataset.theme) {
            return window.parent.document.documentElement.dataset.theme;
          }
          return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
        } catch (_) {
          return 'dark';
        }
      }
    },

    // Browser local storage simulator for standalone development
    _simulate: function(method, params) {
      console.info(`[V07 SDK Simulator] ${method}`, params);
      const storagePrefix = `v07_sim_${this.pluginId}_`;
      switch (method) {
        case 'storage:get': {
          const raw = localStorage.getItem(storagePrefix + params.key);
          return { ok: true, found: raw !== null, value: raw };
        }
        case 'storage:set': {
          localStorage.setItem(storagePrefix + params.key, params.value);
          return { ok: true, success: true };
        }
        case 'storage:delete': {
          localStorage.removeItem(storagePrefix + params.key);
          return { ok: true, success: true };
        }
        case 'storage:keys': {
          const keys = [];
          for (let i = 0; i < localStorage.length; i++) {
            const k = localStorage.key(i);
            if (k.startsWith(storagePrefix)) keys.push(k.slice(storagePrefix.length));
          }
          return { ok: true, keys };
        }
        case 'ledger:save':
          return { ok: true, id: 'sim_' + Date.now(), success: true };
        default:
          return { ok: true, simulated: true };
      }
    }
  };

  // Synchronize host theme
  function syncTheme() {
    const theme = v07.ui.getTheme();
    if (theme === 'light') {
      document.documentElement.setAttribute('data-theme', 'light');
      document.documentElement.classList.add('light');
      document.documentElement.classList.remove('dark');
      if (document.body) { document.body.classList.add('light'); document.body.classList.remove('dark'); }
    } else {
      document.documentElement.setAttribute('data-theme', 'dark');
      document.documentElement.classList.add('dark');
      document.documentElement.classList.remove('light');
      if (document.body) { document.body.classList.add('dark'); document.body.classList.remove('light'); }
    }
  }

  window.v07 = v07;
  window.V07Platform = v07;

  window.addEventListener('storage', syncTheme);
  window.addEventListener('v07-theme-changed', function(e) { if(e.detail && e.detail.theme) { if (e.detail.theme === 'light') { document.documentElement.setAttribute('data-theme', 'light'); document.documentElement.classList.add('light'); document.documentElement.classList.remove('dark'); } else { document.documentElement.setAttribute('data-theme', 'dark'); document.documentElement.classList.add('dark'); document.documentElement.classList.remove('light'); } } });

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', syncTheme);
  } else {
    syncTheme();
  }
})(window);
