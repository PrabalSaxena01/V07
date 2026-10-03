// ─── Settings Page ────────────────────────────────────────────────────────────────

var _activeSettingsTab = 'general';
function switchSettingsTab(tab){
  _activeSettingsTab = tab || 'general';
  var genTab=document.getElementById('settingsTabGeneral');
  var aiTab=document.getElementById('settingsTabAI');
  var notifTab=document.getElementById('settingsTabNotif');
  var genPanel=document.getElementById('settingsPanelGeneral');
  var aiPanel=document.getElementById('settingsPanelAI');
  var notifPanel=document.getElementById('settingsPanelNotifications');
  if(genTab) genTab.classList.toggle('active', _activeSettingsTab === 'general');
  if(aiTab) aiTab.classList.toggle('active', _activeSettingsTab === 'ai');
  if(notifTab) notifTab.classList.toggle('active', _activeSettingsTab === 'notifications');
  if(genPanel) genPanel.style.display = (_activeSettingsTab === 'general' ? 'block' : 'none');
  if(aiPanel) aiPanel.style.display = (_activeSettingsTab === 'ai' ? 'block' : 'none');
  if(notifPanel) notifPanel.style.display = (_activeSettingsTab === 'notifications' ? 'block' : 'none');
  if(_activeSettingsTab === 'ai'){
    loadAISettings();
    loadPluginAIPrefs();
  } else if(_activeSettingsTab === 'notifications'){
    if(typeof loadPluginNotifPrefs === 'function') loadPluginNotifPrefs();
    loadNotifPrefs();
  }
}

function closeSettingsPage(){
  document.getElementById('mainFab').style.display='flex';
  document.getElementById('settingsPage').classList.remove('show');
}
async function openSettingsPage(){
  document.getElementById('mainFab').style.display='none';
  var el=document.getElementById('settingsPage');el.scrollTop=0;el.classList.add('show');
  switchSettingsTab(_activeSettingsTab || 'general');
  await loadNotifPrefs();
  loadThemePref();
  loadVaultPath();
  loadOverlayStatus();
  loadAppLimitsSettings();
}

// ─── Theme ────────────────────────────────────────────────────────────────
var _systemDark=window.matchMedia('(prefers-color-scheme: dark)');
function applyTheme(mode){
  if(mode==='light')document.documentElement.dataset.theme='light';
  else if(mode==='dark')document.documentElement.dataset.theme='dark';
  else delete document.documentElement.dataset.theme;
}
function loadThemePref(){
  var saved=localStorage.getItem('theme')||'system';
  document.getElementById('themeSelect').value=saved;
  applyTheme(saved);
  updateThemeBtn();
}
function setTheme(mode){
  localStorage.setItem('theme',mode);
  applyTheme(mode);
  updateThemeBtn();
  try{fetch(BASE+'/meta/theme',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({theme:mode})})}catch(e){}
}
function toggleTheme(){
  var cur=localStorage.getItem('theme')||'system';
  var next=cur==='dark'?'light':cur==='light'?'system':'dark';
  setTheme(next);
}
function getThemeIcon(){
  var saved=localStorage.getItem('theme')||'system';
  if(saved==='dark')return '🌙';
  if(saved==='light')return '☀️';
  return '🔄';
}
function updateThemeBtn(){
  var btn=document.getElementById('themeToggleBtn');
  if(btn)btn.textContent=getThemeIcon();
}
_systemDark.addEventListener('change',function(){
  var saved=localStorage.getItem('theme')||'system';
  if(saved==='system')applyTheme('system');
  updateThemeBtn();
});
// Apply saved theme immediately on load
(function(){var m=localStorage.getItem('theme');if(m)applyTheme(m);updateThemeBtn()})();

// ─── Vault Init ────────────────────────────────────────────────────────────────
async function initVault(){
  var ok=confirm('This will create empty data files in your vault. Continue?');
  if(!ok)return;
  try{
    var r=await fetch(BASE+'/vault/init',{method:'POST'});
    if(r.ok){toast('Vault initialized');setTimeout(loadDashboard,500)}
    else toast('Failed to initialize vault');
  }catch(e){toast('Error: '+e.message)}
}
async function loadVaultPath(){
  try{
    var r=await fetch(BASE+'/vault/path');
    var j=await r.json();
    var p=j.path||'not set';
    document.getElementById('vaultPathDisplay').textContent=p;
    var fp=document.getElementById('footerVaultPath');if(fp)fp.textContent='Current Vault: '+p;
  }catch(e){document.getElementById('vaultPathDisplay').textContent='error'}
}
async function loadNetworkInfo(){
  try{
    var r=await fetch(BASE+'/network/info');
    var j=await r.json();
    var url=j.url||'';
    var el=document.getElementById('networkUrl');
    if(el)el.textContent=url;
    if(url)renderQR(url);
  }catch(e){}
}
function copyNetworkUrl(){
  var url=document.getElementById('networkUrl').textContent;
  if(navigator.clipboard)navigator.clipboard.writeText(url).then(function(){toast('URL copied!')});
  else{var t=document.createElement('textarea');t.value=url;document.body.appendChild(t);t.select();document.execCommand('copy');document.body.removeChild(t);toast('URL copied!')}
}
function renderQR(text){
  var img=document.getElementById('networkQR');
  if(!img)return;
  try{
    img.src=BASE+'/qr?text='+encodeURIComponent(text);
    img.style.display='block';
  }catch(e){img.style.display='none'}
}
function openNativeVaultPicker(){
  if(window.Android&&Android.openVaultPicker)Android.openVaultPicker();
  else toast('Not available — use FAB to change vault');
}
async function loadNotifPrefs(){
  try{
    var r=await fetch(BASE+'/notify-prefs');
    var j=await r.json();
    document.getElementById('notifOverdue').checked=j.overdue_alert!==false;
    document.getElementById('notifDueToday').checked=j.due_today_alert!==false;
    document.getElementById('notifHabits').checked=j.habits_reminder!==false;
    document.getElementById('notifMood').checked=j.mood_reminder!==false;
    if(document.getElementById('notifTimePriority'))document.getElementById('notifTimePriority').checked=j.time_priority!==false;
    if(document.getElementById('notifGitaDaily'))document.getElementById('notifGitaDaily').checked=j.gita_daily===true;
    if(document.getElementById('notifGitaDailyTime'))document.getElementById('notifGitaDailyTime').value=j.gita_daily_time||'07:00';
    if(document.getElementById('notifChecklist'))document.getElementById('notifChecklist').checked=j.checklist_reminder===true;
  }catch(e){}
}
async function saveNotifPref(key,val){
  try{
    var body={};
    body[key]=val;
    await fetch(BASE+'/notify-prefs',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)});
  }catch(e){}
}
function testNotification(){
  fetch(BASE+'/notify/test',{method:'POST'}).then(function(r){return r.json()}).then(function(){toast('Test notification sent!')}).catch(function(){toast('Failed to send test')})
}
function clearAllNotifications(){
  // This would need a server endpoint
  toast('Cleared');
}
async function loadPluginNotifPrefs(){
  var container=document.getElementById('pluginNotifList');
  if(!container)return;
  try{
    var r=await fetch(BASE+'/plugins/registry');
    var j=await r.json();
    var plugins=(j&&j.plugins)||[];
    var notifPlugins=plugins.filter(function(p){
      return p.permissions&&p.permissions.indexOf('notification')!==-1;
    });
    if(!notifPlugins.length){
      container.innerHTML='<div style="font-size:11px;color:var(--text-inactive);font-family:\'Space Mono\',monospace;padding:6px 0">No plugins with notification permission installed.</div>';
      return;
    }
    var html='';
    notifPlugins.forEach(function(p){
      var feat=(p.features||[]).find(function(f){return f.id==='notifications'});
      var on=(feat?feat.enabled!==false:true)&&(p.enabled!==false);
      html+='<label class="toggle-row">'+
        '<div style="display:flex;flex-direction:column">'+
          '<span style="font-size:13px">'+p.name+'</span>'+
          '<span style="font-size:10px;color:var(--text-inactive);font-family:\'Space Mono\',monospace">'+p.id+'</span>'+
        '</div>'+
        '<label class="kb-toggle"><input type="checkbox" '+(on?'checked':'')+' onchange="savePluginNotifPref(\''+p.id+'\',this.checked)"><div class="kb-toggle-track" data-on="ON" data-off="OFF"></div></label>'+
      '</label>';
    });
    container.innerHTML=html;
  }catch(e){
    container.innerHTML='<div style="font-size:11px;color:var(--red)">Failed to load plugin notification settings.</div>';
  }
}
async function savePluginNotifPref(id,enabled){
  try{
    await fetch(BASE+'/plugins/'+id+'/feature',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({feature:'notifications',enabled:enabled})});
    toast('Notifications '+(enabled?'ON':'OFF')+' for '+id);
  }catch(e){toast('Failed to update notification setting');}
  loadPluginNotifPrefs();
}

// ─── Overlay Bubble ─────────────────────────────────────────────────────────
async function loadOverlayStatus(){
  try{
    var r=await fetch(BASE+'/overlay/status');
    var j=await r.json();
    document.getElementById('overlayToggle').checked=j.enabled===true;
    var statusEl=document.getElementById('overlayStatus');
    if(!j.can_draw_overlays){
      statusEl.innerHTML='⚠️ Overlay permission not granted. <span style="color:var(--accent);cursor:pointer" onclick="requestOverlayPermission()">Grant in Settings</span>';
    } else {
      statusEl.innerHTML=j.enabled?'🟢 Overlay is active':'Tap toggle to enable';
    }
  }catch(e){}
}
async function toggleOverlay(enabled){
  try{
    var r=await fetch(BASE+'/overlay/toggle',{method:'POST'});
    var j=await r.json();
    document.getElementById('overlayToggle').checked=j.enabled===true;
    if(!j.can_draw_overlays){
      document.getElementById('overlayStatus').innerHTML='⚠️ Overlay permission not granted. <span style="color:var(--accent);cursor:pointer" onclick="requestOverlayPermission()">Grant in Settings</span>';
    } else {
      document.getElementById('overlayStatus').innerHTML=j.enabled?'🟢 Overlay is active':'Tap toggle to enable';
    }
    if(j.enabled)toast('Overlay enabled');
    else toast('Overlay disabled');
  }catch(e){toast('Failed to toggle overlay')}
}
function requestOverlayPermission(){
  toast('Grant "Display over other apps" permission for V07');
}

// ─── App Limits ────────────────────────────────────────────────────────────────
var _appLimits={};
async function loadAppLimitsSettings(){
  try{
    var r=await fetch(BASE+'/app-limits');var j=await r.json();
    if(j.limits){_appLimits=JSON.parse(j.limits);}
    var def=_appLimits['default']||30;
    var defInput=document.getElementById('appLimitDefault');
    if(defInput)defInput.value=def;
    renderAppLimitsList();
  }catch(e){}
}
function renderAppLimitsList(){
  var el=document.getElementById('appLimitsList');if(!el)return;
  var keys=Object.keys(_appLimits).filter(function(k){return k!=='default';});
  if(!keys.length){el.innerHTML='<div style="color:var(--text-inactive);padding:4px 0">No custom limits set. All apps default to '+(_appLimits['default']||30)+' min.</div>';return;}
  var h='';
  keys.forEach(function(pkg){
    var min=_appLimits[pkg]||30;
    var name=pkg.split('.').pop();
    h+='<div style="display:flex;align-items:center;gap:8px;padding:6px 0;border-bottom:1px solid var(--border)">';
    h+='<img src="/app-icon?package='+encodeURIComponent(pkg)+'" style="width:20px;height:20px;border-radius:4px;object-fit:contain;flex-shrink:0;background:rgba(255,255,255,0.06)" onerror="this.style.display=\'none\'">';
    h+='<span style="flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;color:var(--text);font-size:12px">'+name+'</span>';
    h+='<input type="number" value="'+min+'" min="5" max="480" style="width:50px;padding:4px 6px;background:var(--surface-container-low);border:1px solid var(--border);border-radius:4px;color:var(--text);font-family:monospace;font-size:12px;text-align:center" onchange="saveAppLimit(\''+pkg+'\',this.value)">';
    h+='<span style="font-size:10px;color:var(--text-inactive)">m</span>';
    h+='<span style="cursor:pointer;color:var(--red);font-size:14px" onclick="removeAppLimit(\''+pkg+'\')">&#x2715;</span>';
    h+='</div>';
  });
  el.innerHTML=h;
}
async function saveAppLimitDefault(){
  var v=parseInt(document.getElementById('appLimitDefault').value)||30;
  _appLimits['default']=v;
  await saveAppLimits();
}
async function saveAppLimit(pkg,min){
  _appLimits[pkg]=parseInt(min)||30;
  await saveAppLimits();
}
async function removeAppLimit(pkg){
  delete _appLimits[pkg];
  await saveAppLimits();
  renderAppLimitsList();
}
async function saveAppLimits(){
  try{
    await fetch(BASE+'/app-limits',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({limits:_appLimits})});
  }catch(e){}
}

// ─── Companion ──────────────────────────────────────────────────────────────
function openCompanion(){
  // Sync events to Firestore first, then open companion in browser
  try{fetch(BASE+'/sync/trigger',{method:'POST'})}catch(e){}
  try{
    var url=window.location.origin+'/companion.html';
    window.open(url,'_blank');
    toast('Opening companion...');
  }catch(e){toast('Failed to open companion')}
}

// ─── AI Core Settings ─────────────────────────────────────────────────────────
var _geminiKeyVisible = false;

function toggleGeminiKeyVisibility(){
  var inp = document.getElementById('geminiApiKeyInput');
  var btn = document.getElementById('geminiKeyToggleBtn');
  if(!inp || !btn) return;
  _geminiKeyVisible = !_geminiKeyVisible;
  inp.type = _geminiKeyVisible ? 'text' : 'password';
  btn.textContent = _geminiKeyVisible ? 'HIDE' : 'SHOW';
}

async function loadAISettings(){
  try {
    var r = await fetch(BASE + '/ai/config');
    var j = await r.json();
    var badge = document.getElementById('aiKeyStatusBadge');
    var modelSelect = document.getElementById('geminiModelSelect');
    var customGroup = document.getElementById('customModelGroup');
    var customInput = document.getElementById('geminiCustomModelInput');
    var provSelect = document.getElementById('aiProviderSelect');
    var provBadge = document.getElementById('aiProviderBadge');
    var baseInput = document.getElementById('geminiBaseUrlInput');

    if (badge) {
      if (j.configured) {
        badge.textContent = 'CONFIGURED (' + (j.maskedKey || 'ACTIVE') + ')';
        badge.style.color = 'var(--primary)';
        badge.style.background = 'rgba(167,200,255,0.12)';
        badge.style.border = '1px solid rgba(167,200,255,0.3)';
      } else {
        badge.textContent = 'NOT CONFIGURED';
        badge.style.color = 'var(--text-inactive)';
        badge.style.background = 'rgba(255,255,255,0.06)';
        badge.style.border = 'none';
      }
    }

    if (modelSelect && j.defaultModel) {
      var targetModel = j.defaultModel;
      if (targetModel === 'gemini-2.0-flash') targetModel = 'gemini-3.6-flash';
      var found = Array.from(modelSelect.options).some(function(opt){ return opt.value === targetModel; });
      if (found) {
        modelSelect.value = targetModel;
        if (customGroup) customGroup.style.display = 'none';
      } else {
        modelSelect.value = '__custom__';
        if (customGroup) customGroup.style.display = 'block';
        if (customInput) customInput.value = targetModel;
      }
    }

    if (provSelect && j.provider) {
      provSelect.value = j.provider;
    }
    if (provBadge) {
      provBadge.textContent = (j.effectiveProvider || 'AUTO').toUpperCase();
    }

    if (baseInput) {
      baseInput.value = (j.baseUrl && j.baseUrl !== 'https://generativelanguage.googleapis.com/v1beta/models') ? j.baseUrl : '';
    }
  } catch(e) {
    console.error('Failed to load AI config:', e);
  }
}

function onModelSelectChange(val){
  var customGroup = document.getElementById('customModelGroup');
  var customInput = document.getElementById('geminiCustomModelInput');
  if (val === '__custom__') {
    if (customGroup) customGroup.style.display = 'block';
    if (customInput) customInput.focus();
  } else {
    if (customGroup) customGroup.style.display = 'none';
    saveGeminiModel(val);
  }
}

function saveCustomModel(val){
  val = (val || '').trim();
  if (val) {
    saveGeminiModel(val);
  }
}

async function saveAIProvider(provider){
  try {
    var r = await fetch(BASE + '/ai/config', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ provider: provider })
    });
    var j = await r.json();
    toast('Protocol set to ' + (provider === 'auto' ? 'Auto-detect' : provider.toUpperCase()));
    var provBadge = document.getElementById('aiProviderBadge');
    if (provBadge && j.effectiveProvider) {
      provBadge.textContent = j.effectiveProvider.toUpperCase();
    }
  } catch(e){}
}

async function saveGeminiApiKey(){
  var inp = document.getElementById('geminiApiKeyInput');
  if(!inp) return;
  var key = (inp.value || '').trim();
  if(!key){
    toast('Please paste a valid API key');
    return;
  }
  try {
    var r = await fetch(BASE + '/ai/config', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ apiKey: key })
    });
    var j = await r.json();
    if(j.ok){
      inp.value = '';
      _geminiKeyVisible = false;
      inp.type = 'password';
      var btn = document.getElementById('geminiKeyToggleBtn');
      if(btn) btn.textContent = 'SHOW';
      toast('API key saved securely');
      loadAISettings();
    } else {
      toast('Failed to save API key: ' + (j.error || 'unknown error'));
    }
  } catch(e){
    toast('Error saving API key: ' + e.message);
  }
}

async function clearGeminiApiKey(){
  if(!confirm('Clear stored API key?')) return;
  try {
    var r = await fetch(BASE + '/ai/config', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ apiKey: '' })
    });
    var j = await r.json();
    if(j.ok){
      var inp = document.getElementById('geminiApiKeyInput');
      if(inp) inp.value = '';
      toast('API key cleared');
      loadAISettings();
    }
  } catch(e){
    toast('Failed to clear key');
  }
}

async function saveGeminiModel(model){
  if(!model) return;
  try {
    var r = await fetch(BASE + '/ai/config', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ defaultModel: model })
    });
    var j = await r.json();
    toast('Model set to ' + model);
    var provBadge = document.getElementById('aiProviderBadge');
    if (provBadge && j.effectiveProvider) {
      provBadge.textContent = j.effectiveProvider.toUpperCase();
    }
  } catch(e){}
}

async function saveGeminiBaseUrl(url){
  try {
    await fetch(BASE + '/ai/config', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ baseUrl: url.trim() })
    });
    toast('Custom endpoint updated');
  } catch(e){}
}

function onApiKeyInput(val){
  val = (val || '').trim();
  var provBadge = document.getElementById('aiProviderBadge');
  var modelSelect = document.getElementById('geminiModelSelect');
  if (!val) return;
  if (val.startsWith('AIza')) {
    if (provBadge) provBadge.textContent = 'AI STUDIO';
    if (modelSelect && (modelSelect.value === 'claude-3-5-sonnet-20241022' || modelSelect.value === 'gemini-2.0-flash')) {
      modelSelect.value = 'gemini-3.6-flash';
    }
  } else if (val.startsWith('sk-ant-')) {
    if (provBadge) provBadge.textContent = 'ANTHROPIC';
    if (modelSelect && !modelSelect.value.startsWith('claude-')) {
      modelSelect.value = 'claude-3-5-sonnet-20241022';
    }
  } else if (val.startsWith('sk-or-')) {
    if (provBadge) provBadge.textContent = 'OPENROUTER';
  } else if (val.startsWith('sk-')) {
    if (provBadge) provBadge.textContent = 'OPENAI';
  }
}

async function testGeminiConnection(){
  var resultEl = document.getElementById('aiTestResult');
  var btn = document.getElementById('btnTestAI');
  var keyInp = document.getElementById('geminiApiKeyInput');
  var modelSelect = document.getElementById('geminiModelSelect');
  if(!resultEl || !btn) return;

  var currentKey = keyInp ? (keyInp.value || '').trim() : '';
  var currentModel = modelSelect ? modelSelect.value : '';
  if (currentModel === '__custom__') {
    var customInput = document.getElementById('geminiCustomModelInput');
    currentModel = customInput ? (customInput.value || '').trim() : '';
  }
  if (currentModel === 'gemini-2.0-flash') {
    currentModel = 'gemini-3.6-flash';
    if (modelSelect) modelSelect.value = 'gemini-3.6-flash';
  }

  resultEl.style.display = 'block';
  resultEl.innerHTML = '<span style="color:var(--text-inactive)">Testing connection to AI API...</span>';
  btn.disabled = true;

  try {
    var payload = {};
    if (currentKey) payload.apiKey = currentKey;
    if (currentModel) payload.model = currentModel;

    var r = await fetch(BASE + '/ai/test', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    var j = await r.json();
    if(j.ok){
      resultEl.innerHTML = '<span style="color:#67ac89;font-weight:700">🟢 Connected</span> · ' + (j.model || 'AI') + ' responding (' + (j.latencyMs || 0) + 'ms latency)';
      toast('API connected successfully!');
      if (keyInp && currentKey) {
        keyInp.value = '';
      }
      loadAISettings();
    } else {
      resultEl.innerHTML = '<span style="color:var(--red);font-weight:700">🔴 Connection failed:</span> ' + (j.error || 'Unknown error');
      toast('Connection test failed');
    }
  } catch(e){
    resultEl.innerHTML = '<span style="color:var(--red);font-weight:700">🔴 Error:</span> ' + e.message;
  } finally {
    btn.disabled = false;
  }
}

async function loadPluginAIPrefs(){
  var container = document.getElementById('pluginAIList');
  if(!container) return;
  try {
    var r = await fetch(BASE + '/plugins/registry');
    var j = await r.json();
    var plugins = (j && j.plugins) || [];
    var aiPlugins = plugins.filter(function(p){
      return p.permissions && p.permissions.indexOf('ai') !== -1;
    });

    if(!aiPlugins.length){
      container.innerHTML = '<div style="font-size:11px;color:var(--text-inactive);font-family:\'Space Mono\',monospace;padding:6px 0">No plugins currently requesting AI permission. Future plugins declaring "ai" in manifest will appear here with toggle controls.</div>';
      return;
    }

    var html = '';
    aiPlugins.forEach(function(p){
      var feat = (p.features || []).find(function(f){ return f.id === 'ai'; });
      var on = (feat ? feat.enabled !== false : true) && (p.enabled !== false);
      html += '<label class="toggle-row">' +
        '<div style="display:flex;flex-direction:column">' +
          '<span style="font-size:13px">' + p.name + '</span>' +
          '<span style="font-size:10px;color:var(--text-inactive);font-family:\'Space Mono\',monospace">' + p.id + '</span>' +
        '</div>' +
        '<label class="kb-toggle"><input type="checkbox" ' + (on ? 'checked' : '') + ' onchange="savePluginAIPref(\'' + p.id + '\',this.checked)"><div class="kb-toggle-track" data-on="ON" data-off="OFF"></div></label>' +
      '</label>';
    });
    container.innerHTML = html;
  } catch(e){
    container.innerHTML = '<div style="font-size:11px;color:var(--red)">Failed to load plugin AI settings.</div>';
  }
}

async function savePluginAIPref(id, enabled){
  try {
    await fetch(BASE + '/plugins/' + id + '/feature', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ feature: 'ai', enabled: enabled })
    });
    toast('AI access ' + (enabled ? 'GRANTED' : 'REVOKED') + ' for ' + id);
  } catch(e){
    toast('Failed to update plugin AI permission');
  }
  loadPluginAIPrefs();
}
