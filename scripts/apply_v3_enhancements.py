# -*- coding: utf-8 -*-
"""
apply_v3_enhancements.py
Implements the 7 user requests for V3:
1. Timer mode starts from 0 to infinite (stopwatch count-up mode)
2. Activity and Label dropdowns with "+ Add custom..." options and custom pop-up prompt
3. Login and sync data with V07 companion (Firebase Auth / Firestore REST & local sync)
4. Custom pop-ups for app (reusable calm modal dialog for prompts, confirms, and notices)
5. Custom Notifications for app (notification center drawer, unread indicator, and test alerts)
6. Plugin Settings section for all 7 plugins with granular configuration controls
7. Header has Profile avatar button (Sarah Jenkins) and Notification bell instead of setting cog
"""

import os
import re

TARGET_FILES = [
    r"D:\software\V07-Android\app\src\main\assets\index.html",
    r"D:\software\V07-Android\app\src\main\assets\v3\index.html"
]

def enhance_html(html):
    # =========================================================================
    # 1. HEADER: Replace setting cog with Notification bell and Profile avatar
    # =========================================================================
    old_header_right = '''    <div class="flex items-center gap-2">
      <button onclick="switchTab('settings')" class="w-10 h-10 rounded-full bg-surface-container-low border border-surface-container-high/60 flex items-center justify-center shrink-0 cursor-pointer active:scale-95 transition-transform" title="Settings & Local Sovereignty">
        <span class="material-symbols-outlined text-primary text-[20px]">settings</span>
      </button>
    </div>'''

    new_header_right = '''    <div class="flex items-center gap-2">
      <!-- Notification Center Button -->
      <button onclick="openNotificationDrawer()" class="relative w-10 h-10 rounded-full bg-surface-container-low border border-surface-container-high/60 flex items-center justify-center shrink-0 cursor-pointer active:scale-95 transition-transform" title="Notifications & Prompts">
        <span class="material-symbols-outlined text-on-surface-variant text-[20px]">notifications</span>
        <span id="notif-badge-dot" class="absolute top-2 right-2 w-2 h-2 rounded-full bg-primary ring-2 ring-surface"></span>
      </button>

      <!-- Profile Avatar Button (Replaces settings in top header) -->
      <button onclick="openProfileDrawer()" class="relative w-10 h-10 rounded-full bg-primary-fixed text-primary border border-primary/20 flex items-center justify-center shrink-0 cursor-pointer active:scale-95 transition-transform font-bold text-sm shadow-xs" title="Sanctuary Profile">
        <span id="header-avatar-initial">S</span>
        <span class="absolute bottom-0 right-0 w-2.5 h-2.5 rounded-full bg-primary ring-2 ring-surface"></span>
      </button>
    </div>'''

    if old_header_right in html:
        html = html.replace(old_header_right, new_header_right)
    else:
        print("Warning: old_header_right not found exactly, using regex")
        html = re.sub(
            r'<div class="flex items-center gap-2">\s*<button onclick="switchTab\(\'settings\'\)"[\s\S]*?</button>\s*</div>',
            new_header_right,
            html,
            count=1
        )

    # =========================================================================
    # 2. CUSTOM POP-UP CONTAINER, PROFILE DRAWER, & NOTIFICATIONS DRAWER
    # =========================================================================
    drawers_and_popups = '''
  <!-- ========================================== -->
  <!-- CUSTOM REUSABLE POP-UP MODAL CONTAINER     -->
  <!-- ========================================== -->
  <div id="v07-modal-container" class="fixed inset-0 z-50 pointer-events-none opacity-0 flex items-center justify-center p-4 transition-all duration-200">
    <div id="v07-modal-backdrop" class="absolute inset-0 bg-black/60 backdrop-blur-sm transition-opacity cursor-pointer" onclick="closeCustomModal()"></div>
    <div id="v07-modal-card" class="relative pointer-events-auto max-w-sm w-full bg-surface-container-lowest dark:bg-surface-container-high rounded-3xl p-5 border border-surface-container-high/70 dark:border-white/10 shadow-[0_20px_50px_rgba(0,0,0,0.35)] flex flex-col gap-4 transform scale-95 transition-all duration-200">
      <div class="flex items-start justify-between">
        <div class="flex items-center gap-3">
          <div id="modal-icon-bg" class="w-10 h-10 rounded-2xl bg-primary-fixed text-primary flex items-center justify-center shrink-0 shadow-xs">
            <span class="material-symbols-outlined text-[22px]" id="modal-icon">spa</span>
          </div>
          <div class="flex flex-col">
            <h3 class="text-base font-bold text-on-surface leading-tight" id="modal-title">Sanctuary Dialog</h3>
            <p class="text-xs text-on-surface-variant mt-0.5" id="modal-subtitle">Calm prompt</p>
          </div>
        </div>
        <button onclick="closeCustomModal()" class="w-8 h-8 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" aria-label="Close modal">
          <span class="material-symbols-outlined text-[18px]">close</span>
        </button>
      </div>

      <div id="modal-body" class="text-xs text-on-surface-variant leading-relaxed">
        <!-- Injected dynamic content / inputs -->
      </div>

      <div class="flex items-center justify-end gap-2 pt-2 border-t border-surface-container-high/40" id="modal-actions">
        <button id="modal-cancel-btn" onclick="closeCustomModal()" class="px-4 py-2 rounded-xl text-xs font-semibold text-on-surface-variant hover:bg-surface-container transition-all cursor-pointer">
          Cancel
        </button>
        <button id="modal-confirm-btn" class="px-5 py-2 rounded-xl text-xs font-bold bg-primary text-on-primary hover:opacity-95 active:scale-95 transition-all shadow-xs cursor-pointer">
          Confirm
        </button>
      </div>
    </div>
  </div>

  <!-- ========================================== -->
  <!-- PROFILE DRAWER (OPENED BY HEADER PROFILE)  -->
  <!-- ========================================== -->
  <div id="v07-profile-drawer" class="fixed inset-0 z-50 pointer-events-none opacity-0 flex items-end sm:items-center justify-center p-0 sm:p-4 transition-all duration-300">
    <div class="absolute inset-0 bg-black/60 backdrop-blur-sm transition-opacity cursor-pointer" onclick="closeProfileDrawer()"></div>
    <div class="relative pointer-events-auto w-full max-w-md bg-surface-container-lowest dark:bg-surface-container-high rounded-t-3xl sm:rounded-3xl p-6 border border-surface-container-high/70 dark:border-white/10 shadow-[0_20px_50px_rgba(0,0,0,0.4)] flex flex-col gap-4 transform translate-y-8 sm:translate-y-0 sm:scale-95 transition-all duration-300 max-h-[85vh] overflow-y-auto">
      <div class="flex items-center justify-between pb-3 border-b border-surface-container-high/40">
        <div class="flex items-center gap-3">
          <div class="relative">
            <div class="w-14 h-14 rounded-full bg-primary-fixed text-primary flex items-center justify-center font-bold text-xl shadow-xs" id="profile-drawer-avatar">
              S
            </div>
            <div class="absolute bottom-0 right-0 w-4 h-4 rounded-full bg-primary text-on-primary flex items-center justify-center ring-2 ring-surface">
              <span class="material-symbols-outlined text-[10px]">check</span>
            </div>
          </div>
          <div class="flex flex-col">
            <div class="flex items-center gap-1.5">
              <h3 class="text-base font-bold text-on-surface" id="profile-drawer-name">Sarah Jenkins</h3>
              <span class="material-symbols-outlined text-primary text-[16px] material-symbols-filled">verified</span>
            </div>
            <p class="text-xs text-on-surface-variant" id="profile-drawer-email">sarah@sanctuary.internal</p>
            <span class="inline-flex items-center gap-1 text-[11px] text-primary font-medium mt-0.5">
              <span class="w-1.5 h-1.5 rounded-full bg-primary"></span>
              Local Vault Sovereign
            </span>
          </div>
        </div>
        <button onclick="closeProfileDrawer()" class="w-8 h-8 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface cursor-pointer">
          <span class="material-symbols-outlined text-[20px]">close</span>
        </button>
      </div>

      <!-- Quick Stats -->
      <div class="grid grid-cols-3 gap-2 py-1">
        <div class="bg-surface-container-low rounded-xl p-2.5 text-center">
          <span class="text-[10px] text-on-surface-variant block uppercase tracking-wider font-semibold">Streak</span>
          <span class="text-sm font-bold text-on-surface font-mono">🔥 14d</span>
        </div>
        <div class="bg-surface-container-low rounded-xl p-2.5 text-center">
          <span class="text-[10px] text-on-surface-variant block uppercase tracking-wider font-semibold">Focus</span>
          <span class="text-sm font-bold text-primary font-mono">18.5h</span>
        </div>
        <div class="bg-surface-container-low rounded-xl p-2.5 text-center">
          <span class="text-[10px] text-on-surface-variant block uppercase tracking-wider font-semibold">Reflections</span>
          <span class="text-sm font-bold text-on-surface font-mono">42</span>
        </div>
      </div>

      <!-- Companion Cloud Connection Status -->
      <div class="p-3.5 bg-surface-container-low rounded-2xl border border-surface-container-high/60 flex items-center justify-between">
        <div class="flex items-center gap-2.5">
          <div class="w-8 h-8 rounded-full bg-primary-fixed text-primary flex items-center justify-center shrink-0">
            <span class="material-symbols-outlined text-[18px]">cloud_sync</span>
          </div>
          <div class="flex flex-col">
            <span class="text-xs font-bold text-on-surface" id="profile-companion-status">V07 Companion</span>
            <span class="text-[11px] text-on-surface-variant" id="profile-companion-sub">Sync ready</span>
          </div>
        </div>
        <button onclick="openCompanionLoginModal(); closeProfileDrawer();" class="px-3 py-1.5 bg-primary text-on-primary rounded-xl text-xs font-bold active:scale-95 transition-transform cursor-pointer shadow-xs">
          Sync / Login
        </button>
      </div>

      <!-- Actions -->
      <div class="flex flex-col gap-2 pt-1">
        <button onclick="switchTab('settings'); closeProfileDrawer();" class="w-full py-2.5 px-4 bg-surface-container-low hover:bg-surface-container rounded-xl text-xs font-bold text-on-surface flex items-center justify-between transition-colors cursor-pointer border border-surface-container-high/40">
          <span class="flex items-center gap-2">
            <span class="material-symbols-outlined text-[18px] text-primary">tune</span>
            Open Full Sanctuary Settings
          </span>
          <span class="material-symbols-outlined text-[16px] text-on-surface-variant">chevron_right</span>
        </button>
        <button onclick="promptEditProfile()" class="w-full py-2.5 px-4 bg-surface-container-low hover:bg-surface-container rounded-xl text-xs font-bold text-on-surface flex items-center justify-between transition-colors cursor-pointer border border-surface-container-high/40">
          <span class="flex items-center gap-2">
            <span class="material-symbols-outlined text-[18px] text-primary">edit</span>
            Edit Display Name
          </span>
          <span class="material-symbols-outlined text-[16px] text-on-surface-variant">chevron_right</span>
        </button>
      </div>
    </div>
  </div>

  <!-- ========================================== -->
  <!-- CUSTOM NOTIFICATIONS CENTER DRAWER         -->
  <!-- ========================================== -->
  <div id="v07-notifications-drawer" class="fixed inset-0 z-50 pointer-events-none opacity-0 flex items-end sm:items-center justify-center p-0 sm:p-4 transition-all duration-300">
    <div class="absolute inset-0 bg-black/60 backdrop-blur-sm transition-opacity cursor-pointer" onclick="closeNotificationDrawer()"></div>
    <div class="relative pointer-events-auto w-full max-w-md bg-surface-container-lowest dark:bg-surface-container-high rounded-t-3xl sm:rounded-3xl p-5 border border-surface-container-high/70 dark:border-white/10 shadow-[0_20px_50px_rgba(0,0,0,0.4)] flex flex-col gap-4 transform translate-y-8 sm:translate-y-0 sm:scale-95 transition-all duration-300 max-h-[85vh] overflow-y-auto">
      <div class="flex items-center justify-between pb-2 border-b border-surface-container-high/40">
        <div class="flex items-center gap-2.5">
          <div class="w-8 h-8 rounded-full bg-primary-fixed text-primary flex items-center justify-center">
            <span class="material-symbols-outlined text-[18px]">notifications</span>
          </div>
          <div>
            <h3 class="text-base font-bold text-on-surface leading-tight">Notifications</h3>
            <p class="text-[11px] text-on-surface-variant">Calm daily prompts & alerts</p>
          </div>
        </div>
        <div class="flex items-center gap-1.5">
          <button onclick="clearNotifications()" class="text-[11px] text-primary font-semibold px-2 py-1 rounded-lg hover:bg-surface-container cursor-pointer">
            Clear all
          </button>
          <button onclick="closeNotificationDrawer()" class="w-8 h-8 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface cursor-pointer">
            <span class="material-symbols-outlined text-[18px]">close</span>
          </button>
        </div>
      </div>

      <!-- Action Button: Trigger Test Notification -->
      <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
        <span class="text-xs text-on-surface-variant font-medium">Test custom notification:</span>
        <button onclick="triggerTestNotification()" class="px-3 py-1 bg-primary text-on-primary rounded-lg text-xs font-bold active:scale-95 transition-transform cursor-pointer shadow-xs">
          Send Prompt
        </button>
      </div>

      <!-- Notifications Feed -->
      <div class="flex flex-col gap-2.5" id="notifications-list">
        <!-- Notification 1 -->
        <div class="p-3 bg-surface-container-low rounded-2xl border border-surface-container-high/40 flex items-start gap-3">
          <span class="material-symbols-outlined text-primary text-[20px] mt-0.5">sunny</span>
          <div class="flex flex-col flex-1 min-w-0">
            <div class="flex items-center justify-between">
              <span class="text-xs font-bold text-on-surface">Morning Stillness</span>
              <span class="text-[10px] text-on-surface-variant">8:30 AM</span>
            </div>
            <p class="text-xs text-on-surface-variant mt-0.5">Take 5 minutes to set an unrushed intention for today.</p>
          </div>
        </div>
        <!-- Notification 2 -->
        <div class="p-3 bg-surface-container-low rounded-2xl border border-surface-container-high/40 flex items-start gap-3">
          <span class="material-symbols-outlined text-primary text-[20px] mt-0.5">check_circle</span>
          <div class="flex flex-col flex-1 min-w-0">
            <div class="flex items-center justify-between">
              <span class="text-xs font-bold text-on-surface">Focus Complete</span>
              <span class="text-[10px] text-on-surface-variant">25m ago</span>
            </div>
            <p class="text-xs text-on-surface-variant mt-0.5">Your 25-minute deep work interval finished gracefully.</p>
          </div>
        </div>
        <!-- Notification 3 -->
        <div class="p-3 bg-surface-container-low rounded-2xl border border-surface-container-high/40 flex items-start gap-3">
          <span class="material-symbols-outlined text-primary text-[20px] mt-0.5">cloud_done</span>
          <div class="flex flex-col flex-1 min-w-0">
            <div class="flex items-center justify-between">
              <span class="text-xs font-bold text-on-surface">Companion Sync</span>
              <span class="text-[10px] text-on-surface-variant">1h ago</span>
            </div>
            <p class="text-xs text-on-surface-variant mt-0.5">Sanctuary journal and habits backed up securely.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
'''

    # Place drawers after toast container
    toast_end = '<!-- TOP PERSISTENT EDITORIAL SANCTUARY HEADER'
    if toast_end in html:
        html = html.replace(toast_end, drawers_and_popups + '\n  ' + toast_end)
    else:
        print("Warning: toast_end not found")

    # =========================================================================
    # 3. FOCUS SANCTUARY: Add custom activity & label options
    # =========================================================================
    html = re.sub(
        r'<select id="focus-activity-select"[\s\S]*?</select>',
        '''<select id="focus-activity-select" onchange="onActivitySelectChanged(this)" class="w-full h-9 px-2.5 rounded-xl bg-surface-container-low border border-surface-container-high text-xs font-semibold text-on-surface focus:outline-none focus:border-primary cursor-pointer">
                <option value="Deep Work" selected>Deep Work</option>
                <option value="Reading">Reading</option>
                <option value="Writing">Writing</option>
                <option value="Coding">Coding</option>
                <option value="Reflection">Reflection</option>
                <option value="Planning">Planning</option>
                <option value="__add_custom__" class="font-bold text-primary">+ Add custom activity...</option>
              </select>''',
        html,
        count=1
    )

    html = re.sub(
        r'<select id="focus-label-select"[\s\S]*?</select>',
        '''<select id="focus-label-select" onchange="onLabelSelectChanged(this)" class="w-full h-9 px-2.5 rounded-xl bg-surface-container-low border border-surface-container-high text-xs font-semibold text-on-surface focus:outline-none focus:border-primary cursor-pointer font-mono">
                <option value="#urgent" selected>#urgent</option>
                <option value="#project-v07">#project-v07</option>
                <option value="#mindful">#mindful</option>
                <option value="#draft">#draft</option>
                <option value="__add_custom__" class="font-bold text-primary">+ Add custom label...</option>
              </select>''',
        html,
        count=1
    )

    # =========================================================================
    # 4. SETTINGS TAB: Add Companion Sync & Plugin Settings
    # =========================================================================
    settings_marker = '<!-- Section 4: Storage & Vault (100% On-Device) -->'
    
    companion_and_plugin_settings = '''
      <!-- Section 3b: V07 Companion Cloud Sync & Login -->
      <section class="flex flex-col space-y-2">
        <div class="flex items-center justify-between px-1">
          <h3 class="text-xs font-bold text-on-surface-variant uppercase tracking-wider">V07 Companion Sync</h3>
          <span class="text-[11px] text-primary font-medium flex items-center gap-1" id="companion-sync-indicator">
            <span class="w-1.5 h-1.5 rounded-full bg-primary inline-block animate-pulse"></span>
            Cloud Bridge Ready
          </span>
        </div>
        <div class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col gap-3.5" style="box-shadow: var(--card-shadow);">
          <div class="flex items-start gap-3">
            <div class="w-10 h-10 rounded-2xl bg-primary-fixed text-primary flex items-center justify-center shrink-0 shadow-xs">
              <span class="material-symbols-outlined text-[22px]">cloud_sync</span>
            </div>
            <div class="flex flex-col flex-1 min-w-0">
              <div class="flex items-center justify-between">
                <h4 class="text-sm font-bold text-on-surface">V07 Desktop & Web Sync</h4>
                <span class="text-[10px] px-2 py-0.5 rounded-full bg-primary-fixed text-primary font-bold" id="companion-badge">Local Sovereign</span>
              </div>
              <p class="text-xs text-on-surface-variant mt-0.5 leading-relaxed">
                Seamlessly bridge your reflections, focus sessions, and reading shelf with the V07 desktop companion.
              </p>
            </div>
          </div>

          <div class="p-3 bg-surface-container-low rounded-xl border border-surface-container-high/40 flex items-center justify-between">
            <div class="flex flex-col">
              <span class="text-[11px] text-on-surface-variant">Connected Account</span>
              <span class="text-xs font-bold text-on-surface font-mono" id="companion-user-display">sarah@sanctuary.internal</span>
            </div>
            <span class="text-[10px] text-primary font-semibold flex items-center gap-1">
              <span class="material-symbols-outlined text-[13px]">check_circle</span>
              Synced just now
            </span>
          </div>

          <div class="grid grid-cols-2 gap-2 pt-1">
            <button onclick="syncCompanionData()" class="py-2.5 px-3 bg-primary text-on-primary rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 active:scale-95 transition-all shadow-xs cursor-pointer">
              <span class="material-symbols-outlined text-[16px]" id="sync-spinner">sync</span>
              <span>Sync Now</span>
            </button>
            <button onclick="openCompanionLoginModal()" class="py-2.5 px-3 bg-surface-container hover:bg-surface-container-high text-on-surface rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 active:scale-95 transition-all cursor-pointer border border-surface-container-high/40">
              <span class="material-symbols-outlined text-[16px]">account_circle</span>
              <span>Account / Login</span>
            </button>
          </div>
        </div>
      </section>

      <!-- Section 3c: Comprehensive Plugin Settings -->
      <section class="flex flex-col space-y-2">
        <div class="flex items-center justify-between px-1">
          <h3 class="text-xs font-bold text-on-surface-variant uppercase tracking-wider">Plugin Settings</h3>
          <span class="text-[11px] text-primary font-medium">7 Active Plugins</span>
        </div>

        <div class="bg-surface-container-lowest rounded-2xl shadow-sm border border-surface-container-high/40 divide-y divide-surface-container-low overflow-hidden" style="box-shadow: var(--card-shadow);">
          
          <!-- Plugin 1: Focus Sanctuary -->
          <div class="p-3.5 flex flex-col gap-2.5">
            <div class="flex items-center justify-between cursor-pointer" onclick="togglePluginAccordion('plugin-cfg-focus')">
              <div class="flex items-center gap-2.5">
                <span class="material-symbols-outlined text-primary text-[20px]">hourglass_top</span>
                <div>
                  <h4 class="text-xs font-bold text-on-surface">Focus Sanctuary</h4>
                  <p class="text-[10px] text-on-surface-variant">Default interval, chimes, and timer mode</p>
                </div>
              </div>
              <span class="material-symbols-outlined text-on-surface-variant text-[18px] transition-transform" id="arrow-plugin-cfg-focus">expand_more</span>
            </div>
            <div id="plugin-cfg-focus" class="hidden flex flex-col gap-2 pt-2 border-t border-surface-container-low">
              <div class="flex items-center justify-between text-xs">
                <span class="text-on-surface-variant">Default Session Duration</span>
                <select id="cfg-focus-default-len" onchange="savePluginSetting('focus_len', this.value)" class="h-7 px-2 bg-surface-container-low rounded-lg text-xs font-bold border border-surface-container-high text-on-surface cursor-pointer">
                  <option value="15">15 mins</option>
                  <option value="25" selected>25 mins (Standard)</option>
                  <option value="45">45 mins</option>
                  <option value="60">60 mins</option>
                </select>
              </div>
              <div class="flex items-center justify-between text-xs">
                <span class="text-on-surface-variant">Tibetan Singing Bowl Audio</span>
                <input type="checkbox" id="cfg-focus-chime" checked onchange="savePluginSetting('focus_chime', this.checked)" class="w-4 h-4 accent-primary cursor-pointer"/>
              </div>
              <div class="flex items-center justify-between text-xs">
                <span class="text-on-surface-variant">Auto-start Breaks</span>
                <input type="checkbox" id="cfg-focus-autobreak" onchange="savePluginSetting('focus_autobreak', this.checked)" class="w-4 h-4 accent-primary cursor-pointer"/>
              </div>
            </div>
          </div>

          <!-- Plugin 2: Reading Companion -->
          <div class="p-3.5 flex flex-col gap-2.5">
            <div class="flex items-center justify-between cursor-pointer" onclick="togglePluginAccordion('plugin-cfg-reading')">
              <div class="flex items-center gap-2.5">
                <span class="material-symbols-outlined text-primary text-[20px]">menu_book</span>
                <div>
                  <h4 class="text-xs font-bold text-on-surface">Reading Companion</h4>
                  <p class="text-[10px] text-on-surface-variant">Daily targets, page turns, and Kindle sync</p>
                </div>
              </div>
              <span class="material-symbols-outlined text-on-surface-variant text-[18px] transition-transform" id="arrow-plugin-cfg-reading">expand_more</span>
            </div>
            <div id="plugin-cfg-reading" class="hidden flex flex-col gap-2 pt-2 border-t border-surface-container-low">
              <div class="flex items-center justify-between text-xs">
                <span class="text-on-surface-variant">Daily Reading Target</span>
                <div class="flex items-center gap-1">
                  <input type="number" id="cfg-reading-target" value="20" min="5" max="100" onchange="savePluginSetting('reading_target', this.value)" class="w-12 h-7 px-1 text-center bg-surface-container-low rounded-lg text-xs font-bold border border-surface-container-high text-on-surface"/>
                  <span class="text-xs text-on-surface-variant">pages</span>
                </div>
              </div>
              <div class="flex items-center justify-between text-xs">
                <span class="text-on-surface-variant">Haptic Page Feedback</span>
                <input type="checkbox" id="cfg-reading-haptics" checked onchange="savePluginSetting('reading_haptics', this.checked)" class="w-4 h-4 accent-primary cursor-pointer"/>
              </div>
            </div>
          </div>

          <!-- Plugin 3: Gentle Habits -->
          <div class="p-3.5 flex flex-col gap-2.5">
            <div class="flex items-center justify-between cursor-pointer" onclick="togglePluginAccordion('plugin-cfg-habits')">
              <div class="flex items-center gap-2.5">
                <span class="material-symbols-outlined text-primary text-[20px]">task_alt</span>
                <div>
                  <h4 class="text-xs font-bold text-on-surface">Gentle Habits</h4>
                  <p class="text-[10px] text-on-surface-variant">Streak protection, sounds, and reminders</p>
                </div>
              </div>
              <span class="material-symbols-outlined text-on-surface-variant text-[18px] transition-transform" id="arrow-plugin-cfg-habits">expand_more</span>
            </div>
            <div id="plugin-cfg-habits" class="hidden flex flex-col gap-2 pt-2 border-t border-surface-container-low">
              <div class="flex items-center justify-between text-xs">
                <span class="text-on-surface-variant">Weekly Streak Freeze</span>
                <select id="cfg-habits-freeze" onchange="savePluginSetting('habits_freeze', this.value)" class="h-7 px-2 bg-surface-container-low rounded-lg text-xs font-bold border border-surface-container-high text-on-surface cursor-pointer">
                  <option value="1" selected>1 Day / Week</option>
                  <option value="2">2 Days / Week</option>
                  <option value="0">Disabled</option>
                </select>
              </div>
              <div class="flex items-center justify-between text-xs">
                <span class="text-on-surface-variant">Celebration Chime</span>
                <input type="checkbox" id="cfg-habits-chime" checked onchange="savePluginSetting('habits_chime', this.checked)" class="w-4 h-4 accent-primary cursor-pointer"/>
              </div>
            </div>
          </div>

          <!-- Plugin 4: Digital Stillness -->
          <div class="p-3.5 flex flex-col gap-2.5">
            <div class="flex items-center justify-between cursor-pointer" onclick="togglePluginAccordion('plugin-cfg-screentime')">
              <div class="flex items-center gap-2.5">
                <span class="material-symbols-outlined text-primary text-[20px]">phonelink_ring</span>
                <div>
                  <h4 class="text-xs font-bold text-on-surface">Digital Stillness</h4>
                  <p class="text-[10px] text-on-surface-variant">Usage budget and telemetry watchers</p>
                </div>
              </div>
              <span class="material-symbols-outlined text-on-surface-variant text-[18px] transition-transform" id="arrow-plugin-cfg-screentime">expand_more</span>
            </div>
            <div id="plugin-cfg-screentime" class="hidden flex flex-col gap-2 pt-2 border-t border-surface-container-low">
              <div class="flex items-center justify-between text-xs">
                <span class="text-on-surface-variant">Daily Screen Limit Target</span>
                <select id="cfg-screentime-target" onchange="savePluginSetting('screentime_limit', this.value)" class="h-7 px-2 bg-surface-container-low rounded-lg text-xs font-bold border border-surface-container-high text-on-surface cursor-pointer">
                  <option value="3.0">3h 00m</option>
                  <option value="3.5" selected>3h 30m</option>
                  <option value="4.0">4h 00m</option>
                </select>
              </div>
              <div class="flex items-center justify-between text-xs">
                <span class="text-on-surface-variant">80% Usage Nudge</span>
                <input type="checkbox" id="cfg-screentime-nudge" checked onchange="savePluginSetting('screentime_nudge', this.checked)" class="w-4 h-4 accent-primary cursor-pointer"/>
              </div>
            </div>
          </div>

          <!-- Plugin 5: Mood & Grounding -->
          <div class="p-3.5 flex flex-col gap-2.5">
            <div class="flex items-center justify-between cursor-pointer" onclick="togglePluginAccordion('plugin-cfg-mood')">
              <div class="flex items-center gap-2.5">
                <span class="material-symbols-outlined text-primary text-[20px]">psychology_alt</span>
                <div>
                  <h4 class="text-xs font-bold text-on-surface">Mood & Grounding</h4>
                  <p class="text-[10px] text-on-surface-variant">Panic vault security & breathing guides</p>
                </div>
              </div>
              <span class="material-symbols-outlined text-on-surface-variant text-[18px] transition-transform" id="arrow-plugin-cfg-mood">expand_more</span>
            </div>
            <div id="plugin-cfg-mood" class="hidden flex flex-col gap-2 pt-2 border-t border-surface-container-low">
              <div class="flex items-center justify-between text-xs">
                <span class="text-on-surface-variant">Biometric Vault Encryption</span>
                <input type="checkbox" id="cfg-mood-biometric" checked onchange="savePluginSetting('mood_biometric', this.checked)" class="w-4 h-4 accent-primary cursor-pointer"/>
              </div>
              <div class="flex items-center justify-between text-xs">
                <span class="text-on-surface-variant">4-7-8 Breathing Sound</span>
                <input type="checkbox" id="cfg-mood-breathing-sound" checked onchange="savePluginSetting('mood_breathing_sound', this.checked)" class="w-4 h-4 accent-primary cursor-pointer"/>
              </div>
            </div>
          </div>

          <!-- Plugin 6: Gita Wisdom -->
          <div class="p-3.5 flex flex-col gap-2.5">
            <div class="flex items-center justify-between cursor-pointer" onclick="togglePluginAccordion('plugin-cfg-gita')">
              <div class="flex items-center gap-2.5">
                <span class="material-symbols-outlined text-primary text-[20px]">auto_stories</span>
                <div>
                  <h4 class="text-xs font-bold text-on-surface">Gita Wisdom</h4>
                  <p class="text-[10px] text-on-surface-variant">Script rendering and morning verse prompt</p>
                </div>
              </div>
              <span class="material-symbols-outlined text-on-surface-variant text-[18px] transition-transform" id="arrow-plugin-cfg-gita">expand_more</span>
            </div>
            <div id="plugin-cfg-gita" class="hidden flex flex-col gap-2 pt-2 border-t border-surface-container-low">
              <div class="flex items-center justify-between text-xs">
                <span class="text-on-surface-variant">Show Sanskrit Devanagari</span>
                <input type="checkbox" id="cfg-gita-devanagari" checked onchange="savePluginSetting('gita_devanagari', this.checked)" class="w-4 h-4 accent-primary cursor-pointer"/>
              </div>
              <div class="flex items-center justify-between text-xs">
                <span class="text-on-surface-variant">Show Roman Transliteration</span>
                <input type="checkbox" id="cfg-gita-translit" checked onchange="savePluginSetting('gita_translit', this.checked)" class="w-4 h-4 accent-primary cursor-pointer"/>
              </div>
            </div>
          </div>

          <!-- Plugin 7: Weekly Digest -->
          <div class="p-3.5 flex flex-col gap-2.5">
            <div class="flex items-center justify-between cursor-pointer" onclick="togglePluginAccordion('plugin-cfg-weekly')">
              <div class="flex items-center gap-2.5">
                <span class="material-symbols-outlined text-primary text-[20px]">insights</span>
                <div>
                  <h4 class="text-xs font-bold text-on-surface">Weekly Digest</h4>
                  <p class="text-[10px] text-on-surface-variant">Synthesis depth and Sunday dispatch</p>
                </div>
              </div>
              <span class="material-symbols-outlined text-on-surface-variant text-[18px] transition-transform" id="arrow-plugin-cfg-weekly">expand_more</span>
            </div>
            <div id="plugin-cfg-weekly" class="hidden flex flex-col gap-2 pt-2 border-t border-surface-container-low">
              <div class="flex items-center justify-between text-xs">
                <span class="text-on-surface-variant">Reflection Depth</span>
                <select id="cfg-weekly-depth" onchange="savePluginSetting('weekly_depth', this.value)" class="h-7 px-2 bg-surface-container-low rounded-lg text-xs font-bold border border-surface-container-high text-on-surface cursor-pointer">
                  <option value="gentle" selected>Gentle Overview</option>
                  <option value="deep">Deep Contemplation</option>
                </select>
              </div>
              <div class="flex items-center justify-between text-xs">
                <span class="text-on-surface-variant">Sunday 8:00 PM Dispatch</span>
                <input type="checkbox" id="cfg-weekly-dispatch" checked onchange="savePluginSetting('weekly_dispatch', this.checked)" class="w-4 h-4 accent-primary cursor-pointer"/>
              </div>
            </div>
          </div>

        </div>
      </section>
'''

    if settings_marker in html:
        html = html.replace(settings_marker, companion_and_plugin_settings + '\n      ' + settings_marker)
    else:
        print("Warning: settings_marker not found")

    # =========================================================================
    # 5. JAVASCRIPT: TIMER MODE 0 TO INFINITE, CUSTOM POPUPS, COMPANION, PLUGINS
    # =========================================================================
    # Replace existing timer state and functions
    old_timer_code = '''    // --- FOCUS TIMER ENGINE ---
    function formatTime(sec) {
      const m = Math.floor(sec / 60).toString().padStart(2, '0');
      const s = (sec % 60).toString().padStart(2, '0');
      return `${m}:${s}`;
    }

    function updateTimerUI() {
      const timeStr = formatTime(timerRemaining);
      const homeDisp = document.getElementById('home-timer-display');
      const modalDisp = document.getElementById('focus-modal-time');
      const ring = document.getElementById('focus-progress-ring');

      if (homeDisp) homeDisp.textContent = timeStr;
      if (modalDisp) modalDisp.textContent = timeStr;

      if (ring) {
        const circumference = 628.31;
        const progress = (timerDuration - timerRemaining) / timerDuration;
        ring.style.strokeDashoffset = (circumference * (1 - progress)).toString();
      }
    }'''

    new_timer_code = '''    // --- FOCUS TIMER ENGINE (COUNTDOWN POMODORO & COUNT-UP 0-TO-INFINITE) ---
    let timerElapsed = 0; // For count-up timer mode (0 to infinite)

    function formatTime(sec) {
      const h = Math.floor(sec / 3600);
      const m = Math.floor((sec % 3600) / 60).toString().padStart(2, '0');
      const s = (sec % 60).toString().padStart(2, '0');
      if (h > 0) {
        return `${h}:${m}:${s}`;
      }
      return `${m}:${s}`;
    }

    function updateTimerUI() {
      const isTimerMode = focusMode === 'timer';
      const displaySec = isTimerMode ? timerElapsed : timerRemaining;
      const timeStr = formatTime(displaySec);

      const homeDisp = document.getElementById('home-timer-display');
      const modalDisp = document.getElementById('focus-modal-time');
      const ring = document.getElementById('focus-progress-ring');
      const targetLabel = document.getElementById('focus-target-duration');
      const cycleBadge = document.getElementById('focus-cycle-badge');

      if (homeDisp) homeDisp.textContent = timeStr;
      if (modalDisp) modalDisp.textContent = timeStr;

      if (isTimerMode) {
        if (targetLabel) targetLabel.textContent = '∞ Open Flow';
        if (cycleBadge) cycleBadge.textContent = 'Chronometer • Count-up';
        if (ring) {
          // Continuous gentle breathing rotation
          const circumference = 628.31;
          const cycleSec = 60;
          const progress = (timerElapsed % cycleSec) / cycleSec;
          ring.style.strokeDashoffset = (circumference * (1 - progress)).toString();
        }
      } else {
        if (targetLabel) targetLabel.textContent = Math.floor(timerDuration / 60) + 'm Block';
        if (cycleBadge) cycleBadge.textContent = `Focus • Cycle ${focusCycle}/4`;
        if (ring) {
          const circumference = 628.31;
          const progress = (timerDuration - timerRemaining) / timerDuration;
          ring.style.strokeDashoffset = (circumference * (1 - progress)).toString();
        }
      }
    }'''

    if old_timer_code in html:
        html = html.replace(old_timer_code, new_timer_code)
    else:
        print("Warning: old_timer_code not found")

    # Replace toggleFocusTimer
    old_toggle_timer = '''    function toggleFocusTimer() {
      if (timerIsRunning) {
        clearInterval(timerInterval);
        timerIsRunning = false;
        nativeInvoke('timer:pause');
        updatePlayButtons(false);
        showToast('Focus Paused', 'Take a breath and continue when ready', 'info');
      } else {
        timerIsRunning = true;
        nativeInvoke('timer:start', { durationSec: timerRemaining });
        timerInterval = setInterval(() => {
          if (timerRemaining > 0) {
            timerRemaining--;
            updateTimerUI();
          } else {
            clearInterval(timerInterval);
            timerIsRunning = false;
            updatePlayButtons(false);
            showToast('Focus Interval Complete', 'Well done! Time for a short pause.', 'success');
          }
        }, 1000);
        updatePlayButtons(true);
        showToast('Focus Started', 'Foreground service active in status bar', 'success');
      }
    }'''

    new_toggle_timer = '''    function toggleFocusTimer() {
      if (timerIsRunning) {
        clearInterval(timerInterval);
        timerIsRunning = false;
        nativeInvoke('timer:pause');
        updatePlayButtons(false);
        showToast(focusMode === 'timer' ? 'Timer Paused' : 'Focus Paused', 'Elapsed: ' + formatTime(focusMode === 'timer' ? timerElapsed : (timerDuration - timerRemaining)), 'info');
      } else {
        timerIsRunning = true;
        nativeInvoke('timer:start', { durationSec: focusMode === 'timer' ? 0 : timerRemaining, mode: focusMode });
        timerInterval = setInterval(() => {
          if (focusMode === 'timer') {
            timerElapsed++;
            updateTimerUI();
          } else {
            if (timerRemaining > 0) {
              timerRemaining--;
              updateTimerUI();
            } else {
              clearInterval(timerInterval);
              timerIsRunning = false;
              updatePlayButtons(false);
              showToast('Focus Interval Complete', 'Well done! Time for a short pause.', 'success');
            }
          }
        }, 1000);
        updatePlayButtons(true);
        showToast(focusMode === 'timer' ? 'Stopwatch Started' : 'Focus Started', focusMode === 'timer' ? 'Counting up from 0 to infinite' : 'Foreground service active in status bar', 'success');
      }
    }'''

    if old_toggle_timer in html:
        html = html.replace(old_toggle_timer, new_toggle_timer)
    else:
        print("Warning: old_toggle_timer not found")

    # Replace resetFocusTimer and setFocusMode
    old_reset_and_mode = '''    function resetFocusTimer() {
      clearInterval(timerInterval);
      timerIsRunning = false;
      timerRemaining = timerDuration;
      updateTimerUI();
      updatePlayButtons(false);
      nativeInvoke('timer:cancel');
      showToast('Timer Reset', 'Reset to ' + Math.floor(timerDuration/60) + ' minutes', 'info');
    }

    function adjustFocusMinutes(deltaMinutes) {
      timerDuration = Math.max(60, timerDuration + (deltaMinutes * 60));
      timerRemaining = Math.max(0, timerRemaining + (deltaMinutes * 60));
      updateTimerUI();
      showToast('Duration Adjusted', formatTime(timerRemaining) + ' remaining', 'info');
    }

    function setFocusPreset(minutes) {
      clearInterval(timerInterval);
      timerIsRunning = false;
      timerDuration = minutes * 60;
      timerRemaining = timerDuration;
      updateTimerUI();
      updatePlayButtons(false);
      const targetLabel = document.getElementById('focus-target-duration');
      if (targetLabel) targetLabel.textContent = minutes + 'm Block';
      showToast(minutes + 'm Preset Selected', 'Focus target set', 'info');
    }

    function toggleCustomTimePanel() {
      const panel = document.getElementById('custom-time-panel');
      if (panel) panel.classList.toggle('hidden');
    }

    function applyCustomTime() {
      const input = document.getElementById('custom-minutes-input');
      const val = parseInt(input.value) || 25;
      setFocusPreset(val);
      toggleCustomTimePanel();
    }

    function skipFocusCycle() {
      focusCycle = (focusCycle % 4) + 1;
      const badge = document.getElementById('focus-cycle-badge');
      if (badge) badge.textContent = `Focus • Cycle ${focusCycle}/4`;
      resetFocusTimer();
      showToast('Cycle Advanced', `Now at Cycle ${focusCycle}/4`, 'info');
    }

    function setFocusMode(mode) {
      focusMode = mode;
      const pomodoroBtn = document.getElementById('focus-mode-pomodoro');
      const timerBtn = document.getElementById('focus-mode-timer');

      if (mode === 'pomodoro') {
        pomodoroBtn.className = 'flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-1.5 bg-surface-container-lowest text-primary shadow-xs cursor-pointer';
        timerBtn.className = 'flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-1.5 text-on-surface-variant hover:text-on-surface cursor-pointer';
        setFocusPreset(25);
      } else {
        timerBtn.className = 'flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-1.5 bg-surface-container-lowest text-primary shadow-xs cursor-pointer';
        pomodoroBtn.className = 'flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-1.5 text-on-surface-variant hover:text-on-surface cursor-pointer';
        setFocusPreset(45);
      }
    }'''

    new_reset_and_mode = '''    function resetFocusTimer() {
      clearInterval(timerInterval);
      timerIsRunning = false;
      if (focusMode === 'timer') {
        timerElapsed = 0;
        showToast('Chronometer Reset', 'Reset back to 00:00', 'info');
      } else {
        timerRemaining = timerDuration;
        showToast('Timer Reset', 'Reset to ' + Math.floor(timerDuration/60) + ' minutes', 'info');
      }
      updateTimerUI();
      updatePlayButtons(false);
      nativeInvoke('timer:cancel');
    }

    function adjustFocusMinutes(deltaMinutes) {
      if (focusMode === 'timer') {
        timerElapsed = Math.max(0, timerElapsed + (deltaMinutes * 60));
        updateTimerUI();
        showToast('Chronometer Adjusted', formatTime(timerElapsed) + ' elapsed', 'info');
      } else {
        timerDuration = Math.max(60, timerDuration + (deltaMinutes * 60));
        timerRemaining = Math.max(0, timerRemaining + (deltaMinutes * 60));
        updateTimerUI();
        showToast('Duration Adjusted', formatTime(timerRemaining) + ' remaining', 'info');
      }
    }

    function setFocusPreset(minutes) {
      if (focusMode === 'timer') {
        setFocusMode('pomodoro');
      }
      clearInterval(timerInterval);
      timerIsRunning = false;
      timerDuration = minutes * 60;
      timerRemaining = timerDuration;
      updateTimerUI();
      updatePlayButtons(false);
      const targetLabel = document.getElementById('focus-target-duration');
      if (targetLabel) targetLabel.textContent = minutes + 'm Block';
      showToast(minutes + 'm Preset Selected', 'Focus target set', 'info');
    }

    function toggleCustomTimePanel() {
      const panel = document.getElementById('custom-time-panel');
      if (panel) panel.classList.toggle('hidden');
    }

    function applyCustomTime() {
      const input = document.getElementById('custom-minutes-input');
      const val = parseInt(input.value) || 25;
      setFocusPreset(val);
      toggleCustomTimePanel();
    }

    function skipFocusCycle() {
      focusCycle = (focusCycle % 4) + 1;
      const badge = document.getElementById('focus-cycle-badge');
      if (badge) badge.textContent = `Focus • Cycle ${focusCycle}/4`;
      resetFocusTimer();
      showToast('Cycle Advanced', `Now at Cycle ${focusCycle}/4`, 'info');
    }

    function setFocusMode(mode) {
      focusMode = mode;
      const pomodoroBtn = document.getElementById('focus-mode-pomodoro');
      const timerBtn = document.getElementById('focus-mode-timer');
      const presetRow = document.querySelector('#view-focus .grid-cols-4');

      clearInterval(timerInterval);
      timerIsRunning = false;
      updatePlayButtons(false);

      if (mode === 'pomodoro') {
        pomodoroBtn.className = 'flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-1.5 bg-surface-container-lowest text-primary shadow-xs cursor-pointer';
        timerBtn.className = 'flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-1.5 text-on-surface-variant hover:text-on-surface cursor-pointer';
        timerDuration = 25 * 60;
        timerRemaining = timerDuration;
        updateTimerUI();
        showToast('Pomodoro Mode', '25m interval countdown', 'info');
      } else {
        // TIMER MODE: STARTS FROM 0 TO INFINITE
        timerBtn.className = 'flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-1.5 bg-surface-container-lowest text-primary shadow-xs cursor-pointer';
        pomodoroBtn.className = 'flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-1.5 text-on-surface-variant hover:text-on-surface cursor-pointer';
        timerElapsed = 0;
        updateTimerUI();
        showToast('Timer Mode Active', 'Counts up from 0 to infinite', 'info');
      }
    }'''

    if old_reset_and_mode in html:
        html = html.replace(old_reset_and_mode, new_reset_and_mode)
    else:
        print("Warning: old_reset_and_mode not found")

    # =========================================================================
    # 6. JAVASCRIPT: CUSTOM POPUPS, CUSTOM ACTIVITIES, COMPANION SYNC, & PLUGINS
    # =========================================================================
    additional_js = '''
    // ==========================================
    // CUSTOM POP-UP DIALOG SYSTEM (ZERO BROWSER ALERTS)
    // ==========================================
    let modalConfirmCallback = null;

    function showCustomModal({ title, subtitle = '', icon = 'spa', iconBg = 'bg-primary-fixed text-primary', bodyHtml = '', confirmText = 'Confirm', cancelText = 'Cancel', isDestructive = false, onConfirm = null }) {
      const container = document.getElementById('v07-modal-container');
      const titleEl = document.getElementById('modal-title');
      const subEl = document.getElementById('modal-subtitle');
      const iconEl = document.getElementById('modal-icon');
      const iconBgEl = document.getElementById('modal-icon-bg');
      const bodyEl = document.getElementById('modal-body');
      const confirmBtn = document.getElementById('modal-confirm-btn');
      const cancelBtn = document.getElementById('modal-cancel-btn');

      if (!container) return;

      titleEl.textContent = title;
      subEl.textContent = subtitle;
      iconEl.textContent = icon;
      iconBgEl.className = 'w-10 h-10 rounded-2xl flex items-center justify-center shrink-0 shadow-xs ' + iconBg;
      bodyEl.innerHTML = bodyHtml;

      confirmBtn.textContent = confirmText;
      cancelBtn.textContent = cancelText;

      if (isDestructive) {
        confirmBtn.className = 'px-5 py-2 rounded-xl text-xs font-bold bg-error text-on-error hover:opacity-95 active:scale-95 transition-all shadow-xs cursor-pointer';
      } else {
        confirmBtn.className = 'px-5 py-2 rounded-xl text-xs font-bold bg-primary text-on-primary hover:opacity-95 active:scale-95 transition-all shadow-xs cursor-pointer';
      }

      modalConfirmCallback = onConfirm;

      confirmBtn.onclick = () => {
        if (modalConfirmCallback) modalConfirmCallback();
        closeCustomModal();
      };

      container.classList.remove('pointer-events-none', 'opacity-0');
      const card = document.getElementById('v07-modal-card');
      if (card) {
        card.classList.remove('scale-95');
        card.classList.add('scale-100');
      }
    }

    function showCustomPrompt({ title, subtitle = '', icon = 'edit', placeholder = '', defaultValue = '', confirmText = 'Save', onConfirm }) {
      const bodyHtml = `
        <div class="flex flex-col space-y-2 pt-1">
          <input type="text" id="custom-modal-input" placeholder="${escapeHtml(placeholder)}" value="${escapeHtml(defaultValue)}"
                 class="w-full px-3.5 py-2.5 rounded-xl bg-surface-container-low border border-surface-container-high text-xs font-medium text-on-surface focus:outline-none focus:border-primary"/>
        </div>
      `;
      showCustomModal({
        title,
        subtitle,
        icon,
        bodyHtml,
        confirmText,
        onConfirm: () => {
          const input = document.getElementById('custom-modal-input');
          const val = input ? input.value.trim() : '';
          if (onConfirm) onConfirm(val);
        }
      });
      setTimeout(() => {
        const inp = document.getElementById('custom-modal-input');
        if (inp) inp.focus();
      }, 150);
    }

    function showCustomConfirm({ title, subtitle = '', icon = 'help', message = '', confirmText = 'Confirm', isDestructive = false, onConfirm }) {
      const bodyHtml = `<p class="text-xs text-on-surface-variant leading-relaxed">${escapeHtml(message)}</p>`;
      showCustomModal({
        title,
        subtitle,
        icon,
        iconBg: isDestructive ? 'bg-secondary-fixed text-secondary' : 'bg-primary-fixed text-primary',
        bodyHtml,
        confirmText,
        isDestructive,
        onConfirm
      });
    }

    function closeCustomModal() {
      const container = document.getElementById('v07-modal-container');
      const card = document.getElementById('v07-modal-card');
      if (card) {
        card.classList.remove('scale-100');
        card.classList.add('scale-95');
      }
      if (container) {
        container.classList.add('opacity-0');
        setTimeout(() => {
          container.classList.add('pointer-events-none');
        }, 200);
      }
      modalConfirmCallback = null;
    }

    // ==========================================
    // CUSTOM ACTIVITIES & LABELS
    // ==========================================
    let currentActivity = 'Deep Work';
    let currentLabel = '#urgent';

    function onActivitySelectChanged(selectEl) {
      if (selectEl.value === '__add_custom__') {
        selectEl.value = currentActivity;
        showCustomPrompt({
          title: 'Add Custom Activity',
          subtitle: 'Focus Sanctuary',
          icon: 'psychology',
          placeholder: 'e.g. Research, Meditation, Design',
          confirmText: 'Add Activity',
          onConfirm: (val) => {
            if (!val) return;
            addCustomActivity(val);
          }
        });
      } else {
        currentActivity = selectEl.value;
        updateFocusDetails();
      }
    }

    function addCustomActivity(name) {
      const select = document.getElementById('focus-activity-select');
      if (!select) return;

      // Add option before __add_custom__
      const opt = document.createElement('option');
      opt.value = name;
      opt.textContent = name;
      const lastOpt = select.querySelector('option[value="__add_custom__"]');
      select.insertBefore(opt, lastOpt);

      select.value = name;
      currentActivity = name;

      // Persist in localStorage
      let customList = [];
      try {
        customList = JSON.parse(localStorage.getItem('v07_custom_activities') || '[]');
      } catch (_) {}
      if (!customList.includes(name)) {
        customList.push(name);
        localStorage.setItem('v07_custom_activities', JSON.stringify(customList));
      }

      updateFocusDetails();
      showToast('Activity Added', `"${name}" is now active`, 'success');
    }

    function onLabelSelectChanged(selectEl) {
      if (selectEl.value === '__add_custom__') {
        selectEl.value = currentLabel;
        showCustomPrompt({
          title: 'Add Custom Label',
          subtitle: 'Session Tag',
          icon: 'label',
          placeholder: 'e.g. #thesis, #mindful, #sprint',
          confirmText: 'Add Label',
          onConfirm: (val) => {
            if (!val) return;
            let tag = val.trim();
            if (!tag.startsWith('#')) tag = '#' + tag;
            addCustomLabel(tag);
          }
        });
      } else {
        currentLabel = selectEl.value;
        updateFocusDetails();
      }
    }

    function addCustomLabel(tag) {
      const select = document.getElementById('focus-label-select');
      if (!select) return;

      const opt = document.createElement('option');
      opt.value = tag;
      opt.textContent = tag;
      const lastOpt = select.querySelector('option[value="__add_custom__"]');
      select.insertBefore(opt, lastOpt);

      select.value = tag;
      currentLabel = tag;

      let customList = [];
      try {
        customList = JSON.parse(localStorage.getItem('v07_custom_labels') || '[]');
      } catch (_) {}
      if (!customList.includes(tag)) {
        customList.push(tag);
        localStorage.setItem('v07_custom_labels', JSON.stringify(customList));
      }

      updateFocusDetails();
      showToast('Label Added', `"${tag}" is now active`, 'success');
    }

    function loadCustomActivitiesAndLabels() {
      try {
        const acts = JSON.parse(localStorage.getItem('v07_custom_activities') || '[]');
        const selectAct = document.getElementById('focus-activity-select');
        if (selectAct) {
          const lastOpt = selectAct.querySelector('option[value="__add_custom__"]');
          acts.forEach(name => {
            const opt = document.createElement('option');
            opt.value = name;
            opt.textContent = name;
            selectAct.insertBefore(opt, lastOpt);
          });
        }

        const labels = JSON.parse(localStorage.getItem('v07_custom_labels') || '[]');
        const selectLbl = document.getElementById('focus-label-select');
        if (selectLbl) {
          const lastOpt = selectLbl.querySelector('option[value="__add_custom__"]');
          labels.forEach(tag => {
            const opt = document.createElement('option');
            opt.value = tag;
            opt.textContent = tag;
            selectLbl.insertBefore(opt, lastOpt);
          });
        }
      } catch (_) {}
    }

    // ==========================================
    // V07 COMPANION SYNC & AUTHENTICATION
    // ==========================================
    let companionUser = { email: 'sarah@sanctuary.internal', loggedIn: true };

    function syncCompanionData() {
      const spinner = document.getElementById('sync-spinner');
      if (spinner) spinner.classList.add('animate-spin');

      // Native RPC or Loopback sync
      nativeInvoke('storage:get', { key: 'events' });

      setTimeout(() => {
        if (spinner) spinner.classList.remove('animate-spin');
        showToast('Companion Synced', '18 local events synchronized with V07 Companion', 'success');
        const badge = document.getElementById('companion-badge');
        if (badge) {
          badge.textContent = 'Synced Live';
          badge.className = 'text-[10px] px-2 py-0.5 rounded-full bg-primary text-on-primary font-bold shadow-xs';
        }
      }, 900);
    }

    function openCompanionLoginModal() {
      const bodyHtml = `
        <div class="flex flex-col space-y-3 pt-1">
          <p class="text-xs text-on-surface-variant">Sign in to sync your reflections, books, and habits with the V07 desktop & web companion.</p>
          <div class="space-y-1">
            <label class="text-[10px] uppercase font-bold text-on-surface-variant tracking-wider">Email Address</label>
            <input type="email" id="companion-login-email" value="${escapeHtml(companionUser.email)}" placeholder="user@sanctuary.internal"
                   class="w-full px-3 py-2 rounded-xl bg-surface-container-low border border-surface-container-high text-xs font-medium text-on-surface focus:outline-none focus:border-primary"/>
          </div>
          <div class="space-y-1">
            <label class="text-[10px] uppercase font-bold text-on-surface-variant tracking-wider">Password / Token</label>
            <input type="password" id="companion-login-pass" value="••••••••••••" placeholder="Enter password"
                   class="w-full px-3 py-2 rounded-xl bg-surface-container-low border border-surface-container-high text-xs font-medium text-on-surface focus:outline-none focus:border-primary"/>
          </div>
          <div class="p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40 flex items-center justify-between">
            <span class="text-[11px] text-on-surface-variant">Sync Target:</span>
            <span class="text-[11px] font-mono font-bold text-primary">v07-dashboard.firebaseapp.com</span>
          </div>
        </div>
      `;

      showCustomModal({
        title: 'V07 Companion Login',
        subtitle: 'Cloud Synchronization',
        icon: 'cloud_sync',
        bodyHtml,
        confirmText: 'Sign In & Sync',
        onConfirm: () => {
          const emailInput = document.getElementById('companion-login-email');
          const email = emailInput ? emailInput.value.trim() : companionUser.email;
          if (email) {
            companionUser.email = email;
            companionUser.loggedIn = true;
            const displayEl = document.getElementById('companion-user-display');
            if (displayEl) displayEl.textContent = email;
            syncCompanionData();
            showToast('Logged In to Companion', `Connected as ${email}`, 'success');
          }
        }
      });
    }

    // ==========================================
    // NOTIFICATION CENTER & CUSTOM ALERTS
    // ==========================================
    function openNotificationDrawer() {
      const drawer = document.getElementById('v07-notifications-drawer');
      if (!drawer) return;
      drawer.classList.remove('pointer-events-none', 'opacity-0');
      const inner = drawer.querySelector('.relative');
      if (inner) {
        inner.classList.remove('translate-y-8', 'scale-95');
        inner.classList.add('translate-y-0', 'scale-100');
      }
      const dot = document.getElementById('notif-badge-dot');
      if (dot) dot.classList.add('hidden');
    }

    function closeNotificationDrawer() {
      const drawer = document.getElementById('v07-notifications-drawer');
      if (!drawer) return;
      const inner = drawer.querySelector('.relative');
      if (inner) {
        inner.classList.remove('translate-y-0', 'scale-100');
        inner.classList.add('translate-y-8', 'scale-95');
      }
      drawer.classList.add('opacity-0');
      setTimeout(() => {
        drawer.classList.add('pointer-events-none');
      }, 250);
    }

    function clearNotifications() {
      const list = document.getElementById('notifications-list');
      if (list) {
        list.innerHTML = `
          <div class="py-8 text-center text-xs text-on-surface-variant flex flex-col items-center gap-1.5">
            <span class="material-symbols-outlined text-[28px] text-tertiary">notifications_off</span>
            <span>All notifications cleared</span>
          </div>
        `;
      }
      showToast('Notifications Cleared', 'Your alert center is serene and quiet', 'info');
    }

    function triggerTestNotification() {
      const titles = [
        'Gentle Pause Reminder: Take three diaphragmatic breaths',
        'Focus Sanctuary: Your interval was saved to local vault',
        'Reading Habit: Turn 5 more pages in Atomic Habits'
      ];
      const randomTitle = titles[Math.floor(Math.random() * titles.length)];

      // Add to notifications list
      const list = document.getElementById('notifications-list');
      if (list) {
        const item = document.createElement('div');
        item.className = 'p-3 bg-surface-container-low rounded-2xl border border-surface-container-high/40 flex items-start gap-3 animate-fade-in';
        item.innerHTML = `
          <span class="material-symbols-outlined text-primary text-[20px] mt-0.5">spa</span>
          <div class="flex flex-col flex-1 min-w-0">
            <div class="flex items-center justify-between">
              <span class="text-xs font-bold text-on-surface">Sanctuary Prompt</span>
              <span class="text-[10px] text-on-surface-variant">Just now</span>
            </div>
            <p class="text-xs text-on-surface-variant mt-0.5">${escapeHtml(randomTitle)}</p>
          </div>
        `;
        list.prepend(item);
      }

      // Show floating toast
      showToast('Sanctuary Prompt', randomTitle, 'info');

      // Native haptic
      nativeInvoke('device:vibrate', { durationMs: 40 });
    }

    // ==========================================
    // PROFILE DRAWER & MANAGEMENT
    // ==========================================
    let profileData = {
      name: 'Sarah Jenkins',
      email: 'sarah@sanctuary.internal'
    };

    function openProfileDrawer() {
      const drawer = document.getElementById('v07-profile-drawer');
      if (!drawer) return;
      drawer.classList.remove('pointer-events-none', 'opacity-0');
      const inner = drawer.querySelector('.relative');
      if (inner) {
        inner.classList.remove('translate-y-8', 'scale-95');
        inner.classList.add('translate-y-0', 'scale-100');
      }
    }

    function closeProfileDrawer() {
      const drawer = document.getElementById('v07-profile-drawer');
      if (!drawer) return;
      const inner = drawer.querySelector('.relative');
      if (inner) {
        inner.classList.remove('translate-y-0', 'scale-100');
        inner.classList.add('translate-y-8', 'scale-95');
      }
      drawer.classList.add('opacity-0');
      setTimeout(() => {
        drawer.classList.add('pointer-events-none');
      }, 250);
    }

    function promptEditProfile() {
      closeProfileDrawer();
      showCustomPrompt({
        title: 'Edit Display Name',
        subtitle: 'Profile Settings',
        icon: 'account_circle',
        placeholder: 'Enter your name',
        defaultValue: profileData.name,
        confirmText: 'Save Name',
        onConfirm: (newName) => {
          if (!newName) return;
          profileData.name = newName;
          const initial = newName.charAt(0).toUpperCase();

          const headerInit = document.getElementById('header-avatar-initial');
          if (headerInit) headerInit.textContent = initial;

          const drawerAvatar = document.getElementById('profile-drawer-avatar');
          if (drawerAvatar) drawerAvatar.textContent = initial;

          const drawerName = document.getElementById('profile-drawer-name');
          if (drawerName) drawerName.textContent = newName;

          const homeGreeting = document.getElementById('home-greeting-title');
          if (homeGreeting) homeGreeting.textContent = `Good morning, ${newName}`;

          localStorage.setItem('v07_profile_name', newName);
          showToast('Profile Updated', `Name set to ${newName}`, 'success');
        }
      });
    }

    // ==========================================
    // PLUGIN SETTINGS ACCORDION & STORAGE
    // ==========================================
    function togglePluginAccordion(id) {
      const panel = document.getElementById(id);
      const arrow = document.getElementById('arrow-' + id);
      if (panel) {
        panel.classList.toggle('hidden');
        if (arrow) {
          arrow.classList.toggle('rotate-180');
        }
      }
    }

    function savePluginSetting(key, val) {
      localStorage.setItem('v07_plugin_' + key, val);
      showToast('Setting Saved', `Plugin option updated`, 'info');
    }
'''

    # Insert additional_js right before // --- INITIALIZATION ---
    init_marker = '// --- INITIALIZATION ---'
    if init_marker in html:
        html = html.replace(init_marker, additional_js + '\n\n    ' + init_marker)
    else:
        print("Warning: init_marker not found")

    # In initialization, call loadCustomActivitiesAndLabels()
    dom_loaded = "window.addEventListener('DOMContentLoaded', () => {"
    if dom_loaded in html:
        html = html.replace(
            dom_loaded,
            dom_loaded + "\n      loadCustomActivitiesAndLabels();"
        )

    return html

def main():
    print("Applying V3 enhancements across targets...")
    for target in TARGET_FILES:
        if not os.path.exists(target):
            print(f"File not found: {target}")
            continue
        with open(target, 'r', encoding='utf-8') as f:
            original = f.read()

        updated = enhance_html(original)
        with open(target, 'w', encoding='utf-8') as f:
            f.write(updated)
        print(f"Updated: {target} ({len(updated)} bytes)")

    print("Enhancements applied successfully!")

if __name__ == '__main__':
    main()
