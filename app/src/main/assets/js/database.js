// ─── Database ─────────────────────────────────────────────────────────────────

var DB_LABELS={sessions:'Session',habits:'Habit',metrics:'Metric',panics:'Panic',journal:'Journal',dsa:'DSA',chess:'Chess'};
var DB_ALL_CATS=['all','sessions','habits','metrics','panics','journal','dsa','chess'];
var DB_ACTIVE_FILTER='all';
var _dbData=[];
var _dbDays=30;

function _dbIconSvg(name,size,color){
  var d=_iconPaths[name];if(!d)return '';
  return '<svg viewBox="0 -960 960 960" width="'+size+'" height="'+size+'" fill="'+color+'" style="flex-shrink:0"><path d="'+d+'"/></svg>';
}
function changeDbDays(){
  _dbDays=parseInt(document.getElementById('dbDays').value);if(isNaN(_dbDays))_dbDays=30;
  loadDatabasePage();
}

var DB_MOD_ICONS={sessions:'history',habits:'emoji_events',metrics:'analytics',panics:'warning',journal:'edit_note',dsa:'school',chess:'chess',tasks:'checklist',checklist:'checklist',daytracker:'calendar_month',thought:'psychology'};
var DB_MOD_COLORS={sessions:'var(--primary)',habits:'var(--secondary)',metrics:'var(--primary)',panics:'var(--accent-red)',journal:'var(--accent-orange)',dsa:'var(--accent-purple)',chess:'var(--text-inactive)'};

function renderDbFilters(){
  var c=document.getElementById('databaseFilters');
  var catIcons={all:'keyboard_command_key',sessions:'history',habits:'emoji_events',metrics:'analytics',panics:'warning',journal:'edit_note',dsa:'school',chess:'chess'};
  c.innerHTML=DB_ALL_CATS.map(function(cat){
    var active=DB_ACTIVE_FILTER===cat;
    var label=cat==='all'?'ALL':(DB_LABELS[cat]||cat).toUpperCase();
    var ic=_dbIconSvg(catIcons[cat],10,active?'#001b3c':'var(--text-inactive)');
    var s=active
      ?'padding:4px 8px;border:1px solid var(--primary);color:#001b3c;background:rgba(167,200,255,0.1);font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:0.08em;cursor:pointer;white-space:nowrap;display:inline-flex;align-items:center;gap:3px'
      :'padding:4px 8px;border:1px solid var(--border);color:var(--text-inactive);background:transparent;font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;text-transform:uppercase;letter-spacing:0.08em;cursor:pointer;white-space:nowrap;display:inline-flex;align-items:center;gap:3px;transition:border-color 0.15s,color 0.15s';
    return '<span onclick="setDbFilter(\''+cat+'\')" style="'+s+'">'+ic+label+'</span>';
  }).join('');
}
function setDbFilter(cat){DB_ACTIVE_FILTER=cat;renderDbFilters();renderDbEvents()}

function renderDbEvents(){
  var container=document.getElementById('databaseContent');
  var cutoff=new Date();
  if(_dbDays>0){
    cutoff.setDate(cutoff.getDate()-_dbDays);
    var cutoffStr=cutoff.getFullYear()+'-'+String(cutoff.getMonth()+1).padStart(2,'0')+'-'+String(cutoff.getDate()).padStart(2,'0');
    var filtered=_dbData.filter(function(e){return(e.ts||'').slice(0,10)>=cutoffStr});
  }else{
    var filtered=_dbData;
  }
  if(DB_ACTIVE_FILTER!=='all')filtered=filtered.filter(function(e){return e.category===DB_ACTIVE_FILTER});
  filtered.sort(function(a,b){return(b.ts||'').localeCompare(a.ts||'')});
  document.getElementById('databaseCount').textContent=filtered.length;
  if(filtered.length===0){
    container.innerHTML='<div style="color:var(--text-inactive);text-align:center;padding:20px;font-size:12px;font-family:\'JetBrains Mono\',\'Mono\',monospace">No records for this period</div>';
    return;
  }
  var html='<table style="width:100%;border-collapse:collapse;table-layout:auto">';
  html+='<colgroup><col style="width:1%"><col style="width:1%"><col></colgroup>';
  html+='<thead><tr style="border-bottom:1px solid var(--border);background:var(--surface-container-low)">';
  html+='<th style="padding:6px 8px;font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em;border-right:1px solid var(--border);white-space:nowrap">Timestamp</th>';
  html+='<th style="padding:6px 8px;font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em;border-right:1px solid var(--border);white-space:nowrap">Module</th>';
  html+='<th style="padding:6px 8px;font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em">Event / Value</th>';
  html+='</tr></thead>';
  html+='<tbody style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:12px">';
  filtered.forEach(function(e,i){
    var bg=i%2===1?'background:rgba(11,14,19,0.3)':'';
    var ts=(e.ts||'').replace('T','<br>');
    var cat=e.category||'unknown';
    var color=DB_MOD_COLORS[cat]||'var(--primary)';
    var label=(DB_LABELS[cat]||cat).toUpperCase();
    var eventText='';
    var eventStyle='color:var(--text)';
    if(cat==='sessions'){
      var act=activityLabel(e.activity);
      if(e.type==='start')eventText=act+(e.custom_label?' \u00b7 '+e.custom_label:'')+' started';
      else if(e.type==='end')eventText=act+' ended <span style="color:var(--text-inactive);margin-left:6px">('+e.duration+'m)</span>';
      else eventText=act;
    }else if(cat==='habits'){
      var act=activityLabel(e.activity);
      eventText=(e.on?'\u2713':'\u2717')+' '+act;
    }else if(cat==='metrics'){
      if(e.pushups)eventText='PUSHUPS: <span style="color:var(--secondary);font-weight:500">'+e.pushups+'</span>';
      if(e.doom_scroll)eventText='DOOM_SCROLL: <span style="color:var(--accent-red);font-weight:500">'+e.doom_scroll+'m</span>'+(e.trigger?' <span style="color:var(--text-inactive)">TRIGGER: '+esc(e.trigger)+'</span>':'');
    }else if(cat==='panics'){
      eventText='PANIC_EVENT';eventStyle='color:var(--accent-red);font-weight:600';
      if(e.trigger)eventText+=' <span style="color:var(--text-inactive);font-weight:400">'+esc(e.trigger)+'</span>';
    }else if(cat==='journal'){
      eventText='<em>"'+esc((e.text||'').slice(0,80))+'</em"';eventStyle='color:var(--accent-orange);font-style:italic';
    }else if(cat==='dsa'){
      eventText='LEETCODE_SOLVED: <span style="color:var(--accent-purple);font-weight:500">#'+esc(e.problem||'?')+'</span>';
    }else if(cat==='chess'){
      eventText='CHESS \u2014 '+(e.result||'');
    }else{
      eventText=esc(JSON.stringify(e).slice(0,120));
    }
    html+='<tr onclick="showDbEventDetail('+i+')" style="border-bottom:1px solid var(--border);'+bg+';cursor:pointer" onmouseenter="this.style.background=\'rgba(167,200,255,0.05)\'" onmouseleave="this.style.background=\''+(i%2===1?'rgba(11,14,19,0.3)':'')+'\'">';
    html+='<td style="padding:6px 8px;border-right:1px solid var(--border);color:var(--text-inactive);white-space:nowrap;font-size:11px">'+ts+'</td>';
    html+='<td style="padding:6px 8px;border-right:1px solid var(--border);text-transform:uppercase;font-size:10px;font-weight:600;color:'+color+';white-space:nowrap">'+label+'</td>';
    html+='<td style="padding:6px 8px;word-break:break-word;'+eventStyle+'">'+eventText+'</td>';
    html+='</tr>';
  });
  html+='</tbody></table>';
  container.innerHTML=html;
}

async function openDatabasePage(){
  document.getElementById('mainFab').style.display='none';_pushOv();
  document.getElementById('databasePage').classList.add('show');
  document.getElementById('databasePage').scrollTop=0;
  _dbDays=30;
  var sel=document.getElementById('dbDays');if(sel)sel.value='30';
  DB_ACTIVE_FILTER='all';
  renderDbFilters();
  loadDatabasePage();
}
function closeDatabasePage(){document.getElementById('mainFab').style.display='flex';document.getElementById('databasePage').classList.remove('show')}

async function loadDatabasePage(){
  var container=document.getElementById('databaseContent');
  if(typeof renderSkeleton==='function') container.innerHTML=renderSkeleton('list',6);
  else container.innerHTML='<div style="color:var(--text-inactive);font-size:12px;text-align:center;padding:20px;font-family:\'JetBrains Mono\',\'Mono\',monospace">Loading...</div>';
  try{
    var fetchDays=_dbDays>0?_dbDays:365;
    var data=await wrapFetch(fetch(BASE+'/database?days='+fetchDays),'database',{days:fetchDays}).then(function(r){return r.json()});
    _dbData=data.events||[];
    renderDbFilters();
    renderDbEvents();
  }catch(e){
    container.innerHTML='<div style="color:var(--accent-red);text-align:center;padding:20px;font-size:12px;font-family:\'JetBrains Mono\',\'Mono\',monospace">LOAD_FAILED<br>'+esc(e.message||e)+'</div>';
  }
}
