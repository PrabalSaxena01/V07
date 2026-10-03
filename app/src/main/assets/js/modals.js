// ─── Modals ───────────────────────────────────────────────────────────────────

var thinkTopics=[];

const MODALS={
  mood:{title:'Log Mood',fields:[{type:'text',key:'mood',label:'Mood',chips:[],required:true},{type:'text',key:'note',label:'Note (optional)',required:false}],submit:async function(vals){return fetchPostParams('/log/mood',vals)},onOpen:async function(){
    var field=document.getElementById('modal_field_mood');
    var chipDiv=document.getElementById('chips_mood');
    if(!chipDiv||!field)return;
    var opts=MODALS.mood.fields[0].options||[];
    chipDiv.innerHTML='';
    opts.forEach(function(o){
      var chip=document.createElement('span');
      chip.className='chip';
      var label=typeof o==='object'?o.label:o;
      chip.textContent=label;
      chip.onclick=function(){
        field.value=typeof o==='object'?o.value:o;
        chipDiv.querySelectorAll('.chip').forEach(function(x){x.classList.remove('selected')});
        chip.classList.add('selected');
      };
      chipDiv.appendChild(chip);
    });
  }},
  energy:{title:'Log Energy',fields:[{type:'text',key:'energy',label:'Energy',chips:[],required:true},{type:'text',key:'note',label:'Note (optional)',required:false}],submit:async function(vals){return fetchPostParams('/log/energy',vals)},onOpen:async function(){
    var field=document.getElementById('modal_field_energy');
    var chipDiv=document.getElementById('chips_energy');
    if(!chipDiv||!field)return;
    var opts=MODALS.energy.fields[0].options||[];
    chipDiv.innerHTML='';
    opts.forEach(function(o){
      var chip=document.createElement('span');
      chip.className='chip';
      var label=typeof o==='object'?o.label:o;
      chip.textContent=label;
      chip.onclick=function(){
        field.value=typeof o==='object'?o.value:o;
        chipDiv.querySelectorAll('.chip').forEach(function(x){x.classList.remove('selected')});
        chip.classList.add('selected');
      };
      chipDiv.appendChild(chip);
    });
  }},
  doom:{title:'Log Doom Scroll',fields:[{type:'number',label:'Minutes',key:'minutes'}],submit:async function(vals){return fetchPostParams('/log/doom',vals)}},
  pushups:{title:'Log Pushups',fields:[{type:'number',label:'Count',key:'count'}],submit:async function(vals){return fetchPostParams('/log/pushups',vals)}},
  panic:{title:'Log Panic',fields:[{type:'text',label:'Trigger (optional)',key:'trigger',required:false}],submit:async function(vals){return fetchPostParams('/log/panic',vals)}},
  think:{title:'Capture Thought',fields:[{type:'text',label:'Topic',key:'topic',chips:[]},{type:'textarea',label:'Text',key:'text'}],submit:async function(vals){return fetchPostParams('/log/think',vals)},onOpen:async function(){
    var topicField=document.getElementById('modal_field_topic');
    var chipsDiv=document.getElementById('chips_topic');
    if(!chipsDiv)return;
    var topics=[];
    try{
      topics=thinkTopics.slice();
    }catch(e){}
    if(topics.length===0){
      try{
        var r=await fetch(BASE+'/meta/think-topics');
        var j=await r.json();
        if(Array.isArray(j))topics=j;
      }catch(e){}
    }
    chipsDiv.innerHTML='';
    if(topics.length>0){
      topics.forEach(function(t){
        var chip=document.createElement('span');
        chip.className='chip';
        chip.textContent=t;
        chip.onclick=function(){topicField.value=t;chipsDiv.querySelectorAll('.chip').forEach(function(x){x.classList.remove('selected')});chip.classList.add('selected')};
        chipsDiv.appendChild(chip);
      });
    }
    var otherChip=document.createElement('span');
    otherChip.className='chip';
    otherChip.textContent='Other\u2026';
    otherChip.style.borderStyle='dashed';
    otherChip.onclick=function(){topicField.value='';topicField.focus();chipsDiv.querySelectorAll('.chip').forEach(function(x){x.classList.remove('selected')});otherChip.classList.add('selected')};
    chipsDiv.appendChild(otherChip);
    topicField.value='Quick';
    chipsDiv.querySelectorAll('.chip').forEach(function(c){if(c.textContent==='Quick'){c.classList.add('selected')}else{c.classList.remove('selected')}});
  }},
  win:{title:'Log Win',fields:[{type:'textarea',label:'Win',key:'text'}],submit:async function(vals){return fetchPostParams('/log/win',vals)}},
  journal:{title:'Journal Entry',fields:[{type:'textarea',label:'Entry',key:'text'}],submit:async function(vals){return fetchPostParams('/log/journal',vals)}},
  dsa:{title:'Log DSA Problem',fields:[{type:'text',label:'Topic',key:'topic'},{type:'text',label:'Problem Name',key:'name'},{type:'select',label:'Platform',key:'platform',options:['LeetCode','Codeforces','CodeChef','HackerRank','Striver','Other']},{type:'select',label:'Difficulty',key:'difficulty',options:['easy','medium','hard']},{type:'text',label:'Description (optional)',key:'description',required:false},{type:'text',label:'Approach (optional)',key:'approach',required:false}],submit:async function(vals){return fetchPostParams('/log/dsa',vals)}},
  chess:{title:'Log Chess Match',fields:[{type:'text',label:'Opponent',key:'opponent'},{type:'select',label:'Result',key:'result',options:['win','loss','draw']},{type:'select',label:'Color',key:'color',options:['white','black']},{type:'text',label:'Notes (optional)',key:'notes',required:false}],submit:async function(vals){return fetchPostParams('/log/chess',vals)}},
  run:{title:'Log Run',fields:[{type:'number',label:'Distance (km)',key:'distance_km',step:0.1},{type:'text',label:'Duration (mm:ss)',key:'duration_min',placeholder:'25:30'},{type:'select',label:'Type',key:'run_type',options:['easy','tempo','interval','long','race']},{type:'text',label:'Notes (optional)',key:'notes',required:false}],submit:async function(vals){return fetchPostParams('/log/run',vals)}},
  task:{title:'Create Task',fields:[{type:'text',label:'Title',key:'title'},{type:'date',label:'Deadline',key:'deadline',required:true},{type:'time',label:'Time (optional)',key:'time',required:false},{type:'select',label:'Priority',key:'priority',options:['low','medium','high']}],submit:async function(vals){try{var r=await fetch(BASE+'/task?'+new URLSearchParams(vals).toString(),{method:'POST'});var j=await r.json();if(j.ok)toast('Task created!');else if(j.detail)toast(j.detail);loadDashboard()}catch(e){toast('Error')}}},
  habit:{title:'Toggle Habit',fields:[{type:'select',label:'Activity',key:'activity',options:[]},{type:'select',label:'State',key:'on',options:[{value:'true',label:'Done'},{value:'false',label:'Skipped'}]},{type:'text',label:'Notes (optional)',key:'notes',required:false}],submit:async function(vals){return fetchPostParams('/log/habit',{activity:vals.activity,on:vals.on==='true',notes:vals.notes})}},
  'session-start':{title:'Start Session',fields:[{type:'select',label:'Activity',key:'activity',options:[]},{type:'text',label:'Custom Label (optional)',key:'custom_label',required:false}],submit:async function(vals){try{var existing=await fetch(BASE+'/session/active').then(function(r){return r.json()});if(existing.active||existing.paused){toast('Session already active — stop it first');return}const r=await fetch(BASE+'/session/start',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({activity:vals.activity,custom_label:vals.custom_label||null,timer_mode:timerMode,timer_seconds:timerMode==='countdown'?timerSeconds:0})});const j=await r.json();if(j.ok){toast('Session started!');if(!timerRunning)timerStart();loadDashboard();loadSessionStatus()}else if(j.detail)toast(j.detail)}catch(e){toast('Error')}}}
};

async function loadMeta(){
  try{
    var[td]=await Promise.allSettled([
      wrapFetch(fetch(BASE+'/meta/dsa-topics'),'loadMeta',{endpoint:'/meta/dsa-topics'}).then(function(r){return r.json()})]);
    if(td.status==='fulfilled'&&Array.isArray(td.value))MODALS.dsa.fields[0].options=td.value;
    var acts=await wrapFetch(fetch(BASE+'/meta/activities'),'loadMeta',{endpoint:'/meta/activities'}).then(function(r){return r.json()}).catch(function(){return[]});
    if(Array.isArray(acts)){MODALS.habit.fields[0].options=acts;MODALS['session-start'].fields[0].options=acts}
    var moodRes=await wrapFetch(fetch(BASE+'/meta/moods'),'loadMeta',{endpoint:'/meta/moods'}).then(function(r){return r.json()}).catch(function(){return[]});
    var energyRes=await wrapFetch(fetch(BASE+'/meta/energies'),'loadMeta',{endpoint:'/meta/energies'}).then(function(r){return r.json()}).catch(function(){return[]});
    if(Array.isArray(moodRes))MODALS.mood.fields[0].options=moodRes;
    if(Array.isArray(energyRes))MODALS.energy.fields[0].options=energyRes;
    thinkTopics=await wrapFetch(fetch(BASE+'/meta/think-topics'),'loadMeta',{endpoint:'/meta/think-topics'}).then(function(r){return r.json()}).catch(function(){return[]});
    if(Array.isArray(thinkTopics))MODALS.think.fields[0].chips=thinkTopics;
  }catch(e){}
}
loadMeta();

document.querySelectorAll('[data-modal]').forEach(function(btn){
  btn.addEventListener('click',function(){openModal(btn.dataset.modal)});
});

function openModal(name){
  var cfg=MODALS[name];
  if(!cfg)return;
  document.getElementById('modalTitle').textContent=cfg.title;
  var fieldsDiv=document.getElementById('modalFields');
  buildFields(fieldsDiv,cfg.fields);
  if(cfg.onOpen) cfg.onOpen();
  document.getElementById('modalSubmit').onclick=async function(){
    var data={};
    var valid=true;
    fieldsDiv.querySelectorAll('input,select,textarea').forEach(function(inp){
      var v=inp.value.trim();
      if(!v&&inp.dataset.required==='1')valid=false;
      data[inp.dataset.key]=v;
    });
    if(!valid){toast('Fill required fields');return}
    closeModal();
    await cfg.submit(data);
  };
  document.getElementById('modalOverlay').classList.add('show');
}

async function logCheckin(){
  var overlay=document.getElementById('modalOverlay');
  document.getElementById('modalTitle').textContent='Check-in';
  document.getElementById('modalFields').innerHTML=
    '<label>Note (optional)</label><input id="ciNote" placeholder="How are you?">'+
    '<label>Date & Time</label><input type="datetime-local" id="ciDateTime" value="'+localDateTime()+'">';
  document.getElementById('modalSubmit').onclick=async function(){
    var note=document.getElementById('ciNote').value;
    var dt=document.getElementById('ciDateTime').value;
    var ts=dt?dt+':00':'';
    var params={note:note||''};
    if(ts){params.ts=ts;params.date=dt.slice(0,10)}
    await fetchPostParams('/log/checkin',params);
    closeModal();loadDashboard();loadSleepUpTimeline();
  };
  overlay.classList.add('show');
}
async function logCheckout(){
  var status=await fetch(BASE+'/sleep-up').then(function(r){return r.json()}).catch(function(){return null});
  var overlay=document.getElementById('modalOverlay');
  document.getElementById('modalTitle').textContent='Check-out';
  document.getElementById('modalFields').innerHTML=
    '<label>Note (optional)</label><input id="coNote" placeholder="Check-out note">'+
    '<label>Date & Time</label><input type="datetime-local" id="coDateTime" value="'+localDateTime()+'">';
  document.getElementById('modalSubmit').onclick=async function(){
    var note=document.getElementById('coNote').value;
    var dt=document.getElementById('coDateTime').value;
    var ts=dt?dt+':00':'';
    var params={note:note||''};
    if(ts){params.ts=ts;params.date=dt.slice(0,10)}
    await fetchPostParams('/log/checkout',params);
    closeModal();loadDashboard();loadSleepUpTimeline();
  };
  overlay.classList.add('show');
}

async function retroCheckin(){
  var overlay=document.getElementById('modalOverlay');
  document.getElementById('modalTitle').textContent='Retroactive Check-in';
  document.getElementById('modalFields').innerHTML=
    '<label>Note (optional)</label><input id="rciNote" placeholder="Check-in note">'+
    '<label>Date & Time</label><input type="datetime-local" id="rciDateTime">';
  document.getElementById('modalSubmit').onclick=async function(){
    var note=document.getElementById('rciNote').value;
    var dt=document.getElementById('rciDateTime').value;
    if(!dt){toast('Date & time required');return}
    var params={note:note||'',ts:dt+':00',date:dt.slice(0,10)};
    await fetchPostParams('/log/checkin',params);
    closeModal();loadSleepUpTimeline();loadDashboard();
  };
  overlay.classList.add('show');
}

async function retroCheckout(){
  var overlay=document.getElementById('modalOverlay');
  document.getElementById('modalTitle').textContent='Retroactive Check-out';
  document.getElementById('modalFields').innerHTML=
    '<label>Note (optional)</label><input id="rcoNote" placeholder="Check-out note">'+
    '<label>Date & Time</label><input type="datetime-local" id="rcoDateTime">';
  document.getElementById('modalSubmit').onclick=async function(){
    var note=document.getElementById('rcoNote').value;
    var dt=document.getElementById('rcoDateTime').value;
    if(!dt){toast('Date & time required');return}
    var params={note:note||'',ts:dt+':00',date:dt.slice(0,10)};
    await fetchPostParams('/log/checkout',params);
    closeModal();loadSleepUpTimeline();loadDashboard();
  };
  overlay.classList.add('show');
}
