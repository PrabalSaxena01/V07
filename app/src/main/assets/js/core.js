// ─── Core Utilities ───
function renderSkeleton(type, count){
  count = count || 3;
  var h = '';
  if(type === 'cards'){
    for(var i=0; i<count; i++){
      h += '<div class="skeleton-card">';
      h += '  <div style="display:flex;justify-content:space-between;align-items:center">';
      h += '    <div class="skeleton-shimmer" style="width:' + (55 + (i*15)%35) + '%;height:14px"></div>';
      h += '    <div class="skeleton-shimmer" style="width:40px;height:12px;border-radius:10px"></div>';
      h += '  </div>';
      h += '  <div class="skeleton-shimmer" style="width:' + (80 + (i*10)%20) + '%;height:10px;opacity:0.7"></div>';
      h += '  <div style="display:flex;gap:8px;margin-top:2px">';
      h += '    <div class="skeleton-shimmer" style="width:50px;height:10px;border-radius:3px"></div>';
      h += '    <div class="skeleton-shimmer" style="width:40px;height:10px;border-radius:3px"></div>';
      h += '  </div>';
      h += '</div>';
    }
    return h;
  }
  if(type === 'list'){
    h += '<div style="display:flex;flex-direction:column;gap:8px;padding:4px 0">';
    for(var i=0; i<count; i++){
      h += '<div style="display:flex;align-items:center;gap:10px;padding:8px 4px;border-bottom:1px solid var(--divider)">';
      h += '  <div class="skeleton-shimmer" style="width:16px;height:16px;border-radius:3px;flex-shrink:0"></div>';
      h += '  <div style="flex:1;display:flex;flex-direction:column;gap:6px">';
      h += '    <div class="skeleton-shimmer" style="width:' + (60 + (i*12)%35) + '%;height:12px"></div>';
      h += '    <div class="skeleton-shimmer" style="width:35%;height:9px;opacity:0.6"></div>';
      h += '  </div>';
      h += '</div>';
    }
    h += '</div>';
    return h;
  }
  if(type === 'chart'){
    h += '<div class="skeleton-bar-row">';
    for(var i=0; i<12; i++){
      var rh = 20 + ((i * 37) % 75);
      h += '<div class="skeleton-shimmer" style="flex:1;height:' + rh + '%;border-radius:3px 3px 0 0"></div>';
    }
    h += '</div>';
    return h;
  }
  if(type === 'grid'){
    h += '<div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-bottom:16px">';
    for(var i=0; i<count; i++){
      h += '<div style="background:var(--surface);border:1px solid var(--border);border-radius:8px;padding:12px;display:flex;flex-direction:column;gap:8px">';
      h += '  <div class="skeleton-shimmer" style="width:50%;height:10px"></div>';
      h += '  <div class="skeleton-shimmer" style="width:70%;height:22px"></div>';
      h += '</div>';
    }
    h += '</div>';
    return h;
  }
  return '<div class="skeleton-card"><div class="skeleton-shimmer" style="width:70%;height:16px"></div><div class="skeleton-shimmer" style="width:90%;height:12px"></div></div>';
}

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
var _iconPaths = {
  'history': 'M480-120q-126 0-223-76.5T131-392q-4-15 6-27.5t27-14.5q16-2 29 6t18 24q24 90 99 147t170 57q117 0 198.5-81.5T760-480q0-117-81.5-198.5T480-760q-69 0-129 32t-101 88h70q17 0 28.5 11.5T360-600q0 17-11.5 28.5T320-560H160q-17 0-28.5-11.5T120-600v-160q0-17 11.5-28.5T160-800q17 0 28.5 11.5T200-760v54q51-64 124.5-99T480-840q75 0 140.5 28.5t114 77q48.5 48.5 77 114T840-480q0 75-28.5 140.5t-77 114q-48.5 48.5-114 77T480-120Zm40-376 100 100q11 11 11 28t-11 28q-11 11-28 11t-28-11L452-452q-6-6-9-13.5t-3-15.5v-159q0-17 11.5-28.5T480-680q17 0 28.5 11.5T520-640v144Z',
  'settings': 'M433-80q-27 0-46.5-18T363-142l-9-66q-13-5-24.5-12T307-235l-62 26q-25 11-50 2t-39-32l-47-82q-14-23-8-49t27-43l53-40q-1-7-1-13.5v-27q0-6.5 1-13.5l-53-40q-21-17-27-43t8-49l47-82q14-23 39-32t50 2l62 26q11-8 23-15t24-12l9-66q4-26 23.5-44t46.5-18h94q27 0 46.5 18t23.5 44l9 66q13 5 24.5 12t22.5 15l62-26q25-11 50-2t39 32l47 82q14 23 8 49t-27 43l-53 40q1 7 1 13.5v27q0 6.5-2 13.5l53 40q21 17 27 43t-8 49l-48 82q-14 23-39 32t-50-2l-60-26q-11 8-23 15t-24 12l-9 66q-4 26-23.5 44T527-80h-94Zm7-80h79l14-106q31-8 57.5-23.5T639-327l99 41 39-68-86-65q5-14 7-29.5t2-31.5q0-16-2-31.5t-7-29.5l86-65-39-68-99 42q-22-23-48.5-38.5T533-694l-13-106h-79l-14 106q-31 8-57.5 23.5T321-633l-99-41-39 68 86 64q-5 15-7 30t-2 32q0 16 2 31t7 30l-86 65 39 68 99-42q22 23 48.5 38.5T427-266l13 106Zm42-180q58 0 99-41t41-99q0-58-41-99t-99-41q-59 0-99.5 41T342-480q0 58 40.5 99t99.5 41Zm-2-140Z',
  'home': 'M240-200h120v-200q0-17 11.5-28.5T400-440h160q17 0 28.5 11.5T600-400v200h120v-360L480-740 240-560v360Zm-80 0v-360q0-19 8.5-36t23.5-28l240-180q21-16 48-16t48 16l240 180q15 11 23.5 28t8.5 36v360q0 33-23.5 56.5T720-120H560q-17 0-28.5-11.5T520-160v-200h-80v200q0 17-11.5 28.5T400-120H240q-33 0-56.5-23.5T160-200Zm320-270Z',
  'edit_note': 'M200-400q-17 0-28.5-11.5T160-440q0-17 11.5-28.5T200-480h200q17 0 28.5 11.5T440-440q0 17-11.5 28.5T400-400H200Zm0-160q-17 0-28.5-11.5T160-600q0-17 11.5-28.5T200-640h360q17 0 28.5 11.5T600-600q0 17-11.5 28.5T560-560H200Zm0-160q-17 0-28.5-11.5T160-760q0-17 11.5-28.5T200-800h360q17 0 28.5 11.5T600-760q0 17-11.5 28.5T560-720H200Zm320 520v-66q0-8 3-15.5t9-13.5l209-208q9-9 20-13t22-4q12 0 23 4.5t20 13.5l37 37q8 9 12.5 20t4.5 22q0 11-4 22.5T863-380L655-172q-6 6-13.5 9t-15.5 3h-66q-17 0-28.5-11.5T520-200Zm300-223-37-37 37 37ZM580-220h38l121-122-18-19-19-18-122 121v38Zm141-141-19-18 37 37-18-19Z',
  'analytics': 'M320-480q-17 0-28.5 11.5T280-440v120q0 17 11.5 28.5T320-280q17 0 28.5-11.5T360-320v-120q0-17-11.5-28.5T320-480Zm320-200q-17 0-28.5 11.5T600-640v320q0 17 11.5 28.5T640-280q17 0 28.5-11.5T680-320v-320q0-17-11.5-28.5T640-680ZM480-400q-17 0-28.5 11.5T440-360v40q0 17 11.5 28.5T480-280q17 0 28.5-11.5T520-320v-40q0-17-11.5-28.5T480-400ZM200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Zm0-560v560-560Zm280 280q17 0 28.5-11.5T520-520q0-17-11.5-28.5T480-560q-17 0-28.5 11.5T440-520q0 17 11.5 28.5T480-480Z',
  'menu_book': 'M260-320q47 0 91.5 10.5T440-278v-394q-41-24-87-36t-93-12q-36 0-71.5 7T120-692v396q35-12 69.5-18t70.5-6Zm260 42q44-21 88.5-31.5T700-320q36 0 70.5 6t69.5 18v-396q-33-14-68.5-21t-71.5-7q-47 0-93 12t-87 36v394Zm-40 97q-14 0-26.5-3.5T430-194q-39-23-82-34.5T260-240q-42 0-82.5 11T100-198q-21 11-40.5-1T40-234v-482q0-11 5.5-21T62-752q46-24 96-36t102-12q58 0 113.5 15T480-740q51-30 106.5-45T700-800q52 0 102 12t96 36q11 5 16.5 15t5.5 21v482q0 23-19.5 35t-40.5 1q-37-20-77.5-31T700-240q-45 0-88 11.5T530-194q-11 6-23.5 9.5T480-181ZM280-494Zm280-115q0-9 6.5-18.5T581-640q29-10 58-15t61-5q20 0 39.5 2.5T778-651q9 2 15.5 10t6.5 18q0 17-11 25t-28 4q-14-3-29.5-4.5T700-600q-26 0-51 5t-48 13q-18 7-29.5-1T560-609Zm0 220q0-9 6.5-18.5T581-420q29-10 58-15t61-5q20 0 39.5 2.5T778-431q9 2 15.5 10t6.5 18q0 17-11 25t-28 4q-14-3-29.5-4.5T700-380q-26 0-51 4.5T601-363q-18 7-29.5-.5T560-389Zm0-110q0-9 6.5-18.5T581-530q29-10 58-15t61-5q20 0 39.5 2.5T778-541q9 2 15.5 10t6.5 18q0 17-11 25t-28 4q-14-3-29.5-4.5T700-490q-26 0-51 5t-48 13q-18 7-29.5-1T560-499Z',
  'keyboard_command_key': 'M260-120q-58 0-99-41t-41-99q0-58 41-99t99-41h60v-160h-60q-58 0-99-41t-41-99q0-58 41-99t99-41q58 0 99 41t41 99v60h160v-60q0-58 41-99t99-41q58 0 99 41t41 99q0 58-41 99t-99 41h-60v160h60q58 0 99 41t41 99q0 58-41 99t-99 41q-58 0-99-41t-41-99v-60H400v60q0 58-41 99t-99 41Zm0-80q25 0 42.5-17.5T320-260v-60h-60q-25 0-42.5 17.5T200-260q0 25 17.5 42.5T260-200Zm440 0q25 0 42.5-17.5T760-260q0-25-17.5-42.5T700-320h-60v60q0 25 17.5 42.5T700-200ZM400-400h160v-160H400v160ZM260-640h60v-60q0-25-17.5-42.5T260-760q-25 0-42.5 17.5T200-700q0 25 17.5 42.5T260-640Zm380 0h60q25 0 42.5-17.5T760-700q0-25-17.5-42.5T700-760q-25 0-42.5 17.5T640-700v60Z',
  'expand_more': 'M480-362q-8 0-15-2.5t-13-8.5L268-557q-11-11-11-28t11-28q11-11 28-11t28 11l156 156 156-156q11-11 28-11t28 11q11 11 11 28t-11 28L508-373q-6 6-13 8.5t-15 2.5Z',
  'psychology': 'm434-410 4 32q1 8 6.5 13t13.5 5h44q8 0 13.5-5t6.5-13l4-32q8-3 14.5-7t11.5-9l30 13q7 3 14 1t11-9l22-38q4-7 2.5-14t-7.5-12l-26-19q2-8 2-16t-2-16l26-19q6-5 7.5-12t-2.5-14l-22-38q-4-7-11-9t-14 1l-30 13q-5-5-11.5-9t-14.5-7l-4-32q-1-8-6.5-13t-13.5-5h-44q-8 0-13.5 5t-6.5 13l-4 32q-8 3-14.5 7t-11.5 9l-30-13q-7-3-14-1t-11 9l-22 38q-4 7-2.5 14t7.5 12l26 19q-2 8-2 16t2 16l-26 19q-6 5-7.5 12t2.5 14l22 38q4 7 11 9t14-1l30-13q5 5 11.5 9t14.5 7Zm46-50q-25 0-42.5-17.5T420-520q0-25 17.5-42.5T480-580q25 0 42.5 17.5T540-520q0 25-17.5 42.5T480-460ZM240-252q-57-52-88.5-121.5T120-520q0-150 105-255t255-105q125 0 221.5 73.5T827-615l52 205q5 19-7 34.5T840-360h-80v120q0 33-23.5 56.5T680-160h-80v40q0 17-11.5 28.5T560-80q-17 0-28.5-11.5T520-120v-80q0-17 11.5-28.5T560-240h120v-160q0-17 11.5-28.5T720-440h68l-38-155q-23-91-98-148t-172-57q-116 0-198 81t-82 197q0 60 24.5 114t69.5 96l26 24v168q0 17-11.5 28.5T280-80q-17 0-28.5-11.5T240-120v-132Zm254-188Z',
  'mood': 'M480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-400Zm0 320q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-100q58 0 107-28t79-76q6-12-1-24t-21-12H316q-14 0-21 12t-1 24q30 48 79.5 76T480-260ZM620-520q25 0 42.5-17.5T680-580q0-25-17.5-42.5T620-640q-25 0-42.5 17.5T560-580q0 25 17.5 42.5T620-520Zm-280 0q25 0 42.5-17.5T400-580q0-25-17.5-42.5T340-640q-25 0-42.5 17.5T280-580q0 25 17.5 42.5T340-520Z',
  'bolt': 'm422-232 207-248H469l29-227-185 267h139l-30 208Zm-62-128H236q-24 0-35.5-21.5T203-423l299-430q10-14 26-19.5t33 .5q17 6 25 21t6 32l-32 259h155q26 0 36.5 23t-6.5 43L416-100q-11 13-27 17t-31-3q-15-7-23.5-21.5T328-139l32-221Zm111-110Z',
  'directions_run': 'M520-80v-200l-84-80-31 138q-4 16-17.5 24.5T358-192l-198-40q-17-3-26-17t-6-31q3-17 17-26.5t31-5.5l152 32 64-324-72 28v96q0 17-11.5 28.5T280-440q-17 0-28.5-11.5T240-480v-122q0-12 6.5-21.5T264-638l134-58q35-15 51.5-19.5T480-720q21 0 39 11t29 29l40 64q21 34 54.5 59t77.5 33q17 3 28.5 15t11.5 29q0 17-11.5 28t-27.5 9q-54-8-101-33.5T540-540l-24 120 72 68q6 6 9 13.5t3 15.5v243q0 17-11.5 28.5T560-40q-17 0-28.5-11.5T520-80Zm20-660q-33 0-56.5-23.5T460-820q0-33 23.5-56.5T540-900q33 0 56.5 23.5T620-820q0 33-23.5 56.5T540-740Z',
  'emoji_events': 'M282-622 168-508q-11 11-27.5 11.5T112-508q-11-11-11.5-27.5T111-564l29-30-28-28q-12-12-12-28t12-28l56-56-29-30q-11-11-11-27.5t12-28.5q11-11 27.5-11.5T196-821l30 29 56-56q12-12 28-12t28 12l28 28 30-29q11-11 27.5-11t28.5 12q11 11 11 28t-11 28L338-678l340 340 114-114q11-11 27.5-11.5T848-452q11 11 11.5 27.5T849-396l-29 30 28 28q12 12 12 28t-12 28l-56 56 29 30q11 11 11 27.5T820-140q-11 11-27.5 11.5T764-139l-30-29-56 56q-12 12-28 12t-28-12l-28-28-30 29q-11 11-27.5 11T508-112q-11-11-11-28t11-28l114-114-340-340Z',
  'sports_martial_arts': 'M200-160h560v-80H200v80Zm132-160h296l-23-160H355l-23 160ZM160-80q-17 0-28.5-11.5T120-120v-120q0-33 23.5-56.5T200-320h52l22-160h-74q-17 0-28.5-11.5T160-520q0-17 11.5-28.5T200-560h560q17 0 28.5 11.5T800-520q0 17-11.5 28.5T760-480h-74l22 160h52q33 0 56.5 23.5T840-240v120q0 17-11.5 28.5T800-80H160Zm320-783q8 0 16 2.5t15 7.5q23 18 49 30t55 12q21 0 40-6t37-16q10-5 19.5-4.5T729-832q8 5 11.5 13.5T742-800l-53 240h-82l39-173-7.5 1q-7.5 1-23.5 1-36 0-70.5-11T480-773q-29 20-62.5 31T349-731q-18 0-26.5-1l-8.5-1 39 173h-82l-53-240q-2-11 1.5-19.5T231-833q8-5 18-5.5t19 5.5q18 10 37 16t40 6q29 0 55-12t49-30q7-5 15-7.5t16-2.5Z',
  'fitness_center': 'M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm440-80h120v-80H640v80Zm-280 0h120v-80H360v80ZM200-200h560v-560H200v560Zm80-320h400v-80H280v80Zm-80 240v-560 560Z',
  'chess': 'M242-249q-20-11-31-29.5T200-320v-192l-96-53q-11-6-16-15t-5-20q0-11 5-20t16-15l338-184q9-5 18.5-7.5T480-829q10 0 19.5 2.5T518-819l381 208q10 5 15.5 14.5T920-576v256q0 17-11.5 28.5T880-280q-17 0-28.5-11.5T840-320v-236l-80 44v192q0 23-11 41.5T718-249L518-141q-9 5-18.5 7.5T480-131q-10 0-19.5-2.5T442-141L242-249Zm238-203 274-148-274-148-274 148 274 148Zm0 241 200-108v-151l-161 89q-9 5-19 7.5t-20 2.5q-10 0-20-2.5t-19-7.5l-161-89v151l200 108Z',
  'school': 'M440-200v-124q-49-11-87.5-41.5T296-442q-75-9-125.5-65.5T120-640v-40q0-33 23.5-56.5T200-760h80q0-33 23.5-56.5T360-840h240q33 0 56.5 23.5T680-760h80q33 0 56.5 23.5T840-680v40q0 76-50.5 132.5T664-442q-18 46-56.5 76.5T520-324v124h120q17 0 28.5 11.5T680-160q0 17-11.5 28.5T640-120H320q-17 0-28.5-11.5T280-160q0-17 11.5-28.5T320-200h120ZM280-528v-152h-80v40q0 38 22 68.5t58 43.5Zm200 128q50 0 85-35t35-85v-240H360v240q0 50 35 85t85 35Zm200-128q36-13 58-43.5t22-68.5v-40h-80v152Zm-200-52Z',
  'monitor_heart': 'M480-280q66 0 113-47t47-113v-120q0-66-47-113t-113-47q-66 0-113 47t-47 113v120q0 66 47 113t113 47Zm0-80q-33 0-56.5-23.5T400-440v-120q0-33 23.5-56.5T480-640q33 0 56.5 23.5T560-560v120q0 33-23.5 56.5T480-360Zm0-120Zm-40 440v-80H120q-17 0-28.5-11.5T80-200v-240h80v240h320v80Zm240 0v-80H520v-320h-80v-40q0-50 35-85t85-35q50 0 85 35t35 85v40h80v-40q0-83-58.5-141.5T560-760q-83 0-141.5 58.5T360-560v40h-40v-40q0-83 58.5-141.5T480-760q83 0 141.5 58.5T680-560v40h80v-40q0-83-58.5-141.5T640-760v-40h40v40h80v240q0 17-11.5 28.5T760-520Zm-360 40h240v-80H360v80Z',
  'bedtime': 'M480-120q-60 0-112.5-25.5T280-230q-34-25-57-64.5t-33-85.5q-60-8-100-51t-40-98q0-58 41-99.5t99-41.5q8-58 50.5-99t100.5-41q14 0 27 2t14 6q-24 24-38.5 57T296-578q0 75 52.5 127.5T476-398h8q66 0 113-47t47-113q0-66-47-113t-113-47q-66 0-113 47t-47 113q0 8 0.5 15.5T332-480H280q-17 0-28.5-11.5T240-520q0-17 11.5-28.5T280-560h48q-44-28-68-74.5T236-720q0-58 41-99t99-41q58 0 99 41t41 99q0 58-41 99t-99 41h-8q-33 0-56.5 23.5T432-608q0 33 23.5 56.5T512-520h-8q-66 0-113 47t-47 113q0 66 47 113t113 47q66 0 113-47t47-113q0-8-0.5-15.5T632-480h52q17 0 28.5 11.5T724-440q0 17-11.5 28.5T684-400h-48q44 28 68 74.5T732-248q0 58-41 99t-99 41q-58 0-99-41t-41-99q0-8 0.5-15.5T556-320h-8q-33 0-56.5-23.5T484-400q0-33 23.5-56.5T560-480h8q66 0 113-47t47-113q0-66-47-113t-113-47q-66 0-113 47t-47 113q0 8 0.5 15.5T432-480H380q-17 0-28.5-11.5T340-520q0-17 11.5-28.5T380-560h48q-44-28-68-74.5T296-720q0-58 41-99t99-41q58 0 99 41t41 99q0 66-47 113t-113 47Zm0 80q66 0 113-47t47-113v-80H320v80q0 66 47 113t113 47Zm-360-40q-17 0-28.5-11.5T80-520v-160h160v160q0 17-11.5 28.5T200-320H120Zm560 0q-17 0-28.5-11.5T640-520v-160h160v160q0 17-11.5 28.5T760-320H680Z',
  'list_alt': 'M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Zm40-80h480v-80H240v80Zm0-160h480v-80H240v80Zm0-160h480v-80H240v80Zm-40 400v-560 560Z',
  'hourglass_empty': 'M240-120v-680h480v680l-240-120-240 120Zm80-120 160-80 160 80v-440H320v440Zm160-260q33 0 56.5-23.5T560-580q0-33-23.5-56.5T480-660q-33 0-56.5 23.5T400-580q0 33 23.5 56.5T480-500Z',
  'storage': 'M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-120H200v120Zm0-200h560v-120H200v120Zm0-200h560v-120H200v120Zm0-200h560v-80H200v80Zm80 480v-80h80v80h-80Zm160 0v-80h80v80h-80Zm160 0v-80h80v80h-80Zm-80-160v-80h80v80h-80Zm-160 0v-80h80v80h-80Zm-160 0v-80h80v80h-80Z',
  'check': 'M382-240 154-468l58-58 170 170 366-366 58 58-424 424Z',
  'check_circle': 'M480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q50 0 93.5-17.5T648-170q-55-35-101-52.5T448-240q-46 0-92 17.5T255-170q37 17 80.5 35T440-120Zm40-188 134-134q12-12 12-28t-12-28L508-530q-12-12-28-12t-28 12l-94 94-42-42q-12-12-28-12t-28 12l-42 42q-12 12-12 28t12 28l70 70q12 12 28 12t28-12Z',
  'checklist': 'M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Zm100-80h80v-80H300v80Zm0-120h260v-80H300v80Zm0-120h260v-80H300v80ZM460-300l60-60 20 20 40-40-60-60-20 20-40-40 20-20-20-20-60 60 60 60Z',
  'calendar_today': 'M480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q50 0 93.5-17.5T648-170q-55-35-101-52.5T448-240q-46 0-92 17.5T255-170q37 17 80.5 35T440-120Zm40-188 134-134q12-12 12-28t-12-28L508-530q-12-12-28-12t-28 12l-94 94-42-42q-12-12-28-12t-28 12l-42 42q-12 12-12 28t12 28l70 70q12 12 28 12t28-12Z',
  'calendar_month': 'M200-80q-33 0-56.5-23.5T120-160v-560q0-33 23.5-56.5T200-800h40v-80h80v80h240v-80h80v80h40q33 0 56.5 23.5T840-720v560q0 33-23.5 56.5T760-80H200Zm0-80h560v-400H200v400Zm0 0v-400 400Zm80-80h80v-80H280v80Zm160 0h80v-80H440v80Zm160 0h80v-80H600v80ZM280-440h80v-80H280v80Zm160 0h80v-80H440v80Zm160 0h80v-80H600v80ZM280-600h80v-80H280v80Zm160 0h80v-80H440v80Z',
  'screen_lock_portrait': 'M400-48q0-11 4.5-20.5T418-87l22-22V160q0-17 11.5-28.5T480-200q17 0 28.5 11.5T520-160v-109l22 22q8 8 17.5 12.5T598-240q11 0 20.5-4.5T636-256l124-124q8-8 12.5-17.5T808-418q0-11-4.5-20.5T792-456L668-580q-8 8-17.5 12.5T632-564q-11 0-20.5-4.5T594-580l-22-22v-158q0-17-11.5-28.5T532-800h-64q-17 0-28.5 11.5T428-760v158l-22 22q-8 8-17.5 12.5T366-564q-11 0-20.5-4.5T328-580L204-456q-8 8-12.5 17.5T188-418q0 11 4.5 20.5T204-380l124 124q8 8 17.5 12.5T364-240q11 0 20.5-4.5T402-256l-22-22V-87l17.5 17.5Q400-59 400-48Zm80-432q0-33-23.5-56.5T400-560h-64q-33 0-56.5 23.5T256-480v240q0 33 23.5 56.5T336-160h64q33 0 56.5-23.5T480-240v-240Z',
  'delete_outline': 'M280-120q-33 0-56.5-23.5T200-200v-520h-40v-80h200v-40h240v40h200v80h-40v520q0 33-23.5 56.5T680-120H280Zm80-160h80v-360h-80v360Zm160 0h80v-360h-80v360Z',
  'delete': 'M280-120q-33 0-56.5-23.5T200-200v-520h-40v-80h200v-40h240v40h200v80h-40v520q0 33-23.5 56.5T680-120H280Zm80-160h80v-360h-80v360Zm160 0h80v-360h-80v360Z',
  'flight_takeoff': 'M480-120q-22 0-40-10t-30-26q-12-16-22-42t-16-54H160q-17 0-28.5-11.5T120-280v-400q0-33 23.5-56.5T200-760h560q33 0 56.5 23.5T840-680v400q0 17-11.5 28.5T800-240H668q-6 38-16 54t-22 42q-12 16-30 26t-40 10h-28Zm0-80h28q22 0 40-10t30-26q12-16 22-42t16-54h80v400H538q6-38 16-54t22-42q12-16 30-26t40-10h28V-200ZM280-240h400v-400H280v400Zm0 0v-400 400Z',
  'cake': 'M240-120q-33 0-56.5-23.5T160-200v-360h640v360q0 33-23.5 56.5T720-120H240Zm0-80h480q17 0 28.5-11.5T760-240v-280H200v280q0 17 11.5 28.5T240-200Zm240-280q-50 0-85-35t-35-85v-80h240v80q0 50-35 85t35 85H480Zm-80-160v-80h160v80H400Zm80 80h-160v-80h160v80Z',
  'folder': 'M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h280l80 80h320q33 0 56.5 23.5T840-760v400q0 33-23.5 56.5T760-280H200Zm0-80h560v-400H516l-80-80H200v480Zm0 0v-480 480Z',
  'push_pin': 'M520-480q-25 0-42.5-17.5T460-540q0-25 17.5-42.5T520-600q25 0 42.5 17.5T580-540q0 25-17.5 42.5T520-480Zm0-400v-160h-80v160h80Zm0 400V400H280v400h160v-80h80v80h160V400H520Zm-80-200h160v-200H440v200Z',
  'add': 'M440-440H240q-17 0-28.5-11.5T200-480v-40q0-17 11.5-28.5T240-560h200V-800q0-17 11.5-28.5T480-840h40q17 0 28.5 11.5T560-800v240h200q17 0 28.5 11.5T800-520v40q0 17-11.5 28.5T760-440H560v240q0 17-11.5 28.5T520-160h-40q-17 0-28.5-11.5T440-200v-240Z',
  'warning': 'M476-80q-17 0-28.5-11.5T436-120v-360q0-14-10-24t-24-10q-14 0-24 10t-10 24v360q0 17-11.5 28.5T328-80h-32q-17 0-28.5-11.5T256-120v-360q0-14-10-24t-24-10q-14 0-24 10t-10 24v360q0 17-11.5 28.5T148-80h-32q-17 0-28.5-11.5T76-120v-360q0-14-10-24t-24-10q-14 0-24 10t-10 24v360q0 34-24 58.5T-28-80h504Zm-20-80h-464l144-400h176l144 400Zm-232-40q17 0 28.5-11.5T256-220q0-17-11.5-28.5T216-260q-17 0-28.5 11.5T176-220q0 17 11.5 28.5T216-180Zm160 0q17 0 28.5-11.5T416-220q0-17-11.5-28.5T376-260q-17 0-28.5 11.5T336-220q0 17 11.5 28.5T376-180Zm160 0q17 0 28.5-11.5T576-220q0-17-11.5-28.5T536-260q-17 0-28.5 11.5T496-220q0 17 11.5 28.5T536-180Z'
};
function _makeIconSvg(name, size, color) {
  var d = _iconPaths[name];
  if (!d) return null;
  var ns = 'http://www.w3.org/2000/svg';
  var svg = document.createElementNS(ns, 'svg');
  svg.setAttribute('viewBox', '0 -960 960 960');
  svg.setAttribute('width', size);
  svg.setAttribute('height', size);
  svg.setAttribute('fill', color);
  var path = document.createElementNS(ns, 'path');
  path.setAttribute('d', d);
  svg.appendChild(path);
  return svg;
}
function replaceMaterialIcons(root) {
  (root || document).querySelectorAll('.material-symbols-outlined').forEach(function(el) {
    if (el.getAttribute('data-replaced')) return;
    el.setAttribute('data-replaced', '1');
    var name = el.textContent.trim();
    var cs = window.getComputedStyle(el);
    var fs = parseInt(cs.fontSize) || 24;
    var fc = cs.color || '#e1e2ea';
    var svg = _makeIconSvg(name, fs, fc);
    if (!svg) return;
    svg.setAttribute('style', el.getAttribute('style') || '');
    svg.style.display = 'inline-block';
    svg.style.verticalAlign = 'middle';
    svg.style.fontSize = fs + 'px';
    if (el.onclick) svg.onclick = el.onclick;
    el.parentNode.replaceChild(svg, el);
  });
}
document.addEventListener('DOMContentLoaded', function() {
  replaceMaterialIcons();
  var obs = new MutationObserver(function(muts) {
    for (var i = 0; i < muts.length; i++) {
      if (muts[i].addedNodes.length) {
        replaceMaterialIcons(muts[i].target);
      }
    }
  });
  obs.observe(document.body, { childList: true, subtree: true });
  try{history.replaceState({tab:'home'},'',location.href)}catch(e){}
  window.addEventListener('popstate', function(e){
    var overlayIds=['gitaPage','databasePage','vaultPage','sessionsPage','moodEnergyPage','sleepUpPage','checklistPage','recycleBinPage','thoughtsPage','tasksPage','appUsagePage','runningPage','dayTrackerPage','logDashboardPage','settingsPage'];
    var closed=false;
    for(var i=0;i<overlayIds.length;i++){
      var el=document.getElementById(overlayIds[i]);
      if(el&&el.classList.contains('show')){el.classList.remove('show');closed=true;break}
    }
    if(!closed){
      var cmdPal=document.getElementById('cmdPaletteOverlay');
      if(cmdPal){cmdPal.remove();return}
      if(currentTab!=='home'){switchTab('home',true)}
    }else{
      var fab=document.getElementById('mainFab');
      if(fab)fab.style.display='flex';
    }
  });
  initGitaSwipe();
});
// ─── Core Utilities (continued) ───
const BASE = '';
function _pushOv(){try{history.pushState({ov:1},'',location.href)}catch(e){}}
var CATEGORY_ICONS = {
  mood: '\u{1F60A}',
  energy: '\u{26A1}',
  metrics: '\u{1F4CA}',
  habits: '\u{1F3AF}',
  journal: '\u{1F4DD}',
  thought: '\u{1F4AD}',
  dsa: '\u{1F4DA}',
  chess: '\u{265F}',
  running: '\u{1F3C3}',
  panics: '\u{1F630}',
  sessions: '\u{23F1}',
  tasks: '\u{1F4CB}',
  shares: '\u{1F512}'
};
const MOOD_LABELS={happy:'\u{1F60A} Happy',energetic:'\u{26A1} Energetic',motivated:'\u{1F680} Motivated',stressed:'\u{1F630} Stressed',anxious:'\u{1F61F} Anxious',tired:'\u{1F634} Tired',confused:'\u{1F615} Confused',relaxed:'\u{1F60C} Relaxed',frustrated:'\u{1F624} Frustrated',hopeful:'\u{1F31F} Hopeful'};
const ENERGY_LABELS={high:'\u{1F525} High',moderate:'\u{26A1} Moderate',balanced:'\u{2696}\u{FE0F} Balanced',low:'\u{1FAB5} Low',exhausted:'\u{1F480} Exhausted'};
function moodLabel(m){return MOOD_LABELS[m]||(m?m:'--')}
function energyLabel(e){return ENERGY_LABELS[e]||(e?e:'--')}
function localDate(){var d=new Date();return d.getFullYear()+'-'+String(d.getMonth()+1).padStart(2,'0')+'-'+String(d.getDate()).padStart(2,'0')}
function localDateTime(){return localDate()+'T'+String(new Date().getHours()).padStart(2,'0')+':'+String(new Date().getMinutes()).padStart(2,'0')}
const ACTIVITY_LABELS = {
  dsa: 'DSA',
  development: 'Development',
  ssb: 'SSB',
  running: 'Running',
  reading: 'Reading',
  content_creation: 'Content Creation',
  doom_scroll: 'Doom Scroll'
};
function activityLabel(a){return ACTIVITY_LABELS[a]||a}
function esc(s){return String(s||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;')}

function toast(msg){
  const el=document.getElementById('toast');
  if(!el){ console.error('Toast element not found'); return; }
  el.textContent=msg;
  el.classList.add('show');
  setTimeout(()=>el.classList.remove('show'),2000);
}

function customPrompt(message,defaultVal){
  return new Promise(function(resolve){
    var overlay=document.createElement('div');
    overlay.style.cssText='position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.7);z-index:200;display:flex;align-items:center;justify-content:center;padding:20px';
    var box=document.createElement('div');
    box.style.cssText='background:var(--surface-container);border:1px solid var(--border);border-radius:12px;padding:20px;width:100%;max-width:340px';
    box.innerHTML='<div style="font-family:\'JetBrains Mono\',\'Mono\',monospace;font-size:14px;font-weight:600;margin-bottom:12px;color:var(--primary)">'+message+'</div>'+
      '<input id="customPromptInput" type="text" value="'+(defaultVal||'')+'" style="width:100%;padding:10px;background:var(--surface-container-low);border:1px solid var(--border);border-radius:6px;color:var(--text);font-size:14px;outline:none;font-family:\'JetBrains Mono\',\'Mono\',monospace;margin-bottom:12px">'+
      '<div style="display:flex;gap:8px">'+
      '<button id="customPromptOk" class="btn btn-accent" style="flex:1">OK</button>'+
      '<button id="customPromptCancel" class="btn" style="flex:1">Cancel</button></div>';
    overlay.appendChild(box);
    document.body.appendChild(overlay);
    var input=document.getElementById('customPromptInput');
    input.focus();
    function close(val){document.body.removeChild(overlay);resolve(val)}
    document.getElementById('customPromptOk').onclick=function(){close(input.value)};
    document.getElementById('customPromptCancel').onclick=function(){close(null)};
    input.onkeydown=function(e){if(e.key==='Enter')close(input.value);if(e.key==='Escape')close(null)}
  });
}

function closeModal(){
  document.getElementById('modalOverlay').classList.remove('show');
}

async function logClientError(context,error,extra){
  try{
    var payload={level:'error',context:context,message:error?.toString?.()||String(error)};
    if(extra) Object.assign(payload,extra);
    await fetch(BASE+'/client/log',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(payload)});
  }catch(_){}
}

function wrapFetch(promise,context,extra){
  return promise.catch(function(e){logClientError(context,e,extra);return Promise.reject(e)});
}

async function api(method,path,body){
  const opts={method,headers:{}};
  if(body){
    opts.headers['Content-Type']='application/json';
    opts.body=JSON.stringify(body);
  }
  const r=await wrapFetch(fetch(BASE+path,opts),'api',{method:method,path:path});
  return r.json();
}

async function fetchPost(path){
  try{
    const r=await wrapFetch(fetch(BASE+path,{method:'POST'}),'fetchPost',{path:path});
    const j=await r.json().catch(()=>({ok:false,detail:'Invalid JSON'}));
    if(j.ok) toast('Done!');
    else if(j.detail) toast(j.detail);
    else toast('Done!');
    loadDashboard();
  }catch(e){logClientError('fetchPost',e,{path:path});toast('Error: '+e.message)}
}

async function fetchPostParams(path,params){
  const qs=new URLSearchParams();
  for(const[k,v]of Object.entries(params)){
    if(v!==null && v!==undefined && v!=='') qs.append(k,v);
  }
  try{
    const r=await wrapFetch(fetch(BASE+path+'?'+qs.toString(),{method:'POST'}),'fetchPostParams',{path:path,params:JSON.stringify(params)});
    const j=await r.json().catch(()=>({ok:false,detail:'Invalid JSON'}));
    if(j.ok) toast('Logged!');
    else if(j.detail) toast(j.detail);
    loadDashboard();
  }catch(e){logClientError('fetchPostParams',e,{path:path});toast('Error: '+e.message)}
}

function buildFields(container,fields,extra){
  container.innerHTML='';
  fields.forEach(function(f){
    var label=document.createElement('label');
    label.textContent=f.label;
    container.appendChild(label);
    var el;
    if(f.type==='select'){
      el=document.createElement('select');
      (f.options||[]).forEach(function(o){
        var opt=document.createElement('option');
        if(typeof o==='object'&&o!==null){opt.value=o.value;opt.textContent=o.label}
        else{opt.value=o;opt.textContent=o}
        el.appendChild(opt)
      });
    }else if(f.type==='textarea'){
      el=document.createElement('textarea');
    }else if(f.type==='number'){
      el=document.createElement('input');el.type='number';if(f.step)el.step=f.step;
    }else if(f.type==='date'){
      el=document.createElement('input');el.type='date';
    }else if(f.type==='time'){
      el=document.createElement('input');el.type='time';
    }else{
      el=document.createElement('input');el.type='text';
    }
    el.dataset.key=f.key;
    el.dataset.required=f.required!==false?'1':'0';
    el.id='modal_field_'+f.key;
    container.appendChild(el);
    if(f.chips){
      var chipDiv=document.createElement('div');
      chipDiv.className='chip-list';
      chipDiv.id='chips_'+f.key;
      var chipSrc=f.chips.length>0?f.chips:f.options||[];
      chipSrc.forEach(function(c){
        var chip=document.createElement('span');
        chip.className='chip';
        if(typeof c==='object'&&c!==null){chip.textContent=c.label;chip.dataset.val=c.value}
        else{chip.textContent=c;chip.dataset.val=c}
        chip.onclick=function(){el.value=chip.dataset.val;chipDiv.querySelectorAll('.chip').forEach(function(x){x.classList.remove('selected')});chip.classList.add('selected')};
        chipDiv.appendChild(chip);
      });
      container.appendChild(chipDiv);
    }
  });
  if(extra) Object.assign(extra,{});
}

// ─── Edit quick-log ───────────────────────────────────────────────────────────

document.querySelectorAll('[data-edit]').forEach(function(btn){
  btn.addEventListener('click',async function(){
    var field=btn.dataset.edit;
    var value=await customPrompt('What should the new value be for "'+field+'"?');
    if(value===null||value.trim()==='')return;
    var qs='field='+encodeURIComponent(field)+'&value='+encodeURIComponent(value.trim());
    wrapFetch(fetch(BASE+'/edit?'+qs,{method:'POST'}),'edit',{field:field,value:value}).then(function(r){return r.json()}).then(function(j){
      if(j.ok) toast('Edited '+field+'!');
      else if(j.detail) toast(j.detail);
      loadDashboard();
    }).catch(function(e){logClientError('edit',e,{field:field});toast('Error')});
  });
});

// ─── Notifications ────────────────────────────────────────────────────────────

function requestNotifications(){
  if('Notification' in window && Notification.permission==='default'){
    Notification.requestPermission();
  }
}
requestNotifications();
