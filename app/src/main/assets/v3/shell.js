// V3 Human-Centered Web Shell Controller
// Checklist.design Compliant: Full Empty, Loading, Error, Focus & Active States
// Modern Mobile UX: Zero CLI/Terminal Tropes, Clean Typography, Responsive Micro-Interactions

(function() {
  'use strict';

  // Session Token & Config
  const urlParams = new URLSearchParams(window.location.search);
  const sessionToken = urlParams.get('token') || localStorage.getItem('v07_v3_token') || '';
  if (sessionToken) localStorage.setItem('v07_v3_token', sessionToken);
  const BASE_URL = window.location.origin;

  // State Cache
  let currentTab = 'dashboard';
  let currentTimelineDate = getTodayDateStr();
  let activeTimerState = { status: 'IDLE', startTimestampMs: 0, pausedDurationMs: 0, mode: 'STOPWATCH', targetDurationMs: 0 };
  let selectedTimerCategory = 'Work';
  let selectedTimerMode = 'STOPWATCH';
  let selectedTimerTargetMinutes = 0;
  let selectedTaskPriority = 'MEDIUM';
  let taskFilter = 'pending';
  let allThoughts = [];
  let tickerInterval = null;

  function getTodayDateStr() {
    const d = new Date();
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  // ─── API CLIENT ───────────────────────────────────────────────────────────
  async function api(path, method = 'GET', body = null) {
    const headers = { 'Content-Type': 'application/json' };
    if (sessionToken) headers['X-V07-Token'] = sessionToken;

    const opts = { method, headers };
    if (body) opts.body = JSON.stringify(body);

    try {
      const resp = await fetch(BASE_URL + path, opts);
      const data = await resp.json();
      if (!resp.ok && !data.ok) {
        throw new Error(data.error || 'Request failed with status ' + resp.status);
      }
      return data;
    } catch (err) {
      console.error('API Error (' + method + ' ' + path + '):', err);
      showToast(err.message || 'Network request failed', 'error');
      throw err;
    }
  }

  // ─── TOAST NOTIFICATIONS ──────────────────────────────────────────────────
  function showToast(msg, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;
    const toast = document.createElement('div');
    toast.className = 'toast';
    if (type === 'error') {
      toast.style.borderColor = 'var(--v07-crimson)';
      toast.style.borderLeftColor = 'var(--v07-crimson)';
    } else if (type === 'success') {
      toast.style.borderColor = 'var(--v07-emerald)';
      toast.style.borderLeftColor = 'var(--v07-emerald)';
    } else if (type === 'warn') {
      toast.style.borderColor = 'var(--v07-amber)';
      toast.style.borderLeftColor = 'var(--v07-amber)';
    }
    toast.textContent = msg;
    container.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(-6px)';
      toast.style.transition = 'all 0.25s ease';
      setTimeout(() => toast.remove(), 250);
    }, 2400);
  }

  // ─── CHECKLIST.DESIGN STATE RENDERERS ─────────────────────────────────────

  /**
   * Renders a checklist.design empty state with:
   * 1. Purposeful SVG illustration/icon
   * 2. Clear concise heading
   * 3. Explanatory supporting copy
   * 4. Primary Call to Action button (optional)
   */
  function renderEmptyState(container, opts) {
    if (!container) return;
    const {
      iconSvg,
      heading,
      description,
      actionText,
      actionCallback
    } = opts;

    container.innerHTML = '';
    const emptyBox = document.createElement('div');
    emptyBox.className = 'empty-state';

    let actionBtnHtml = '';
    if (actionText) {
      actionBtnHtml = `<button class="btn btn-primary empty-state-action">${escapeHtml(actionText)}</button>`;
    }

    emptyBox.innerHTML = `
      <div class="empty-state-icon">
        ${iconSvg}
      </div>
      <div class="empty-state-heading">${escapeHtml(heading)}</div>
      <div class="empty-state-description">${escapeHtml(description)}</div>
      ${actionBtnHtml}
    `;

    if (actionText && typeof actionCallback === 'function') {
      const btn = emptyBox.querySelector('.empty-state-action');
      if (btn) btn.onclick = actionCallback;
    }

    container.appendChild(emptyBox);
  }

  /**
   * Renders shimmering loading skeleton cards to eliminate layout shifts
   */
  function renderLoadingSkeleton(container, count = 3, cardHeight = 52) {
    if (!container) return;
    container.innerHTML = '';
    const skelContainer = document.createElement('div');
    skelContainer.className = 'skeleton-container';
    for (let i = 0; i < count; i++) {
      const box = document.createElement('div');
      box.className = 'skeleton-box skeleton-card';
      box.style.height = cardHeight + 'px';
      skelContainer.appendChild(box);
    }
    container.appendChild(skelContainer);
  }

  /**
   * Renders an accessible error state with actionable retry button
   */
  function renderErrorState(container, message, retryCallback) {
    if (!container) return;
    container.innerHTML = '';
    const errorBox = document.createElement('div');
    errorBox.className = 'error-state';
    errorBox.innerHTML = `
      <div class="error-state-heading">Unable to load data</div>
      <div class="error-state-desc">${escapeHtml(message || 'An unexpected error occurred while fetching information.')}</div>
      <button class="btn btn-ghost" style="margin-top:4px;">Retry</button>
    `;
    const btn = errorBox.querySelector('button');
    if (btn && typeof retryCallback === 'function') {
      btn.onclick = retryCallback;
    }
    container.appendChild(errorBox);
  }

  // ─── NAVIGATION TAB SWITCHER ──────────────────────────────────────────────
  window.switchTab = function(tabName) {
    currentTab = tabName;
    document.querySelectorAll('.tab-pane').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.nav-tab').forEach(el => el.classList.remove('active'));

    const pane = document.getElementById('pane-' + tabName);
    if (pane) pane.classList.add('active');

    const tabs = ['dashboard', 'timeline', 'focus', 'tasks', 'thoughts', 'system'];
    const idx = tabs.indexOf(tabName);
    if (idx >= 0) {
      const navTabs = document.querySelectorAll('.nav-tab');
      if (navTabs[idx]) navTabs[idx].classList.add('active');
    }

    // Refresh tab-specific data on navigation
    if (tabName === 'dashboard') { loadDayGlance(); loadDayTracker(); loadPlugins(); }
    if (tabName === 'timeline') loadTimeline(currentTimelineDate);
    if (tabName === 'focus') loadSessionsHistory();
    if (tabName === 'tasks') loadTasks(taskFilter);
    if (tabName === 'thoughts') loadThoughts();
    if (tabName === 'system') { loadScreenTime(); loadDayTracker(); loadRecycleBin(); loadDatabaseStats(); loadPlugins(); }
  };

  // ─── CORE MODULE 1: FOCUS SESSION TIMER ───────────────────────────────────
  function formatMs(ms) {
    const totalSec = Math.max(0, Math.floor(ms / 1000));
    const h = Math.floor(totalSec / 3600);
    const m = Math.floor((totalSec % 3600) / 60);
    const s = totalSec % 60;
    if (h > 0) {
      return String(h).padStart(2, '0') + ':' + String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0');
    }
    return String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0');
  }

  function updateTimerDisplay() {
    const status = activeTimerState.status || 'IDLE';
    const now = Date.now();
    let elapsedMs = 0;

    if (status === 'RUNNING') {
      elapsedMs = Math.max(0, now - activeTimerState.startTimestampMs - (activeTimerState.pausedDurationMs || 0));
    } else if (status === 'PAUSED') {
      elapsedMs = Math.max(0, (activeTimerState.pauseTimestampMs || now) - activeTimerState.startTimestampMs - (activeTimerState.pausedDurationMs || 0));
    }

    let displayTime = formatMs(elapsedMs);
    if (activeTimerState.mode === 'COUNTDOWN' && activeTimerState.targetDurationMs > 0) {
      const remainingMs = Math.max(0, activeTimerState.targetDurationMs - elapsedMs);
      displayTime = formatMs(remainingMs);
    }

    // Update Focus page digits
    const digitsEl = document.getElementById('timer-display-digits');
    if (digitsEl) digitsEl.textContent = displayTime;

    // Update Top HUD Pill & Beacon
    const hudLabel = document.getElementById('hud-timer-label');
    const hudPill = document.getElementById('hud-timer-pill');
    const beacon = document.getElementById('vector-status-beacon');
    if (hudLabel) {
      if (status === 'RUNNING') {
        hudLabel.textContent = displayTime;
        if (hudPill) hudPill.classList.add('active');
        if (beacon) beacon.className = 'status-beacon active';
      } else if (status === 'PAUSED') {
        hudLabel.textContent = 'Paused ' + displayTime;
        if (hudPill) hudPill.classList.add('active');
        if (beacon) beacon.className = 'status-beacon';
      } else {
        hudLabel.textContent = 'Idle';
        if (hudPill) hudPill.classList.remove('active');
        if (beacon) beacon.className = 'status-beacon';
      }
    }

    // Update Circular SVG Gauge in Dashboard Hero
    const gaugeCircle = document.getElementById('chrono-gauge-circle');
    const perimeter = 440; // 2 * PI * 70
    if (gaugeCircle) {
      if (status === 'IDLE') {
        gaugeCircle.style.strokeDashoffset = '440';
      } else if (activeTimerState.mode === 'COUNTDOWN' && activeTimerState.targetDurationMs > 0) {
        const progress = Math.min(1, elapsedMs / activeTimerState.targetDurationMs);
        gaugeCircle.style.strokeDashoffset = String(perimeter * (1 - progress));
      } else {
        const progress = (elapsedMs % 60000) / 60000;
        gaugeCircle.style.strokeDashoffset = String(perimeter * (1 - progress));
      }
    }

    // Update Dashboard Bento Hero Elements
    const dashDigits = document.getElementById('dash-focus-digits');
    const dashStatus = document.getElementById('dash-timer-status');
    const dashTopic = document.getElementById('dash-focus-topic');
    const dashCat = document.getElementById('dash-focus-category');
    if (dashDigits) dashDigits.textContent = displayTime;
    if (dashStatus) dashStatus.textContent = status === 'IDLE' ? 'Ready' : (status === 'RUNNING' ? 'Active' : 'Paused');
    if (dashTopic) dashTopic.textContent = activeTimerState.topic || 'Ready to Track';
    if (dashCat) dashCat.textContent = status === 'IDLE' ? 'Ready' : (activeTimerState.category + ' • ' + (activeTimerState.mode === 'STOPWATCH' ? 'Stopwatch' : 'Timer'));

    // Update Focus page controls
    const btnStart = document.getElementById('btn-timer-start');
    const btnPause = document.getElementById('btn-timer-pause');
    const btnResume = document.getElementById('btn-timer-resume');
    const btnFinish = document.getElementById('btn-timer-finish');
    const btnDiscard = document.getElementById('btn-timer-discard');
    const setupCard = document.getElementById('timer-setup-card');

    if (btnStart) btnStart.style.display = (status === 'IDLE') ? 'inline-flex' : 'none';
    if (btnPause) btnPause.style.display = (status === 'RUNNING') ? 'inline-flex' : 'none';
    if (btnResume) btnResume.style.display = (status === 'PAUSED') ? 'inline-flex' : 'none';
    if (btnFinish) btnFinish.style.display = (status === 'RUNNING' || status === 'PAUSED') ? 'inline-flex' : 'none';
    if (btnDiscard) btnDiscard.style.display = (status === 'RUNNING' || status === 'PAUSED') ? 'inline-flex' : 'none';
    if (setupCard) setupCard.style.display = (status === 'IDLE') ? 'flex' : 'none';

    // Update Dashboard cockpit controls
    const dStart = document.getElementById('dash-btn-start');
    const dPause = document.getElementById('dash-btn-pause');
    const dResume = document.getElementById('dash-btn-resume');
    const dFinish = document.getElementById('dash-btn-finish');
    const dDiscard = document.getElementById('dash-btn-discard');

    if (dStart) dStart.style.display = (status === 'IDLE') ? 'inline-flex' : 'none';
    if (dPause) dPause.style.display = (status === 'RUNNING') ? 'inline-flex' : 'none';
    if (dResume) dResume.style.display = (status === 'PAUSED') ? 'inline-flex' : 'none';
    if (dFinish) dFinish.style.display = (status === 'RUNNING' || status === 'PAUSED') ? 'inline-flex' : 'none';
    if (dDiscard) dDiscard.style.display = (status === 'RUNNING' || status === 'PAUSED') ? 'inline-flex' : 'none';
  }

  window.selectTimerCategory = function(el, cat) {
    document.querySelectorAll('#timer-category-chips .chip').forEach(c => c.classList.remove('active'));
    el.classList.add('active');
    selectedTimerCategory = cat;
  };

  window.selectTimerMode = function(el, mode, minutes) {
    document.querySelectorAll('#timer-mode-chips .chip').forEach(c => c.classList.remove('active'));
    el.classList.add('active');
    selectedTimerMode = mode;
    selectedTimerTargetMinutes = minutes;
  };

  window.handleTimerStart = async function() {
    const topic = document.getElementById('timer-input-topic').value.trim() || 'Focus Session';
    try {
      await api('/api/v3/sessions', 'POST', {
        topic: topic,
        category: selectedTimerCategory,
        mode: selectedTimerMode,
        targetMinutes: selectedTimerTargetMinutes
      });
      showToast('Started focus session: ' + topic, 'success');
      syncStatus();
    } catch (_) {}
  };

  window.handleTimerPause = async function() {
    await api('/api/v3/sessions', 'PUT', { action: 'pause' });
    syncStatus();
  };

  window.handleTimerResume = async function() {
    await api('/api/v3/sessions', 'PUT', { action: 'resume' });
    syncStatus();
  };

  window.handleTimerFinish = async function() {
    try {
      const res = await api('/api/v3/sessions', 'PUT', { action: 'finish' });
      showToast('Session completed! (' + Math.round((res.duration_sec || 0)/60) + ' min)', 'success');
      syncStatus();
      loadSessionsHistory();
    } catch (_) {}
  };

  window.handleTimerDiscard = async function() {
    if (!confirm('Discard this focus session without saving?')) return;
    await api('/api/v3/sessions', 'PUT', { action: 'discard' });
    showToast('Session discarded', 'info');
    syncStatus();
  };

  async function loadSessionsHistory() {
    const listEl = document.getElementById('recent-sessions-list');
    const countEl = document.getElementById('session-count-badge');
    if (!listEl) return;

    renderLoadingSkeleton(listEl, 2, 48);

    try {
      const res = await api('/api/v3/sessions?limit=25');
      listEl.innerHTML = '';
      const sessions = res.sessions || [];
      if (countEl) countEl.textContent = sessions.length + ' sessions';

      if (sessions.length === 0) {
        renderEmptyState(listEl, {
          iconSvg: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="8"/><line x1="12" y1="2" x2="12" y2="4"/><line x1="12" y1="12" x2="15" y2="9"/></svg>',
          heading: 'No focus sessions recorded',
          description: 'Launch your first session to track deliberate practice, deep work, or study intervals.',
          actionText: 'Start First Session',
          actionCallback: () => {
            const input = document.getElementById('timer-input-topic');
            if (input) input.focus();
            window.scrollTo({ top: 0, behavior: 'smooth' });
          }
        });
        return;
      }

      sessions.forEach(s => {
        const item = document.createElement('div');
        item.className = 'task-item';
        const dateStr = new Date(s.start_time).toLocaleDateString() + ' ' + new Date(s.start_time).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
        const durMin = Math.round((s.duration_sec || 0) / 60);

        item.innerHTML = `
          <div style="flex:1; min-width:0;">
            <div style="font-weight:600; font-size:13px; color:var(--v07-text-lead);" class="truncate-single">${escapeHtml(s.activity)}</div>
            <div style="font-size:11px; color:var(--v07-text-muted); margin-top:2px;">
              ${dateStr} • <span style="color:var(--v07-cyan); font-weight:600;">${durMin} mins</span> ${s.notes ? '• ' + escapeHtml(s.notes) : ''}
            </div>
          </div>
          <button class="btn btn-ghost" style="padding:6px 10px; font-size:12px;" onclick="handleDeleteSession('${s.id}')" title="Delete session">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
          </button>
        `;
        listEl.appendChild(item);
      });
    } catch (e) {
      renderErrorState(listEl, e.message, loadSessionsHistory);
    }
  }

  window.handleDeleteSession = async function(id) {
    if (!confirm('Move session to Recycle Bin?')) return;
    await api('/api/v3/sessions?id=' + encodeURIComponent(id), 'DELETE');
    showToast('Session moved to Recycle Bin', 'info');
    loadSessionsHistory();
  };

  // ─── CORE MODULE 2: TASKS DISCIPLINE ──────────────────────────────────────
  window.selectTaskPriority = function(el, priority) {
    document.querySelectorAll('#task-priority-chips .chip').forEach(c => c.classList.remove('active'));
    el.classList.add('active');
    selectedTaskPriority = priority;
  };

  window.filterTasks = function(status) {
    taskFilter = status;
    const btnPending = document.getElementById('tab-filter-pending');
    const btnCompleted = document.getElementById('tab-filter-completed');
    if (btnPending && btnCompleted) {
      if (status === 'pending') {
        btnPending.style.borderColor = 'var(--v07-cyan)';
        btnPending.style.color = 'var(--v07-cyan)';
        btnCompleted.style.borderColor = 'var(--v07-border)';
        btnCompleted.style.color = 'var(--v07-text-muted)';
      } else {
        btnCompleted.style.borderColor = 'var(--v07-cyan)';
        btnCompleted.style.color = 'var(--v07-cyan)';
        btnPending.style.borderColor = 'var(--v07-border)';
        btnPending.style.color = 'var(--v07-text-muted)';
      }
    }
    loadTasks(status);
  };

  async function loadTasks(status = 'pending') {
    const container = document.getElementById('task-list-container');
    if (!container) return;

    renderLoadingSkeleton(container, 3, 50);

    try {
      const res = await api('/api/v3/tasks?status=' + status);
      container.innerHTML = '';
      const tasks = res.tasks || [];

      if (tasks.length === 0) {
        if (status === 'pending') {
          renderEmptyState(container, {
            iconSvg: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>',
            heading: 'All tasks completed!',
            description: 'You have cleared all pending obligations. Take a rest or add a new task to stay productive.',
            actionText: '+ Add New Task',
            actionCallback: () => {
              const input = document.getElementById('task-input-title');
              if (input) {
                input.focus();
                input.scrollIntoView({ behavior: 'smooth', block: 'center' });
              }
            }
          });
        } else {
          renderEmptyState(container, {
            iconSvg: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="8" r="7"/><polyline points="8.21 13.89 7 23 12 20 17 23 15.79 13.88"/></svg>',
            heading: 'No completed tasks yet',
            description: 'When you check off your pending tasks, they will appear here as finished accomplishments.'
          });
        }
        return;
      }

      tasks.forEach(t => {
        const item = document.createElement('div');
        const isCompleted = (t.status === 'completed');
        item.className = 'task-item' + (isCompleted ? ' completed' : '');
        const pClass = (t.priority || 'MEDIUM').toLowerCase();

        item.innerHTML = `
          <div class="task-checkbox" onclick="handleToggleTask('${t.id}')">
            ${isCompleted ? '<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3.5"><polyline points="20 6 9 17 4 12"/></svg>' : ''}
          </div>
          <div style="flex:1; min-width:0;">
            <div class="task-title truncate-single">${escapeHtml(t.title)}</div>
            <div style="display:flex; align-items:center; gap:8px; margin-top:3px;">
              <span class="task-priority-badge ${pClass}">${escapeHtml(t.priority)}</span>
              ${t.deadline ? `<span style="font-size:11px; color:var(--v07-text-muted);">Due: ${escapeHtml(t.deadline)}</span>` : ''}
            </div>
          </div>
          <button class="btn btn-ghost" style="padding:6px 10px; font-size:12px;" onclick="handleDeleteTask('${t.id}')" title="Move to Recycle Bin">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
          </button>
        `;
        container.appendChild(item);
      });
    } catch (e) {
      renderErrorState(container, e.message, () => loadTasks(status));
    }
  }

  window.handleCreateTask = async function() {
    const input = document.getElementById('task-input-title');
    const title = input.value.trim();
    if (!title) return;

    await api('/api/v3/tasks', 'POST', {
      title: title,
      priority: selectedTaskPriority
    });
    input.value = '';
    showToast('Task added', 'success');
    loadTasks(taskFilter);
    loadDayGlance();
  };

  window.handleToggleTask = async function(id) {
    await api('/api/v3/tasks', 'PUT', { id: id, toggle_status: true });
    loadTasks(taskFilter);
    loadDayGlance();
  };

  window.handleDeleteTask = async function(id) {
    await api('/api/v3/tasks?id=' + encodeURIComponent(id), 'DELETE');
    showToast('Task moved to Recycle Bin', 'info');
    loadTasks(taskFilter);
    loadDayGlance();
  };

  // ─── CORE MODULE 3: THOUGHTS & REFLECTIONS ────────────────────────────────
  async function loadThoughts() {
    const container = document.getElementById('thoughts-list-container');
    if (!container) return;

    renderLoadingSkeleton(container, 3, 72);

    try {
      const res = await api('/api/v3/thoughts?limit=50');
      allThoughts = res.thoughts || [];
      renderThoughts(allThoughts);
    } catch (e) {
      renderErrorState(container, e.message, loadThoughts);
    }
  }

  function renderThoughts(thoughts) {
    const container = document.getElementById('thoughts-list-container');
    if (!container) return;
    container.innerHTML = '';

    const searchInput = document.getElementById('thought-search-input');
    const isSearching = (searchInput && searchInput.value.trim().length > 0);

    if (thoughts.length === 0) {
      if (isSearching) {
        renderEmptyState(container, {
          iconSvg: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>',
          heading: 'No matching notes found',
          description: `No thoughts matched your search "${searchInput.value.trim()}". Try searching with different keywords.`,
          actionText: 'Clear Search',
          actionCallback: () => {
            searchInput.value = '';
            renderThoughts(allThoughts);
          }
        });
      } else {
        renderEmptyState(container, {
          iconSvg: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>',
          heading: 'No thoughts captured yet',
          description: 'Record principles, architecture insights, or notes to build your personal knowledge base.',
          actionText: '+ Write Note',
          actionCallback: () => {
            const el = document.getElementById('thought-input-content');
            if (el) {
              el.focus();
              el.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
          }
        });
      }
      return;
    }

    thoughts.forEach(th => {
      const card = document.createElement('div');
      card.className = 'thought-card' + (th.pinned ? ' pinned' : '');
      const timeStr = new Date(th.timestamp).toLocaleDateString() + ' ' + new Date(th.timestamp).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});

      card.innerHTML = `
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <span style="font-size:11px; font-weight:700; color:var(--v07-cyan); background:var(--v07-surface-raised); padding:2px 8px; border-radius:var(--radius-xs);">${escapeHtml(th.topic || 'General')}</span>
          <div style="display:flex; align-items:center; gap:4px;">
            <button class="btn btn-ghost" style="padding:4px 8px; font-size:12px; ${th.pinned ? 'color:var(--v07-amber);' : 'color:var(--v07-text-muted);'}" onclick="handleToggleThoughtPin('${th.id}')" title="Pin note">★</button>
            <button class="btn btn-ghost" style="padding:4px 8px; font-size:12px;" onclick="handleDeleteThought('${th.id}')" title="Move to Recycle Bin">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
            </button>
          </div>
        </div>
        <div class="thought-content">${escapeHtml(th.content)}</div>
        <div class="thought-meta-row">
          <span>${timeStr}</span>
          ${th.pinned ? '<span style="color:var(--v07-amber); font-weight:600;">PINNED</span>' : ''}
        </div>
      `;
      container.appendChild(card);
    });
  }

  window.handleCreateThought = async function() {
    const contentEl = document.getElementById('thought-input-content');
    const topicEl = document.getElementById('thought-input-topic');
    const pinnedEl = document.getElementById('thought-input-pinned');

    const content = contentEl.value.trim();
    if (!content) return;
    const topic = topicEl.value.trim() || 'General';
    const pinned = pinnedEl ? pinnedEl.checked : false;

    await api('/api/v3/thoughts', 'POST', { content, topic, pinned });
    contentEl.value = '';
    showToast('Note saved', 'success');
    loadThoughts();
    loadDayGlance();
  };

  window.handleToggleThoughtPin = async function(id) {
    await api('/api/v3/thoughts', 'PUT', { id: id, toggle_pinned: true });
    loadThoughts();
  };

  window.handleDeleteThought = async function(id) {
    await api('/api/v3/thoughts?id=' + encodeURIComponent(id), 'DELETE');
    showToast('Note moved to Recycle Bin', 'info');
    loadThoughts();
    loadDayGlance();
  };

  window.handleSearchThoughts = function(q) {
    const clean = q.toLowerCase().trim();
    if (!clean) {
      renderThoughts(allThoughts);
      return;
    }
    const filtered = allThoughts.filter(t => t.content.toLowerCase().includes(clean) || (t.topic && t.topic.toLowerCase().includes(clean)));
    renderThoughts(filtered);
  };

  // ─── CORE MODULE 4: SCREEN TIME TELEMETRY ─────────────────────────────────
  async function loadScreenTime() {
    try {
      const res = await api('/api/v3/screentime');
      const totalDurEl = document.getElementById('st-total-duration');
      const appCountEl = document.getElementById('st-app-count');
      const hudStLabel = document.getElementById('hud-screentime-label');
      const listEl = document.getElementById('st-app-list');

      const durFormatted = res.total_duration_formatted || '--:--';
      if (totalDurEl) totalDurEl.textContent = durFormatted;
      if (hudStLabel) hudStLabel.textContent = durFormatted;
      if (appCountEl) appCountEl.textContent = (res.app_count || 0) + ' apps tracked today';

      if (listEl) {
        listEl.innerHTML = '';
        const apps = res.apps || [];
        if (apps.length === 0) {
          renderEmptyState(listEl, {
            iconSvg: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>',
            heading: 'No screen telemetry yet today',
            description: 'Tap Refresh to query today’s Android app usage statistics.',
            actionText: 'Refresh Telemetry',
            actionCallback: handleSyncScreenTime
          });
          return;
        }

        apps.forEach(a => {
          const row = document.createElement('div');
          row.style.cssText = 'display:flex; flex-direction:column; gap:4px; padding:6px 0; border-bottom:1px solid var(--v07-border);';
          const durMin = Math.round(a.duration_sec / 60);
          const durStr = durMin >= 60 ? Math.floor(durMin/60) + 'h ' + (durMin%60) + 'm' : durMin + 'm';

          const iconSrc = a.icon_url || ('/api/v3/apps/icon?package=' + encodeURIComponent(a.package_name || a.app_name));
          row.innerHTML = `
            <div style="display:flex; justify-content:space-between; align-items:center; font-size:12px;">
              <div style="display:flex; align-items:center; gap:8px; overflow:hidden;">
                <img src="${iconSrc}" alt="" style="width:20px; height:20px; border-radius:5px; object-fit:contain; flex-shrink:0; background:var(--v07-surface-raised);" onerror="this.style.display='none'; this.nextElementSibling.style.display='inline-flex';" />
                <span class="material-symbols-outlined" style="display:none; font-size:16px; color:var(--v07-text-muted); flex-shrink:0;">apps</span>
                <span style="font-weight:600; color:var(--v07-text-lead);" class="truncate-single">${escapeHtml(a.app_name || a.package_name)}</span>
              </div>
              <span style="font-family:var(--font-mono); font-weight:600; color:var(--v07-cyan); white-space:nowrap; margin-left:8px;">${durStr} (${Math.round(a.percentage)}%)</span>
            </div>
            <div style="height:4px; width:100%; background:var(--v07-surface-raised); border-radius:var(--radius-full); overflow:hidden; margin-top:2px;">
              <div style="height:100%; width:${Math.min(100, Math.max(2, a.percentage))}%; background:var(--v07-cyan); transition:width 0.3s ease;"></div>
            </div>
          `;
          listEl.appendChild(row);
        });
      }
    } catch (_) {}
  }

  window.handleSyncScreenTime = async function() {
    showToast('Syncing usage telemetry...', 'info');
    await api('/api/v3/screentime', 'POST');
    loadScreenTime();
    showToast('Screen time updated', 'success');
  };

  // ─── CORE UTIL 1: DAY TRACKER & DAILY TIMELINE (V2 EQUIVALENT) ─────────────
  window.navigateTimelineDay = function(delta) {
    const parts = currentTimelineDate.split('-').map(Number);
    const d = new Date(parts[0], parts[1] - 1, parts[2]);
    d.setDate(d.getDate() + delta);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    currentTimelineDate = `${year}-${month}-${day}`;
    loadTimeline(currentTimelineDate);
  };

  window.handleTimelineDateChange = function(newDate) {
    if (!newDate) return;
    currentTimelineDate = newDate;
    loadTimeline(currentTimelineDate);
  };

  window.resetTimelineToToday = function() {
    currentTimelineDate = getTodayDateStr();
    loadTimeline(currentTimelineDate);
  };

  async function loadTimeline(dateStr) {
    const datePicker = document.getElementById('timeline-date-picker');
    if (datePicker) datePicker.value = dateStr;
    const editionStamp = document.getElementById('tl-edition-stamp');
    if (editionStamp) {
      editionStamp.textContent = (dateStr === getTodayDateStr()) ? 'Today' : dateStr;
    }

    const container = document.getElementById('timeline-stream-list');
    if (container) renderLoadingSkeleton(container, 3, 56);

    try {
      const res = await api('/api/v3/timeline?date=' + encodeURIComponent(dateStr));
      const events = res.events || [];

      // Update Ribbon Stats
      const statFocus = document.getElementById('tl-stat-focus');
      const statSessions = document.getElementById('tl-stat-sessions');
      const statScreen = document.getElementById('tl-stat-screentime');
      const statTasks = document.getElementById('tl-stat-tasks');
      const statThoughts = document.getElementById('tl-stat-thoughts');
      const eventCount = document.getElementById('tl-event-count');

      if (statFocus) statFocus.textContent = res.total_focus_formatted || '0m';
      if (statSessions) statSessions.textContent = (res.session_count || 0) + ' sessions';
      if (statScreen) statScreen.textContent = res.total_screentime_formatted || '--';
      if (statTasks) statTasks.textContent = res.tasks_completed_count || 0;
      if (statThoughts) statThoughts.textContent = res.thoughts_count || 0;
      if (eventCount) eventCount.textContent = events.length + ' events';

      // Also update dashboard glance if date is today
      if (dateStr === getTodayDateStr()) {
        const glanceFocus = document.getElementById('dash-glance-focus');
        const glanceScreen = document.getElementById('dash-glance-screen');
        const glanceTasks = document.getElementById('dash-glance-tasks');
        const glanceThoughts = document.getElementById('dash-glance-thoughts');
        if (glanceFocus) glanceFocus.textContent = res.total_focus_formatted || '0m';
        if (glanceScreen) glanceScreen.textContent = res.total_screentime_formatted || '--';
        if (glanceTasks) glanceTasks.textContent = res.tasks_completed_count || 0;
        if (glanceThoughts) glanceThoughts.textContent = res.thoughts_count || 0;
      }

      if (!container) return;
      container.innerHTML = '';

      if (events.length === 0) {
        renderEmptyState(container, {
          iconSvg: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>',
          heading: 'No activity recorded for ' + dateStr,
          description: 'Focus sessions, completed tasks, and reflections logged on this date will appear in this timeline stream.',
          actionText: 'Start Focus Session',
          actionCallback: () => switchTab('focus')
        });
        return;
      }

      events.forEach(e => {
        const item = document.createElement('div');
        item.className = 'timeline-item';
        const catClass = (e.category || 'sessions').toLowerCase();

        let dotColor = 'var(--v07-cyan)';
        if (catClass === 'tasks') dotColor = 'var(--v07-emerald)';
        if (catClass === 'thoughts') dotColor = 'var(--v07-amber)';
        if (catClass === 'daytracker') dotColor = 'var(--v07-crimson)';

        item.style.setProperty('--item-color', dotColor);

        item.innerHTML = `
          <div class="timeline-item-meta">${escapeHtml(e.category)} • ${escapeHtml(e.time_formatted || '')}</div>
          <div class="timeline-item-title">${escapeHtml(e.title)}</div>
          ${e.subtitle ? `<div class="timeline-item-sub">${escapeHtml(e.subtitle)}</div>` : ''}
        `;
        container.appendChild(item);
      });
    } catch (err) {
      if (container) renderErrorState(container, err.message, () => loadTimeline(dateStr));
    }
  }

  window.handleAddTimelineMilestone = async function() {
    const titleEl = document.getElementById('tl-quick-milestone-title');
    const hourEl = document.getElementById('tl-quick-milestone-hour');
    const title = titleEl ? titleEl.value.trim() : '';
    const hour = hourEl ? parseInt(hourEl.value, 10) : 0;

    if (!title) {
      showToast('Please enter a milestone title', 'warn');
      return;
    }

    try {
      await api('/api/v3/daytracker', 'POST', {
        title: title,
        target_date: currentTimelineDate,
        hour: hour,
        pinned: true
      });
      if (titleEl) titleEl.value = '';
      showToast('Milestone added to ' + currentTimelineDate, 'success');
      loadTimeline(currentTimelineDate);
      loadDayTracker();
    } catch (_) {}
  };

  async function loadDayGlance() {
    try {
      const todayStr = getTodayDateStr();
      const res = await api('/api/v3/timeline?date=' + encodeURIComponent(todayStr));
      const glanceFocus = document.getElementById('dash-glance-focus');
      const glanceScreen = document.getElementById('dash-glance-screen');
      const glanceTasks = document.getElementById('dash-glance-tasks');
      const glanceThoughts = document.getElementById('dash-glance-thoughts');
      if (glanceFocus) glanceFocus.textContent = res.total_focus_formatted || '0m';
      if (glanceScreen) glanceScreen.textContent = res.total_screentime_formatted || '--';
      if (glanceTasks) glanceTasks.textContent = res.tasks_completed_count || 0;
      if (glanceThoughts) glanceThoughts.textContent = res.thoughts_count || 0;
    } catch (_) {}
  }

  async function loadDayTracker() {
    try {
      const res = await api('/api/v3/daytracker');
      const items = res.items || [];
      const dashCard = document.getElementById('dash-daytracker-card');
      const dashList = document.getElementById('dash-daytracker-list');
      const platList = document.getElementById('daytracker-items-list');

      // Dashboard pinned items
      const pinned = items.filter(i => i.pinned);
      if (dashCard && dashList) {
        if (pinned.length > 0) {
          dashCard.style.display = 'flex';
          dashList.innerHTML = '';
          pinned.forEach(item => {
            const daysLeft = calcDaysLeft(item.target_date);
            const div = document.createElement('div');
            div.style.cssText = 'display:flex; justify-content:space-between; align-items:center; background:var(--v07-surface-raised); padding:8px 12px; border-radius:var(--radius-sm); font-size:13px;';
            div.innerHTML = `
              <span style="font-weight:600; color:var(--v07-text-lead);">${escapeHtml(item.title)}</span>
              <span style="font-family:var(--font-mono); font-weight:700; color:var(--v07-amber); font-size:11px;">${daysLeft}</span>
            `;
            dashList.appendChild(div);
          });
        } else {
          dashCard.style.display = 'none';
        }
      }

      // System page items
      if (platList) {
        platList.innerHTML = '';
        if (items.length === 0) {
          renderEmptyState(platList, {
            iconSvg: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="2"/></svg>',
            heading: 'No upcoming targets tracked',
            description: 'Set countdowns for project releases, exams, or major deadlines.',
            actionText: '+ Add Target',
            actionCallback: () => {
              const input = document.getElementById('dt-input-title');
              if (input) {
                input.focus();
                input.scrollIntoView({ behavior: 'smooth', block: 'center' });
              }
            }
          });
          return;
        }
        items.forEach(item => {
          const daysLeft = calcDaysLeft(item.target_date);
          const div = document.createElement('div');
          div.className = 'task-item';
          div.innerHTML = `
            <div style="flex:1; min-width:0;">
              <div style="font-weight:600; font-size:13px; color:var(--v07-text-lead);" class="truncate-single">${escapeHtml(item.title)}</div>
              <div style="font-size:11px; color:var(--v07-amber); font-weight:600; margin-top:2px;">${item.target_date} • ${daysLeft}</div>
            </div>
            <button class="btn btn-ghost" style="padding:6px 10px; font-size:12px;" onclick="handleDeleteDayTracker('${item.id}')" title="Delete target">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
            </button>
          `;
          platList.appendChild(div);
        });
      }
    } catch (_) {}
  }

  function calcDaysLeft(targetDateStr) {
    if (!targetDateStr) return '';
    const now = new Date();
    now.setHours(0,0,0,0);
    const target = new Date(targetDateStr);
    target.setHours(0,0,0,0);
    const diff = Math.round((target - now) / (1000 * 60 * 60 * 24));
    if (diff === 0) return 'TODAY';
    if (diff === 1) return 'TOMORROW';
    if (diff > 1) return diff + ' DAYS LEFT';
    return Math.abs(diff) + ' DAYS AGO';
  }

  window.handleCreateDayTracker = async function() {
    const titleEl = document.getElementById('dt-input-title');
    const dateEl = document.getElementById('dt-input-date');
    const title = titleEl ? titleEl.value.trim() : '';
    const date = dateEl ? dateEl.value.trim() : '';
    if (!title || !date) {
      showToast('Please enter both title and target date', 'warn');
      return;
    }
    await api('/api/v3/daytracker', 'POST', { title, target_date: date, pinned: true });
    if (titleEl) titleEl.value = '';
    if (dateEl) dateEl.value = '';
    showToast('Target added', 'success');
    loadDayTracker();
    if (date === currentTimelineDate) loadTimeline(currentTimelineDate);
  };

  window.handleDeleteDayTracker = async function(id) {
    await api('/api/v3/daytracker?id=' + encodeURIComponent(id), 'DELETE');
    showToast('Target removed', 'info');
    loadDayTracker();
    loadTimeline(currentTimelineDate);
  };

  // ─── CORE UTIL 2: RECYCLE BIN ─────────────────────────────────────────────
  async function loadRecycleBin() {
    try {
      const res = await api('/api/v3/recyclebin');
      const listEl = document.getElementById('recycle-bin-list');
      if (!listEl) return;
      listEl.innerHTML = '';

      const items = res.items || [];
      if (items.length === 0) {
        renderEmptyState(listEl, {
          iconSvg: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>',
          heading: 'Recycle Bin is empty',
          description: 'Deleted tasks, sessions, and reflections are safely held here before permanent removal.'
        });
        return;
      }

      items.forEach(r => {
        const item = document.createElement('div');
        item.className = 'task-item';
        const dateStr = new Date(r.deleted_at).toLocaleDateString() + ' ' + new Date(r.deleted_at).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});

        item.innerHTML = `
          <div style="flex:1; min-width:0;">
            <div style="font-size:11px; font-weight:700; color:var(--v07-cyan); text-transform:uppercase;">${escapeHtml(r.category)}</div>
            <div style="font-size:12px; color:var(--v07-text-body); margin:2px 0;" class="truncate-single">${escapeHtml(r.data_json)}</div>
            <div style="font-size:10px; color:var(--v07-text-muted);">${dateStr}</div>
          </div>
          <div style="display:flex; gap:6px;">
            <button class="btn btn-ghost" style="padding:4px 8px; font-size:11px; color:var(--v07-emerald);" onclick="handleRestoreRecycleItem('${r.recycle_id}')">Restore</button>
            <button class="btn btn-ghost" style="padding:4px 8px; font-size:11px; color:var(--v07-crimson);" onclick="handlePurgeRecycleItem('${r.recycle_id}')">Purge</button>
          </div>
        `;
        listEl.appendChild(item);
      });
    } catch (_) {}
  }

  window.handleRestoreRecycleItem = async function(recycleId) {
    await api('/api/v3/recyclebin', 'POST', { recycle_id: recycleId });
    showToast('Item restored from Recycle Bin', 'success');
    loadRecycleBin();
    loadTasks(taskFilter);
    loadThoughts();
    loadSessionsHistory();
  };

  window.handlePurgeRecycleItem = async function(recycleId) {
    await api('/api/v3/recyclebin?recycle_id=' + encodeURIComponent(recycleId), 'DELETE');
    showToast('Item permanently deleted', 'info');
    loadRecycleBin();
  };

  window.handleEmptyRecycleBin = async function() {
    if (!confirm('Permanently delete all items in the Recycle Bin?')) return;
    await api('/api/v3/recyclebin', 'DELETE');
    showToast('Recycle Bin emptied', 'info');
    loadRecycleBin();
  };

  // ─── CORE UTIL 3: DATABASE MANAGER ────────────────────────────────────────
  async function loadDatabaseStats() {
    try {
      const res = await api('/api/v3/database/stats');
      const pathEl = document.getElementById('db-stat-path');
      const sizeEl = document.getElementById('db-stat-size');
      const walEl = document.getElementById('db-stat-wal');

      if (pathEl) pathEl.textContent = res.db_path || '--';
      if (sizeEl) sizeEl.textContent = res.db_size_formatted || '--';
      if (walEl) walEl.textContent = res.is_wal_enabled ? 'WAL Enabled' : 'Standard';
    } catch (_) {}
  }

  window.handleDbVacuum = async function() {
    showToast('Running database optimization...', 'info');
    await api('/api/v3/database/vacuum', 'POST');
    showToast('Optimization complete! Database compacted.', 'success');
    loadDatabaseStats();
  };

  window.handleDbBackup = async function() {
    showToast('Creating database snapshot...', 'info');
    const res = await api('/api/v3/database/backup', 'POST');
    showToast('Snapshot backup created at: ' + (res.backup_path || 'backups/'), 'success');
  };

  window.handleDbImportLegacy = async function() {
    if (!confirm('Import legacy sessions, tasks, and thoughts into V3? Junk files and chess data will be automatically discarded.')) return;
    showToast('Beginning legacy data import...', 'info');
    try {
      const res = await api('/api/v3/database/import-legacy', 'POST');
      showToast(`Imported ${res.sessions_imported} sessions, ${res.tasks_imported} tasks, ${res.thoughts_imported} thoughts`, 'success');
      loadDatabaseStats();
      syncStatus();
    } catch (e) {
      showToast('Legacy import error: ' + e.message, 'error');
    }
  };

  // ─── PLUGIN PLATFORM & STARTER PACK (MODERN CARDS) ────────────────────────
  const PLUGIN_META = {
    'com.v07.reader': {
      title: 'The Library',
      desc: 'EPUB reader, highlighted passages & marginalia',
      color: 'var(--v07-cyan)',
      bg: 'var(--v07-cyan-tint)',
      svg: '<svg viewBox="0 0 24 24" fill="none" stroke="var(--v07-cyan)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>'
    },
    'com.v07.habits': {
      title: 'Habits & Cadence',
      desc: 'Daily streaks, protocols & consistency tracking',
      color: 'var(--v07-emerald)',
      bg: 'var(--v07-emerald-tint)',
      svg: '<svg viewBox="0 0 24 24" fill="none" stroke="var(--v07-emerald)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/><path d="m9 16 2 2 4-4"/></svg>'
    },
    'com.v07.chronosynth': {
      title: 'Weekly Retrospective',
      desc: 'Synthesized focus trajectories & weekly audit',
      color: 'var(--v07-violet)',
      bg: 'var(--v07-violet-tint)',
      svg: '<svg viewBox="0 0 24 24" fill="none" stroke="var(--v07-violet)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12h3l3-7 4 14 4-9 3 4h3"/></svg>'
    },
    'com.v07.gamify': {
      title: 'The Expedition',
      desc: 'RPG character attributes, XP ranks & bounties',
      color: 'var(--v07-amber)',
      bg: 'var(--v07-amber-tint)',
      svg: '<svg viewBox="0 0 24 24" fill="none" stroke="var(--v07-amber)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><polygon points="12 6 15 11 9 11 12 6"/></svg>'
    },
    'com.v07.vitals': {
      title: 'Vital Signs',
      desc: 'Energy tracking, mood logs & wellness telemetry',
      color: 'var(--v07-crimson)',
      bg: 'var(--v07-crimson-tint)',
      svg: '<svg viewBox="0 0 24 24" fill="none" stroke="var(--v07-crimson)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/><path d="M3.22 12H9.5l1.5-3 2 6 1.5-3h6.28"/></svg>'
    }
  };

  // ─── PLUGIN PLATFORM & STARTER PACK (DESIGN.md SECTION 31) ───────────────
  const PLUGIN_META = {
    'com.v07.reader': {
      title: 'The Library',
      desc: 'EPUB reader, highlighted passages & marginalia',
      color: 'var(--v07-cyan)',
      bg: 'var(--v07-cyan-tint)',
      svg: '<svg viewBox="0 0 24 24" fill="none" stroke="var(--v07-cyan)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>'
    },
    'com.v07.habits': {
      title: 'Habits & Cadence',
      desc: 'Daily streaks, protocols & consistency tracking',
      color: 'var(--v07-emerald)',
      bg: 'var(--v07-emerald-tint)',
      svg: '<svg viewBox="0 0 24 24" fill="none" stroke="var(--v07-emerald)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/><path d="m9 16 2 2 4-4"/></svg>'
    },
    'com.v07.chronosynth': {
      title: 'Weekly Retrospective',
      desc: 'Synthesized focus trajectories & weekly audit',
      color: 'var(--v07-violet)',
      bg: 'var(--v07-violet-tint)',
      svg: '<svg viewBox="0 0 24 24" fill="none" stroke="var(--v07-violet)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12h3l3-7 4 14 4-9 3 4h3"/></svg>'
    },
    'com.v07.gamify': {
      title: 'The Expedition',
      desc: 'RPG character attributes, XP ranks & bounties',
      color: 'var(--v07-amber)',
      bg: 'var(--v07-amber-tint)',
      svg: '<svg viewBox="0 0 24 24" fill="none" stroke="var(--v07-amber)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><polygon points="12 6 15 11 9 11 12 6"/></svg>'
    },
    'com.v07.vitals': {
      title: 'Vital Signs',
      desc: 'Energy tracking, mood logs & wellness telemetry',
      color: 'var(--v07-crimson)',
      bg: 'var(--v07-crimson-tint)',
      svg: '<svg viewBox="0 0 24 24" fill="none" stroke="var(--v07-crimson)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/><path d="M3.22 12H9.5l1.5-3 2 6 1.5-3h6.28"/></svg>'
    }
  };

  // Section 31.4 & 31.5: Dashboard Utility Opt-In / Opt-Out Contract
  function isWidgetEnabledOnDashboard(pluginId, widgetKey) {
    const key = `v07_widget_dash_${pluginId}_${widgetKey}`;
    const val = localStorage.getItem(key);
    return val === null ? true : val === 'true';
  }

  function setWidgetEnabledOnDashboard(pluginId, widgetKey, enabled) {
    const key = `v07_widget_dash_${pluginId}_${widgetKey}`;
    localStorage.setItem(key, enabled ? 'true' : 'false');
    showToast(enabled ? 'Utility added to dashboard' : 'Utility removed from dashboard', 'info');
    loadPlugins();
  }

  async function loadPlugins() {
    try {
      const res = await api('/api/v3/plugins');
      const plugins = res.plugins || [];
      const kitGrid = document.getElementById('kit-tiles-grid');
      const managerList = document.getElementById('plugin-manager-list');
      const widgetsContainer = document.getElementById('dashboard-widgets-container');
      const dashUtilsCount = document.getElementById('dash-utilities-count');
      const dashUtilsHeader = document.getElementById('dash-utilities-header');
      const statusBadge = document.getElementById('plugin-status-total-badge');

      if (kitGrid) kitGrid.innerHTML = '';
      if (managerList) managerList.innerHTML = '';
      if (widgetsContainer) widgetsContainer.innerHTML = '';

      let activePluginsCount = 0;
      let activeWidgetsCount = 0;

      plugins.forEach(p => {
        const isEnabled = (p.state === 'ENABLED' || p.state === 'RUNNING');
        if (isEnabled) activePluginsCount++;

        const meta = PLUGIN_META[p.id] || {
          title: p.nav_label || p.name,
          desc: p.description || 'System extension plugin',
          color: 'var(--v07-cyan)',
          bg: 'var(--v07-cyan-tint)',
          svg: '<svg viewBox="0 0 24 24" fill="none" stroke="var(--v07-cyan)" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="12" cy="12" r="3"/></svg>'
        };

        // 1. Modern Plugin Card on Dashboard (Section 31)
        if (kitGrid && isEnabled) {
          const card = document.createElement('div');
          card.className = 'plugin-card';
          card.innerHTML = `
            <div class="plugin-icon-box" style="background:${meta.bg}; border-color:${meta.color};">
              ${meta.svg}
            </div>
            <div class="plugin-content">
              <div class="plugin-meta-row">
                <span class="plugin-name">${escapeHtml(meta.title)}</span>
                <span class="plugin-version">v${escapeHtml(p.version)}</span>
              </div>
              <div class="plugin-desc">${escapeHtml(meta.desc)}</div>
            </div>
            <div class="plugin-arrow">&rarr;</div>
          `;
          card.onclick = () => openPluginPage(p);
          kitGrid.appendChild(card);
        }

        // 2. Embedded Dashboard Utilities (Section 31.3 & 31.4: User Opt-In)
        if (widgetsContainer && isEnabled && p.widgets && p.widgets.length > 0) {
          p.widgets.forEach(w => {
            const widgetKey = w.id || w.entry;
            const userWantsWidget = isWidgetEnabledOnDashboard(p.id, widgetKey);
            if (!userWantsWidget) return;

            activeWidgetsCount++;

            // Sizing mapping per DESIGN.md: compact 72px, small 110px, medium 170px, large 250px
            let widgetHeight = '180px';
            if (w.default_size === 'compact') widgetHeight = '84px';
            else if (w.default_size === 'small') widgetHeight = '120px';
            else if (w.default_size === 'medium') widgetHeight = '180px';
            else if (w.default_size === 'large') widgetHeight = '260px';

            const wCard = document.createElement('div');
            wCard.className = 'card';
            wCard.style.padding = '0';
            wCard.style.overflow = 'hidden';
            wCard.innerHTML = `
              <div class="card-header" style="padding:10px 14px; background:var(--v07-surface-raised);">
                <div style="display:flex; align-items:center; gap:8px;">
                  <span style="display:inline-block; width:6px; height:6px; border-radius:50%; background:${meta.color};"></span>
                  <span class="card-title">${escapeHtml(w.title || meta.title)}</span>
                </div>
                <div style="display:flex; align-items:center; gap:6px;">
                  <span class="chip" style="font-size:10px; padding:2px 6px;">${w.default_size || 'utility'}</span>
                  <button class="btn btn-ghost" style="padding:2px 6px; font-size:11px; line-height:1;" title="Hide from dashboard" onclick="setWidgetEnabledOnDashboard('${p.id}', '${widgetKey}', false)">&times;</button>
                </div>
              </div>
              <div style="position:relative; width:100%; height:${widgetHeight};">
                <iframe style="width:100%; height:100%; border:none; display:block;" src="${BASE_URL}/plugins/${p.id}/${w.entry}?token=${sessionToken}" sandbox="allow-scripts allow-forms allow-same-origin"></iframe>
              </div>
            `;
            widgetsContainer.appendChild(wCard);
          });
        }

        // 3. Dedicated Plugin Management Space in Tab 6 (Section 31.1 & 31.4)
        if (managerList) {
          const row = document.createElement('div');
          row.className = 'card';
          row.style.background = 'var(--v07-surface-raised)';
          row.style.gap = '8px';

          // Permissions formatting
          const perms = (p.permissions || []).map(pm => `<span class="chip" style="font-size:9px; padding:1px 5px;">${escapeHtml(pm)}</span>`).join(' ') || '<span style="font-size:10px; color:var(--v07-text-muted);">sandbox</span>';

          // Widgets dashboard toggle switches
          let widgetToggleHtml = '';
          if (p.widgets && p.widgets.length > 0) {
            widgetToggleHtml = `
              <div style="margin-top:6px; padding-top:6px; border-top:1px solid var(--v07-border); display:flex; flex-direction:column; gap:4px;">
                <span style="font-size:10px; font-weight:700; color:var(--v07-text-muted); text-transform:uppercase;">Dashboard Utilities</span>
                ${p.widgets.map(w => {
                  const wKey = w.id || w.entry;
                  const activeOnDash = isWidgetEnabledOnDashboard(p.id, wKey);
                  return `
                    <div class="utility-toggle-row">
                      <span>${escapeHtml(w.title)}</span>
                      <button class="btn ${activeOnDash ? 'btn-ghost' : 'btn-secondary'}" style="padding:2px 8px; font-size:10px;" onclick="setWidgetEnabledOnDashboard('${p.id}', '${wKey}', ${!activeOnDash})">
                        ${activeOnDash ? '✓ On Dashboard' : '+ Show on Dashboard'}
                      </button>
                    </div>
                  `;
                }).join('')}
              </div>
            `;
          }

          row.innerHTML = `
            <div style="display:flex; justify-content:space-between; align-items:flex-start; gap:8px;">
              <div style="display:flex; align-items:center; gap:10px;">
                <div class="plugin-icon-box" style="width:34px; height:34px; background:${meta.bg}; border-color:${meta.color};">
                  ${meta.svg}
                </div>
                <div>
                  <div style="font-weight:700; font-size:13px; color:var(--v07-text-lead);">${escapeHtml(p.name)} <span class="brand-tag">v${p.version}</span></div>
                  <div style="font-size:11px; color:var(--v07-text-muted); font-family:var(--font-mono);">${escapeHtml(p.id)}</div>
                </div>
              </div>
              <span class="gauge-status-badge" style="color:${isEnabled ? 'var(--v07-emerald)' : 'var(--v07-text-muted)'}; border-color:${isEnabled ? 'var(--v07-emerald)' : 'var(--v07-border)'};">
                ${isEnabled ? 'Active' : 'Disabled'}
              </span>
            </div>

            <div style="font-size:12px; color:var(--v07-text-body);">${escapeHtml(p.description)}</div>

            <div style="display:flex; align-items:center; justify-content:space-between; margin-top:4px; flex-wrap:wrap; gap:6px;">
              <div style="display:flex; gap:4px; align-items:center;">
                <span style="font-size:10px; color:var(--v07-text-muted); font-weight:700;">PERMS:</span>
                ${perms}
              </div>
              <div style="display:flex; gap:6px;">
                ${isEnabled ? `<button class="btn btn-ghost" style="padding:4px 10px; font-size:11px;" onclick='openPluginPage(${JSON.stringify(p)})'>Open &rarr;</button>` : ''}
                <button class="btn ${isEnabled ? 'btn-warn' : 'btn-success'}" style="padding:4px 10px; font-size:11px;" onclick="handleTogglePlugin('${p.id}', ${!isEnabled})">
                  ${isEnabled ? 'Disable' : 'Enable'}
                </button>
              </div>
            </div>

            ${isEnabled ? widgetToggleHtml : ''}
          `;
          managerList.appendChild(row);
        }
      });

      if (dashUtilsCount) dashUtilsCount.textContent = `${activeWidgetsCount} Active`;
      if (statusBadge) statusBadge.textContent = `${activePluginsCount} Active`;
      if (dashUtilsHeader) dashUtilsHeader.style.display = (activeWidgetsCount > 0) ? 'flex' : 'none';

    } catch (err) {
      console.error('Failed to load plugins:', err);
    }
  }

  window.setWidgetEnabledOnDashboard = setWidgetEnabledOnDashboard;

  window.openPluginPage = function(plugin) {
    const modal = document.getElementById('plugin-modal');
    const frame = document.getElementById('plugin-modal-frame');
    const title = document.getElementById('modal-plugin-title');
    if (modal && frame) {
      if (title) title.textContent = plugin.nav_label || plugin.name;
      frame.src = `${BASE_URL}/plugins/${plugin.id}/${plugin.entry}?token=${sessionToken}`;
      modal.style.display = 'flex';
    } else {
      window.location.href = `${BASE_URL}/plugins/${plugin.id}/${plugin.entry}?token=${sessionToken}`;
    }
  };

  window.closePluginModal = function() {
    const modal = document.getElementById('plugin-modal');
    const frame = document.getElementById('plugin-modal-frame');
    if (modal) modal.style.display = 'none';
    if (frame) frame.src = 'about:blank';
    loadDayGlance();
    loadPlugins();
  };

  window.handleTogglePlugin = async function(id, enable) {
    await api('/api/v3/plugins/toggle', 'POST', { id: id, enabled: enable });
    showToast(`Plugin ${enable ? 'enabled' : 'disabled'}`, 'info');
    loadPlugins();
  };

  // ─── BACKGROUND SYNC LOOP ─────────────────────────────────────────────────
  async function syncStatus() {
    try {
      const res = await api('/api/v3/status');
      if (res.timer) {
        activeTimerState = {
          status: res.timer.status,
          topic: res.timer.topic,
          category: res.timer.category,
          mode: res.timer.mode,
          startTimestampMs: Date.now() - (res.timer.elapsed_ms || 0),
          pausedDurationMs: 0,
          targetDurationMs: (res.timer.mode === 'COUNTDOWN') ? (res.timer.elapsed_ms + res.timer.remaining_ms) : 0
        };
      }
      updateTimerDisplay();
    } catch (_) {}
  }

  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  // ─── INITIALIZATION ───────────────────────────────────────────────────────
  window.addEventListener('DOMContentLoaded', () => {
    switchTab('dashboard');
    syncStatus();
    loadScreenTime();
    loadDayGlance();

    // 1-second display ticker
    if (tickerInterval) clearInterval(tickerInterval);
    tickerInterval = setInterval(updateTimerDisplay, 1000);

    // 4-second background state synchronizer
    setInterval(syncStatus, 4000);
  });

})();
