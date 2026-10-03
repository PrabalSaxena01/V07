// ─── Sessions ───
var timerSeconds=1500,timerRemaining=1500,timerRunning=false,timerInterval=null,timerMode='countdown';
try{var _savedTs=parseInt(localStorage.getItem('sessTimerTs'))||0;if(_savedTs>0&&Date.now()-_savedTs<10000){var _savedRemaining=parseInt(localStorage.getItem('sessTimerRemaining'))||0;if(_savedRemaining>0){timerRemaining=Math.max(0,_savedRemaining-Math.floor((Date.now()-_savedTs)/1000));timerSeconds=parseInt(localStorage.getItem('sessTimerSeconds'))||timerSeconds;timerRunning=localStorage.getItem('sessTimerRunning')==='1';if(timerRunning&&!timerInterval){timerStart()}}}}catch(e){}
window.addEventListener('beforeunload',function(){try{localStorage.setItem('sessTimerRemaining',timerRemaining);localStorage.setItem('sessTimerSeconds',timerSeconds);localStorage.setItem('sessTimerRunning',timerRunning?'1':'0');localStorage.setItem('sessTimerTs',Date.now())}catch(e){}})
var spb=null;

function formatTime(s){var m=Math.floor(s/60);var sec=s%60;return String(m).padStart(2,'0')+':'+String(sec).padStart(2,'0')}

function updateTimerDisplay(){
  var el=document.getElementById('timerDisplay');
  if(timerMode==='stopwatch'){
    el.textContent=formatTime(timerRemaining);
    el.style.color='var(--green)';
  }else{
    el.textContent=formatTime(timerRemaining);
    el.style.color=timerRemaining<=60&&timerRemaining>0?'var(--red)':'var(--text)';
  }
  // Update home focus timer
  _setHomeTimer(timerRemaining,timerMode==='countdown');
  if(spb&&timerMode==='countdown'&&timerSeconds>0){spb.style.width=Math.round(((timerSeconds-timerRemaining)/timerSeconds)*100)+'%';}
}
function _setHomeTimer(totalSec,isCountdown){
  var h=document.getElementById('timerHH'),m=document.getElementById('timerMM'),s=document.getElementById('timerSS');
  if(!h)return;
  var hrs=Math.floor(totalSec/3600);var mins=Math.floor((totalSec%3600)/60);var secs=totalSec%60;
  h.textContent=String(hrs).padStart(2,'0');m.textContent=String(mins).padStart(2,'0');s.textContent=String(secs).padStart(2,'0');
  var warn=isCountdown&&totalSec<=60&&totalSec>0;
  var col=warn?'var(--red)':'var(--primary)';
  h.style.color=col;m.style.color=col;s.style.color=col;
}

function setTimerMode(mode){
  if(timerRunning)return;
  timerMode=mode;localStorage.setItem('sessTimerMode',mode);
  var cd=document.getElementById('modeCountdown');
  var sw=document.getElementById('modeStopwatch');
  if(mode==='countdown'){
    cd.style.borderBottom='2px solid var(--primary)';cd.style.color='var(--primary)';
    sw.style.borderBottom='2px solid transparent';sw.style.color='var(--text-inactive)';
  }else{
    sw.style.borderBottom='2px solid var(--primary)';sw.style.color='var(--primary)';
    cd.style.borderBottom='2px solid transparent';cd.style.color='var(--text-inactive)';
  }
  document.getElementById('timerPresets').style.display=mode==='stopwatch'?'none':'';
  timerReset();
}

function setTimer(m,s){timerRunning=false;if(timerInterval){clearInterval(timerInterval);timerInterval=null}
  timerSeconds=m*60+s;timerRemaining=timerSeconds;updateTimerDisplay()}

async function customTimer(){var inp=await customPrompt('How many minutes for the countdown?');if(inp===null)return;var m=parseInt(inp)||0;var s=parseInt(await customPrompt('And how many seconds?'))||0;if(m===0&&s===0)return;setTimer(m,s)}

function timerTick(){
  if(timerMode==='stopwatch'){
    timerRemaining++;
    updateTimerDisplay();
  }else{
    if(timerRemaining<=0){
      timerRunning=false;if(timerInterval){clearInterval(timerInterval);timerInterval=null}
      updateTimerDisplay();
      if('Notification' in window && Notification.permission==='granted'){
        new Notification('Timer Done!',{body:'Time is up.'});
      }
      toast('Time\'s up!');
      try{fetch(BASE+'/session/stop',{method:'POST'}).then(function(){loadSessionsPage()})}catch(e){}
      return;
    }
    timerRemaining--;
    updateTimerDisplay();
  }
}

function timerStart(){
  if(timerRunning)return;
  if(timerMode==='countdown'&&timerSeconds<=0){setTimer(25,0)}
  timerRunning=true;
  timerInterval=setInterval(timerTick,1000)
}

function timerPause(){timerRunning=false;if(timerInterval){clearInterval(timerInterval);timerInterval=null}}

function timerReset(){timerRunning=false;if(timerInterval){clearInterval(timerInterval);timerInterval=null}
  timerRemaining=timerMode==='stopwatch'?0:timerSeconds;updateTimerDisplay();
  document.getElementById('modeCountdown').disabled=false;document.getElementById('modeStopwatch').disabled=false}

async function sessionStart(){
  var activity=document.getElementById('sessActivity').value;
  var isCustom=false;
  if(activity==='__custom__'){
    activity=document.getElementById('customActivityInput').value.trim();
    if(!activity){toast('Enter a custom activity name');return}
    isCustom=true;
  }
  var label=document.getElementById('sessLabel').value.trim()||null;
  if(!activity){toast('Select an activity');return}
  try{
    var existing=await fetch(BASE+'/session/active').then(function(r){return r.json()});
    if(existing.active||existing.paused){toast('Session already active — stop it first');return}
    var r=await fetch(BASE+'/session/start',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({activity:activity,custom_label:label,timer_mode:timerMode,timer_seconds:timerMode==='countdown'?timerSeconds:0})});
    var j=await r.json();
    if(j.ok){
      if(isCustom){
        try{await fetch(BASE+'/session/add-custom-activity',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({activity:activity})})}catch(e2){}
        _sessActivityItems=[]
      }
      toast('Session started!');
      loadSessionStatus()
    }
    else if(j.detail) toast(j.detail)
  }catch(e){toast('Error')}
}

function toggleCustomActivity(){
  document.getElementById('customActivityWrap').style.display=document.getElementById('sessActivity').value==='__custom__'?'':'none';
}

async function loadSessionStatus(){
  try{
    var r=await fetch(BASE+'/session/active');
    var j=await r.json();
    var pauseBtn=document.getElementById('sessPauseBtn');
    var resumeBtn=document.getElementById('sessResumeBtn');
    var stopBtn=document.getElementById('sessStopBtn');
    var statusText=document.getElementById('sessionStatusText');
    var modeCd=document.getElementById('modeCountdown');
    var modeSw=document.getElementById('modeStopwatch');
    if(j.active||j.paused){
      var s=j.session||{};
      var sessMode=s.mode||'stopwatch';
      var targetMs=s.target_duration||0;
      var elapsedMs=s.elapsed_ms||0;
      var remainingSecs=sessMode==='countdown'&&targetMs>0?Math.max(Math.floor((targetMs-elapsedMs)/1000),0):0;
      var elapsedSecs=Math.floor(elapsedMs/1000);
      var sal=document.getElementById('sessionActivityLabel');
      if(j.active){
        pauseBtn.style.display='';resumeBtn.style.display='none';stopBtn.style.display='';
        statusText.textContent='Active: '+activityLabel(s.activity)+(j.elapsed_minutes!=null?' ('+j.elapsed_minutes+'m)':'');
        if(sal)sal.textContent=activityLabel(s.activity);
        if(sessMode==='countdown'&&targetMs>0){
          timerMode='countdown';timerSeconds=Math.floor(targetMs/1000);timerRemaining=remainingSecs;updateTimerDisplay();
          modeCd.className='btn btn-accent';modeSw.className='btn';
          document.getElementById('timerPresets').style.display='';
        }else{
          timerMode='stopwatch';timerRemaining=elapsedSecs;updateTimerDisplay();
          modeCd.className='btn';modeSw.className='btn btn-accent';
          document.getElementById('timerPresets').style.display='none';
        }
        modeCd.disabled=true;modeSw.disabled=true;
        timerRunning=false;if(timerInterval){clearInterval(timerInterval);timerInterval=null}
        timerStart();
      }else{
        pauseBtn.style.display='none';resumeBtn.style.display='';stopBtn.style.display='';
        statusText.textContent='Paused: '+activityLabel(s.activity)+(j.elapsed_minutes!=null?' ('+j.elapsed_minutes+'m)':'');
        if(sal)sal.textContent=activityLabel(s.activity);
        if(sessMode==='countdown'&&targetMs>0){
          timerMode='countdown';timerSeconds=Math.floor(targetMs/1000);timerRemaining=remainingSecs;updateTimerDisplay();
          modeCd.className='btn btn-accent';modeSw.className='btn';
          document.getElementById('timerPresets').style.display='';
        }else{
          timerMode='stopwatch';timerRemaining=elapsedSecs;updateTimerDisplay();
          modeCd.className='btn';modeSw.className='btn btn-accent';
          document.getElementById('timerPresets').style.display='none';
        }
        modeCd.disabled=true;modeSw.disabled=true;
        if(timerRunning)timerPause();
      }
      // Update home page focus timer
      var sab=document.getElementById('sessionActiveBadge');
      var spid=document.getElementById('sessionPid');
      spb=document.getElementById('sessionProgressBar');
      if(sab)sab.textContent=j.active?'● ACTIVE':'◉ PAUSED';
      if(spid){var pidHash=s.id?s.id.slice(-4):String(Math.floor(Math.random()*9000)+1000);spid.textContent='PID '+pidHash;}
      _setHomeTimer(j.active?timerRemaining:(timerRemaining),sessMode==='countdown');
      if(spb){
        if(sessMode==='countdown'&&timerSeconds>0){spb.style.width=Math.round(((timerSeconds-timerRemaining)/timerSeconds)*100)+'%';spb.style.background='var(--green)';}
        else{spb.style.width='100%';spb.style.background='var(--primary)';}
      }
    }else{
      pauseBtn.style.display='none';resumeBtn.style.display='none';stopBtn.style.display='none';
      statusText.textContent='';
      modeCd.disabled=false;modeSw.disabled=false;
      var sal=document.getElementById('sessionActivityLabel');if(sal)sal.textContent='NO SESSION';
      var savedMode=localStorage.getItem('sessTimerMode')||'countdown';timerMode=savedMode;setTimerMode(savedMode);if(savedMode==='countdown'){setTimer(25,0)}else{setTimer(0,0)}
      var sab=document.getElementById('sessionActiveBadge');
      var spid=document.getElementById('sessionPid');
      spb=document.getElementById('sessionProgressBar');
      if(sab)sab.textContent='';
      _setHomeTimer(0,false);
      if(spb){spb.style.width='0%';}
    }
  }catch(e){}
}

async function sessionPause(){
  try{
    await fetch(BASE+'/session/pause',{method:'POST'});
    if(timerRunning)timerPause();
    toast('Paused');
    loadSessionStatus()
  }catch(e){toast('Error')}
}
async function sessionResume(){
  try{
    await fetch(BASE+'/session/resume',{method:'POST'});
    toast('Resumed');
    loadSessionStatus()
  }catch(e){toast('Error')}
}
async function sessionStop(){
  try{
    await fetch(BASE+'/session/stop',{method:'POST'});
    toast('Stopped');
    loadSessionStatus();
    loadSessionsPage()
  }catch(e){toast('Error')}
}

async function deleteSessionEvent(category,eventId){
  if(!confirm('Move to recycle bin?'))return;
  try{
    var r=await fetch(BASE+'/event/'+category+'/'+eventId,{method:'DELETE'});
    var j=await r.json();
    if(j.ok||j.recycled){toast('Moved to recycle bin');loadSessionsPage()}
    else if(j.detail) toast(j.detail)
  }catch(e){toast('Error')}
}

async function deleteSessionPair(startId,endId){
  if(!confirm('Move to recycle bin?'))return;
  try{
    var r1=await fetch(BASE+'/event/sessions/'+startId,{method:'DELETE'});
    var j1=await r1.json();
    var ok=!!(j1.ok||j1.recycled);
    if(endId){
      var r2=await fetch(BASE+'/event/sessions/'+endId,{method:'DELETE'});
      var j2=await r2.json();
      ok=ok||!!(j2.ok||j2.recycled);
    }
    if(ok){toast('Moved to recycle bin');loadSessionsPage()}
  }catch(e){toast('Error')}
}

async function loadSessionsPage(){
  loadSessActivities();
  loadSessionStatus();
  var d=document.getElementById('sessDate').value||localDate();
  try{
    var data=await wrapFetch(fetch(BASE+'/day/'+d),'sessionsPage',{date:d}).then(function(r){return r.json()});
    var list=document.getElementById('sessionList');
    var sessions=data.events.filter(function(e){return e._category==='sessions'});
    if(sessions.length===0){list.innerHTML='<div style="color:var(--text-inactive);font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:12px;text-align:center;padding:12px">No sessions recorded for this day</div>';return}
    var pairs=[];
    var openStart=null;
    sessions.forEach(function(e){
      if(e.type==='start'){openStart=e}
      else if(e.type==='end'&&openStart){
        var dur=e.duration||0;
        var mins=Math.floor(dur);var secs=Math.round((dur-mins)*60);
        pairs.push({start:openStart,end:e,activity:openStart.activity||e.activity,label:openStart.custom_label||'',duration:mins,start_ts:openStart.ts});
        openStart=null
      }
    });
    if(openStart){
      var elapsed=Math.floor((Date.now()-new Date(openStart.ts).getTime())/60000);
      pairs.push({start:openStart,end:null,activity:openStart.activity,label:openStart.custom_label||'',duration:elapsed,start_ts:openStart.ts,active:true})
    }
    if(pairs.length===0&&sessions.length>0){
      list.innerHTML=sessions.map(function(e){
        var t=e.ts?.slice(11,16)||'';
        var label=e.activity||e.type||'session';
        var extra=e.custom_label?' ('+e.custom_label+')':'';
        return '<div style="padding:6px 8px;border-bottom:1px solid var(--border);font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:12px;display:flex;justify-content:space-between;align-items:center">'+
          '<span style="color:var(--text-inactive)">['+t+']</span> <span style="flex:1;margin-left:6px">'+activityLabel(label)+extra+'</span>'+
          '<span onclick="deleteSessionEvent(\'sessions\',\''+e.id+'\')" style="color:var(--red);cursor:pointer;font-size:11px;padding:2px 6px">&#x2716;</span></div>'
      }).join('');
      return
    }
    list.innerHTML=pairs.map(function(p){
      var t=p.start_ts.slice(11,16);
      var durStr=p.duration+'m';
      var activeTag=p.active?' <span style="color:var(--green);font-size:10px">ACTIVE</span>':'';
      var label=p.label?' <span style="color:var(--text-inactive)">'+p.label+'</span>':'';
      var endTime=p.end?p.end.ts?.slice(11,16):'';
      var editBtn='<span onclick="event.stopPropagation();editSessionPair(\''+p.start.id+'\',\''+(p.end?p.end.id:'')+'\',\''+esc(p.activity)+'\',\''+esc(p.label||'')+'\',\''+p.duration+'\')" style="color:var(--primary);cursor:pointer;font-size:11px;padding:2px 6px">&#x270F;</span>';
      var delBtn='<span onclick="event.stopPropagation();deleteSessionPair(\''+p.start.id+'\',\''+(p.end?p.end.id:'')+'\')" style="color:var(--red);cursor:pointer;font-size:11px;padding:2px 6px">&#x2716;</span>';
      return '<div onclick="editSessionPair(\''+p.start.id+'\',\''+(p.end?p.end.id:'')+'\',\''+esc(p.activity)+'\',\''+esc(p.label||'')+'\',\''+p.duration+'\')" style="padding:6px 8px;border-bottom:1px solid var(--border);font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:12px;cursor:pointer">'+
        '<div style="display:flex;justify-content:space-between;align-items:center">'+
        '<span><span style="color:var(--text-inactive)">['+t+']</span> <span style="color:var(--primary)">></span> <span style="font-weight:600">'+activityLabel(p.activity)+'</span>'+label+activeTag+'</span>'+
        '<span style="color:var(--green);flex-shrink:0;margin-left:6px">'+durStr+'</span></div>'+
        '<div style="display:flex;justify-content:space-between;font-size:10px;color:var(--text-inactive);margin-top:2px;padding-left:20px">'+
        '<span>'+(endTime?'&rarr; '+endTime:'...')+'</span>'+
        '<span>'+editBtn+' '+delBtn+'</span></div></div>'
    }).join('')
  }catch(e){document.getElementById('sessionList').innerHTML='<div style="color:var(--red);font-size:13px;text-align:center;padding:10px">Failed to load</div>'}
}

var _sessActivityItems=[];

function renderActivityDropdown(){
  var dd=document.getElementById('sessActivityDropdown');
  dd.innerHTML='';
  _sessActivityItems.forEach(function(item){
    if(item.sep){dd.innerHTML+='<div style="padding:2px 10px;color:var(--border);font-size:10px;text-align:center;pointer-events:none">\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500</div>';return}
    var delBtn=item.deletable?'<span class="act-del" data-activity="'+esc(item.value)+'" style="margin-left:auto;padding:2px 6px;cursor:pointer;color:var(--red);font-size:11px;display:none" onclick="event.stopPropagation();deleteRecentActivity(\''+esc(item.value)+'\')">\u2716</span>':'';
    dd.innerHTML+='<div class="act-item" data-value="'+esc(item.value)+'" onclick="selectActivity(\''+esc(item.value)+'\',\''+esc(item.label)+'\')" style="padding:8px 10px;cursor:pointer;display:flex;align-items:center;gap:6px;border-bottom:1px solid var(--border);font-size:13px'+(item.custom?';font-style:italic':'')+'">'+
      '<span>'+(item.icon||'')+'</span><span>'+item.label+'</span>'+delBtn+'</div>';
  });
  dd.querySelectorAll('.act-item').forEach(function(el){el.addEventListener('mouseenter',function(){var d=el.querySelector('.act-del');if(d)d.style.display='block'});el.addEventListener('mouseleave',function(){var d=el.querySelector('.act-del');if(d)d.style.display='none'})});
}

function toggleActivityDropdown(){
  var dd=document.getElementById('sessActivityDropdown');
  dd.style.display=dd.style.display==='none'?'block':'none';
}

function selectActivity(value,label){
  document.getElementById('sessActivity').value=value;
  document.getElementById('sessActivityLabel').textContent=label;
  document.getElementById('sessActivityDropdown').style.display='none';
  toggleCustomActivity();
}

function closeActivityDropdown(e){
  var t=e.target;
  if(!t.closest('.custom-select')){var dd=document.getElementById('sessActivityDropdown');if(dd)dd.style.display='none'}
}
document.addEventListener('click',closeActivityDropdown);

async function deleteRecentActivity(name){
  if(!confirm('Remove "'+name+'" from recent activities?'))return;
  try{
    var r=await fetch(BASE+'/session/delete-activity',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({activity:name})});
    var j=await r.json();
    if(j.ok){toast('Removed');_sessActivityItems=[];loadSessActivities()}
    else toast('Failed');
  }catch(e){toast('Error')}
}

async function loadSessActivities(){
  if(_sessActivityItems.length>0)return;
  try{
    var recent=await fetch(BASE+'/session/recent-activities').then(function(r){return r.json()});
    if(recent.ok&&recent.activities&&recent.activities.length){
      recent.activities.forEach(function(a){_sessActivityItems.push({value:a,label:activityLabel(a),custom:true,deletable:true})});
      _sessActivityItems.push({sep:true});
    }
  }catch(e){}
  try{
    var acts=await wrapFetch(fetch(BASE+'/meta/activities'),'sessActivities',{}).then(function(r){return r.json()});
    acts.forEach(function(a){_sessActivityItems.push({value:a,label:activityLabel(a),custom:false,deletable:false})});
    _sessActivityItems.push({value:'__custom__',label:'\u2795 Add Activity',custom:true,deletable:false});
  }catch(e){}
  renderActivityDropdown();
}

function editSessionPair(startId,endId,activity,label,duration){
  var ov=document.getElementById('editSessOverlay');
  if(!ov){
    ov=document.createElement('div');
    ov.id='editSessOverlay';
    ov.style.cssText='position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,0.6);display:flex;align-items:center;justify-content:center;z-index:9999';
    ov.innerHTML='<div style="background:var(--card-bg);border-radius:12px;padding:20px;width:300px;max-width:90vw;display:flex;flex-direction:column;gap:12px;color:var(--text);font-size:13px">'+
      '<div style="font-weight:700;font-size:15px;margin-bottom:4px">Edit Session</div>'+
      '<label>Activity</label>'+
      '<input id="editSessActivity" style="background:var(--bg);border:1px solid var(--border);border-radius:6px;padding:8px;color:var(--text);font-size:13px;width:100%;box-sizing:border-box">'+
      '<label>Label (optional)</label>'+
      '<input id="editSessLabel" style="background:var(--bg);border:1px solid var(--border);border-radius:6px;padding:8px;color:var(--text);font-size:13px;width:100%;box-sizing:border-box">'+
      '<label>Duration (minutes)</label>'+
      '<input id="editSessDuration" type="number" min="1" style="background:var(--bg);border:1px solid var(--border);border-radius:6px;padding:8px;color:var(--text);font-size:13px;width:100%;box-sizing:border-box">'+
      '<div style="display:flex;gap:8px;margin-top:4px">'+
      '<button id="editSessSaveBtn" style="flex:1;background:var(--primary);color:var(--bg);border:none;border-radius:6px;padding:8px;font-size:13px;cursor:pointer;font-weight:600">Save</button>'+
      '<button id="editSessCancelBtn" style="flex:1;background:var(--border);color:var(--text);border:none;border-radius:6px;padding:8px;font-size:13px;cursor:pointer">Cancel</button></div></div>';
    document.body.appendChild(ov);
  }
  ov.style.display='flex';
  var actInput=document.getElementById('editSessActivity');
  var lblInput=document.getElementById('editSessLabel');
  var durInput=document.getElementById('editSessDuration');
  actInput.value=activity;
  lblInput.value=label;
  durInput.value=duration;
  actInput.focus();
  var saveBtn=document.getElementById('editSessSaveBtn');
  var cancelBtn=document.getElementById('editSessCancelBtn');
  var cleanup=function(){ov.style.display='none';saveBtn.onclick=null;cancelBtn.onclick=null};
  cancelBtn.onclick=cleanup;
  saveBtn.onclick=async function(){
    var newAct=actInput.value.trim();
    var newLbl=lblInput.value.trim();
    var newDur=parseInt(durInput.value);
    if(!newAct||!newDur||newDur<1){toast('Invalid values');return}
    var body={activity:newAct,custom_label:newLbl,duration_minutes:newDur};
    try{
      var r=await fetch(BASE+'/event/sessions/'+startId+'/edit',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)});
      var j=await r.json();
      if(j.ok){toast('Updated');cleanup();loadSessionsPage()}else toast('Edit failed: '+(j.error||'unknown'))
    }catch(e){toast('Error: '+e)}
  };
}

function openSessionsPage(){
  document.getElementById('mainFab').style.display='none';_pushOv();
  var el=document.getElementById('sessionsPage');el.scrollTop=0;el.classList.add('show');
  var today=localDate();
  document.getElementById('sessDate').value=today;
  loadSessionsPage()
}
function closeSessionsPage(){document.getElementById('mainFab').style.display='flex';document.getElementById('sessionsPage').classList.remove('show')}

document.getElementById('sessDate').addEventListener('change',loadSessionsPage);
