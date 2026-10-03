// ─── Recycle Bin ───
// ─── Recycle Bin Page ────────────────────────────────────────────────────────────

var _recycleFilter='';

async function openRecycleBinPage(){
  document.getElementById('mainFab').style.display='none';_pushOv();
  var el=document.getElementById('recycleBinPage');el.scrollTop=0;el.classList.add('show');
  loadRecycleBin('');
}
function closeRecycleBinPage(){document.getElementById('mainFab').style.display='flex';document.getElementById('recycleBinPage').classList.remove('show')}

async function loadRecycleBin(category){
  _recycleFilter=category;
  var btns=document.querySelectorAll('#recycleBinPage .page-header + div button');
  btns.forEach(function(b){b.className='btn'});
  var activeBtn=Array.from(btns).find(function(b){return(b.dataset.cat||'')===category});
  if(!activeBtn)activeBtn=btns[0];
  activeBtn.className='btn btn-accent';
  var container=document.getElementById('recycleBinContent');
  if(typeof renderSkeleton==='function') container.innerHTML=renderSkeleton('cards',3);
  else container.innerHTML='<div style="color:var(--muted);font-size:14px;text-align:center;padding:20px">Loading...</div>';
  try{
    var qs=category?'?category='+encodeURIComponent(category):'';
    var r=await fetch(BASE+'/recycle-bin'+qs);
    var j=await r.json();
    var entries=j.entries||[];
    if(entries.length===0){
      container.innerHTML='<div style="color:var(--muted);text-align:center;padding:20px">Recycle bin is empty</div>';
      return;
    }
    container.innerHTML=entries.map(function(e){
      var icon=CATEGORY_ICONS[e.category]||'\u{1F5D1}';
      var catLabel=e.category.charAt(0).toUpperCase()+e.category.slice(1);
      var preview='';
      var detail='';
      var snap=e.data.snapshot||e.data;
      if(e.category==='tasks'){
        preview='<b>'+(snap.title||'Task')+'</b>';
        detail=(snap.deadline?'Due '+snap.deadline:'')+(snap.priority?' \u00b7 ['+snap.priority+']':'')+(snap.time?' \u00b7 '+snap.time:'');
      }else if(e.category==='sessions'){
        preview=activityLabel(snap.activity||'')+(snap.custom_label?' \u00b7 '+snap.custom_label:'');
        detail=(snap.type||'start')+(snap.duration?' \u00b7 '+snap.duration+'m':'')+(snap.start_ts?' \u00b7 from '+snap.start_ts.slice(11,16):'');
      }else if(e.category==='mood'){
        preview=moodLabel(snap.mood||'');
        detail=snap.energy?'Energy: '+energyLabel(snap.energy):''+(snap.note?' \u00b7 '+snap.note:'');
      }else if(e.category==='energy'){
        preview=energyLabel(snap.energy||'');
        detail=snap.note?'Note: '+snap.note:'';
      }else if(e.category==='journal'){
        preview=(snap.subtype||'journal').toUpperCase();
        detail=(snap.text||'').slice(0,100);
      }else if(e.category==='metrics'){
        if(snap.type==='checkin')preview='\u2705 Check-in';else if(snap.type==='checkout')preview='\u{1F534} Checkout';
        else if(snap.type==='panic')preview='\u{1F630} Panic';
        else if(snap.doom_scroll)preview='Doom: '+snap.doom_scroll+'m';
        else if(snap.pushups)preview='Pushups: '+snap.pushups;
        else preview=snap.type||'Metric';
        detail=snap.note||snap.time||'';
      }else if(e.category==='dsa'){
        preview=(snap.problem||'DSA Problem');
        detail=(snap.topic?'('+snap.topic+')':'')+(snap.platform?' \u00b7 '+snap.platform:'')+(snap.difficulty?' \u00b7 '+snap.difficulty:'');
      }else if(e.category==='running'){
        preview=(snap.distance_km||0)+'km Run';
        detail=(snap.duration_min||0)+'min'+(snap.run_type?' \u00b7 '+snap.run_type:'');
      }else if(e.category==='chess'){
        preview='Chess: '+(snap.result||'');
        detail='vs '+(snap.opponent||'')+(snap.color?' ('+snap.color+')':'');
      }else if(e.category==='thought'){
        preview=(snap.text||'').slice(0,60);
        detail=snap.topic?'Topic: '+snap.topic:'';
      }else if(e.category==='panics'){
        preview='Panic';
        detail=snap.note||'';
      }else if(e.category==='habits'){
        preview=activityLabel(snap.activity||'')+(snap.on?' \u2705':' \u274C');
        detail=snap.notes||'';
      }else{
        try{preview=JSON.stringify(snap).slice(0,80)}catch(_){preview=''}
      }
      var deletedAt=e.deleted_at?e.deleted_at.slice(0,16).replace('T',' '):'';
      return '<div style="background:var(--card);border-radius:8px;padding:8px 10px;margin-bottom:4px;display:flex;align-items:center;gap:8px">'+
        '<span style="font-size:18px">'+icon+'</span>'+
        '<div style="flex:1;min-width:0">'+
          '<div style="font-size:13px">'+preview+'</div>'+
          (detail?'<div style="font-size:11px;color:var(--muted);margin-top:2px">'+detail+'</div>':'')+
          '<div style="font-size:10px;color:var(--muted);margin-top:2px">'+catLabel+' \u00b7 '+deletedAt+'</div>'+
        '</div>'+
        '<button class="btn btn-accent" onclick="restoreRecycleItem(\''+e.recycle_id+'\')" style="padding:4px 10px;font-size:11px">Restore</button>'+
      '</div>';
    }).join('');
  }catch(e){
    container.innerHTML='<div style="color:var(--red);text-align:center;padding:20px">Failed to load</div>';
  }
}

async function restoreRecycleItem(recycleId){
  try{
    var r=await fetch(BASE+'/recycle-bin/'+recycleId+'/restore',{method:'POST'});
    var j=await r.json();
    if(j.ok){toast('Restored!');loadRecycleBin(_recycleFilter);loadDashboard()}
    else if(j.detail)toast(j.detail)
  }catch(e){toast('Error restoring')}
}
async function clearAllRecycle(){
  if(!confirm('Permanently delete all items in the recycle bin? This cannot be undone.'))return;
  try{
    var qs=_recycleFilter?'?category='+encodeURIComponent(_recycleFilter):'';
    var r=await fetch(BASE+'/recycle-bin/clear'+qs,{method:'POST'});
    var j=await r.json();
    if(j.ok){toast('Recycle bin cleared');loadRecycleBin(_recycleFilter)}
  }catch(e){toast('Error clearing recycle bin')}
}
