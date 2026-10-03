// ─── Gita ───
// ─── Gita ─────────────────────────────────────────────────────────────────────

var gitaData=null;

function renderGita(verse){
  gitaData=verse;
  if(!verse||verse.done){
    document.getElementById('gitaChapterLabel').textContent=verse?.message||'All verses read!';
    document.getElementById('gitaVerseCounter').textContent='';
    document.getElementById('gitaPageShloka').textContent='';
    document.getElementById('gitaPageTrans').textContent='';
    document.getElementById('gitaThoughtInput').style.display='none';
    document.getElementById('gitaActionsSection').style.display='none';
    return;
  }
  var isRead=verse.already_read;
  var chName='';
  var _chNames={1:'Arjuna Vishada',2:'Sankhya',3:'Karma',4:'Jnana',5:'Karma Vairagya',6:'Dhyana',7:'Jnana Vijnana',8:'Aksara Brahma',9:'Raja Vidya',10:'Vibhuti',11:'Visvarupa',12:'Bhakti',13:'Prakrti Purusha',14:'Gunatraya',15:'Purushottama',16:'Daivasura',17:'Sraddhatraya',18:'Mokshasannyasa'};
  chName=_chNames[verse.chapter]||'';
  document.getElementById('gitaChapterLabel').textContent='CHAPTER '+verse.chapter+': '+chName;
  document.getElementById('gitaVerseCounter').textContent='VERSE '+verse.verse+' / 700';

  var shloka=verse.shloka?.text||'';
  document.getElementById('gitaPageShloka').textContent=shloka;

  var eng=verse.english_sivananda;
  document.getElementById('gitaPageTrans').textContent=eng?.text||'';

  var ht=verse.hindi_translation;
  var htSection=document.getElementById('gitaHindiTransSection');
  if(ht&&ht.text){htSection.style.display='block';document.getElementById('gitaPageHindiTrans').textContent=ht.text}
  else{htSection.style.display='none'}

  var hc=verse.hindi_commentary;
  var hcSection=document.getElementById('gitaHindiCommSection');
  if(hc&&hc.text){hcSection.style.display='block';document.getElementById('gitaPageHindiComm').textContent=hc.text}
  else{hcSection.style.display='none'}

  var gb=verse.english_gambirananda;
  var gbSection=document.getElementById('gitaGambSection');
  if(gb&&gb.text){gbSection.style.display='block';document.getElementById('gitaPageGamb').textContent=gb.text}
  else{gbSection.style.display='none'}

  document.getElementById('gitaThoughtInput').style.display='none';
  document.getElementById('gitaActionsSection').style.display='flex';
  loadGitaVerseThoughts();

  var unread=verse.unread_count||0;
  var total=verse.total_verses||701;
  var read=total-unread;
  document.getElementById('gitaPageProgress').textContent=read+'/'+total+' read';

  var readBtn=document.getElementById('gitaPageReadBtn');
  if(readBtn){readBtn.textContent=isRead?'MARK UNREAD':'MARK READ';readBtn.style.borderColor=isRead?'var(--yellow)':'var(--primary)';readBtn.style.color=isRead?'var(--yellow)':'var(--primary)';}

  var dotsEl=document.getElementById('gitaProgressDots');
  var dots='';
  var chapterCount=Math.ceil(total/(verse.verse_per_chapter||40));
  for(var i=1;i<=chapterCount;i++){
    if(i==verse.chapter){dots+='<div style="width:2px;height:16px;background:var(--primary);border-radius:1px"></div>'}
    else{dots+='<div style="width:4px;height:4px;background:var(--border);border-radius:50%"></div>'}
  }
  dotsEl.innerHTML='<div style="display:flex;flex-direction:column;gap:8px;align-items:center">'+dots+'</div>';

  var readingArea=document.getElementById('gitaReadingArea');
  if(readingArea)readingArea.scrollTop=0;

  setupGitaScrollAnimations();
}

var _gitaObserver=null;
function setupGitaScrollAnimations(){
  if(_gitaObserver)_gitaObserver.disconnect();
  var sections=document.querySelectorAll('#gitaReadingArea .gita-section');
  sections.forEach(function(s){s.classList.remove('visible')});
  _gitaObserver=new IntersectionObserver(function(entries){
    entries.forEach(function(entry){
      if(entry.isIntersecting){entry.target.classList.add('visible');_gitaObserver.unobserve(entry.target)}
    });
  },{root:document.getElementById('gitaReadingArea'),threshold:0.15});
  sections.forEach(function(s){_gitaObserver.observe(s)});
  setTimeout(function(){sections[0]&&sections[0].classList.add('visible')},100);
}

async function toggleGitaRead(){
  if(!gitaData||gitaData.done)return;
  try{
    if(gitaData.already_read){
      await fetch(BASE+'/gita/unread?verse_id='+gitaData.id,{method:'POST'});
      gitaData.already_read=false;
      toast('Marked unread');
    }else{
      var r=await fetch(BASE+'/gita/read',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({verse_id:gitaData.id})});
      var j=await r.json();
      if(j.ok){gitaData.already_read=true;toast('Marked read')}
      else if(j.already_read){gitaData.already_read=true;toast('Already read')}
    }
    loadDashboard()
  }catch(e){toast('Error')}
}

async function navGita(dir){
  if(!gitaData||gitaData.done)return;
  var area=document.getElementById('gitaReadingArea');
  if(area){
    area.classList.remove('slide-out-next','slide-out-prev');
    area.classList.add(dir==='next'?'slide-out-next':'slide-out-prev');
  }
  var endpoint=dir==='next'?'/gita/next?verse_id=':'/gita/prev?verse_id=';
  try{
    var r=await fetch(BASE+endpoint+gitaData.id);
    var j=await r.json();
    if(j.done){if(area)area.classList.remove('slide-out-next','slide-out-prev');toast(j.message);return}
    j.already_read=j.already_read||false;
    renderGita(j);
    if(area){
      requestAnimationFrame(function(){
        area.classList.remove('slide-out-next','slide-out-prev');
      });
    }
  }catch(e){if(area)area.classList.remove('slide-out-next','slide-out-prev');toast('Error')}
}

async function submitGitaThought(){
  var ta=document.getElementById('gitaThoughtText');
  var text=ta.value.trim();
  if(!text){toast('Write something first');return}
  if(!gitaData||!gitaData.id){toast('No verse selected');return}
  try{
    var r=await fetch(BASE+'/gita/review',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({verse_id:gitaData.id,text:text,rating:3,type:'reflection'})});
    var j=await r.json();
    if(!j.ok){toast('Failed to save');return}
    ta.value='';
    document.getElementById('gitaThoughtInput').style.display='none';
    toast('Thought saved!');
    loadGitaVerseThoughts();
  }catch(e){toast('Error saving thought')}
}

async function loadGitaVerseThoughts(){
  var el=document.getElementById('gitaVerseThoughts');
  if(!el||!gitaData||!gitaData.id){if(el)el.innerHTML='';return}
  try{
    var r=await fetch(BASE+'/gita/thoughts?verse_id='+encodeURIComponent(gitaData.id));
    if(!r.ok){el.innerHTML='';return}
    var j=await r.json();
    var thoughts=j.thoughts||[];
    if(thoughts.length===0){el.innerHTML='';return}
    var html='<div style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.2em;margin-bottom:12px;text-align:center">Thoughts on this verse</div>';
    thoughts.forEach(function(t){
      var ts=(t.ts||'').slice(0,16).replace('T',' ');
      html+='<div style="padding:10px 12px;border:1px solid var(--border);background:var(--surface);margin-bottom:8px;display:flex;align-items:flex-start;gap:10px">';
      html+='<div style="flex:1;font-family:\'General Sans\',sans-serif;font-size:13px;line-height:1.6;color:var(--text)">'+esc(t.text||'')+'</div>';
      html+='<span onclick="deleteGitaVerseThought(\''+t.id+'\')" style="cursor:pointer;color:var(--text-inactive);flex-shrink:0;transition:color 0.15s" onmouseenter="this.style.color=\'var(--red)\'" onmouseleave="this.style.color=\'var(--text-inactive)\'"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg></span>';
      html+='</div>';
    });
    el.innerHTML=html;
  }catch(e){el.innerHTML=''}
}

async function deleteGitaVerseThought(id){
  if(!confirm('Delete this thought?'))return;
  try{
    await fetch(BASE+'/event/thought/'+id,{method:'DELETE'});
    toast('Thought deleted');
    loadGitaVerseThoughts();
  }catch(e){toast('Error')}
}



function openGitaPage(){
  document.getElementById('mainFab').style.display='none';
  var el=document.getElementById('gitaPage');
  el.scrollTop=0;
  el.classList.add('show');
  var ra=document.getElementById('gitaReadingArea');
  if(ra)ra.scrollTop=0;
  initGitaSwipe();
  setTimeout(function(){setupGitaScrollAnimations()},200);
  if(!gitaData){
    fetch(BASE+'/gita/today').then(function(r){return r.json()}).then(function(v){
      renderGita(v);
    }).catch(function(e){console.error('Failed to load gita today',e)});
  } else {
    loadGitaVerseThoughts();
  }
  try{history.pushState({overlay:'gitaPage'},'',location.href)}catch(e){}
}
function closeGitaPage(){document.getElementById('mainFab').style.display='flex';closeGitaSidebar();document.getElementById('gitaPage').classList.remove('show');document.getElementById('gitaThoughtInput').style.display='none'}
var _gitaSwipeInit=false;var _gitaSwipeX=0;var _gitaSwipeY=0;
function initGitaSwipe(){if(_gitaSwipeInit)return;_gitaSwipeInit=true;var el=document.getElementById('gitaReadingArea');if(!el)return;el.addEventListener('touchstart',function(e){_gitaSwipeX=e.touches[0].clientX;_gitaSwipeY=e.touches[0].clientY},{passive:true});el.addEventListener('touchend',function(e){var dx=e.changedTouches[0].clientX-_gitaSwipeX;var dy=Math.abs(e.changedTouches[0].clientY-_gitaSwipeY);if(dy>40)return;if(dx<-80){navGita('next')}else if(dx>80){navGita('prev')}},{passive:true})}
function toggleGitaThoughtInput(){var s=document.getElementById('gitaThoughtInput');if(s.style.display==='none'||!s.style.display){s.style.display='block';var ta=document.getElementById('gitaThoughtText');if(ta){ta.value='';setTimeout(function(){ta.focus()},100)}setTimeout(function(){var ra=document.getElementById('gitaReadingArea');if(ra)ra.scrollTop=ra.scrollHeight},50)}else{s.style.display='none'}}
function closeGitaThoughtInput(){document.getElementById('gitaThoughtInput').style.display='none'}

// ─── Gita Tracker Sidebar ─────────────────────────────────────────────────────

var gitaTrackerData=null;

async function openGitaSidebar(){
  if(!document.getElementById('gitaPage').classList.contains('show'))return;
  document.getElementById('gitaSidebar').classList.add('open');
  document.getElementById('gitaSidebarOverlay').classList.add('show');
  await loadGitaTracker();
}
function closeGitaSidebar(){
  document.getElementById('gitaSidebar').classList.remove('open');
  document.getElementById('gitaSidebarOverlay').classList.remove('show');
}

async function loadVerseFromSidebar(verseId){
  try{
    var r=await fetch(BASE+'/gita/verse/'+verseId);
    var j=await r.json();
    if(j.shloka||j.english_sivananda||j.hindi_translation){
      j.already_read=gitaTrackerData?.verses?.find(function(v){return v.id===verseId})?.read||false;
      var readIds=gitaTrackerData?.verses?.filter(function(v){return v.read}).length||0;
      var total=gitaTrackerData?.total||701;
      j.total_verses=total;
      j.read_count=readIds;
      j.unread_count=total-readIds;
      renderGita(j);
      closeGitaSidebar();
      openGitaPage();
    }else{
      toast('Verse data not found');
    }
  }catch(e){toast('Failed to load verse')}
}

async function loadGitaTracker(){
  var container=document.getElementById('gitaTrackerList');
  if(gitaTrackerData){renderGitaTracker(gitaTrackerData);return}
  try{
    var r=await fetch(BASE+'/gita/all');
    var j=await r.json();
    gitaTrackerData=j;
    renderGitaTracker(j);
  }catch(e){
    container.innerHTML='<div style="color:var(--red);font-size:13px;text-align:center;padding:10px">Failed to load</div>';
  }
}

function toggleChVerses(chNum){
  var el=document.getElementById('chVerses_'+chNum);
  var arrow=document.getElementById('chArrow_'+chNum);
  if(!el)return;
  if(el.classList.contains('open')){el.classList.remove('open');if(arrow)arrow.style.transform='rotate(0deg)'}
  else{el.classList.add('open');if(arrow)arrow.style.transform='rotate(90deg)'}
}
function renderGitaTracker(data){
  var container=document.getElementById('gitaTrackerList');
  var chapters=data.chapters||[];
  var readCount=data.read_count||0;
  var total=data.total||701;
  document.getElementById('gitaTrackerProgress').innerHTML=
    '<span style="color:var(--secondary)">'+readCount+'</span><span style="color:var(--text-inactive)">/'+total+' read</span>'+
    '<span style="margin-left:8px;color:var(--text-inactive)">('+(total-readCount)+' remaining)</span>';
  var firstUnread=null;
  for(var ci=0;ci<chapters.length;ci++){
    var chVerses=chapters[ci].verses||[];
    for(var vi=0;vi<chVerses.length;vi++){
      if(!chVerses[vi].read){firstUnread=chapters[ci].number;break;}
    }
    if(firstUnread)break;
  }
  var html='';
  chapters.forEach(function(ch){
    var chRead=ch.read||0;
    var chTotal=ch.total||0;
    var isOpen=ch.number==firstUnread;
    var chPct=chTotal>0?Math.round((chRead/chTotal)*100):0;
    html+='<div class="gita-chapter-block">';
    html+='<div class="ch-header" onclick="toggleChVerses('+ch.number+')">';
    html+='<span id="chArrow_'+ch.number+'" style="display:inline-block;transition:transform 0.2s;font-size:10px;color:var(--text-inactive);'+(isOpen?'transform:rotate(90deg)':'')+'">&#9654;</span>';
    html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive);min-width:18px">'+ch.number+'</span>';
    html+='<div style="flex:1;min-width:0">';
    html+='<div style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:11px;text-transform:uppercase;letter-spacing:0.08em;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">'+(ch.name||'Chapter '+ch.number)+'</div>';
    html+='<div style="display:flex;align-items:center;gap:6px;margin-top:4px">';
    html+='<div style="flex:1;height:2px;background:var(--border);overflow:hidden;border-radius:1px"><div style="height:100%;width:'+chPct+'%;background:'+(chPct===100?'var(--secondary)':'var(--primary)')+';border-radius:1px"></div></div>';
    html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive);white-space:nowrap">'+chRead+'/'+chTotal+'</span>';
    html+='</div></div>';
    html+='</div>';
    html+='<div class="ch-verses'+(isOpen?' open':'')+'" id="chVerses_'+ch.number+'">';
    var verses=ch.verses||[];
    verses.forEach(function(v){
      var readColor=v.read?'var(--text-inactive)':'var(--text)';
      var opacity=v.read?'0.45':'1';
      html+='<div data-verse-id="'+esc(v.id)+'" style="display:flex;align-items:flex-start;gap:8px;padding:8px 4px;border-bottom:1px solid rgba(44,44,44,0.4);cursor:pointer;opacity:'+opacity+';transition:opacity 0.15s" onclick="loadVerseFromSidebar(\''+esc(v.id)+'\')">';
      html+='<input type="checkbox" '+(v.read?'checked':'')+' onclick="event.stopPropagation();toggleVerseRead(\''+esc(v.id)+'\',this.checked)" style="width:12px;height:12px;margin-top:2px;cursor:pointer;flex-shrink:0;accent-color:var(--primary)">';
      html+='<div style="flex:1;min-width:0">';
      html+='<div style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:10px;color:var(--primary);margin-bottom:2px">'+v.id+'</div>';
      if(v.shloka_preview){
        html+='<div style="font-size:11px;color:'+readColor+';line-height:1.4;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;font-family:\'Marcellus\',serif">'+esc(v.shloka_preview)+'</div>';
      }
      html+='</div>';
      html+='</div>';
    });
    html+='</div></div>';
  });
  container.innerHTML=html;
}

async function toggleVerseRead(verseId, isRead){
  try{
    var row=event&&event.target?event.target.closest('[data-verse-id]'):null;
    if(row){row.style.opacity=isRead?'0.45':'1'}
    if(isRead){
      await fetch(BASE+'/gita/read',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({verse_id:verseId})});
    }else{
      await fetch(BASE+'/gita/unread?verse_id='+verseId,{method:'POST'});
    }
    gitaTrackerData=null;
    await loadGitaTracker();
    loadDashboard();
  }catch(e){toast('Error')}
}
