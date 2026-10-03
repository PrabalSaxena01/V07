// ─── Navigation ───
var LOG_MODULES=[
  {id:'mood',win:'VITALS',icon:'monitor_heart',label:'MOOD & ENERGY',desc:'Track your emotional and energy levels.',color:'var(--accent-purple)',page:'openMoodEnergyPage()'},
  {id:'sleep',win:'SLEEP',icon:'bedtime',label:'SLEEP LOG',desc:'Record check-in and check-out sleep data.',color:'#68A6FF',page:'openSleepUpPage()'},
  {id:'run',win:'RUN',icon:'directions_run',label:'RUN',desc:'View running stats and log a run.',color:'#68A6FF',page:'openRunningPage()'},
  {id:'thoughts',win:'NOTES',icon:'psychology',label:'ALL THOUGHTS',desc:'Access your persistent stream of consciousness and notes.',color:'var(--accent-purple)',page:'openThoughtsPage()'},
  {id:'tasks',win:'TASKS',icon:'list_alt',label:'ALL TASKS',desc:'Define and track the execution of your daily directives.',color:'var(--secondary)',page:'openTasksPage(\'all\')'},
  {id:'capsule',win:'ARCHIVE',icon:'hourglass_empty',label:'TIME CAPSULE',desc:'Explore your long-term digital memory and archives.',color:'var(--yellow)',page:'openVaultPage()'},
  {id:'db',win:'RECORDS',icon:'storage',label:'DATABASE',desc:'The central repository for all recorded life-log metrics.',color:'#FF9F45',page:'openDatabasePage()'},
  {id:'checklist',win:'ROUTINE',icon:'checklist',label:'CHECKLIST',desc:'Maintain operational discipline with daily system checks.',color:'#68A6FF',page:'openChecklist()'},
  {id:'tracker',win:'SCHEDULE',icon:'calendar_month',label:'TIME TRACKER',desc:'Categorized date and event tracking system.',color:'var(--secondary)',page:'openDayTrackerPage()'},
  {id:'appusage',win:'USAGE',icon:'screen_lock_portrait',label:'SCREEN TIME',desc:'Monitor your digital consumption patterns.',color:'#FF9F45',page:'openAppUsagePage()'},
  {id:'recycle',win:'CLEANUP',icon:'delete_outline',label:'RECYCLE BIN',desc:'System-wide cleanup for all deleted logs and files.',color:'var(--red)',page:'openRecycleBinPage()',danger:true}
];

function openLogDashboard(){
  document.getElementById('mainFab').style.display='none';
  var el=document.getElementById('logDashboardPage');el.scrollTop=0;el.classList.add('show');
  var container=document.getElementById('logDashboardContent');
  var html='<div style="display:grid;grid-template-columns:1fr 1fr;gap:8px">';
  LOG_MODULES.forEach(function(m){
    var span=m.danger?'grid-column:1/-1;':'';
    html+='<div onclick="closeLogDashboard();switchTab(\'home\',true);'+m.page+'" style="border:1px solid var(--border);border-bottom:2px solid '+m.color+';background:var(--surface);cursor:pointer;display:flex;flex-direction:column;overflow:hidden;transition:opacity 0.15s;'+span+'" onmouseover="this.style.opacity=\'0.85\'" onmouseout="this.style.opacity=\'1\'">';
    html+='<div style="padding:4px 8px;display:flex;justify-content:space-between;align-items:center;border-bottom:1px solid var(--border)">';
    html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:9px;font-weight:700;opacity:0.8;color:'+m.color+'">'+m.win+(m.danger?' &bull; <span style="color:var(--crayon-red,#C94A29)">DANGER ZONE</span>':'')+'</span>';
    html+='</div>';
    html+='<div style="padding:12px;display:flex;flex-direction:column;gap:6px">';
    html+='<div style="display:flex;align-items:center;gap:6px">';
    html+='<span class="material-symbols-outlined" style="font-size:18px;color:'+m.color+'">'+m.icon+'</span>';
    html+='<span style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:12px;font-weight:700;text-transform:uppercase;letter-spacing:0.05em;color:'+m.color+'">'+m.label+'</span>';
    html+='</div>';
    html+='<p style="font-size:11px;opacity:0.7;line-height:1.4">'+m.desc+'</p>';
    html+='</div></div>';
  });
  html+='</div>';
  container.innerHTML=html;
}
function closeLogDashboard(){
  document.getElementById('mainFab').style.display='flex';
  document.getElementById('logDashboardPage').classList.remove('show');
}

// ─── Tab Navigation ──────────────────────────────────────────────────────────
var currentTab = 'home';

function closeAllOverlays(){
  var ids=['logDashboardPage','gitaPage','databasePage','vaultPage','sessionsPage','moodEnergyPage','sleepUpPage','checklistPage','recycleBinPage','thoughtsPage','tasksPage','appUsagePage','runningPage','dayTrackerPage','settingsPage'];
  ids.forEach(function(id){var el=document.getElementById(id);if(el)el.classList.remove('show')});
  closeGitaSidebar();
  closeTimelineSidebar();
  var mob=document.getElementById('modalOverlay');if(mob)mob.classList.remove('show');
}

function switchTab(tab, fromPopstate) {
  closeAllOverlays();
  currentTab = tab;
  var nav = document.getElementById('bottomNav');
  nav.querySelectorAll('a').forEach(function(a) {
    a.classList.toggle('active', a.dataset.tab === tab);
  });
  var titleEl = document.getElementById('pageTitle');
  var titles = { home: (localStorage.getItem('v07_dashboard_name') || 'V07'), log: 'TOOLKIT', track: 'TIMELINE', gita: 'READING', settings: 'SETTINGS' };
  titleEl.textContent = titles[tab] || (localStorage.getItem('v07_dashboard_name') || 'V07');
  var fab = document.getElementById('mainFab');
  if (tab === 'settings') { fab.style.display = 'none'; openSettingsPage(); }
  else if (tab === 'log') { fab.style.display = 'flex'; openLogDashboard(); }
  else if (tab === 'gita') { fab.style.display = 'none'; openGitaPage(); }
  else { fab.style.display = 'flex'; }
  if(!fromPopstate)try{history.pushState({tab:tab},'',location.href)}catch(e){}
}

function handleFabClick() {
  if (currentTab === 'settings') return;
  openCommandLauncher();
}

function openCommandLauncher() {
  var existing = document.getElementById('cmdPaletteOverlay');
  if (existing) { closeCmdPalette(); return; }
  openPalettePanel();
}

function openPalettePanel() {
  var overlay = document.createElement('div');
  overlay.id = 'cmdPaletteOverlay';
  overlay.className = 'cmd-palette-overlay';
  var panel = document.createElement('div');
  panel.className = 'cmd-palette-panel';
  var icons = {
    think:'M9 21c0 .55.45 1 1 1h4c.55 0 1-.45 1-1v-1H9v1zm3-19C8.14 2 5 5.14 5 9c0 2.38 1.19 4.47 3 5.74V17c0 .55.45 1 1 1h6c.55 0 1-.45 1-1v-2.26c1.81-1.27 3-3.36 3-5.74 0-3.86-3.14-7-7-7z',
    mood:'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-5-6c.78 2.34 2.72 4 5 4s4.22-1.66 5-4H7zm1-2c.55 0 1-.45 1-1s-.45-1-1-1-1 .45-1 1 .45 1 1 1zm8 0c.55 0 1-.45 1-1s-.45-1-1-1-1 .45-1 1 .45 1 1 1z',
    bolt:'M11 21h-1l1-7H7.5c-.88 0-.33-.75-.31-.78C8.48 10.94 10.42 7.54 13.01 3h1l-1 7h3.51c.4 0 .62.19.4.66C12.97 17.55 11 21 11 21z',
    run:'M13.49 5.48c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm-3.6 13.9l1-4.4 2.1 2v6h2v-7.5l-2.1-2 .6-3C14.8 12 16.8 13 19 13v-2c-1.9 0-3.5-1-4.3-2.4l-1-1.6c-.56-.89-1.68-1.25-2.65-.84L7 11v5h2v-3.4l1.8-.7-1.6 8.1-4.9-1-.4 2 7 1.4z',
    win:'M19 5h-2V3H7v2H5c-1.1 0-2 .9-2 2v1c0 2.55 1.92 4.63 4.39 4.94.63 1.5 1.98 2.63 3.61 2.96V19H7v2h10v-2h-4v-3.1c1.63-.33 2.98-1.46 3.61-2.96C19.08 12.63 21 10.55 21 8V7c0-1.1-.9-2-2-2zM5 8V7h2v3.82C5.84 10.4 5 9.3 5 8zm14 0c0 1.3-.84 2.4-2 2.82V7h2v1z',
    habit:'M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-2 10h-4v4h-2v-4H7v-2h4V7h2v4h4v2z',
    dsa:'M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 18H6V4h12v16zm-7-1h2v-2h2v-2h-2v-2h-2v2H9v2h2v2z',
    chess:'M19 22H5v-2h14v2zM7 2v1h1v1c0 .55.45 1 1 1h5c.55 0 1-.45 1-1V3h1V2H7zm4 7V6h-1v3H9l-1 7h2v4h2v-4h2v4h2v-4h2l-1-7h-2V9h-1z',
    pushups:'M20.57 14.86L22 13.43 20.57 12 17 15.57 8.43 7 12 3.43 10.57 2 9.14 3.43 7.71 2 5.57 4.14 4.14 2.71 2.71 4.14l1.43 1.43L2 7.71l1.43 1.43L2 10.57 3.43 12 7 8.43 15.57 17 12 20.57 13.43 22l1.43-1.43L16.29 22l2.14-2.14 1.43 1.43 1.43-1.43-1.43-1.43L22 16.29z',
    panic:'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z'
  };
  function makeRow(icon, label, arrow, onclick) {
    var row = document.createElement('div');
    row.className = 'cmd-palette-row';
    row.innerHTML = '<svg viewBox="0 0 24 24"><path d="'+icons[icon]+'"/></svg><span class="cmd-label">'+label+'</span>'+(arrow?'<span class="cmd-arrow">\u2192</span>':'');
    row.onclick = onclick;
    return row;
  }
  function renderPage1() {
    panel.innerHTML = '';
    panel.appendChild(makeRow('think','THINK',false,function(){closeCmdPalette();openModal('think')}));
    panel.appendChild(makeRow('mood','MOOD',false,function(){closeCmdPalette();openModal('mood')}));
    panel.appendChild(makeRow('bolt','ENERGY',false,function(){closeCmdPalette();openModal('energy')}));
    panel.appendChild(makeRow('run','RUN',false,function(){closeCmdPalette();openModal('run')}));
    var moreRow = makeRow(null,'MORE',true,function(){renderPage2()});
    moreRow.innerHTML = '<span class="cmd-label" style="font-size:16px;letter-spacing:2px"> \u2026 </span><span class="cmd-label">MORE</span><span class="cmd-arrow">\u2192</span>';
    panel.appendChild(moreRow);
    panel.querySelectorAll('.cmd-palette-row').forEach(function(r,i){
      r.style.opacity='0';r.style.transform='translateY(8px)';
      setTimeout(function(){r.style.transition='all 0.18s ease';r.style.opacity='1';r.style.transform='translateY(0)'},40+i*25);
    });
  }
  function renderPage2() {
    panel.innerHTML = '';
    var backRow = document.createElement('div');
    backRow.className = 'cmd-palette-row';
    backRow.innerHTML = '<span class="cmd-label" style="font-size:14px">\u2190</span><span class="cmd-label">BACK</span>';
    backRow.onclick = function(){renderPage1()};
    panel.appendChild(backRow);
    panel.appendChild(makeRow('win','WIN',false,function(){closeCmdPalette();openModal('win')}));
    panel.appendChild(makeRow('habit','HABIT',false,function(){closeCmdPalette();openModal('habit')}));
    panel.appendChild(makeRow('dsa','DSA',false,function(){closeCmdPalette();openModal('dsa')}));
    panel.appendChild(makeRow('chess','CHESS',false,function(){closeCmdPalette();openModal('chess')}));
    panel.appendChild(makeRow('pushups','PUSHUPS',false,function(){closeCmdPalette();openModal('pushups')}));
    panel.appendChild(makeRow('panic','PANIC',false,function(){closeCmdPalette();openModal('panic')}));
    panel.querySelectorAll('.cmd-palette-row').forEach(function(r,i){
      r.style.opacity='0';r.style.transform='translateX(20px)';
      setTimeout(function(){r.style.transition='all 0.18s ease';r.style.opacity='1';r.style.transform='translateX(0)'},30+i*25);
    });
  }
  overlay.onclick = function(e){if(e.target===overlay)closeCmdPalette()};
  overlay.appendChild(panel);
  document.body.appendChild(overlay);
  requestAnimationFrame(function(){overlay.classList.add('open')});
  renderPage1();
  requestAnimationFrame(function(){requestAnimationFrame(function(){panel.classList.add('show')})});
}

function closeCmdPalette() {
  var overlay = document.getElementById('cmdPaletteOverlay');
  if(overlay){overlay.remove()}
}
