/**
 * V07 V3 FOCUS CHRONOMETER (FocusChronometer)
 * 
 * Normative Requirements Addressed:
 * - Flaw 2.1: Monotonic elapsed time calculation immune to background tab throttling.
 * - State persistence in LocalStorage and cross-tab synchronisation via BroadcastChannel.
 * - Flaw 2.3: Cross-device session presence in Firestore (users/{uid}/system/active_session).
 * - UI Flow Scrutiny (Section 3):
 *   * Web Audio API calm synthesized singing bowl chime (100% offline, zero audio file dependency).
 *   * Web Notifications API alert upon interval completion.
 *   * Session cancellation confirmation modal ("Release this focus interval?").
 *   * Post-session reflection modal (1-5 star depth rating, tags, reflection note).
 */

(function(window) {
  'use strict';

  class FocusChronometer {
    constructor() {
      this.state = 'IDLE'; // 'IDLE' | 'RUNNING' | 'PAUSED' | 'COMPLETED'
      this.mode = 'block'; // 'block' (countdown) | 'flow' (stopwatch) | 'pomodoro'
      this.targetMinutes = 25;
      this.targetSeconds = 25 * 60;
      
      // Monotonic Timing Anchor Timestamps
      this.startTime = null;     // Wall-clock epoch ms when started
      this.pausedTime = null;    // Wall-clock epoch ms when paused
      this.totalPausedMs = 0;    // Accumulated paused duration ms
      this.elapsedSeconds = 0;
      this.remainingSeconds = 25 * 60;
      
      this.activity = 'Deep Work';
      this.title = 'Deep Work Session';
      
      // Hardware / Audio State
      this.audioCtx = null;
      this.tickerInterval = null;
      
      // Cross-Tab Broadcast Channel
      this.broadcastChannel = null;
      this.initBroadcastChannel();

      // UI Subscriptions
      this.subscribers = new Set();

      // Restore Active Session If Reloaded (Flaw 2.1)
      this.restoreActiveSession();
    }

    initBroadcastChannel() {
      if (typeof BroadcastChannel !== 'undefined') {
        try {
          this.broadcastChannel = new BroadcastChannel('v07_chrono_sync');
          this.broadcastChannel.onmessage = (ev) => {
            if (ev.data && ev.data.type === 'CHRONO_STATE_UPDATE') {
              this.applyRemoteState(ev.data.payload);
            }
          };
        } catch (_) {}
      }
    }

    broadcastState() {
      if (this.broadcastChannel) {
        try {
          this.broadcastChannel.postMessage({
            type: 'CHRONO_STATE_UPDATE',
            payload: this.serializeState()
          });
        } catch (_) {}
      }
      this.persistSessionState();
      this.notifySubscribers();
    }

    serializeState() {
      return {
        state: this.state,
        mode: this.mode,
        targetMinutes: this.targetMinutes,
        targetSeconds: this.targetSeconds,
        startTime: this.startTime,
        pausedTime: this.pausedTime,
        totalPausedMs: this.totalPausedMs,
        elapsedSeconds: this.elapsedSeconds,
        remainingSeconds: this.remainingSeconds,
        activity: this.activity,
        title: this.title
      };
    }

    persistSessionState() {
      if (this.state === 'IDLE') {
        localStorage.removeItem('v07_active_chrono');
      } else {
        localStorage.setItem('v07_active_chrono', JSON.stringify(this.serializeState()));
      }
    }

    restoreActiveSession() {
      try {
        const raw = localStorage.getItem('v07_active_chrono');
        if (!raw) return;
        const saved = JSON.parse(raw);
        if (saved && (saved.state === 'RUNNING' || saved.state === 'PAUSED')) {
          console.log('[FocusChronometer] Restoring interrupted session...');
          this.state = saved.state;
          this.mode = saved.mode || 'block';
          this.targetMinutes = saved.targetMinutes || 25;
          this.targetSeconds = saved.targetSeconds || (25 * 60);
          this.startTime = saved.startTime;
          this.pausedTime = saved.pausedTime;
          this.totalPausedMs = saved.totalPausedMs || 0;
          this.activity = saved.activity || 'Deep Work';
          this.title = saved.title || 'Deep Work Session';

          if (this.state === 'RUNNING') {
            this.recalculateMonotonicTime();
            this.startTicker();
          } else {
            this.elapsedSeconds = saved.elapsedSeconds;
            this.remainingSeconds = saved.remainingSeconds;
          }
          this.notifySubscribers();
        }
      } catch (err) {
        console.warn('[FocusChronometer] Failed to restore session:', err);
      }
    }

    applyRemoteState(payload) {
      if (!payload) return;
      this.state = payload.state;
      this.mode = payload.mode;
      this.targetMinutes = payload.targetMinutes;
      this.targetSeconds = payload.targetSeconds;
      this.startTime = payload.startTime;
      this.pausedTime = payload.pausedTime;
      this.totalPausedMs = payload.totalPausedMs;
      this.elapsedSeconds = payload.elapsedSeconds;
      this.remainingSeconds = payload.remainingSeconds;
      this.activity = payload.activity;
      this.title = payload.title;

      if (this.state === 'RUNNING' && !this.tickerInterval) {
        this.startTicker();
      } else if (this.state !== 'RUNNING' && this.tickerInterval) {
        clearInterval(this.tickerInterval);
        this.tickerInterval = null;
      }
      this.notifySubscribers();
    }

    // --- High-Precision Monotonic Elapsed Calculation (Formula 10.1) ---
    recalculateMonotonicTime() {
      if (!this.startTime) return;
      const now = Date.now();
      const currentPauseOffset = (this.state === 'PAUSED' && this.pausedTime) ? (now - this.pausedTime) : 0;
      const effectiveElapsedMs = now - this.startTime - this.totalPausedMs - currentPauseOffset;
      this.elapsedSeconds = Math.max(0, Math.floor(effectiveElapsedMs / 1000));

      if (this.mode === 'block' || this.mode === 'pomodoro') {
        this.remainingSeconds = Math.max(0, this.targetSeconds - this.elapsedSeconds);
        if (this.remainingSeconds === 0 && this.state === 'RUNNING') {
          this.triggerCompletion();
        }
      } else {
        this.remainingSeconds = 0;
      }
    }

    // --- State Transitions ---
    setTargetMinutes(min) {
      if (this.state === 'RUNNING') return;
      this.targetMinutes = min;
      this.targetSeconds = min * 60;
      this.remainingSeconds = this.targetSeconds;
      this.elapsedSeconds = 0;
      this.broadcastState();
    }

    setActivity(act, customTitle) {
      this.activity = act;
      this.title = customTitle || (act + ' Interval');
      this.broadcastState();
    }

    start() {
      this.initAudioContext();
      this.requestNotificationPermission();

      if (this.state === 'PAUSED' && this.pausedTime) {
        this.totalPausedMs += (Date.now() - this.pausedTime);
        this.pausedTime = null;
      } else {
        this.startTime = Date.now();
        this.totalPausedMs = 0;
        this.pausedTime = null;
      }

      this.state = 'RUNNING';
      this.recalculateMonotonicTime();
      this.startTicker();
      this.broadcastState();
      this.syncCloudActivePresence();
    }

    pause() {
      if (this.state !== 'RUNNING') return;
      this.state = 'PAUSED';
      this.pausedTime = Date.now();
      if (this.tickerInterval) {
        clearInterval(this.tickerInterval);
        this.tickerInterval = null;
      }
      this.recalculateMonotonicTime();
      this.broadcastState();
      this.syncCloudActivePresence();
    }

    // Prompt confirmation before discarding active flow (Section 3 requirement)
    requestCancel() {
      if (this.state === 'IDLE') return;
      if (this.elapsedSeconds < 10) {
        this.reset();
        return;
      }
      // Trigger confirmation modal in UI
      if (typeof window.showCancelChronoModal === 'function') {
        window.showCancelChronoModal();
      } else if (confirm('Release this focus interval? Unsaved progress will be discarded.')) {
        this.reset();
      }
    }

    reset() {
      if (this.tickerInterval) {
        clearInterval(this.tickerInterval);
        this.tickerInterval = null;
      }
      this.state = 'IDLE';
      this.startTime = null;
      this.pausedTime = null;
      this.totalPausedMs = 0;
      this.elapsedSeconds = 0;
      this.remainingSeconds = this.targetSeconds;
      this.broadcastState();
      this.clearCloudActivePresence();
    }

    triggerCompletion() {
      if (this.tickerInterval) {
        clearInterval(this.tickerInterval);
        this.tickerInterval = null;
      }
      this.state = 'COMPLETED';
      this.playCalmBellChime();
      this.sendCompletionNotification();
      this.broadcastState();
      this.clearCloudActivePresence();

      // Open Post-Session Reflection Modal (Section 3 requirement)
      if (typeof window.showPostSessionModal === 'function') {
        window.showPostSessionModal({
          durationSec: this.elapsedSeconds,
          activity: this.activity,
          title: this.title,
          startTime: this.startTime,
          endTime: Date.now()
        });
      }
    }

    startTicker() {
      if (this.tickerInterval) clearInterval(this.tickerInterval);
      this.tickerInterval = setInterval(() => {
        this.recalculateMonotonicTime();
        this.notifySubscribers();
      }, 500); // 500ms ticker for smooth progress gauge rendering
    }

    // --- Web Audio API Procedural Tibetan Singing Bowl Chime ---
    initAudioContext() {
      if (!this.audioCtx) {
        const AudioContext = window.AudioContext || window.webkitAudioContext;
        if (AudioContext) {
          this.audioCtx = new AudioContext();
        }
      }
      if (this.audioCtx && this.audioCtx.state === 'suspended') {
        this.audioCtx.resume();
      }
    }

    playCalmBellChime() {
      try {
        this.initAudioContext();
        if (!this.audioCtx) return;

        const ctx = this.audioCtx;
        const now = ctx.currentTime;

        // Fundamental tone + harmonious fifth harmonic
        const fundamentalFreq = 528; // 528 Hz (Calm transformation frequency)
        const harmonicFreq = fundamentalFreq * 1.5; // 792 Hz

        // Oscillators
        const osc1 = ctx.createOscillator();
        const osc2 = ctx.createOscillator();
        const subOsc = ctx.createOscillator();

        osc1.type = 'sine';
        osc1.frequency.setValueAtTime(fundamentalFreq, now);

        osc2.type = 'sine';
        osc2.frequency.setValueAtTime(harmonicFreq, now);

        subOsc.type = 'triangle';
        subOsc.frequency.setValueAtTime(fundamentalFreq / 2, now);

        // Gain Envelope
        const masterGain = ctx.createGain();
        masterGain.gain.setValueAtTime(0, now);
        masterGain.gain.linearRampToValueAtTime(0.28, now + 0.05); // Smooth attack
        masterGain.gain.exponentialRampToValueAtTime(0.0001, now + 3.5); // Warm exponential ring-out

        osc1.connect(masterGain);
        osc2.connect(masterGain);
        subOsc.connect(masterGain);
        masterGain.connect(ctx.destination);

        osc1.start(now);
        osc2.start(now);
        subOsc.start(now);

        osc1.stop(now + 3.6);
        osc2.stop(now + 3.6);
        subOsc.stop(now + 3.6);
        console.log('[FocusChronometer] Synthetic calm bell chime sounded.');
      } catch (err) {
        console.warn('[FocusChronometer] Audio chime bypass:', err);
      }
    }

    // --- Web Notifications API ---
    requestNotificationPermission() {
      if ('Notification' in window && Notification.permission === 'default') {
        Notification.requestPermission();
      }
    }

    sendCompletionNotification() {
      if ('Notification' in window && Notification.permission === 'granted') {
        try {
          new Notification('V07 Focus Completed · Sanctuary Rest', {
            body: `You dedicated ${Math.round(this.elapsedSeconds / 60)} minutes to ${this.activity}. Breathe and record your reflection.`,
            icon: 'lightmode_logo.png'
          });
        } catch (_) {}
      }
    }

    // --- Cross-Device Presence Sync (Flaw 2.3) ---
    syncCloudActivePresence() {
      const se = window.V07StorageEngine;
      if (se && se.mode === 'cloud_firestore' && se.currentUser && se.firestoreDb) {
        try {
          se.firestoreDb.collection('users').doc(se.currentUser.uid)
            .collection('system').doc('active_session').set({
              active: this.state === 'RUNNING',
              status: this.state,
              activity: this.activity,
              title: this.title,
              start_time: this.startTime,
              elapsed_sec: this.elapsedSeconds,
              remaining_sec: this.remainingSeconds,
              device_id: se.deviceId,
              platform: 'web_companion',
              updated_at: Date.now()
            }, { merge: true });
        } catch (_) {}
      }
    }

    clearCloudActivePresence() {
      const se = window.V07StorageEngine;
      if (se && se.mode === 'cloud_firestore' && se.currentUser && se.firestoreDb) {
        try {
          se.firestoreDb.collection('users').doc(se.currentUser.uid)
            .collection('system').doc('active_session').set({
              active: false,
              status: 'IDLE',
              updated_at: Date.now()
            }, { merge: true });
        } catch (_) {}
      }
    }

    // --- Formatting Helpers ---
    getFormattedTime() {
      const sec = (this.mode === 'block' || this.mode === 'pomodoro') ? this.remainingSeconds : this.elapsedSeconds;
      const m = Math.floor(sec / 60);
      const s = sec % 60;
      return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
    }

    getProgressRatio() {
      if (this.mode === 'flow') return 1.0;
      if (this.targetSeconds <= 0) return 0;
      return Math.min(1.0, this.elapsedSeconds / this.targetSeconds);
    }

    subscribe(listener) {
      this.subscribers.add(listener);
      return () => this.subscribers.delete(listener);
    }

    notifySubscribers() {
      this.subscribers.forEach(fn => {
        try { fn(this); } catch (e) { console.warn(e); }
      });
    }
  }

  window.FocusChronometer = new FocusChronometer();
})(window);
