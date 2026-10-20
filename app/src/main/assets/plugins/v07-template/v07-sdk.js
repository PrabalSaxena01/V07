/**
 * V07 Sanctuary Universal Plugin SDK (v3.0)
 * Fully compliant with docs/v3/PLUGIN_API.md and docs/v3/PLUGIN_SPECIFICATION.md
 * 
 * Provides:
 * - V07Platform.storage (isolated partition get/set/delete/keys/clear, domain events)
 * - V07Platform.events (platform bus on/emit)
 * - V07Platform.ui (toast, openModal, setWidgetBadge, requestResize, close, getTheme)
 * - V07Platform.notifications (schedule, cancel)
 * - V07Platform.telemetry (screen time, focus sessions)
 * 
 * Supports:
 * - Direct native bridge injection (V3NativeBridge)
 * - Parent window bridge (when inside iframe)
 * - Local loopback HTTP server
 * - Offline browser development simulation (localStorage)
 */
(function(window) {
  'use strict';

  // Detect plugin ID from script tag, path, or parent context
  let detectedPluginId = 'com.v07.plugin';
  try {
    const pathParts = window.location.pathname.split('/');
    const pluginIndex = pathParts.indexOf('plugins');
    if (pluginIndex !== -1 && pathParts[pluginIndex + 1]) {
      detectedPluginId = pathParts[pluginIndex + 1];
    } else if (window.__V07_PLUGIN_ID) {
      detectedPluginId = window.__V07_PLUGIN_ID;
    }
  } catch (_) {}

  // Event bus subscription store
  const eventListeners = new Map();

  const V07Platform = {
    version: '3.0.0',
    pluginId: detectedPluginId,

    // Core low-level RPC dispatcher
    invoke: async function(method, params = {}) {
      // 1. Direct native bridge in current context
      if (window.V3NativeBridge && typeof window.V3NativeBridge.invoke === 'function') {
        try {
          const resStr = window.V3NativeBridge.invoke(this.pluginId, method, JSON.stringify(params));
          const res = JSON.parse(resStr);
          if (res.ok) return res.result;
          throw new Error(res.error || 'Native bridge error');
        } catch (e) {
          console.warn('[V07Platform SDK] Direct invoke error:', e);
        }
      }

      // 2. Parent window bridge (when embedded in iframe)
      if (window.parent && window.parent !== window) {
        if (typeof window.parent.nativeInvoke === 'function') {
          return await window.parent.nativeInvoke(method, params);
        }
        if (window.parent.V3NativeBridge && typeof window.parent.V3NativeBridge.invoke === 'function') {
          try {
            const resStr = window.parent.V3NativeBridge.invoke(this.pluginId, method, JSON.stringify(params));
            const res = JSON.parse(resStr);
            if (res.ok) return res.result;
            throw new Error(res.error || 'Native bridge error');
          } catch (e) {
            console.warn('[V07Platform SDK] Parent bridge error:', e);
          }
        }
      }

      // 3. Fallback to Local HTTP Daemon
      try {
        const token = (window.parent && window.parent.V3PlatformHost) ? window.parent.V3PlatformHost.sessionToken : (window.sessionToken || '');
        const resp = await fetch(`/api/v3/plugins/${this.pluginId}/data?method=${encodeURIComponent(method)}`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', 'X-V07-Token': token },
          body: JSON.stringify(params)
        });
        if (resp.ok) return await resp.json();
      } catch (_) {}

      // 4. Standalone browser simulation fallback (offline developer mode)
      return this._simulate(method, params);
    },

    // =========================================================================
    // 1. Storage API (Requires: storage:isolated)
    // =========================================================================
    storage: {
      get: async function(key) {
        try {
          const res = await V07Platform.invoke('storage:get', { key: String(key) });
          if (!res || !res.found || res.value === null || res.value === undefined) return null;
          try { return JSON.parse(res.value); } catch (_) { return res.value; }
        } catch (_) {
          return null;
        }
      },
      set: async function(key, value) {
        const valStr = typeof value === 'string' ? value : JSON.stringify(value);
        return await V07Platform.invoke('storage:set', { key: String(key), value: valStr });
      },
      delete: async function(key) {
        return await V07Platform.invoke('storage:delete', { key: String(key) });
      },
      keys: async function() {
        const res = await V07Platform.invoke('storage:keys', {});
        return (res && res.keys) ? res.keys : [];
      },
      clear: async function() {
        const allKeys = await this.keys();
        for (const k of allKeys) {
          await this.delete(k);
        }
      },
      // Structured Domain Records (Partitioned SQLite plugin_events)
      insertEvent: async function(event) {
        const payload = {
          id: event.id || ('ev_' + Date.now() + '_' + Math.random().toString(36).substr(2, 5)),
          category: event.category || 'general',
          date: event.date || new Date().toISOString().slice(0, 10),
          timestamp: event.timestamp || Date.now(),
          payloadJson: typeof event.payload === 'string' ? event.payload : JSON.stringify(event.payload || {})
        };
        return await V07Platform.invoke('events:insert', payload);
      },
      queryEvents: async function(options = {}) {
        const res = await V07Platform.invoke('events:query', {
          category: options.category || '',
          date: options.date || '',
          limit: options.limit || 100
        });
        return (res && res.events) ? res.events : [];
      },
      deleteEvent: async function(id) {
        return await V07Platform.invoke('events:delete', { id: String(id) });
      }
    },

    // =========================================================================
    // 2. Event Bus API (Requires: events:listen / events:emit)
    // =========================================================================
    events: {
      on: function(eventName, handler) {
        if (!eventListeners.has(eventName)) {
          eventListeners.set(eventName, new Set());
        }
        eventListeners.get(eventName).add(handler);

        // Listen for window postMessages from host shell
        const messageHandler = (e) => {
          if (e.data && e.data.type === 'v07:event' && e.data.event === eventName) {
            handler(e.data.payload);
          }
        };
        window.addEventListener('message', messageHandler);

        // Return unsubscribe function
        return function unsubscribe() {
          const set = eventListeners.get(eventName);
          if (set) set.delete(handler);
          window.removeEventListener('message', messageHandler);
        };
      },
      emit: async function(eventName, payload = {}) {
        // Broadcast locally
        const listeners = eventListeners.get(eventName);
        if (listeners) {
          listeners.forEach(fn => { try { fn(payload); } catch (e) { console.error(e); } });
        }
        // Broadcast to parent shell
        if (window.parent && window.parent !== window) {
          window.parent.postMessage({ type: 'v07:event:emit', pluginId: V07Platform.pluginId, event: eventName, payload }, '*');
        }
        return await V07Platform.invoke('events:emit', { event: eventName, payload });
      }
    },

    // =========================================================================
    // 3. UI & Feedback API
    // =========================================================================
    ui: {
      toast: function(message, type = 'info') {
        if (window.parent && typeof window.parent.showToast === 'function') {
          window.parent.showToast(message, type);
        } else if (window.V3NativeBridge && typeof window.V3NativeBridge.invoke === 'function') {
          window.V3NativeBridge.invoke(V07Platform.pluginId, 'ui:toast', JSON.stringify({ message, type }));
        } else if (window.parent && window.parent.V3NativeBridge && typeof window.parent.V3NativeBridge.invoke === 'function') {
          window.parent.V3NativeBridge.invoke(V07Platform.pluginId, 'ui:toast', JSON.stringify({ message, type }));
        } else {
          console.log([V07 Toast ] );
        }
        try {
          let t = document.getElementById('v07-inpage-toast');
          if (!t) {
            t = document.createElement('div');
            t.id = 'v07-inpage-toast';
            t.style.cssText = 'position:fixed;bottom:24px;left:50%;transform:translateX(-50%) translateY(20px);background:var(--brand-teal,#2E7D6A);color:#FFFFFF;padding:8px 16px;border-radius:9999px;font-size:12px;font-weight:600;box-shadow:0 4px 12px rgba(0,0,0,0.15);z-index:9999;pointer-events:none;opacity:0;transition:all 0.3s cubic-bezier(0.16,1,0.3,1);font-family:var(--font-main,sans-serif);';
            document.body.appendChild(t);
          }
          if (type === 'error') t.style.background = 'var(--cedar,#8C5E58)';
          else if (type === 'info') t.style.background = 'var(--slate,#182230)';
          else t.style.background = 'var(--brand-teal,#2E7D6A)';
          t.textContent = (type === 'success' ? '✓ ' : '') + message;
          t.style.opacity = '1';
          t.style.transform = 'translateX(-50%) translateY(0)';
          clearTimeout(window.__inpageToastTimeout);
          window.__inpageToastTimeout = setTimeout(() => {
            t.style.opacity = '0';
            t.style.transform = 'translateX(-50%) translateY(20px)';
          }, 2400);
        } catch (_) {}
      },
      showToast: function(title, message = '', type = 'info') {
        const full = message ? `${title}: ${message}` : title;
        this.toast(full, type);
      },
      openModal: async function(options) {
        return await V07Platform.invoke('ui:openModal', options || {});
      },
      setWidgetBadge: function(count) {
        if (window.parent && window.parent !== window) {
          window.parent.postMessage({ type: 'v3:widget:badge', pluginId: V07Platform.pluginId, count: Number(count) }, '*');
        }
      },
      requestResize: function(height) {
        if (window.parent && window.parent !== window) {
          window.parent.postMessage({ type: 'v3:widget:resize', pluginId: V07Platform.pluginId, height: Number(height) }, '*');
        }
      },
      close: function() {
        if (window.parent && typeof window.parent.closePluginModal === 'function') {
          window.parent.closePluginModal();
        } else if (window.parent && typeof window.parent.closeCustomModal === 'function') {
          window.parent.closeCustomModal();
        } else {
          window.history.back();
        }
      },
      getTheme: function() {
        try {
          return (window.parent && window.parent.document.documentElement.dataset.theme) ||
                 document.documentElement.dataset.theme ||
                 localStorage.getItem('v07_theme') ||
                 (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
        } catch (_) {
          return 'dark';
        }
      }
    },

    // =========================================================================
    // 4. Host Alarms & Notifications (Requires: notifications:schedule)
    // =========================================================================
    notifications: {
      schedule: async function(options) {
        const res = await V07Platform.invoke('notifications:schedule', {
          title: options.title || 'Sanctuary Reminder',
          body: options.body || '',
          time: options.time || '09:00',
          repeat: options.repeat !== false
        });
        return (res && res.alarmId) ? res.alarmId : ('alarm_' + Date.now());
      },
      cancel: async function(alarmId) {
        return await V07Platform.invoke('notifications:cancel', { alarmId: String(alarmId) });
      }
    },

    // =========================================================================
    // 5. Telemetry Queries (Requires: telemetry:read)
    // =========================================================================
    telemetry: {
      getScreenTimeToday: async function() {
        return await V07Platform.invoke('telemetry:getScreenTime', {});
      },
      getDetailedScreenTime: async function(date) {
        return await V07Platform.invoke('telemetry:getDetailedScreenTime', { date: date || '' });
      },
      getActiveFocusSession: async function() {
        return await V07Platform.invoke('telemetry:getActiveSession', {});
      }
    },

    // Legacy backwards-compatibility alias for older plugins
    ledger: {
      addEntry: async function(entry) {
        return await V07Platform.storage.insertEvent({
          category: entry.category || 'plugin',
          payload: { title: entry.title || 'Activity', content: entry.content || '', timestamp: entry.timestamp || Date.now() }
        });
      }
    },

    // Standalone browser simulator for local development
    _simulate: function(method, params) {
      console.info(`[V07Platform Simulator] ${method}`, params);
      const prefix = `v07_sim_${this.pluginId}_`;
      switch (method) {
        case 'storage:get': {
          const raw = localStorage.getItem(prefix + params.key);
          return { ok: true, found: raw !== null, value: raw };
        }
        case 'storage:set': {
          localStorage.setItem(prefix + params.key, params.value);
          return { ok: true, success: true };
        }
        case 'storage:delete': {
          localStorage.removeItem(prefix + params.key);
          return { ok: true, success: true };
        }
        case 'storage:keys': {
          const keys = [];
          for (let i = 0; i < localStorage.length; i++) {
            const k = localStorage.key(i);
            if (k && k.startsWith(prefix)) keys.push(k.slice(prefix.length));
          }
          return { ok: true, keys };
        }
        case 'events:insert': {
          const evs = JSON.parse(localStorage.getItem(prefix + 'events') || '[]');
          evs.push(params);
          localStorage.setItem(prefix + 'events', JSON.stringify(evs));
          return { ok: true, success: true, id: params.id };
        }
        case 'events:query': {
          const evs = JSON.parse(localStorage.getItem(prefix + 'events') || '[]');
          return { ok: true, events: evs };
        }
        case 'telemetry:getScreenTime': {
          return { ok: true, total_duration_sec: 7200, app_count: 5 };
        }
        default:
          return { ok: true, simulated: true };
      }
    }
  };

  // Synchronize Host Theme
  function syncTheme() {
    const theme = V07Platform.ui.getTheme();
    if (theme === 'light') {
      document.documentElement.setAttribute('data-theme', 'light');
    } else {
      document.documentElement.removeAttribute('data-theme');
    }
  }

  // Register on window under both standard and shorthand names
  window.V07Platform = V07Platform;
  window.v07 = V07Platform;

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', syncTheme);
  } else {
    syncTheme();
  }

  // Dispatch platform ready event
  window.dispatchEvent(new CustomEvent('V07PlatformReady', { detail: V07Platform }));
})(window);
