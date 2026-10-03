// Gamify plugin — XP, levels, streaks, perfect days.
// Reads existing event categories via V07.* bridge (synchronous) and maintains
// its own per-day XP ledger in the 'gamify' event category.

(function () {
  // ─── Level curve: cumulative XP required to REACH level L ───────────────
  // xpForLevel(L) = 100 * L^1.5
  function xpForLevel(level) {
    return Math.floor(100 * Math.pow(Math.max(level, 0), 1.5));
  }

  function levelFromXp(xp) {
    var level = 1;
    while (xpForLevel(level) <= xp) level++;
    return Math.max(1, level - 1);
  }

  function progressFromXp(xp) {
    xp = Math.max(0, xp);
    var level = levelFromXp(xp);
    var cur = xpForLevel(level);
    var next = xpForLevel(level + 1);
    var span = next - cur;
    var curXp = Math.max(0, xp - cur);
    return { level: level, currentXp: curXp, span: span, pct: span > 0 ? Math.min(1, curXp / span) : 0 };
  }

  // ─── XP sources ─────────────────────────────────────────────────────────
  var SOURCES = [
    { key: 'login',     label: 'Login',            xp: 10, note: 'First open of the day' },
    { key: 'streak',    label: 'Login streak',     xp: 5,  note: 'Streak bonus per day' },
    { key: 'mood',      label: 'Mood check-in',    xp: 5,  note: 'Per mood logged' },
    { key: 'session',   label: 'Session done',     xp: 15, note: 'Per completed session' },
    { key: 'habit',     label: 'Habit on',         xp: 7,  note: 'Per habit toggled on' },
    { key: 'dsa',       label: 'DSA solved',       xp: 20, note: 'Per completed DSA problem' },
    { key: 'journal',   label: 'Journal/thought',  xp: 5,  note: 'Per entry' },
    { key: 'pushups',   label: 'Pushups',          xp: 10, note: 'Per pushup log' },
    { key: 'perfect',   label: 'Perfect day',      xp: 50, note: 'All habits + session + mood' }
  ];

  function todayStr() {
    var d = new Date();
    var m = ('0' + (d.getMonth() + 1)).slice(-2);
    var day = ('0' + d.getDate()).slice(-2);
    return d.getFullYear() + '-' + m + '-' + day;
  }

  function dateStr(d) {
    var m = ('0' + (d.getMonth() + 1)).slice(-2);
    var day = ('0' + d.getDate()).slice(-2);
    return d.getFullYear() + '-' + m + '-' + day;
  }

  // ─── Reads existing categories via bridge (sync) ────────────────────────
  function getV07() {
    if (typeof window.V07 !== 'undefined' && window.V07) return window.V07;
    if (window.parent && typeof window.parent.V07 !== 'undefined' && window.parent.V07) return window.parent.V07;
    return null;
  }

  function readEventsAll(cat) {
    var v = getV07();
    if (v && v.readEventsAll) {
      var res = v.readEventsAll(cat);
      return (res && res.events) ? res.events : [];
    }
    return [];
  }

  function readEventsForDate(cat, date) {
    var v = getV07();
    if (v && v.readEvents) {
      var res = v.readEvents(cat, date);
      return (res && res.events) ? res.events : [];
    }
    return [];
  }

  function countForDate(events, date, predicate) {
    return events.filter(function (e) { return e.date === date && (!predicate || predicate(e)); }).length;
  }

  // ─── Gamify ledger ──────────────────────────────────────────────────────
  function readLedger() {
    return readEventsAll('gamify');
  }

  function ledgerForDate(ledger, date) {
    return ledger.filter(function (e) { return e.date === date; });
  }

  function ledgerHasLogin(ledger, date) {
    return ledgerForDate(ledger, date).some(function (e) { return e.kind === 'login'; });
  }

  // Compute XP breakdown for a date from live data.
  function computeDayBreakdown(date, events) {
    var bd = { login: 0, streak: 0, mood: 0, session: 0, habit: 0, dsa: 0, journal: 0, pushups: 0, perfect: 0 };

    if (ledgerHasLogin(events.ledger, date)) bd.login = 1;

    bd.mood = countForDate(events.mood, date);
    bd.session = countForDate(events.sessions, date, function (e) { return e.type === 'end'; });

    // Count habit completions from legacy 'habits' and current 'hbt' categories
    var h1 = countForDate(events.habits, date, function (e) { return e.on === true; });
    var h2 = countForDate(events.hbt, date, function (e) { return e.kind === 'log' && e.on === true; });
    bd.habit = h1 + h2;

    bd.dsa = countForDate(events.dsa, date, function (e) { return e.completed === true || e.completed === 'true'; });
    bd.journal = countForDate(events.journal, date) + countForDate(events.thought, date);
    bd.pushups = countForDate(events.metrics, date, function (e) { return e.pushups != null; });

    // Perfect day bonus
    bd.perfect = (bd.login > 0 && bd.session > 0 && bd.habit >= 3) ? 1 : 0;

    return bd;
  }

  function xpFromBreakdown(bd, streakDays) {
    var total = 0;
    if (bd.login > 0) total += SOURCES[0].xp * bd.login;
    if (bd.login > 0 && streakDays > 0) total += SOURCES[1].xp * streakDays;
    total += SOURCES[2].xp * bd.mood;
    total += SOURCES[3].xp * bd.session;
    total += SOURCES[4].xp * bd.habit;
    total += SOURCES[5].xp * bd.dsa;
    total += SOURCES[6].xp * bd.journal;
    total += SOURCES[7].xp * bd.pushups;
    total += SOURCES[8].xp * bd.perfect;
    return total;
  }

  function streakForDate(sortedDates, dateStr2) {
    var count = 0;
    var set = {};
    sortedDates.forEach(function (x) { set[x] = true; });
    var d = new Date(dateStr2 + 'T00:00:00');
    // If target date has no events yet today, count from yesterday
    if (!set[dateStr(d)]) {
      d.setDate(d.getDate() - 1);
    }
    while (set[dateStr(d)]) {
      count++;
      d.setDate(d.getDate() - 1);
    }
    return count;
  }

  // Recompute totals across all dates with recorded activity
  function recomputeAll(events) {
    var dateSet = {};
    if (events.ledger) events.ledger.forEach(function (e) { if (e.date && e.kind === 'login') dateSet[e.date] = true; });
    if (events.mood) events.mood.forEach(function (e) { if (e.date) dateSet[e.date] = true; });
    if (events.sessions) events.sessions.forEach(function (e) { if (e.date) dateSet[e.date] = true; });
    if (events.habits) events.habits.forEach(function (e) { if (e.date && e.on) dateSet[e.date] = true; });
    if (events.hbt) events.hbt.forEach(function (e) { if (e.date && e.on) dateSet[e.date] = true; });
    if (events.dsa) events.dsa.forEach(function (e) { if (e.date) dateSet[e.date] = true; });
    if (events.journal) events.journal.forEach(function (e) { if (e.date) dateSet[e.date] = true; });
    if (events.thought) events.thought.forEach(function (e) { if (e.date) dateSet[e.date] = true; });
    if (events.metrics) events.metrics.forEach(function (e) { if (e.date) dateSet[e.date] = true; });
    dateSet[todayStr()] = true;

    var dates = Object.keys(dateSet).sort();
    var streak = streakForDate(dates, todayStr());

    var totalXp = 0;
    var byDate = {};
    for (var i = 0; i < dates.length; i++) {
      var d = dates[i];
      var bd = computeDayBreakdown(d, events);
      var s = (d === todayStr()) ? streak : streakForDate(dates, d);
      var dayXp = xpFromBreakdown(bd, s);
      byDate[d] = { xp: dayXp, breakdown: bd };
      totalXp += dayXp;
    }
    return { total: totalXp, byDate: byDate, streak: streak, dates: dates };
  }

  // ─── Record login (first open of the day) ───────────────────────────────
  function ensureLogin() {
    var today = todayStr();
    var ledger = readLedger();
    if (!ledgerHasLogin(ledger, today)) {
      var v = getV07();
      if (v && v.writeEvent) {
        v.writeEvent('gamify', { kind: 'login', date: today, ts: new Date().toISOString() });
      }
    }
  }

  // ─── Shared API for the full page, the widget, and the FAB runner ────────
  window.v07Gamify = {
    today: todayStr,
    stats: function () {
      var events = loadAll();
      var data = recomputeAll(events);
      var today = todayStr();
      data.loggedInToday = ledgerHasLogin(events.ledger, today);
      return {
        level: progressFromXp(data.total).level,
        total: data.total,
        today: data.byDate[today] ? data.byDate[today].xp : 0,
        streak: data.streak,
        loggedInToday: data.loggedInToday,
        byDate: data.byDate
      };
    },
    checkIn: function () {
      var today = todayStr();
      var ledger = readLedger();
      var already = ledgerHasLogin(ledger, today);
      ensureLogin();
      var v = getV07();
      if (v && v.toast) {
        v.toast(already ? 'Already checked in today (+10 XP)' : 'Checked in +10 XP');
      }
      return !already;
    }
  };

  // FAB quick action (runner mode): expose a function the dashboard can call.
  window.v07QuickAdd = function () {
    v07Gamify.checkIn();
  };

  // ─── Rendering ──────────────────────────────────────────────────────────
  var root = null;

  function el(tag, cls, txt) {
    var e = document.createElement(tag);
    if (cls) e.className = cls;
    if (txt !== undefined) e.textContent = txt;
    return e;
  }

  function formatMonthDay(dStr) {
    try {
      var parts = dStr.split('-');
      var dt = new Date(parseInt(parts[0], 10), parseInt(parts[1], 10) - 1, parseInt(parts[2], 10));
      return dt.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' });
    } catch(e) {
      return dStr;
    }
  }

  function getBreakdownSummary(bd) {
    if (!bd) return 'No recorded activity';
    var parts = [];
    if (bd.login) parts.push('Login (+10)');
    if (bd.habit) parts.push(bd.habit + ' habit' + (bd.habit > 1 ? 's' : ''));
    if (bd.session) parts.push(bd.session + ' session' + (bd.session > 1 ? 's' : ''));
    if (bd.dsa) parts.push(bd.dsa + ' DSA');
    if (bd.pushups) parts.push(bd.pushups + ' pushups');
    if (bd.journal) parts.push(bd.journal + ' note' + (bd.journal > 1 ? 's' : ''));
    if (bd.mood) parts.push(bd.mood + ' mood');
    if (bd.perfect) parts.push('⭐ Perfect Day');
    return parts.length ? parts.join(', ') : 'No recorded activity';
  }

  // ─── Heatmap Renderer ───────────────────────────────────────────────────
  function renderHeatmap(data, parentEl) {
    var card = el('div', 'g-heatmap-card');

    var hHeader = el('div', 'g-heatmap-header');
    hHeader.appendChild(el('div', 'g-sec-title', 'ACTIVITY HEATMAP'));
    hHeader.appendChild(el('div', 'g-heatmap-sub', '26-WEEK CONSISTENCY'));
    card.appendChild(hHeader);

    // Detail banner for inspecting clicked day (defaults to Today)
    var today = todayStr();
    var todayEntry = data.byDate[today];
    var todayXp = todayEntry ? todayEntry.xp : 0;
    var todaySummary = todayEntry ? getBreakdownSummary(todayEntry.breakdown) : 'No activities yet today';

    var detailEl = el('div', 'g-heatmap-detail');
    detailEl.id = 'g-heatmap-detail';
    detailEl.innerHTML =
      '<div><span style="color:var(--text);font-weight:700">📅 ' + formatMonthDay(today) + ' (Today)</span><div style="font-size:9.5px;color:var(--muted);margin-top:2px">' + todaySummary + '</div></div>' +
      '<span style="color:var(--primary);font-weight:700;font-size:13px">+' + todayXp + ' XP</span>';
    card.appendChild(detailEl);

    // Heatmap Viewport (horizontal scrolling)
    var vp = el('div', 'g-heatmap-viewport');
    vp.id = 'g-heatmap-scroll';

    var wrapper = el('div', 'g-heatmap-wrapper');

    // Calculate dates: 26 weeks ending with current week
    var now = new Date();
    now.setHours(0,0,0,0);
    var dayOfWeek = (now.getDay() + 6) % 7; // 0=Mon, 6=Sun
    var thisMon = new Date(now);
    thisMon.setDate(now.getDate() - dayOfWeek);

    var startMon = new Date(thisMon);
    startMon.setDate(thisMon.getDate() - (25 * 7)); // 25 weeks prior (26 weeks total)

    // Month row
    var monthsRow = el('div', 'g-heatmap-months');
    var lastMonth = -1;
    var cur = new Date(startMon);

    var weeksData = [];
    for (var w = 0; w < 26; w++) {
      var weekDays = [];
      var colMonth = cur.getMonth();
      if (colMonth !== lastMonth) {
        lastMonth = colMonth;
        var mLbl = el('span', 'g-heatmap-month-lbl', cur.toLocaleDateString('en-US', { month: 'short' }));
        mLbl.style.left = (w * 20) + 'px'; // 16px cell + 4px gap = 20px
        monthsRow.appendChild(mLbl);
      }
      for (var d = 0; d < 7; d++) {
        var dStr = dateStr(cur);
        var isToday = (dStr === today);
        var isFuture = (cur > now);
        var dayEntry = data.byDate[dStr];
        var xp = dayEntry ? dayEntry.xp : 0;
        var bd = dayEntry ? dayEntry.breakdown : null;

        weekDays.push({
          date: dStr,
          dayOfWeek: d,
          isToday: isToday,
          isFuture: isFuture,
          xp: xp,
          breakdown: bd
        });
        cur.setDate(cur.getDate() + 1);
      }
      weeksData.push(weekDays);
    }
    wrapper.appendChild(monthsRow);

    var body = el('div', 'g-heatmap-body');
    var dayLabels = el('div', 'g-heatmap-day-labels');
    dayLabels.innerHTML = '<span>M</span><span>W</span><span>F</span><span>S</span>';
    body.appendChild(dayLabels);

    var grid = el('div', 'g-heatmap-grid');
    var activeDaysCount = 0;

    weeksData.forEach(function (week) {
      var col = el('div', 'g-heatmap-col');
      week.forEach(function (cellData) {
        var cell = el('div', 'g-h-cell');
        cell.dataset.date = cellData.date;
        cell.dataset.xp = cellData.xp;

        if (cellData.isFuture) {
          cell.classList.add('is-future');
        } else {
          if (cellData.xp > 0) activeDaysCount++;
          if (cellData.xp === 0) cell.classList.add('intensity-0');
          else if (cellData.xp <= 20) cell.classList.add('intensity-1');
          else if (cellData.xp <= 50) cell.classList.add('intensity-2');
          else if (cellData.xp <= 90) cell.classList.add('intensity-3');
          else cell.classList.add('intensity-4');

          if (cellData.isToday) cell.classList.add('is-today');

          cell.onclick = function () {
            wrapper.querySelectorAll('.g-h-cell.selected').forEach(function (c) { c.classList.remove('selected'); });
            cell.classList.add('selected');
            var dayLabel = formatMonthDay(cellData.date) + (cellData.isToday ? ' (Today)' : '');
            var summ = getBreakdownSummary(cellData.breakdown);
            detailEl.innerHTML =
              '<div><span style="color:var(--text);font-weight:700">📅 ' + dayLabel + '</span><div style="font-size:9.5px;color:var(--muted);margin-top:2px">' + summ + '</div></div>' +
              '<span style="color:var(--primary);font-weight:700;font-size:13px">+' + cellData.xp + ' XP</span>';
          };
        }
        col.appendChild(cell);
      });
      grid.appendChild(col);
    });

    body.appendChild(grid);
    wrapper.appendChild(body);
    vp.appendChild(wrapper);
    card.appendChild(vp);

    // Heatmap footer with legend
    var footer = el('div', 'g-heatmap-footer');
    var legend = el('div', 'g-heatmap-legend');
    legend.innerHTML =
      '<span>Less</span>' +
      '<span class="g-h-cell mini intensity-0"></span>' +
      '<span class="g-h-cell mini intensity-1"></span>' +
      '<span class="g-h-cell mini intensity-2"></span>' +
      '<span class="g-h-cell mini intensity-3"></span>' +
      '<span class="g-h-cell mini intensity-4"></span>' +
      '<span>More</span>';
    footer.appendChild(legend);

    var statEl = el('div', '', activeDaysCount + ' active days · ' + (data.streak > 0 ? (data.streak + 'd streak') : 'No streak'));
    footer.appendChild(statEl);
    card.appendChild(footer);

    parentEl.appendChild(card);

    // Auto-focus on present day
    setTimeout(function () {
      vp.scrollLeft = vp.scrollWidth - vp.clientWidth;
      var todayCell = vp.querySelector('.g-h-cell.is-today');
      if (todayCell) {
        todayCell.scrollIntoView({ inline: 'center', block: 'nearest', behavior: 'smooth' });
      }
    }, 60);
  }

  function render(data) {
    if (!root) root = document.getElementById('gamify-root');
    if (!root) return;
    root.innerHTML = '';

    var today = todayStr();
    var prog = progressFromXp(data.total);

    var header = el('div', 'g-header');
    var lvlRow = el('div', 'g-lvl-row');
    lvlRow.appendChild(el('div', 'g-lvl-badge', 'LVL ' + prog.level));
    lvlRow.appendChild(el('div', 'g-total', data.total.toLocaleString() + ' XP'));
    header.appendChild(lvlRow);

    var bar = el('div', 'g-bar');
    var fill = el('div', 'g-bar-fill');
    fill.style.width = Math.round(prog.pct * 100) + '%';
    bar.appendChild(fill);
    header.appendChild(bar);
    var barLbl = el('div', 'g-bar-lbl', prog.currentXp + ' / ' + prog.span + ' XP to next level');
    header.appendChild(barLbl);

    var stats = el('div', 'g-stats');
    var todayXp = data.byDate[today] ? data.byDate[today].xp : 0;
    stats.appendChild(el('div', 'g-stat g-stat-xp', todayXp + ' XP today'));
    var streakLbl = data.streak > 1 ? data.streak + ' day streak' : (data.streak === 1 ? '1 day streak' : 'No streak yet');
    stats.appendChild(el('div', 'g-stat g-stat-streak', streakLbl));
    header.appendChild(stats);

    root.appendChild(header);

    if (data.loggedInToday) {
      root.appendChild(el('div', 'g-done', 'Checked in today +10 XP'));
    } else {
      var loginBtn = el('button', 'g-btn', 'CHECK IN (+10 XP)');
      loginBtn.onclick = function () { ensureLogin(); boot(); };
      root.appendChild(loginBtn);
    }

    // ─── Render Activity Heatmap Grid (auto-focused on present day) ───────
    renderHeatmap(data, root);

    var todayBreakdown = data.byDate[today];
    if (todayBreakdown) {
      root.appendChild(el('div', 'g-sec-title', "TODAY'S XP BREAKDOWN"));
      var bdList = el('div', 'g-bd-list');
      SOURCES.forEach(function (src) {
        var count = todayBreakdown.breakdown[src.key] || 0;
        if (count <= 0) return;
        var xp = src.xp * count;
        var row = el('div', 'g-bd-row');
        row.appendChild(el('span', 'g-bd-label', src.label + ' x' + count));
        row.appendChild(el('span', 'g-bd-xp', '+' + xp + ' XP'));
        bdList.appendChild(row);
      });
      if (!bdList.children.length) bdList.appendChild(el('div', 'g-bd-empty', 'No XP earned today yet.'));
      root.appendChild(bdList);
    }

    var recent = data.dates.slice().sort().reverse().slice(0, 7);
    if (recent.length) {
      root.appendChild(el('div', 'g-sec-title', 'RECENT DAYS'));
      var hist = el('div', 'g-hist');
      recent.forEach(function (d) {
        var entry = data.byDate[d];
        var row = el('div', 'g-hist-row');
        row.appendChild(el('span', 'g-hist-date', d));
        row.appendChild(el('span', 'g-hist-xp', (entry ? entry.xp : 0) + ' XP'));
        hist.appendChild(row);
      });
      root.appendChild(hist);
    }
  }

  function loadAll() {
    return {
      ledger: readLedger(),
      mood: readEventsAll('mood'),
      sessions: readEventsAll('sessions'),
      habits: readEventsAll('habits'),
      hbt: readEventsAll('hbt'),
      dsa: readEventsAll('dsa'),
      journal: readEventsAll('journal'),
      thought: readEventsAll('thought'),
      metrics: readEventsAll('metrics')
    };
  }

  function boot() {
    var events = loadAll();
    var data = recomputeAll(events);
    data.loggedInToday = ledgerHasLogin(events.ledger, todayStr());
    render(data);
    ensureLogin();
    var events2 = loadAll();
    var data2 = recomputeAll(events2);
    data2.loggedInToday = ledgerHasLogin(events2.ledger, todayStr());
    render(data2);
  }

  // Full-page UI only: skip in runner FAB mode, and skip when no #gamify-root
  // (e.g. the compact widget loads this file just for the shared API).
  function tryBoot() {
    var v = getV07();
    if (v && v.mode === 'runner') return;
    if (!document.getElementById('gamify-root')) return;
    boot();
  }

  window.v07DailyCheckin = window.v07QuickAdd = function() {
    ensureLogin();
    var toastFn = (window.parent && window.parent.toast) || window.toast;
    if(toastFn) toastFn('Daily Gamify Check-in recorded! +10 XP');
    tryBoot();
  };

  window.addEventListener('message', function(ev) {
    if (ev.data && ev.data.type === 'v07_data_updated') {
      tryBoot();
    }
  });

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', tryBoot);
  } else {
    tryBoot();
  }
  setTimeout(tryBoot, 500);
})();