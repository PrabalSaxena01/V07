// ─── Tasks Page ───────────────────────────────────────────────────────────────

var _tasksFilter='all';
async function openTasksPage(filter){
  document.getElementById('mainFab').style.display='none';_pushOv();
  var el=document.getElementById('tasksPage');el.scrollTop=0;el.classList.add('show');
  loadTasksPage(filter||'all');
}
function closeTasksPage(){document.getElementById('mainFab').style.display='flex';document.getElementById('tasksPage').classList.remove('show')}

function renderTaskFilters(){
  var c=document.getElementById('tasksFilters');
  var filters=[
    {key:'all',label:'ALL',color:'var(--primary)'},
    {key:'today',label:'TODAY',color:'var(--primary)'},
    {key:'upcoming',label:'UPCOMING',color:'var(--secondary)'},
    {key:'overdue',label:'OVERDUE',color:'var(--accent-red)'},
    {key:'done',label:'DONE',color:'var(--text-inactive)'}
  ];
  c.innerHTML=filters.map(function(f){
    var active=_tasksFilter===f.key;
    var style=active
      ?'flex:1;min-width:80px;padding:10px 12px;background:var(--surface-container-low);color:'+f.color+';font-family:\'Space Mono\',\'Mono\',monospace;font-size:11px;font-weight:700;border:none;border-right:1px solid var(--border);text-transform:uppercase;letter-spacing:0.12em;cursor:pointer;text-align:center;display:flex;align-items:center;justify-content:center;gap:6px'
      :'flex:1;min-width:80px;padding:10px 12px;background:transparent;color:var(--text-inactive);font-family:\'Space Mono\',\'Mono\',monospace;font-size:11px;border:none;border-right:1px solid var(--border);text-transform:uppercase;letter-spacing:0.12em;cursor:pointer;text-align:center;transition:color 0.15s;display:flex;align-items:center;justify-content:center;gap:6px';
    var dot='<span style="width:5px;height:5px;border-radius:50%;background:'+f.color+';flex-shrink:0"></span>';
    return '<button onclick="loadTasksPage(\''+f.key+'\')" style="'+style+'">'+dot+f.label+'</button>';
  }).join('');
}

async function loadTasksPage(filter){
  _tasksFilter=filter||'all';
  renderTaskFilters();
  var container=document.getElementById('tasksContent');
  if(typeof renderSkeleton==='function') container.innerHTML=renderSkeleton('cards',3);
  else container.innerHTML='<div style="color:var(--muted);font-size:14px;text-align:center;padding:20px">Loading...</div>';
  try{
    var r=await fetch(BASE+'/tasks?filter='+_tasksFilter);
    var j=await r.json();
    var tasks=j.tasks||[];
    var progressEl=document.getElementById('tasksProgress');
    if(tasks.length===0){
      progressEl.style.display='none';
      container.innerHTML='<div style="color:var(--muted);text-align:center;padding:20px">No tasks</div>';
      return;
    }
    var today=localDate();
    var doneTasks=tasks.filter(function(t){return t.status==='done'});
    var activeTasks=tasks.filter(function(t){return t.status!=='done'});
    var todayTasks=activeTasks.filter(function(t){
      return t.deadline===today;
    });
    var overdueTasks=activeTasks.filter(function(t){
      return t.deadline&&t.deadline<today;
    });
    var upcomingTasks=activeTasks.filter(function(t){
      return !t.deadline||t.deadline>=today;
    }).filter(function(t){return todayTasks.indexOf(t)===-1});

    var totalActive=activeTasks.length;
    var totalDone=doneTasks.length;
    var total=totalActive+totalDone;
    if(total>0){
      progressEl.style.display='block';
      var pct=total>0?Math.round((totalDone/total)*100):0;
      document.getElementById('tasksProgressLabel').textContent=totalDone+' / '+total+' ('+pct+'%)';
      document.getElementById('tasksProgressBar').style.width=pct+'%';
    }else{
      progressEl.style.display='none';
    }

    var html='';

    function renderSection(title,color,count,label,tasksArr,opts){
      if(tasksArr.length===0&&opts&&opts.skipEmpty)return'';
      var h='';
      h+='<div style="margin-bottom:16px">';
      h+='<div style="display:flex;justify-content:space-between;align-items:end;border-bottom:1px solid var(--border);padding-bottom:4px;margin-bottom:2px">';
      h+='<h3 style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:10px;font-weight:700;color:'+color+';text-transform:uppercase;letter-spacing:0.15em">&gt; // '+title+'</h3>';
      h+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive);text-transform:uppercase">'+label+'</span>';
      h+='</div>';
      if(tasksArr.length===0){
        h+='<div style="padding:12px 0;text-align:center;font-size:11px;color:var(--text-inactive);font-family:\'JetBrains Mono\',\'Mono\',monospace">No items</div>';
      }
      tasksArr.forEach(function(t){
        var isDone=t.status==='done';
        var checked=isDone;
        var priorityColor=t.priority==='high'?'var(--accent-red)':t.priority==='medium'?'var(--yellow)':'var(--text-inactive)';
        var priorityLabel=t.priority==='high'?'CRITICAL':t.priority==='medium'?'MEDIUM':'LOW';
        var deadline=t.deadline||'';
        var time=t.deadline_time?' '+t.deadline_time:'';
        var overdueLabel='';
        if(!isDone&&deadline&&deadline<today){
          var days=Math.floor((new Date()-new Date(deadline+'T00:00:00'))/(86400000));
          overdueLabel=' <span style="color:var(--accent-red);font-size:9px;font-family:\'JetBrains Mono\',\'Mono\',monospace">LATENCY: '+days+'D</span>';
        }
        var doneTime='';
        if(isDone&&t.completed_at){
          doneTime=t.completed_at.slice(11,16)||'';
        }
        h+='<div class="task-row" style="display:flex;gap:12px;padding:10px 4px;border-bottom:1px solid var(--divider);transition:background 0.15s">';
        if(isDone){
          h+='<div style="flex-shrink:0;width:16px;height:16px;background:var(--secondary);display:flex;align-items:center;justify-content:center;cursor:pointer;margin-top:1px" onclick="toggleTaskPage(\''+t.id+'\','+isDone+')">';
          h+='<span style="font-size:10px;color:var(--on-primary);font-weight:700">&#x2713;</span></div>';
        }else{
          h+='<div style="flex-shrink:0;width:16px;height:16px;border:1px solid var(--border);display:flex;align-items:center;justify-content:center;cursor:pointer;margin-top:1px;transition:border-color 0.15s" onclick="toggleTaskPage(\''+t.id+'\','+isDone+')"></div>';
        }
        h+='<div style="flex:1;min-width:0">';
        var titleStyle=isDone?'font-size:13px;color:var(--text-inactive);text-decoration:line-through;text-transform:uppercase;font-weight:500':'font-size:13px;color:var(--text);text-transform:uppercase;font-weight:500';
        h+='<div style="'+titleStyle+'">'+esc(t.title)+overdueLabel+'</div>';
        h+='<div style="display:flex;flex-wrap:wrap;gap:8px;margin-top:3px">';
        if(isDone&&doneTime){
          h+='<span style="font-size:9px;color:var(--secondary);font-family:\'JetBrains Mono\',\'Mono\',monospace;text-transform:uppercase">COMPLETED: '+doneTime+'</span>';
        }else{
          h+='<span style="font-size:9px;color:'+priorityColor+';font-family:\'JetBrains Mono\',\'Mono\',monospace;text-transform:uppercase">'+priorityLabel+'</span>';
          if(deadline)h+='<span style="font-size:9px;color:var(--text-inactive);font-family:\'JetBrains Mono\',\'Mono\',monospace;text-transform:uppercase">DUE: '+deadline+(time?' '+time:'')+'</span>';
        }
        h+='</div></div></div>';
      });
      h+='</div>';
      return h;
    }

    if(_tasksFilter==='all'){
      html+=renderSection('TODAY','var(--primary)',todayTasks.length,todayTasks.length+' ACTIVE',todayTasks,{skipEmpty:true});
      html+=renderSection('OVERDUE','var(--accent-red)',overdueTasks.length,overdueTasks.length+' LATENCY',overdueTasks,{skipEmpty:true});
      html+=renderSection('UPCOMING','var(--secondary)',upcomingTasks.length,upcomingTasks.length+' QUEUED',upcomingTasks);
      html+=renderSection('DONE','var(--text-inactive)',doneTasks.length,'COMPLETED',doneTasks);
    }else if(_tasksFilter==='today'){
      html+=renderSection('TODAY','var(--primary)',todayTasks.length,todayTasks.length+' ACTIVE',todayTasks);
    }else if(_tasksFilter==='upcoming'){
      html+=renderSection('UPCOMING','var(--secondary)',upcomingTasks.length,upcomingTasks.length+' QUEUED',upcomingTasks);
    }else if(_tasksFilter==='overdue'){
      html+=renderSection('OVERDUE','var(--accent-red)',overdueTasks.length,overdueTasks.length+' LATENCY',overdueTasks);
    }else if(_tasksFilter==='done'){
      html+=renderSection('DONE','var(--text-inactive)',doneTasks.length,'COMPLETED',doneTasks);
    }
    container.innerHTML=html;
  }catch(e){
    container.innerHTML='<div style="color:var(--red);text-align:center;padding:20px">Failed to load</div>';
  }
}

async function toggleTaskPage(id,wasDone){
  // Optimistic update in place to prevent full page rerendering!
  var container=document.getElementById('tasksContent');
  if(container){
    var row=Array.from(container.querySelectorAll('.task-row')).find(function(r){
      return r.querySelector('[onclick*="'+id+'"]');
    });
    if(row){
      var checkDiv=row.querySelector('div[onclick*="toggleTaskPage"]');
      var titleDiv=row.querySelector('div[style*="font-size:13px"]');
      if(checkDiv){
        var willBeDone=!wasDone;
        if(willBeDone){
          checkDiv.style.background='var(--secondary)';
          checkDiv.style.border='none';
          checkDiv.innerHTML='<span style="font-size:10px;color:var(--on-primary);font-weight:700">&#x2713;</span>';
          if(titleDiv){titleDiv.style.color='var(--text-inactive)';titleDiv.style.textDecoration='line-through';}
        }else{
          checkDiv.style.background='transparent';
          checkDiv.style.border='1px solid var(--border)';
          checkDiv.innerHTML='';
          if(titleDiv){titleDiv.style.color='var(--text)';titleDiv.style.textDecoration='none';}
        }
      }
    }
  }

  try{
    if(wasDone){
      await fetch(BASE+'/task/'+id+'/reopen',{method:'POST'});
    }else{
      await fetch(BASE+'/task/'+id+'/done',{method:'POST'});
    }
  }catch(e){
    toast('Error syncing task');
    loadTasksPage(_tasksFilter||'all');
  }
}

async function deleteTaskPage(id){
  if(!confirm('Move to recycle bin?'))return;
  try{
    var r=await fetch(BASE+'/task/'+id,{method:'DELETE'});
    var j=await r.json().catch(()=>({ok:false,detail:'Invalid JSON'}));
    if(j.ok||j.recycled){toast('Moved to recycle bin');loadTasksPage(_tasksFilter||'all');loadDashboard()}
    else toast(j.detail||'Failed');
  }catch(e){toast('Error: '+e.message)}
}
