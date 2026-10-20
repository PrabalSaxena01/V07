#!/usr/bin/env python3
"""
Comprehensive Fix Script for Sanctuary Web App:
1. Re-injects missing #tab-journal markup with #journal-entries-container
2. Fixes syntax error in refreshOverlayState() (unexpected token 'catch')
3. Fixes openScreenTimeModal() (eliminates broken modal.showModal() on <div>)
4. Implements Open Timer mode as an actual Stopwatch counting upwards (0 -> infinity)
"""

import os
import re

INDEX_PATH = r"D:\software\V07-Android\app\src\main\assets\v3\index.html"

with open(INDEX_PATH, "r", encoding="utf-8") as f:
    c = f.read()

# ---------------------------------------------------------------------------
# 1. INJECT MISSING TAB-JOURNAL MARKUP
# ---------------------------------------------------------------------------
tab_journal_markup = """    </div>

    <!-- ========================================== -->
    <!-- 3. JOURNAL TAB                             -->
    <!-- ========================================== -->
    <div id="tab-journal" class="page-tab flex-col w-full max-w-[680px] mx-auto pb-12">
      <!-- Sanctuary Header -->
      <header class="flex items-end justify-between pt-2 pb-space-lg">
        <div class="flex flex-col gap-1">
          <h2 class="text-2xl sm:text-3xl font-semibold text-on-surface tracking-tight">Journal</h2>
          <p class="font-body-md text-body-md text-on-surface-variant">Somewhere to put your thoughts.</p>
        </div>
        <button onclick="openWriteModal()" class="flex items-center gap-space-xs px-space-md py-2.5 bg-primary text-on-primary rounded-full shadow-[0_4px_16px_rgba(28,97,80,0.18)] active:scale-[0.98] transition-transform cursor-pointer hover:opacity-95" id="write-button">
          <span class="material-symbols-outlined text-[18px]">stylus</span>
          <span class="font-label-lg text-label-lg font-medium">Write</span>
        </button>
      </header>

      <!-- Tactile Search & Filters -->
      <section class="flex flex-col gap-space-sm mb-space-xl">
        <div class="relative w-full flex items-center">
          <span class="material-symbols-outlined absolute left-3.5 text-outline text-[20px] pointer-events-none select-none">search</span>
          <input class="w-full bg-surface-container-lowest text-on-surface placeholder:text-outline pl-11 pr-4 py-3 rounded-xl shadow-sm border border-surface-container-high/40 outline-none focus:bg-surface-container-low transition-colors font-body-md text-body-md" id="journal-search" placeholder="Search your thoughts..." type="text" oninput="applyJournalFilters()"/>
        </div>
        <!-- Soft Horizontal Tag Rail -->
        <div class="flex items-center gap-2 overflow-x-auto no-scrollbar py-1 -mx-margin px-margin">
          <button class="journal-filter-chip px-3.5 py-1.5 rounded-full font-label-md text-label-md bg-primary text-on-primary shrink-0 transition-colors shadow-sm cursor-pointer" data-tag="all" onclick="selectJournalTag('all', this)">
            All
          </button>
          <button class="journal-filter-chip px-3.5 py-1.5 rounded-full font-label-md text-label-md bg-surface-container-low text-on-surface-variant hover:text-on-surface shrink-0 transition-colors cursor-pointer" data-tag="gratitude" onclick="selectJournalTag('gratitude', this)">
            #gratitude
          </button>
          <button class="journal-filter-chip px-3.5 py-1.5 rounded-full font-label-md text-label-md bg-surface-container-low text-on-surface-variant hover:text-on-surface shrink-0 transition-colors cursor-pointer" data-tag="focus" onclick="selectJournalTag('focus', this)">
            #focus
          </button>
          <button class="journal-filter-chip px-3.5 py-1.5 rounded-full font-label-md text-label-md bg-surface-container-low text-on-surface-variant hover:text-on-surface shrink-0 transition-colors cursor-pointer" data-tag="calm" onclick="selectJournalTag('calm', this)">
            #calm
          </button>
          <button class="journal-filter-chip px-3.5 py-1.5 rounded-full font-label-md text-label-md bg-surface-container-low text-on-surface-variant hover:text-on-surface shrink-0 transition-colors cursor-pointer" data-tag="morning" onclick="selectJournalTag('morning', this)">
            #morning
          </button>
          <button class="journal-filter-chip px-3.5 py-1.5 rounded-full font-label-md text-label-md bg-surface-container-low text-on-surface-variant hover:text-on-surface shrink-0 transition-colors cursor-pointer" data-tag="reading" onclick="selectJournalTag('reading', this)">
            #reading
          </button>
        </div>
      </section>

      <!-- Lined Linen Notebook Canvas -->
      <div class="relative w-full rounded-2xl bg-surface-container-lowest p-6 shadow-sm border border-surface-container-high/40" style="box-shadow: var(--card-shadow);">
        <!-- Faint Notebook Linen Accent Bookmark -->
        <div class="absolute -top-1.5 left-10 w-6 h-3 bg-secondary-fixed rounded-b-sm shadow-sm opacity-80 pointer-events-none"></div>

        <div class="flex flex-col gap-space-2xl" id="journal-entries-container">
          <!-- Populated dynamically with Today, Yesterday, earlier thoughts -->
        </div>

        <!-- Reflection Prompt Card -->
        <div class="mt-space-2xl pt-space-lg flex flex-col items-center text-center bg-surface-container-low rounded-xl p-space-lg border border-surface-container-high/30">
          <div class="w-8 h-8 rounded-full bg-surface-container flex items-center justify-center text-secondary mb-2">
            <span class="material-symbols-outlined text-[18px]">nature</span>
          </div>
          <p class="font-body-md text-body-md text-on-surface font-medium">How was your evening pause?</p>
          <p class="font-body-sm text-body-sm text-on-surface-variant mt-0.5">Capture a gentle thought before the sun sets.</p>
          <button onclick="openWriteModal()" class="mt-3 px-4 py-1.5 bg-surface-container-lowest text-primary rounded-full font-label-md text-label-md shadow-sm hover:bg-surface-bright transition-colors cursor-pointer active:scale-95 font-medium border border-surface-container-high/40">
            Reflect now
          </button>
        </div>
      </div>
    </div>
"""

old_day_tab_header = """    </div>

    <!-- ========================================== -->
    <!-- 4. DAY TAB                                 -->
    <!-- ========================================== -->
    <div id="tab-day\""""

if 'id="tab-journal"' not in c:
    c = c.replace(old_day_tab_header, tab_journal_markup + "\n    <!-- ========================================== -->\n    <!-- 4. DAY TAB                                 -->\n    <div id=\"tab-day\"")
    print("Injected tab-journal markup")

# ---------------------------------------------------------------------------
# 2. FIX SYNTAX ERROR IN refreshOverlayState()
# ---------------------------------------------------------------------------
old_refresh = """    function refreshOverlayState() {
      const res = callCoreBridge('overlay:getStatus', {});
          if (res && res.success && res.result) {
            const running = res.result.running === true;
            const hasUsage = res.result.hasUsagePermission === true;

            // Update Tools badge & button
            const hudBadge = document.getElementById('hud-status-badge');
            if (hudBadge) {
              hudBadge.textContent = running ? 'HUD Active' : 'HUD Standby';
              hudBadge.className = running 
                ? 'px-space-xs py-0.5 rounded-full bg-primary/20 text-primary font-label-sm text-label-sm font-semibold'
                : 'px-space-xs py-0.5 rounded-full bg-surface-container-low text-secondary font-label-sm text-label-sm font-medium';
            }
            const toggleBtn = document.getElementById('tools-overlay-toggle-btn');
            if (toggleBtn) {
              toggleBtn.textContent = running ? 'Stop HUD' : 'Toggle HUD';
              toggleBtn.className = running
                ? 'h-8 px-3 rounded-full bg-secondary text-on-secondary font-label-sm text-xs font-semibold cursor-pointer active:scale-95'
                : 'h-8 px-3 rounded-full bg-primary text-on-primary font-label-sm text-xs font-semibold cursor-pointer active:scale-95';
            }

            // Update Settings switch
            const settingsBadge = document.getElementById('settings-hud-status');
            if (settingsBadge) {
              settingsBadge.textContent = running ? 'Active' : 'Standby';
              settingsBadge.className = running 
                ? 'px-2 py-0.2 rounded-full text-[10px] font-semibold bg-primary/20 text-primary'
                : 'px-2 py-0.2 rounded-full text-[10px] font-semibold bg-surface-container text-secondary';
            }
            const switchBtn = document.getElementById('btn-toggle-overlay-settings');
            const switchThumb = document.getElementById('toggle-overlay-thumb');
            const switchIcon = document.getElementById('toggle-overlay-icon');
            if (switchBtn && switchThumb) {
              if (running) {
                switchBtn.className = 'toggle-switch shrink-0 w-12 h-7 rounded-full bg-primary p-0.5 transition-colors relative flex items-center cursor-pointer';
                switchThumb.className = 'w-6 h-6 rounded-full bg-surface-container-lowest shadow-sm transform translate-x-5 transition-transform flex items-center justify-center';
                if (switchIcon) {
                  switchIcon.className = 'material-symbols-outlined text-[12px] text-primary opacity-100';
                }
              } else {
                switchBtn.className = 'toggle-switch shrink-0 w-12 h-7 rounded-full bg-surface-container-high p-0.5 transition-colors relative flex items-center cursor-pointer';
                switchThumb.className = 'w-6 h-6 rounded-full bg-surface-container-lowest shadow-sm transform translate-x-0 transition-transform flex items-center justify-center';
                if (switchIcon) {
                  switchIcon.className = 'material-symbols-outlined text-[12px] text-outline opacity-0';
                }
              }
            }

            // Update Usage Access badge
            const usageBadge = document.getElementById('usage-perm-badge');
            if (usageBadge) {
              usageBadge.textContent = hasUsage ? 'Granted' : 'Permission Required';
              usageBadge.className = hasUsage 
                ? 'px-2.5 py-1 rounded-full bg-primary/10 text-primary font-label-sm text-xs font-semibold'
                : 'px-2.5 py-1 rounded-full bg-error-container text-on-error-container font-label-sm text-xs font-semibold';
            }
          }
        } catch(e) {}
      }
    }"""

new_refresh = """    function refreshOverlayState() {
      try {
        const res = callCoreBridge('overlay:getStatus', {});
        if (res && res.success && res.result) {
          const running = res.result.running === true;
          const hasUsage = res.result.hasUsagePermission === true;

          // Update Tools badge & button
          const hudBadge = document.getElementById('hud-status-badge');
          if (hudBadge) {
            hudBadge.textContent = running ? 'HUD Active' : 'HUD Standby';
            hudBadge.className = running 
              ? 'px-space-xs py-0.5 rounded-full bg-primary/20 text-primary font-label-sm text-label-sm font-semibold'
              : 'px-space-xs py-0.5 rounded-full bg-surface-container-low text-secondary font-label-sm text-label-sm font-medium';
          }
          const toggleBtn = document.getElementById('tools-overlay-toggle-btn');
          if (toggleBtn) {
            toggleBtn.textContent = running ? 'Stop HUD' : 'Toggle HUD';
            toggleBtn.className = running
              ? 'h-8 px-3 rounded-full bg-secondary text-on-secondary font-label-sm text-xs font-semibold cursor-pointer active:scale-95'
              : 'h-8 px-3 rounded-full bg-primary text-on-primary font-label-sm text-xs font-semibold cursor-pointer active:scale-95';
          }

          // Update Settings switch
          const settingsBadge = document.getElementById('settings-hud-status');
          if (settingsBadge) {
            settingsBadge.textContent = running ? 'Active' : 'Standby';
            settingsBadge.className = running 
              ? 'px-2 py-0.2 rounded-full text-[10px] font-semibold bg-primary/20 text-primary'
              : 'px-2 py-0.2 rounded-full text-[10px] font-semibold bg-surface-container text-secondary';
          }
          const switchBtn = document.getElementById('btn-toggle-overlay-settings');
          const switchThumb = document.getElementById('toggle-overlay-thumb');
          const switchIcon = document.getElementById('toggle-overlay-icon');
          if (switchBtn && switchThumb) {
            if (running) {
              switchBtn.className = 'toggle-switch shrink-0 w-12 h-7 rounded-full bg-primary p-0.5 transition-colors relative flex items-center cursor-pointer';
              switchThumb.className = 'w-6 h-6 rounded-full bg-surface-container-lowest shadow-sm transform translate-x-5 transition-transform flex items-center justify-center';
              if (switchIcon) {
                switchIcon.className = 'material-symbols-outlined text-[12px] text-primary opacity-100';
              }
            } else {
              switchBtn.className = 'toggle-switch shrink-0 w-12 h-7 rounded-full bg-surface-container-high p-0.5 transition-colors relative flex items-center cursor-pointer';
              switchThumb.className = 'w-6 h-6 rounded-full bg-surface-container-lowest shadow-sm transform translate-x-0 transition-transform flex items-center justify-center';
              if (switchIcon) {
                switchIcon.className = 'material-symbols-outlined text-[12px] text-outline opacity-0';
              }
            }
          }

          // Update Usage Access badge
          const usageBadge = document.getElementById('usage-perm-badge');
          if (usageBadge) {
            usageBadge.textContent = hasUsage ? 'Granted' : 'Permission Required';
            usageBadge.className = hasUsage 
              ? 'px-2.5 py-1 rounded-full bg-primary/10 text-primary font-label-sm text-xs font-semibold'
              : 'px-2.5 py-1 rounded-full bg-error-container text-on-error-container font-label-sm text-xs font-semibold';
          }
        }
      } catch (e) {
        console.warn('Error refreshing overlay state:', e);
      }
    }"""

c_norm = c.replace('\r\n', '\n')
old_refresh_norm = old_refresh.replace('\r\n', '\n')
if old_refresh_norm in c_norm:
    c_norm = c_norm.replace(old_refresh_norm, new_refresh)
    print("Fixed refreshOverlayState syntax error")
else:
    print("old_refresh_norm not found")

# ---------------------------------------------------------------------------
# 3. FIX openScreenTimeModal() & REMOVE BROKEN showModal() WRAPPER
# ---------------------------------------------------------------------------
old_open_screentime = """    function openScreenTimeModal() {
      const modal = document.getElementById('modal-screentime');
      if (modal) {
        modal.classList.remove('translate-y-full', 'opacity-0', 'pointer-events-none');
        modal.classList.add('translate-y-0', 'opacity-100', 'pointer-events-auto');
        notifyNativeFullscreen(true);
        loadLiveScreenTime();
      }
    }"""

new_open_screentime = """    function openScreenTimeModal() {
      const modal = document.getElementById('modal-screentime');
      if (modal) {
        modal.classList.remove('translate-y-full', 'opacity-0', 'pointer-events-none');
        modal.classList.add('translate-y-0', 'opacity-100', 'pointer-events-auto');
        notifyNativeFullscreen(true);
        if (typeof loadScreenTimeLive === 'function') {
          loadScreenTimeLive();
        }
      }
    }"""

old_screentime_override = """    // Wrap openScreenTimeModal to load live data
    const originalOpenScreenTime = window.openScreenTimeModal;
    window.openScreenTimeModal = function() {
      const modal = document.getElementById('modal-screentime');
      if (modal) modal.showModal();
      loadScreenTimeLive();
    };"""

new_screentime_override = """    // Ensure openScreenTimeModal uses full-view container and loads live data
    window.openScreenTimeModal = openScreenTimeModal;"""

c_norm = c_norm.replace(old_open_screentime.replace('\r\n', '\n'), new_open_screentime)
c_norm = c_norm.replace(old_screentime_override.replace('\r\n', '\n'), new_screentime_override)

# ---------------------------------------------------------------------------
# 4. IMPLEMENT OPEN TIMER AS STOPWATCH (0 -> INFINITY)
# ---------------------------------------------------------------------------
old_focus_timer_block = """    function setFocusMode(mode) {
      const btnP = document.getElementById('focus-mode-pomodoro');
      const btnT = document.getElementById('focus-mode-timer');
      if (mode === 'pomodoro') {
        btnP.className = 'flex-1 py-2 px-3 rounded-lg font-semibold text-xs transition-all flex items-center justify-center gap-1.5 bg-surface-container-lowest text-primary shadow-xs cursor-pointer';
        btnT.className = 'flex-1 py-2 px-3 rounded-lg font-semibold text-xs transition-all flex items-center justify-center gap-1.5 text-on-surface-variant hover:text-on-surface cursor-pointer';
        setFocusPreset(25);
      } else {
        btnT.className = 'flex-1 py-2 px-3 rounded-lg font-semibold text-xs transition-all flex items-center justify-center gap-1.5 bg-surface-container-lowest text-primary shadow-xs cursor-pointer';
        btnP.className = 'flex-1 py-2 px-3 rounded-lg font-semibold text-xs transition-all flex items-center justify-center gap-1.5 text-on-surface-variant hover:text-on-surface cursor-pointer';
        setFocusPreset(45);
      }
    }

    function setFocusPreset(mins) {
      focusModalTotalSeconds = mins * 60;
      focusModalSeconds = focusModalTotalSeconds;
      document.getElementById('focus-target-duration').textContent = `${mins}m Block`;
      renderFocusModal();
    }

    function adjustFocusMinutes(delta) {
      focusModalSeconds = Math.max(60, focusModalSeconds + (delta * 60));
      focusModalTotalSeconds = Math.max(focusModalSeconds, focusModalTotalSeconds);
      renderFocusModal();
    }

    function renderFocusModal() {
      try {
        const mins = Math.floor(focusModalSeconds / 60);
        const secs = focusModalSeconds % 60;
        const timeStr = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
        const modalTime = document.getElementById('focus-modal-time');
        if (modalTime) modalTime.textContent = timeStr;
        const preview = document.getElementById('tools-focus-time-preview');
        if (preview) preview.textContent = timeStr;
        const homeTimer = document.getElementById('home-timer-display');
        if (homeTimer) homeTimer.textContent = timeStr;

        // Update ring
        const ring = document.getElementById('focus-progress-ring');
        if (ring) {
          const perimeter = 2 * Math.PI * 100; // ~628.31
          const frac = focusModalTotalSeconds > 0 ? (focusModalSeconds / focusModalTotalSeconds) : 0;
          const offset = perimeter * (1 - frac);
          ring.style.strokeDashoffset = offset;
        }
      } catch (e) {
        console.warn('renderFocusModal notice:', e);
      }
    }

    function toggleFocusTimer() {
      const toggleBtn = document.getElementById('focus-modal-toggle-btn');
      const icon = document.getElementById('focus-modal-play-icon');
      const text = document.getElementById('focus-modal-play-text');
      const phase = document.getElementById('focus-modal-phase');

      if (focusModalRunning) {
        clearInterval(focusModalInterval);
        focusModalRunning = false;
        icon.textContent = 'play_arrow';
        text.textContent = 'Resume focus';
        phase.innerHTML = '<span class="material-symbols-outlined text-[14px]">pause</span><span>Paused</span>';
        apiV3('/api/v3/sessions/pause', { method: 'POST' }).catch(() => {});
      } else {
        focusModalRunning = true;
        icon.textContent = 'pause';
        text.textContent = 'Pause focus';
        phase.innerHTML = '<span class="material-symbols-outlined text-[14px]">lock_clock</span><span>Focus Active</span>';

        apiV3('/api/v3/sessions/start', {
          method: 'POST',
          headers: {'Content-Type': 'application/json'},
          body: JSON.stringify({ topic: 'Deep Presence', category: 'focus', mode: 'COUNTDOWN', duration_minutes: Math.ceil(focusModalSeconds / 60) })
        }).catch(() => {});

        focusModalInterval = setInterval(() => {
          if (focusModalSeconds > 0) {
            focusModalSeconds--;
            renderFocusModal();
          } else {
            clearInterval(focusModalInterval);
            focusModalRunning = false;
            icon.textContent = 'check_circle';
            text.textContent = 'Complete';
            phase.innerHTML = '<span class="material-symbols-outlined text-[14px]">check</span><span>Interval Complete</span>';
            apiV3('/api/v3/sessions/finish', { method: 'POST' }).catch(() => {});
          }
        }, 1000);
      }
    }

    function resetFocusTimer() {
      clearInterval(focusModalInterval);
      focusModalRunning = false;
      focusModalSeconds = focusModalTotalSeconds;
      renderFocusModal();
      document.getElementById('focus-modal-play-icon').textContent = 'play_arrow';
      document.getElementById('focus-modal-play-text').textContent = 'Start focus';
      document.getElementById('focus-modal-phase').innerHTML = '<span class="material-symbols-outlined text-[14px]">restart_alt</span><span>Ready</span>';
    }"""

new_focus_timer_block = """    let focusCurrentMode = 'pomodoro'; // 'pomodoro' (countdown) or 'timer' (stopwatch 0 -> infinity)

    function setFocusMode(mode) {
      focusCurrentMode = mode;
      const btnP = document.getElementById('focus-mode-pomodoro');
      const btnT = document.getElementById('focus-mode-timer');
      clearInterval(focusModalInterval);
      focusModalRunning = false;
      const playIcon = document.getElementById('focus-modal-play-icon');
      const playText = document.getElementById('focus-modal-play-text');
      if (playIcon) playIcon.textContent = 'play_arrow';
      if (playText) playText.textContent = 'Start focus';

      if (mode === 'pomodoro') {
        if (btnP) btnP.className = 'flex-1 py-2 px-3 rounded-lg font-semibold text-xs transition-all flex items-center justify-center gap-1.5 bg-surface-container-lowest text-primary shadow-xs cursor-pointer';
        if (btnT) btnT.className = 'flex-1 py-2 px-3 rounded-lg font-semibold text-xs transition-all flex items-center justify-center gap-1.5 text-on-surface-variant hover:text-on-surface cursor-pointer';
        setFocusPreset(25);
      } else {
        // Stopwatch Mode: 0 -> infinity
        if (btnT) btnT.className = 'flex-1 py-2 px-3 rounded-lg font-semibold text-xs transition-all flex items-center justify-center gap-1.5 bg-surface-container-lowest text-primary shadow-xs cursor-pointer';
        if (btnP) btnP.className = 'flex-1 py-2 px-3 rounded-lg font-semibold text-xs transition-all flex items-center justify-center gap-1.5 text-on-surface-variant hover:text-on-surface cursor-pointer';
        focusModalSeconds = 0;
        focusModalTotalSeconds = 0;
        const durLabel = document.getElementById('focus-target-duration');
        if (durLabel) durLabel.textContent = 'Open Flow (0 -> ∞)';
        const phase = document.getElementById('focus-modal-phase');
        if (phase) phase.innerHTML = '<span class="material-symbols-outlined text-[14px]">all_inclusive</span><span>Stopwatch Ready</span>';
        renderFocusModal();
      }
    }

    function setFocusPreset(mins) {
      focusCurrentMode = 'pomodoro';
      focusModalTotalSeconds = mins * 60;
      focusModalSeconds = focusModalTotalSeconds;
      const durLabel = document.getElementById('focus-target-duration');
      if (durLabel) durLabel.textContent = `${mins}m Block`;
      const phase = document.getElementById('focus-modal-phase');
      if (phase) phase.innerHTML = '<span class="material-symbols-outlined text-[14px]">lock_clock</span><span>Ready</span>';
      renderFocusModal();
    }

    function adjustFocusMinutes(delta) {
      if (focusCurrentMode === 'timer') {
        focusModalSeconds = Math.max(0, focusModalSeconds + (delta * 60));
      } else {
        focusModalSeconds = Math.max(60, focusModalSeconds + (delta * 60));
        focusModalTotalSeconds = Math.max(focusModalSeconds, focusModalTotalSeconds);
      }
      renderFocusModal();
    }

    function renderFocusModal() {
      try {
        let timeStr;
        const ring = document.getElementById('focus-progress-ring');
        const perimeter = 2 * Math.PI * 100; // ~628.31

        if (focusCurrentMode === 'timer') {
          // Stopwatch mode: 0 -> infinity
          const hours = Math.floor(focusModalSeconds / 3600);
          const mins = Math.floor((focusModalSeconds % 3600) / 60);
          const secs = focusModalSeconds % 60;
          if (hours > 0) {
            timeStr = `${hours}:${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
          } else {
            timeStr = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
          }
          if (ring) {
            // Smooth revolution per 60 mins
            const frac = ((focusModalSeconds % 3600) / 3600);
            ring.style.strokeDashoffset = perimeter * (1 - frac);
          }
        } else {
          // Pomodoro mode: countdown to 0
          const mins = Math.floor(focusModalSeconds / 60);
          const secs = focusModalSeconds % 60;
          timeStr = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
          if (ring) {
            const frac = focusModalTotalSeconds > 0 ? (focusModalSeconds / focusModalTotalSeconds) : 0;
            ring.style.strokeDashoffset = perimeter * (1 - frac);
          }
        }

        const modalTime = document.getElementById('focus-modal-time');
        if (modalTime) modalTime.textContent = timeStr;
        const preview = document.getElementById('tools-focus-time-preview');
        if (preview) preview.textContent = timeStr;
        const homeTimer = document.getElementById('home-timer-display');
        if (homeTimer) homeTimer.textContent = timeStr;
      } catch (e) {
        console.warn('renderFocusModal notice:', e);
      }
    }

    function toggleFocusTimer() {
      const toggleBtn = document.getElementById('focus-modal-toggle-btn');
      const icon = document.getElementById('focus-modal-play-icon');
      const text = document.getElementById('focus-modal-play-text');
      const phase = document.getElementById('focus-modal-phase');

      if (focusModalRunning) {
        clearInterval(focusModalInterval);
        focusModalRunning = false;
        if (icon) icon.textContent = 'play_arrow';
        if (text) text.textContent = 'Resume focus';
        if (phase) phase.innerHTML = '<span class="material-symbols-outlined text-[14px]">pause</span><span>Paused</span>';
        apiV3('/api/v3/sessions/pause', { method: 'POST' }).catch(() => {});
      } else {
        focusModalRunning = true;
        if (icon) icon.textContent = 'pause';
        if (text) text.textContent = 'Pause focus';
        if (phase) {
          phase.innerHTML = focusCurrentMode === 'timer'
            ? '<span class="material-symbols-outlined text-[14px]">all_inclusive</span><span>Open Flow Active</span>'
            : '<span class="material-symbols-outlined text-[14px]">lock_clock</span><span>Focus Active</span>';
        }

        apiV3('/api/v3/sessions/start', {
          method: 'POST',
          headers: {'Content-Type': 'application/json'},
          body: JSON.stringify({
            topic: 'Deep Presence',
            category: 'focus',
            mode: focusCurrentMode === 'timer' ? 'STOPWATCH' : 'COUNTDOWN',
            duration_minutes: focusCurrentMode === 'timer' ? 0 : Math.ceil(focusModalSeconds / 60)
          })
        }).catch(() => {});

        focusModalInterval = setInterval(() => {
          if (focusCurrentMode === 'timer') {
            // Stopwatch counts UP from 0 towards infinity
            focusModalSeconds++;
            renderFocusModal();
          } else {
            // Pomodoro countdown to 0
            if (focusModalSeconds > 0) {
              focusModalSeconds--;
              renderFocusModal();
            } else {
              clearInterval(focusModalInterval);
              focusModalRunning = false;
              if (icon) icon.textContent = 'check_circle';
              if (text) text.textContent = 'Complete';
              if (phase) phase.innerHTML = '<span class="material-symbols-outlined text-[14px]">check</span><span>Interval Complete</span>';
              apiV3('/api/v3/sessions/finish', { method: 'POST' }).catch(() => {});
            }
          }
        }, 1000);
      }
    }

    function resetFocusTimer() {
      clearInterval(focusModalInterval);
      focusModalRunning = false;
      const playIcon = document.getElementById('focus-modal-play-icon');
      const playText = document.getElementById('focus-modal-play-text');
      const phase = document.getElementById('focus-modal-phase');

      if (focusCurrentMode === 'timer') {
        focusModalSeconds = 0;
        if (phase) phase.innerHTML = '<span class="material-symbols-outlined text-[14px]">all_inclusive</span><span>Stopwatch Ready</span>';
      } else {
        focusModalSeconds = focusModalTotalSeconds;
        if (phase) phase.innerHTML = '<span class="material-symbols-outlined text-[14px]">restart_alt</span><span>Ready</span>';
      }
      renderFocusModal();
      if (playIcon) playIcon.textContent = 'play_arrow';
      if (playText) playText.textContent = 'Start focus';
    }"""

if old_focus_timer_block.replace('\r\n', '\n') in c_norm:
    c_norm = c_norm.replace(old_focus_timer_block.replace('\r\n', '\n'), new_focus_timer_block)
    print("Replaced focus timer logic with 0 -> infinity stopwatch")
else:
    print("old_focus_timer_block not matched")

with open(INDEX_PATH, "w", encoding="utf-8", newline="\n") as f:
    f.write(c_norm)

print("Updated app/src/main/assets/v3/index.html cleanly!")
