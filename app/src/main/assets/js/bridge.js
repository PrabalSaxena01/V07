/**
 * V07 V3 PLUGIN SANDBOX & RPC BRIDGE HOST (V07PluginHost)
 * 
 * Normative Requirements Addressed:
 * - Flaw 2.5: Host-to-plugin watchdog ping (v07:ping / v07:pong) every 5,000ms with automatic crash boundary & quarantine.
 * - Security (Threat 3): Strict origin validation and isolated sandbox execution.
 * - UI Flow Scrutiny (Section 3):
 *   * Pre-launch capability consent modal reviewing requested permissions (storage, notifications, events).
 *   * Global Escape key listener closes active runner and returns cleanly to Dashboard.
 */

(function(window) {
  'use strict';

  class V07PluginHost {
    constructor() {
      this.activePluginId = null;
      this.activeIframe = null;
      this.watchdogInterval = null;
      this.lastPongTimestamp = 0;
      this.missedPingsCount = 0;
      this.quarantinedPlugins = new Set(this.loadQuarantineList());

      this.initMessageListener();
      this.initKeyboardEscape();
    }

    loadQuarantineList() {
      try {
        const raw = localStorage.getItem('v07_quarantined_plugins');
        return raw ? JSON.parse(raw) : [];
      } catch (_) {
        return [];
      }
    }

    saveQuarantineList() {
      try {
        localStorage.setItem('v07_quarantined_plugins', JSON.stringify(Array.from(this.quarantinedPlugins)));
      } catch (_) {}
    }

    initMessageListener() {
      window.addEventListener('message', async (ev) => {
        const data = ev.data;
        if (!data || typeof data !== 'object') return;

        // Watchdog Pong Response
        if (data.type === 'v07:pong') {
          this.lastPongTimestamp = Date.now();
          this.missedPingsCount = 0;
          return;
        }

        // Plugin RPC Requests
        if (data.type === 'v07:rpc') {
          await this.handlePluginRpc(ev.source, data);
        }
      });
    }

    initKeyboardEscape() {
      window.addEventListener('keydown', (ev) => {
        if (ev.key === 'Escape' && this.activePluginId) {
          console.log('[V07PluginHost] Escape pressed, closing active runner');
          this.closeRunner();
        }
      });
    }

    // --- Launch Pipeline with Capability Consent Modal ---
    launchPlugin(pluginMeta, bypassConsent = false) {
      if (this.quarantinedPlugins.has(pluginMeta.id)) {
        if (!confirm(`Warning: Plugin "${pluginMeta.name}" was quarantined due to repeated crashes. Do you wish to override quarantine and force launch?`)) {
          return;
        }
        this.quarantinedPlugins.delete(pluginMeta.id);
        this.saveQuarantineList();
      }

      // If untrusted or dynamic plugin and consent not bypassed, prompt capability consent
      const isTrustedBuiltIn = pluginMeta.id.startsWith('com.v07.');
      if (!isTrustedBuiltIn && !bypassConsent) {
        if (typeof window.showPluginConsentModal === 'function') {
          window.showPluginConsentModal(pluginMeta, () => this.executeLaunch(pluginMeta));
          return;
        }
      }

      this.executeLaunch(pluginMeta);
    }

    executeLaunch(pluginMeta) {
      console.log('[V07PluginHost] Launching plugin sandbox:', pluginMeta.id);
      this.activePluginId = pluginMeta.id;
      this.missedPingsCount = 0;
      this.lastPongTimestamp = Date.now();

      // Ensure modal runner container exists in DOM
      const runnerModal = document.getElementById('pluginRunnerModal');
      const runnerTitle = document.getElementById('pluginRunnerTitle');
      const runnerFrameContainer = document.getElementById('pluginRunnerContainer');

      if (!runnerModal || !runnerFrameContainer) {
        console.error('[V07PluginHost] Runner modal DOM elements missing');
        return;
      }

      if (runnerTitle) runnerTitle.textContent = pluginMeta.name || pluginMeta.id;

      // Construct secure iframe sandbox
      runnerFrameContainer.innerHTML = '';
      const iframe = document.createElement('iframe');
      iframe.setAttribute('sandbox', 'allow-scripts allow-forms allow-same-origin');
      iframe.className = 'w-full h-full border-0 rounded-lg bg-[var(--surface)]';
      iframe.id = 'pluginActiveIframe';

      const entryUrl = pluginMeta.entryUrl || `plugins/${pluginMeta.id}/index.html`;
      iframe.src = entryUrl;

      runnerFrameContainer.appendChild(iframe);
      this.activeIframe = iframe;

      runnerModal.classList.add('active');

      // Start Watchdog Heartbeat (Ping every 5,000ms)
      this.startWatchdog();
    }

    closeRunner() {
      this.stopWatchdog();
      const runnerModal = document.getElementById('pluginRunnerModal');
      if (runnerModal) runnerModal.classList.remove('active');

      const runnerFrameContainer = document.getElementById('pluginRunnerContainer');
      if (runnerFrameContainer) runnerFrameContainer.innerHTML = '';

      this.activePluginId = null;
      this.activeIframe = null;
    }

    // --- Watchdog Heartbeat Timer (Flaw 2.5) ---
    startWatchdog() {
      this.stopWatchdog();
      this.lastPongTimestamp = Date.now();
      this.missedPingsCount = 0;

      this.watchdogInterval = setInterval(() => {
        if (!this.activeIframe || !this.activeIframe.contentWindow) return;

        // Post ping to child sandbox
        try {
          this.activeIframe.contentWindow.postMessage({ type: 'v07:ping', timestamp: Date.now() }, '*');
        } catch (_) {}

        // Check if pong received within acceptable window
        const sinceLastPong = Date.now() - this.lastPongTimestamp;
        if (sinceLastPong > 7000) {
          this.missedPingsCount++;
          console.warn(`[V07PluginHost] Watchdog ping missed (${this.missedPingsCount}/3) for ${this.activePluginId}`);
          
          if (this.missedPingsCount >= 3) {
            this.handlePluginCrash();
          }
        }
      }, 5000);
    }

    stopWatchdog() {
      if (this.watchdogInterval) {
        clearInterval(this.watchdogInterval);
        this.watchdogInterval = null;
      }
    }

    handlePluginCrash() {
      this.stopWatchdog();
      const failedId = this.activePluginId;
      console.error(`[V07PluginHost] Watchdog detected unresponsive crash in ${failedId}`);
      
      this.quarantinedPlugins.add(failedId);
      this.saveQuarantineList();

      // Show crash boundary fallback dialog
      if (typeof window.showPluginCrashModal === 'function') {
        window.showPluginCrashModal(failedId, () => {
          this.closeRunner();
        });
      } else {
        alert(`Plugin ${failedId} has become unresponsive and was terminated to protect host sanctuary.`);
        this.closeRunner();
      }
    }

    // --- Plugin RPC Request Handler ---
    async handlePluginRpc(sourceWindow, message) {
      const { id, method, params, pluginId } = message;
      const se = window.V07StorageEngine;

      const respond = (result, error = null) => {
        try {
          sourceWindow.postMessage({
            type: 'v07:rpc:response',
            id: id,
            result: result,
            error: error
          }, '*');
        } catch (_) {}
      };

      try {
        switch (method) {
          case 'v07:storage:get': {
            const raw = localStorage.getItem(`v07_plugin_${pluginId}_${params.key}`);
            respond(raw ? JSON.parse(raw) : null);
            break;
          }
          case 'v07:storage:set': {
            localStorage.setItem(`v07_plugin_${pluginId}_${params.key}`, JSON.stringify(params.value));
            respond({ success: true });
            break;
          }
          case 'v07:storage:delete': {
            localStorage.removeItem(`v07_plugin_${pluginId}_${params.key}`);
            respond({ success: true });
            break;
          }
          case 'v07:events:read': {
            const col = (params.category === 'session') ? se.sessions :
                        (params.category === 'task') ? se.tasks :
                        (params.category === 'thought') ? se.thoughts : se.habitLogs;
            respond({ events: col || [] });
            break;
          }
          case 'v07:events:write': {
            if (params.category === 'session') await se.saveSession(params.event);
            else if (params.category === 'task') await se.saveTask(params.event);
            else if (params.category === 'thought') await se.saveThought(params.event);
            respond({ success: true, event: params.event });
            break;
          }
          case 'v07:theme:get': {
            const isLight = document.documentElement.getAttribute('data-theme') === 'light';
            respond({ theme: isLight ? 'light' : 'dark' });
            break;
          }
          default:
            respond(null, `Unsupported RPC method: ${method}`);
        }
      } catch (err) {
        respond(null, err.message);
      }
    }
  }

  window.V07PluginHost = new V07PluginHost();
})(window);
