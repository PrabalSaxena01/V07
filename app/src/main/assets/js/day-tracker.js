// ─── Day Tracker ───

function openDayTrackerPage(){
  document.getElementById('mainFab').style.display='none';_pushOv();
  var el=document.getElementById('dayTrackerPage');el.scrollTop=0;el.classList.add('show');
  loadDayTrackerEntries();
}
function closeDayTrackerPage(){document.getElementById('mainFab').style.display='flex';document.getElementById('dayTrackerPage').classList.remove('show')}

function _hourOpts(selected){
  var h=selected!=null?parseInt(selected):new Date().getHours();
  var opts='';for(var i=0;i<24;i++){
    var v=i<10?'0'+i:''+i;
    opts+='<option value="'+v+'"'+(i==h?' selected':'')+'>'+v+':00</option>';
  }
  return opts;
}

function _timeDiffLabel(dateStr,hour){
  var now=new Date();
  var h=(hour!=null&&hour!=='')?parseInt(hour):0;
  var entry=new Date(dateStr+'T'+(h<10?'0':'')+h+':00:00');
  var diffMs=entry-now;
  var absMs=Math.abs(diffMs);
  var sign=diffMs>=0?1:-1;
  var totalHours=Math.floor(absMs/3600000);
  var diffDays=Math.floor(totalHours/24);
  var remHours=totalHours%24;
  var label,dColor;
  if(sign>0){
    if(diffDays>0)label=diffDays+'d '+remHours+'h left';
    else if(totalHours>0)label=totalHours+'h left';
    else label='Now';
    dColor='var(--accent2)';
  }else if(sign<0){
    if(diffDays>0)label=diffDays+'d '+remHours+'h ago';
    else if(totalHours>0)label=totalHours+'h ago';
    else label='Today';
    dColor='var(--muted)';
  }else{
    label='Now';dColor='var(--green)';
  }
  return{label:label,dColor:dColor};
}

var _dtMarqueeIdx=0;var _dtMarqueeTimer=null;
async function loadDayTrackerPreview(){
  try{
    var r=await fetch(BASE+'/daytracker');
    var j=await r.json();
    var entries=j.entries||[];
    var pinned=entries.filter(function(e){return e.pinned===true||e.pinned==='true'}).sort(function(a,b){return(a.date||'').localeCompare(b.date||'')});
    if(typeof updateDashboardData === 'function'){
      updateDashboardData({
        days: pinned.map(function(e){
          var td=_timeDiffLabel(e.date,e.hour);
          return {
            title: e.title || e.name || e.date,
            name: e.title || e.name || e.date,
            diff_days: td.label,
            past: td.dColor === 'var(--muted)'
          };
        })
      });
    }
    var carousel=document.getElementById('dayTrackerCarousel');
    var countEl=document.getElementById('dayTrackerCount');
    if(!carousel)return;
    if(pinned.length===0){
      carousel.innerHTML='<div style="flex-shrink:0;scroll-snap-align:start;border:1px solid var(--border);background:var(--surface-container);padding:12px;display:flex;flex-direction:column;gap:8px;border-radius:8px;width:160px;cursor:pointer" onclick="openDayTrackerPage()"><div style="display:flex;justify-content:space-between;align-items:center"><span style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.05em">NO PINNED ITEMS</span></div><div style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:12px;color:var(--text-inactive)">Pin items here</div></div>';
    }else{
      var icons=['flight_takeoff','cake','event_busy','school','work','favorite'];
      var bcolors=[{c:'rgba(167,200,255,0.2)',t:'var(--primary)'},{c:'rgba(141,108,255,0.3)',t:'var(--accent)'},{c:'rgba(244,211,94,0.3)',t:'var(--yellow)'},{c:'rgba(77,224,134,0.2)',t:'var(--green)'},{c:'rgba(255,159,69,0.3)',t:'var(--orange)'},{c:'rgba(141,108,255,0.3)',t:'var(--accent)'}];
      carousel.innerHTML=pinned.map(function(e,i){
        var td=_timeDiffLabel(e.date,e.hour);
        var bc=bcolors[i%bcolors.length];
        var catIcon=DT_ICONS[e.category]||DT_ICONS['default'];
        var catColor=DT_COLORS[e.category]||DT_COLORS['default'];
        var subLabel='Scheduled';
        if(td.dColor==='var(--muted)')subLabel='Past';
        else if(td.label.indexOf('d')>0)subLabel='Upcoming';
        return '<div style="flex-shrink:0;scroll-snap-align:start;border:1px solid var(--border);border-color:'+bc.c+';background:var(--surface-container);padding:12px;display:flex;flex-direction:column;gap:8px;border-radius:8px;width:160px;cursor:pointer" onclick="openDayTrackerPage()">'+
          '<div style="display:flex;justify-content:space-between;align-items:center">'+
            '<span style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.05em">'+esc((e.title||e.date).toUpperCase()).slice(0,10)+'</span>'+
            _dbIconSvg(catIcon,12,catColor)+
          '</div>'+
          '<div style="display:flex;align-items:baseline;gap:8px">'+
            '<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:18px;color:var(--text);font-variant-numeric:tabular-nums">'+td.label+'</span>'+
            '<span style="font-size:8px;color:'+bc.t+';text-transform:uppercase;font-weight:bold;letter-spacing:-0.02em">'+subLabel+'</span>'+
          '</div>'+
        '</div>';
      }).join('');
    }
  }catch(e){}
}

var _dtFilter='all';
var DT_CATEGORIES=[
  {key:'all',label:'ALL',color:'var(--primary)'},
  {key:'travel',label:'TRAVEL',color:'var(--primary)'},
  {key:'birthday',label:'BIRTHDAY',color:'var(--accent)'},
  {key:'assignment',label:'ASSIGNMENT',color:'var(--secondary)'},
  {key:'others',label:'OTHERS',color:'var(--text-inactive)'}
];
var DT_ICONS={travel:'flight_takeoff',birthday:'cake',assignment:'menu_book',others:'folder',default:'event'};
var DT_COLORS={travel:'var(--primary)',birthday:'var(--accent)',assignment:'var(--secondary)',others:'var(--text-inactive)',default:'var(--primary)'};

function renderDtFilters(){
  var c=document.getElementById('dayTrackerFilters');
  c.innerHTML=DT_CATEGORIES.map(function(f){
    var active=_dtFilter===f.key;
    var style=active
      ?'flex:1;min-width:60px;padding:10px 8px;background:var(--surface-container-low);color:'+f.color+';font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;font-weight:700;border:none;border-right:1px solid var(--border);text-transform:uppercase;letter-spacing:0.1em;cursor:pointer;text-align:center;display:flex;align-items:center;justify-content:center;gap:6px'
      :'flex:1;min-width:60px;padding:10px 8px;background:transparent;color:var(--text-inactive);font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;border:none;border-right:1px solid var(--border);text-transform:uppercase;letter-spacing:0.1em;cursor:pointer;text-align:center;transition:color 0.15s;display:flex;align-items:center;justify-content:center;gap:6px';
    var dot='<span style="width:5px;height:5px;border-radius:50%;background:'+f.color+';flex-shrink:0"></span>';
    return '<button onclick="setDtFilter(\''+f.key+'\')" style="'+style+'">'+dot+f.label+'</button>';
  }).join('');
}
function setDtFilter(f){_dtFilter=f;renderDtFilters();loadDayTrackerEntries()}

async function loadDayTrackerEntries(){
  renderDtFilters();
  var c=document.getElementById('dayTrackerContent');
  if(typeof renderSkeleton==='function') c.innerHTML=renderSkeleton('cards',3);
  else c.innerHTML='<div style="color:var(--muted);text-align:center;padding:20px">Loading...</div>';
  try{
    var r=await fetch(BASE+'/daytracker');
    var j=await r.json();
    var entries=j.entries||[];
    if(entries.length===0){
      c.innerHTML='<div style="color:var(--muted);text-align:center;padding:20px">No dates tracked yet</div>';
      return;
    }
    if(_dtFilter!=='all'){
      entries=entries.filter(function(e){
        return(e.category||'others').toLowerCase()===_dtFilter;
      });
    }
    entries.sort(function(a,b){return(b.pinned?'1':'0').localeCompare(a.pinned?'1':'0')||(b.date||'').localeCompare(a.date||'')});
    var html='<div style="display:flex;flex-direction:column;gap:8px">';
    entries.forEach(function(e){
      var td=_timeDiffLabel(e.date,e.hour);
      var cat=(e.category||'others').toLowerCase();
      var iconName=DT_ICONS[cat]||DT_ICONS.default;
      var color=DT_COLORS[cat]||DT_COLORS.default;
      var svgPath=_iconPaths[iconName];
      var iconHtml=svgPath?'<svg viewBox="0 -960 960 960" width="14" height="14" fill="'+color+'"><path d="'+svgPath+'"/></svg>':'<span style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;color:'+color+';text-transform:uppercase">'+iconName.replace(/_/g,' ')+'</span>';
      var catLabel=cat.toUpperCase();
      var dateDisplay=e.date+(e.hour!=null?' '+e.hour+':00':'');
      html+='<div style="border:1px solid var(--border);background:var(--surface-container-low);padding:12px;transition:border-color 0.2s">';
      html+='<div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:4px">';
      html+='<div style="display:flex;align-items:center;gap:8px">';
      html+=iconHtml;
      html+='<span style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;color:'+color+';text-transform:uppercase">'+catLabel+'</span>';
      html+='</div>';
      html+='<div style="display:flex;gap:8px;align-items:center">';
      html+='<span onclick="toggleDayTrackerPin(\''+e.id+'\','+(e.pinned?'false':'true')+')" style="cursor:pointer;font-size:14px;color:'+(e.pinned?'var(--yellow)':'var(--text-inactive)')+'">'+(e.pinned?'\uD83D\uDCCC':'\u25CB')+'</span>';
      html+='<span onclick="editDayTrackerEntry(\''+e.id+'\',\''+esc(e.title||'')+'\',\''+e.date+'\','+(e.hour!=null?e.hour:'null')+',\''+cat+'\')" style="cursor:pointer;color:var(--text-inactive);transition:color 0.15s"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></span>';
      html+='<span onclick="deleteDayTrackerEntry(\''+e.id+'\')" style="cursor:pointer;color:var(--text-inactive);transition:color 0.15s"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg></span>';
      html+='</div></div>';
      html+='<div style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:11px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em;margin-bottom:2px">'+esc(e.title||'Untitled')+'</div>';
      html+='<div style="display:flex;justify-content:space-between;align-items:baseline">';
      html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive)">'+dateDisplay+'</span>';
      html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:18px;color:'+td.dColor+'">'+td.label+'</span>';
      html+='</div></div>';
    });
    html+='</div>';
    c.innerHTML=html;
  }catch(e){
    c.innerHTML='<div style="color:var(--red);text-align:center;padding:20px">Failed to load</div>';
  }
}

function showAddDayTrackerModal(){
  var overlay=document.getElementById('modalOverlay');
  document.getElementById('modalTitle').textContent='Add Date';
  var catHtml='<label>Category</label><div style="display:flex;gap:8px;margin-bottom:8px">'+
    '<div class="dt-cat-btn" data-cat="travel" onclick="selectDtCat(this,\'travel\')" style="flex:1;padding:10px 6px;border:2px solid var(--border);text-align:center;cursor:pointer;font-family:\'Space Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em;transition:all 0.15s;display:flex;flex-direction:column;align-items:center;gap:4px"><span style="font-size:16px">✈</span>TRAVEL</div>'+
    '<div class="dt-cat-btn" data-cat="birthday" onclick="selectDtCat(this,\'birthday\')" style="flex:1;padding:10px 6px;border:2px solid var(--border);text-align:center;cursor:pointer;font-family:\'Space Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em;transition:all 0.15s;display:flex;flex-direction:column;align-items:center;gap:4px"><span style="font-size:16px">🎂</span>BIRTHDAY</div>'+
    '<div class="dt-cat-btn" data-cat="assignment" onclick="selectDtCat(this,\'assignment\')" style="flex:1;padding:10px 6px;border:2px solid var(--border);text-align:center;cursor:pointer;font-family:\'Space Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em;transition:all 0.15s;display:flex;flex-direction:column;align-items:center;gap:4px"><span style="font-size:16px">📚</span>ASSIGNMENT</div>'+
    '<div class="dt-cat-btn" data-cat="others" onclick="selectDtCat(this,\'others\')" style="flex:1;padding:10px 6px;border:2px solid var(--border);text-align:center;cursor:pointer;font-family:\'Space Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em;transition:all 0.15s;display:flex;flex-direction:column;align-items:center;gap:4px"><span style="font-size:16px">📁</span>OTHERS</div>'+
    '</div>';
  document.getElementById('modalFields').innerHTML=
    catHtml+
    '<label>Title</label><input id="dtTitle" placeholder="e.g. Birthday, Project Deadline">'+
    '<label>Date</label><input type="date" id="dtDate" value="'+localDate()+'">'+
    '<label>Hour</label><select id="dtHour">'+_hourOpts(new Date().getHours())+'</select>';
  _dtModalCat='others';
  var defBtn=document.querySelector('.dt-cat-btn[data-cat="others"]');
  if(defBtn)selectDtCat(defBtn,'others');
  document.getElementById('modalSubmit').onclick=async function(){
    var title=document.getElementById('dtTitle').value;
    var date=document.getElementById('dtDate').value;
    var hour=document.getElementById('dtHour').value;
    if(!date){toast('Date required');return}
    await fetch(BASE+'/daytracker',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({title:title||date,date:date,hour:parseInt(hour),category:_dtModalCat,pinned:false})});
    closeModal();loadDayTrackerEntries();loadDayTrackerPreview();
  };
  overlay.classList.add('show');
}

var _dtModalCat='others';
function selectDtCat(el,cat){
  _dtModalCat=cat;
  var colors={travel:'var(--primary)',birthday:'var(--accent-purple)',assignment:'var(--secondary)',others:'var(--text-inactive)'};
  var bgs={travel:'rgba(167,200,255,0.1)',birthday:'rgba(141,108,255,0.15)',assignment:'rgba(77,224,134,0.1)',others:'transparent'};
  document.querySelectorAll('.dt-cat-btn').forEach(function(b){
    b.style.border='2px solid var(--border)';b.style.color='var(--text-inactive)';b.style.background='transparent';
  });
  el.style.border='2px solid '+colors[cat];el.style.color=colors[cat];el.style.background=bgs[cat];
}

function editDayTrackerEntry(id,oldTitle,oldDate,oldHour,oldCat){
  var overlay=document.getElementById('modalOverlay');
  document.getElementById('modalTitle').textContent='Edit Date';
  var cat=oldCat||'others';
  var catHtml='<label>Category</label><div style="display:flex;gap:8px;margin-bottom:8px">'+
    '<div class="dt-cat-btn" data-cat="travel" onclick="selectDtCat(this,\'travel\')" style="flex:1;padding:10px 6px;border:2px solid var(--border);text-align:center;cursor:pointer;font-family:\'Space Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em;transition:all 0.15s;display:flex;flex-direction:column;align-items:center;gap:4px"><span style="font-size:16px">✈</span>TRAVEL</div>'+
    '<div class="dt-cat-btn" data-cat="birthday" onclick="selectDtCat(this,\'birthday\')" style="flex:1;padding:10px 6px;border:2px solid var(--border);text-align:center;cursor:pointer;font-family:\'Space Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em;transition:all 0.15s;display:flex;flex-direction:column;align-items:center;gap:4px"><span style="font-size:16px">🎂</span>BIRTHDAY</div>'+
    '<div class="dt-cat-btn" data-cat="assignment" onclick="selectDtCat(this,\'assignment\')" style="flex:1;padding:10px 6px;border:2px solid var(--border);text-align:center;cursor:pointer;font-family:\'Space Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em;transition:all 0.15s;display:flex;flex-direction:column;align-items:center;gap:4px"><span style="font-size:16px">📚</span>ASSIGNMENT</div>'+
    '<div class="dt-cat-btn" data-cat="others" onclick="selectDtCat(this,\'others\')" style="flex:1;padding:10px 6px;border:2px solid var(--border);text-align:center;cursor:pointer;font-family:\'Space Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em;transition:all 0.15s;display:flex;flex-direction:column;align-items:center;gap:4px"><span style="font-size:16px">📁</span>OTHERS</div>'+
    '</div>';
  document.getElementById('modalFields').innerHTML=
    catHtml+
    '<label>Title</label><input id="dtTitle" value="'+esc(oldTitle)+'">'+
    '<label>Date</label><input type="date" id="dtDate" value="'+oldDate+'">'+
    '<label>Hour</label><select id="dtHour">'+_hourOpts(oldHour)+'</select>';
  _dtModalCat=cat;
  var targetBtn=document.querySelector('.dt-cat-btn[data-cat="'+cat+'"]');
  if(targetBtn)selectDtCat(targetBtn,cat);
  document.getElementById('modalSubmit').onclick=async function(){
    var title=document.getElementById('dtTitle').value;
    var date=document.getElementById('dtDate').value;
    var hour=document.getElementById('dtHour').value;
    if(!date){toast('Date required');return}
    try{
      await fetch(BASE+'/daytracker',{method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify({id:id,title:title,date:date,hour:parseInt(hour),category:_dtModalCat})});
      loadDayTrackerEntries();loadDayTrackerPreview();closeModal();
    }catch(e){toast('Error')}
  };
  overlay.classList.add('show');
}

async function deleteDayTrackerEntry(id){
  if(!confirm('Delete this date?'))return;
  try{await fetch(BASE+'/daytracker?id='+id,{method:'DELETE'});loadDayTrackerEntries();loadDayTrackerPreview()}catch(e){}
}

async function toggleDayTrackerPin(id,pinned){
  try{
    await fetch(BASE+'/daytracker',{method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify({id:id,pinned:pinned})});
    loadDayTrackerEntries();loadDayTrackerPreview();
  }catch(e){}
}
