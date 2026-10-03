// ─── Running Page & Pin Functions ───

// ─── Running Page ─────────────────────────────────────────────────────────────

var RUN_TYPE_COLORS={easy:'var(--green)',tempo:'var(--yellow)',interval:'var(--red)',long:'var(--accent2)',race:'var(--accent)'};
var RUN_TYPE_LABELS={easy:'Easy',tempo:'Tempo',interval:'Interval',long:'Long',race:'Race'};

var _runWeeks=4;

async function openRunningPage(){
  document.getElementById('mainFab').style.display='none';_pushOv();
  _runWeeks=4;
  var el=document.getElementById('runningPage');el.scrollTop=0;el.classList.add('show');
  var c=document.getElementById('runningContent');
  if(typeof renderSkeleton==='function') c.innerHTML=renderSkeleton('grid',4)+renderSkeleton('list',4);
  else c.innerHTML='<div style="color:var(--muted);font-size:14px;text-align:center;padding:20px;">Loading...</div>';
  loadRunPage();
}

function setRunWeeks(n){
  _runWeeks=n;
  var c=document.getElementById('runningContent');
  if(typeof renderSkeleton==='function') c.innerHTML=renderSkeleton('grid',4)+renderSkeleton('list',4);
  else c.innerHTML='<div style="color:var(--muted);font-size:14px;text-align:center;padding:20px;">Loading...</div>';
  loadRunPage();
}

async function loadRunPage(){
  var c=document.getElementById('runningContent');
  try{
    var r=await fetch(BASE+'/running/stats?weeks='+_runWeeks);
    if(!r.ok)throw new Error('HTTP '+r.status+' '+r.statusText);
    var j=await r.json();
    if(!j.weekly)throw new Error('Missing weekly field');
    var wk=j.weekly;
    var mo=j.monthly;
    var period=j.period;
    var daily=j.daily||[];
    var recent=j.recent_runs||[];
    var html='';

    function fmtTime(sec){
      var h=Math.floor(sec/3600);
      var m=Math.floor((sec%3600)/60);
      var s=sec%60;
      if(h>0)return h+'h '+m+'m '+s+'s';
      return m+'m '+s+'s';
    }
    var RUN_COLORS={easy:'var(--secondary)',tempo:'var(--accent-orange)',interval:'var(--red)',long:'var(--primary)',race:'var(--red)'};
    var RUN_LABELS={easy:'EASY',tempo:'TEMPO',interval:'INTERVAL',long:'LONG',race:'RACE'};

    html+='<button onclick="closeRunningPage();openModal(\'run\')" style="width:100%;height:44px;border:1px solid var(--border);background:var(--surface);display:flex;align-items:center;justify-content:center;gap:6px;cursor:pointer;transition:background 0.15s;margin-bottom:16px"><span style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:11px;color:var(--text);text-transform:uppercase;letter-spacing:0.15em">+ LOG RUN</span></button>';

    html+='<div style="display:flex;gap:4px;margin-bottom:16px">';
    var _filters=[{v:1,l:'1W'},{v:2,l:'2W'},{v:4,l:'1M'},{v:12,l:'3M'}];
    _filters.forEach(function(f){
      var sel=f.v===_runWeeks;
      html+='<button onclick="setRunWeeks('+f.v+')" style="flex:1;height:30px;border:1px solid '+(sel?'var(--primary)':'var(--border)')+';background:'+(sel?'var(--primary)':'var(--surface)')+';color:'+(sel?'var(--bg)':'var(--text)')+';cursor:pointer;font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;text-transform:uppercase;letter-spacing:0.1em;border-radius:4px">'+f.l+'</button>';
    });
    html+='</div>';

    var pRuns = period ? (period.runs || 0) : (wk.runs || 0);
    var pKm = period ? (period.km || 0) : (wk.km || 0);
    var pSec = period ? (period.seconds || 0) : (wk.seconds || 0);
    var pPace = j.avg_pace || '--:--';

    html+='<div style="display:grid;grid-template-columns:1fr 1fr;gap:1px;background:var(--border);margin-bottom:8px">';
    var stats=[
      {label:'DISTANCE ('+_runWeeks+'W)',value:pKm+'<span style="font-size:11px;margin-left:3px;color:var(--text-inactive)">KM</span>',color:'var(--primary)'},
      {label:'RUNS ('+_runWeeks+'W)',value:String(pRuns).padStart(2,'0'),color:'var(--text)'},
      {label:'AVG PACE',value:pPace+'<span style="font-size:11px;margin-left:3px;color:var(--text-inactive)">/KM</span>',color:'var(--secondary)'},
      {label:'TOTAL TIME',value:fmtTime(pSec),color:'var(--accent-orange)'}
    ];
    stats.forEach(function(s){
      html+='<div style="background:var(--surface);padding:10px">';
      html+='<span style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em;display:block;margin-bottom:6px">'+s.label+'</span>';
      html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:20px;font-weight:500;color:'+s.color+'">'+s.value+'</span>';
      html+='</div>';
    });
    html+='</div>';

    html+='<div style="border:1px solid var(--border);background:var(--surface);padding:8px 12px;margin-bottom:20px;display:flex;justify-content:space-between;align-items:center;font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.05em">';
    html+='<span>THIS WEEK: <b style="color:var(--primary)">'+(wk.km||0)+' KM</b> ('+(wk.runs||0)+' runs)</span>';
    html+='<span>THIS MONTH: <b style="color:var(--primary)">'+(mo.km||0)+' KM</b> ('+(mo.runs||0)+' runs)</span>';
    html+='</div>';

    if(daily.length>0){
      html+='<div style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em;margin-bottom:8px">WEEKLY BREAKDOWN</div>';
      html+='<div style="overflow-x:auto;margin-bottom:24px;border:1px solid var(--border)">';
      html+='<table style="width:100%;border-collapse:collapse;font-size:11px">';
      html+='<thead style="background:var(--surface-container-low)"><tr style="color:var(--text-inactive);text-align:left;border-bottom:1px solid var(--border)">';
      html+='<th style="padding:8px 4px;text-transform:uppercase;font-weight:500">DATE</th>';
      html+='<th style="padding:8px 4px;text-transform:uppercase;font-weight:500">RUNS</th>';
      html+='<th style="padding:8px 4px;text-transform:uppercase;font-weight:500;text-align:right">KM</th>';
      html+='<th style="padding:8px 4px;text-transform:uppercase;font-weight:500;text-align:right">TIME</th>';
      html+='<th style="padding:8px 4px;text-transform:uppercase;font-weight:500;text-align:right">PACE</th>';
      html+='</tr></thead><tbody>';
      daily.forEach(function(d){
        var dayName=new Date(d.date+'T00:00:00').toLocaleDateString('en-US',{weekday:'short'}).toUpperCase();
        html+='<tr style="border-bottom:1px solid var(--border);color:var(--text)">';
        html+='<td style="padding:8px 4px;font-family:\'Space Mono\',\'Mono\',monospace">'+dayName+' '+d.date.slice(5).replace(/-/g,'.')+'</td>';
        html+='<td style="padding:8px 4px;font-family:\'JetBrains Mono\',\'Mono\',monospace">'+d.count+'</td>';
        html+='<td style="padding:8px 4px;text-align:right;font-family:\'JetBrains Mono\',\'Mono\',monospace">'+d.total_km+'</td>';
        html+='<td style="padding:8px 4px;text-align:right;font-family:\'JetBrains Mono\',\'Mono\',monospace">'+fmtTime(d.total_sec)+'</td>';
        html+='<td style="padding:8px 4px;text-align:right;color:var(--primary);font-family:\'JetBrains Mono\',\'Mono\',monospace">'+d.avg_pace+'</td>';
        html+='</tr>';
      });
      html+='</tbody></table></div>';
    }

    if(recent.length>0){
      html+='<div style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em;margin-bottom:8px">RECENT RUNS</div>';
      html+='<div style="display:flex;flex-direction:column;gap:8px">';
      recent.forEach(function(run){
        var color=RUN_COLORS[run.run_type]||'var(--text-inactive)';
        var typeLabel=RUN_LABELS[run.run_type]||(run.run_type||'').toUpperCase();
        var runDate=run.ts?run.ts.slice(0,10).replace(/-/g,'.')+' \u00b7 '+run.ts.slice(11,16):'';
        html+='<div style="background:var(--surface);border:1px solid var(--border);border-left:4px solid '+color+';padding:8px;display:flex;justify-content:space-between;align-items:center;transition:background 0.15s">';
        html+='<div>';
        html+='<div style="display:flex;align-items:center;gap:8px;margin-bottom:4px">';
        html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:18px;font-weight:600;color:var(--text)">'+run.distance_km+' KM</span>';
        html+='<span style="font-size:10px;padding:2px 8px;border:1px solid '+color+';color:'+color+';background:'+color+'/10;border-radius:9999px;text-transform:uppercase;font-family:\'Space Mono\',\'Mono\',monospace;font-weight:500">'+typeLabel+'</span>';
        html+='</div>';
        html+='<span style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em">'+runDate+'</span>';
        html+='</div>';
        html+='<div style="text-align:right">';
        html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:18px;font-weight:500;color:var(--text);display:block">'+fmtTime(run.duration_sec||0)+'</span>';
        html+='<span style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive);text-transform:uppercase">'+run.pace+' /KM</span>';
        html+='</div>';
        html+='</div>';
      });
      html+='</div>';
    }

    if(recent.length===0&&daily.length===0){
      html+='<div style="display:flex;flex-direction:column;align-items:center;justify-content:center;padding:48px 20px;opacity:0.3"><span class="material-symbols-outlined" style="font-size:40px;margin-bottom:12px">directions_run</span><span style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:11px;text-transform:uppercase;letter-spacing:0.3em">NO RUNS LOGGED YET</span></div>';
    }

    c.innerHTML=html;
  }catch(e){
    c.innerHTML='<div style="color:var(--red);text-align:center;padding:20px">Failed to load running stats<br><span style="font-size:11px;color:var(--muted)">'+esc(e.message||e)+'</span></div>';
  }
}
function closeRunningPage(){document.getElementById('mainFab').style.display='flex';document.getElementById('runningPage').classList.remove('show')}

// ─── Pin Functions ──────────────────────────────────────────────────────────

async function pinTask(id,pinned,e,cb){
  if(e)e.stopPropagation();
  try{await fetch(BASE+'/task/'+id+'/pin?pinned='+pinned,{method:'POST'});loadDashboard();if(typeof cb==='function')cb();}catch(e){}
}

async function pinThought(id,pinned){
  try{await fetch(BASE+'/thought/pin?thought_id='+id+'&pinned='+pinned,{method:'POST'});openThoughtsPage();loadDashboard();loadDayTrackerPreview()}catch(e){}
}
