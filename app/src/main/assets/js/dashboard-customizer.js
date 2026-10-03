// ─── Dashboard Customizer & Dynamic Grid Engine ──────────────────────────────

var _dashboardWidgets = [];
var _dashboardEditMode = false;
var _dashboardCachedData = {};
var _widgetDragEl = null;
var _widgetDragActive = false;
var _widgetDragStartY = 0;
var _widgetJustDragged = false;
var _widgetDragPointerId = null;

var DEFAULT_DASHBOARD_LAYOUT = [
  { id: 'habits', size: '4*2' },
  { id: 'time_tracker', size: '4*2' },
  { id: 'tasks', size: '4*2' },
  { id: 'feed', size: '4*2' },
  { id: 'daily_read', size: '4*2' },
  { id: 'plugins', size: '4*2' }
];

function loadDashboardLayout(){
  try {
    var raw = localStorage.getItem('v07_dashboard_widgets');
    if(raw){
      _dashboardWidgets = JSON.parse(raw);
    }
  } catch(e){}
  if(!_dashboardWidgets || !_dashboardWidgets.length){
    _dashboardWidgets = JSON.parse(JSON.stringify(DEFAULT_DASHBOARD_LAYOUT));
  }
  renderDashboardGrid();
}

function saveDashboardLayout(){
  try {
    localStorage.setItem('v07_dashboard_widgets', JSON.stringify(_dashboardWidgets));
  } catch(e){}
}

function toggleDashboardEditMode(){
  _dashboardEditMode = !_dashboardEditMode;
  var btn = document.getElementById('editDashboardBtn');
  if(btn){
    btn.textContent = _dashboardEditMode ? 'DONE' : 'EDIT';
    btn.style.color = _dashboardEditMode ? 'var(--green)' : 'var(--text-inactive)';
  }
  var addBtn = document.getElementById('addWidgetsBtn');
  if(addBtn){
    addBtn.style.display = _dashboardEditMode ? 'flex' : 'none';
  }
  renderDashboardGrid();
}

function cycleWidgetSize(widgetId, event){
  if(event) { event.stopPropagation(); event.preventDefault(); }
  var item = _dashboardWidgets.find(function(w){ return w.id === widgetId; });
  if(!item) return;

  var spec = WidgetRegistry.get(widgetId);
  var sizes = (spec && spec.supportedSizes) || ['1*1', '2*1', '4*2'];
  var currentIdx = sizes.indexOf(item.size);
  var nextIdx = (currentIdx + 1) % sizes.length;
  item.size = sizes[nextIdx];

  saveDashboardLayout();
  renderDashboardGrid();
}

function removeWidgetFromDashboard(widgetId, event){
  if(event) { event.stopPropagation(); event.preventDefault(); }
  _dashboardWidgets = _dashboardWidgets.filter(function(w){ return w.id !== widgetId; });
  saveDashboardLayout();
  renderDashboardGrid();
  toast('Widget removed');
}

function addWidgetToDashboard(widgetId){
  var spec = WidgetRegistry.get(widgetId);
  if(!spec) return;

  var exists = _dashboardWidgets.find(function(w){ return w.id === widgetId; });
  if(exists){
    toast('Widget already on dashboard');
    return;
  }

  _dashboardWidgets.push({
    id: widgetId,
    size: spec.defaultSize || '4*2'
  });
  saveDashboardLayout();
  closeWidgetGalleryModal();
  renderDashboardGrid();
  toast(spec.title + ' added');
}

function updateDashboardData(data){
  _dashboardCachedData = Object.assign(_dashboardCachedData, data || {});
  renderDashboardGrid();
}

function renderDashboardGrid(){
  var grid = document.getElementById('dashboardWidgetGrid');
  if(!grid) return;

  if(!_dashboardWidgets || !_dashboardWidgets.length){
    grid.innerHTML = '<div style="grid-column:span 4;padding:24px;text-align:center;color:var(--text-inactive)">No widgets. Tap "+ ADD WIDGET" below to customize.</div>';
    return;
  }

  // Optimize: Check if DOM structure already matches so we avoid full page rerendering!
  var existingCards = Array.from(grid.querySelectorAll('.widget-card'));
  var matches = existingCards.length === _dashboardWidgets.length && _dashboardWidgets.every(function(item, idx){
    var card = existingCards[idx];
    if(!card) return false;
    var idMatch = card.getAttribute('data-widget-id') === item.id;
    var sizeClass = item.size === '1*1' ? 'size-1-1' : item.size === '2*1' ? 'size-2-1' : 'size-4-2';
    var hasSize = card.classList.contains(sizeClass);
    var hasEdit = card.classList.contains('edit-mode') === !!_dashboardEditMode;
    return idMatch && hasSize && hasEdit;
  });

  if(!matches){
    var html = '';
    _dashboardWidgets.forEach(function(item){
      var spec = WidgetRegistry.get(item.id);
      if(!spec) return;

      var sizeClass = item.size === '1*1' ? 'size-1-1' : item.size === '2*1' ? 'size-2-1' : 'size-4-2';
      var editClass = _dashboardEditMode ? ' edit-mode' : '';

      html += '<div class="widget-card ' + sizeClass + editClass + '" data-widget-id="' + item.id + '" oncontextmenu="if(!_dashboardEditMode){toggleDashboardEditMode();return false;}">';
      
      if(_dashboardEditMode){
        html += '<div class="widget-edit-bar">';
        html += '  <button class="widget-size-pill" onclick="cycleWidgetSize(\'' + item.id + '\', event)">' + item.size + '</button>';
        html += '  <div style="display:flex;align-items:center;gap:6px">';
        html += '    <span class="widget-drag-handle" onpointerdown="widgetOnPointerDown(event)" ontouchstart="widgetOnTouchStart(event)" title="Drag to reorder">⠿</span>';
        html += '    <button class="widget-remove-btn" onclick="removeWidgetFromDashboard(\'' + item.id + '\', event)">✕</button>';
        html += '  </div>';
        html += '</div>';
      }

      html += '  <div class="widget-content" id="widgetContent_' + item.id + '"></div>';
      html += '</div>';
    });

    grid.innerHTML = html;
  }

  // Render contents
  _dashboardWidgets.forEach(function(item){
    var spec = WidgetRegistry.get(item.id);
    var contentEl = document.getElementById('widgetContent_' + item.id);
    if(spec && contentEl){
      try {
        spec.render(contentEl, item.size, _dashboardCachedData);
      } catch(err){}
    }
  });
}

// ─── FLIP Smooth Reordering Animation ────────────────────────────────────────
function animateFLIP(container, movingEl, reorderFn){
  if(!container || !movingEl){ reorderFn(); return; }
  var children = Array.from(container.children);
  var firstMap = new Map();
  children.forEach(function(el){
    firstMap.set(el, el.getBoundingClientRect());
  });

  reorderFn();

  children.forEach(function(el){
    if(el === movingEl) return;
    var first = firstMap.get(el);
    if(!first) return;
    var last = el.getBoundingClientRect();
    var dx = first.left - last.left;
    var dy = first.top - last.top;
    if(dx !== 0 || dy !== 0){
      el.style.transform = 'translate(' + dx + 'px, ' + dy + 'px)';
      el.style.transition = 'none';
      requestAnimationFrame(function(){
        el.style.transition = 'transform 0.22s cubic-bezier(0.2, 0, 0, 1)';
        el.style.transform = '';
      });
    }
  });
}

// ─── Drag & Drop Reordering for Widgets ─────────────────────────────────────
function widgetReorderDOM(clientY){
  var grid = document.getElementById('dashboardWidgetGrid');
  if(!grid || !_widgetDragEl) return;

  var cards = Array.from(grid.querySelectorAll('.widget-card:not(.widget-dragging)'));
  var target = null;
  for(var i = 0; i < cards.length; i++){
    var box = cards[i].getBoundingClientRect();
    if(clientY >= box.top && clientY <= box.bottom){
      target = cards[i];
      break;
    }
  }
  if(!target || target === _widgetDragEl) return;
  var rect = target.getBoundingClientRect();
  animateFLIP(grid, _widgetDragEl, function(){
    if(clientY > rect.top + rect.height / 2){
      grid.insertBefore(_widgetDragEl, target.nextSibling);
    } else {
      grid.insertBefore(_widgetDragEl, target);
    }
  });
}

function widgetSaveCurrentOrder(){
  var grid = document.getElementById('dashboardWidgetGrid');
  if(grid){
    var newOrder = [];
    grid.querySelectorAll('.widget-card').forEach(function(el){
      var wid = el.getAttribute('data-widget-id');
      var existing = _dashboardWidgets.find(function(w){ return w.id === wid; });
      if(existing) newOrder.push(existing);
    });
    if(newOrder.length === _dashboardWidgets.length){
      _dashboardWidgets = newOrder;
      saveDashboardLayout();
    }
  }
}

function widgetOnPointerDown(e){
  try{ window.getSelection()?.removeAllRanges(); }catch(err){}
  var card = e.currentTarget.closest('.widget-card');
  if(!card) return;
  e.preventDefault();
  e.stopPropagation();
  _widgetDragEl = card;
  _widgetDragActive = true;
  _widgetDragPointerId = e.pointerId;
  _widgetJustDragged = false;
  _widgetDragStartY = e.clientY;
  card.classList.add('widget-dragging');
  document.body.style.userSelect = 'none';

  function onPointerMove(ev){
    if(!_widgetDragActive || !_widgetDragEl) return;
    if(ev.pointerId !== _widgetDragPointerId) return;
    if(ev.cancelable) ev.preventDefault();
    if(Math.abs(ev.clientY - _widgetDragStartY) > 5) _widgetJustDragged = true;
    widgetReorderDOM(ev.clientY);
  }

  function onPointerUp(ev){
    if(ev.pointerId !== _widgetDragPointerId) return;
    window.removeEventListener('pointermove', onPointerMove);
    window.removeEventListener('pointerup', onPointerUp);
    window.removeEventListener('pointercancel', onPointerUp);
    document.body.style.userSelect = '';
    _widgetDragActive = false;
    _widgetDragPointerId = null;
    if(_widgetDragEl) _widgetDragEl.classList.remove('widget-dragging');
    _widgetDragEl = null;
    widgetSaveCurrentOrder();
    setTimeout(function(){ _widgetJustDragged = false; }, 200);
  }

  window.addEventListener('pointermove', onPointerMove, {passive: false});
  window.addEventListener('pointerup', onPointerUp, {passive: false});
  window.addEventListener('pointercancel', onPointerUp, {passive: false});
}

function widgetOnTouchStart(e){
  try{ window.getSelection()?.removeAllRanges(); }catch(err){}
  var card = e.currentTarget.closest('.widget-card');
  if(!card) return;
  var touch = e.touches[0];
  if(!touch) return;
  e.preventDefault();
  e.stopPropagation();
  _widgetDragEl = card;
  _widgetDragActive = true;
  _widgetJustDragged = false;
  _widgetDragStartY = touch.clientY;
  card.classList.add('widget-dragging');
  document.body.style.userSelect = 'none';

  function onTouchMove(ev){
    if(!_widgetDragActive || !_widgetDragEl) return;
    var t0 = ev.touches[0];
    if(!t0) return;
    if(ev.cancelable) ev.preventDefault();
    if(Math.abs(t0.clientY - _widgetDragStartY) > 5) _widgetJustDragged = true;
    widgetReorderDOM(t0.clientY);
  }

  function onTouchEnd(ev){
    window.removeEventListener('touchmove', onTouchMove);
    window.removeEventListener('touchend', onTouchEnd);
    window.removeEventListener('touchcancel', onTouchEnd);
    document.body.style.userSelect = '';
    _widgetDragActive = false;
    if(_widgetDragEl) _widgetDragEl.classList.remove('widget-dragging');
    _widgetDragEl = null;
    widgetSaveCurrentOrder();
    setTimeout(function(){ _widgetJustDragged = false; }, 200);
  }

  window.addEventListener('touchmove', onTouchMove, {passive: false});
  window.addEventListener('touchend', onTouchEnd, {passive: false});
  window.addEventListener('touchcancel', onTouchEnd, {passive: false});
}

// ─── Widget Gallery Modal ──────────────────────────────────────────────────
function openWidgetGalleryModal(){
  var modal = document.getElementById('widgetGalleryModal');
  var list = document.getElementById('widgetGalleryList');
  if(!modal || !list) return;

  var all = WidgetRegistry.getAll();
  var html = '';

  all.forEach(function(spec){
    var isAdded = _dashboardWidgets.some(function(w){ return w.id === spec.id; });
    var sizesText = (spec.supportedSizes || []).join(' · ');

    html += '<div class="gallery-item">';
    html += '  <div style="display:flex;align-items:center;gap:12px;flex:1">';
    html += '    <div class="gallery-item-icon"><span class="material-symbols-outlined">' + (spec.icon || 'widgets') + '</span></div>';
    html += '    <div>';
    html += '      <div style="font-weight:600;font-size:14px;color:var(--text)">' + esc(spec.title) + '</div>';
    html += '      <div style="font-size:11px;color:var(--text-inactive);margin-top:2px">' + esc(spec.description || '') + '</div>';
    html += '      <div style="font-size:9px;color:var(--primary);margin-top:4px;font-family:\'Space Mono\',monospace">SIZES: ' + sizesText + '</div>';
    html += '    </div>';
    html += '  </div>';
    if(isAdded){
      html += '  <button class="btn btn-outline btn-sm" disabled style="opacity:0.5;font-size:10px">ADDED</button>';
    } else {
      html += '  <button class="btn btn-accent btn-sm" style="font-size:10px" onclick="addWidgetToDashboard(\'' + spec.id + '\')">+ ADD</button>';
    }
    html += '</div>';
  });

  list.innerHTML = html;
  modal.classList.add('show');
}

function closeWidgetGalleryModal(){
  var modal = document.getElementById('widgetGalleryModal');
  if(modal) modal.classList.remove('show');
}

// Initialize on page load
if(document.readyState === 'loading'){
  document.addEventListener('DOMContentLoaded', function(){
    loadDashboardLayout();
  });
} else {
  loadDashboardLayout();
}
