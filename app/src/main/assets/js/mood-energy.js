// ─── Mood/Energy Timeline ───

function openMoodEnergyPage(){
  document.getElementById('mainFab').style.display='none';_pushOv();
  var el=document.getElementById('moodEnergyPage');el.scrollTop=0;el.classList.add('show');
  loadMoodEnergyTimeline()
}
function closeMoodEnergyPage(){document.getElementById('mainFab').style.display='flex';document.getElementById('moodEnergyPage').classList.remove('show')}

var MOOD_COLORS={'happy':'var(--secondary)','very_happy':'var(--secondary)','focused':'var(--secondary)','energetic':'var(--secondary)','neutral':'var(--text)','calm':'var(--primary)','tired':'var(--accent-red)','sad':'var(--accent-red)','anxious':'var(--accent)','angry':'var(--accent-red)','stressed':'var(--orange)','excited':'var(--yellow)','motivated':'var(--secondary)','confused':'var(--primary)','relaxed':'var(--primary)','frustrated':'var(--accent-red)','hopeful':'var(--yellow)'};
var ENERGY_COLORS={'very_high':'var(--secondary)','high':'var(--secondary)','peak':'var(--secondary)','moderate':'var(--primary)','medium':'var(--primary)','low':'var(--yellow)','exhausted':'var(--accent-red)','depleted':'var(--accent-red)','balanced':'var(--primary)'};
var MOOD_INTENSITY={'happy':'MODERATE','very_high':'HIGH','focused':'PEAK','energetic':'HIGH','neutral':'LOW','calm':'LOW','tired':'HIGH','sad':'MODERATE','anxious':'MODERATE','angry':'HIGH','stressed':'MODERATE','excited':'PEAK','motivated':'HIGH','confused':'LOW','relaxed':'LOW','frustrated':'MODERATE','hopeful':'MODERATE'};
var ENERGY_INTENSITY={'very_high':'PEAK','high':'HIGH','peak':'PEAK','moderate':'MODERATE','medium':'MODERATE','low':'HIGH','exhausted':'HIGH','depleted':'CRITICAL','balanced':'MODERATE'};

async function loadMoodEnergyTimeline(){
  var days=parseInt(document.getElementById('moodEnergyDays').value);if(isNaN(days))days=7;
  var cutoff=days===0?null:new Date();if(cutoff)cutoff.setDate(cutoff.getDate()-days);
  try{
    var cats=['mood','energy'];
    var allItems=[];
    for(var ci=0;ci<cats.length;ci++){
      var r=await wrapFetch(fetch(BASE+'/events/'+cats[ci]),'meTimeline_'+cats[ci],{});
      var j=await r.json();
      var events=j.events||[];
      events.forEach(function(e){
        if((e.mood||e.energy)&&(!cutoff||new Date(e.ts)>=cutoff)){
          var t=new Date(e.ts);
          var h=String(t.getHours()).padStart(2,'0')+':'+String(t.getMinutes()).padStart(2,'0');
          var dayKey=t.getFullYear()+'-'+String(t.getMonth()+1).padStart(2,'0')+'-'+String(t.getDate()).padStart(2,'0');
          var isMood=!!e.mood;
          var rawKey=isMood?e.mood:e.energy;
          var label=isMood?moodLabel(e.mood):energyLabel(e.energy);
          var intensity=isMood?(MOOD_INTENSITY[rawKey]||'MODERATE'):(ENERGY_INTENSITY[rawKey]||'MODERATE');
          var color=isMood?(MOOD_COLORS[rawKey]||'var(--text)'):(ENERGY_COLORS[rawKey]||'var(--text)');
          var emoji=isMood?moodLabel(e.mood):energyLabel(e.energy);
          var subType=isMood?'Mood':'Energy';
          allItems.push({ts:e.ts,day:dayKey,time:h,label:label,intensity:intensity,color:color,emoji:emoji,subType:subType,note:e.note||''});
        }
      });
    }
    allItems.sort(function(a,b){return b.ts.localeCompare(a.ts)});
    var list=document.getElementById('moodEnergyList');
    if(allItems.length===0){list.innerHTML='<div style="color:var(--muted);font-size:13px;text-align:center;padding:20px;border-left:none">No mood or energy entries yet</div>';list.style.borderLeft='none';return}
    list.style.borderLeft='1px solid var(--border)';
    var grouped={};var order=[];
    allItems.forEach(function(item){
      if(!grouped[item.day]){grouped[item.day]=[];order.push(item.day)}
      grouped[item.day].push(item);
    });
    var months=['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
    var html='';
    order.forEach(function(day,idx){
      var d=new Date(day+'T00:00:00');
      var months2=['JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC'];
      var dayLabel=months2[d.getMonth()]+' '+d.getDate()+', '+d.getFullYear();
      var isToday=day===new Date().getFullYear()+'-'+String(new Date().getMonth()+1).padStart(2,'0')+'-'+String(new Date().getDate()).padStart(2,'0');
      var dayTag=isToday?' (TODAY)':'';
      html+='<div style="position:relative;padding:16px 0 8px 20px">';
      html+='<div style="position:absolute;left:-5px;top:20px;width:9px;height:9px;background:var(--border);transform:rotate(45deg)"></div>';
      html+='<span style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.15em">'+dayLabel+dayTag+'</span>';
      html+='<div style="position:absolute;left:-1px;top:34px;width:calc(100% + 2px);height:1px;background:var(--border)"></div>';
      html+='</div>';
      grouped[day].forEach(function(item){
        html+='<div style="position:relative;display:flex;gap:16px;padding:16px 0 16px 20px;border-bottom:1px solid var(--border);transition:background 0.15s">';
        html+='<div style="flex-shrink:0;width:56px;display:flex;flex-direction:column;align-items:center;gap:4px">';
        html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive)">'+item.time+'</span>';
        html+='<span style="font-size:20px">'+item.emoji+'</span>';
        html+='</div>';
        html+='<div style="flex:1;min-width:0">';
        html+='<div style="display:flex;align-items:center;gap:8px;margin-bottom:4px">';
        html+='<span style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:12px;font-weight:700;color:'+item.color+';text-transform:uppercase;letter-spacing:0.1em">'+item.label+'</span>';
        html+='<span style="width:3px;height:3px;border-radius:50%;background:var(--border)"></span>';
        html+='<span style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive);text-transform:uppercase">INTENSITY: '+item.intensity+'</span>';
        html+='</div>';
        if(item.note)html+='<p style="font-size:13px;color:var(--text);opacity:0.8;line-height:1.6;font-style:italic">'+esc(item.note)+'</p>';
        html+='</div></div>';
      });
    });
    list.innerHTML=html;
  }catch(e){document.getElementById('moodEnergyList').innerHTML='<div style="color:var(--red);font-size:13px;text-align:center;padding:20px">Failed to load</div>'}
}
