// ─── Timeline Sidebar ───
function openTimelineSidebar(){
  document.getElementById('timelineSidebarOverlay').classList.add('show');
  document.getElementById('timelineSidebar').classList.add('open');
  var today=localDate();
  document.getElementById('timelineSidebarDate').value=today;
  loadTimelineSidebar();
}
function closeTimelineSidebar(){
  document.getElementById('timelineSidebarOverlay').classList.remove('show');
  document.getElementById('timelineSidebar').classList.remove('open');
}
async function loadTimelineSidebar(){
  var container=document.getElementById('timelineSidebarContent');
  var dateStr=document.getElementById('timelineSidebarDate').value;
  container.innerHTML='<div style="color:var(--muted);text-align:center;padding:20px;">Loading...</div>';
  try{
    var data=await wrapFetch(fetch(BASE+'/day/'+dateStr),'timeline',{date:dateStr}).then(function(r){return r.json()});
    container.innerHTML='';
    var events=data.events;
    if(!events||events.length===0){
      container.innerHTML='<div style="color:var(--muted);text-align:center;padding:20px;">No events for this day</div>';
      return;
    }
    var lines=events.map(function(e){
      var icon=CATEGORY_ICONS[e._category]||'\u{1F4CB}';
      var time=e.ts?.slice(11,16)||'';
      var text=icon+' '+time+' '+e._label+' ';
      if(e._category==='mood')text+='Mood: '+(e.mood||'')+(e.energy?' · Energy: '+e.energy:'')+(e.note?' ('+e.note+')':'');
      else if(e._category==='energy')text+='Energy: '+(e.energy||'')+(e.note?' ('+e.note+')':'');
      else if(e._category==='metrics'){
        if(e.type==='checkin')text+='\u2705 Check-in '+e.time+(e.note?' · '+e.note:'');
        else if(e.type==='checkout')text+='\u{1F534} Checkout '+e.time+(e.note?' · '+e.note:'');
        else if(e.type==='panic')text+='\u{1F630} Panic'+(e.note?' · '+e.note:'');
        else if(e.doom_scroll)text+='\u{1F4F1} Doom: '+e.doom_scroll+'m';
        else if(e.pushups)text+='\u{1F4AA} Pushups: '+e.pushups;
        else text+=JSON.stringify(e).slice(0,80);
      }else if(e._category==='habits')text+='\u{1F3AF} '+(e.on?'\u2705':'\u274C')+' '+activityLabel(e.activity)+(e.note?' · '+e.note:'');
      else if(e._category==='journal')text+='\u{1F4DD} '+(e.text||'').slice(0,80);
      else if(e._category==='thought')text+='\u{1F4AD} '+(e.text||'').slice(0,80);
      else if(e._category==='sessions'){
        if(e.type==='start')text+='\u23F1 '+activityLabel(e.activity)+' started';
        else if(e.type==='end')text+='\u23F9 '+activityLabel(e.activity)+' ended ('+e.duration+'m)';
      }else if(e._category==='tasks'){
        if(e.action==='completed')text+='\u2705 '+(e.title||e.task_id);
        else text+='\u{1F4CB} '+(e.action||'')+(e.title?' '+e.title:'');
      }else if(e._category==='dsa')text+='\u{1F4DA} '+(e.problem||'')+(e.topic?' ('+e.topic+')':'');
      else if(e._category==='chess')text+='\u265F '+(e.result||'')+' vs '+(e.opponent||'');
      else if(e._category==='running'){var rs=e.duration_min||0;var rm=Math.floor(rs/60);var rss=rs%60;text+='\u{1F3C3} '+(e.distance_km||0)+'km '+rm+':'+(rss<10?'0':'')+rss+(e.pace?' '+e.pace+'/km':'');}
      else if(e._category==='panics')text+='\u{1F630} Panic'+(e.note?' · '+e.note:'');
      else if(e._category==='checklist')text+='\u2611 '+(e.label||'')+(e.done?' \u2705':'');
      else if(e._category==='daytracker')text+='\u23F0 '+(e.activity||'')+(e.on===true?' ON':e.on===false?' OFF':'')+(e.notes?' · '+e.notes:'');
      else text+=JSON.stringify(e).slice(0,80);
      return '<div style="padding:4px 0;border-bottom:1px solid var(--card2);font-size:12px;color:var(--muted);line-height:1.4">'+esc(text)+'</div>';
    }).join('');
    container.innerHTML=lines;
  }catch(e){
    container.innerHTML='<div style="color:var(--red);text-align:center;padding:20px;">Failed to load</div>';
  }
}
