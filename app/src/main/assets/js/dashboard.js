// ─── Dashboard ────────────────────────────────────────────────────────────────

var _dashCache={},_dashboardLoading=false,_lastDashboardLoad=0;
function _dc(key,val){var h=JSON.stringify(val);if(_dashCache[key]===h)return false;_dashCache[key]=h;return true}

async function loadDashboard(){
  if(_dashboardLoading)return;
  _dashboardLoading=true;
  try{
    var f=function(p){return wrapFetch(fetch(BASE+p),'loadDashboard',{endpoint:p}).then(function(r){return r.json()})};
    var results=await Promise.allSettled([
      f('/gita/today'),
      f('/streak'),
      f('/tasks?filter=today'),
      f('/tasks?filter=overdue'),
      f('/session/today'),
      f('/events/mood/today'),
      f('/events/energy/today'),
      f('/sleep-up')]);
    function r(i){var x=results[i];return x.status==='fulfilled'?x.value:null}
    var gita=r(0),streak=r(1),tasksRes=r(2),overdueRes=r(3),sessionToday=r(4),moodToday=r(5),energyToday=r(6),sleepUp=r(7);
    if(!tasksRes)tasksRes={tasks:[]};if(!overdueRes)overdueRes={tasks:[]};if(!moodToday)moodToday={events:[]};if(!energyToday)energyToday={events:[]};
    if(_dc('gita',gita))renderGita(gita);
    var allMood=moodToday?.events||[];
    var latestMood=allMood.filter(function(e){return e.mood}).pop();
    var allEnergy=energyToday?.events||[];
    var latestEnergy=allEnergy.filter(function(e){return e.energy}).pop();
    if(_dc('streak',streak)){
      var streakVal=streak?.overall_streak||0;
      document.getElementById('streakBadge').textContent=streakVal>0?'\uD83D\uDD25 '+streakVal+'d':''
    }
    if(_dc('mood',latestMood)){
      if(latestMood&&latestMood.mood){
        document.getElementById('statMood').innerHTML=moodLabel(latestMood.mood)+' <span style="color:var(--text-inactive)" id="statMoodTime"></span>';
        var mt=new Date(latestMood.ts);var th=mt.getHours()%12||12;var tm=String(mt.getMinutes()).padStart(2,'0');var ap=mt.getHours()>=12?'pm':'am';
        var months=['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
        document.getElementById('statMoodTime').textContent='@ '+th+':'+tm+ap+' '+String(mt.getDate()).padStart(2,'0')+'.'+months[mt.getMonth()].slice(0,3).toUpperCase()+'.'+String(mt.getFullYear()).slice(2);
      }
    }
    if(_dc('energy',latestEnergy)){
      if(latestEnergy&&latestEnergy.energy){
        document.getElementById('statEnergy').innerHTML=energyLabel(latestEnergy.energy)+' <span style="color:var(--text-inactive)" id="statEnergyTime"></span>';
        var et=new Date(latestEnergy.ts);var eh=et.getHours()%12||12;var em=String(et.getMinutes()).padStart(2,'0');var ep=et.getHours()>=12?'pm':'am';
        var months=['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
        document.getElementById('statEnergyTime').textContent='@ '+eh+':'+em+ep+' '+String(et.getDate()).padStart(2,'0')+'.'+months[et.getMonth()].slice(0,3).toUpperCase()+'.'+String(et.getFullYear()).slice(2);
      }
    }
    if(_dc('sleep',sleepUp)){
      if(sleepUp){
        var sleepDur=sleepUp.sleep_minutes||0;
        var sleepH=Math.floor(sleepDur/60);var sleepM=sleepDur%60;
        var sl=document.getElementById('statSleepTotal');if(sl)sl.textContent=sleepUp.sleep_formatted||(sleepH>0?sleepH+'h '+sleepM+'m':sleepM+'m');
        var statusLabel='--';
        if(sleepUp.status==='checked_in'){
          statusLabel='\u{1F4A4} Asleep';
          if(sleepUp.up_formatted)statusLabel+=' ('+sleepUp.up_formatted+' up)';
        }else if(sleepUp.status==='checked_out'){
          statusLabel='\u2600\uFE0F Awake';
          if(sleepUp.up_formatted)statusLabel+=' ('+sleepUp.up_formatted+' up)';
        }
        var ss=document.getElementById('statSleepStatus');if(ss)ss.textContent=statusLabel;
      }
    }
    if(_dc('session',sessionToday)){
      if(sessionToday){
        var totalMin=sessionToday.total_minutes||0;
        var sH=Math.floor(totalMin/60);var sM=totalMin%60;
        var sText=sH>0?sH+'h '+sM+'m':sM+'m';
        if(sessionToday.active){sText+=' \u00b7 Active '+sessionToday.active_minutes+'m'}
        var st=document.getElementById('statSessionTotal');if(st)st.textContent=sessionToday.session_count>0?sText:'--';
        var sc=document.getElementById('statSessionCount');if(sc)sc.textContent=sessionToday.session_count+' session'+(sessionToday.session_count!==1?'s':'');
      }
    }
    var tasks=tasksRes?.tasks||[];
    if(_dc('tasks',tasks)){
      var tc=document.getElementById('tasksCard');
      var tpl=document.getElementById('taskProgressLabel');
      var tpb=document.getElementById('taskProgressBarContainer');
      var tpf=document.getElementById('taskProgressBarFill');
      var tpc=document.getElementById('taskProgressCount');
      var done=tasks.filter(function(t){return t.status==='done'}).length;
      if(tpl)tpl.textContent=done+'/'+tasks.length;
      if(tpb&&tasks.length>0){tpb.style.display='';if(tpc)tpc.textContent=done+' / '+tasks.length;if(tpf)tpf.style.width=Math.round((done/tasks.length)*100)+'%';}
      if(tasks.length===0){
        tc.innerHTML='<div style="color:var(--text-inactive);font-size:13px">No tasks due today</div>';
      }else{
        tc.innerHTML=tasks.map(function(t){
          var isDone=t.status==='done';
          var circle=isDone?'<div class="task-toggle ok" onclick="toggleTask(\''+t.id+'\')">&#10003;</div>':'<div class="task-toggle" onclick="toggleTask(\''+t.id+'\')"></div>';
          var pinBtn='<span onclick="pinTask(\''+t.id+'\','+(t.pinned?'false':'true')+',event)" style="cursor:pointer;font-size:16px;color:'+(t.pinned?'var(--yellow)':'var(--muted)')+';margin-left:4px;padding:4px 2px">'+(t.pinned?'\uD83D\uDCCC':'\u25CB')+'</span>';
          return '<div class="task-row">'+circle+'<span class="task-title'+(isDone?' task-done':'')+'">'+(t.priority==='high'?'\u{1F534}':t.priority==='medium'?'\u{1F7E1}':'\u{1F7E2}')+' '+t.title+(t.deadline_time?' @'+t.deadline_time:'')+'</span>'+pinBtn+'<span style="color:var(--muted);flex-shrink:0;font-size:11px">'+t.deadline+'</span></div>'
        }).join('')
      }
    }
    var overdue=overdueRes?.tasks||[];
    if(_dc('overdue',overdue)){
      var nas=document.getElementById('needsAttentionSection');
      if(overdue.length===0){
        if(nas)nas.style.display='none';
      }else{
        if(nas){nas.style.display='block';document.getElementById('attentionText').textContent=overdue.length+' task'+(overdue.length>1?'s':'')+' overdue — review required';}
      }
    }
    loadFeed();
    if(typeof updateDashboardData === 'function'){
      updateDashboardData({
        tasks: tasks,
        streak: streak?.overall_streak || 0,
        mood: latestMood,
        energy: latestEnergy,
        sleep: sleepUp
      });
    }
    _lastDashboardLoad=Date.now();
  }catch(e){}
  _dashboardLoading=false;
}

async function toggleTask(id){
  var tc = document.getElementById('tasksCard');
  if(tc){
    var row = Array.from(tc.querySelectorAll('.task-row')).find(function(r){
      return r.querySelector('[onclick*="' + id + '"]');
    });
    if(row){
      var toggle = row.querySelector('.task-toggle');
      var title = row.querySelector('.task-title');
      if(toggle && title){
        var willBeDone = !toggle.classList.contains('ok');
        toggle.classList.toggle('ok', willBeDone);
        toggle.innerHTML = willBeDone ? '&#10003;' : '';
        title.classList.toggle('task-done', willBeDone);
        var doneRows = tc.querySelectorAll('.task-toggle.ok').length;
        var allRows = tc.querySelectorAll('.task-row').length;
        var tpl = document.getElementById('taskProgressLabel');
        var tpf = document.getElementById('taskProgressBarFill');
        var tpc = document.getElementById('taskProgressCount');
        if(tpl) tpl.textContent = doneRows + '/' + allRows;
        if(tpc) tpc.textContent = doneRows + ' / ' + allRows;
        if(tpf && allRows > 0) tpf.style.width = Math.round((doneRows / allRows) * 100) + '%';
      }
    }
  }
  try{
    var data=await wrapFetch(fetch(BASE+'/task/'+id+'/done',{method:'POST'}),'toggleTask',{taskId:id}).then(function(r){return r.json()});
    if(data.ok)toast('Toggled done!');
  }catch(e){
    toast('Error syncing task');
    loadDashboard();
  }
}

var _feedCache='';
async function loadFeed(){
  try{
    var feedResults=await Promise.allSettled([
      wrapFetch(fetch(BASE+'/events/thought/today'),'loadFeed',{endpoint:'/events/thought/today'}).then(function(r){return r.json()}),
      wrapFetch(fetch(BASE+'/events/journal/today'),'loadFeed',{endpoint:'/events/journal/today'}).then(function(r){return r.json()}),
      wrapFetch(fetch(BASE+'/events/running/today'),'loadFeed',{endpoint:'/events/running/today'}).then(function(r){return r.json()}),
      wrapFetch(fetch(BASE+'/events/gita/today'),'loadFeed',{endpoint:'/events/gita/today'}).then(function(r){return r.json()})]);
    var thoughts=feedResults[0].status==='fulfilled'?feedResults[0].value:null;
    var journals=feedResults[1].status==='fulfilled'?feedResults[1].value:null;
    var runs=feedResults[2].status==='fulfilled'?feedResults[2].value:null;
    var gitas=feedResults[3].status==='fulfilled'?feedResults[3].value:null;
    var items=[];
    (thoughts?.events||[]).forEach(function(e){
      var topic=e.topic?' ('+esc(e.topic)+')':'';
      items.push({ts:e.ts,html:'<span style="color:var(--accent);text-transform:uppercase;margin-right:8px">THINK:</span>'+esc((e.text||'').slice(0,60))+topic});
    });
    (journals?.events||[]).forEach(function(e){
      var label=e.subtype==='win'?'<span style="color:var(--yellow);text-transform:uppercase;margin-right:8px">WIN:</span>':e.subtype==='lesson'?'<span style="color:var(--green);text-transform:uppercase;margin-right:8px">LESSON:</span>':'<span style="color:var(--green);text-transform:uppercase;margin-right:8px">JOURNAL:</span>';
      items.push({ts:e.ts,html:label+esc((e.text||'').slice(0,60))});
    });
    (runs?.events||[]).forEach(function(e){
      var secs=e.duration_min||0;var m=Math.floor(secs/60);var s=secs%60;
      var durStr=m+':'+(s<10?'0':'')+s;
      var pace=e.pace||'';
      items.push({ts:e.ts,html:'<span style="color:var(--green);text-transform:uppercase;margin-right:8px">RUN:</span>'+((e.distance_km||0)+' km')+' in '+durStr+(pace?' ('+pace+'/km)':'')+' ('+((e.run_type)||'')+')'});
    });
    (gitas?.events||[]).forEach(function(e){
      items.push({ts:e.ts,html:'<span style="color:var(--accent);text-transform:uppercase;margin-right:8px">GITA:</span>Read '+(e.chapter||'')+'.'+(e.verse||'')});
    });
    items.sort(function(a,b){return(b.ts||'').localeCompare(a.ts||'')});
    var feedHtml=items.length===0?'<div style="padding:6px 0;color:var(--text-inactive);opacity:0.5;font-style:italic">Waiting for new input...</div>':items.slice(0,5).map(function(i){
      var ts=i.ts?new Date(i.ts):null;
      var timeStr='';
      if(ts){var h=ts.getHours()%12||12;var m=String(ts.getMinutes()).padStart(2,'0');timeStr='['+h+':'+m+']';}
      return '<div style="display:flex;align-items:flex-start;gap:12px;padding:6px 0;border-bottom:1px solid rgba(27,27,27,0.5)"><span style="color:var(--text-inactive);white-space:nowrap">'+timeStr+'</span><span style="color:var(--text)">'+i.html+'</span></div>';
    }).join('');
    if(_feedCache!==feedHtml){_feedCache=feedHtml;document.getElementById('feedCard')&&(document.getElementById('feedCard').innerHTML=feedHtml)}
    if(typeof updateDashboardData === 'function'){
      updateDashboardData({ feed: items });
    }
  }catch(e){}
}

var td=document.getElementById('todayDate');if(td)td.textContent=new Date().toLocaleDateString('en-US',{weekday:'short',month:'short',day:'numeric'});
loadDashboard();
loadScreenTime(true);
loadDayTrackerPreview();
setInterval(loadDashboard,30000);
var _lastScreenTimeLoad=Date.now();
setInterval(function(){_lastScreenTimeLoad=Date.now();loadScreenTime(true)},5*60*1000);
if('serviceWorker'in navigator){navigator.serviceWorker.getRegistrations().then(function(regs){regs.forEach(function(r){r.unregister()})});caches.keys().then(function(ks){ks.forEach(function(k){caches.delete(k)})})}
document.addEventListener('visibilitychange',function(){if(!document.hidden){loadSessionStatus();if(Date.now()-_lastDashboardLoad>15000){loadDashboard();loadScreenTime(true);loadDayTrackerPreview()}if(Date.now()-_lastScreenTimeLoad>5*60*1000){_lastScreenTimeLoad=Date.now();loadScreenTime(true)}}})
