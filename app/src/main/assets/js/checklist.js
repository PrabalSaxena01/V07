// ─── Daily Checklist ──────────────────────────────────────────────────────────

var _clFreq='S';
function toggleClFreq(f){
  _clFreq=f;
  var s=document.getElementById('clFreqS'),d=document.getElementById('clFreqD'),w=document.getElementById('clFreqW');
  s.style.border=d.style.border=w.style.border='2px solid var(--border)';
  s.style.color=d.style.color=w.style.color='var(--text-inactive)';
  s.style.background=d.style.background=w.style.background='transparent';
  var el={S:s,D:d,W:w}[f];
  el.style.border='2px solid var(--primary)';el.style.color='var(--primary)';el.style.background='rgba(167,200,255,0.1)';
}
function openChecklist(){
  document.getElementById('mainFab').style.display='none';_pushOv();
  document.getElementById('checklistPage').classList.add('show');
  document.getElementById('checklistPage').scrollTop=0;
  var cl = document.getElementById('checklistItems');
  if(cl && typeof renderSkeleton === 'function') cl.innerHTML = renderSkeleton('list', 5);
  loadChecklistItems();
}
function closeChecklist(){
  document.getElementById('mainFab').style.display='flex';
  document.getElementById('checklistPage').classList.remove('show');
}

async function loadChecklistItems(){
  try{
    var r1=await fetch(BASE+'/checklist/today');
    var j1=await r1.json();
    var dailyItems=(j1.items||[]).map(function(i){i._freq='D';return i});
    var r2=await fetch(BASE+'/checklist/week');
    var j2=await r2.json();
    var weeklyItems=(j2.items||[]).map(function(i){i._freq='W';return i});
    var allItems=dailyItems.concat(weeklyItems);
    var container=document.getElementById('checklistItems');
    if(allItems.length===0){
      container.innerHTML='<div style="display:flex;flex-direction:column;align-items:center;justify-content:center;padding:48px 20px;opacity:0.3"><span class="material-symbols-outlined" style="font-size:40px;margin-bottom:12px">check_circle</span><span style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:11px;text-transform:uppercase;letter-spacing:0.3em">ALL DIRECTIVES FULFILLED</span></div>';
      return;
    }
    var active=[], done=[], overdue=[];
    allItems.forEach(function(item){
      if(item.done)done.push(item);
      else if(item.overdue)overdue.push(item);
      else active.push(item);
    });
    var sorted=overdue.concat(active).concat(done);
    var html='';
    sorted.forEach(function(item){
      var isDone=!!item.done;
      var isOverdue=!isDone&&!!item.overdue;
      var cls='cl-item'+(isDone?' done':'')+(isOverdue?' overdue':'');
      var freq=item._freq||item.frequency||(item.recurring?'W':'D');
      var circleBg,circleIcon;
      if(isDone){circleBg='var(--secondary)';circleIcon='<span class="material-symbols-outlined" style="font-size:10px;color:#090909">check</span>'}
      else if(isOverdue){circleBg='var(--accent-orange)';circleIcon='<span class="material-symbols-outlined" style="font-size:10px;color:#090909">warning</span>'}
      else{circleBg='transparent';circleIcon=''}
      var labelColor=isDone?'var(--secondary)':(isOverdue?'var(--accent-orange)':'var(--text)');
      var labelStyle=isDone?'opacity:0.5;text-decoration:line-through':'';
      var freqLabel;
      if(isDone)freqLabel='<span style="color:var(--secondary);font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;font-weight:700">COMPLETED</span>';
      else if(freq==='W')freqLabel='<span style="color:var(--accent-purple);font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;font-weight:700">[W]</span>';
      else freqLabel='<span style="color:var(--text-inactive);font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;font-weight:700">[D]</span>';
      html+='<div class="'+cls+'">';
      html+='<button onclick="event.stopPropagation();toggleChecklistItem(\''+item.id+'\')" style="width:16px;height:16px;border:1px solid '+(isDone?'var(--secondary)':(isOverdue?'var(--accent-orange)':'var(--border)'))+';display:flex;align-items:center;justify-content:center;flex-shrink:0;cursor:pointer;background:'+(isDone?'rgba(77,224,134,0.1)':circleBg)+';border-radius:50%;padding:0">'+circleIcon+'</button>';
      html+='<div style="flex:1;display:flex;align-items:center;justify-content:space-between;min-width:0;gap:8px">';
      html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:14px;font-weight:500;color:'+labelColor+';text-transform:uppercase;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;'+labelStyle+'">'+esc(item.label)+'</span>';
      html+='<div style="display:flex;align-items:center;gap:6px;flex-shrink:0">';
      html+=freqLabel;
      html+='<span onclick="event.stopPropagation();deleteChecklistItem(\''+item.id+'\')" style="cursor:pointer;color:var(--text-inactive);display:flex;align-items:center;transition:color 0.15s" onmouseenter="this.style.color=\'var(--red)\'" onmouseleave="this.style.color=\'var(--text-inactive)\'"><span class="material-symbols-outlined" style="font-size:16px">delete</span></span>';
      html+='</div></div></div>';
    });
    container.innerHTML=html;
  }catch(e){}
}

async function addChecklistItem(){
  var input=document.getElementById('checklistInput');
  var label=input.value.trim();
  if(!label)return;
  var recurring=(_clFreq==='D'||_clFreq==='W');
  var frequency=_clFreq;
  try{
    await fetch(BASE+'/checklist/today',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({label:label,recurring:recurring,frequency:frequency})});
    input.value='';
    loadChecklistItems();
  }catch(e){}
}

async function toggleChecklistItem(id){
  var el=document.getElementById('checklistItems');
  if(el){
    var row=Array.from(el.children).find(function(r){
      return r.querySelector('[onclick*="'+id+'"]');
    });
    if(row){
      var btn=row.querySelector('[onclick*="toggleChecklistItem"]');
      var label=row.querySelector('span[style*="font-size:14px"]');
      if(btn){
        var isChecked=btn.textContent.trim()!=='';
        btn.textContent=isChecked?'':'✓';
        btn.style.background=isChecked?'transparent':'var(--secondary)';
        btn.style.color=isChecked?'transparent':'var(--on-primary)';
        if(label){
          label.style.textDecoration=isChecked?'none':'line-through';
          label.style.color=isChecked?'var(--text)':'var(--text-inactive)';
        }
      }
    }
  }
  try{
    await fetch(BASE+'/checklist/'+id+'/toggle',{method:'POST'});
  }catch(e){
    loadChecklistItems();
  }
}

async function editChecklistItem(id,currentLabel,currentRecurring){
  var newLabel=await customPrompt('Edit task label:',currentLabel);
  if(newLabel===null||!newLabel.trim())return;
  try{
    await fetch(BASE+'/checklist/'+id+'/edit',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({label:newLabel.trim(),recurring:currentRecurring})});
    loadChecklistItems();
  }catch(e){}
}

async function deleteChecklistItem(id){
  if(!confirm('Delete this task?'))return;
  try{
    await fetch(BASE+'/checklist/'+id,{method:'DELETE'});
    loadChecklistItems();
  }catch(e){}
}
