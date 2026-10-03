// ─── Modular Core Widgets (Gamify & Plugin-Style Cyberpunk Cards) ─────────────

var WidgetRegistry = {
  _widgets: {},

  register: function(id, spec){
    this._widgets[id] = spec;
  },

  get: function(id){
    return this._widgets[id];
  },

  getAll: function(){
    var arr = [];
    for(var k in this._widgets){
      if(this._widgets.hasOwnProperty(k)){
        arr.push(this._widgets[k]);
      }
    }
    return arr;
  }
};

// Helper to generate consistent Gamify-style widget header
function _widgetHeaderHtml(title, color, iconSvg, openAction, countBadge){
  var h = '<div class="widget-header-bar" onclick="' + openAction + '">';
  h += '  <div style="display:flex;align-items:center;gap:6px;min-width:0;flex:1">';
  h += '    <span style="display:flex;align-items:center;color:' + color + ';flex-shrink:0">' + iconSvg + '</span>';
  h += '    <span style="font-family:\'Space Mono\',monospace;font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:0.08em;color:' + color + ';overflow:hidden;text-overflow:ellipsis;white-space:nowrap">' + title + '</span>';
  if(countBadge){
    h += '    <span class="widget-chip-badge" style="color:' + color + ';border-color:' + color + '30;background:' + color + '15">' + countBadge + '</span>';
  }
  h += '  </div>';
  h += '  <div style="display:flex;align-items:center;gap:6px;flex-shrink:0">';
  h += '    <span style="display:inline-flex;align-items:center;gap:2px;font-family:\'Space Mono\',monospace;font-size:9px;font-weight:600;color:var(--text-inactive);letter-spacing:0.06em">OPEN <svg viewBox="0 -960 960 960" width="12" height="12" fill="currentColor" style="display:block"><path d="m321-80-71-71 329-329-329-329 71-71 400 400L321-80Z"/></svg></span>';
  h += '  </div>';
  h += '</div>';
  return h;
}

// ─── 0. GAMIFY WIDGET ───────────────────────────────────────────────────────
WidgetRegistry.register('gamify', {
  id: 'gamify',
  title: 'Gamify',
  icon: 'military_tech',
  color: '#4DE086',
  category: 'gamification',
  supportedSizes: ['1*1', '2*1', '4*2'],
  defaultSize: '4*2',
  description: 'XP, level progression, daily quest bonuses, and rank.',

  render: function(container, size, data){
    var s = (window.v07Gamify && window.v07Gamify.stats) ? window.v07Gamify.stats() : { level: 1, total: 55, today: 0, streak: 0 };
    var lvl = s.level || 1;
    var total = s.total || 0;
    var today = s.today || 0;
    var streak = s.streak || 0;
    var rank = (lvl >= 8) ? 'ARCHITECT' : (lvl >= 7) ? 'ELITE' : (lvl >= 6) ? 'MASTER' : (lvl >= 5) ? 'EXPERT' : (lvl >= 4) ? 'SPECIALIST' : (lvl >= 3) ? 'OPERATOR' : (lvl >= 2) ? 'APPRENTICE' : 'NOVICE';
    var progress = Math.min(100, Math.round(((total % 100) / 100) * 100)) || 20;
    var toNext = 100 - (total % 100);
    var iconSvg = '<svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M19 5h-2V3H7v2H5c-1.1 0-2 .9-2 2v1c0 2.55 1.92 4.63 4.39 4.94A5.01 5.01 0 0 0 11 15.9V19H7v2h10v-2h-4v-3.1a5.01 5.01 0 0 0 3.61-2.96C19.08 12.63 21 10.55 21 8V7c0-1.1-.9-2-2-2zM5 8V7h2v3.82C5.84 10.4 5 9.3 5 8zm14 0c0 1.3-.84 2.4-2 2.82V7h2v1z"/></svg>';

    if(size === '1*1'){
      container.innerHTML =
        '<div class="widget-gamify-card widget-clickable" onclick="openPluginPage(\'com.v07.gamify\')">' +
        '  <div class="gw-row1">' +
        '    <span class="gw-badge" style="color:#4DE086;background:#4DE08615;border-color:#4DE08630">LVL ' + lvl + '</span>' +
        '    <span class="gw-rank">' + rank + '</span>' +
        '  </div>' +
        '  <div class="widget-stat-center" style="margin:4px 0">' +
        '    <span style="font-size:18px;font-family:\'JetBrains Mono\',monospace;font-weight:700;color:var(--text)">' + total + '</span>' +
        '    <span style="font-size:8px;color:var(--text-inactive);text-transform:uppercase">TOTAL XP</span>' +
        '  </div>' +
        '  <div class="gw-bar-track"><div class="gw-bar-fill" style="width:' + progress + '%;background:#4DE086;box-shadow:0 0 6px #4DE08680"></div></div>' +
        '</div>';
    } else if(size === '2*1'){
      container.innerHTML =
        '<div class="widget-gamify-card widget-clickable" onclick="openPluginPage(\'com.v07.gamify\')">' +
        _widgetHeaderHtml('GAMIFY', '#4DE086', iconSvg, 'openPluginPage(\'com.v07.gamify\')', 'LVL ' + lvl) +
        '  <div class="widget-card-body">' +
        '    <div class="gw-row1">' +
        '      <div class="gw-lvl-group">' +
        '        <span class="gw-badge" style="color:#4DE086;background:#4DE08615;border-color:#4DE08630">LVL ' + lvl + '</span>' +
        '        <span class="gw-rank">' + rank + '</span>' +
        '      </div>' +
        '      <span class="gw-total">' + total.toLocaleString() + ' <span style="font-size:9px;color:#8A8F98;font-weight:400">XP</span></span>' +
        '    </div>' +
        '    <div class="gw-bar-track"><div class="gw-bar-fill" style="width:' + progress + '%;background:#4DE086;box-shadow:0 0 6px #4DE08680"></div></div>' +
        '    <div class="gw-chips">' +
        '      <span class="gw-chip"><span class="gw-dot ' + (today > 0 ? 'on' : 'off') + '"></span> TODAY <span class="gw-chip-val">+' + today + ' XP</span></span>' +
        '      <span class="gw-chip">STREAK <span class="gw-chip-val">' + streak + 'd</span></span>' +
        '    </div>' +
        '  </div>' +
        '</div>';
    } else { // 4*2
      container.innerHTML =
        '<div class="widget-gamify-card">' +
        _widgetHeaderHtml('GAMIFY', '#4DE086', iconSvg, 'openPluginPage(\'com.v07.gamify\')', 'LVL ' + lvl) +
        '  <div class="widget-card-body" onclick="openPluginPage(\'com.v07.gamify\')">' +
        '    <div class="gw-row1">' +
        '      <div class="gw-lvl-group">' +
        '        <span class="gw-badge" style="color:#4DE086;background:#4DE08615;border-color:#4DE08630">LVL ' + lvl + '</span>' +
        '        <span class="gw-rank">' + rank + '</span>' +
        '      </div>' +
        '      <span class="gw-total">' + total.toLocaleString() + ' <span style="font-size:9px;color:#8A8F98;font-weight:400">XP</span></span>' +
        '    </div>' +
        '    <div class="gw-bar-track"><div class="gw-bar-fill" style="width:' + progress + '%;background:#4DE086;box-shadow:0 0 6px #4DE08680"></div></div>' +
        '    <div class="gw-chips">' +
        '      <span class="gw-chip"><span class="gw-dot ' + (today > 0 ? 'on' : 'off') + '"></span> TODAY <span class="gw-chip-val">+' + today + ' XP</span></span>' +
        '      <span class="gw-chip">STREAK <span class="gw-chip-val">' + streak + 'd</span></span>' +
        '      <span class="gw-chip">NEXT <span class="gw-chip-val">' + toNext + ' XP</span></span>' +
        '    </div>' +
        '  </div>' +
        '</div>';
    }
  }
});

// ─── 1. TASKS WIDGET ────────────────────────────────────────────────────────
WidgetRegistry.register('tasks', {
  id: 'tasks',
  title: 'Tasks',
  icon: 'checklist',
  color: '#A7C8FF',
  category: 'productivity',
  supportedSizes: ['1*1', '2*1', '4*2'],
  defaultSize: '4*2',
  description: 'Daily tasks, progress bar, completion chips, and priority tracker.',

  render: function(container, size, data){
    var tasks = (data && data.tasks) || [];
    var doneCount = tasks.filter(function(t){ return t.status === 'done'; }).length;
    var totalCount = tasks.length;
    var pct = totalCount > 0 ? Math.round((doneCount / totalCount) * 100) : 0;
    var iconSvg = '<svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-9 14l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>';

    if(size === '1*1'){
      container.innerHTML =
        '<div class="widget-gamify-card widget-clickable" onclick="openTasksPage(\'all\')">' +
        '  <div class="gw-row1">' +
        '    <span class="gw-badge" style="color:#A7C8FF;background:#A7C8FF15;border-color:#A7C8FF30">TASKS</span>' +
        '    <span class="gw-rank">' + doneCount + '/' + totalCount + '</span>' +
        '  </div>' +
        '  <div class="widget-stat-center" style="margin:4px 0">' +
        '    <span style="font-size:18px;font-family:\'JetBrains Mono\',monospace;font-weight:700;color:' + (pct===100?'var(--green)':'var(--text)') + '">' + pct + '%</span>' +
        '    <span style="font-size:8px;color:var(--text-inactive);text-transform:uppercase">DONE</span>' +
        '  </div>' +
        '  <div class="gw-bar-track"><div class="gw-bar-fill" style="width:' + pct + '%;background:#A7C8FF;box-shadow:0 0 6px #A7C8FF80"></div></div>' +
        '</div>';
    } else if(size === '2*1'){
      container.innerHTML =
        '<div class="widget-gamify-card widget-clickable" onclick="openTasksPage(\'all\')">' +
        _widgetHeaderHtml('TASKS', '#A7C8FF', iconSvg, 'openTasksPage(\'all\')', doneCount + '/' + totalCount) +
        '  <div class="widget-card-body">' +
        '    <div class="gw-row1">' +
        '      <div class="gw-lvl-group">' +
        '        <span class="gw-badge" style="color:#A7C8FF;background:#A7C8FF15;border-color:#A7C8FF30">TASKS</span>' +
        '        <span class="gw-rank">' + doneCount + '/' + totalCount + ' COMPLETED</span>' +
        '      </div>' +
        '      <span class="gw-total">' + pct + '%</span>' +
        '    </div>' +
        '    <div class="gw-bar-track"><div class="gw-bar-fill" style="width:' + pct + '%;background:#A7C8FF;box-shadow:0 0 6px #A7C8FF80"></div></div>' +
        '    <div class="gw-chips">' +
        '      <span class="gw-chip"><span class="gw-dot ' + (doneCount>0?'on':'off') + '" style="background:#A7C8FF;box-shadow:0 0 5px #A7C8FF"></span> DONE <span class="gw-chip-val">' + doneCount + '</span></span>' +
        '      <span class="gw-chip">PENDING <span class="gw-chip-val">' + (totalCount - doneCount) + '</span></span>' +
        '    </div>' +
        '  </div>' +
        '</div>';
    } else { // 4*2
      var rowsHtml = tasks.length === 0 ? '<div style="color:var(--text-inactive);font-size:12px;padding:4px 0">No tasks due today</div>' :
        tasks.slice(0, 3).map(function(t){
          var isDone = t.status === 'done';
          return '<div class="widget-compact-row">' +
                 '  <span class="task-toggle-mini ' + (isDone ? 'ok' : '') + '" onclick="event.stopPropagation();toggleTask(\'' + t.id + '\')">' + (isDone ? '✓' : '') + '</span>' +
                 '  <span class="widget-row-text ' + (isDone ? 'task-done' : '') + '">' + esc(t.title) + (t.deadline_time ? ' @' + t.deadline_time : '') + '</span>' +
                 '  <span style="font-size:10px;color:var(--text-inactive);flex-shrink:0">' + esc(t.deadline || '') + '</span>' +
                 '</div>';
        }).join('');

      container.innerHTML =
        '<div class="widget-gamify-card">' +
        _widgetHeaderHtml('TASKS', '#A7C8FF', iconSvg, 'openTasksPage(\'all\')', doneCount + '/' + totalCount) +
        '  <div class="widget-card-body" onclick="openTasksPage(\'all\')">' +
        '    <div class="gw-row1">' +
        '      <div class="gw-lvl-group">' +
        '        <span class="gw-badge" style="color:#A7C8FF;background:#A7C8FF15;border-color:#A7C8FF30">' + (pct===100?'ALL DONE':'PROGRESS') + '</span>' +
        '        <span class="gw-rank">' + doneCount + ' OF ' + totalCount + ' COMPLETED</span>' +
        '      </div>' +
        '      <span class="gw-total">' + pct + '%</span>' +
        '    </div>' +
        '    <div class="gw-bar-track"><div class="gw-bar-fill" style="width:' + pct + '%;background:#A7C8FF;box-shadow:0 0 6px #A7C8FF80"></div></div>' +
        '    <div style="display:flex;flex-direction:column;gap:6px;margin:2px 0">' + rowsHtml + '</div>' +
        '    <div class="gw-chips">' +
        '      <span class="gw-chip"><span class="gw-dot ' + (doneCount>0?'on':'off') + '" style="background:#A7C8FF;box-shadow:0 0 5px #A7C8FF"></span> TODAY <span class="gw-chip-val">' + doneCount + '</span></span>' +
        '      <span class="gw-chip">PENDING <span class="gw-chip-val">' + (totalCount - doneCount) + '</span></span>' +
        '      <span class="gw-chip" onclick="event.stopPropagation();openModal(\'task\')">+ ADD TASK</span>' +
        '    </div>' +
        '  </div>' +
        '</div>';
    }
  }
});

// ─── 2. TIME TRACKER WIDGET ────────────────────────────────────────────────
WidgetRegistry.register('time_tracker', {
  id: 'time_tracker',
  title: 'Time Tracker',
  icon: 'schedule',
  color: '#8D6CFF',
  category: 'focus',
  supportedSizes: ['1*1', '2*1', '4*2'],
  defaultSize: '4*2',
  description: 'Milestone countdowns, event timers, and pinned dates.',

  render: function(container, size, data){
    var days = (data && data.days) || [];
    var count = days.length;
    var iconSvg = '<svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M19 3h-1V1h-2v2H8V1H6v2H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM7 10h5v5H7z"/></svg>';

    if(size === '1*1'){
      var first = days[0];
      container.innerHTML =
        '<div class="widget-gamify-card widget-clickable" onclick="openDayTrackerPage()">' +
        '  <div class="gw-row1">' +
        '    <span class="gw-badge" style="color:#8D6CFF;background:#8D6CFF15;border-color:#8D6CFF30">TIME</span>' +
        '    <span class="gw-rank">' + count + ' PINNED</span>' +
        '  </div>' +
        '  <div class="widget-stat-center" style="margin:4px 0">' +
        '    <span style="font-size:17px;font-family:\'JetBrains Mono\',monospace;font-weight:700;color:var(--text)">' + (first ? (first.diff_days || '0d') : '--') + '</span>' +
        '    <span style="font-size:8px;color:var(--text-inactive);text-transform:uppercase;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;max-width:100%">' + esc((first && (first.title || first.name)) || 'None') + '</span>' +
        '  </div>' +
        '</div>';
    } else if(size === '2*1'){
      var first2 = days.slice(0, 2);
      var chipsHtml = first2.map(function(d){
        return '<span class="gw-chip">' + esc(d.title || d.name || '').slice(0,10) + ' <span class="gw-chip-val">' + (d.diff_days || '0d') + '</span></span>';
      }).join('');

      container.innerHTML =
        '<div class="widget-gamify-card widget-clickable" onclick="openDayTrackerPage()">' +
        _widgetHeaderHtml('TIME TRACKER', '#8D6CFF', iconSvg, 'openDayTrackerPage()', count + ' PINNED') +
        '  <div class="widget-card-body">' +
        '    <div class="gw-row1">' +
        '      <div class="gw-lvl-group">' +
        '        <span class="gw-badge" style="color:#8D6CFF;background:#8D6CFF15;border-color:#8D6CFF30">COUNTDOWN</span>' +
        '        <span class="gw-rank">' + count + ' PINNED</span>' +
        '      </div>' +
        '    </div>' +
        '    <div class="gw-chips" style="margin-top:4px">' + (chipsHtml || '<span class="gw-chip">NO PINNED DATES</span>') + '</div>' +
        '  </div>' +
        '</div>';
    } else { // 4*2
      var cardsHtml = days.length === 0 ?
        '<div style="border:1px dashed var(--border);border-radius:6px;padding:12px;text-align:center;color:var(--text-inactive);font-size:11px">Pin milestone events in Day Tracker</div>' :
        '<div style="display:flex;gap:8px;overflow-x:auto;padding-bottom:4px;scrollbar-width:none" class="custom-scrollbar">' +
        days.map(function(d){
          return '<div style="flex-shrink:0;border:1px solid var(--border);background:rgba(255,255,255,0.02);padding:8px 12px;border-radius:6px;min-width:130px">' +
                 '  <div style="font-size:9px;color:var(--text-inactive);text-transform:uppercase">// ' + esc(d.title || d.name || '') + '</div>' +
                 '  <div style="font-size:16px;font-family:\'JetBrains Mono\',monospace;font-weight:700;color:var(--text);margin-top:2px">' + (d.diff_days || '0d') + '</div>' +
                 '  <div style="font-size:9px;color:#8D6CFF;text-transform:uppercase">' + (d.past ? 'PAST' : 'REMAINING') + '</div>' +
                 '</div>';
        }).join('') + '</div>';

      container.innerHTML =
        '<div class="widget-gamify-card">' +
        _widgetHeaderHtml('TIME TRACKER', '#8D6CFF', iconSvg, 'openDayTrackerPage()', count + ' PINNED') +
        '  <div class="widget-card-body" onclick="openDayTrackerPage()">' +
             cardsHtml +
        '    <div class="gw-chips" style="margin-top:4px">' +
        '      <span class="gw-chip"><span class="gw-dot on" style="background:#8D6CFF;box-shadow:0 0 5px #8D6CFF"></span> PINNED <span class="gw-chip-val">' + count + '</span></span>' +
        '      <span class="gw-chip">MANAGE DATES &rsaquo;</span>' +
        '    </div>' +
        '  </div>' +
        '</div>';
    }
  }
});

// ─── 3. HABITS WIDGET ──────────────────────────────────────────────────────
WidgetRegistry.register('habits', {
  id: 'habits',
  title: 'Habits',
  icon: 'repeat',
  color: '#FF9F45',
  category: 'productivity',
  supportedSizes: ['1*1', '2*1', '4*2'],
  defaultSize: '4*2',
  description: 'Daily habit streaks, routine check-ins, and completion rings.',

  render: function(container, size, data){
    var habits = (data && data.habits) || [];
    var streak = (data && data.streak) || 0;
    var done = habits.filter(function(h){ return h.done; }).length;
    var total = habits.length;
    var pct = total > 0 ? Math.round((done / total) * 100) : 0;
    var iconSvg = '<svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>';

    if(size === '1*1'){
      container.innerHTML =
        '<div class="widget-gamify-card widget-clickable" onclick="switchTab(\'log\')">' +
        '  <div class="gw-row1">' +
        '    <span class="gw-badge" style="color:#FF9F45;background:#FF9F4515;border-color:#FF9F4530">HABITS</span>' +
        '    <span class="gw-rank">' + streak + 'd</span>' +
        '  </div>' +
        '  <div class="widget-stat-center" style="margin:4px 0">' +
        '    <span style="font-size:18px;font-family:\'JetBrains Mono\',monospace;font-weight:700;color:var(--text)">' + done + '/' + total + '</span>' +
        '    <span style="font-size:8px;color:var(--text-inactive);text-transform:uppercase">TODAY</span>' +
        '  </div>' +
        '  <div class="gw-bar-track"><div class="gw-bar-fill" style="width:' + pct + '%;background:#FF9F45;box-shadow:0 0 6px #FF9F4580"></div></div>' +
        '</div>';
    } else if(size === '2*1'){
      container.innerHTML =
        '<div class="widget-gamify-card widget-clickable" onclick="switchTab(\'log\')">' +
        _widgetHeaderHtml('HABITS', '#FF9F45', iconSvg, 'switchTab(\'log\')', done + '/' + total) +
        '  <div class="widget-card-body">' +
        '    <div class="gw-row1">' +
        '      <div class="gw-lvl-group">' +
        '        <span class="gw-badge" style="color:#FF9F45;background:#FF9F4515;border-color:#FF9F4530">TODAY ' + done + '/' + total + '</span>' +
        '        <span class="gw-rank">' + (total>0?pct+'%':'NO HABITS') + '</span>' +
        '      </div>' +
        '      <span class="gw-total">' + streak + 'd STREAK</span>' +
        '    </div>' +
        '    <div class="gw-bar-track"><div class="gw-bar-fill" style="width:' + pct + '%;background:#FF9F45;box-shadow:0 0 6px #FF9F4580"></div></div>' +
        '    <div class="gw-chips">' +
        '      <span class="gw-chip"><span class="gw-dot ' + (done>0?'on':'off') + '" style="background:#FF9F45;box-shadow:0 0 5px #FF9F45"></span> DONE <span class="gw-chip-val">' + done + '</span></span>' +
        '      <span class="gw-chip">STREAK <span class="gw-chip-val">' + streak + 'd</span></span>' +
        '    </div>' +
        '  </div>' +
        '</div>';
    } else { // 4*2
      container.innerHTML =
        '<div class="widget-gamify-card">' +
        _widgetHeaderHtml('HABITS', '#FF9F45', iconSvg, 'switchTab(\'log\')', 'STREAK ' + streak + 'd') +
        '  <div class="widget-card-body" onclick="switchTab(\'log\')">' +
        '    <div class="gw-row1">' +
        '      <div class="gw-lvl-group">' +
        '        <span class="gw-badge" style="color:#FF9F45;background:#FF9F4515;border-color:#FF9F4530">TODAY ' + done + '/' + total + '</span>' +
        '        <span class="gw-rank">' + (total > 0 ? done + ' COMPLETED' : 'NO HABITS CONFIGURED') + '</span>' +
        '      </div>' +
        '      <span class="gw-total">' + streak + 'd <span style="font-size:9px;color:#8A8F98;font-weight:400">STREAK</span></span>' +
        '    </div>' +
        '    <div class="gw-bar-track"><div class="gw-bar-fill" style="width:' + pct + '%;background:#FF9F45;box-shadow:0 0 6px #FF9F4580"></div></div>' +
        '    <div class="gw-chips">' +
        '      <span class="gw-chip"><span class="gw-dot ' + (done>0?'on':'off') + '" style="background:#FF9F45;box-shadow:0 0 5px #FF9F45"></span> COMPLETED <span class="gw-chip-val">' + done + '</span></span>' +
        '      <span class="gw-chip">STREAK <span class="gw-chip-val">' + streak + 'd</span></span>' +
        '      <span class="gw-chip">TAP TO LOG &rsaquo;</span>' +
        '    </div>' +
        '  </div>' +
        '</div>';
    }
  }
});

// ─── 4. DAILY READ WIDGET ──────────────────────────────────────────────────
WidgetRegistry.register('daily_read', {
  id: 'daily_read',
  title: 'Daily Read',
  icon: 'menu_book',
  color: '#8D6CFF',
  category: 'reflection',
  supportedSizes: ['1*1', '2*1', '4*2'],
  defaultSize: '4*2',
  description: 'Daily verse or philosophical passage with reflection notes.',

  render: function(container, size, data){
    var currentItem = gitaData;
    var secLabel = currentItem ? ('CHAPTER ' + currentItem.chapter + ' · VERSE ' + currentItem.verse) : 'BHAGAVAD GITA';
    var textSnippet = (currentItem && currentItem.shloka && currentItem.shloka.text) || 'Loading daily verse...';
    var transSnippet = (currentItem && currentItem.english_sivananda && currentItem.english_sivananda.text) || '';
    var iconSvg = '<svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M21 5c-1.11-.35-2.33-.5-3.5-.5-1.95 0-4.05.4-5.5 1.5-1.45-1.1-3.55-1.5-5.5-1.5S2.45 4.9 1 6v14.65c0 .25.25.5.5.5.1 0 .15-.05.25-.05C3.1 20.45 5.05 20 6.5 20c1.95 0 4.05.4 5.5 1.5 1.35-.85 3.8-1.5 5.5-1.5 1.65 0 3.35.3 4.75 1.05.1.05.15.05.25.05.25 0 .5-.25.5-.5V6c-.6-.45-1.25-.75-2-1zm0 13.5c-1.1-.35-2.3-.5-3.5-.5-1.7 0-4.15.65-5.5 1.5V8c1.35-.85 3.8-1.5 5.5-1.5 1.2 0 2.4.15 3.5.5v11.5z"/></svg>';

    if(!currentItem && typeof BASE !== 'undefined'){
      fetch(BASE + '/gita/today').then(function(r){ return r.json(); }).then(function(v){
        if(v && typeof renderGita === 'function') renderGita(v);
      }).catch(function(){});
    }

    if(size === '1*1'){
      container.innerHTML =
        '<div class="widget-gamify-card widget-clickable" onclick="switchTab(\'gita\')">' +
        '  <div class="gw-row1">' +
        '    <span class="gw-badge" style="color:#8D6CFF;background:#8D6CFF15;border-color:#8D6CFF30">READ</span>' +
        '    <span class="gw-rank">DAILY</span>' +
        '  </div>' +
        '  <div class="widget-stat-center" style="margin:4px 0">' +
        '    <span style="font-size:14px;font-family:\'JetBrains Mono\',monospace;font-weight:700;color:var(--text)">' + (currentItem ? (currentItem.chapter + '.' + currentItem.verse) : 'GITA') + '</span>' +
        '    <span style="font-size:8px;color:var(--text-inactive);text-transform:uppercase">BHAGAVAD GITA</span>' +
        '  </div>' +
        '</div>';
    } else if(size === '2*1'){
      container.innerHTML =
        '<div class="widget-gamify-card widget-clickable" onclick="switchTab(\'gita\')">' +
        _widgetHeaderHtml('READ: BHAGAVAD GITA', '#8D6CFF', iconSvg, 'switchTab(\'gita\')', secLabel) +
        '  <div class="widget-card-body">' +
        '    <div class="gw-row1">' +
        '      <div class="gw-lvl-group">' +
        '        <span class="gw-badge" style="color:#8D6CFF;background:#8D6CFF15;border-color:#8D6CFF30">' + esc(secLabel) + '</span>' +
        '        <span class="gw-rank">VYASA</span>' +
        '      </div>' +
        '    </div>' +
        '    <div style="font-size:11px;color:var(--text);margin:2px 0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">' +
               esc(textSnippet.slice(0, 50)) +
        '    </div>' +
        '    <div class="gw-chips">' +
        '      <span class="gw-chip">READ FULL &rsaquo;</span>' +
        '      <span class="gw-chip" onclick="event.stopPropagation();switchTab(\'gita\');toggleGitaThoughtInput();">REFLECT</span>' +
        '    </div>' +
        '  </div>' +
        '</div>';
    } else { // 4*2
      container.innerHTML =
        '<div class="widget-gamify-card">' +
        _widgetHeaderHtml('DAILY READ: BHAGAVAD GITA', '#8D6CFF', iconSvg, 'switchTab(\'gita\')', secLabel) +
        '  <div class="widget-card-body" onclick="switchTab(\'gita\')">' +
        '    <div class="gw-row1">' +
        '      <div class="gw-lvl-group">' +
        '        <span class="gw-badge" style="color:#8D6CFF;background:#8D6CFF15;border-color:#8D6CFF30">' + esc(secLabel) + '</span>' +
        '        <span class="gw-rank">VYASA</span>' +
        '      </div>' +
        '      <span class="gw-total" style="font-size:10px;color:var(--text-inactive)">ACTIVE PASSAGE</span>' +
        '    </div>' +
        '    <div style="font-size:14px;line-height:1.5;color:var(--text);margin:6px 0;font-family:\'Marcellus\',serif;font-weight:500">' +
               esc(textSnippet.slice(0, 140)) +
        '    </div>' +
        (transSnippet ? '    <div style="font-size:11px;color:var(--text-inactive);font-style:italic;line-height:1.4;margin-bottom:6px">' + esc(transSnippet.slice(0, 120)) + '...</div>' : '') +
        '    <div class="gw-chips">' +
        '      <span class="gw-chip"><span class="gw-dot on" style="background:#8D6CFF;box-shadow:0 0 5px #8D6CFF"></span> READ FULL</span>' +
        '      <span class="gw-chip" onclick="event.stopPropagation();switchTab(\'gita\');toggleGitaThoughtInput();">REFLECT</span>' +
        '      <span class="gw-chip" onclick="event.stopPropagation();if(typeof toggleGitaRead===\'function\')toggleGitaRead();">MARK READ</span>' +
        '    </div>' +
        '  </div>' +
        '</div>';
    }
  }
});

// ─── 5. MOOD & ENERGY WIDGET ───────────────────────────────────────────────
WidgetRegistry.register('mood_energy', {
  id: 'mood_energy',
  title: 'Mood & Energy',
  icon: 'battery_charging_full',
  color: '#4DE086',
  category: 'health',
  supportedSizes: ['1*1', '2*1'],
  defaultSize: '2*1',
  description: 'Log and track daily mental and physical state with quick ratings.',

  render: function(container, size, data){
    var mood = (data && data.mood) || null;
    var energy = (data && data.energy) || null;
    var iconSvg = '<svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M7 2v11h3v9l7-12h-4l4-8z"/></svg>';

    if(size === '1*1'){
      container.innerHTML =
        '<div class="widget-gamify-card widget-clickable" onclick="openMoodModal()">' +
        '  <div class="gw-row1">' +
        '    <span class="gw-badge" style="color:#4DE086;background:#4DE08615;border-color:#4DE08630">VITALS</span>' +
        '  </div>' +
        '  <div class="widget-stat-center" style="margin:4px 0">' +
        '    <span style="font-size:16px;font-family:\'JetBrains Mono\',monospace;font-weight:700;color:var(--text)">' + (mood ? moodLabel(mood.mood) : '--') + '</span>' +
        '    <span style="font-size:8px;color:var(--text-inactive);text-transform:uppercase">' + (energy ? 'Energy: ' + energy.energy + '/5' : 'Tap to log') + '</span>' +
        '  </div>' +
        '</div>';
    } else { // 2*1
      container.innerHTML =
        '<div class="widget-gamify-card widget-clickable" onclick="openMoodModal()">' +
        _widgetHeaderHtml('MOOD & ENERGY', '#4DE086', iconSvg, 'openMoodModal()', '') +
        '  <div class="widget-card-body">' +
        '    <div class="gw-chips" style="margin-top:2px">' +
        '      <span class="gw-chip" onclick="event.stopPropagation();openMoodModal();"><span class="gw-dot ' + (mood?'on':'off') + '" style="background:#4DE086;box-shadow:0 0 5px #4DE086"></span> MOOD <span class="gw-chip-val">' + (mood ? moodLabel(mood.mood) : '+ LOG') + '</span></span>' +
        '      <span class="gw-chip" onclick="event.stopPropagation();openEnergyModal();">ENERGY <span class="gw-chip-val">' + (energy ? energy.energy + '/5' : '+ LOG') + '</span></span>' +
        '    </div>' +
        '  </div>' +
        '</div>';
    }
  }
});

// ─── 6. SLEEP WIDGET ───────────────────────────────────────────────────────
WidgetRegistry.register('sleep', {
  id: 'sleep',
  title: 'Sleep Status',
  icon: 'bedtime',
  color: '#68A6FF',
  category: 'health',
  supportedSizes: ['1*1', '2*1'],
  defaultSize: '2*1',
  description: 'Sleep duration, asleep/awake status, and wake-up logs.',

  render: function(container, size, data){
    var sleep = (data && data.sleep) || null;
    var dur = (sleep && sleep.sleep_formatted) || '--';
    var status = (sleep && sleep.status === 'checked_in') ? 'Asleep' : (sleep && sleep.status === 'checked_out') ? 'Awake' : '--';
    var iconSvg = '<svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M12.3 2a10 10 0 0 0-1.9 19.8 10 10 0 0 0 11.6-11.6A10 10 0 0 0 12.3 2z"/></svg>';

    if(size === '1*1'){
      container.innerHTML =
        '<div class="widget-gamify-card widget-clickable" onclick="openSleepPage()">' +
        '  <div class="gw-row1">' +
        '    <span class="gw-badge" style="color:#68A6FF;background:#68A6FF15;border-color:#68A6FF30">SLEEP</span>' +
        '    <span class="gw-rank">' + status + '</span>' +
        '  </div>' +
        '  <div class="widget-stat-center" style="margin:4px 0">' +
        '    <span style="font-size:16px;font-family:\'JetBrains Mono\',monospace;font-weight:700;color:var(--text)">' + dur + '</span>' +
        '  </div>' +
        '</div>';
    } else { // 2*1
      container.innerHTML =
        '<div class="widget-gamify-card widget-clickable" onclick="openSleepPage()">' +
        _widgetHeaderHtml('SLEEP & RECOVERY', '#68A6FF', iconSvg, 'openSleepPage()', status) +
        '  <div class="widget-card-body">' +
        '    <div class="gw-row1">' +
        '      <div class="gw-lvl-group">' +
        '        <span class="gw-badge" style="color:#68A6FF;background:#68A6FF15;border-color:#68A6FF30">TOTAL SLEEP</span>' +
        '        <span class="gw-total">' + dur + '</span>' +
        '      </div>' +
        '      <span class="gw-rank">' + (sleep && sleep.up_formatted ? sleep.up_formatted + ' UP' : '--') + '</span>' +
        '    </div>' +
        '    <div class="gw-chips" style="margin-top:6px">' +
        '      <span class="gw-chip"><span class="gw-dot ' + (sleep?'on':'off') + '" style="background:#68A6FF;box-shadow:0 0 5px #68A6FF"></span> ' + status.toUpperCase() + '</span>' +
        '      <span class="gw-chip">LOG SLEEP &rsaquo;</span>' +
        '    </div>' +
        '  </div>' +
        '</div>';
    }
  }
});

// ─── 7. ACTIVITY FEED WIDGET ──────────────────────────────────────────────
WidgetRegistry.register('feed', {
  id: 'feed',
  title: 'Activity Feed',
  icon: 'stream',
  color: '#4DE086',
  category: 'reflection',
  supportedSizes: ['2*1', '4*2'],
  defaultSize: '4*2',
  description: 'Stream of recent thoughts, journals, wins, lessons, and runs.',

  render: function(container, size, data){
    var feed = (data && data.feed) || [];
    var iconSvg = '<svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M4 14h4v-4H4v4zm0 5h4v-4H4v4zM4 9h4V5H4v4zm5 5h12v-4H9v4zm0 5h12v-4H9v4zM9 5v4h12V5H9z"/></svg>';

    if(size === '2*1'){
      var latest = feed[0];
      var text = latest ? (latest.text || latest.html || 'No activity') : 'Waiting for new input...';
      container.innerHTML =
        '<div class="widget-gamify-card widget-clickable" onclick="openTimelineSidebar()">' +
        '  <div class="gw-row1">' +
        '    <span class="gw-badge" style="color:#4DE086;background:#4DE08615;border-color:#4DE08630">LIFE LOG</span>' +
        '    <span class="gw-rank">LATEST</span>' +
        '  </div>' +
        '  <div style="font-size:11px;color:var(--text);margin:6px 0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">' +
             text +
        '  </div>' +
        '</div>';
    } else { // 4*2
      var feedItems = feed.length === 0 ?
        '<div style="padding:6px 0;color:var(--text-inactive);opacity:0.5;font-style:italic">Waiting for new input...</div>' :
        feed.slice(0, 4).map(function(i){
          return '<div style="font-size:11px;padding:4px 0;border-bottom:1px solid rgba(255,255,255,0.05);color:var(--text-inactive);overflow:hidden;text-overflow:ellipsis;white-space:nowrap">' + (i.html || esc(i.text || '')) + '</div>';
        }).join('');

      container.innerHTML =
        '<div class="widget-gamify-card">' +
        _widgetHeaderHtml('LIFE LOG', '#4DE086', iconSvg, 'openTimelineSidebar()', feed.length + ' ENTRIES') +
        '  <div class="widget-card-body" onclick="openTimelineSidebar()">' +
        '    <div style="display:flex;flex-direction:column;gap:2px;margin-bottom:6px">' + feedItems + '</div>' +
        '    <div class="gw-chips">' +
        '      <span class="gw-chip"><span class="gw-dot on" style="background:#4DE086;box-shadow:0 0 5px #4DE086"></span> STREAM</span>' +
        '      <span class="gw-chip">TIMELINE &rsaquo;</span>' +
        '    </div>' +
        '  </div>' +
        '</div>';
    }
  }
});

// ─── 8. PLUGINS WIDGET ────────────────────────────────────────────────────
WidgetRegistry.register('plugins', {
  id: 'plugins',
  title: 'Installed Plugins',
  icon: 'extension',
  color: '#A7C8FF',
  category: 'plugins',
  supportedSizes: ['4*2'],
  defaultSize: '4*2',
  description: 'Container for all active plugin widgets (Gamify, custom plugins).',

  render: function(container, size, data){
    var iconSvg = '<svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M20.5 11H19V7c0-1.1-.9-2-2-2h-4V3.5a2.5 2.5 0 0 0-5 0V5H4c-1.1 0-1.99.9-1.99 2v3.8H3.5c1.49 0 2.7 1.21 2.7 2.7s-1.21 2.7-2.7 2.7H2V20c0 1.1.9 2 2 2h3.8v-1.5c0-1.49 1.21-2.7 2.7-2.7 1.49 0 2.7 1.21 2.7 2.7V22H17c1.1 0 2-.9 2-2v-4h1.5a2.5 2.5 0 0 0 0-5z"/></svg>';
    container.innerHTML =
      '<div class="widget-gamify-card">' +
      _widgetHeaderHtml('PLUGINS', '#A7C8FF', iconSvg, 'openSettingsTab(\'plugins\')', '') +
      '  <div class="widget-card-body">' +
      '    <div id="pluginWidgetsArea" style="display:flex;flex-direction:column;gap:8px"></div>' +
      '  </div>' +
      '</div>';
    if(typeof renderPluginWidgets === 'function'){
      renderPluginWidgets();
    }
  }
});
