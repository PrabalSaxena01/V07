// ─── Sleep/Up Timeline ───

function openSleepUpPage(){
  document.getElementById('mainFab').style.display='none';_pushOv();
  var el=document.getElementById('sleepUpPage');el.scrollTop=0;el.classList.add('show');
  loadSleepUpTimeline()
}
function closeSleepUpPage(){document.getElementById('mainFab').style.display='flex';document.getElementById('sleepUpPage').classList.remove('show')}

async function loadSleepUpTimeline(){
  try{
    var r=await wrapFetch(fetch(BASE+'/events/metrics'),'sleepUpTimeline',{});
    var j=await r.json();
    var events=(j.events||[]).filter(function(e){return e.type==='checkin'||e.type==='checkout'});
    var list=document.getElementById('sleepUpList');
    if(events.length===0){list.innerHTML='<div style="color:var(--muted);font-size:13px;text-align:center;padding:20px">No sleep/check-in entries yet</div>';return}
    var sorted=events.sort(function(a,b){return(a.ts||'').localeCompare(b.ts||'')});
    var pairs=[];var openCi=null;
    sorted.forEach(function(e){
      if(e.type==='checkin'){openCi=e}
      else if(e.type==='checkout'&&openCi){
        pairs.push({checkin:openCi,checkout:e});
        openCi=null
      }
    });
    if(openCi)pairs.push({checkin:openCi,checkout:null});
    var byDate={};var dateOrder=[];
    pairs.forEach(function(p){
      var ciDate=p.checkin.ts?.slice(0,10)||p.checkin.date||'';
      if(!ciDate)return;
      if(!byDate[ciDate]){byDate[ciDate]=[];dateOrder.push(ciDate)}
      byDate[ciDate].push(p);
    });
    dateOrder.sort().reverse();
    var months=['JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC'];
    var html='';
    dateOrder.forEach(function(day){
      var d=new Date(day+'T00:00:00');
      var dayLabel=months[d.getMonth()]+' '+d.getDate()+', '+d.getFullYear();
      var dayPairs=byDate[day];
      var totalSleep=0;
      dayPairs.forEach(function(p){
        if(p.checkout&&p.checkin.ts&&p.checkout.ts){
          var ciMs=new Date(p.checkin.ts).getTime();var coMs=new Date(p.checkout.ts).getTime();
          totalSleep+=Math.floor((coMs-ciMs)/60000);
        }
      });
      var sleepH=Math.floor(totalSleep/60);var sleepM=totalSleep%60;
      var sleepStr=totalSleep>0?(sleepH>0?sleepH+'h ':'')+sleepM+'m':'';
      html+='<div style="position:relative;padding:16px 0 8px 20px;border-left:1px solid var(--border);margin-left:12px">';
      html+='<div style="position:absolute;left:-5px;top:20px;width:9px;height:9px;background:var(--border);transform:rotate(45deg)"></div>';
      html+='<div style="display:flex;justify-content:space-between;align-items:center">';
      html+='<span style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.15em">'+dayLabel+'</span>';
      if(sleepStr)html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:11px;color:var(--primary)">'+sleepStr+'</span>';
      html+='</div></div>';
      dayPairs.forEach(function(p){
        var ciTime=p.checkin.time||p.checkin.ts?.slice(11,16)||'';
        var coTime=p.checkout?(p.checkout.time||p.checkout.ts?.slice(11,16)||''):'';
        var durStr='';
        var durColor='var(--text-inactive)';
        if(p.checkout&&p.checkin.ts&&p.checkout.ts){
          var ciMs=new Date(p.checkin.ts).getTime();var coMs=new Date(p.checkout.ts).getTime();
          var diffMin=Math.floor((coMs-ciMs)/60000);
          var h=Math.floor(diffMin/60);var m=diffMin%60;
          durStr=(h>0?h+'h ':'')+m+'m';
          durColor=diffMin>=420?'var(--secondary)':diffMin>=360?'var(--primary)':'var(--yellow)';
        }
        html+='<div style="margin-left:12px;padding:12px 0 12px 20px;border-bottom:1px solid var(--border)">';
        html+='<div style="display:flex;align-items:center;gap:12px">';
        html+='<div style="flex-shrink:0;width:48px;text-align:center">';
        html+='<div style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:16px;color:var(--text)">'+ciTime+'</div>';
        html+='<div style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:9px;color:var(--green);text-transform:uppercase;letter-spacing:0.1em">CHECK-IN</div>';
        html+='</div>';
        html+='<div style="flex-shrink:0;width:24px;height:1px;background:var(--border);position:relative">';
        html+='<div style="position:absolute;left:50%;top:50%;transform:translate(-50%,-50%);width:4px;height:4px;border-radius:50%;background:var(--border)"></div>';
        html+='</div>';
        html+='<div style="flex-shrink:0;width:48px;text-align:center">';
        html+='<div style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:16px;color:'+(coTime?'var(--text)':'var(--green)')+'">'+(coTime||'Active')+'</div>';
        html+='<div style="font-family:\'Space Mono\',\'Mono\',monospace;font-size:9px;color:var(--accent-red);text-transform:uppercase;letter-spacing:0.1em">CHECK-OUT</div>';
        html+='</div>';
        if(durStr){
          html+='<div style="flex:1;text-align:right">';
          html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:14px;color:'+durColor+'">'+durStr+'</span>';
          html+='</div>';
        }
        html+='</div></div>';
      });
    });
    list.innerHTML=html;
  }catch(e){document.getElementById('sleepUpList').innerHTML='<div style="color:var(--red);font-size:13px;text-align:center;padding:20px">Failed to load</div>'}
}
