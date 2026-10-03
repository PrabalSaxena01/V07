// ─── Thoughts Page ───

// ─── Thoughts Page ────────────────────────────────────────────────────────────

var _thoughtsFilter='all';
var _thoughtsExpanded=false;
var _allThoughtsData=[];
var THOUGHT_FILTERS=[
  {key:'all',label:'ALL',color:'var(--primary)',icon:'<svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><rect x="3" y="3" width="8" height="8" rx="1"/><rect x="13" y="3" width="8" height="8" rx="1"/><rect x="3" y="13" width="8" height="8" rx="1"/><rect x="13" y="13" width="8" height="8" rx="1"/></svg>'},
  {key:'quick',label:'QUICK',color:'var(--secondary)',icon:'<svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M13 2L3 14h9l-1 10 10-12h-9l1-10z"/></svg>'},
  {key:'gita',label:'GITA',color:'var(--yellow)',icon:'<svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M4 4h16v2H4zm0 4h16v12H4z"/></svg>'},
  {key:'pinned',label:'PINNED',color:'var(--red)',icon:'<svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M16 3a1 1 0 0 1 .7.3l4 4a1 1 0 0 1-.18 1.42l-10.5 7a1 1 0 0 1-1.24-.06l-3.5-3.5a1 1 0 0 1 .06-1.24l10.5-7a1 1 0 0 1 1.36.34zM7.5 14.5l3 3L5 23l1.5-5.5z"/></svg>'},
  {key:'deleted',label:'DELETED',color:'var(--text-inactive)',icon:'<svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5l-1-1h-5l-1 1H5v2h14V4h-3.5z"/></svg>'}
];
var THOUGHT_PRIMARY_KEYS=['all','quick','gita'];

function renderThoughtFilters(){
  var c=document.getElementById('thoughtsFilters');
  var visible=_thoughtsExpanded?THOUGHT_FILTERS:THOUGHT_FILTERS.filter(function(f){return THOUGHT_PRIMARY_KEYS.indexOf(f.key)!==-1});
  var html=visible.map(function(f){
    var active=_thoughtsFilter===f.key;
    return '<button onclick="setThoughtsFilter(\''+f.key+'\')" style="flex:1;min-width:72px;padding:12px 6px;background:'+(active?'var(--surface-container-low)':'transparent')+';color:'+(active?f.color:'var(--text-inactive)')+';font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;letter-spacing:0.1em;font-weight:500;border:none;border-right:1px solid var(--border);cursor:pointer;display:flex;align-items:center;justify-content:center;gap:5px;transition:color 0.15s;text-transform:uppercase">'+f.icon+f.label+'</button>';
  }).join('');
  if(!_thoughtsExpanded){
    html+='<button onclick="toggleThoughtsExpand()" style="padding:12px 10px;background:transparent;color:var(--text-inactive);font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;letter-spacing:0.1em;border:none;cursor:pointer;display:flex;align-items:center;justify-content:center;gap:4px;transition:color 0.15s;text-transform:uppercase;white-space:nowrap">MORE<svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor"><path d="M7 10l5 5 5-5z"/></svg></button>';
  }else{
    html+='<button onclick="toggleThoughtsExpand()" style="padding:12px 10px;background:transparent;color:var(--text-inactive);font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;letter-spacing:0.1em;border:none;cursor:pointer;display:flex;align-items:center;justify-content:center;gap:4px;transition:color 0.15s;text-transform:uppercase;white-space:nowrap">LESS<svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor"><path d="M7 14l5-5 5 5z"/></svg></button>';
  }
  c.innerHTML=html;
}
function toggleThoughtsExpand(){_thoughtsExpanded=!_thoughtsExpanded;renderThoughtFilters()}
function setThoughtsFilter(f){_thoughtsFilter=f;renderThoughtFilters();renderFilteredThoughts()}
function filterThoughtsSearch(){renderFilteredThoughts()}

async function openThoughtsPage(){
  document.getElementById('mainFab').style.display='none';_pushOv();
  var el=document.getElementById('thoughtsPage');el.scrollTop=0;el.classList.add('show');
  renderThoughtFilters();
  var container=document.getElementById('thoughtsContent');
  if(typeof renderSkeleton==='function') container.innerHTML=renderSkeleton('cards',3);
  else container.innerHTML='<div style="color:var(--muted);font-size:14px;text-align:center;padding:20px">Loading...</div>';
  try{
    var r=await fetch(BASE+'/thoughts');
    var j=await r.json();
    _allThoughtsData=j.thoughts||[];
    renderFilteredThoughts();
  }catch(e){
    container.innerHTML='<div style="color:var(--red);text-align:center;padding:20px">Failed to load</div>';
  }
}

function renderFilteredThoughts(){
  var container=document.getElementById('thoughtsContent');
  var thoughts=_allThoughtsData.slice();
  var searchVal=(document.getElementById('thoughtsSearchInput')?.value||'').toLowerCase().trim();
  if(_thoughtsFilter==='pinned'){
    thoughts=thoughts.filter(function(t){return t.pinned});
  }else if(_thoughtsFilter==='quick'){
    thoughts=thoughts.filter(function(t){return !t.visited});
  }else if(_thoughtsFilter==='gita'){
    thoughts=thoughts.filter(function(t){return(t.topic||'').toLowerCase()==='geeta'});
  }else if(_thoughtsFilter==='deleted'){
    thoughts=thoughts.filter(function(t){return t.deleted});
  }
  if(searchVal){
    thoughts=thoughts.filter(function(t){
      return (t.topic||'').toLowerCase().includes(searchVal)||(t.text||'').toLowerCase().includes(searchVal);
    });
  }
  if(thoughts.length===0){
    container.innerHTML='<div style="display:flex;flex-direction:column;align-items:center;justify-content:center;padding:48px 20px;opacity:0.3"><span class="material-symbols-outlined" style="font-size:40px;margin-bottom:12px">cloud_done</span><span style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:11px;text-transform:uppercase;letter-spacing:0.3em">SYSTEM CLEAR // WAITING FOR INPUT...</span></div>';
    return;
  }
  var pinSvg='<svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M16 3a1 1 0 0 1 .7.3l4 4a1 1 0 0 1-.18 1.42l-10.5 7a1 1 0 0 1-1.24-.06l-3.5-3.5a1 1 0 0 1 .06-1.24l10.5-7a1 1 0 0 1 1.36.34zM7.5 14.5l3 3L5 23l1.5-5.5z"/></svg>';
  var html='<div style="display:grid;grid-template-columns:1fr 1fr;grid-auto-rows:1fr;gap:1px;background:var(--border)">';
  thoughts.forEach(function(t,i){
    var time=t.ts?t.ts.slice(11,16)+' // '+t.ts.slice(5,10).replace(/-/g,'.'):'';
    var dateTag=t.ts?t.ts.slice(0,10).replace(/-/g,'.'):'';
    var borderColor=(i%2===0)?'border-right:1px solid var(--border)':'';
    var topic=t.topic||t.text?.slice(0,40)||'Untitled';
    var preview=t.text?(t.text.length>140?t.text.slice(0,140)+'...':t.text):'';
    var cardIcon='';
    if(t.pinned)cardIcon='<svg width="16" height="16" viewBox="0 0 24 24" fill="var(--red)"><path d="M16 3a1 1 0 0 1 .7.3l4 4a1 1 0 0 1-.18 1.42l-10.5 7a1 1 0 0 1-1.24-.06l-3.5-3.5a1 1 0 0 1 .06-1.24l10.5-7a1 1 0 0 1 1.36.34zM7.5 14.5l3 3L5 23l1.5-5.5z"/></svg>';
    else if(!t.visited)cardIcon='<svg width="16" height="16" viewBox="0 0 24 24" fill="var(--secondary)"><path d="M13 2L3 14h9l-1 10 10-12h-9l1-10z"/></svg>';
    else if((t.topic||'').toLowerCase()==='geeta')cardIcon='<svg width="16" height="16" viewBox="0 0 24 24" fill="var(--yellow)"><path d="M4 4h16v2H4zm0 4h16v12H4z"/></svg>';
    html+='<div style="background:var(--surface);padding:14px;'+borderColor+';transition:border-color 0.2s;cursor:pointer;display:flex;flex-direction:column;overflow:hidden" onclick="openThoughtDetail(\''+esc(t.id)+'\')">';
    html+='<div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:6px">';
    html+='<span style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em">'+time+'</span>';
    if(cardIcon)html+=cardIcon;
    html+='</div>';
    html+='<h3 style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:13px;font-weight:600;color:var(--text);margin-bottom:6px;line-height:1.3;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">'+esc(topic)+'</h3>';
    if(preview){
      html+='<p style="color:var(--text-inactive);font-size:11px;line-height:1.5;flex:1;opacity:0.7;display:-webkit-box;-webkit-line-clamp:3;-webkit-box-orient:vertical;overflow:hidden">'+esc(preview)+'</p>';
    }else{
      html+='<div style="flex:1"></div>';
    }
    html+='<div style="display:flex;align-items:center;justify-content:space-between;border-top:1px solid var(--border);padding-top:6px;margin-top:auto">';
    html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive)">'+dateTag+'</span>';
    html+='<span onclick="event.stopPropagation();pinThought(\''+t.id+'\','+(t.pinned?'false':'true')+')" style="color:var(--text-inactive);cursor:pointer;flex-shrink:0;display:flex">'+pinSvg+'</span>';
    html+='</div></div>';
  });
  html+='</div>';
  html+='<div style="display:flex;flex-direction:column;align-items:center;justify-content:center;padding:32px 20px;opacity:0.3"><span class="material-symbols-outlined" style="font-size:40px;margin-bottom:12px">cloud_done</span><span style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:11px;text-transform:uppercase;letter-spacing:0.3em">SYSTEM CLEAR // WAITING FOR INPUT...</span></div>';
  container.innerHTML=html;
}
function closeThoughtsPage(){document.getElementById('mainFab').style.display='flex';document.getElementById('thoughtsPage').classList.remove('show')}
function openThoughtDetail(id){
  var t=_allThoughtsData.find(function(x){return x.id===id});
  if(!t)return;
  var time=t.ts?t.ts.slice(11,16)+' // '+t.ts.slice(0,10).replace(/-/g,'.'):'';
  var topic=t.topic||'Untitled';
  var text=t.text||'No content';
  var tags='';
  if(t.pinned)tags+='<span style="padding:3px 8px;border:1px solid var(--red);color:var(--red);font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;letter-spacing:0.1em">PINNED</span>';
  tags+='<span style="padding:3px 8px;border:1px solid var(--border);color:var(--text-inactive);font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;letter-spacing:0.1em">THOUGHT</span>';
  var modal=document.createElement('div');
  modal.id='thoughtDetailModal';
  modal.style.cssText='position:fixed;top:0;left:0;width:100%;height:100%;z-index:200;display:flex;align-items:center;justify-content:center';
  modal.innerHTML='<div style="position:absolute;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.6)" onclick="closeThoughtDetail()"></div>'+
    '<div style="position:relative;width:90%;max-width:480px;max-height:80vh;background:var(--surface);border:1px solid var(--border);overflow-y:auto;padding:24px">'+
    '<div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:16px">'+
    '<span style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.15em">'+time+'</span>'+
    '<button onclick="closeThoughtDetail()" style="color:var(--text-inactive);background:none;border:none;cursor:pointer;padding:2px"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg></button>'+
    '</div>'+
    '<h3 style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:16px;font-weight:600;color:var(--text);margin-bottom:16px;line-height:1.4">'+esc(topic)+'</h3>'+
    '<p style="font-family:\'General Sans\',sans-serif;font-size:14px;line-height:1.8;color:var(--text-inactive);white-space:pre-wrap;margin-bottom:20px">'+esc(text)+'</p>'+
    '<div style="display:flex;align-items:center;justify-content:space-between;border-top:1px solid var(--border);padding-top:12px">'+
    '<div style="display:flex;gap:6px;align-items:center">'+tags+'</div>'+
    '<button onclick="deleteThoughtItem(\''+esc(t.id)+'\');closeThoughtDetail()" style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;color:var(--red);text-transform:uppercase;letter-spacing:0.1em;background:none;border:1px solid var(--red);padding:4px 12px;cursor:pointer;transition:all 0.15s">DELETE</button>'+
    '</div></div>';
  document.body.appendChild(modal);
}
function closeThoughtDetail(){var m=document.getElementById('thoughtDetailModal');if(m)m.remove()}

async function toggleThoughtVisited(id,visited){
  try{
    await fetch(BASE+'/thought/visit?thought_id='+id+'&visited='+visited,{method:'POST'});
  }catch(e){}
}

async function deleteThoughtItem(id){
  if(!confirm('Move to recycle bin?'))return;
  try{
    var r=await fetch(BASE+'/event/thought/'+id,{method:'DELETE'});
    var j=await r.json();
    if(j.ok||j.recycled){toast('Moved to recycle bin');openThoughtsPage();loadDashboard()}
  }catch(e){toast('Error')}
}
