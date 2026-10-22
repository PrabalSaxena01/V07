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
  function esc(str) {
    if (str == null) return '';
    return String(str)
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

    getTodayDateString() {
      const d = new Date();
      return d.toISOString().split('T')[0];
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
        statusPill.title = 'Click to manage Profile & Cloud Sync';
        statusPill.onclick = () => window.SanctuaryApp.openProfileModal();
        const activeProf = se.getActiveProfileId ? se.getActiveProfileId() : 'guest';
        if (se.mode === 'cloud_firestore') {
          statusPill.className = 'flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-[var(--success-dim)] text-[var(--success)] border border-[var(--success)]/20 cursor-pointer';
          statusIcon.textContent = 'cloud_done';
          statusText.textContent = se.currentUser?.email ? `${se.currentUser.email.split('@')[0]} (Cloud)` : `Profile: ${activeProf}`;
        } else {
          statusPill.className = 'flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-[var(--warning-dim)] text-[var(--warning)] border border-[var(--warning)]/20 cursor-pointer';
          statusIcon.textContent = 'person';
          statusText.textContent = `Profile: ${activeProf}`;
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

      // 3. Render Circular SVG Focus Gauge
      this.updateChronoGaugeDisplay();

      // 4. Render Sovereign Activity Spine (Timeline)
      this.renderActivitySpine(todaySessions);
    }

    updateChronoGaugeDisplay() {
      const chrono = window.FocusChronometer;
      const digitsEl = document.getElementById('chronoDigits');
      const progressCircle = document.getElementById('chronoProgressCircle');
      const modeLabel = document.getElementById('chronoModeLabel');
      const toggleBtn = document.getElementById('chronoToggleBtn');
      const toggleIcon = document.getElementById('chronoToggleIcon');

      // 1. Cockpit Mini Gauge
      if (digitsEl) digitsEl.textContent = chrono.getFormattedTime();
      if (modeLabel) modeLabel.textContent = ${chrono.activity} · ;

      if (progressCircle) {
        const radius = 100;
        const circumference = 2 * Math.PI * radius;
        const ratio = chrono.getProgressRatio();
        const offset = circumference - (ratio * circumference);
        progressCircle.style.strokeDashoffset = offset;
      }

      if (toggleBtn && toggleIcon) {
        if (chrono.state === 'RUNNING') {
          toggleIcon.textContent = 'pause';
          toggleBtn.className = 'touch-target px-6 py-2.5 rounded-xl font-bold bg-[var(--warning)] text-[var(--text-inverse)] flex items-center gap-2 btn-press';
        } else {
          toggleIcon.textContent = 'play_arrow';
          toggleBtn.className = 'touch-target px-6 py-2.5 rounded-xl font-bold bg-[var(--primary)] text-white flex items-center gap-2 btn-press';
        }
      }

      // 2. Dedicated Focus Sanctuary Full Circle Timer
      const sanctDigits = document.getElementById('sanctuaryChronoDigits');
      const sanctCircle = document.getElementById('sanctuaryProgressCircle');
      const sanctModePill = document.getElementById('sanctuaryModePill');
      const sanctActLabel = document.getElementById('sanctuaryChronoActivityName');
      const sanctToggleBtn = document.getElementById('sanctuaryToggleBtn');
      const sanctToggleIcon = document.getElementById('sanctuaryToggleIcon');
      const sanctToggleText = document.getElementById('sanctuaryToggleText');

      if (sanctDigits) sanctDigits.textContent = chrono.getFormattedTime();
      if (sanctModePill) sanctModePill.textContent = ${chrono.mode.toUpperCase()} MODE · M;
      if (sanctActLabel) sanctActLabel.textContent = chrono.activity;

      if (sanctCircle) {
        const sRadius = 118;
        const sCircumference = 2 * Math.PI * sRadius;
        const sRatio = chrono.getProgressRatio();
        const sOffset = sCircumference - (sRatio * sCircumference);
        sanctCircle.style.strokeDashoffset = sOffset;

        if (chrono.state === 'RUNNING') {
          sanctCircle.classList.add('chrono-running-pulse');
        } else {
          sanctCircle.classList.remove('chrono-running-pulse');
        }
      }

      if (sanctToggleBtn && sanctToggleIcon) {
        if (chrono.state === 'RUNNING') {
          sanctToggleIcon.textContent = 'pause';
          if (sanctToggleText) sanctToggleText.textContent = 'Pause Interval';
          sanctToggleBtn.className = 'touch-target px-8 py-2.5 rounded-xl font-bold bg-[var(--warning)] text-[var(--text-inverse)] flex items-center gap-2 btn-press';
        } else {
          sanctToggleIcon.textContent = 'play_arrow';
          if (sanctToggleText) sanctToggleText.textContent = 'Begin Flow';
          sanctToggleBtn.className = 'touch-target px-8 py-2.5 rounded-xl font-bold bg-[var(--primary)] text-white flex items-center gap-2 btn-press';
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
        ...sessions.map(s => ({ type: 'session', ts: s.startTime || s.ts || 0, title: s.title || s.activity, detail: `${Math.round((s.durationSec || 0) / 60)}m focus · ${s.notes || 'In the zone'}`, icon: 'timer', color: 'var(--accent-chrono)' })),
        ...thoughts.map(t => ({ type: 'thought', ts: t.ts || 0, title: t.title || 'Reflection', detail: t.content || '', icon: 'edit_note', color: 'var(--accent-reader)' })),
        ...habitLogs.map(h => ({ type: 'habit', ts: h.ts || 0, title: h.habit_name || 'Ritual', detail: 'Completed daily cadence', icon: 'check_circle', color: 'var(--accent-habits)' }))
      ].sort((a, b) => b.ts - a.ts);

      if (events.length === 0) {
        container.innerHTML = `
          <div class="p-8 text-center border border-dashed border-[var(--border)] rounded-xl">
            <span class="material-symbols-outlined text-3xl text-[var(--text-muted)] mb-2">bedtime</span>
            <p class="text-xs font-semibold text-[var(--text-secondary)]">Stillness on this date</p>
            <p class="text-[11px] text-[var(--text-muted)] mt-1">Start a deep focus interval or record a thought to illuminate this day.</p>
          </div>
        `;
        return;
      }

      container.innerHTML = events.map(ev => `
        <div class="timeline-item">
          <div class="timeline-dot" style="background:${ev.color};">
            <span class="material-symbols-outlined text-[11px] text-white">${ev.icon}</span>
          </div>
          <div class="bg-[var(--surface-alt)] p-3 rounded-xl border border-[var(--border)]">
            <div class="flex items-center justify-between mb-1">
              <span class="text-xs font-bold text-[var(--text)] truncate-single">${esc(ev.title)}</span>
              <span class="text-[10px] text-[var(--text-muted)] tabular-nums">${ev.ts ? new Date(ev.ts).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}</span>
            </div>
            <p class="text-xs text-[var(--text-secondary)] line-clamp-2">${esc(ev.detail)}</p>
          </div>
        </div>
      `).join('');
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
          const dateStr = d.toISOString().split('T')[0];
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
        past7Dates.push(d.toISOString().split('T')[0]);
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
          <div class="p-8 text-center border border-dashed border-[var(--border)] rounded-xl my-4">
            <span class="material-symbols-outlined text-3xl text-[var(--text-muted)] mb-2">checklist</span>
            <p class="text-xs font-semibold text-[var(--text-secondary)]">No obligations in this view</p>
            <p class="text-[11px] text-[var(--text-muted)] mt-1">Enjoy unburdened clarity, or add an obligation above.</p>
          </div>
        `;
        return;
      }

      container.innerHTML = filtered.map(t => {
        const isDone = t.status === 'completed';
        const priorityColor = t.priority === 'high' ? 'var(--danger)' : t.priority === 'low' ? 'var(--info)' : 'var(--warning)';

        return `
          <div class="flex items-center justify-between p-3.5 bg-[var(--surface-alt)] border border-[var(--border)] rounded-xl mb-2 gap-3 group transition-all">
            <div class="flex items-center gap-3 min-w-0 flex-1">
              <button onclick="window.SanctuaryViews.toggleTaskStatus('${esc(t.id)}')" class="touch-target text-[var(--text-muted)] hover:text-[var(--primary)] flex-shrink-0">
                <span class="material-symbols-outlined text-xl ${isDone ? 'text-[var(--success)]' : ''}">
                  ${isDone ? 'check_circle' : 'radio_button_unchecked'}
                </span>
              </button>
              <div class="min-w-0 flex-1">
                <p class="text-sm font-semibold ${isDone ? 'line-through opacity-60 text-[var(--text-muted)]' : 'text-[var(--text)]'} truncate-single">${esc(t.title)}</p>
                ${t.deadline ? `<span class="text-[10px] text-[var(--text-muted)] flex items-center gap-1 mt-0.5"><span class="material-symbols-outlined text-[11px]">schedule</span>${esc(t.deadline)}</span>` : ''}
              </div>
            </div>
            <div class="flex items-center gap-2">
              <span class="px-2 py-0.5 rounded text-[10px] font-bold" style="background:${priorityColor}22; color:${priorityColor};">${esc(t.priority || 'MED').toUpperCase()}</span>
              <button onclick="window.SanctuaryViews.openTaskEditor('${esc(t.id)}')" class="text-[var(--text-muted)] hover:text-[var(--text)] p-1.5 rounded-lg transition-colors">
                <span class="material-symbols-outlined text-base">edit</span>
              </button>
              <button onclick="window.SanctuaryViews.deleteTaskWithUndo('${esc(t.id)}')" class="text-[var(--text-muted)] hover:text-[var(--danger)] p-1.5 rounded-lg transition-colors" title="Delete obligation">
                <span class="material-symbols-outlined text-base">delete</span>
              </button>
            </div>
          </div>
        `;
      }).join('');
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
          <div class="p-8 text-center border border-dashed border-[var(--border)] rounded-xl my-4">
            <span class="material-symbols-outlined text-3xl text-[var(--text-muted)] mb-2">history_edu</span>
            <p class="text-xs font-semibold text-[var(--text-secondary)]">No reflections recorded for this day</p>
            <p class="text-[11px] text-[var(--text-muted)] mt-1">Capture a fleeting insight or contemplative realization above.</p>
          </div>
        `;
        return;
      }

      container.innerHTML = thoughts.map(th => `
        <div class="p-4 bg-[var(--surface-alt)] border border-[var(--border)] rounded-xl mb-3">
          <div class="flex items-center justify-between mb-2">
            <span class="text-xs font-bold text-[var(--primary)]">${esc(th.title || 'Contemplation')}</span>
            <span class="text-[10px] text-[var(--text-muted)] tabular-nums">${th.ts ? new Date(th.ts).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}</span>
          </div>
          <div class="prose-sanctuary text-xs leading-relaxed text-[var(--text)]">
            ${renderSimpleMarkdown(th.content)}
          </div>
        </div>
      `).join('');
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
