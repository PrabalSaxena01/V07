# -*- coding: utf-8 -*-
with open('app/src/main/assets/index.html', encoding='utf-8') as f:
    html = f.read()

tests = [
    # 1. Timer mode
    ("Timer mode count-up variable", "let timerElapsed = 0;" in html),
    ("Timer mode label", "∞ Open Flow" in html),
    ("Timer mode toast", "Counts up from 0 to infinite" in html),
    
    # 2. Activity & Label custom options
    ("Activity custom option", '<option value="__add_custom__" class="font-bold text-primary">+ Add custom activity...</option>' in html),
    ("Label custom option", '<option value="__add_custom__" class="font-bold text-primary">+ Add custom label...</option>' in html),
    ("Activity change handler", 'onchange="onActivitySelectChanged(this)"' in html),
    ("Label change handler", 'onchange="onLabelSelectChanged(this)"' in html),
    ("addCustomActivity function", "function addCustomActivity(name)" in html),
    ("addCustomLabel function", "function addCustomLabel(tag)" in html),
    
    # 3. Companion sync & login
    ("Companion sync section", "V07 Companion Sync" in html),
    ("Companion login modal function", "function openCompanionLoginModal()" in html),
    ("Companion sync function", "function syncCompanionData()" in html),
    ("Companion user display", 'id="companion-user-display"' in html),
    
    # 4. Custom popups
    ("Custom modal container", 'id="v07-modal-container"' in html),
    ("showCustomModal function", "function showCustomModal(" in html),
    ("showCustomPrompt function", "function showCustomPrompt(" in html),
    ("showCustomConfirm function", "function showCustomConfirm(" in html),
    
    # 5. Custom notifications
    ("Notifications drawer container", 'id="v07-notifications-drawer"' in html),
    ("openNotificationDrawer function", "function openNotificationDrawer()" in html),
    ("triggerTestNotification function", "function triggerTestNotification()" in html),
    ("Notification badge dot", 'id="notif-badge-dot"' in html),
    
    # 6. Plugin settings
    ("Plugin settings section", "Plugin Settings" in html),
    ("Focus plugin settings", 'id="plugin-cfg-focus"' in html),
    ("Reading plugin settings", 'id="plugin-cfg-reading"' in html),
    ("Habits plugin settings", 'id="plugin-cfg-habits"' in html),
    ("Screen time plugin settings", 'id="plugin-cfg-screentime"' in html),
    ("Mood plugin settings", 'id="plugin-cfg-mood"' in html),
    ("Gita plugin settings", 'id="plugin-cfg-gita"' in html),
    ("Weekly digest plugin settings", 'id="plugin-cfg-weekly"' in html),
    ("togglePluginAccordion function", "function togglePluginAccordion(id)" in html),
    ("savePluginSetting function", "function savePluginSetting(key, val)" in html),
    
    # 7. Header profile
    ("Header avatar initial", 'id="header-avatar-initial"' in html),
    ("openProfileDrawer call in header", 'onclick="openProfileDrawer()"' in html),
    ("Profile drawer container", 'id="v07-profile-drawer"' in html)
]

print("=== ENHANCEMENT VERIFICATION REPORT ===")
passed = 0
for name, result in tests:
    status = "PASS" if result else "FAIL"
    if result: passed += 1
    print(f"[{status}] {name}")

print(f"\nResult: {passed}/{len(tests)} tests passed.")
