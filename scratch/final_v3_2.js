
    // Global Navigation State
    let activeTab = 'home';
    let activeToolView = null;
    let selectedTag = 'all';
    let writerSelectedTags = ['gratitude'];

    // Focus Timer State
    let timerDuration = 25 * 60;
    let timerRemaining = 25 * 60;
    let timerIsRunning = false;
    let timerInterval = null;
    let focusCycle = 1;
    let focusMode = 'pomodoro';

    // Reading State
    let currentBookPage = 142;
    const totalBookPages = 228;

    // Toast Timer
    let toastTimeout = null;

    // --- CUSTOM TACTILE TOAST SYSTEM ---
    function showToast(title, subtitle = '', type = 'success') {
      const container = document.getElementById('v07-toast-container');
      const titleEl = document.getElementById('toast-title');
      const subEl = document.getElementById('toast-subtitle');
      const iconEl = document.getElementById('toast-icon');
      const iconBg = document.getElementById('toast-icon-bg');

      if (!container) return;

      titleEl.textContent = title;
      subEl.textContent = subtitle;

      // Color/icon variant
      if (type === 'success') {
        iconEl.textContent = 'check_circle';
        iconBg.className = 'w-8 h-8 rounded-full bg-primary-fixed text-primary flex items-center justify-center shrink-0';
      } else if (type === 'info') {
        iconEl.textContent = 'spa';
        iconBg.className = 'w-8 h-8 rounded-full bg-surface-container text-primary flex items-center justify-center shrink-0';
      } else if (type === 'warning') {
        iconEl.textContent = 'warning';
        iconBg.className = 'w-8 h-8 rounded-full bg-secondary-fixed text-secondary flex items-center justify-center shrink-0';
      } else if (type === 'heart') {
        iconEl.textContent = 'bookmark_heart';
        iconBg.className = 'w-8 h-8 rounded-full bg-secondary-fixed text-secondary flex items-center justify-center shrink-0';
      }

      container.classList.remove('toast-enter', 'toast-exit');
      container.classList.add('toast-show');

      if (toastTimeout) clearTimeout(toastTimeout);
      toastTimeout = setTimeout(() => {
        dismissToast();
      }, 2600);
    }

    function dismissToast() {
      const container = document.getElementById('v07-toast-container');
      if (container) {
        container.classList.remove('toast-show');
        container.classList.add('toast-exit');
      }
    }
    window.showToast = showToast;

    // --- TAB SWITCHING ---
    function switchTab(tab) {
      activeTab = tab;
      document.querySelectorAll('.page-tab').forEach(el => el.classList.remove('active'));
      const activeEl = document.getElementById('tab-' + tab);
      if (activeEl) activeEl.classList.add('active');

      // Update Nav active classes
      document.querySelectorAll('#app-bottom-nav .nav-item').forEach(item => {
        if (item.getAttribute('data-tab') === tab) {
          item.classList.add('nav-active');
          item.classList.remove('text-on-surface-variant');
        } else {
          item.classList.remove('nav-active');
          item.classList.add('text-on-surface-variant');
        }
      });

      // Update Top Header
      const headerTitle = document.getElementById('header-title');
      const headerSub = document.getElementById('header-sub');
      const headerIcon = document.getElementById('header-icon');

      const headers = {
        home: { title: 'Home', sub: 'Calm sanctuary', icon: 'spa' },
        tools: { title: 'Tools', sub: 'Daily companions', icon: 'grid_view' },
        journal: { title: 'Journal', sub: 'Reflections & thoughts', icon: 'edit_note' },
        day: { title: 'Day', sub: 'Timeline & rhythm', icon: 'calendar_today' },
        settings: { title: 'Settings', sub: 'Your space & preferences', icon: 'settings' }
      };

      if (headers[tab]) {
        if (headerTitle) headerTitle.textContent = headers[tab].title;
        if (headerSub) headerSub.textContent = headers[tab].sub;
        if (headerIcon) headerIcon.textContent = headers[tab].icon;
      }

      window.scrollTo(0, 0);
    }

    // --- FULL-PAGE TOOL NAVIGATION (ZERO POPUPS) ---
    function navigateTo(toolId) {
      activeToolView = toolId;
      const targetView = document.getElementById('view-' + toolId);
      if (targetView) {
        targetView.classList.remove('hidden');
        document.getElementById('app-bottom-nav').style.display = 'none';
        targetView.scrollTop = 0;
      }
    }

    function navigateBack() {
      if (activeToolView) {
        const view = document.getElementById('view-' + activeToolView);
        if (view) view.classList.add('hidden');
        activeToolView = null;
        document.getElementById('app-bottom-nav').style.display = 'flex';
      }
    }

    // --- HARDWARE BACK PRESS HANDLER ---
    window.handleBackPress = function() {
      if (activeToolView) {
        navigateBack();
        return true;
      }
      if (activeTab !== 'home') {
        switchTab('home');
        return true;
      }
      return false;
    };

    // --- NATIVE BRIDGE INVOCATION ---
    async function nativeInvoke(method, params = {}) {
      try {
        if (window.V07Platform && typeof window.V07Platform.invoke === 'function') {
          const res = await window.V07Platform.invoke(method, params);
          if (res && (res.ok || res.status === 'success')) {
            return res.result !== undefined ? res.result : (res.data !== undefined ? res.data : res);
          }
          return res;
        }
      } catch (err) {
        console.warn('Native RPC call failed:', method, err);
      }
      return null;
    }

    // --- THEME SWITCHER ---
    function setAppTheme(theme) {
      const isDark = theme === 'dark';
      document.documentElement.classList.toggle('dark', isDark);

      // Update check icons in Settings
      const checkLight = document.getElementById('settings-check-light');
      const checkDark = document.getElementById('settings-check-dark');
      const cardLight = document.getElementById('settings-theme-light');
      const cardDark = document.getElementById('settings-theme-dark');

      if (checkLight && checkDark && cardLight && cardDark) {
        if (isDark) {
          checkDark.classList.remove('opacity-0');
          checkLight.classList.add('opacity-0');
          cardDark.classList.add('border-primary');
          cardDark.classList.remove('border-surface-container-high');
          cardLight.classList.remove('border-primary/50');
          cardLight.classList.add('border-surface-container-high');
        } else {
          checkLight.classList.remove('opacity-0');
          checkDark.classList.add('opacity-0');
          cardLight.classList.add('border-primary/50');
          cardLight.classList.remove('border-surface-container-high');
          cardDark.classList.remove('border-primary');
          cardDark.classList.add('border-surface-container-high');
        }
      }

      nativeInvoke('storage:set', { key: 'app_theme', value: theme });
      showToast(isDark ? 'Night Ink theme active' : 'Warm Paper theme active', 'Theme preference stored locally', 'info');
    }

    function toggleSettingSwitch(button, settingKey) {
      const isChecked = button.getAttribute('aria-checked') === 'true';
      const thumb = button.querySelector('div');
      const icon = thumb ? thumb.querySelector('span') : null;

      if (isChecked) {
        button.setAttribute('aria-checked', 'false');
        button.classList.remove('bg-primary');
        button.classList.add('bg-surface-container-high');
        if (thumb) {
          thumb.classList.remove('translate-x-5');
          thumb.classList.add('translate-x-0');
        }
        if (icon) icon.classList.add('opacity-0');
        showToast('Notification disabled', settingKey.replace('_', ' '), 'info');
      } else {
        button.setAttribute('aria-checked', 'true');
        button.classList.remove('bg-surface-container-high');
        button.classList.add('bg-primary');
        if (thumb) {
          thumb.classList.remove('translate-x-0');
          thumb.classList.add('translate-x-5');
        }
        if (icon) icon.classList.remove('opacity-0');
        showToast('Notification enabled', settingKey.replace('_', ' '), 'success');
      }
      nativeInvoke('storage:set', { key: 'setting_' + settingKey, value: !isChecked });
    }

    function copyVaultPath(button) {
      navigator.clipboard?.writeText('/storage/emulated/0/V07Journal');
      showToast('Vault path copied', '/storage/emulated/0/V07Journal', 'success');
    }

    function promptEditProfile() {
      showToast('Profile Edit', 'Encrypted local profile is active', 'info');
    }

    function shareSanctuaryStatus() {
      showToast('Status Shared', 'Mindful streak shared to clipboard', 'info');
    }

    function handleExportData() {
      showToast('Export Complete', 'Saved to /storage/emulated/0/V07Journal/export.json', 'success');
    }

    function handleBackupVault() {
      showToast('Vault Backed Up', 'All SQLite tables exported securely', 'success');
    }

    function requestUsagePermission() {
      nativeInvoke('telemetry:requestPermission');
      showToast('Settings Opened', 'Please enable Usage Access for V07', 'info');
    }

    function inspectDatabase() {
      showToast('Database Status', 'SQLite tables: events, tasks, journal (OK)', 'info');
    }

    function clearCache() {
      showToast('Cache Cleared', '14.8 MB local webview cache purged', 'success');
    }

    function resetAppSetup() {
      showToast('Reset Canceled', 'Hold for 5s to confirm erase', 'warning');
    }

    // --- FOCUS TIMER ENGINE (COUNTDOWN POMODORO & COUNT-UP 0-TO-INFINITE) ---
    let timerElapsed = 0; // For count-up timer mode (0 to infinite)

    function formatTime(sec) {
      const h = Math.floor(sec / 3600);
      const m = Math.floor((sec % 3600) / 60).toString().padStart(2, '0');
      const s = (sec % 60).toString().padStart(2, '0');
      if (h > 0) {
        return `${h}:${m}:${s}`;
      }
      return `${m}:${s}`;
    }

    function updateTimerUI() {
      const isTimerMode = focusMode === 'timer';
      const displaySec = isTimerMode ? timerElapsed : timerRemaining;
      const timeStr = formatTime(displaySec);

      const homeDisp = document.getElementById('home-timer-display');
      const modalDisp = document.getElementById('focus-modal-time');
      const ring = document.getElementById('focus-progress-ring');
      const targetLabel = document.getElementById('focus-target-duration');
      const cycleBadge = document.getElementById('focus-cycle-badge');

      if (homeDisp) homeDisp.textContent = timeStr;
      if (modalDisp) modalDisp.textContent = timeStr;

      if (isTimerMode) {
        if (targetLabel) targetLabel.textContent = '∞ Open Flow';
        if (cycleBadge) cycleBadge.textContent = 'Chronometer • Count-up';
        if (ring) {
          // Continuous gentle breathing rotation
          const circumference = 628.31;
          const cycleSec = 60;
          const progress = (timerElapsed % cycleSec) / cycleSec;
          ring.style.strokeDashoffset = (circumference * (1 - progress)).toString();
        }
      } else {
        if (targetLabel) targetLabel.textContent = Math.floor(timerDuration / 60) + 'm Block';
        if (cycleBadge) cycleBadge.textContent = `Focus • Cycle ${focusCycle}/4`;
        if (ring) {
          const circumference = 628.31;
          const progress = (timerDuration - timerRemaining) / timerDuration;
          ring.style.strokeDashoffset = (circumference * (1 - progress)).toString();
        }
      }
    }

    function toggleFocusTimer() {
      if (timerIsRunning) {
        clearInterval(timerInterval);
        timerIsRunning = false;
        nativeInvoke('timer:pause');
        updatePlayButtons(false);
        showToast(focusMode === 'timer' ? 'Timer Paused' : 'Focus Paused', 'Elapsed: ' + formatTime(focusMode === 'timer' ? timerElapsed : (timerDuration - timerRemaining)), 'info');
      } else {
        timerIsRunning = true;
        nativeInvoke('timer:start', { durationSec: focusMode === 'timer' ? 0 : timerRemaining, mode: focusMode });
        timerInterval = setInterval(() => {
          if (focusMode === 'timer') {
            timerElapsed++;
            updateTimerUI();
          } else {
            if (timerRemaining > 0) {
              timerRemaining--;
              updateTimerUI();
            } else {
              clearInterval(timerInterval);
              timerIsRunning = false;
              updatePlayButtons(false);
              showToast('Focus Interval Complete', 'Well done! Time for a short pause.', 'success');
            }
          }
        }, 1000);
        updatePlayButtons(true);
        showToast(focusMode === 'timer' ? 'Stopwatch Started' : 'Focus Started', focusMode === 'timer' ? 'Counting up from 0 to infinite' : 'Foreground service active in status bar', 'success');
      }
    }

    function updatePlayButtons(running) {
      const homeBtnText = document.getElementById('home-timer-text');
      const homeBtnIcon = document.getElementById('home-timer-icon');
      const modalBtnText = document.getElementById('focus-modal-play-text');
      const modalBtnIcon = document.getElementById('focus-modal-play-icon');

      if (homeBtnText) homeBtnText.textContent = running ? 'Pause focus' : 'Start focus';
      if (homeBtnIcon) homeBtnIcon.textContent = running ? 'pause' : 'play_arrow';
      if (modalBtnText) modalBtnText.textContent = running ? 'Pause' : 'Start focus';
      if (modalBtnIcon) modalBtnIcon.textContent = running ? 'pause' : 'play_arrow';
    }

    function resetFocusTimer() {
      clearInterval(timerInterval);
      timerIsRunning = false;
      if (focusMode === 'timer') {
        timerElapsed = 0;
        showToast('Chronometer Reset', 'Reset back to 00:00', 'info');
      } else {
        timerRemaining = timerDuration;
        showToast('Timer Reset', 'Reset to ' + Math.floor(timerDuration/60) + ' minutes', 'info');
      }
      updateTimerUI();
      updatePlayButtons(false);
      nativeInvoke('timer:cancel');
    }

    function adjustFocusMinutes(deltaMinutes) {
      if (focusMode === 'timer') {
        timerElapsed = Math.max(0, timerElapsed + (deltaMinutes * 60));
        updateTimerUI();
        showToast('Chronometer Adjusted', formatTime(timerElapsed) + ' elapsed', 'info');
      } else {
        timerDuration = Math.max(60, timerDuration + (deltaMinutes * 60));
        timerRemaining = Math.max(0, timerRemaining + (deltaMinutes * 60));
        updateTimerUI();
        showToast('Duration Adjusted', formatTime(timerRemaining) + ' remaining', 'info');
      }
    }

    function setFocusPreset(minutes) {
      if (focusMode === 'timer') {
        setFocusMode('pomodoro');
      }
      clearInterval(timerInterval);
      timerIsRunning = false;
      timerDuration = minutes * 60;
      timerRemaining = timerDuration;
      updateTimerUI();
      updatePlayButtons(false);
      const targetLabel = document.getElementById('focus-target-duration');
      if (targetLabel) targetLabel.textContent = minutes + 'm Block';
      showToast(minutes + 'm Preset Selected', 'Focus target set', 'info');
    }

    function toggleCustomTimePanel() {
      const panel = document.getElementById('custom-time-panel');
      if (panel) panel.classList.toggle('hidden');
    }

    function applyCustomTime() {
      const input = document.getElementById('custom-minutes-input');
      const val = parseInt(input.value) || 25;
      setFocusPreset(val);
      toggleCustomTimePanel();
    }

    function skipFocusCycle() {
      focusCycle = (focusCycle % 4) + 1;
      const badge = document.getElementById('focus-cycle-badge');
      if (badge) badge.textContent = `Focus • Cycle ${focusCycle}/4`;
      resetFocusTimer();
      showToast('Cycle Advanced', `Now at Cycle ${focusCycle}/4`, 'info');
    }

    function setFocusMode(mode) {
      focusMode = mode;
      const pomodoroBtn = document.getElementById('focus-mode-pomodoro');
      const timerBtn = document.getElementById('focus-mode-timer');
      const presetRow = document.querySelector('#view-focus .grid-cols-4');

      clearInterval(timerInterval);
      timerIsRunning = false;
      updatePlayButtons(false);

      if (mode === 'pomodoro') {
        pomodoroBtn.className = 'flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-1.5 bg-surface-container-lowest text-primary shadow-xs cursor-pointer';
        timerBtn.className = 'flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-1.5 text-on-surface-variant hover:text-on-surface cursor-pointer';
        timerDuration = 25 * 60;
        timerRemaining = timerDuration;
        updateTimerUI();
        showToast('Pomodoro Mode', '25m interval countdown', 'info');
      } else {
        // TIMER MODE: STARTS FROM 0 TO INFINITE
        timerBtn.className = 'flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-1.5 bg-surface-container-lowest text-primary shadow-xs cursor-pointer';
        pomodoroBtn.className = 'flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-1.5 text-on-surface-variant hover:text-on-surface cursor-pointer';
        timerElapsed = 0;
        updateTimerUI();
        showToast('Timer Mode Active', 'Counts up from 0 to infinite', 'info');
      }
    }

    function updateFocusDetails() {
      const act = document.getElementById('focus-activity-select').value;
      const tag = document.getElementById('focus-label-select').value;
      const pill = document.getElementById('details-summary-pill');
      const activeTag = document.getElementById('focus-active-tag');
      if (pill) pill.innerHTML = `<span class="w-1.5 h-1.5 rounded-full bg-primary"></span><span>${act} • ${tag}</span>`;
      if (activeTag) activeTag.textContent = act;
      showToast('Session Details', `${act} • ${tag}`, 'info');
    }

    function handleSaveParkingThought(e) {
      e.preventDefault();
      const input = document.getElementById('parking-input');
      const text = input.value.trim();
      if (!text) return;
      input.value = '';

      const shelf = document.getElementById('parking-shelf');
      const card = document.createElement('div');
      card.className = 'flex items-center justify-between p-2.5 bg-surface-container-lowest border border-surface-container-high rounded-xl text-on-surface text-xs';
      card.innerHTML = `
        <div class="flex items-center gap-2 truncate">
          <span class="w-1.5 h-1.5 rounded-full bg-primary shrink-0"></span>
          <span class="truncate">${escapeHtml(text)}</span>
        </div>
        <button onclick="this.parentElement.remove(); showToast('Thought Dismissed', '', 'info')" class="w-6 h-6 rounded flex items-center justify-center text-on-surface-variant hover:text-error cursor-pointer">
          <span class="material-symbols-outlined text-[16px]">check</span>
        </button>
      `;
      shelf.prepend(card);
      nativeInvoke('thoughts:add', { text });
      showToast('Thought Captured', 'Saved to parking lot for later review', 'success');
    }

    // --- READING COMPANION ENGINE ---
    function adjustBookPages(delta) {
      currentBookPage = Math.max(1, Math.min(totalBookPages, currentBookPage + delta));
      const pct = Math.round((currentBookPage / totalBookPages) * 100);
      const pctBadge = document.getElementById('book-pct-badge');
      const pageInfo = document.getElementById('book-page-info');
      const bar = document.getElementById('book-progress-bar');

      if (pctBadge) pctBadge.textContent = `${pct}% complete`;
      if (pageInfo) pageInfo.textContent = `Page ${currentBookPage} of ${totalBookPages} · Last read Today`;
      if (bar) bar.style.width = `${pct}%`;

      nativeInvoke('storage:set', { key: 'book_atomic_habits_page', value: currentBookPage });
      showToast('Reading Progress', `Page ${currentBookPage} of ${totalBookPages} (${pct}%)`, 'info');
    }

    function promptUpdatePage() {
      const page = prompt('Enter current page number:', currentBookPage);
      if (page) {
        const num = parseInt(page);
        if (!isNaN(num)) adjustBookPages(num - currentBookPage);
      }
    }

    function promptAddBook() {
      const title = prompt('Enter book title:');
      if (title) {
        showToast('Book Added', `"${title}" added to your shelf`, 'success');
      }
    }

    function filterShelf(filter, btn) {
      document.querySelectorAll('.shelf-pill').forEach(b => {
        b.className = 'shelf-pill px-2.5 py-1 rounded-md text-on-surface-variant hover:text-on-surface cursor-pointer';
      });
      btn.className = 'shelf-pill px-2.5 py-1 rounded-md bg-surface-container-lowest text-primary font-bold shadow-xs cursor-pointer';
      showToast('Shelf Filter', `Showing ${filter} books`, 'info');
    }

    // --- GENTLE HABITS ENGINE ---
    function toggleHabitRow(row, name) {
      const isDone = row.getAttribute('data-completed') === 'true';
      const checkbox = row.querySelector('.habit-checkbox');
      const title = row.querySelector('.habit-name');

      if (isDone) {
        row.setAttribute('data-completed', 'false');
        checkbox.className = 'habit-checkbox w-6 h-6 rounded-md border-2 border-outline-variant bg-transparent flex items-center justify-center shrink-0 transition-transform active:scale-90';
        checkbox.innerHTML = '<span class="material-symbols-outlined text-[16px] opacity-0 text-on-surface">check</span>';
        title.classList.remove('line-through', 'opacity-70');
        showToast('Habit Unchecked', name, 'info');
      } else {
        row.setAttribute('data-completed', 'true');
        checkbox.className = 'habit-checkbox w-6 h-6 rounded-md bg-primary text-on-primary flex items-center justify-center shrink-0 transition-transform active:scale-90';
        checkbox.innerHTML = '<span class="material-symbols-outlined text-[16px]">check</span>';
        title.classList.add('line-through', 'opacity-70');
        showToast('Habit Completed!', `${name} · 🔥 Streak extended`, 'success');
      }
      nativeInvoke('storage:set', { key: 'habit_' + name, value: !isDone });
    }

    function quickToggleHabit(name, el) {
      showToast('Habit Checked', `${name} logged for today`, 'success');
    }

    function promptAddHabit() {
      const habit = prompt('Enter new ritual habit:');
      if (habit) {
        showToast('Habit Added', `"${habit}" added to daily rhythm`, 'success');
      }
    }

    function renderHeatmapGrid() {
      const grid = document.getElementById('heatmap-grid');
      if (!grid) return;
      grid.innerHTML = '';
      for (let i = 1; i <= 30; i++) {
        const cell = document.createElement('span');
        cell.className = 'w-6 h-6 rounded-md flex items-center justify-center text-[10px] font-semibold transition-transform active:scale-95 cursor-pointer ';
        if (i === 24) {
          cell.className += 'bg-primary text-on-primary ring-2 ring-primary-fixed shadow-xs font-bold';
        } else if (i % 4 === 0) {
          cell.className += 'bg-primary text-on-primary';
        } else if (i % 3 === 0) {
          cell.className += 'bg-primary-fixed text-on-surface';
        } else if (i % 2 === 0) {
          cell.className += 'bg-primary-fixed-dim text-on-surface';
        } else {
          cell.className += 'bg-surface-container-high text-on-surface-variant';
        }
        cell.textContent = i;
        cell.onclick = () => showToast(`Sep ${i} Consistency`, '3 of 4 rituals completed', 'info');
        grid.appendChild(cell);
      }
    }

    // --- DIGITAL STILLNESS ENGINE ---
    function switchRhythmView(mode) {
      const dailyBtn = document.getElementById('btn-st-daily');
      const weeklyBtn = document.getElementById('btn-st-weekly');
      const dailyView = document.getElementById('st-view-daily-bars');
      const weeklyView = document.getElementById('st-view-weekly-bars');
      const title = document.getElementById('st-chart-title');
      const sub = document.getElementById('st-chart-sub');

      if (mode === 'daily') {
        dailyBtn.className = 'py-1 px-4 rounded-full bg-primary text-on-primary font-bold text-xs shadow-xs transition-all flex items-center gap-1.5 cursor-pointer';
        weeklyBtn.className = 'py-1 px-4 rounded-full text-on-surface-variant hover:text-on-surface font-semibold text-xs transition-all flex items-center gap-1.5 cursor-pointer';
        dailyView.classList.remove('hidden');
        weeklyView.classList.add('hidden');
        title.textContent = "Today's Rhythm";
        sub.textContent = "Hourly distribution from 6 AM to 11 PM";
        showToast('Daily Rhythm View', 'Showing hourly distribution', 'info');
      } else {
        weeklyBtn.className = 'py-1 px-4 rounded-full bg-primary text-on-primary font-bold text-xs shadow-xs transition-all flex items-center gap-1.5 cursor-pointer';
        dailyBtn.className = 'py-1 px-4 rounded-full text-on-surface-variant hover:text-on-surface font-semibold text-xs transition-all flex items-center gap-1.5 cursor-pointer';
        dailyView.classList.add('hidden');
        weeklyView.classList.remove('hidden');
        title.textContent = "Weekly Rhythm";
        sub.textContent = "7-day overview & comparison";
        showToast('Weekly Rhythm View', 'Showing 7-day overview', 'info');
      }
    }

    function selectWeeklyDay(day, time, status) {
      const nameEl = document.getElementById('weekly-pill-name');
      const timeEl = document.getElementById('weekly-pill-time');
      if (nameEl) nameEl.textContent = day;
      if (timeEl) timeEl.textContent = `${time} · ${status}`;
      showToast(`${day} Screen Time`, `${time} (${status})`, 'info');
    }

    async function refreshScreenTime() {
      showToast('Refreshing Telemetry', 'Fetching native app stats from Android...', 'info');
      try {
        const data = await nativeInvoke('telemetry:getSummary');
        if (data && data.totalFormatted) {
          const totalDisp = document.getElementById('screentime-modal-total');
          const homeDisp = document.getElementById('stat-screen-time');
          if (totalDisp) totalDisp.textContent = data.totalFormatted;
          if (homeDisp) homeDisp.textContent = data.totalFormatted;

          if (data.topApps && data.topApps.length > 0) {
            renderTopApps(data.topApps);
          }
          showToast('Telemetry Updated', `Total screen time: ${data.totalFormatted}`, 'success');
        }
      } catch (e) {
        showToast('Telemetry Fallback', 'Using cached app stats', 'warning');
      }
    }

    function renderTopApps(apps) {
      const list = document.getElementById('screentime-apps-list');
      if (!list) return;
      list.innerHTML = '';
      const colors = ['bg-[#FF9F43]', 'bg-primary', 'bg-secondary', 'bg-[#38BDF8]', 'bg-[#4DE086]'];

      apps.slice(0, 6).forEach((app, idx) => {
        const row = document.createElement('div');
        row.className = 'p-3 flex items-center justify-between hover:bg-surface-container-low transition-colors';
        const color = colors[idx % colors.length];
        row.innerHTML = `
          <div class="flex items-center gap-3 min-w-0">
            <div class="w-8 h-8 rounded-lg ${color}/15 flex items-center justify-center font-bold text-xs text-on-surface">
              ${escapeHtml(app.appName ? app.appName.charAt(0) : 'A')}
            </div>
            <div class="flex flex-col min-w-0">
              <span class="text-xs font-bold text-on-surface truncate">${escapeHtml(app.appName || 'Application')}</span>
              <span class="text-[10px] text-on-surface-variant font-medium">${escapeHtml(app.category || 'App')}</span>
            </div>
          </div>
          <div class="text-right">
            <span class="text-xs font-bold text-on-surface">${escapeHtml(app.durationFormatted || '0m')}</span>
            <span class="text-[10px] text-primary block">${app.percentOfTotal || 0}%</span>
          </div>
        `;
        list.appendChild(row);
      });
    }

    // --- MOOD & PANIC VAULT ENGINE ---
    function switchMoodSubtab(sub) {
      const checkinView = document.getElementById('mood-view-checkin');
      const panicView = document.getElementById('mood-view-panic');
      const tabMoodBtn = document.getElementById('tab-mood-btn');
      const tabPanicBtn = document.getElementById('tab-panic-btn');

      if (sub === 'checkin') {
        checkinView.classList.remove('hidden');
        panicView.classList.add('hidden');
        tabMoodBtn.className = 'flex-1 py-2 px-3 rounded-xl bg-surface-container-lowest text-primary font-bold text-xs shadow-xs transition-all flex items-center justify-center gap-1.5 cursor-pointer';
        tabPanicBtn.className = 'flex-1 py-2 px-3 rounded-xl text-on-surface-variant font-bold text-xs hover:text-on-surface transition-all flex items-center justify-center gap-1.5 cursor-pointer';
      } else {
        checkinView.classList.add('hidden');
        panicView.classList.remove('hidden');
        tabPanicBtn.className = 'flex-1 py-2 px-3 rounded-xl bg-surface-container-lowest text-secondary font-bold text-xs shadow-xs transition-all flex items-center justify-center gap-1.5 cursor-pointer';
        tabMoodBtn.className = 'flex-1 py-2 px-3 rounded-xl text-on-surface-variant font-bold text-xs hover:text-on-surface transition-all flex items-center justify-center gap-1.5 cursor-pointer';
      }
    }

    function selectMood(btn, mood) {
      document.querySelectorAll('.mood-pill').forEach(b => {
        b.className = 'mood-pill flex flex-col items-center justify-center py-2 px-1 rounded-xl bg-surface-container-low text-on-surface hover:bg-surface-container transition-colors cursor-pointer';
      });
      btn.className = 'mood-pill flex flex-col items-center justify-center py-2 px-1 rounded-xl bg-primary-fixed text-primary font-bold shadow-xs transition-colors cursor-pointer';
      const label = document.getElementById('selected-mood-name');
      if (label) label.textContent = mood;
      showToast('Mood Selected', mood, 'info');
    }

    function updateEnergyDisplay(val) {
      const disp = document.getElementById('energy-display-val');
      if (disp) disp.textContent = `${val} / 10`;
    }

    function handleSaveMoodCheckin() {
      const mood = document.getElementById('selected-mood-name').textContent;
      const energy = document.getElementById('energy-slider').value;
      const note = document.getElementById('checkin-note').value;
      nativeInvoke('storage:set', { key: 'latest_mood', value: { mood, energy, note, date: new Date().toISOString() } });
      showToast('Check-in Saved', `${mood} (Energy ${energy}/10) logged to vault`, 'success');
      document.getElementById('checkin-note').value = '';
    }

    function toggleSensationTag(btn) {
      btn.classList.toggle('bg-secondary-fixed');
      btn.classList.toggle('text-on-secondary-fixed');
      btn.classList.toggle('bg-surface-container-high');
      btn.classList.toggle('text-on-surface');
    }

    function setDistressLevel(lvl) {
      document.querySelectorAll('.distress-btn').forEach(b => {
        b.className = 'distress-btn flex-1 h-7 rounded bg-secondary-fixed/50 hover:bg-secondary-fixed text-xs font-semibold';
      });
      const btns = document.querySelectorAll('.distress-btn');
      if (btns[lvl - 1]) {
        btns[lvl - 1].className = 'distress-btn flex-1 h-7 rounded bg-secondary text-on-secondary font-bold text-xs';
      }
      const label = document.getElementById('panic-distress-label');
      if (label) label.textContent = `${lvl >= 7 ? 'Severe' : (lvl >= 4 ? 'Moderate' : 'Mild')} (${lvl} / 10)`;
      showToast('Distress Level', `${lvl} of 10`, 'warning');
    }

    function handleSavePanicEntry() {
      const note = document.getElementById('panic-racing-note').value;
      nativeInvoke('storage:set', { key: 'panic_entry_' + Date.now(), value: { note, date: new Date().toISOString() } });
      document.getElementById('panic-racing-note').value = '';
      showToast('Entry Stored Privately', 'Isolated securely in Panic Vault', 'success');
    }

    function unlockPanicVault() {
      document.getElementById('panic-vault-locked-state').classList.add('hidden');
      document.getElementById('panic-vault-decrypted-state').classList.remove('hidden');
      showToast('Vault Decrypted', 'Authenticated with device biometric key', 'success');
    }

    function relockPanicVault() {
      document.getElementById('panic-vault-locked-state').classList.remove('hidden');
      document.getElementById('panic-vault-decrypted-state').classList.add('hidden');
      showToast('Vault Secured', 'Panic entries locked', 'info');
    }

    // --- GITA WISDOM ENGINE ---
    function saveGitaVerseToJournal() {
      const entry = {
        title: "Gita Wisdom 2.47 • Nishkama Karma",
        body: "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन। मा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥\n\nYou have a right to your actions, but never to your action's fruits.",
        tags: ["gita", "wisdom", "calm"],
        date: new Date().toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
      };
      nativeInvoke('thoughts:add', { text: `${entry.title}: ${entry.body}` });
      showToast('Verse Saved to Journal', 'Saved to your Calm Journal under #gita', 'heart');
    }

    function cycleGitaVerse() {
      showToast('Next Verse Loaded', 'Chapter 2 · Verse 48 (Samatvam Yoga Uchyate)', 'info');
    }

    // --- WEEKLY REFLECTION ENGINE ---
    function switchLens(lens, btn) {
      document.querySelectorAll('.lens-tab').forEach(b => {
        b.className = 'lens-tab px-2 py-0.5 rounded-md text-on-surface-variant hover:text-on-surface cursor-pointer';
      });
      btn.className = 'lens-tab px-2 py-0.5 rounded-md bg-surface-container-lowest text-primary font-bold shadow-xs cursor-pointer';

      const textEl = document.getElementById('weekly-reflection-text');
      const texts = {
        summary: "Your focus was strongest earlier in the week, especially on Tuesday morning. Protect the quieter hours of your morning for the work that needs your full attention.",
        deeper: "Midweek energy dipped slightly as meeting volume rose. You naturally regained calm rhythm through evening quiet walks and offline reading.",
        next: "Schedule high-clarity writing blocks before 11:00 AM on Monday and Thursday. Protect your quiet rest boundaries."
      };
      if (textEl && texts[lens]) textEl.textContent = texts[lens];
      showToast('Reflection Lens', lens.charAt(0).toUpperCase() + lens.slice(1), 'info');
    }

    function generateNewReflection() {
      showToast('Synthesizing Reflection', 'Analyzing weekly SQLite habits and focus trends...', 'info');
      setTimeout(() => {
        showToast('Reflection Generated', 'Weekly wisdom ready', 'success');
      }, 800);
    }

    function copyWeeklyReflection() {
      const text = document.getElementById('weekly-reflection-text').textContent;
      navigator.clipboard?.writeText(text);
      showToast('Copied to Clipboard', 'Reflection copied', 'success');
    }

    // --- JOURNAL ENGINE ---
    function toggleWriterTag(btn, tag) {
      btn.classList.toggle('bg-primary');
      btn.classList.toggle('text-on-primary');
      btn.classList.toggle('bg-surface-container-low');
      btn.classList.toggle('text-on-surface-variant');
    }

    function saveJournalEntry() {
      const title = document.getElementById('writer-title').value.trim() || 'Untitled Reflection';
      const body = document.getElementById('writer-body').value.trim();

      if (!body) {
        showToast('Empty Entry', 'Please write a thought before saving', 'warning');
        return;
      }

      nativeInvoke('thoughts:add', { text: `${title}: ${body}` });
      showToast('Saved to Journal', `"${title}" safely stored in SQLite vault`, 'success');
      document.getElementById('writer-title').value = '';
      document.getElementById('writer-body').value = '';
      navigateBack();
      loadJournalEntries();
    }

    function loadJournalEntries() {
      const stream = document.getElementById('journal-entries-stream');
      if (!stream) return;

      const entries = [
        {
          title: "Walking under the banyan trees",
          date: "Today, 10:45 AM",
          tag: "#calm",
          body: "The morning breeze was extraordinarily cool. Notice how much lighter the thoughts feel when breathing slowly."
        },
        {
          title: "Flow session on V07 architecture",
          date: "Yesterday, 3:30 PM",
          tag: "#focus",
          body: "Deep work block completed. Zero popups, full bleed pages, native SQLite telemetry connected."
        },
        {
          title: "Early morning gratitude",
          date: "Sep 22, 7:15 AM",
          tag: "#gratitude",
          body: "Quiet tea, notebook on wooden table, silence before the world wakes."
        }
      ];

      stream.innerHTML = '';
      entries.forEach(item => {
        const card = document.createElement('article');
        card.className = 'bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 space-y-2';
        card.innerHTML = `
          <div class="flex items-center justify-between">
            <span class="text-xs font-bold text-primary">${escapeHtml(item.tag)}</span>
            <span class="text-[11px] text-on-surface-variant">${escapeHtml(item.date)}</span>
          </div>
          <h4 class="text-sm font-bold text-on-surface">${escapeHtml(item.title)}</h4>
          <p class="text-xs text-on-surface-variant leading-relaxed">${escapeHtml(item.body)}</p>
        `;
        stream.appendChild(card);
      });
    }

    function selectJournalTag(tag, btn) {
      document.querySelectorAll('.journal-filter-chip').forEach(b => {
        b.className = 'journal-filter-chip px-3 py-1 rounded-full text-xs font-semibold bg-surface-container-low text-on-surface-variant hover:text-on-surface shrink-0 transition-colors cursor-pointer';
      });
      btn.className = 'journal-filter-chip px-3 py-1 rounded-full text-xs font-semibold bg-primary text-on-primary shrink-0 transition-colors shadow-sm cursor-pointer';
      showToast('Journal Filter', `Filtered by ${tag}`, 'info');
    }

    function handleJournalSearch(query) {
      // Live search filter
    }

    // --- HOME TASKS ENGINE ---
    function handleAddTask(e) {
      e.preventDefault();
      const input = document.getElementById('new-task-input');
      const title = input.value.trim();
      if (!title) return;
      input.value = '';

      nativeInvoke('tasks:add', { title });
      renderTaskItem({ title, completed: false });
      showToast('Task Added', `"${title}" added to today's flow`, 'success');
    }

    function renderTaskItem(task) {
      const list = document.getElementById('home-tasks-list');
      if (!list) return;
      const row = document.createElement('div');
      row.className = 'flex items-center justify-between p-3 rounded-xl bg-surface-container-low cursor-pointer active:scale-[0.99] transition-transform';
      row.innerHTML = `
        <div class="flex items-center gap-3">
          <div class="w-5 h-5 rounded-md border-2 border-outline-variant bg-transparent flex items-center justify-center shrink-0">
            <span class="material-symbols-outlined text-[15px] opacity-0 text-on-surface">check</span>
          </div>
          <span class="text-xs font-medium text-on-surface">${escapeHtml(task.title)}</span>
        </div>
      `;
      row.onclick = () => {
        const check = row.querySelector('.material-symbols-outlined');
        const box = row.querySelector('div');
        const text = row.querySelector('span:last-child');
        box.classList.toggle('bg-primary');
        box.classList.toggle('border-primary');
        check.classList.toggle('opacity-0');
        text.classList.toggle('line-through');
        text.classList.toggle('opacity-70');
        showToast('Task Completed', task.title, 'success');
      };
      list.prepend(row);
    }

    function escapeHtml(str) {
      if (!str) return '';
      return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
    }

    
    // ==========================================
    // CUSTOM POP-UP DIALOG SYSTEM (ZERO BROWSER ALERTS)
    // ==========================================
    let modalConfirmCallback = null;

        function showCustomModal({
      title,
      subtitle = '',
      icon = 'spa',
      iconBg = 'bg-primary-fixed text-primary',
      bodyHtml = '',
      confirmText = 'Confirm',
      cancelText = 'Cancel',
      isDestructive = false,
      onConfirm = null
    }) {
      const container = document.getElementById('v07-modal-container');
      const card = document.getElementById('v07-modal-card');
      const titleEl = document.getElementById('modal-title');
      const subEl = document.getElementById('modal-subtitle');
      const iconEl = document.getElementById('modal-icon');
      const iconBgEl = document.getElementById('modal-icon-bg');
      const bodyEl = document.getElementById('modal-body');
      const confirmBtn = document.getElementById('modal-confirm-btn');
      const cancelBtn = document.getElementById('modal-cancel-btn');

      if (!container || !card) return;

      if (titleEl) titleEl.textContent = title;
      if (subEl) subEl.textContent = subtitle;
      if (iconEl) iconEl.textContent = icon;
      if (iconBgEl) iconBgEl.className = `w-10 h-10 rounded-2xl ${iconBg} flex items-center justify-center shrink-0 shadow-xs`;
      if (bodyEl) bodyEl.innerHTML = bodyHtml;

      if (confirmBtn) {
        confirmBtn.textContent = confirmText;
        confirmBtn.className = isDestructive
          ? 'px-5 py-2 rounded-xl text-xs font-bold bg-error text-on-error hover:opacity-95 active:scale-95 transition-all shadow-xs cursor-pointer'
          : 'px-5 py-2 rounded-xl text-xs font-bold bg-primary text-on-primary hover:opacity-95 active:scale-95 transition-all shadow-xs cursor-pointer';

        confirmBtn.onclick = () => {
          if (onConfirm) onConfirm();
          closeCustomModal();
        };
      }

      if (cancelBtn) {
        cancelBtn.textContent = cancelText;
      }

      // Show container and animate in
      container.classList.remove('hidden');
      requestAnimationFrame(() => {
        container.classList.remove('pointer-events-none', 'opacity-0');
        card.classList.remove('scale-95');
        card.classList.add('scale-100');
      });
    }

    function closeCustomModal() {
      const container = document.getElementById('v07-modal-container');
      const card = document.getElementById('v07-modal-card');
      if (card) {
        card.classList.remove('scale-100');
        card.classList.add('scale-95');
      }
      if (container) {
        container.classList.add('opacity-0', 'pointer-events-none');
        setTimeout(() => {
          container.classList.add('hidden');
        }, 220);
      }
      modalConfirmCallback = null;
    }

    // ==========================================
    // CUSTOM ACTIVITIES & LABELS
    // ==========================================
    let currentActivity = 'Deep Work';
    let currentLabel = '#urgent';

    function onActivitySelectChanged(selectEl) {
      if (selectEl.value === '__add_custom__') {
        selectEl.value = currentActivity;
        showCustomPrompt({
          title: 'Add Custom Activity',
          subtitle: 'Focus Sanctuary',
          icon: 'psychology',
          placeholder: 'e.g. Research, Meditation, Design',
          confirmText: 'Add Activity',
          onConfirm: (val) => {
            if (!val) return;
            addCustomActivity(val);
          }
        });
      } else {
        currentActivity = selectEl.value;
        updateFocusDetails();
      }
    }

    function addCustomActivity(name) {
      const select = document.getElementById('focus-activity-select');
      if (!select) return;

      // Add option before __add_custom__
      const opt = document.createElement('option');
      opt.value = name;
      opt.textContent = name;
      const lastOpt = select.querySelector('option[value="__add_custom__"]');
      select.insertBefore(opt, lastOpt);

      select.value = name;
      currentActivity = name;

      // Persist in localStorage
      let customList = [];
      try {
        customList = JSON.parse(localStorage.getItem('v07_custom_activities') || '[]');
      } catch (_) {}
      if (!customList.includes(name)) {
        customList.push(name);
        localStorage.setItem('v07_custom_activities', JSON.stringify(customList));
      }

      updateFocusDetails();
      showToast('Activity Added', `"${name}" is now active`, 'success');
    }

    function onLabelSelectChanged(selectEl) {
      if (selectEl.value === '__add_custom__') {
        selectEl.value = currentLabel;
        showCustomPrompt({
          title: 'Add Custom Label',
          subtitle: 'Session Tag',
          icon: 'label',
          placeholder: 'e.g. #thesis, #mindful, #sprint',
          confirmText: 'Add Label',
          onConfirm: (val) => {
            if (!val) return;
            let tag = val.trim();
            if (!tag.startsWith('#')) tag = '#' + tag;
            addCustomLabel(tag);
          }
        });
      } else {
        currentLabel = selectEl.value;
        updateFocusDetails();
      }
    }

    function addCustomLabel(tag) {
      const select = document.getElementById('focus-label-select');
      if (!select) return;

      const opt = document.createElement('option');
      opt.value = tag;
      opt.textContent = tag;
      const lastOpt = select.querySelector('option[value="__add_custom__"]');
      select.insertBefore(opt, lastOpt);

      select.value = tag;
      currentLabel = tag;

      let customList = [];
      try {
        customList = JSON.parse(localStorage.getItem('v07_custom_labels') || '[]');
      } catch (_) {}
      if (!customList.includes(tag)) {
        customList.push(tag);
        localStorage.setItem('v07_custom_labels', JSON.stringify(customList));
      }

      updateFocusDetails();
      showToast('Label Added', `"${tag}" is now active`, 'success');
    }

    function loadCustomActivitiesAndLabels() {
      try {
        const acts = JSON.parse(localStorage.getItem('v07_custom_activities') || '[]');
        const selectAct = document.getElementById('focus-activity-select');
        if (selectAct) {
          const lastOpt = selectAct.querySelector('option[value="__add_custom__"]');
          acts.forEach(name => {
            const opt = document.createElement('option');
            opt.value = name;
            opt.textContent = name;
            selectAct.insertBefore(opt, lastOpt);
          });
        }

        const labels = JSON.parse(localStorage.getItem('v07_custom_labels') || '[]');
        const selectLbl = document.getElementById('focus-label-select');
        if (selectLbl) {
          const lastOpt = selectLbl.querySelector('option[value="__add_custom__"]');
          labels.forEach(tag => {
            const opt = document.createElement('option');
            opt.value = tag;
            opt.textContent = tag;
            selectLbl.insertBefore(opt, lastOpt);
          });
        }
      } catch (_) {}
    }

    // ==========================================
    // V07 COMPANION SYNC & AUTHENTICATION
    // ==========================================
    let companionUser = { email: 'sarah@sanctuary.internal', loggedIn: true };

    function syncCompanionData() {
      const spinner = document.getElementById('sync-spinner');
      if (spinner) spinner.classList.add('animate-spin');

      // Native RPC or Loopback sync
      nativeInvoke('storage:get', { key: 'events' });

      setTimeout(() => {
        if (spinner) spinner.classList.remove('animate-spin');
        showToast('Companion Synced', '18 local events synchronized with V07 Companion', 'success');
        const badge = document.getElementById('companion-badge');
        if (badge) {
          badge.textContent = 'Synced Live';
          badge.className = 'text-[10px] px-2 py-0.5 rounded-full bg-primary text-on-primary font-bold shadow-xs';
        }
      }, 900);
    }

    function openCompanionLoginModal() {
      const bodyHtml = `
        <div class="flex flex-col space-y-3 pt-1">
          <p class="text-xs text-on-surface-variant">Sign in to sync your reflections, books, and habits with the V07 desktop & web companion.</p>
          <div class="space-y-1">
            <label class="text-[10px] uppercase font-bold text-on-surface-variant tracking-wider">Email Address</label>
            <input type="email" id="companion-login-email" value="${escapeHtml(companionUser.email)}" placeholder="user@sanctuary.internal"
                   class="w-full px-3 py-2 rounded-xl bg-surface-container-low border border-surface-container-high text-xs font-medium text-on-surface focus:outline-none focus:border-primary"/>
          </div>
          <div class="space-y-1">
            <label class="text-[10px] uppercase font-bold text-on-surface-variant tracking-wider">Password / Token</label>
            <input type="password" id="companion-login-pass" value="••••••••••••" placeholder="Enter password"
                   class="w-full px-3 py-2 rounded-xl bg-surface-container-low border border-surface-container-high text-xs font-medium text-on-surface focus:outline-none focus:border-primary"/>
          </div>
          <div class="p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40 flex items-center justify-between">
            <span class="text-[11px] text-on-surface-variant">Sync Target:</span>
            <span class="text-[11px] font-mono font-bold text-primary">v07-dashboard.firebaseapp.com</span>
          </div>
        </div>
      `;

      showCustomModal({
        title: 'V07 Companion Login',
        subtitle: 'Cloud Synchronization',
        icon: 'cloud_sync',
        bodyHtml,
        confirmText: 'Sign In & Sync',
        onConfirm: () => {
          const emailInput = document.getElementById('companion-login-email');
          const email = emailInput ? emailInput.value.trim() : companionUser.email;
          if (email) {
            companionUser.email = email;
            companionUser.loggedIn = true;
            const displayEl = document.getElementById('companion-user-display');
            if (displayEl) displayEl.textContent = email;
            syncCompanionData();
            showToast('Logged In to Companion', `Connected as ${email}`, 'success');
          }
        }
      });
    }

    // ==========================================
    // NOTIFICATION CENTER & CUSTOM ALERTS
    // ==========================================
        function openNotificationDrawer() {
      const drawer = document.getElementById('v07-notifications-drawer');
      if (!drawer) return;
      drawer.classList.remove('hidden');
      requestAnimationFrame(() => {
        drawer.classList.remove('pointer-events-none', 'opacity-0');
        const inner = drawer.querySelector('.relative');
        if (inner) {
          inner.classList.remove('translate-y-8', 'scale-95');
          inner.classList.add('translate-y-0', 'scale-100');
        }
      });
      const dot = document.getElementById('notif-badge-dot');
      if (dot) dot.classList.add('hidden');
    }

    function closeNotificationDrawer() {
      const drawer = document.getElementById('v07-notifications-drawer');
      if (!drawer) return;
      const inner = drawer.querySelector('.relative');
      if (inner) {
        inner.classList.remove('translate-y-0', 'scale-100');
        inner.classList.add('translate-y-8', 'scale-95');
      }
      drawer.classList.add('opacity-0', 'pointer-events-none');
      setTimeout(() => {
        drawer.classList.add('hidden');
      }, 250);
    }

    
    // ==========================================
    // NOTIFICATIONS & DRAWER HELPERS
    // ==========================================
    function openProfileDrawer() {
      const drawer = document.getElementById('v07-profile-drawer');
      if (!drawer) return;
      drawer.classList.remove('hidden');
      requestAnimationFrame(() => {
        drawer.classList.remove('pointer-events-none', 'opacity-0');
        const inner = drawer.querySelector('.relative');
        if (inner) {
          inner.classList.remove('translate-y-8', 'scale-95');
          inner.classList.add('translate-y-0', 'scale-100');
        }
      });
    }

    function clearNotifications() {
      const list = document.getElementById('notifications-list');
      if (list) {
        list.innerHTML = `
          <div class="py-8 text-center text-xs text-on-surface-variant flex flex-col items-center gap-1.5">
            <span class="material-symbols-outlined text-[24px] opacity-40">notifications_off</span>
            <span>All caught up. No unread notifications.</span>
          </div>
        `;
      }
      if (typeof showToast === 'function') {
        showToast('Notifications Cleared', 'All notices marked as read', 'info');
      }
    }

    function triggerTestNotification() {
      const list = document.getElementById('notifications-list');
      if (list) {
        const item = document.createElement('div');
        item.className = 'p-3 bg-surface-container-low rounded-2xl border border-surface-container-high/40 flex items-start gap-3';
        item.innerHTML = `
          <span class="material-symbols-outlined text-primary text-[20px] mt-0.5">notifications_active</span>
          <div class="flex flex-col flex-1 min-w-0">
            <div class="flex items-center justify-between">
              <span class="text-xs font-bold text-on-surface">Sanctuary Reminder</span>
              <span class="text-[10px] text-primary font-semibold">Just now</span>
            </div>
            <p class="text-xs text-on-surface-variant mt-0.5">Stay centered. A mindful breath restores clear presence.</p>
          </div>
        `;
        list.insertBefore(item, list.firstChild);
      }
      if (typeof showToast === 'function') {
        showToast('Notification Dispatched', 'Calm mindfulness prompt delivered', 'success');
      }
    }

    function setCatalogFilter(filter) {
      if (typeof catalogFilter !== 'undefined') {
        catalogFilter = filter;
      }
      document.querySelectorAll('.cat-filter-btn').forEach(btn => {
        if (btn.getAttribute('data-filter') === filter) {
          btn.className = 'cat-filter-btn px-3 py-1.5 rounded-full text-xs font-bold bg-primary text-on-primary transition-all cursor-pointer';
        } else {
          btn.className = 'cat-filter-btn px-3 py-1.5 rounded-full text-xs font-bold bg-surface-container text-on-surface-variant transition-all cursor-pointer';
        }
      });
      if (typeof renderPluginCatalog === 'function') {
        renderPluginCatalog();
      }
    }

    function selectWeek(weekKey, btn) {
      document.querySelectorAll('.week-chip').forEach(chip => {
        chip.className = 'week-chip px-3.5 py-1.5 rounded-full text-xs font-semibold bg-surface-container text-on-surface-variant hover:bg-surface-container-high shrink-0 cursor-pointer';
      });
      if (btn) {
        btn.className = 'week-chip px-3.5 py-1.5 rounded-full text-xs font-bold bg-primary text-on-primary shadow-xs shrink-0 cursor-pointer';
      }
      if (typeof showToast === 'function') {
        showToast('Week Selected', 'Viewing analytics for ' + weekKey, 'info');
      }
    }

    function closeProfileDrawer() {
      const drawer = document.getElementById('v07-profile-drawer');
      if (!drawer) return;
      const inner = drawer.querySelector('.relative');
      if (inner) {
        inner.classList.remove('translate-y-0', 'scale-100');
        inner.classList.add('translate-y-8', 'scale-95');
      }
      drawer.classList.add('opacity-0', 'pointer-events-none');
      setTimeout(() => {
        drawer.classList.add('hidden');
      }, 250);
    }

    // ==========================================
    // PLUGIN SETTINGS ACCORDION & STORAGE
    // ==========================================
    function togglePluginAccordion(id) {
      const panel = document.getElementById(id);
      const arrow = document.getElementById('arrow-' + id);
      if (panel) {
        panel.classList.toggle('hidden');
        if (arrow) {
          arrow.classList.toggle('rotate-180');
        }
      }
    }

    function savePluginSetting(key, val) {
      localStorage.setItem('v07_plugin_' + key, val);
      showToast('Setting Saved', `Plugin option updated`, 'info');
    }


    
    // =========================================================================
    // V3 PLUGIN KIT ENGINE (ADDING, REMOVING, CUSTOMIZING, LIFECYCLE)
    // =========================================================================
    let isManageKitMode = false;
    let catalogFilter = 'all';

    // Master list of Sanctuary plugins (built-in starter pack + extensible catalog)
    let sanctuaryPlugins = [
      {
        id: 'focus',
        name: 'Focus Sanctuary',
        category: 'Presence & Rhythm',
        desc: 'Quiet intervals for deep presence and unhurried work.',
        icon: 'hourglass_top',
        color: 'text-primary bg-primary-fixed',
        enabled: true,
        isBuiltIn: true,
        version: '3.0.0',
        permissions: ['storage:isolated', 'telemetry:read', 'notification'],
        settings: {
          focus_len: '25',
          focus_chime: true,
          focus_autobreak: false,
          focus_openflow: false
        }
      },
      {
        id: 'reading',
        name: 'Reading Companion',
        category: 'Literary Shelf',
        desc: 'Keep track of what you are reading with serene page logs.',
        icon: 'menu_book',
        color: 'text-secondary bg-secondary-fixed',
        enabled: true,
        isBuiltIn: true,
        version: '3.0.0',
        permissions: ['storage:isolated'],
        settings: {
          reading_target: 20,
          reading_haptics: true,
          reading_autoarchive: false
        }
      },
      {
        id: 'habits',
        name: 'Gentle Habits',
        category: 'Daily Rhythms',
        desc: 'Nurture daily rituals without pressure or broken-streak anxiety.',
        icon: 'task_alt',
        color: 'text-tertiary bg-tertiary-fixed',
        enabled: true,
        isBuiltIn: true,
        version: '3.0.0',
        permissions: ['storage:isolated', 'notification'],
        settings: {
          habits_freeze: '1',
          habits_chime: true,
          habits_evening_prompt: true
        }
      },
      {
        id: 'screentime',
        name: 'Digital Stillness',
        category: 'Telemetry & Boundaries',
        desc: 'Continuous awareness of digital attention and usage ceilings.',
        icon: 'phonelink_ring',
        color: 'text-primary bg-primary-fixed',
        enabled: true,
        isBuiltIn: true,
        version: '3.0.0',
        permissions: ['telemetry:read', 'notification'],
        settings: {
          screentime_limit: '3.5',
          screentime_nudge: true,
          screentime_blocker: false
        }
      },
      {
        id: 'mood',
        name: 'Mood & Energy',
        category: 'Sovereign Check-in',
        desc: 'A safe, calm emotional log with biometric vault privacy.',
        icon: 'psychology_alt',
        color: 'text-secondary bg-secondary-fixed',
        enabled: true,
        isBuiltIn: true,
        version: '3.0.0',
        permissions: ['storage:isolated'],
        settings: {
          mood_biometric: true,
          mood_breathing_sound: true,
          mood_morning_prompt: true
        }
      },
      {
        id: 'gita',
        name: 'Gita Wisdom',
        category: 'Wisdom & Philosophy',
        desc: 'Contemplative verses and philosophical clarity for daily life.',
        icon: 'auto_stories',
        color: 'text-tertiary bg-tertiary-fixed',
        enabled: true,
        isBuiltIn: true,
        version: '3.0.0',
        permissions: ['storage:isolated'],
        settings: {
          gita_devanagari: true,
          gita_translit: true,
          gita_morning_verse: true
        }
      },
      {
        id: 'weekly',
        name: 'Weekly Digest',
        category: 'Executive Briefing',
        desc: 'Comprehensive Sunday synthesis of your focus, reading, and habits.',
        icon: 'insights',
        color: 'text-primary bg-primary-fixed',
        enabled: true,
        isBuiltIn: true,
        version: '3.0.0',
        permissions: ['storage:isolated', 'telemetry:read'],
        settings: {
          weekly_depth: 'gentle',
          weekly_dispatch: true,
          weekly_telemetry: true
        }
      },
      // Starter Pack Extensions
      {
        id: 'gamify',
        name: 'Gamify Quests',
        category: 'Sanctuary Progression',
        desc: 'Earn mindful XP for completing rituals, protecting streaks, and leveling up.',
        icon: 'military_tech',
        color: 'text-[#4DE086] bg-[#4DE086]/15',
        enabled: false,
        isBuiltIn: true,
        version: '1.0.0',
        permissions: ['storage:isolated', 'notification'],
        settings: {
          gamify_xp_sound: true,
          gamify_streak_shield: true
        }
      },
      {
        id: 'tasks',
        name: 'Sovereign Tasks',
        category: 'Action Items',
        desc: 'Minimalist offline daily checklist and quiet focus priorities.',
        icon: 'checklist',
        color: 'text-primary bg-primary-fixed',
        enabled: false,
        isBuiltIn: true,
        version: '1.0.0',
        permissions: ['storage:isolated'],
        settings: {
          tasks_auto_clear: false,
          tasks_priority_sort: true
        }
      },
      {
        id: 'vitals',
        name: 'Body Rhythm & Vitals',
        category: 'Physical Presence',
        desc: 'Water intake tracker, posture cues, and resting cadence logs.',
        icon: 'monitor_heart',
        color: 'text-[#E06D53] bg-[#E06D53]/15',
        enabled: false,
        isBuiltIn: true,
        version: '1.0.0',
        permissions: ['storage:isolated'],
        settings: {
          vitals_water_target: '2000',
          vitals_posture_reminder: true
        }
      }
    ];

    function initPluginKit() {
      try {
        // Load custom plugins from localStorage
        const customSaved = localStorage.getItem('v07_custom_plugins');
        if (customSaved) {
          const parsed = JSON.parse(customSaved);
          if (Array.isArray(parsed)) {
            parsed.forEach(cp => {
              if (!sanctuaryPlugins.find(p => p.id === cp.id)) {
                sanctuaryPlugins.push(cp);
              }
            });
          }
        }

        // Load enabled/disabled states
        const savedStates = localStorage.getItem('v07_plugin_states');
        if (savedStates) {
          const statesMap = JSON.parse(savedStates);
          sanctuaryPlugins.forEach(p => {
            if (statesMap[p.id] !== undefined) {
              p.enabled = Boolean(statesMap[p.id]);
            }
          });
        }

        // Query native registry if available
        const nativeList = nativeInvoke('plugin:list');
        if (nativeList && nativeList.ok && nativeList.result && Array.isArray(nativeList.result.plugins)) {
          nativeList.result.plugins.forEach(np => {
            let existing = sanctuaryPlugins.find(p => p.id === np.id || ('com.v07.' + p.id) === np.id);
            if (existing) {
              if (np.state === 'DISABLED') existing.enabled = false;
              else if (np.state === 'ENABLED') existing.enabled = true;
            }
          });
        }
      } catch (e) {
        console.warn('initPluginKit load error:', e);
      }

      renderKitTiles();
    }

    function savePluginKitStates() {
      try {
        const states = {};
        sanctuaryPlugins.forEach(p => { states[p.id] = p.enabled; });
        localStorage.setItem('v07_plugin_states', JSON.stringify(states));
      } catch (e) {
        console.warn('savePluginKitStates error:', e);
      }
    }

    function getPluginStatusPreview(pluginId) {
      switch (pluginId) {
        case 'focus':
          const isTimer = focusMode === 'timer';
          return isTimer ? '∞ Open Flow • Ready' : (Math.floor(timerDuration / 60) + 'm Block • Ready');
        case 'reading':
          return 'Atomic Habits • p. 42 / 320';
        case 'habits':
          return '4 of 5 rituals met today';
        case 'screentime':
          return '1h 52m • Mindful range';
        case 'mood':
          return 'Grounded • Calm energy';
        case 'gita':
          return 'Chapter 2, Verse 47';
        case 'weekly':
          return 'Week 38 Briefing Ready';
        case 'gamify':
          return 'Level 7 • 1,420 XP';
        case 'tasks':
          return '3 pending focus priorities';
        case 'vitals':
          return '72 bpm • 1.8L hydration';
        default:
          return 'Ready in kit';
      }
    }

    function renderKitTiles() {
      const container = document.getElementById('kit-tiles-container');
      const countBadge = document.getElementById('kit-count-badge');
      const inactiveSection = document.getElementById('kit-inactive-section');
      const inactiveTiles = document.getElementById('kit-inactive-tiles');
      const inactiveBadge = document.getElementById('kit-inactive-badge');

      if (!container) return;

      const active = sanctuaryPlugins.filter(p => p.enabled);
      const inactive = sanctuaryPlugins.filter(p => !p.enabled);

      if (countBadge) countBadge.textContent = `${active.length} active`;

      // Render Active Cards
      if (active.length === 0) {
        container.innerHTML = `
          <div class="p-8 text-center bg-surface-container-lowest rounded-3xl border border-surface-container-high/40 flex flex-col items-center gap-3 animate-fade-in shadow-xs">
            <div class="w-14 h-14 rounded-2xl bg-primary-fixed text-primary flex items-center justify-center">
              <span class="material-symbols-outlined text-[30px]">spa</span>
            </div>
            <div class="flex flex-col">
              <h3 class="text-base font-bold text-on-surface">Your Kit is Serene & Empty</h3>
              <p class="text-xs text-on-surface-variant max-w-xs mt-1">No tools are currently active. Tap below to enable companions from the starter pack or import a custom tool.</p>
            </div>
            <button onclick="openPluginCatalogDrawer()" class="mt-2 px-5 py-2.5 bg-primary text-on-primary rounded-xl text-xs font-bold flex items-center gap-1.5 active:scale-95 transition-all shadow-xs cursor-pointer">
              <span class="material-symbols-outlined text-[18px]">add</span>
              <span>Explore Tool Catalog</span>
            </button>
          </div>
        `;
      } else {
        container.innerHTML = active.map(p => `
          <article class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col gap-3 transition-all hover:border-primary/30" style="box-shadow: var(--card-shadow);" id="card-plugin-${p.id}">
            <div class="flex items-start justify-between">
              <div class="flex items-center gap-3">
                <div class="w-11 h-11 rounded-xl ${p.color} flex items-center justify-center shrink-0 shadow-2xs">
                  <span class="material-symbols-outlined text-[22px]">${p.icon}</span>
                </div>
                <div class="flex flex-col">
                  <div class="flex items-center gap-1.5">
                    <h3 class="text-base font-semibold text-on-surface">${escapeHtml(p.name)}</h3>
                    <span class="text-[10px] px-2 py-0.2 rounded-full bg-surface-container text-on-surface-variant font-medium">${escapeHtml(p.category)}</span>
                  </div>
                  <p class="text-xs text-on-surface-variant mt-0.5 truncate-multi">${escapeHtml(p.desc)}</p>
                </div>
              </div>

              <!-- Manage Switch or Customize Gear -->
              <div class="flex items-center gap-1">
                ${isManageKitMode ? `
                  <label class="v07-switch" title="Toggle tool visibility in Kit">
                    <input type="checkbox" checked onchange="togglePluginEnabled('${p.id}', this.checked)">
                    <span class="v07-switch-slider"></span>
                  </label>
                ` : `
                  <button onclick="openPluginSettingsModal('${p.id}')" class="w-8 h-8 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container hover:text-on-surface active:scale-90 transition-all cursor-pointer" title="Customize ${escapeHtml(p.name)}">
                    <span class="material-symbols-outlined text-[19px]">settings</span>
                  </button>
                `}
              </div>
            </div>

            <!-- Metric Preview & Primary Action -->
            <div class="flex items-center justify-between pt-2 border-t border-surface-container-high/30">
              <span class="text-xs font-mono font-medium text-primary flex items-center gap-1">
                <span class="w-1.5 h-1.5 rounded-full bg-primary inline-block"></span>
                <span>${escapeHtml(getPluginStatusPreview(p.id))}</span>
              </span>
              <div class="flex items-center gap-2">
                <button onclick="openPluginSettingsModal('${p.id}')" class="px-3 py-1.5 rounded-xl bg-surface-container hover:bg-surface-container-high text-on-surface text-xs font-semibold flex items-center gap-1 active:scale-95 transition-all cursor-pointer border border-surface-container-high/40">
                  <span class="material-symbols-outlined text-[15px]">tune</span>
                  <span>Customize</span>
                </button>
                <button onclick="openPluginFullPage('${p.id}')" class="px-3.5 py-1.5 bg-primary text-on-primary rounded-xl text-xs font-bold flex items-center gap-1 active:scale-95 transition-all shadow-xs cursor-pointer">
                  <span>Open</span>
                  <span class="material-symbols-outlined text-[15px]">arrow_forward</span>
                </button>
              </div>
            </div>
          </article>
        `).join('');
      }

      // Render Inactive Section
      if (inactive.length > 0) {
        if (inactiveSection) inactiveSection.classList.remove('hidden');
        if (inactiveBadge) inactiveBadge.textContent = inactive.length;
        if (inactiveTiles) {
          inactiveTiles.innerHTML = inactive.map(p => `
            <div class="p-3 bg-surface-container-low/60 rounded-2xl border border-surface-container-high/40 flex items-center justify-between opacity-80 hover:opacity-100 transition-opacity">
              <div class="flex items-center gap-2.5">
                <div class="w-9 h-9 rounded-xl ${p.color} flex items-center justify-center shrink-0 opacity-75">
                  <span class="material-symbols-outlined text-[18px]">${p.icon}</span>
                </div>
                <div class="flex flex-col">
                  <span class="text-xs font-bold text-on-surface">${escapeHtml(p.name)}</span>
                  <span class="text-[10px] text-on-surface-variant">${escapeHtml(p.category)} • Disabled</span>
                </div>
              </div>
              <div class="flex items-center gap-2">
                ${!p.isBuiltIn ? `
                  <button onclick="uninstallCustomPlugin('${p.id}')" class="w-8 h-8 rounded-full flex items-center justify-center text-error hover:bg-error/10 cursor-pointer" title="Uninstall plugin">
                    <span class="material-symbols-outlined text-[18px]">delete</span>
                  </button>
                ` : ''}
                <button onclick="togglePluginEnabled('${p.id}', true)" class="px-3 py-1 bg-surface-container-high hover:bg-primary hover:text-on-primary text-on-surface rounded-xl text-xs font-bold transition-all cursor-pointer">
                  Enable
                </button>
              </div>
            </div>
          `).join('');
        }
      } else {
        if (inactiveSection) inactiveSection.classList.add('hidden');
      }
    }

    function toggleManageKitMode() {
      isManageKitMode = !isManageKitMode;
      const banner = document.getElementById('manage-kit-banner');
      const label = document.getElementById('manage-kit-label');
      const icon = document.getElementById('manage-kit-icon');
      const btn = document.getElementById('manage-kit-btn');

      if (banner) {
        if (isManageKitMode) banner.classList.remove('hidden');
        else banner.classList.add('hidden');
      }

      if (label) label.textContent = isManageKitMode ? 'Done' : 'Manage';
      if (icon) icon.textContent = isManageKitMode ? 'check' : 'tune';

      if (btn) {
        if (isManageKitMode) {
          btn.classList.add('bg-primary', 'text-on-primary');
          btn.classList.remove('bg-surface-container-low', 'text-on-surface');
          if (icon) icon.classList.remove('text-primary');
        } else {
          btn.classList.remove('bg-primary', 'text-on-primary');
          btn.classList.add('bg-surface-container-low', 'text-on-surface');
          if (icon) icon.classList.add('text-primary');
        }
      }

      renderKitTiles();
    }

    function toggleInactiveKitAccordion() {
      const tiles = document.getElementById('kit-inactive-tiles');
      const arrow = document.getElementById('arrow-inactive-kit');
      if (tiles) {
        tiles.classList.toggle('hidden');
        if (arrow) arrow.classList.toggle('rotate-180');
      }
    }

    function openPluginFullPage(pluginId) {
      const knownViews = ['focus', 'reading', 'habits', 'screentime', 'mood', 'gita', 'weekly'];
      if (knownViews.includes(pluginId)) {
        navigateTo(pluginId);
      } else {
        // Starter pack extension or custom plugin page
        showCustomModal({
          title: sanctuaryPlugins.find(p => p.id === pluginId)?.name || 'Plugin Sandbox',
          subtitle: 'Plugin Viewport',
          icon: 'extension',
          bodyHtml: `
            <div class="py-4 text-center flex flex-col items-center gap-2">
              <span class="material-symbols-outlined text-[36px] text-primary">hourglass_empty</span>
              <p class="text-xs text-on-surface-variant">The full sandboxed iframe viewport for <b>${escapeHtml(pluginId)}</b> is ready to run inside the V3 platform host.</p>
            </div>
          `,
          confirmText: 'Back to Kit',
          onConfirm: () => {}
        });
      }
    }

    function togglePluginEnabled(pluginId, enableState) {
      const plugin = sanctuaryPlugins.find(p => p.id === pluginId);
      if (!plugin) return;

      plugin.enabled = (enableState !== undefined) ? enableState : !plugin.enabled;
      savePluginKitStates();

      // RPC sync with native bridge
      if (plugin.enabled) {
        nativeInvoke('plugin:enable', { id: plugin.id });
        showToast('Tool Activated', `${plugin.name} is now active in your Kit`, 'success');
      } else {
        nativeInvoke('plugin:disable', { id: plugin.id });
        showToast('Tool Hidden', `${plugin.name} moved to inactive companions`, 'info');
      }

      renderKitTiles();
      if (typeof renderPluginCatalog === 'function') renderPluginCatalog();
    }

    // =========================================================================
    // DEDICATED CONTEXTUAL PLUGIN SETTINGS MODAL
    // =========================================================================
    function openPluginSettingsModal(pluginId) {
      const p = sanctuaryPlugins.find(item => item.id === pluginId);
      if (!p) return;

      let settingsHtml = '';

      switch (pluginId) {
        case 'focus':
          const currentLen = localStorage.getItem('v07_plugin_focus_len') || '25';
          const currentChime = localStorage.getItem('v07_plugin_focus_chime') !== 'false';
          const currentBreak = localStorage.getItem('v07_plugin_focus_autobreak') === 'true';
          const currentOpenFlow = localStorage.getItem('v07_plugin_focus_openflow') === 'true';

          settingsHtml = `
            <div class="flex flex-col space-y-3 pt-1">
              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Default Duration</span>
                  <span class="text-[10px] text-on-surface-variant">Standard countdown block size</span>
                </div>
                <select id="modal-cfg-focus-len" class="h-8 px-2 bg-surface-container rounded-lg text-xs font-bold border border-surface-container-high text-on-surface">
                  <option value="15" ${currentLen === '15' ? 'selected' : ''}>15 mins</option>
                  <option value="25" ${currentLen === '25' ? 'selected' : ''}>25 mins</option>
                  <option value="45" ${currentLen === '45' ? 'selected' : ''}>45 mins</option>
                  <option value="60" ${currentLen === '60' ? 'selected' : ''}>60 mins</option>
                </select>
              </div>

              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Tibetan Singing Bowl</span>
                  <span class="text-[10px] text-on-surface-variant">Soft resonance on start and completion</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-focus-chime" ${currentChime ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>

              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Auto-start Breaks</span>
                  <span class="text-[10px] text-on-surface-variant">Begin 5m pause immediately</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-focus-break" ${currentBreak ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>

              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Default to Open Flow</span>
                  <span class="text-[10px] text-on-surface-variant">Open timer from 0 to infinite</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-focus-openflow" ${currentOpenFlow ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>
            </div>
          `;
          break;

        case 'reading':
          const readTarget = localStorage.getItem('v07_plugin_reading_target') || '20';
          const readHaptics = localStorage.getItem('v07_plugin_reading_haptics') !== 'false';

          settingsHtml = `
            <div class="flex flex-col space-y-3 pt-1">
              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Daily Reading Goal</span>
                  <span class="text-[10px] text-on-surface-variant">Pages to turn each day</span>
                </div>
                <div class="flex items-center gap-1.5">
                  <input type="number" id="modal-cfg-reading-target" value="${readTarget}" min="5" max="150"
                         class="w-14 h-8 px-1 text-center bg-surface-container rounded-lg text-xs font-bold border border-surface-container-high text-on-surface"/>
                  <span class="text-xs text-on-surface-variant">pg</span>
                </div>
              </div>

              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Haptic Feedback</span>
                  <span class="text-[10px] text-on-surface-variant">Tactile micro-tick on page logs</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-reading-haptics" ${readHaptics ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>
            </div>
          `;
          break;

        case 'habits':
          const habitsFreeze = localStorage.getItem('v07_plugin_habits_freeze') || '1';
          const habitsChime = localStorage.getItem('v07_plugin_habits_chime') !== 'false';

          settingsHtml = `
            <div class="flex flex-col space-y-3 pt-1">
              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Weekly Streak Freeze</span>
                  <span class="text-[10px] text-on-surface-variant">Grace days without breaking streak</span>
                </div>
                <select id="modal-cfg-habits-freeze" class="h-8 px-2 bg-surface-container rounded-lg text-xs font-bold border border-surface-container-high text-on-surface">
                  <option value="1" ${habitsFreeze === '1' ? 'selected' : ''}>1 Day / Wk</option>
                  <option value="2" ${habitsFreeze === '2' ? 'selected' : ''}>2 Days / Wk</option>
                  <option value="0" ${habitsFreeze === '0' ? 'selected' : ''}>Disabled</option>
                </select>
              </div>

              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Celebration Chime</span>
                  <span class="text-[10px] text-on-surface-variant">Play gentle tone upon completing all rituals</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-habits-chime" ${habitsChime ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>
            </div>
          `;
          break;

        case 'screentime':
          const stLimit = localStorage.getItem('v07_plugin_screentime_limit') || '3.5';
          const stNudge = localStorage.getItem('v07_plugin_screentime_nudge') !== 'false';

          settingsHtml = `
            <div class="flex flex-col space-y-3 pt-1">
              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Daily Screen Ceiling</span>
                  <span class="text-[10px] text-on-surface-variant">Mindful daily allotment</span>
                </div>
                <select id="modal-cfg-screentime-limit" class="h-8 px-2 bg-surface-container rounded-lg text-xs font-bold border border-surface-container-high text-on-surface">
                  <option value="2.5" ${stLimit === '2.5' ? 'selected' : ''}>2h 30m</option>
                  <option value="3.0" ${stLimit === '3.0' ? 'selected' : ''}>3h 00m</option>
                  <option value="3.5" ${stLimit === '3.5' ? 'selected' : ''}>3h 30m</option>
                  <option value="4.0" ${stLimit === '4.0' ? 'selected' : ''}>4h 00m</option>
                </select>
              </div>

              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">80% Gentle Nudge</span>
                  <span class="text-[10px] text-on-surface-variant">Send quiet notification before ceiling</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-screentime-nudge" ${stNudge ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>
            </div>
          `;
          break;

        case 'mood':
          const moodBio = localStorage.getItem('v07_plugin_mood_biometric') !== 'false';
          const moodSound = localStorage.getItem('v07_plugin_mood_breathing_sound') !== 'false';

          settingsHtml = `
            <div class="flex flex-col space-y-3 pt-1">
              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Biometric Vault Encryption</span>
                  <span class="text-[10px] text-on-surface-variant">Lock panic & vulnerability entries</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-mood-bio" ${moodBio ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>

              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">4-7-8 Breathing Guide Audio</span>
                  <span class="text-[10px] text-on-surface-variant">Calming ocean breath guidance</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-mood-sound" ${moodSound ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>
            </div>
          `;
          break;

        case 'gita':
          const gitaDev = localStorage.getItem('v07_plugin_gita_devanagari') !== 'false';
          const gitaTrans = localStorage.getItem('v07_plugin_gita_translit') !== 'false';

          settingsHtml = `
            <div class="flex flex-col space-y-3 pt-1">
              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Sanskrit Devanagari</span>
                  <span class="text-[10px] text-on-surface-variant">Display original sacred glyphs</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-gita-dev" ${gitaDev ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>

              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Roman Transliteration</span>
                  <span class="text-[10px] text-on-surface-variant">Phonetic English pronunciation</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-gita-trans" ${gitaTrans ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>
            </div>
          `;
          break;

        case 'weekly':
          const wkDepth = localStorage.getItem('v07_plugin_weekly_depth') || 'gentle';
          const wkDispatch = localStorage.getItem('v07_plugin_weekly_dispatch') !== 'false';

          settingsHtml = `
            <div class="flex flex-col space-y-3 pt-1">
              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Synthesis Depth</span>
                  <span class="text-[10px] text-on-surface-variant">Complexity of Sunday summary</span>
                </div>
                <select id="modal-cfg-weekly-depth" class="h-8 px-2 bg-surface-container rounded-lg text-xs font-bold border border-surface-container-high text-on-surface">
                  <option value="gentle" ${wkDepth === 'gentle' ? 'selected' : ''}>Gentle Overview</option>
                  <option value="deep" ${wkDepth === 'deep' ? 'selected' : ''}>Deep Contemplation</option>
                </select>
              </div>

              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Sunday 8:00 PM Dispatch</span>
                  <span class="text-[10px] text-on-surface-variant">Automated prompt when briefing is ready</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-weekly-dispatch" ${wkDispatch ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>
            </div>
          `;
          break;

        default:
          settingsHtml = `
            <div class="p-3 bg-surface-container-low rounded-xl text-xs text-on-surface-variant text-center">
              Standard local sandbox preferences are active for this plugin.
            </div>
          `;
          break;
      }

      const modalBody = `
        <div class="flex flex-col space-y-3">
          <div class="flex items-center justify-between pb-2 border-b border-surface-container-high/40">
            <span class="text-[10px] text-primary font-bold uppercase tracking-wider">Version ${p.version} • ${p.category}</span>
            <button onclick="togglePluginEnabled('${p.id}', false); closeCustomModal();" class="text-[11px] text-error font-semibold hover:underline cursor-pointer flex items-center gap-1">
              <span class="material-symbols-outlined text-[13px]">visibility_off</span>
              <span>Hide from Kit</span>
            </button>
          </div>

          ${settingsHtml}

          <div class="pt-2 flex items-center justify-between border-t border-surface-container-high/40">
            <button onclick="resetPluginSettings('${p.id}')" class="text-xs font-semibold text-on-surface-variant hover:text-on-surface cursor-pointer flex items-center gap-1">
              <span class="material-symbols-outlined text-[15px]">restart_alt</span>
              <span>Reset Defaults</span>
            </button>
            <span class="text-[10px] text-on-surface-variant font-mono">ID: ${escapeHtml(p.id)}</span>
          </div>
        </div>
      `;

      showCustomModal({
        title: `${p.name} Settings`,
        subtitle: 'Plugin preferences',
        icon: p.icon,
        bodyHtml: modalBody,
        confirmText: 'Save Settings',
        onConfirm: () => {
          savePluginSettingsFromModal(p.id);
        }
      });
    }

    function savePluginSettingsFromModal(pluginId) {
      try {
        switch (pluginId) {
          case 'focus':
            const fLen = document.getElementById('modal-cfg-focus-len')?.value;
            const fChime = document.getElementById('modal-cfg-focus-chime')?.checked;
            const fBreak = document.getElementById('modal-cfg-focus-break')?.checked;
            const fOpen = document.getElementById('modal-cfg-focus-openflow')?.checked;
            if (fLen) localStorage.setItem('v07_plugin_focus_len', fLen);
            if (fChime !== undefined) localStorage.setItem('v07_plugin_focus_chime', fChime);
            if (fBreak !== undefined) localStorage.setItem('v07_plugin_focus_autobreak', fBreak);
            if (fOpen !== undefined) localStorage.setItem('v07_plugin_focus_openflow', fOpen);
            break;

          case 'reading':
            const rTarget = document.getElementById('modal-cfg-reading-target')?.value;
            const rHaptics = document.getElementById('modal-cfg-reading-haptics')?.checked;
            if (rTarget) localStorage.setItem('v07_plugin_reading_target', rTarget);
            if (rHaptics !== undefined) localStorage.setItem('v07_plugin_reading_haptics', rHaptics);
            break;

          case 'habits':
            const hFreeze = document.getElementById('modal-cfg-habits-freeze')?.value;
            const hChime = document.getElementById('modal-cfg-habits-chime')?.checked;
            if (hFreeze) localStorage.setItem('v07_plugin_habits_freeze', hFreeze);
            if (hChime !== undefined) localStorage.setItem('v07_plugin_habits_chime', hChime);
            break;

          case 'screentime':
            const stLimit = document.getElementById('modal-cfg-screentime-limit')?.value;
            const stNudge = document.getElementById('modal-cfg-screentime-nudge')?.checked;
            if (stLimit) localStorage.setItem('v07_plugin_screentime_limit', stLimit);
            if (stNudge !== undefined) localStorage.setItem('v07_plugin_screentime_nudge', stNudge);
            break;

          case 'mood':
            const mBio = document.getElementById('modal-cfg-mood-bio')?.checked;
            const mSound = document.getElementById('modal-cfg-mood-sound')?.checked;
            if (mBio !== undefined) localStorage.setItem('v07_plugin_mood_biometric', mBio);
            if (mSound !== undefined) localStorage.setItem('v07_plugin_mood_breathing_sound', mSound);
            break;

          case 'gita':
            const gDev = document.getElementById('modal-cfg-gita-dev')?.checked;
            const gTrans = document.getElementById('modal-cfg-gita-trans')?.checked;
            if (gDev !== undefined) localStorage.setItem('v07_plugin_gita_devanagari', gDev);
            if (gTrans !== undefined) localStorage.setItem('v07_plugin_gita_translit', gTrans);
            break;

          case 'weekly':
            const wDepth = document.getElementById('modal-cfg-weekly-depth')?.value;
            const wDispatch = document.getElementById('modal-cfg-weekly-dispatch')?.checked;
            if (wDepth) localStorage.setItem('v07_plugin_weekly_depth', wDepth);
            if (wDispatch !== undefined) localStorage.setItem('v07_plugin_weekly_dispatch', wDispatch);
            break;
        }

        // Native isolated storage save
        nativeInvoke('storage:set', { key: `config_${pluginId}`, value: JSON.stringify({ updated: Date.now() }) });
        showToast('Settings Saved', `${pluginId} preferences updated`, 'success');
        renderKitTiles();
      } catch (e) {
        console.warn('savePluginSettingsFromModal error:', e);
      }
    }

    function resetPluginSettings(pluginId) {
      const keys = [
        `v07_plugin_${pluginId}_len`,
        `v07_plugin_${pluginId}_chime`,
        `v07_plugin_${pluginId}_autobreak`,
        `v07_plugin_${pluginId}_target`,
        `v07_plugin_${pluginId}_haptics`,
        `v07_plugin_${pluginId}_freeze`,
        `v07_plugin_${pluginId}_limit`,
        `v07_plugin_${pluginId}_nudge`,
        `v07_plugin_${pluginId}_biometric`,
        `v07_plugin_${pluginId}_breathing_sound`,
        `v07_plugin_${pluginId}_devanagari`,
        `v07_plugin_${pluginId}_translit`,
        `v07_plugin_${pluginId}_depth`,
        `v07_plugin_${pluginId}_dispatch`
      ];
      keys.forEach(k => localStorage.removeItem(k));
      closeCustomModal();
      showToast('Settings Reset', `${pluginId} options restored to factory defaults`, 'info');
      renderKitTiles();
    }

    // =========================================================================
    // PLUGIN CATALOG DRAWER (+ ADD TOOL TO KIT)
    // =========================================================================
        function openPluginCatalogDrawer() {
      const drawer = document.getElementById('v07-plugin-catalog-drawer');
      if (!drawer) return;
      drawer.classList.remove('hidden');
      requestAnimationFrame(() => {
        drawer.classList.remove('pointer-events-none', 'opacity-0');
        const inner = drawer.querySelector('.relative');
        if (inner) {
          inner.classList.remove('translate-y-8', 'scale-95');
          inner.classList.add('translate-y-0', 'scale-100');
        }
      });
      renderPluginCatalog();
    }

    function closePluginCatalogDrawer() {
      const drawer = document.getElementById('v07-plugin-catalog-drawer');
      if (!drawer) return;
      const inner = drawer.querySelector('.relative');
      if (inner) {
        inner.classList.remove('translate-y-0', 'scale-100');
        inner.classList.add('translate-y-8', 'scale-95');
      }
      drawer.classList.add('opacity-0', 'pointer-events-none');
      setTimeout(() => {
        drawer.classList.add('hidden');
      }, 250);
    }

    function renderPluginCatalog() {
      const container = document.getElementById('catalog-plugin-list');
      if (!container) return;

      let list = sanctuaryPlugins;
      if (catalogFilter === 'available') {
        list = sanctuaryPlugins.filter(p => !p.enabled);
      } else if (catalogFilter === 'active') {
        list = sanctuaryPlugins.filter(p => p.enabled);
      }

      if (list.length === 0) {
        container.innerHTML = `
          <div class="py-8 text-center text-xs text-on-surface-variant flex flex-col items-center gap-1.5">
            <span class="material-symbols-outlined text-[26px]">category</span>
            <span>No tools match this filter.</span>
          </div>
        `;
        return;
      }

      container.innerHTML = list.map(p => `
        <div class="p-3.5 bg-surface-container-low rounded-2xl border border-surface-container-high/40 flex flex-col gap-2.5">
          <div class="flex items-start justify-between">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-xl ${p.color} flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[20px]">${p.icon}</span>
              </div>
              <div class="flex flex-col">
                <div class="flex items-center gap-1.5">
                  <h4 class="text-xs font-bold text-on-surface">${escapeHtml(p.name)}</h4>
                  <span class="text-[9px] px-1.5 py-0.2 rounded-full bg-surface-container text-on-surface-variant font-mono">v${p.version}</span>
                </div>
                <span class="text-[10px] text-primary font-medium">${escapeHtml(p.category)}</span>
              </div>
            </div>

            <!-- Add / Active Status Action -->
            <div>
              ${p.enabled ? `
                <span class="px-2.5 py-1 rounded-full bg-primary-fixed text-primary text-[10px] font-bold flex items-center gap-1">
                  <span class="material-symbols-outlined text-[13px]">check</span>
                  <span>In Kit</span>
                </span>
              ` : `
                <button onclick="togglePluginEnabled('${p.id}', true); renderPluginCatalog();" class="px-3.5 py-1.5 bg-primary text-on-primary rounded-xl text-xs font-bold flex items-center gap-1 active:scale-95 transition-all shadow-xs cursor-pointer hover:opacity-95">
                  <span class="material-symbols-outlined text-[14px]">add</span>
                  <span>Add to Kit</span>
                </button>
              `}
            </div>
          </div>

          <p class="text-xs text-on-surface-variant leading-relaxed">${escapeHtml(p.desc)}</p>

          <!-- Permissions pill badges -->
          <div class="flex items-center gap-1.5 flex-wrap pt-1 border-t border-surface-container-high/30">
            <span class="text-[9px] uppercase font-bold text-on-surface-variant tracking-wider">Permissions:</span>
            ${(p.permissions || []).map(perm => `
              <span class="text-[9px] px-1.5 py-0.2 rounded-md bg-surface-container text-on-surface-variant font-mono">${escapeHtml(perm)}</span>
            `).join('')}
          </div>
        </div>
      `).join('');
    }

    // =========================================================================
    // IMPORT CUSTOM PLUGIN MANIFEST FLOW
    // =========================================================================
    function promptImportCustomPlugin() {
      const bodyHtml = `
        <div class="flex flex-col space-y-3 pt-1">
          <p class="text-xs text-on-surface-variant">Sideload an external V3 plugin by entering its manifest specifications or JSON definition.</p>
          
          <div class="space-y-1">
            <label class="text-[10px] uppercase font-bold text-on-surface-variant tracking-wider">Plugin Name</label>
            <input type="text" id="import-plugin-name" placeholder="e.g. Water Sanctuary"
                   class="w-full px-3 py-2 rounded-xl bg-surface-container-low border border-surface-container-high text-xs font-medium text-on-surface focus:outline-none focus:border-primary"/>
          </div>

          <div class="space-y-1">
            <label class="text-[10px] uppercase font-bold text-on-surface-variant tracking-wider">Plugin ID</label>
            <input type="text" id="import-plugin-id" placeholder="e.g. com.sanctuary.water"
                   class="w-full px-3 py-2 rounded-xl bg-surface-container-low border border-surface-container-high text-xs font-mono text-on-surface focus:outline-none focus:border-primary"/>
          </div>

          <div class="grid grid-cols-2 gap-2">
            <div class="space-y-1">
              <label class="text-[10px] uppercase font-bold text-on-surface-variant tracking-wider">Material Icon</label>
              <input type="text" id="import-plugin-icon" placeholder="e.g. water_drop" value="extension"
                     class="w-full px-3 py-2 rounded-xl bg-surface-container-low border border-surface-container-high text-xs font-medium text-on-surface focus:outline-none focus:border-primary"/>
            </div>
            <div class="space-y-1">
              <label class="text-[10px] uppercase font-bold text-on-surface-variant tracking-wider">Category</label>
              <input type="text" id="import-plugin-category" placeholder="e.g. Wellness" value="Custom Companion"
                     class="w-full px-3 py-2 rounded-xl bg-surface-container-low border border-surface-container-high text-xs font-medium text-on-surface focus:outline-none focus:border-primary"/>
            </div>
          </div>

          <div class="space-y-1">
            <label class="text-[10px] uppercase font-bold text-on-surface-variant tracking-wider">Short Description</label>
            <input type="text" id="import-plugin-desc" placeholder="Brief explanation of this tool"
                   class="w-full px-3 py-2 rounded-xl bg-surface-container-low border border-surface-container-high text-xs font-medium text-on-surface focus:outline-none focus:border-primary"/>
          </div>
        </div>
      `;

      showCustomModal({
        title: 'Import Custom Plugin',
        subtitle: 'External Manifest',
        icon: 'file_download',
        bodyHtml,
        confirmText: 'Register Plugin',
        onConfirm: () => {
          const name = document.getElementById('import-plugin-name')?.value.trim();
          let id = document.getElementById('import-plugin-id')?.value.trim();
          const icon = document.getElementById('import-plugin-icon')?.value.trim() || 'extension';
          const category = document.getElementById('import-plugin-category')?.value.trim() || 'Custom Companion';
          const desc = document.getElementById('import-plugin-desc')?.value.trim() || 'Custom imported sanctuary tool.';

          if (!name) {
            showToast('Import Error', 'Plugin name is required', 'error');
            return;
          }
          if (!id) {
            id = 'custom.' + name.toLowerCase().replace(/[^a-z0-9]/g, '');
          }

          const newPlugin = {
            id,
            name,
            category,
            desc,
            icon,
            color: 'text-primary bg-primary-fixed',
            enabled: true,
            isBuiltIn: false,
            version: '1.0.0',
            permissions: ['storage:isolated'],
            settings: {}
          };

          sanctuaryPlugins.push(newPlugin);

          // Save custom plugins to localStorage
          try {
            const customs = sanctuaryPlugins.filter(p => !p.isBuiltIn);
            localStorage.setItem('v07_custom_plugins', JSON.stringify(customs));
          } catch (e) {}

          savePluginKitStates();

          // RPC register with native Kotlin registry
          const manifestJson = JSON.stringify({
            id: newPlugin.id,
            name: newPlugin.name,
            version: newPlugin.version,
            description: newPlugin.desc,
            permissions: newPlugin.permissions,
            navIcon: newPlugin.icon
          });
          nativeInvoke('plugin:register', { manifest: manifestJson });

          showToast('Plugin Registered', `${name} added to your Kit`, 'success');
          renderKitTiles();
          renderPluginCatalog();
        }
      });
    }

    function uninstallCustomPlugin(pluginId) {
      const idx = sanctuaryPlugins.findIndex(p => p.id === pluginId);
      if (idx === -1) return;
      const name = sanctuaryPlugins[idx].name;

      showCustomModal({
        title: 'Uninstall Plugin?',
        subtitle: 'Confirm Removal',
        icon: 'delete',
        bodyHtml: `<p class="text-xs text-on-surface-variant">Are you sure you want to remove <b>${escapeHtml(name)}</b>? This will purge its local state from your sanctuary kit.</p>`,
        confirmText: 'Uninstall',
        onConfirm: () => {
          sanctuaryPlugins.splice(idx, 1);
          try {
            const customs = sanctuaryPlugins.filter(p => !p.isBuiltIn);
            localStorage.setItem('v07_custom_plugins', JSON.stringify(customs));
          } catch (e) {}
          savePluginKitStates();
          nativeInvoke('plugin:unregister', { id: pluginId });
          showToast('Plugin Uninstalled', `${name} removed from your Kit`, 'info');
          renderKitTiles();
          renderPluginCatalog();
        }
      });
    }


    // --- INITIALIZATION ---
    window.addEventListener('DOMContentLoaded', () => {
      initPluginKit();
      loadCustomActivitiesAndLabels();
      renderHeatmapGrid();
      loadJournalEntries();
      refreshScreenTime();
      updateTimerUI();

      // Seed initial tasks
      renderTaskItem({ title: 'Review morning priorities in stillness' });
      renderTaskItem({ title: '25 min focus block without tabs' });
    });
  