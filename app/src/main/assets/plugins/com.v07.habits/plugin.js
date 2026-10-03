// Habits plugin — Habit Matrix and Habit Streak Cards
// Data lives in 'hbt' event category:
//   kind:'def'  -> single meta event holding the habit list [{id,name,color,created}]
//   kind:'log'  -> one event per (habit, date): {habit:id, on:true/false, date}

(function () {
  var CAT = 'hbt';
  var ACCENT = '#FFB454';
  var PALETTE = ['#FFB454', '#68A6FF', '#4DE086', '#F4D35E', '#FF6F61', '#C792EA', '#FF9F45', '#63D1F4'];

  // ─── Canonical Habits Registry ───────────────────────────────────────────
  var CANONICAL_HABITS = [
    { id: 'hbt_ssb', name: 'SSB Practice', icon: '⚔️', color: '#FFB454' },
    { id: 'hbt_running', name: 'Running', icon: '🏃', color: '#4DE086' },
    { id: 'hbt_dsa', name: 'DSA Problem Solving', icon: '💻', color: '#68A6FF' },
    { id: 'hbt_chess', name: 'Chess Training', icon: '♟️', color: '#F4D35E' },
    { id: 'hbt_content_creation', name: 'Content Creation', icon: '✍️', color: '#FF6F61' },
    { id: 'hbt_development', name: 'Development', icon: '⚡', color: '#63D1F4' },
    { id: 'hbt_meditation', name: 'Meditation', icon: '🧘', color: '#C792EA' },
    { id: 'hmudx8p39', name: 'Pushups', icon: '💪', color: '#FF9F45' },
    { id: 'hmud3qf88', name: 'Mindfulness', icon: '🌿', color: '#80CBC4' },
    { id: 'hmud3qz2i', name: 'Deep Study', icon: '📚', color: '#A78BFA' },
    { id: 'hmudmiwt3', name: 'Daily Routine', icon: '✨', color: '#FBBF24' }
  ];

  var HABIT_ALIAS_MAP = {
    'ssb': 'hbt_ssb', 'ssb practice': 'hbt_ssb', 'hbt_ssb': 'hbt_ssb', 'h_ssb': 'hbt_ssb',
    'running': 'hbt_running', 'run': 'hbt_running', 'running & conditioning': 'hbt_running', 'athletics & physical conditioning': 'hbt_running', 'hbt_running': 'hbt_running', 'h_running': 'hbt_running',
    'dsa': 'hbt_dsa', 'dsa problem solving': 'hbt_dsa', 'h_dsa': 'hbt_dsa', 'hbt_dsa': 'hbt_dsa', 'algo': 'hbt_dsa', 'leetcode': 'hbt_dsa',
    'chess': 'hbt_chess', 'chess training': 'hbt_chess', 'hbt_chess': 'hbt_chess', 'h_chess': 'hbt_chess',
    'content_creation': 'hbt_content_creation', 'content creation': 'hbt_content_creation', 'hbt_content_creation': 'hbt_content_creation', 'h_content_creation': 'hbt_content_creation',
    'development': 'hbt_development', 'dev': 'hbt_development', 'hbt_development': 'hbt_development', 'h_development': 'hbt_development',
    'meditation': 'hbt_meditation', 'morning meditation & breathwork': 'hbt_meditation', 'hbt_morning_meditation': 'hbt_meditation', 'h_meditation': 'hbt_meditation', 'hbt_meditation': 'hbt_meditation',
    'pushups': 'hmudx8p39', 'pushup': 'hmudx8p39', 'hmudx8p39': 'hmudx8p39', 'hbt_pushups': 'hmudx8p39',
    'mindfulness': 'hmud3qf88', 'hmud3qf88': 'hmud3qf88', 'hbt_mindfulness': 'hmud3qf88',
    'deep study': 'hmud3qz2i', 'deep_study': 'hmud3qz2i', 'deep work focus (50m)': 'hmud3qz2i', 'hbt_deep_work': 'hmud3qz2i', 'hmud3qz2i': 'hmud3qz2i',
    'daily routine': 'hmudmiwt3', 'daily_routine': 'hmudmiwt3', 'hmudmiwt3': 'hmudmiwt3'
  };

  function normalizeHabitId(raw) {
    if (!raw) return null;
    var clean = String(raw).trim().replace(/^Habit:\s*/i, '').trim();
    var key = clean.toLowerCase();
    if (HABIT_ALIAS_MAP[key]) return HABIT_ALIAS_MAP[key];
    if (HABIT_ALIAS_MAP[key.replace(/_/g, ' ')]) return HABIT_ALIAS_MAP[key.replace(/_/g, ' ')];
    for (var i = 0; i < CANONICAL_HABITS.length; i++) {
      if (CANONICAL_HABITS[i].id === clean || CANONICAL_HABITS[i].name.toLowerCase() === key) {
        return CANONICAL_HABITS[i].id;
      }
    }
    if (key === 'bhagavad gita contemplation' || key === 'digital sunset & mindful stillness' || key === 'hbt_gita' || key === 'hbt_digital_sunset') {
      return null;
    }
    return clean;
  }

  var DEFAULT_HABITS = CANONICAL_HABITS;

  // ─── Date helpers ────────────────────────────────────────────────────────
  function pad(n) { return ('0' + n).slice(-2); }
  function todayStr() {
    var d = new Date();
    return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate());
  }
  function dateStr(d) {
    return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate());
  }
  function parseDate(s) { return new Date(s + 'T00:00:00'); }
  function addDays(d, n) { var x = new Date(d.getTime()); x.setDate(x.getDate() + n); return x; }
  function mondayBefore(d) { return addDays(parseDate(dateStr(d)), -((parseDate(dateStr(d)).getDay() + 6) % 7)); }

  // ─── Bridge access ───────────────────────────────────────────────────────
  function getV07() {
    if (typeof window.V07 !== 'undefined' && window.V07) return window.V07;
    if (window.parent && typeof window.parent.V07 !== 'undefined' && window.parent.V07) return window.parent.V07;
    return null;
  }

  function readAll() {
    var v = getV07();
    if (v && v.readEventsAll) {
      var r = v.readEventsAll(CAT);
      if (r && r.events) {
        try { localStorage.setItem('v07_hbt_events', JSON.stringify(r.events)); } catch(e){}
        return r.events;
      }
    }
    try {
      var s = localStorage.getItem('v07_hbt_events');
      if (s) return JSON.parse(s);
    } catch(e){}
    return [];
  }

  function writeEvent(data) {
    var v = getV07();
    if (v && v.writeEvent) {
      var res = v.writeEvent(CAT, data);
      if (res && (res.ok || res.event)) return res;
    }
    try {
      var evs = readAll();
      var ev = Object.assign({ id: 'h_' + Date.now().toString(36), date: todayStr(), ts: new Date().toISOString() }, data);
      evs.push(ev);
      localStorage.setItem('v07_hbt_events', JSON.stringify(evs));
      return { ok: true, event: ev };
    } catch(e){}
    return null;
  }

  function updateEvent(id, fields) {
    var v = getV07();
    if (v && v.updateEvent) {
      v.updateEvent(CAT, id, fields);
    }
    try {
      var evs = readAll();
      evs.forEach(function (e) {
        if (e.id === id) Object.assign(e, fields);
      });
      localStorage.setItem('v07_hbt_events', JSON.stringify(evs));
      return { ok: true };
    } catch(e){}
    return null;
  }

  function showToast(msg) {
    var v = getV07();
    if (v && v.toast) v.toast(msg);
    else console.log(msg);
  }

  function findDefEvent(events) {
    var defs = events.filter(function (e) { return e.kind === 'def'; });
    if (!defs.length) return null;
    return defs.sort(function (a, b) { return (a.ts || '').localeCompare(b.ts || ''); }).pop();
  }

  // ─── State ───────────────────────────────────────────────────────────────
  var _defId = null;
  var _habits = [];
  var _logs = [];
  var _loaded = false;

  function loadState() {
    _loaded = false;
    var events = readAll();
    var def = findDefEvent(events);
    _defId = def ? def.id : null;

    var deletedHabits = [];
    try { deletedHabits = JSON.parse(localStorage.getItem('v07_deleted_habits') || '[]'); } catch(e){}

    var rawDefs = (def && Array.isArray(def.habits) && def.habits.length > 0) ? def.habits : CANONICAL_HABITS;

    // Deduplicate and canonicalize habits list
    var habitMap = new Map();
    CANONICAL_HABITS.forEach(function(ch) {
      if (deletedHabits.indexOf(ch.id) === -1) {
        habitMap.set(ch.id, Object.assign({}, ch));
      }
    });

    rawDefs.forEach(function(h) {
      if (!h) return;
      var nid = normalizeHabitId(h.id || h.name);
      if (!nid) return; // dropped dummy habits
      if (deletedHabits.indexOf(nid) !== -1) return; // deleted habit

      var canon = CANONICAL_HABITS.find(function(c) { return c.id === nid; });
      if (canon) {
        habitMap.set(nid, Object.assign({}, canon, h, { id: canon.id, name: canon.name }));
      } else if (!habitMap.has(nid)) {
        habitMap.set(nid, {
          id: nid,
          name: h.name || nid,
          color: h.color || PALETTE[habitMap.size % PALETTE.length],
          created: h.created || todayStr()
        });
      }
    });

    _habits = Array.from(habitMap.values());

    // Map all habit identifiers for log matching
    var ids = {};
    _habits.forEach(function (h) {
      ids[h.id] = h.id;
      if (h.name) ids[h.name.toLowerCase()] = h.id;
      var clean = h.id.replace('hbt_', '').replace('h_', '');
      ids[clean] = h.id;
    });

    // Deduplicate logs strictly by canonical habit id + date
    var seenLogs = new Set();
    var dedupedLogs = [];

    events.forEach(function (e) {
      if (e.kind !== 'log' || !e.habit || !e.date) return;
      var nid = normalizeHabitId(e.habit) || ids[e.habit] || ids[e.habit.toLowerCase()];
      if (nid && habitMap.has(nid)) {
        var key = nid + '_' + e.date;
        if (!seenLogs.has(key)) {
          seenLogs.add(key);
          var cloned = Object.assign({}, e, { habit: nid });
          dedupedLogs.push(cloned);
        }
      }
    });

    _logs = dedupedLogs;
    _loaded = true;
  }

  function saveDefs() {
    if (_defId) {
      updateEvent(_defId, { habits: _habits, date: todayStr() });
    } else {
      writeEvent({ kind: 'def', habits: _habits, date: todayStr(), ts: new Date().toISOString() });
      var def = findDefEvent(readAll());
      _defId = def ? def.id : null;
    }
  }

  function logFor(habitId, date) {
    for (var i = 0; i < _logs.length; i++) {
      if (_logs[i].habit === habitId && _logs[i].date === date) return _logs[i];
    }
    return null;
  }

  function setOnForDate(habitId, date, on) {
    if (!_loaded) loadState();
    var e = logFor(habitId, date);
    if (e) {
      if (e.on !== on) updateEvent(e.id, { on: on, date: date });
    } else if (on) {
      writeEvent({ kind: 'log', habit: habitId, date: date, on: true, ts: new Date().toISOString() });
    }
    loadState();
  }

  // ─── Icon inference ──────────────────────────────────────────────────────
  function getHabitIcon(name) {
    if(!name) return '⭐';
    var trim = name.trim();
    var emojiRegex = /^(\u00a9|\u00ae|[\u2000-\u3300]|\ud83c[\ud000-\udfff]|\ud83d[\ud000-\udfff]|\ud83e[\ud000-\udfff])/;
    var m = trim.match(emojiRegex);
    if(m) return m[0];
    var low = trim.toLowerCase();
    if(low.indexOf('ssb') !== -1) return '⚔️';
    if(low.indexOf('run') !== -1 || low.indexOf('jog') !== -1) return '🏃';
    if(low.indexOf('dsa') !== -1 || low.indexOf('algo') !== -1 || low.indexOf('leet') !== -1) return '💻';
    if(low.indexOf('chess') !== -1) return '♟️';
    if(low.indexOf('content') !== -1) return '✍️';
    if(low.indexOf('dev') !== -1 || low.indexOf('code') !== -1 || low.indexOf('program') !== -1) return '⚡';
    if(low.indexOf('meditat') !== -1 || low.indexOf('zen') !== -1) return '🧘';
    if(low.indexOf('pushup') !== -1 || low.indexOf('workout') !== -1 || low.indexOf('gym') !== -1) return '💪';
    if(low.indexOf('mindful') !== -1 || low.indexOf('spa') !== -1) return '🌿';
    if(low.indexOf('deep study') !== -1 || low.indexOf('study') !== -1 || low.indexOf('focus') !== -1) return '📚';
    if(low.indexOf('routine') !== -1) return '✨';
    if(low.indexOf('read') !== -1 || low.indexOf('book') !== -1) return '📖';
    if(low.indexOf('water') !== -1 || low.indexOf('hydrat') !== -1) return '💧';
    if(low.indexOf('sleep') !== -1 || low.indexOf('wake') !== -1) return '😴';
    if(low.indexOf('walk') !== -1 || low.indexOf('step') !== -1) return '🚶';
    if(low.indexOf('journal') !== -1 || low.indexOf('write') !== -1) return '✍️';
    return '🔥';
  }

  // ─── Per-habit Streak & Consistency Calculations ─────────────────────────
  function habitStreak(habitId) {
    var logsMap = {};
    _logs.forEach(function(e){ if(e.habit === habitId && e.on) logsMap[e.date] = true; });
    var d = todayStr();
    if(!logsMap[d]) d = dateStr(addDays(parseDate(d), -1));
    var s = 0;
    while(logsMap[d]){ s++; d = dateStr(addDays(parseDate(d), -1)); }
    return s;
  }

  function habitBestStreak(habitId) {
    var dates = [];
    _logs.forEach(function(e){ if(e.habit === habitId && e.on && e.date) dates.push(e.date); });
    dates = Array.from(new Set(dates)).sort();
    var best = 0, run = 0, prev = null;
    dates.forEach(function(k){
      var prevDay = dateStr(addDays(parseDate(k), -1));
      run = (prev && prev === prevDay) ? run + 1 : 1;
      if(run > best) best = run;
      prev = k;
    });
    return Math.max(best, habitStreak(habitId));
  }

  function habit30DayStats(habitId) {
    var logsMap = {};
    _logs.forEach(function(e){ if(e.habit === habitId && e.on) logsMap[e.date] = true; });
    var t = todayStr();
    var done = 0;
    for(var i = 0; i < 30; i++){
      var day = dateStr(addDays(parseDate(t), -i));
      if(logsMap[day]) done++;
    }
    var pct = Math.round((done / 30) * 100);
    return { done: done, total: 30, pct: pct };
  }

  // ─── Current Week Matrix Days ────────────────────────────────────────────
  // Returns array of 7 day objects: [Mon..Sun] for current week
  function currentWeekDays() {
    var today = todayStr();
    var start = mondayBefore(parseDate(today));
    var days = [];
    var names = ['M', 'T', 'W', 'T', 'F', 'S', 'S'];
    for(var i = 0; i < 7; i++){
      var d = addDays(start, i);
      var ds = dateStr(d);
      days.push({
        name: names[i],
        date: ds,
        isToday: ds === today,
        isFuture: ds > today
      });
    }
    return days;
  }

  // ─── Aggregates ──────────────────────────────────────────────────────────
  function countsByDate() {
    var per = {};
    _logs.forEach(function (e) {
      if (e.on && e.date && e.habit) {
        if (!per[e.date]) per[e.date] = {};
        per[e.date][e.habit] = true;
      }
    });
    var out = {};
    Object.keys(per).forEach(function (k) { out[k] = Object.keys(per[k]).length; });
    return out;
  }

  function overallCurrentStreak() {
    var habits = _habits;
    if (!habits.length) return 0;
    var counts = countsByDate();
    var d = todayStr();
    var isPerf = function(date){ return (counts[date] || 0) >= habits.length; };
    if (!isPerf(d)) d = dateStr(addDays(parseDate(d), -1));
    var s = 0;
    while (isPerf(d)) { s++; d = dateStr(addDays(parseDate(d), -1)); }
    return s;
  }

  function overallBestStreak() {
    var habits = _habits;
    if (!habits.length) return 0;
    var counts = countsByDate();
    var isPerf = function(date){ return (counts[date] || 0) >= habits.length; };
    var perfect = {};
    Object.keys(counts).forEach(function (k) { if (isPerf(k)) perfect[k] = true; });
    var dates = Object.keys(perfect).sort();
    var best = 0, run = 0, prev = null;
    dates.forEach(function (k) {
      var prevDay = dateStr(addDays(parseDate(k), -1));
      run = (prev && prev === prevDay) ? run + 1 : 1;
      if (run > best) best = run;
      prev = k;
    });
    return Math.max(best, overallCurrentStreak());
  }

  // ─── Mutations ───────────────────────────────────────────────────────────
  function addHabit(name) {
    if (!name || !name.trim()) return false;
    if (!_loaded) loadState();
    try { localStorage.setItem('v07_user_created_habits', 'true'); } catch(e){}
    _habits.push({
      id: 'h' + Date.now().toString(36),
      name: name.trim(),
      color: PALETTE[_habits.length % PALETTE.length],
      created: todayStr()
    });
    saveDefs();
    return true;
  }

  function removeHabit(id) {
    if (!_loaded) loadState();
    try {
      var del = JSON.parse(localStorage.getItem('v07_deleted_habits') || '[]');
      if (del.indexOf(id) === -1) { del.push(id); localStorage.setItem('v07_deleted_habits', JSON.stringify(del)); }
      localStorage.setItem('v07_user_created_habits', 'true');
    } catch(e){}
    _habits = _habits.filter(function (h) { return h.id !== id; });
    saveDefs();
    loadState();
  }

  function logAll() {
    if (!_loaded) loadState();
    if (!_habits.length) { showToast('No habits yet — add one below'); return false; }
    _habits.forEach(function (h) { setOnForDate(h.id, todayStr(), true); });
    showToast('All habits logged (' + _habits.length + ' today)');
    return true;
  }

  // ─── Shared API ──────────────────────────────────────────────────────────
  window.v07Habits = {
    today: todayStr,
    reload: loadState,
    habits: function () { loadState(); return _habits.slice(); },
    getHabits: function () { loadState(); return _habits.slice(); },
    logs: function () { loadState(); return _logs.slice(); },
    getLogs: function () { loadState(); return _logs.slice(); },
    isDoneOnDate: function(habitId, date) {
      loadState();
      var e = logFor(habitId, date);
      return !!(e && e.on);
    },
    stats: function () {
      loadState();
      var counts = countsByDate();
      var t = todayStr();
      return {
        habits: _habits.length,
        todayDone: counts[t] || 0,
        todayTotal: _habits.length,
        current: overallCurrentStreak(),
        best: overallBestStreak(),
        logs: _logs.length
      };
    },
    setOn: function (habitId, on) { setOnForDate(habitId, todayStr(), !!on); },
    setOnForDate: setOnForDate,
    addHabit: addHabit,
    removeHabit: removeHabit,
    logAll: logAll,
    habitStreak: habitStreak,
    habitBestStreak: habitBestStreak,
    habit30DayStats: habit30DayStats,
    getHabitIcon: getHabitIcon,
    currentWeekDays: currentWeekDays,
    accent: ACCENT
  };

  // FAB quick action
  window.v07LogAll = function () { v07Habits.logAll(); };

  // ─── Full-page UI ────────────────────────────────────────────────────────
  var root = null;

  function render() {
    if (!root) root = document.getElementById('hbt-root');
    if (!root) return;
    loadState();
    var s = v07Habits.stats();
    var today = todayStr();
    var week = currentWeekDays();
    var habits = _habits;

    var html = '';

    // ── Header ──
    html += '<div class="h-header">';
    html += '  <div class="h-title">HABITS</div>';
    html += '  <div class="h-chips">';
    html += '    <span class="h-chip">🔥 ' + s.todayDone + '/' + s.todayTotal + ' TODAY</span>';
    html += '    <span class="h-chip h-chip-dim">' + habits.length + ' ACTIVE</span>';
    html += '  </div>';
    html += '</div>';

    // ── 1. HABIT MATRIX ──
    html += '<div class="h-card">';
    html += '  <div class="h-sec">';
    html += '    <span>1. HABIT MATRIX</span>';
    html += '    <button class="h-btn h-btn-accent" onclick="v07Habits.logAll();v07Habits.render();" style="padding:3px 8px;font-size:9px">LOG ALL TODAY</button>';
    html += '  </div>';
    html += '  <div class="h-matrix-wrap">';
    html += '    <table class="h-matrix-table">';
    html += '      <thead><tr>';
    html += '        <th class="h-matrix-th" style="text-align:left;padding-left:8px;font-size:9.5px">HABIT</th>';
    week.forEach(function(day){
      html += '      <th class="h-matrix-th ' + (day.isToday ? 'today-th' : '') + '">';
      html += '        <div>' + day.name + '</div>';
      html += '        <div style="font-size:7.5px;font-weight:400;opacity:0.7">' + day.date.slice(8) + '</div>';
      html += '      </th>';
    });
    html += '      </tr></thead>';
    html += '      <tbody>';

    if(!habits.length){
      html += '<tr><td colspan="8" class="h-empty" style="padding:28px 8px;text-align:center"><div style="font-size:18px;margin-bottom:6px">🌱</div><div style="font-weight:700;font-size:12px;color:var(--text);margin-bottom:3px">No Habits Configured</div><div style="font-size:10px;color:var(--muted)">Tap "+ NEW HABIT" below to start tracking.</div></td></tr>';
    } else {
      habits.forEach(function(h){
        var icon = getHabitIcon(h.name);
        html += '<tr class="h-matrix-row">';
        html += '  <td class="h-matrix-label-td">';
        html += '    <div class="h-matrix-label" title="' + h.name + '">';
        html += '      <span class="h-matrix-icon">' + icon + '</span>';
        html += '      <span>' + h.name + '</span>';
        html += '    </div>';
        html += '  </td>';

        week.forEach(function(day){
          var isDone = false;
          for(var li=0; li<_logs.length; li++){
            if(_logs[li].habit === h.id && _logs[li].date === day.date){
              isDone = _logs[li].on === true;
              break;
            }
          }
          var cellClass = isDone ? 'h-block-filled' : 'h-block-empty';
          var ringClass = day.isToday ? ' h-block-today-ring' : '';
          var blockChar = isDone ? '▰' : '·';
          var onclick = day.isFuture ? '' : 'onclick="v07Habits.setOnForDate(\'' + h.id + '\',\'' + day.date + '\',' + (!isDone) + ');v07Habits.render();"';
          var title = h.name + ' on ' + day.date + (isDone ? ' (Done)' : ' (Not done)');

          html += '<td class="h-matrix-cell ' + (day.isToday ? 'today-col' : '') + '" ' + onclick + ' title="' + title + '">';
          html += '  <div class="h-block-cell ' + cellClass + ringClass + '" style="' + (isDone ? 'background:' + h.color + ';border-color:' + h.color + ';' : '') + '">';
          html +=      blockChar;
          html += '  </div>';
          html += '</td>';
        });
        html += '</tr>';
      });
    }

    html += '      </tbody>';
    html += '    </table>';
    html += '  </div>';
    html += '</div>';

    // ── 2. 🔥 HABIT STREAK CARDS ──
    html += '<div class="h-card">';
    html += '  <div class="h-sec">';
    html += '    <span>2. 🔥 HABIT STREAK CARDS</span>';
    html += '    <span style="font-size:9.5px;color:var(--muted)">' + habits.length + ' HABITS</span>';
    html += '  </div>';
    html += '  <div class="h-streaks-grid">';

    if(!habits.length){
      html += '<div style="grid-column:1/-1;padding:24px 12px;text-align:center;color:var(--muted);font-size:11px;border:1px dashed var(--border);border-radius:8px">No streak cards yet. Add a habit below!</div>';
    }

    habits.forEach(function(h){
      var curStreak = habitStreak(h.id);
      var bStreak = habitBestStreak(h.id);
      var stats30 = habit30DayStats(h.id);
      var icon = getHabitIcon(h.name);

      var isDoneToday = false;
      for(var li=0; li<_logs.length; li++){
        if(_logs[li].habit === h.id && _logs[li].date === today){
          isDoneToday = _logs[li].on === true;
          break;
        }
      }

      html += '<div class="h-streak-card ' + (isDoneToday ? 'done-today' : '') + '" style="border-left:3px solid ' + h.color + '">';
      html += '  <div class="h-streak-head">';
      html += '    <div class="h-streak-title">';
      html += '      <span>' + icon + '</span>';
      html += '      <span>' + h.name + '</span>';
      html += '    </div>';
      html += '    <span class="h-del" onclick="event.stopPropagation();v07Habits.confirmDelete(\'' + h.id + '\',\'' + h.name.replace(/['"\\]/g,'') + '\')" title="Delete">✕</span>';
      html += '  </div>';

      html += '  <div class="h-streak-center" onclick="v07Habits.setOnForDate(\'' + h.id + '\',\'' + today + '\',' + (!isDoneToday) + ');v07Habits.render();">';
      html += '    <div class="h-streak-fire-num" style="color:' + h.color + '">';
      html += '      <span>🔥</span><span>' + curStreak + '</span>';
      html += '    </div>';
      html += '    <div class="h-streak-fire-label">DAY STREAK</div>';
      html += '  </div>';

      html += '  <div class="h-streak-footer">';
      html += '    <div class="h-streak-meta-row">';
      html += '      <span class="h-streak-best">Best: ' + bStreak + ' days</span>';
      html += '      <span class="h-streak-ratio" style="color:' + h.color + '">' + stats30.done + '/30 days ' + stats30.pct + '%</span>';
      html += '    </div>';
      html += '    <div class="h-streak-bar-track">';
      html += '      <div class="h-streak-bar-fill" style="width:' + stats30.pct + '%;background:' + h.color + '"></div>';
      html += '    </div>';
      html += '    <button class="h-streak-check-btn ' + (isDoneToday ? 'checked' : '') + '" onclick="v07Habits.setOnForDate(\'' + h.id + '\',\'' + today + '\',' + (!isDoneToday) + ');v07Habits.render();">';
      html +=        isDoneToday ? '✓ DONE TODAY' : '+ MARK DONE';
      html += '    </button>';
      html += '  </div>';
      html += '</div>';
    });

    html += '  </div>';
    html += '</div>';

    // ── Manage / Add Habit ──
    html += '<div class="h-card">';
    html += '  <div class="h-sec">ADD NEW HABIT</div>';
    html += '  <div class="h-add-row">';
    html += '    <input type="text" id="newHabitInput" class="h-input" placeholder="e.g. 🧠 DSA, 🏃 Running, 📖 Reading..." maxlength="40" onkeydown="if(event.key===\'Enter\')addNewHabitFromInput()">';
    html += '    <button class="h-btn h-btn-accent" onclick="addNewHabitFromInput()" style="padding:6px 14px">+ ADD</button>';
    html += '  </div>';
    html += '</div>';

    root.innerHTML = html;
  }

  window.addNewHabitFromInput = function(){
    var inp = document.getElementById('newHabitInput');
    if(!inp) return;
    var v = (inp.value || '').trim();
    if(v){
      if(v07Habits.addHabit(v)){
        showToast('Habit "' + v + '" added');
        inp.value = '';
        render();
      }
    } else {
      showToast('Enter a habit name');
    }
  };

  // confirmDelete: uses host page custom modal if available, otherwise native confirm
  v07Habits.confirmDelete = function(habitId, habitName) {
    var doDelete = function() {
      v07Habits.removeHabit(habitId);
      v07Habits.render();
    };
    // Try host page modal (works when loaded inline in V3 WebView)
    var host = (typeof showCustomModal !== 'undefined') ? showCustomModal : (window.parent && typeof window.parent.showCustomModal !== 'undefined' ? window.parent.showCustomModal : null);
    if (host) {
      host({
        title: 'Delete Habit?',
        subtitle: habitName,
        icon: 'delete',
        iconBg: 'bg-error/15 text-error',
        bodyHtml: '<p class="text-xs text-on-surface-variant">This will remove <strong>' + habitName + '</strong> and all its streak history permanently.</p>',
        confirmText: 'Delete',
        cancelText: 'Keep',
        isDestructive: true,
        onConfirm: doDelete
      });
    } else {
      // Fallback: Android native AlertDialog via V3Activity onJsConfirm
      if (confirm('Delete ' + habitName + '?')) doDelete();
    }
  };

  v07Habits.render = render;

  function boot() {
    loadState();
    render();
  }

  function tryBoot() {
    loadState();
    var v = getV07();
    if (v && v.mode === 'runner') return;
    if (!document.getElementById('hbt-root')) return;
    boot();
  }

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