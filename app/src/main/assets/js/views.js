/**
 * V07 V3 SANCTUARY VIEWS & UI CONTROLLERS (SanctuaryViews)
 * 
 * Normative Requirements Addressed:
 * - Impeccable Law 1: Zero Text Breakage (Mandatory esc() sanitization & truncation guards).
 * - UI Flow Scrutiny (Section 3):
 *   * Dynamic habit denominator in adherence calculation (Section 4.1).
 *   * 140-day activity heatmap & multi-day habit check-in matrix.
 *   * Inline habit editing modal (title, icon, color, cadence).
 *   * Obligations (tasks) edit modal, soft-delete with 5s undo snackbar.
 *   * Rich Markdown thoughts renderer.
 *   * Timeless Wisdom (Gita) reader with proper Sanskrit unicode.
 *   * Reading Journey with vector SVG fallbacks on broken covers.
 *   * Sovereign Vault JSON export & schema-validated import diff preview.
 */

(function(window) {
  'use strict';

  // --- Impeccable String Escaping & Markdown Parser ---
  function cleanText(input) {
    if (input == null) return '';
    if (typeof input === 'object') {
      if (input.value) input = input.value;
      else if (Array.isArray(input)) input = input.join(', ');
      else input = '';
    }
    const str = String(input).replace(/<[^>]+>/g, ' ');
    const txt = document.createElement('textarea');
    txt.innerHTML = str;
    return txt.value.replace(/\s+/g, ' ').trim();
  }

  function esc(str) {
    if (str == null) return '';
    return cleanText(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function renderSimpleMarkdown(md) {
    if (!md) return '';
    let html = esc(md);
    // Headers
    html = html.replace(/^### (.*$)/gim, '<h4 class="text-sm font-bold mt-2 mb-1 text-[var(--text)]">$1</h4>');
    html = html.replace(/^## (.*$)/gim, '<h3 class="text-base font-bold mt-3 mb-1 text-[var(--text)]">$1</h3>');
    html = html.replace(/^# (.*$)/gim, '<h2 class="text-lg font-bold mt-3 mb-2 text-[var(--text)]">$1</h2>');
    // Bold & Italics
    html = html.replace(/\*\*(.*?)\*\*/g, '<strong class="font-semibold text-[var(--text)]">$1</strong>');
    html = html.replace(/\*(.*?)\*/g, '<em class="italic text-[var(--text-secondary)]">$1</em>');
    // Blockquotes
    html = html.replace(/^\&gt; (.*$)/gim, '<blockquote class="border-l-2 border-[var(--primary)] pl-3 my-2 text-[var(--text-secondary)] italic">$1</blockquote>');
    // Checklists & Bullet lists
    html = html.replace(/^- \[x\] (.*$)/gim, '<div class="flex items-center gap-2 text-xs text-[var(--success)] my-1"><span class="material-symbols-outlined text-sm">check_box</span><span class="line-through opacity-70">$1</span></div>');
    html = html.replace(/^- \[ \] (.*$)/gim, '<div class="flex items-center gap-2 text-xs text-[var(--text-secondary)] my-1"><span class="material-symbols-outlined text-sm">check_box_outline_blank</span><span>$1</span></div>');
    html = html.replace(/^- (.*$)/gim, '<li class="ml-4 list-disc text-xs text-[var(--text-secondary)] my-0.5">$1</li>');
    // Line breaks
    html = html.replace(/\n/g, '<br>');
    return html;
  }

  // Fallback procedural SVG book cover (Impeccable Law 3)
  const FALLBACK_BOOK_COVER = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 120 170" fill="%23161B22"><rect width="120" height="170" rx="6" stroke="%2328303E" stroke-width="2"/><path d="M20 40h80M20 60h80M20 80h50" stroke="%2364748B" stroke-width="2" stroke-linecap="round"/><text x="60" y="125" fill="%2394A3B8" font-size="11" font-family="sans-serif" font-weight="600" text-anchor="middle">SOVEREIGN</text><text x="60" y="140" fill="%2364748B" font-size="9" font-family="sans-serif" text-anchor="middle">READER</text></svg>';

  class SanctuaryViews {
    constructor() {
      this.selectedDate = this.getTodayDateString();
      this.taskFilter = 'pending'; // 'pending' | 'completed' | 'all'
      this.gitaDataset = null;
      this.readingList = [
        { id: 'bk_1', title: 'Deep Work', author: 'Cal Newport', totalPages: 304, currentPage: 184, cover: '' },
        { id: 'bk_2', title: 'The Bhagavad Gita', author: 'Eknath Easwaran', totalPages: 380, currentPage: 220, cover: '' },
        { id: 'bk_3', title: 'Atomic Habits', author: 'James Clear', totalPages: 320, currentPage: 320, cover: '' }
      ];
      this.initGitaLoader();
    }

    getTodayDateString(d = new Date()) {
      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    }

    formatDateHeader(dateStr) {
      try {
        const parts = dateStr.split('-');
        const dateObj = new Date(parts[0], parts[1] - 1, parts[2]);
        const options = { weekday: 'long', month: 'short', day: 'numeric', year: 'numeric' };
        return dateObj.toLocaleDateString('en-US', options);
      } catch (_) {
        return dateStr;
      }
    }

    // --- Gita Dataset Pre-loader ---
    async initGitaLoader() {
      try {
        const res = await fetch('gita.json');
        if (res.ok) {
          this.gitaDataset = await res.json();
          console.log('[SanctuaryViews] Gita dataset loaded:', this.gitaDataset.length, 'verses');
        }
      } catch (err) {
        console.warn('[SanctuaryViews] Gita dataset load error:', err.message);
      }
    }

    // --- Render All Views Pipeline ---
    renderAll() {
      this.renderTopBarHUD();
      this.renderCockpit();
      this.renderHabitsCadence();
      this.renderTasks();
      this.renderJournal();
      this.renderWisdomGita();
      this.renderReader();
      this.renderPluginHub();
      this.renderAnalytics();
      this.renderVaultConsole();
    }

    // --- Top Bar HUD & Date Selector ---
    renderTopBarHUD() {
      const se = window.V07StorageEngine;
      const statusPill = document.getElementById('hudStatusPill');
      const statusIcon = document.getElementById('hudStatusIcon');
      const statusText = document.getElementById('hudStatusText');
      const mutationBadge = document.getElementById('hudMutationBadge');
      const dateDisplay = document.getElementById('hudDateDisplay');

      if (dateDisplay) {
        dateDisplay.textContent = this.formatDateHeader(this.selectedDate);
      }

      if (statusPill && statusText && statusIcon) {
        statusPill.style.cursor = 'pointer';
        statusPill.title = 'Click to manage Profile, Account & Cloud Sync';
        statusPill.onclick = () => window.SanctuaryApp.openProfileModal();
        const activeProf = se.getActiveProfileId ? se.getActiveProfileId() : 'guest';
        const isAuth = !!se.currentUser;
        const syncOn = se.isSyncEnabled !== false;

        if (se.mode === 'app_sqlite') {
          statusPill.className = 'flex items-center gap-1.5 px-2 py-1 rounded-full text-xs font-semibold bg-[var(--primary-dim)] text-[var(--primary)] border border-[var(--primary)]/20 cursor-pointer';
          statusIcon.textContent = syncOn ? (isAuth ? 'sync' : 'database') : 'cloud_off';
          statusText.textContent = isAuth ? (syncOn ? 'Cloud Sync' : 'Sync Off') : 'App Local';
        } else if (se.mode === 'cloud_firestore') {
          statusPill.className = syncOn 
            ? 'flex items-center gap-1.5 px-2 py-1 rounded-full text-xs font-semibold bg-[var(--success-dim)] text-[var(--success)] border border-[var(--success)]/20 cursor-pointer'
            : 'flex items-center gap-1.5 px-2 py-1 rounded-full text-xs font-semibold bg-[var(--warning-dim)] text-[var(--warning)] border border-[var(--warning)]/20 cursor-pointer';
          statusIcon.textContent = syncOn ? 'cloud_done' : 'cloud_off';
          statusText.textContent = syncOn ? (se.currentUser?.email ? `${se.currentUser.email.split('@')[0]}` : 'Cloud') : 'Paused';
        } else {
          statusPill.className = 'flex items-center gap-1.5 px-2 py-1 rounded-full text-xs font-semibold bg-[var(--warning-dim)] text-[var(--warning)] border border-[var(--warning)]/20 cursor-pointer';
          statusIcon.textContent = 'person';
          statusText.textContent = `${activeProf}`;
        }
      }

      if (mutationBadge) {
        if (se.uncommittedCount > 0) {
          mutationBadge.classList.remove('hidden');
          mutationBadge.textContent = se.uncommittedCount;
        } else {
          mutationBadge.classList.add('hidden');
        }
      }
    }

    // --- View 1: Sanctuary Cockpit ---
    renderCockpit() {
      const se = window.V07StorageEngine;
      const chrono = window.FocusChronometer;

      // 1. Calculate Today's Metrics
      const todaySessions = se.getSessionsForDate(this.selectedDate);
      const totalFocusSec = todaySessions.reduce((acc, s) => acc + (s.durationSec || (s.duration_min ? s.duration_min * 60 : 0)), 0);
      const totalFocusMin = Math.round(totalFocusSec / 60);

      const pendingTasksCount = se.tasks.filter(t => t.status === 'pending' && !se.isTombstoned(t.id)).length;
      const habitStats = se.getHabitConsistency(this.selectedDate);

      // 2. Update KPI Pills
      const kpiFocus = document.getElementById('kpiFocusMin');
      const kpiTasks = document.getElementById('kpiPendingTasks');
      const kpiHabits = document.getElementById('kpiHabitRatio');
      const kpiHabitSub = document.getElementById('kpiHabitSub');

      if (kpiFocus) kpiFocus.textContent = totalFocusMin + 'm';
      if (kpiTasks) kpiTasks.textContent = pendingTasksCount;
      if (kpiHabits) kpiHabits.textContent = habitStats.percentage + '%';
      if (kpiHabitSub) kpiHabitSub.textContent = `${habitStats.checked}/${habitStats.total} rituals completed`;

      // Telemetry & Glance IDs
      const dashFocusGlance = document.getElementById('dash-glance-focus');
      const dashTasksGlance = document.getElementById('dash-glance-tasks');
      if (dashFocusGlance) dashFocusGlance.textContent = totalFocusMin + 'm';
      if (dashTasksGlance) dashTasksGlance.textContent = `${se.tasks.filter(t => t.status === 'completed' && !se.isTombstoned(t.id)).length} / ${se.tasks.filter(t => !se.isTombstoned(t.id)).length}`;

      // 3. Render Circular SVG Focus Gauge
      this.updateChronoGaugeDisplay();

      // 4. Render Sovereign Activity Spine (Timeline)
      this.renderActivitySpine(todaySessions);
    }

    updateChronoGaugeDisplay() {
      const chrono = window.FocusChronometer;
      const digitsEl = document.getElementById('chronoDigits');
      const dashDigitsEl = document.getElementById('dash-focus-digits');
      const hudTimerEl = document.getElementById('hud-timer-label');
      const progressCircle = document.getElementById('chronoProgressCircle');
      const modeLabel = document.getElementById('chronoModeLabel');
      const categoryBadge = document.getElementById('dash-focus-category');
      const timerStatus = document.getElementById('dash-timer-status');
      const toggleBtn = document.getElementById('chronoToggleBtn');
      const toggleIcon = document.getElementById('chronoToggleIcon');

      const timeStr = chrono.getFormattedTime();
      if (digitsEl) digitsEl.textContent = timeStr;
      if (dashDigitsEl) dashDigitsEl.textContent = timeStr;
      if (hudTimerEl) hudTimerEl.textContent = timeStr;
      if (modeLabel) modeLabel.textContent = `${chrono.activity} · ${chrono.mode.toUpperCase()}`;
      if (categoryBadge) categoryBadge.textContent = chrono.activity || 'Deep Work';

      if (timerStatus) {
        if (chrono.state === 'RUNNING') {
          timerStatus.textContent = 'Running';
          timerStatus.style.color = 'var(--color-info)';
          timerStatus.style.borderColor = 'var(--color-info)';
        } else if (chrono.state === 'PAUSED') {
          timerStatus.textContent = 'Paused';
          timerStatus.style.color = 'var(--color-warning)';
          timerStatus.style.borderColor = 'var(--color-warning)';
        } else {
          timerStatus.textContent = 'Ready';
          timerStatus.style.color = 'var(--color-text-muted)';
          timerStatus.style.borderColor = 'var(--color-border)';
        }
      }

      if (progressCircle) {
        let radius = 70;
        try {
          if (progressCircle.r && progressCircle.r.baseVal) {
            radius = progressCircle.r.baseVal.value;
          }
        } catch (_) {}
        const circumference = 2 * Math.PI * radius;
        const ratio = chrono.getProgressRatio();
        const offset = circumference - (ratio * circumference);
        progressCircle.style.strokeDashoffset = offset;
      }

      if (toggleBtn) {
        if (chrono.state === 'RUNNING') {
          if (toggleIcon) toggleIcon.textContent = 'pause';
          toggleBtn.className = 'btn btn-warn';
          toggleBtn.innerHTML = `
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></svg>
            <span>Pause</span>
          `;
        } else {
          if (toggleIcon) toggleIcon.textContent = 'play_arrow';
          toggleBtn.className = 'btn btn-primary';
          toggleBtn.innerHTML = `
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"/></svg>
            <span>Begin Flow</span>
          `;
        }
      }
    }

    renderActivitySpine(sessions) {
      const container = document.getElementById('cockpitTimelineSpine');
      if (!container) return;

      const se = window.V07StorageEngine;
      const thoughts = se.getThoughtsForDate(this.selectedDate);
      const habitLogs = se.getHabitCheckInsForDate(this.selectedDate);

      // Merge and sort all daily events chronologically
      const events = [
        ...sessions.map(s => ({ type: 'Focus Session', ts: s.startTime || s.ts || 0, title: s.title || s.activity, detail: `${Math.round((s.durationSec || 0) / 60)}m focus · ${s.notes || 'In the zone'}`, color: 'var(--color-info)' })),
        ...thoughts.map(t => ({ type: 'Insight Captured', ts: t.ts || 0, title: t.title || 'Reflection', detail: t.content || '', color: 'var(--color-warning)' })),
        ...habitLogs.map(h => ({ type: 'Ritual Completed', ts: h.ts || 0, title: h.habit_name || 'Daily Protocol', detail: 'Consistent cadence achieved', color: 'var(--color-success)' }))
      ].sort((a, b) => b.ts - a.ts);

      if (events.length === 0) {
        container.innerHTML = `
          <div class="empty-state">
            <div class="empty-state-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 14 14"/></svg>
            </div>
            <div class="empty-state-heading">Stillness on this date</div>
            <div class="empty-state-description">Start a deep focus interval or record a thought to illuminate this day.</div>
          </div>
        `;
        return;
      }

      container.innerHTML = `
        <div class="timeline-stream-container">
          ${events.map(ev => `
            <div class="timeline-item" style="--item-color: ${ev.color};">
              <div class="timeline-item-meta">${ev.ts ? new Date(ev.ts).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''} • ${esc(ev.type)}</div>
              <div class="timeline-item-title">${esc(ev.title)}</div>
              <div class="timeline-item-sub line-clamp-2">${esc(ev.detail)}</div>
            </div>
          `).join('')}
        </div>
      `;
    }

    // --- View 2: Habit Cadence Matrix & 140-Day Heatmap ---
    renderHabitsCadence() {
      const se = window.V07StorageEngine;
      const habitsList = se.getHabitsList();

      // 1. Render 140-Day Heatmap (20 Weeks x 7 Days)
      const heatmapContainer = document.getElementById('habitsHeatmapContainer');
      if (heatmapContainer) {
        const days = [];
        const today = new Date();
        for (let i = 139; i >= 0; i--) {
          const d = new Date(today);
          d.setDate(today.getDate() - i);
          const dateStr = this.getTodayDateString(d);
          const logs = se.getHabitCheckInsForDate(dateStr);
          const level = logs.length === 0 ? 0 : logs.length <= 3 ? 1 : logs.length <= 6 ? 2 : logs.length <= 9 ? 3 : 4;
          days.push(`<div class="heatmap-day" data-level="${level}" title="${dateStr}: ${logs.length} habits completed"></div>`);
        }
        heatmapContainer.innerHTML = days.join('');
      }

      // 2. Render Past 7 Days Column Headers
      const daysRow = document.getElementById('habitMatrixDaysRow');
      const past7Dates = [];
      const now = new Date();
      for (let i = 6; i >= 0; i--) {
        const d = new Date(now);
        d.setDate(now.getDate() - i);
        past7Dates.push(this.getTodayDateString(d));
      }

      if (daysRow) {
        daysRow.innerHTML = past7Dates.map(dateStr => {
          const d = new Date(dateStr + 'T00:00:00');
          const dayName = d.toLocaleDateString('en-US', { weekday: 'narrow' });
          const isToday = dateStr === this.getTodayDateString();
          return `<div class="text-center w-7 font-bold text-[10px] ${isToday ? 'text-[var(--primary)] font-extrabold' : 'text-[var(--text-muted)]'}">${dayName}</div>`;
        }).join('');
      }

      // 3. Render Habit Rows with Multi-Day Direct Check-in (Section 3 Requirement)
      const listContainer = document.getElementById('habitsListContainer');
      if (!listContainer) return;

      listContainer.innerHTML = habitsList.map(h => {
        const dotsHtml = past7Dates.map(dateStr => {
          const isChecked = se.isHabitChecked(h.id, dateStr);
          return `
            <div onclick="window.SanctuaryViews.toggleHabit('${esc(h.id)}', '${dateStr}')" 
                 class="habit-day-dot ${isChecked ? 'checked' : ''}" 
                 title="${esc(h.title)} · ${dateStr}">
              ${isChecked ? '<span class="material-symbols-outlined text-[13px]">check</span>' : ''}
            </div>
          `;
        }).join('');

        return `
          <div class="habit-row">
            <div class="flex items-center gap-3 min-w-0 flex-1">
              <span class="text-xl flex-shrink-0">${esc(h.emoji || '✨')}</span>
              <div class="min-w-0 flex-1">
                <p class="text-sm font-semibold text-[var(--text)] truncate-single">${esc(h.title)}</p>
                <p class="text-[11px] text-[var(--text-muted)]">${esc(h.frequency || 'Daily Cadence')}</p>
              </div>
              <button onclick="window.SanctuaryViews.openHabitEditor('${esc(h.id)}')" class="text-[var(--text-muted)] hover:text-[var(--text)] p-1 rounded-md transition-colors" title="Edit habit">
                <span class="material-symbols-outlined text-base">tune</span>
              </button>
            </div>
            <div class="habit-dots-week">
              ${dotsHtml}
            </div>
          </div>
        `;
      }).join('');
    }

    async toggleHabit(habitId, dateStr) {
      await window.V07StorageEngine.toggleHabitCheckIn(habitId, dateStr);
      this.renderCockpit();
      this.renderHabitsCadence();
      this.showToast('Habit check-in updated (+20 XP)');
    }

    // --- View 3: Obligations & Tasks ---
    renderTasks() {
      const se = window.V07StorageEngine;
      const container = document.getElementById('tasksListContainer');
      if (!container) return;

      const filtered = se.tasks.filter(t => {
        if (se.isTombstoned(t.id)) return false;
        if (this.taskFilter === 'pending') return t.status === 'pending';
        if (this.taskFilter === 'completed') return t.status === 'completed';
        return true;
      });

      if (filtered.length === 0) {
        container.innerHTML = `
          <div class="empty-state">
            <div class="empty-state-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
            </div>
            <div class="empty-state-heading">No obligations in this view</div>
            <div class="empty-state-description">Enjoy unburdened clarity, or add an obligation above.</div>
          </div>
        `;
        return;
      }

      container.innerHTML = `
        <div class="task-list">
          ${filtered.map(t => {
            const isDone = t.status === 'completed';
            const priorityClass = t.priority === 'high' ? 'high' : t.priority === 'low' ? 'low' : 'medium';

            return `
              <div class="task-item ${isDone ? 'completed' : ''}">
                <div class="task-checkbox" onclick="window.SanctuaryViews.toggleTaskStatus('${esc(t.id)}')">
                  ${isDone ? '✓' : ''}
                </div>
                <span class="task-title" onclick="window.SanctuaryViews.toggleTaskStatus('${esc(t.id)}')">${esc(t.title)}</span>
                <span class="task-priority-badge ${priorityClass}">${esc(t.priority || 'MED').toUpperCase()}</span>
                <button onclick="window.SanctuaryViews.openTaskEditor('${esc(t.id)}')" class="btn btn-ghost !p-1.5 !min-h-0" title="Edit obligation">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
                </button>
                <button onclick="window.SanctuaryViews.deleteTaskWithUndo('${esc(t.id)}')" class="btn btn-ghost !p-1.5 !min-h-0 hover:!text-[var(--color-danger)]" title="Delete obligation">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                </button>
              </div>
            `;
          }).join('')}
        </div>
      `;
    }

    async toggleTaskStatus(taskId) {
      const se = window.V07StorageEngine;
      const t = se.tasks.find(x => x.id === taskId);
      if (t) {
        t.status = t.status === 'completed' ? 'pending' : 'completed';
        t.completedAt = t.status === 'completed' ? Date.now() : null;
        await se.saveTask(t);
        this.renderCockpit();
        this.renderTasks();
        this.showToast(t.status === 'completed' ? 'Obligation completed (+40 XP)' : 'Obligation reopened');
      }
    }

    deleteTaskWithUndo(taskId) {
      const se = window.V07StorageEngine;
      const removed = se.softDeleteTask(taskId, () => {
        console.log('[SanctuaryViews] Soft delete finalized for task:', taskId);
      });

      if (removed) {
        this.renderCockpit();
        this.renderTasks();
        this.showToast(`Obligation removed: "${removed.title}"`, true);
      }
    }

    // --- View 4: Contemplative Journal / Thoughts ---
    renderJournal() {
      const se = window.V07StorageEngine;
      const container = document.getElementById('journalEntriesContainer');
      if (!container) return;

      const thoughts = se.getThoughtsForDate(this.selectedDate);
      if (thoughts.length === 0) {
        container.innerHTML = `
          <div class="empty-state">
            <div class="empty-state-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            </div>
            <div class="empty-state-heading">No reflections recorded for this day</div>
            <div class="empty-state-description">Capture a fleeting insight or contemplative realization above.</div>
          </div>
        `;
        return;
      }

      container.innerHTML = `
        <div style="display:flex; flex-direction:column; gap:10px;">
          ${thoughts.map(th => `
            <div class="thought-card ${th.pinned ? 'pinned' : ''}">
              <div class="thought-content">${renderSimpleMarkdown(th.content)}</div>
              <div class="thought-meta-row">
                <span style="font-weight:700; color:${th.pinned ? 'var(--color-info)' : 'var(--color-warning)'};">${esc(th.title || 'CONTEMPLATION').toUpperCase()}${th.pinned ? ' • PINNED' : ''}</span>
                <span>${th.ts ? new Date(th.ts).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}</span>
              </div>
            </div>
          `).join('')}
        </div>
      `;
    }

    // --- View 5: Timeless Wisdom (Bhagavad Gita) ---
    renderWisdomGita(chapter = 2, verse = 47) {
      const sanskritEl = document.getElementById('gitaSanskritVerse');
      const hindiEl = document.getElementById('gitaHindiTranslation');
      const englishEl = document.getElementById('gitaEnglishTranslation');
      const refHeading = document.getElementById('gitaVerseHeading');

      if (refHeading) refHeading.textContent = `CHAPTER ${chapter} · SHLOKA ${verse}`;

      // Check if dataset is available
      if (this.gitaDataset && Array.isArray(this.gitaDataset)) {
        const item = this.gitaDataset.find(v => v.chapter === chapter && v.verse === verse);
        if (item) {
          if (sanskritEl) sanskritEl.innerHTML = esc(item.shloka?.text || '');
          if (hindiEl) hindiEl.textContent = item.hindi_translation?.text || '';
          if (englishEl) englishEl.textContent = `"${item.english_sivananda?.text || item.english_gambirananda?.text || ''}"`;
          return;
        }
      }

      // Default Classic Canonical Verse (Gita 2.47)
      if (sanskritEl) sanskritEl.innerHTML = 'कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।<br>मा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥';
      if (hindiEl) hindiEl.textContent = 'तेरा कर्म करने में ही अधिकार है, उसके फलों में कभी नहीं; इसलिए तू फल की इच्छा वाला मत हो और तेरी अकर्मण्यता में भी आसक्ति न हो।';
      if (englishEl) englishEl.textContent = '"Thy right is to work only, but never to its fruits; let not the fruit of action be thy motive, nor let thy attachment be to inaction."';
    }

    async saveGitaContemplation() {
      const inp = document.getElementById('gitaReflectionNote');
      if (!inp || !inp.value.trim()) return;

      const chap = parseInt(document.getElementById('gitaChapterSelect')?.value) || 2;
      const ver = parseInt(document.getElementById('gitaVerseSelect')?.value) || 47;
      const note = inp.value.trim();

      await window.V07StorageEngine.saveThought({
        title: `Gita Wisdom Reflection (${chap}.${ver})`,
        content: note,
        date: this.selectedDate,
        ts: Date.now()
      });

      inp.value = '';
      this.renderJournal();
      this.renderCockpit();
      this.showToast('Contemplative reflection illuminated in sanctuary');
    }

    // --- View 6: Reading Journey (Reader) ---
    renderReader() {
      const container = document.getElementById('readingListContainer');
      if (!container) return;

      container.innerHTML = this.readingList.map(bk => {
        const pct = Math.min(100, Math.round((bk.currentPage / bk.totalPages) * 100));
        return `
          <div class="flex gap-4 p-4 bg-[var(--surface-alt)] border border-[var(--border)] rounded-xl">
            <img src="${esc(bk.cover || FALLBACK_BOOK_COVER)}" 
                 onerror="this.onerror=null; this.src='${FALLBACK_BOOK_COVER}';" 
                 class="w-16 h-24 object-cover rounded-md border border-[var(--border)] flex-shrink-0" alt="Cover" />
            <div class="flex-1 min-w-0 flex flex-col justify-between">
              <div>
                <h4 class="text-sm font-bold text-[var(--text)] truncate-single">${esc(bk.title)}</h4>
                <p class="text-xs text-[var(--text-secondary)]">${esc(bk.author)}</p>
              </div>
              <div>
                <div class="flex items-center justify-between text-[11px] text-[var(--text-muted)] mb-1">
                  <span>${bk.currentPage} / ${bk.totalPages} pages</span>
                  <span class="font-bold text-[var(--primary)]">${pct}%</span>
                </div>
                <div class="w-full h-1.5 bg-[var(--surface-hover)] rounded-full overflow-hidden">
                  <div class="h-full bg-[var(--accent-reader)] rounded-full" style="width:${pct}%;"></div>
                </div>
              </div>
            </div>
          </div>
        `;
      }).join('');
    }

    // --- View 7: Plugin Hub ---
    renderPluginHub() {
      const container = document.getElementById('pluginCardsContainer');
      if (!container) return;

      const plugins = [
        { id: 'com.v07.habits', name: 'Habits Cadence', desc: '140-day consistency heatmap & ritual tracking.', icon: 'repeat', category: 'Ritual' },
        { id: 'com.v07.tasks', name: 'Obligations & Directives', desc: 'Prioritized checklist engine with undo recovery.', icon: 'checklist', category: 'Focus' },
        { id: 'com.v07.timetracker', name: 'Deep Chronometer', desc: 'Interval timer with monotonic background immunity.', icon: 'timer', category: 'Focus' },
        { id: 'com.v07.vitals', name: 'Mindful Vitals', desc: 'Serene mood and emotional energy reflections.', icon: 'favorite', category: 'Mindfulness' },
        { id: 'com.v07.running', name: 'Athletic Conditioning', desc: 'Cardiovascular endurance & physical cadence.', icon: 'directions_run', category: 'Health' },
        { id: 'com.v07.reader', name: 'Sovereign Reader', desc: 'Kindle & shelf sync for contemplative reading.', icon: 'menu_book', category: 'Wisdom' },
        { id: 'com.v07.gamify', name: 'Sovereign Progression', desc: 'Meaningful XP rewards for focused presence.', icon: 'military_tech', category: 'Mastery' },
        { id: 'com.v07.chronosynth', name: 'Chronosynth Audio', desc: 'Ambient binaural frequencies and singing bowls.', icon: 'graphic_eq', category: 'Audio' },
        { id: 'com.v07.gita', name: 'Timeless Wisdom', desc: 'Daily Bhagavad Gita shlokas and contemplation.', icon: 'auto_stories', category: 'Wisdom' }
      ];

      container.innerHTML = plugins.map(p => `
        <div class="p-4 bg-[var(--surface)] border border-[var(--border)] rounded-xl flex flex-col justify-between hover:border-[var(--border-focus)] transition-colors">
          <div>
            <div class="flex items-center justify-between mb-3">
              <span class="material-symbols-outlined text-2xl text-[var(--primary)]">${p.icon}</span>
              <span class="text-[10px] font-bold px-2 py-0.5 rounded bg-[var(--surface-alt)] text-[var(--text-muted)]">${p.category}</span>
            </div>
            <h4 class="text-sm font-bold text-[var(--text)] mb-1">${esc(p.name)}</h4>
            <p class="text-xs text-[var(--text-secondary)] line-clamp-2">${esc(p.desc)}</p>
          </div>
          <button onclick="window.V07PluginHost.launchPlugin({ id: '${p.id}', name: '${esc(p.name)}' })" 
                  class="mt-4 w-full py-2 rounded-lg bg-[var(--surface-alt)] hover:bg-[var(--primary)] hover:text-white text-xs font-semibold text-[var(--text)] transition-colors flex items-center justify-center gap-1.5 btn-press">
            <span class="material-symbols-outlined text-sm">open_in_new</span> Launch Runner
          </button>
        </div>
      `).join('');
    }

    // --- View 8: Analytics & Gamification ---
    renderAnalytics() {
      const se = window.V07StorageEngine;
      const totalSessions = se.sessions.length;
      const totalHabitLogs = se.habitLogs.filter(h => h.completed).length;
      const completedTasks = se.tasks.filter(t => t.status === 'completed').length;

      // Calculate dynamic level & XP
      const totalXp = (totalSessions * 50) + (totalHabitLogs * 20) + (completedTasks * 40);
      const level = Math.floor(totalXp / 500) + 1;
      const currentLevelXp = totalXp % 500;
      const levelProgress = Math.round((currentLevelXp / 500) * 100);

      const xpDisplay = document.getElementById('analyticsTotalXp');
      const lvlDisplay = document.getElementById('analyticsLevelBadge');
      const progressEl = document.getElementById('analyticsLevelProgress');

      if (xpDisplay) xpDisplay.textContent = totalXp.toLocaleString() + ' XP';
      if (lvlDisplay) lvlDisplay.textContent = 'Level ' + level;
      if (progressEl) progressEl.style.width = levelProgress + '%';
    }

    // --- View 9: Sovereign Vault & Settings ---
    renderVaultConsole() {
      const se = window.V07StorageEngine;
      const deviceUuidEl = document.getElementById('vaultDeviceUuid');
      const enginePill = document.getElementById('vaultEngineBadge');
      const countsEl = document.getElementById('vaultStatsCounts');

      if (deviceUuidEl) deviceUuidEl.textContent = se.deviceId;
      if (enginePill) {
        enginePill.textContent = se.mode === 'cloud_firestore' ? 'Cloud Firestore Active' : 'Local Sanctuary (Offline Guest)';
      }
      if (countsEl) {
        countsEl.textContent = `${se.sessions.length} sessions · ${se.tasks.length} obligations · ${se.thoughts.length} thoughts · ${se.tombstones.size} tombstones`;
      }
    }

    // --- Toast / Snackbar Notification Engine ---
    showToast(message, allowUndo = false) {
      const container = document.getElementById('toast-container');
      if (!container) return;

      const toast = document.createElement('div');
      toast.className = 'toast';
      toast.innerHTML = `
        <span class="material-symbols-outlined text-[var(--primary)] text-base">info</span>
        <span class="truncate-single flex-1">${esc(message)}</span>
        ${allowUndo ? `<button onclick="window.SanctuaryViews.executeUndo(this)" class="toast-undo-btn">UNDO</button>` : ''}
      `;

      container.appendChild(toast);
      setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        setTimeout(() => toast.remove(), 250);
      }, 4000);
    }

    executeUndo(btnEl) {
      if (window.V07StorageEngine.undoSoftDelete()) {
        this.renderCockpit();
        this.renderTasks();
        this.renderJournal();
        btnEl.parentElement.remove();
        this.showToast('Action undone successfully');
      }
    }

    // --- Modals Interactivity ---
    openHabitEditor(habitId) {
      const h = window.V07StorageEngine.canonicalizeHabit(habitId);
      if (!h) return;
      const modal = document.getElementById('habitEditorModal');
      const titleInp = document.getElementById('editHabitTitle');
      const idInp = document.getElementById('editHabitId');

      if (titleInp) titleInp.value = h.title;
      if (idInp) idInp.value = h.id;
      if (modal) modal.classList.add('active');
    }

    openTaskEditor(taskId) {
      const t = window.V07StorageEngine.tasks.find(x => x.id === taskId);
      if (!t) return;
      const modal = document.getElementById('taskEditorModal');
      const titleInp = document.getElementById('editTaskTitle');
      const idInp = document.getElementById('editTaskId');
      const prioritySel = document.getElementById('editTaskPriority');
      const deadlineInp = document.getElementById('editTaskDeadline');

      if (titleInp) titleInp.value = t.title;
      if (idInp) idInp.value = t.id;
      if (prioritySel) prioritySel.value = t.priority || 'medium';
      if (deadlineInp) deadlineInp.value = t.deadline || '';
      if (modal) modal.classList.add('active');
    }
  }

  window.SanctuaryViews = new SanctuaryViews();
})(window);
