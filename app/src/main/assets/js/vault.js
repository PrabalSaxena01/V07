// ─── Vault / Time Capsule ───
var VAULT_TAGS_DATA=[];
var VAULT_SELECTED_TAG='';
var VAULT_SEARCH_TIMEOUT=null;
var VAULT_SHARES_DATA=[];

async function openVaultPage(){
  document.getElementById('mainFab').style.display='none';_pushOv();
  var el=document.getElementById('vaultPage');el.scrollTop=0;el.classList.add('show');
  document.getElementById('vaultSearch').value='';
  VAULT_SELECTED_TAG='';
  await loadVaultTags();
  loadVaultShares();
}
function closeVaultPage(){document.getElementById('mainFab').style.display='flex';document.getElementById('vaultPage').classList.remove('show')}

async function loadVaultTags(){
  try{
    var data=await api('GET','/tags');
    VAULT_TAGS_DATA=data.tags||[];
    renderVaultTags();
  }catch(e){}
}

function renderVaultTags(){
  var c=document.getElementById('vaultTags');
  var html='<span class="chip'+(VAULT_SELECTED_TAG===''? ' selected':'')+'" onclick="filterVaultByTag(\'\')">All</span>';
  VAULT_TAGS_DATA.forEach(function(t){
    html+='<span class="chip'+(VAULT_SELECTED_TAG===t.name?' selected':'')+'" onclick="filterVaultByTag(\''+esc(t.name)+'\')">'+esc(t.name)+'</span>';
  });
  c.innerHTML=html;
}

function filterVaultByTag(tag){
  VAULT_SELECTED_TAG=tag;
  renderVaultTags();
  loadVaultShares();
}

function debounceVaultSearch(){
  clearTimeout(VAULT_SEARCH_TIMEOUT);
  VAULT_SEARCH_TIMEOUT=setTimeout(loadVaultShares,300);
}

async function loadVaultShares(){
  var container=document.getElementById('vaultContent');
  container.innerHTML='<div style="color:var(--muted);font-size:14px;text-align:center;padding:20px;">Loading...</div>';
  try{
    var params=[];
    var search=document.getElementById('vaultSearch').value.trim();
    if(search) params.push('search='+encodeURIComponent(search));
    if(VAULT_SELECTED_TAG) params.push('tag='+encodeURIComponent(VAULT_SELECTED_TAG));
    var qs=params.length?'?'+params.join('&'):'';
    var data=await api('GET','/shares'+qs);
    VAULT_SHARES_DATA=data.shares||[];
    renderVaultShares();
  }catch(e){
    container.innerHTML='<div style="color:var(--red);text-align:center;padding:20px;">Failed to load<br><span style="font-size:11px">'+esc(e.message||e)+'</span></div>';
  }
}

function renderVaultShares(){
  var container=document.getElementById('vaultContent');
  if(!VAULT_SHARES_DATA.length){
    container.innerHTML='<div style="color:var(--muted);text-align:center;padding:20px;">No shares found</div>';
    return;
  }
  var html='';
  VAULT_SHARES_DATA.forEach(function(s){
    var typeIcon=s.type==='image'?'&#x1F5BC;':s.type==='url'?'&#x1F517;':s.type==='video'?'&#x1F3AC;':s.type==='audio'?'&#x1F3B5;':s.type==='pdf'?'&#x1F4D6;':'&#x1F4DD;';
    var dateStr=s.date||'';
    var metaParts=[];
    if(s.captured_time) metaParts.push(s.captured_time);
    if(s.location) metaParts.push(s.location);
    var metaStr=metaParts.length?metaParts.join(' \u00b7 '):'';
    var tags=s.tags?s.tags.split(',').filter(function(t){return t.trim()}).map(function(t){
      var tn=t.trim();
      var isBuiltIn=tn==='URL'||tn==='Image'||tn==='Video'||tn==='Audio'||tn==='PDF';
      var bg=isBuiltIn?'var(--card2)':'var(--accent)';
      return '<span style="display:inline-block;padding:2px 8px;background:'+bg+';color:var(--text);border-radius:10px;font-size:10px;margin-left:4px">'+esc(tn)+'</span>';
    }).join(''):'';
    var pinIcon=s.pinned?'&#x1F4CC; ':'';
    var pinStyle=s.pinned?'color:#f5a623;':'';
    html+='<div class="card" style="padding:12px;margin-bottom:8px;cursor:pointer'+(s.pinned?';border-left:3px solid #f5a623':'')+'" onclick="showVaultDetail(\''+esc(s.id)+'\')">';
    html+='<div style="display:flex;align-items:center;gap:8px;margin-bottom:4px">';
    html+='<span style="font-size:14px;cursor:pointer;flex-shrink:0" title="'+(s.pinned?'Unpin':'Pin')+'" onclick="event.stopPropagation();togglePinShare(\''+esc(s.id)+'\')">'+pinIcon+'</span>';
    html+='<span style="font-size:16px">'+typeIcon+'</span>';
    html+='<span style="font-size:14px;font-weight:600;color:var(--text);flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">'+esc(s.title||'Untitled')+'</span>';
    html+='<span style="font-size:11px;color:var(--muted);white-space:nowrap;margin-right:4px">'+esc(dateStr)+'</span>';
    html+='<span onclick="event.stopPropagation();deleteVaultShare(\''+esc(s.id)+'\')" style="font-size:14px;color:var(--red);cursor:pointer;flex-shrink:0" title="Delete">&#x2716;</span>';
    html+='</div>';
    if(metaStr) html+='<div style="font-size:11px;color:var(--muted);margin-bottom:4px">&#x1F4CD; '+esc(metaStr)+'</div>';
    if(s.type==='image'&&s.file_path){
      var fname=s.file_path.split('/').pop();
      html+='<div style="margin:6px 0;border-radius:8px;overflow:hidden;max-height:120px"><img src="/vault-media/'+esc(fname)+'" style="width:100%;height:120px;object-fit:cover" onerror="this.parentElement.style.display=\'none\'"></div>';
    }
    if((s.type==='video'||s.type==='audio')&&s.file_path){
      var fname=s.file_path.split('/').pop();
      var isAudio=s.type==='audio';
      html+='<div style="margin:6px 0">'+(isAudio?'<audio':'<video')+' controls style="width:100%;max-height:100px;border-radius:8px" src="/vault-media/'+esc(fname)+'"></'+(isAudio?'audio':'video')+'></div>';
    }
    if(s.type==='pdf'&&s.file_path){
      var fname=s.file_path.split('/').pop();
      html+='<div style="margin:6px 0;padding:8px;background:var(--card2);border-radius:8px;font-size:12px;color:var(--accent)">&#x1F4D6; PDF Document — <a href="/vault-media/'+esc(fname)+'" target="_blank" style="color:var(--accent)">Open</a></div>';
    }
    if(s.type==='url'&&s.text){
      var isMedia=(s.text).match(/\.(mp4|webm|ogg|mp3|wav|m4a|mkv|avi|mov|opus)$/i);
      var isPdf=(s.text).match(/\.pdf$/i);
      var fname=(s.file_path||s.text).split('/').pop();
      if(isMedia&&s.file_path){
        var isAudio=s.text.match(/\.(ogg|mp3|wav|m4a|opus)$/i);
        html+='<div style="margin:6px 0">'+(isAudio?'<audio':'<video')+' controls style="width:100%;max-height:100px;border-radius:8px" src="/vault-media/'+esc(fname)+'"></'+(isAudio?'audio':'video')+'></div>';
      }else if(isPdf&&s.file_path){
        html+='<div style="margin:6px 0;padding:8px;background:var(--card2);border-radius:8px;font-size:12px;color:var(--accent)">&#x1F4D6; PDF — <a href="/vault-media/'+esc(fname)+'" target="_blank" style="color:var(--accent)">Open</a></div>';
      }else{
        html+='<div style="font-size:12px;color:var(--accent);margin-bottom:4px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">'+esc(s.text)+'</div>';
      }
    }
    if(s.type==='text'&&s.text){
      html+='<div style="font-size:12px;color:var(--muted);margin-bottom:4px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">'+esc(s.text.substring(0,120))+'</div>';
    }
    if(s.info) html+='<div style="font-size:12px;color:var(--muted);margin-bottom:4px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">'+esc(s.info)+'</div>';
    if(tags) html+='<div style="margin-top:4px">'+tags+'</div>';
    html+='</div>';
  });
  container.innerHTML=html;
}

function showVaultDetail(id){
  var s=VAULT_SHARES_DATA.find(function(x){return x.id===id});
  if(!s) return;
  var typeIcon=s.type==='image'?'&#x1F5BC;':s.type==='url'?'&#x1F517;':s.type==='video'?'&#x1F3AC;':s.type==='audio'?'&#x1F3B5;':s.type==='pdf'?'&#x1F4D6;':'&#x1F4DD;';
  var tags=s.tags?s.tags.split(',').filter(function(t){return t.trim()}).map(function(t){
    var tn=t.trim();
    var isBuiltIn=tn==='URL'||tn==='Image'||tn==='Video'||tn==='Audio'||tn==='PDF';
    var bg=isBuiltIn?'var(--card2)':'var(--accent)';
    return '<span style="display:inline-block;padding:3px 10px;background:'+bg+';color:var(--text);border-radius:12px;font-size:11px">'+esc(tn)+'</span>';
  }).join(' '):'';
  var html='<div style="text-align:center;font-size:24px;margin-bottom:8px">'+typeIcon+'</div>';
  html+='<h3 style="font-size:16px;margin:0 0 8px 0;font-family:\'JetBrains Mono\',\'Mono\',monospace">'+esc(s.title||'Untitled')+'</h3>';
  if(s.info) html+='<p style="font-size:13px;color:var(--muted);margin:0 0 8px 0">'+esc(s.info)+'</p>';
  var metaItems=[];
  if(s.date) metaItems.push('&#x1F4C5; '+esc(s.date));
  if(s.captured_time) metaItems.push('&#x1F552; '+esc(s.captured_time));
  if(s.location) metaItems.push('&#x1F4CD; '+esc(s.location));
  if(metaItems.length) html+='<div style="font-size:12px;color:var(--muted);margin-bottom:8px">'+metaItems.join(' &nbsp; ')+'</div>';
  if(s.type==='image'&&s.file_path){
    var fname=s.file_path.split('/').pop();
    html+='<div style="margin-bottom:8px;border-radius:8px;overflow:hidden"><img src="/vault-media/'+esc(fname)+'" style="width:100%;border-radius:8px" onerror="this.style.display=\'none\'"></div>';
  }
  if((s.type==='video'||s.type==='audio')&&s.file_path){
    var fname=s.file_path.split('/').pop();
    var isAudio=s.type==='audio';
    html+='<div style="margin-bottom:8px">'+(isAudio?'<audio':'<video')+' controls style="width:100%;border-radius:8px" src="/vault-media/'+esc(fname)+'"></'+(isAudio?'audio':'video')+'></div>';
  }
  if(s.type==='pdf'&&s.file_path){
    var fname=s.file_path.split('/').pop();
    html+='<div style="margin-bottom:8px;padding:12px;background:var(--card2);border-radius:8px;text-align:center"><div style="font-size:32px;margin-bottom:8px">&#x1F4D6;</div><a href="/vault-media/'+esc(fname)+'" target="_blank" style="color:var(--accent);font-size:14px;text-decoration:underline">Open PDF</a></div>';
  }
  if(s.type==='url'&&s.text){
    var isMedia=(s.text).match(/\.(mp4|webm|ogg|mp3|wav|m4a|mkv|avi|mov|opus)$/i);
    var isPdf=(s.text).match(/\.pdf$/i);
    var fname=(s.file_path||s.text).split('/').pop();
    if(isMedia&&s.file_path){
      var isAudio=s.text.match(/\.(ogg|mp3|wav|m4a|opus)$/i);
      html+='<div style="margin-bottom:8px">'+(isAudio?'<audio':'<video')+' controls style="width:100%;border-radius:8px" src="/vault-media/'+esc(fname)+'"></'+(isAudio?'audio':'video')+'></div>';
    }else if(isPdf&&s.file_path){
      html+='<div style="margin-bottom:8px;padding:12px;background:var(--card2);border-radius:8px;text-align:center"><div style="font-size:32px;margin-bottom:8px">&#x1F4D6;</div><a href="/vault-media/'+esc(fname)+'" target="_blank" style="color:var(--accent);font-size:14px;text-decoration:underline">Open PDF</a></div>';
    }else{
      html+='<a href="'+esc(s.text)+'" target="_blank" style="display:block;padding:10px;background:var(--card2);border-radius:8px;font-size:13px;color:var(--accent);word-break:break-all;margin-bottom:8px;text-decoration:underline">'+esc(s.text)+'</a>';
    }
  }
  if(s.type==='text'&&s.text){
    html+='<div style="padding:10px;background:var(--card2);border-radius:8px;font-size:12px;color:var(--text);word-break:break-word;margin-bottom:8px;white-space:pre-wrap">'+esc(s.text)+'</div>';
  }
  if(tags) html+='<div style="margin-bottom:8px">'+tags+'</div>';
  document.getElementById('modalOverlay').innerHTML='<div class="modal" style="max-width:400px">'+html+'<div class="modal-btns"><button class="btn" onclick="togglePinShare(\''+esc(s.id)+'\')">'+(s.pinned?'Unpin':'Pin')+'</button><button class="btn btn-red" onclick="deleteVaultShare(\''+esc(s.id)+'\')">Delete</button><button class="btn" onclick="closeModal()">Close</button></div></div>';
  document.getElementById('modalOverlay').classList.add('show');
}

async function deleteVaultShare(id){
  if(!confirm('Move to Recycle Bin?')) return;
  try{
    var data=await api('DELETE','/shares?id='+id);
    if(data.ok){
      closeModal();
      toast('Moved to Recycle Bin');
      loadVaultShares();
    }else{toast(data.error||'Failed');}
  }catch(e){toast('Error: '+e.message);}
}

async function togglePinShare(id){
  try{
    var data=await api('POST','/shares/pin?id='+id);
    if(data.ok){
      toast(data.pinned?'Pinned':'Unpinned');
      closeModal();
      loadVaultShares();
    }
  }catch(e){toast('Error: '+e.message);}
}

var VAULT_RECYCLE_DATA=[];
async function loadRecycleShares(){
  VAULT_RECYCLE_DATA=[];
  document.getElementById('vaultTags').innerHTML='';
  document.getElementById('vaultSearch').placeholder='Search recycle bin...';
  document.getElementById('vaultSearch').value='';
  var container=document.getElementById('vaultContent');
  container.innerHTML='<div style="color:var(--muted);font-size:14px;text-align:center;padding:20px;">Loading...</div>';
  try{
    var data=await api('GET','/shares?deleted=true');
    VAULT_RECYCLE_DATA=data.shares||[];
    renderRecycleShares();
  }catch(e){
    container.innerHTML='<div style="color:var(--red);text-align:center;padding:20px;">Failed to load</div>';
  }
}

function renderRecycleShares(){
  var container=document.getElementById('vaultContent');
  var html='<div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px"><button class="btn" onclick="backToTimeCapsule()" style="font-size:12px">&#x2190; Back to Time Capsule</button><span style="font-size:12px;color:var(--muted)">'+VAULT_RECYCLE_DATA.length+' items</span></div>';
  if(!VAULT_RECYCLE_DATA.length){
    html+='<div style="color:var(--muted);text-align:center;padding:20px;">Recycle bin is empty</div>';
    container.innerHTML=html;
    return;
  }
  VAULT_RECYCLE_DATA.forEach(function(s){
    var typeIcon=s.type==='image'?'&#x1F5BC;':s.type==='url'?'&#x1F517;':s.type==='video'?'&#x1F3AC;':s.type==='audio'?'&#x1F3B5;':s.type==='pdf'?'&#x1F4D6;':'&#x1F4DD;';
    html+='<div class="card" style="padding:12px;margin-bottom:8px;opacity:0.7">';
    html+='<div style="display:flex;align-items:center;gap:8px;margin-bottom:4px">';
    html+='<span style="font-size:16px">'+typeIcon+'</span>';
    html+='<span style="font-size:14px;font-weight:600;color:var(--text);flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">'+esc(s.title||'Untitled')+'</span>';
    html+='</div>';
    html+='<div style="display:flex;gap:8px;margin-top:6px">';
    html+='<button class="btn" onclick="restoreShare(\''+esc(s.id)+'\')" style="font-size:12px;padding:4px 10px">&#x21A9; Restore</button>';
    html+='<button class="btn btn-red" onclick="permanentlyDeleteShare(\''+esc(s.id)+'\')" style="font-size:12px;padding:4px 10px">&#x1F5D1; Delete Forever</button>';
    html+='</div></div>';
  });
  container.innerHTML=html;
}

function backToTimeCapsule(){
  document.getElementById('vaultSearch').placeholder='Search time capsule...';
  document.getElementById('vaultSearch').value='';
  loadVaultTags();
  loadVaultShares();
}

async function restoreShare(id){
  try{
    var data=await api('POST','/shares/restore?id='+id);
    if(data.ok){
      toast('Restored');
      loadRecycleShares();
    }else{toast(data.error||'Failed');}
  }catch(e){toast('Error: '+e.message);}
}

async function permanentlyDeleteShare(id){
  if(!confirm('Permanently delete this item? This cannot be undone.')) return;
  try{
    var data=await api('DELETE','/shares/permanent?id='+id);
    if(data.ok){
      toast('Permanently deleted');
      loadRecycleShares();
    }else{toast(data.error||'Failed');}
  }catch(e){toast('Error: '+e.message);}
}

function openTagManager(){
  document.getElementById('tagManagerModal').classList.add('show');
  renderTagManagerList();
}
function closeTagManager(){document.getElementById('tagManagerModal').classList.remove('show')}

function renderTagManagerList(){
  var c=document.getElementById('tagManagerList');
  if(!VAULT_TAGS_DATA.length){
    c.innerHTML='<div style="color:var(--muted);text-align:center;padding:12px;font-size:13px">No tags yet</div>';
    return;
  }
  var html='';
  VAULT_TAGS_DATA.forEach(function(t){
    var builtIn=t.id&&t.id.startsWith('_');
    html+='<div style="display:flex;align-items:center;justify-content:space-between;padding:8px 10px;background:var(--card);border-radius:8px">';
    html+='<span style="font-size:13px;color:var(--text)">'+esc(t.name)+(builtIn?' <span style="font-size:10px;color:var(--muted)">(auto)</span>':'')+'</span>';
    if(!builtIn) html+='<button class="btn btn-red" onclick="deleteTag(\''+esc(t.id)+'\')" style="padding:4px 10px;font-size:11px">Delete</button>';
    html+='</div>';
  });
  c.innerHTML=html;
}

async function createTag(){
  var input=document.getElementById('newTagName');
  var name=input.value.trim();
  if(!name){toast('Enter tag name');return;}
  try{
    var data=await api('POST','/tags',{name:name});
    if(data.ok){
      input.value='';
      await loadVaultTags();
      renderTagManagerList();
      toast('Tag created');
    }else{toast(data.error||'Failed');}
  }catch(e){toast('Error: '+e.message);}
}

async function deleteTag(id){
  try{
    var data=await api('DELETE','/tags?id='+id);
    if(data.ok){
      await loadVaultTags();
      renderTagManagerList();
      if(VAULT_SELECTED_TAG) VAULT_SELECTED_TAG='';
      renderVaultTags();
      toast('Tag deleted');
    }
  }catch(e){toast('Error: '+e.message);}
}
