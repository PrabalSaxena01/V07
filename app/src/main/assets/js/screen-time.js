// ─── Screen Time / App Usage ───

async function loadScreenTime(force,retry){
  try{
    var url=BASE+'/app-usage/today?t='+Date.now()+(force?'&force=true':'');
    var r=await fetch(url);
    var j=await r.json();
    var statEl=document.getElementById('statScreenHeader');
    if(!j.exists||!j.summary){
      if(!retry){
        setTimeout(function(){loadScreenTime(true,true)},3000);
      }
      statEl.textContent='--';
      return;
    }
    var s=j.summary;
    statEl.textContent=s.total_screen_time_formatted||'--';
  }catch(e){}
}

function refreshScreenTime(){
  var el=document.getElementById('statScreenHeader');
  el.textContent='...';
  loadScreenTime(true);
}

var _screenDate=localDate();
async function openAppUsagePage(){
  document.getElementById('mainFab').style.display='none';_pushOv();
  _screenDate=localDate();
  document.getElementById('appUsageDate').value=_screenDate;
  var el=document.getElementById('appUsagePage');el.scrollTop=0;el.classList.add('show');
  loadAppUsageForDate();
  loadScreenTime(true);
}

function screenDateNav(dir){
  var d=new Date(_screenDate+'T00:00:00');
  d.setDate(d.getDate()+dir);
  _screenDate=d.getFullYear()+'-'+String(d.getMonth()+1).padStart(2,'0')+'-'+String(d.getDate()).padStart(2,'0');
  document.getElementById('appUsageDate').value=_screenDate;
  loadAppUsageForDate();
}

async function loadAppUsageForDate(){
  var dateVal=document.getElementById('appUsageDate').value;
  if(!dateVal)return;
  var container=document.getElementById('appUsageContent');
  if(typeof renderSkeleton==='function') container.innerHTML=renderSkeleton('grid',2)+renderSkeleton('chart')+renderSkeleton('cards',2);
  else container.innerHTML='<div style="color:var(--text-inactive);font-size:13px;text-align:center;padding:20px">Loading...</div>';
  try{
    var r=await fetch(BASE+'/app-usage?date='+dateVal);
    var j=await r.json();
    if(!j.exists||!j.summary){
      container.innerHTML='<div style="color:var(--text-inactive);text-align:center;padding:24px;font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:13px">No usage recorded for '+dateVal+'</div>';
      return;
    }
    var s=j.summary;
    var usage=Array.isArray(j.usage)?j.usage:[];
    var hourly=Array.isArray(j.hourly_usage)?j.hourly_usage:[];
    var timeline=Array.isArray(j.timeline)?j.timeline:[];
    var sessions=Array.isArray(j.sessions)?j.sessions:[];
    var usageList=usage.slice().sort(function(a,b){return(b.total_duration_sec||0)-(a.total_duration_sec||0)});
    var top3=usageList.slice(0,3).map(function(u){return u.package||u.app});
    var appColors={};
    var rankColors=['var(--primary)','var(--accent-purple)','#FF9F45'];
    var OTHER_COLOR='rgba(167,200,255,0.18)';
    for(var ri=0;ri<top3.length;ri++){appColors[top3[ri]]=rankColors[ri];}
    var hourApps=[];
    var hourTotal=[];
    var hourTotalFromSessions=0;
    for(var hi=0;hi<24;hi++){hourApps.push({});hourTotal.push(0);}
    var dayStart=new Date(dateVal+'T00:00:00').getTime();
    sessions.forEach(function(se){
      var appKey=se.package||se.app;
      if(!appKey)return;
      var rawSt=se.start_ms||0;
      var rawEn=se.end_ms||(rawSt+(se.duration_sec?se.duration_sec*1000:0));
      var st=rawSt>1000000000000?(rawSt-dayStart):rawSt;
      var en=rawEn>1000000000000?(rawEn-dayStart):rawEn;
      if(en<=st||en<=0||st>=86400000)return;
      st=Math.max(st,0);
      en=Math.min(en,86400000);
      var sh=Math.max(Math.floor(st/3600000),0);
      var eh=Math.min(Math.floor((en-1)/3600000),23);
      for(var h=sh;h<=eh;h++){
        var hs=h*3600000;
        var ov=Math.min(en,hs+3600000)-Math.max(st,hs);
        if(ov<=0)continue;
        var mins=ov/60000;
        hourApps[h][appKey]=(hourApps[h][appKey]||0)+mins;
        hourTotal[h]+=mins;
        hourTotalFromSessions+=mins;
      }
    });
    if(hourTotalFromSessions<=0&&hourly.length>0){
      hourly.forEach(function(he){
        var hh=he.hour;
        if(hh>=0&&hh<24){
          var m=(he.duration_sec||(he.duration_min?he.duration_min*60:0))/60;
          hourTotal[hh]+=m;
        }
      });
    }
    var appDisplay=function(ak){
      for(var ui=0;ui<usage.length;ui++){if((usage[ui].package||usage[ui].app)===ak)return usage[ui].app||ak;}
      return ak;
    };
    var badge=document.getElementById('screenTimeBadge');
    if(badge)badge.textContent=s.total_screen_time_formatted||'';

    var html='';

    // ── Section 1: Summary Grid (2x2) ──
    html+='<div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-bottom:16px">';
    html+='<div style="border:1px solid var(--border);padding:10px;background:var(--surface);display:flex;flex-direction:column;gap:2px">';
    html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.15em">TOTAL_LOAD</span>';
    html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:18px;font-weight:600;color:var(--text)">'+s.total_screen_time_formatted+'</span>';
    html+='</div>';
    html+='<div style="border:1px solid var(--border);padding:10px;background:var(--surface);display:flex;flex-direction:column;gap:2px">';
    html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.15em">ACTIVE_APPS</span>';
    html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:18px;font-weight:600;color:var(--text)">'+s.unique_apps_used+'</span>';
    html+='</div>';
    html+='<div style="border:1px solid var(--border);padding:10px;background:var(--surface);display:flex;flex-direction:column;gap:2px">';
    html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.15em">SESSIONS</span>';
    html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:18px;font-weight:600;color:var(--text)">'+s.session_count+'</span>';
    html+='</div>';
    html+='<div style="border:1px solid var(--border);padding:10px;background:var(--surface);display:flex;flex-direction:column;gap:2px">';
    html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.15em">UNLOCKS</span>';
    html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:18px;font-weight:600;color:var(--primary)">'+(s.unlock_count||s.session_count||'--')+'</span>';
    html+='</div>';
    html+='</div>';

    // ── Top App Peak Banner ──
    if(s.top_app){
      var topPct=s.top_app.percentage||Math.round(s.top_app.duration_sec/(s.total_screen_time_sec||1)*100);
      var topMin=Math.round((s.top_app.duration_sec||0)/60);
      html+='<div style="border:1px solid rgba(255,159,69,0.3);background:rgba(255,159,69,0.05);padding:10px 12px;margin-bottom:16px;display:flex;align-items:center;justify-content:space-between">';
      html+='<div style="display:flex;align-items:center;gap:8px">';
      html+='<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#FF9F45" stroke-width="2"><path d="M12 9v4m0 4h.01M10.29 3.86l-8.1 14A2 2 0 004 21h16a2 2 0 001.81-3.14l-8.1-14a2 2 0 00-3.42 0z"/></svg>';
      html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:10px;color:#FF9F45;text-transform:uppercase;font-weight:700;letter-spacing:0.1em">TOP_APP: '+(s.top_app.app||'').toUpperCase()+' ('+topPct+'%)</span>';
      html+='</div>';
      html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:12px;color:#FF9F45">'+topMin+'M</span>';
      html+='</div>';
    }

    // ── Section 2: Chrono Distribution (Segmented Bar Chart) ──
    html+='<div style="margin-bottom:20px">';
    html+='<div style="display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:10px;padding:0 4px">';
    html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em;font-weight:700">HOURLY USAGE DISTRIBUTION</span>';
    html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive);letter-spacing:0.05em">UNIT: 1H / 60M · ⇄ SCROLL</span>';
    html+='</div>';
    html+='<div style="border:1px solid var(--border);background:var(--surface);padding:14px 12px;overflow-x:auto;-webkit-overflow-scrolling:touch">';
    html+='<div style="display:flex;gap:8px;min-width:920px;position:relative">';
    // Y-axis labels
    html+='<div style="display:flex;flex-direction:column;justify-content:space-between;font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:8px;color:var(--text-inactive);height:140px;border-right:1px solid var(--border);padding-right:6px;flex-shrink:0;user-select:none">';
    for(var yl=60;yl>=0;yl-=10){html+='<span>'+yl+'M</span>';}
    html+='</div>';
    // Main plot + X-axis area
    html+='<div style="flex:1;position:relative;display:flex;flex-direction:column">';
    // Dashed grid lines
    html+='<div style="position:absolute;top:0;left:0;right:0;height:140px;display:flex;flex-direction:column;justify-content:space-between;pointer-events:none">';
    for(var gl=0;gl<7;gl++){html+='<div style="border-top:1px dashed var(--border);width:100%"></div>';}
    html+='</div>';
    // Bars row
    html+='<div style="display:flex;align-items:flex-end;height:140px;padding:0 4px;position:relative;z-index:1;border-bottom:1px solid var(--border)">';
    var Y_MAX=60;
    var peakMin=0;
    for(var hi=0;hi<24;hi++){if(hourTotal[hi]>peakMin)peakMin=hourTotal[hi];}
    var hasSessions=sessions.length>0&&hourTotalFromSessions>0;
    var durLabelFn=function(m){return m>=60?Math.floor(m/60)+'h'+(m%60>0?(m%60)+'m':''):m+'m';};
    var rankIdx=function(ak){return appColors[ak]?top3.indexOf(ak):99;};
    for(var hi=0;hi<24;hi++){
      var durMin=Math.round(hourTotal[hi]);
      var barPct=Math.min((durMin/Y_MAX)*100,100);
      var isPeak=(durMin===Math.round(peakMin)&&durMin>0);
      var topApp='';
      var segDesc='';
      if(durMin>0&&hasSessions){
        var segKeys=Object.keys(hourApps[hi]).sort(function(a,b){return hourApps[hi][b]-hourApps[hi][a]});
        topApp=appDisplay(segKeys[0]);
        segDesc=segKeys.slice(0,3).map(function(k){return appDisplay(k)+': '+Math.round(hourApps[hi][k])+'m'}).join(', ');
      }
      var hourStr=String(hi).padStart(2,'0')+':00';
      var tooltipTitle=hourStr+' ('+durMin+'m)'+(segDesc?' — '+segDesc:'');

      html+='<div class="chrono-bar-col" title="'+esc(tooltipTitle)+'" style="flex:1;min-width:34px;display:flex;flex-direction:column;align-items:center;justify-content:flex-end;height:100%;padding:0 2px;box-sizing:border-box">';
      if(durMin>0){
        html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:8px;font-weight:600;color:'+(isPeak?'var(--primary)':'var(--text-inactive)')+';margin-bottom:4px;white-space:nowrap;line-height:1">'+durLabelFn(durMin)+'</span>';
      }
      html+='<div style="width:16px;height:'+barPct+'%;background:'+(hasSessions?'transparent':'rgba(167,200,255,0.22)')+';border:'+(isPeak?'1px solid var(--primary)':'1px solid rgba(167,200,255,0.35)')+';border-radius:3px 3px 0 0;min-height:'+(durMin>0?'3px':'0')+';overflow:hidden;display:flex;flex-direction:column-reverse;transition:all 0.15s">';
      if(hasSessions&&durMin>0){
        var segApps=Object.keys(hourApps[hi]).sort(function(a,b){return hourApps[hi][b]-hourApps[hi][a]}).sort(function(a,b){return rankIdx(a)-rankIdx(b)});
        segApps.forEach(function(ak){
          var am=Math.round(hourApps[hi][ak]);
          if(am<=0)return;
          html+='<div title="'+esc(appDisplay(ak))+' '+am+'m" style="height:'+(am/durMin*100)+'%;min-height:2px;background:'+(appColors[ak]||OTHER_COLOR)+'"></div>';
        });
      }
      html+='</div>';
      html+='</div>';
    }
    html+='</div>';
    // X-axis hours row (cleanly separated from bars)
    html+='<div style="display:flex;padding:6px 4px 0;align-items:center">';
    for(var hi=0;hi<24;hi++){
      html+='<span style="flex:1;min-width:34px;text-align:center;font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:8px;color:var(--text-inactive);user-select:none">'+String(hi).padStart(2,'0')+'</span>';
    }
    html+='</div>';
    html+='</div></div>';
    if(hasSessions&&top3.length>0){
      html+='<div style="display:flex;flex-wrap:wrap;gap:14px;align-items:center;margin-top:12px;padding:6px 4px;border-top:1px solid var(--border)">';
      top3.forEach(function(ak,ti){
        html+='<span style="display:inline-flex;align-items:center;gap:6px;font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:8px;color:var(--text-inactive);text-transform:uppercase"><span style="width:8px;height:8px;border-radius:2px;background:'+rankColors[ti]+';display:inline-block"></span>'+esc(appDisplay(ak))+'</span>';
      });
      html+='<span style="display:inline-flex;align-items:center;gap:6px;font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:8px;color:var(--text-inactive);text-transform:uppercase"><span style="width:8px;height:8px;border-radius:2px;background:'+OTHER_COLOR+';display:inline-block"></span>OTHER</span>';
      html+='</div>';
    }
    html+='</div></div>';

    // ── Section 3: Category Distribution ──
    var cats={};
    usage.forEach(function(u){
      var cat=u.category||u.app_category||'other';
      if(!cats[cat])cats[cat]={time:0,apps:[]};
      cats[cat].time+=u.total_duration_sec||0;
      cats[cat].apps.push(u.app);
    });
    var catKeys=Object.keys(cats).sort(function(a,b){return cats[b].time-cats[a].time});
    if(catKeys.length>0){
      var catColors=['var(--primary)','var(--secondary)','var(--accent-purple)','#FF9F45','var(--text-inactive)'];
      html+='<div style="margin-bottom:16px">';
      html+='<div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;padding:0 4px">';
      html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em;font-weight:700">CATEGORY BREAKDOWN</span>';
      html+='</div>';
      html+='<div style="border:1px solid var(--border);background:var(--surface);padding:12px">';
      var maxCatTime=Math.max.apply(null,catKeys.map(function(k){return cats[k].time}));
      catKeys.forEach(function(cat,idx){
        var catTime=cats[cat];
        var durMin=Math.round(catTime.time/60);
        var durLabel=durMin>=60?Math.floor(durMin/60)+'H '+durMin%60+'M':durMin+'M';
        var barPct=maxCatTime>0?Math.round((catTime.time/maxCatTime)*100):0;
        var color=catColors[idx%catColors.length];
        html+='<div style="margin-bottom:'+(idx<catKeys.length-1?'12px':'0')+'">';
        html+='<div style="display:flex;justify-content:space-between;font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:10px;text-transform:uppercase;margin-bottom:4px">';
        html+='<span style="color:var(--text)">'+esc(cat)+'</span>';
        html+='<span style="color:'+color+';font-weight:600">'+durLabel+'</span>';
        html+='</div>';
        html+='<div style="width:100%;height:2px;background:var(--border)">';
        html+='<div style="height:100%;width:'+barPct+'%;background:'+color+'"></div>';
        html+='</div></div>';
      });
      html+='</div></div>';
    }

    // ── Section 4: App Load Metrics ──
    var totalSec=s.total_screen_time_sec||1;
    var sortedUsage=usage.slice().sort(function(a,b){return b.total_duration_sec-a.total_duration_sec});
    if(sortedUsage.length>0){
      var SHOW_COUNT=5;
      html+='<div style="margin-bottom:16px">';
      html+='<div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;padding:0 4px">';
      html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em;font-weight:700">APPLICATION USAGE BREAKDOWN</span>';
      html+='</div>';
      html+='<div style="border:1px solid var(--border);background:var(--surface);overflow:hidden">';
      var visibleApps=sortedUsage.slice(0,SHOW_COUNT);
      visibleApps.forEach(function(u,idx){
        var durMin=Math.round(u.total_duration_sec/60);
        var durLabel=durMin>=60?Math.floor(durMin/60)+'H '+durMin%60+'M':durMin+'M';
        var pct=Math.round((u.total_duration_sec/totalSec)*100);
        var color=pct>30?'var(--primary)':pct>15?'var(--secondary)':'var(--text)';
        html+='<div style="padding:10px 12px;display:flex;justify-content:space-between;align-items:center;border-bottom:1px solid var(--border)'+(idx>=visibleApps.length-1?';border-bottom:none':'')+'">';
        var iconSrc = '/api/v3/apps/icon?package=' + encodeURIComponent(u.package || u.app); html+='<div style="display:flex;align-items:center;gap:8px;overflow:hidden"><img src="'+iconSrc+'" style="width:20px;height:20px;border-radius:4px;object-fit:contain" onerror="this.style.display=\'none\'" /><span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:12px;text-transform:uppercase;color:var(--text)">'+esc(u.app)+'</span></div>';
        html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:12px;color:'+color+'">'+durLabel+'</span>';
        html+='</div>';
      });
      if(sortedUsage.length>SHOW_COUNT){
        html+='<div id="appUsageMore" style="display:none">';
        var moreApps=sortedUsage.slice(SHOW_COUNT);
        moreApps.forEach(function(u,idx){
          var durMin=Math.round(u.total_duration_sec/60);
          var durLabel=durMin>=60?Math.floor(durMin/60)+'H '+durMin%60+'M':durMin+'M';
          var pct=Math.round((u.total_duration_sec/totalSec)*100);
          var color=pct>30?'var(--primary)':pct>15?'var(--secondary)':'var(--text)';
          html+='<div style="padding:10px 12px;display:flex;justify-content:space-between;align-items:center;border-bottom:1px solid var(--border)'+(idx>=moreApps.length-1?';border-bottom:none':'')+'">';
          var iconSrc = '/api/v3/apps/icon?package=' + encodeURIComponent(u.package || u.app); html+='<div style="display:flex;align-items:center;gap:8px;overflow:hidden"><img src="'+iconSrc+'" style="width:20px;height:20px;border-radius:4px;object-fit:contain" onerror="this.style.display=\'none\'" /><span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:12px;text-transform:uppercase;color:var(--text)">'+esc(u.app)+'</span></div>';
          html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:12px;color:'+color+'">'+durLabel+'</span>';
          html+='</div>';
        });
        html+='</div>';
        html+='<div onclick="var el=document.getElementById(\'appUsageMore\');el.style.display=el.style.display===\'none\'?\'block\':\'none\';this.textContent=el.style.display===\'none\'?\'[+] SHOW ALL APPS\':\'[-] HIDE APPS\'" style="padding:10px;text-align:center;font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em;cursor:pointer;transition:color 0.15s" onmouseover="this.style.color=\'var(--text)\'" onmouseout="this.style.color=\'var(--text-inactive)\'">[+] SHOW ALL APPS</div>';
      }
      html+='</div></div>';
    }

    // ── Section 5: Session Log ──
    if(timeline.length>0){
      var sortedAsc=timeline.slice().sort(function(a,b){return(a.timestamp_ms||0)-(b.timestamp_ms||0)});
      var sessions=[];
      var open=null;
      sortedAsc.forEach(function(ev){
        if(ev.event==='START'){
          open={app:ev.app,time:ev.time,startTime:ev.time,startMs:ev.timestamp_ms||0};
        }else if(ev.event==='END'&&open&&ev.app===open.app){
          open.endTime=ev.time;open.endMs=ev.timestamp_ms||0;
          open.durSec=Math.max(Math.round((open.endMs-open.startMs)/1000),0);
          sessions.push(open);
          open=null;
        }
      });
      sessions.sort(function(a,b){return(b.startMs||0)-(a.startMs||0)});
      if(sessions.length>0){
        var maxSessionSec=Math.max.apply(null,sessions.map(function(s){return s.durSec||0}));
        html+='<div style="margin-bottom:24px">';
        html+='<div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;padding:0 4px">';
        html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:10px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em;font-weight:700">ACTIVE SESSIONS</span>';
        html+='<span style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive)">'+sessions.length+' SESSIONS</span>';
        html+='</div>';
        html+='<div style="border:1px solid var(--border);background:var(--surface);padding:12px">';
        var sessionColors=['var(--primary)','var(--secondary)','var(--accent-purple)','#FF9F45','var(--yellow)'];
        sessions.slice(0,30).forEach(function(s,idx){
          var durMin=Math.round(s.durSec/60);
          var durLabel=durMin>=60?Math.floor(durMin/60)+'H '+durMin%60+'M':durMin+'M';
          var barPct=maxSessionSec>0?Math.min(Math.round((s.durSec/maxSessionSec)*100),100):0;
          var color=sessionColors[idx%sessionColors.length];
          var isLong=s.durSec>1800;
          html+='<div style="margin-bottom:'+(idx<Math.min(sessions.length,30)-1?'14px':'0')+'">';
          html+='<div style="display:flex;justify-content:space-between;align-items:center;font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:10px;text-transform:uppercase;margin-bottom:4px">';
          html+='<div style="display:flex;gap:10px;align-items:center">';
          html+='<span style="color:var(--text-inactive)">['+esc(s.time||'')+']</span>';
          html+='<span style="color:var(--text);font-weight:600">'+esc(s.app||'')+'</span>';
          html+='</div>';
          html+='<span style="font-family:\'Lora\',serif;font-style:italic;font-size:12px;color:'+(isLong?'var(--primary)':'var(--text)')+'">'+durLabel+'</span>';
          html+='</div>';
          html+='<div style="width:100%;height:2px;background:var(--border)">';
          html+='<div style="height:100%;width:'+barPct+'%;background:'+color+'"></div>';
          html+='</div></div>';
        });
        if(sessions.length>30){
          html+='<div style="padding-top:10px;text-align:center;font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:9px;color:var(--text-inactive)">[+] '+(sessions.length-30)+' MORE_SESSIONS</div>';
        }
        html+='</div></div>';
      }
    }

    container.innerHTML=html;
  }catch(e){
    container.innerHTML='<div style="color:var(--red);text-align:center;padding:20px;font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:13px">Failed to load<br><span style="font-size:11px;color:var(--text-inactive)">'+esc(e.message||e)+'</span></div>';
  }
}
function closeAppUsagePage(){document.getElementById('mainFab').style.display='flex';document.getElementById('appUsagePage').classList.remove('show')}
