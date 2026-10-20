# -*- coding: utf-8 -*-
"""
apply_kit_recommendations.py
Implements the Impeccable recommendations for Adding, Removing, and Customizing plugins in Kit:
1. In-Page Plugin Customization Gear in all 7 plugin view headers.
2. Dynamic Kit Management in tab-tools with tactile cards, Manage Kit mode, and Empty State.
3. Dedicated Plugin Settings Modal with 44px touch toggles, isolated preferences, and Reset to Defaults.
4. Plugin Catalog Drawer (+ Add Tool) supporting Starter Pack plugins (Gamify, Tasks, Vitals) and Custom Plugin Import.
5. Bidirectional sync with native PlatformJsBridge (plugin:list, plugin:enable, plugin:disable, plugin:register).
"""

import os
import re

TARGET_FILES = [
    r"D:\software\V07-Android\app\src\main\assets\index.html",
    r"D:\software\V07-Android\app\src\main\assets\v3\index.html"
]

def enhance_kit(html):
    # =========================================================================
    # 1. CSS: Tactile switch styles and truncation guards
    # =========================================================================
    kit_css = '''
    /* Tactile 44px Impeccable Switch */
    .v07-switch {
      position: relative;
      display: inline-flex;
      align-items: center;
      width: 48px;
      height: 28px;
      cursor: pointer;
      user-select: none;
      -webkit-tap-highlight-color: transparent;
    }
    .v07-switch input {
      opacity: 0;
      width: 0;
      height: 0;
    }
    .v07-switch-slider {
      position: absolute;
      top: 0; left: 0; right: 0; bottom: 0;
      background-color: var(--color-surface-container-high, #e0e0e0);
      transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
      border-radius: 9999px;
      border: 1px solid rgba(0,0,0,0.06);
    }
    .v07-switch-slider:before {
      position: absolute;
      content: "";
      height: 22px;
      width: 22px;
      left: 3px;
      bottom: 2px;
      background-color: white;
      transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
      border-radius: 50%;
      box-shadow: 0 2px 4px rgba(0,0,0,0.18);
    }
    .v07-switch input:checked + .v07-switch-slider {
      background-color: var(--color-primary, #2D6A4F);
    }
    .v07-switch input:checked + .v07-switch-slider:before {
      transform: translateX(20px);
    }
    '''

    if '/* Tactile 44px Impeccable Switch */' not in html:
        style_marker = '</style>'
        html = html.replace(style_marker, kit_css + '\n  ' + style_marker, 1)

    # =========================================================================
    # 2. IN-PAGE PLUGIN VIEW HEADERS: Add settings gear [⚙️] to all 7 views
    # =========================================================================
    # 2a. Focus Sanctuary Header
    old_focus_header = '''      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Focus Sanctuary</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">Quiet intervals for deep presence</p>
          </div>
        </div>
        <button onclick="resetFocusTimer()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Reset">
          <span class="material-symbols-outlined text-[20px]">restart_alt</span>
        </button>
      </header>'''

    new_focus_header = '''      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Focus Sanctuary</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">Quiet intervals for deep presence</p>
          </div>
        </div>
        <div class="flex items-center gap-1">
          <button onclick="openPluginSettingsModal('focus')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Customize Focus Sanctuary">
            <span class="material-symbols-outlined text-[20px]">settings</span>
          </button>
          <button onclick="resetFocusTimer()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Reset">
            <span class="material-symbols-outlined text-[20px]">restart_alt</span>
          </button>
        </div>
      </header>'''

    if old_focus_header in html:
        html = html.replace(old_focus_header, new_focus_header)

    # 2b. Reading View Header
    old_reading_header = '''      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Your Books</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">Keep track of what you're reading</p>
          </div>
        </div>
        <button onclick="promptAddBook()" class="px-3 py-1.5 bg-primary text-on-primary rounded-full text-xs font-bold flex items-center gap-1 cursor-pointer hover:opacity-95 shadow-xs">
          <span class="material-symbols-outlined text-[15px]">add</span>
          <span>Add book</span>
        </button>
      </header>'''

    new_reading_header = '''      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Your Books</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">Keep track of what you're reading</p>
          </div>
        </div>
        <div class="flex items-center gap-1.5">
          <button onclick="openPluginSettingsModal('reading')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Customize Reading Companion">
            <span class="material-symbols-outlined text-[20px]">settings</span>
          </button>
          <button onclick="promptAddBook()" class="px-3 py-1.5 bg-primary text-on-primary rounded-full text-xs font-bold flex items-center gap-1 cursor-pointer hover:opacity-95 shadow-xs">
            <span class="material-symbols-outlined text-[15px]">add</span>
            <span>Add book</span>
          </button>
        </div>
      </header>'''

    if old_reading_header in html:
        html = html.replace(old_reading_header, new_reading_header)

    # 2c. Habits View Header
    old_habits_header = '''      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Habits</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">Keep track of what you do regularly</p>
          </div>
        </div>
        <button onclick="promptAddHabit()" class="w-9 h-9 rounded-full flex items-center justify-center text-primary hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Add Habit">
          <span class="material-symbols-outlined text-[22px]">add</span>
        </button>
      </header>'''

    new_habits_header = '''      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Habits</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">Keep track of what you do regularly</p>
          </div>
        </div>
        <div class="flex items-center gap-1">
          <button onclick="openPluginSettingsModal('habits')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Customize Habits">
            <span class="material-symbols-outlined text-[20px]">settings</span>
          </button>
          <button onclick="promptAddHabit()" class="w-9 h-9 rounded-full flex items-center justify-center text-primary hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Add Habit">
            <span class="material-symbols-outlined text-[22px]">add</span>
          </button>
        </div>
      </header>'''

    if old_habits_header in html:
        html = html.replace(old_habits_header, new_habits_header)

    # 2d. Screen Time View Header
    old_screentime_header = '''      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Digital Stillness</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">Telemetry & usage boundaries</p>
          </div>
        </div>
        <button onclick="refreshScreenTime()" class="w-9 h-9 rounded-full flex items-center justify-center text-primary hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Refresh">
          <span class="material-symbols-outlined text-[20px]">sync</span>
        </button>
      </header>'''

    new_screentime_header = '''      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Digital Stillness</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">Telemetry & usage boundaries</p>
          </div>
        </div>
        <div class="flex items-center gap-1">
          <button onclick="openPluginSettingsModal('screentime')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Customize Digital Stillness">
            <span class="material-symbols-outlined text-[20px]">settings</span>
          </button>
          <button onclick="refreshScreenTime()" class="w-9 h-9 rounded-full flex items-center justify-center text-primary hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Refresh">
            <span class="material-symbols-outlined text-[20px]">sync</span>
          </button>
        </div>
      </header>'''

    if old_screentime_header in html:
        html = html.replace(old_screentime_header, new_screentime_header)

    # 2e. Mood View Header
    old_mood_header = '''      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Mood & Energy</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">A quick check-in with yourself</p>
          </div>
        </div>
        <button onclick="navigateBack()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer" aria-label="Close">
          <span class="material-symbols-outlined text-[20px]">close</span>
        </button>
      </header>'''

    new_mood_header = '''      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Mood & Energy</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">A quick check-in with yourself</p>
          </div>
        </div>
        <div class="flex items-center gap-1">
          <button onclick="openPluginSettingsModal('mood')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Customize Mood & Grounding">
            <span class="material-symbols-outlined text-[20px]">settings</span>
          </button>
          <button onclick="navigateBack()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer" aria-label="Close">
            <span class="material-symbols-outlined text-[20px]">close</span>
          </button>
        </div>
      </header>'''

    if old_mood_header in html:
        html = html.replace(old_mood_header, new_mood_header)

    # 2f. Gita View Header
    old_gita_header = '''      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Gita Wisdom</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">Contemplative verses & philosophy</p>
          </div>
        </div>
        <button onclick="saveGitaVerseToJournal()" class="px-3 py-1.5 bg-primary-fixed text-primary rounded-full text-xs font-bold flex items-center gap-1 cursor-pointer active:scale-95 shadow-xs">
          <span class="material-symbols-outlined text-[15px]">bookmark</span>
          <span>Save to journal</span>
        </button>
      </header>'''

    new_gita_header = '''      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Gita Wisdom</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">Contemplative verses & philosophy</p>
          </div>
        </div>
        <div class="flex items-center gap-1.5">
          <button onclick="openPluginSettingsModal('gita')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Customize Gita Wisdom">
            <span class="material-symbols-outlined text-[20px]">settings</span>
          </button>
          <button onclick="saveGitaVerseToJournal()" class="px-3 py-1.5 bg-primary-fixed text-primary rounded-full text-xs font-bold flex items-center gap-1 cursor-pointer active:scale-95 shadow-xs">
            <span class="material-symbols-outlined text-[15px]">bookmark</span>
            <span>Save to journal</span>
          </button>
        </div>
      </header>'''

    if old_gita_header in html:
        html = html.replace(old_gita_header, new_gita_header)

    # 2g. Weekly View Header
    old_weekly_header = '''      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Weekly Digest</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">See how your week went</p>
          </div>
        </div>
        <button onclick="navigateBack()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer">
          <span class="material-symbols-outlined text-[20px]">close</span>
        </button>
      </header>'''

    new_weekly_header = '''      <header class="sticky top-0 z-10 h-16 px-4 bg-surface/95 backdrop-blur-md border-b border-surface-container-high/40 flex items-center justify-between shadow-xs">
        <div class="flex items-center gap-2.5">
          <button onclick="navigateBack()" class="w-10 h-10 rounded-full flex items-center justify-center text-on-surface hover:bg-surface-container active:scale-90 transition-transform cursor-pointer" aria-label="Back">
            <span class="material-symbols-outlined text-[24px]">arrow_back</span>
          </button>
          <div>
            <h2 class="text-base font-bold text-on-surface leading-tight">Weekly Digest</h2>
            <p class="text-[11px] text-on-surface-variant leading-none">See how your week went</p>
          </div>
        </div>
        <div class="flex items-center gap-1">
          <button onclick="openPluginSettingsModal('weekly')" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container active:scale-95 transition-all cursor-pointer" title="Customize Weekly Digest">
            <span class="material-symbols-outlined text-[20px]">settings</span>
          </button>
          <button onclick="navigateBack()" class="w-9 h-9 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-surface-container cursor-pointer">
            <span class="material-symbols-outlined text-[20px]">close</span>
          </button>
        </div>
      </header>'''

    if old_weekly_header in html:
        html = html.replace(old_weekly_header, new_weekly_header)

    # =========================================================================
    # 3. TOOL KIT TAB (tab-tools): Replace static content with dynamic Kit surface
    # =========================================================================
    kit_tab_regex = r'<div id="tab-tools" class="page-tab flex-col w-full space-y-4">[\s\S]*?(?=\s*<!-- ==========================================\s*-->\s*<!-- 3\. JOURNAL TAB)'
    
    new_kit_tab = '''<div id="tab-tools" class="page-tab flex-col w-full space-y-4">
      <!-- Kit Header with Add & Manage Actions -->
      <div class="flex items-center justify-between gap-2 pt-1">
        <div class="flex items-center gap-2">
          <h2 class="text-2xl font-bold text-on-surface tracking-tight whitespace-nowrap">Tool Kit</h2>
          <span id="kit-count-badge" class="px-2.5 py-0.5 rounded-full bg-primary-fixed text-primary text-xs font-bold shrink-0">8 active</span>
        </div>
        <div class="flex items-center gap-1.5 shrink-0">
          <button onclick="toggleManageKitMode()" id="manage-kit-btn" class="h-8 px-2.5 rounded-xl bg-surface-container-low border border-surface-container-high/60 text-xs font-bold text-on-surface flex items-center gap-1 active:scale-95 transition-all cursor-pointer shadow-2xs hover:bg-surface-container" title="Manage Kit tools and ordering">
            <span class="material-symbols-outlined text-[16px] text-primary" id="manage-kit-icon">tune</span>
            <span id="manage-kit-label">Manage</span>
          </button>
          <button onclick="openPluginCatalogDrawer()" class="h-8 px-3 rounded-xl bg-primary text-on-primary text-xs font-bold flex items-center gap-1 active:scale-95 transition-all cursor-pointer shadow-xs hover:opacity-95" title="Add tools from starter pack or external manifest">
            <span class="material-symbols-outlined text-[16px]">add</span>
            <span>Add Tool</span>
          </button>
        </div>
      </div>
      <p class="text-xs text-on-surface-variant -mt-2">Calm modules for reflection, focus, and mindful presence.</p>

      <!-- Manage Kit Info Banner (Shown only when in Manage Mode) -->
      <div id="manage-kit-banner" class="hidden p-3.5 bg-primary-fixed/30 border border-primary/20 rounded-2xl flex items-center justify-between animate-fade-in">
        <div class="flex items-center gap-2.5">
          <span class="material-symbols-outlined text-primary text-[20px]">info</span>
          <span class="text-xs font-medium text-on-surface">Toggle tools to enable or hide them from your sanctuary kit.</span>
        </div>
        <button onclick="toggleManageKitMode()" class="px-3 py-1 bg-primary text-on-primary rounded-xl text-xs font-bold active:scale-95 cursor-pointer">
          Done
        </button>
      </div>

      <!-- Dynamic Active Kit Tiles Container -->
      <div id="kit-tiles-container" class="flex flex-col space-y-3.5">
        <!-- Injected dynamically by renderKitTiles() -->
      </div>

      <!-- Inactive / Disabled Tools Section (Collapsible) -->
      <div id="kit-inactive-section" class="hidden pt-2 flex-col space-y-2.5">
        <div class="flex items-center justify-between px-1 cursor-pointer" onclick="toggleInactiveKitAccordion()">
          <div class="flex items-center gap-2">
            <span class="text-xs font-bold text-on-surface-variant uppercase tracking-wider">Inactive Companions</span>
            <span id="kit-inactive-badge" class="px-1.5 py-0.2 rounded-full bg-surface-container text-on-surface-variant text-[10px] font-bold">0</span>
          </div>
          <span class="material-symbols-outlined text-on-surface-variant text-[18px] transition-transform" id="arrow-inactive-kit">expand_more</span>
        </div>
        <div id="kit-inactive-tiles" class="hidden flex flex-col space-y-3">
          <!-- Inactive tiles injected dynamically -->
        </div>
      </div>

    </div>'''

    html = re.sub(kit_tab_regex, new_kit_tab + '\n    ', html, count=1)

    # =========================================================================
    # 4. PLUGIN CATALOG DRAWER: Slide-up modal for discovering & adding plugins
    # =========================================================================
    catalog_drawer_html = '''
  <!-- ========================================== -->
  <!-- PLUGIN CATALOG DRAWER (+ ADD TOOL)         -->
  <!-- ========================================== -->
  <div id="v07-plugin-catalog-drawer" class="fixed inset-0 z-50 pointer-events-none opacity-0 flex items-end sm:items-center justify-center p-0 sm:p-4 transition-all duration-300">
    <div class="absolute inset-0 bg-black/60 backdrop-blur-sm transition-opacity cursor-pointer" onclick="closePluginCatalogDrawer()"></div>
    <div class="relative pointer-events-auto w-full max-w-lg bg-surface-container-lowest dark:bg-surface-container-high rounded-t-3xl sm:rounded-3xl p-5 border border-surface-container-high/70 dark:border-white/10 shadow-[0_20px_50px_rgba(0,0,0,0.4)] flex flex-col gap-4 transform translate-y-8 sm:translate-y-0 sm:scale-95 transition-all duration-300 max-h-[88vh] overflow-hidden">
      
      <!-- Catalog Header -->
      <div class="flex items-center justify-between pb-3 border-b border-surface-container-high/40">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-2xl bg-primary-fixed text-primary flex items-center justify-center shrink-0 shadow-xs">
            <span class="material-symbols-outlined text-[22px]">extension</span>
          </div>
          <div class="flex flex-col">
            <h3 class="text-base font-bold text-on-surface">Plugin Sanctuary Catalog</h3>
            <p class="text-xs text-on-surface-variant">Enable starter pack tools or import custom companions</p>
          </div>
        </div>
        <button onclick="closePluginCatalogDrawer()" class="w-8 h-8 rounded-full flex items-center justify-center text-on-surface-variant hover:text-on-surface cursor-pointer">
          <span class="material-symbols-outlined text-[20px]">close</span>
        </button>
      </div>

      <!-- Filter Chips & Search -->
      <div class="flex items-center justify-between gap-2 overflow-x-auto no-scrollbar pb-1">
        <div class="flex items-center gap-1.5 shrink-0">
          <button onclick="setCatalogFilter('all')" class="cat-filter-btn px-3 py-1.5 rounded-full text-xs font-bold bg-primary text-on-primary transition-all cursor-pointer" data-filter="all">All</button>
          <button onclick="setCatalogFilter('available')" class="cat-filter-btn px-3 py-1.5 rounded-full text-xs font-bold bg-surface-container text-on-surface-variant transition-all cursor-pointer" data-filter="available">Available</button>
          <button onclick="setCatalogFilter('active')" class="cat-filter-btn px-3 py-1.5 rounded-full text-xs font-bold bg-surface-container text-on-surface-variant transition-all cursor-pointer" data-filter="active">Active in Kit</button>
        </div>
        <button onclick="promptImportCustomPlugin()" class="px-3 py-1.5 rounded-full bg-secondary-fixed text-secondary text-xs font-bold flex items-center gap-1 shrink-0 cursor-pointer active:scale-95 shadow-xs" title="Import external plugin manifest">
          <span class="material-symbols-outlined text-[15px]">file_download</span>
          <span>Import Manifest</span>
        </button>
      </div>

      <!-- Catalog List Container -->
      <div id="catalog-plugin-list" class="flex flex-col space-y-3 overflow-y-auto pr-1 flex-1 py-1 max-h-[55vh]">
        <!-- Rendered by renderPluginCatalog() -->
      </div>

    </div>
  </div>
'''

    if 'id="v07-plugin-catalog-drawer"' not in html:
        profile_drawer_marker = '<div id="v07-profile-drawer"'
        html = html.replace(profile_drawer_marker, catalog_drawer_html + '\n  ' + profile_drawer_marker, 1)

    # =========================================================================
    # 5. JAVASCRIPT: Full Plugin Management Engine
    # =========================================================================
    kit_engine_js = '''
    // =========================================================================
    // V3 PLUGIN KIT ENGINE (ADDING, REMOVING, CUSTOMIZING, LIFECYCLE)
    // =========================================================================
    let isManageKitMode = false;
    let catalogFilter = 'all';

    // Master list of Sanctuary plugins (built-in starter pack + extensible catalog)
    let sanctuaryPlugins = [
      {
        id: 'focus',
        name: 'Focus Sanctuary',
        category: 'Presence & Rhythm',
        desc: 'Quiet intervals for deep presence and unhurried work.',
        icon: 'hourglass_top',
        color: 'text-primary bg-primary-fixed',
        enabled: true,
        isBuiltIn: true,
        version: '3.0.0',
        permissions: ['storage:isolated', 'telemetry:read', 'notification'],
        settings: {
          focus_len: '25',
          focus_chime: true,
          focus_autobreak: false,
          focus_openflow: false
        }
      },
      {
        id: 'reading',
        name: 'Reading Companion',
        category: 'Literary Shelf',
        desc: 'Keep track of what you are reading with serene page logs.',
        icon: 'menu_book',
        color: 'text-secondary bg-secondary-fixed',
        enabled: true,
        isBuiltIn: true,
        version: '3.0.0',
        permissions: ['storage:isolated'],
        settings: {
          reading_target: 20,
          reading_haptics: true,
          reading_autoarchive: false
        }
      },
      {
        id: 'habits',
        name: 'Gentle Habits',
        category: 'Daily Rhythms',
        desc: 'Nurture daily rituals without pressure or broken-streak anxiety.',
        icon: 'task_alt',
        color: 'text-tertiary bg-tertiary-fixed',
        enabled: true,
        isBuiltIn: true,
        version: '3.0.0',
        permissions: ['storage:isolated', 'notification'],
        settings: {
          habits_freeze: '1',
          habits_chime: true,
          habits_evening_prompt: true
        }
      },
      {
        id: 'screentime',
        name: 'Digital Stillness',
        category: 'Telemetry & Boundaries',
        desc: 'Continuous awareness of digital attention and usage ceilings.',
        icon: 'phonelink_ring',
        color: 'text-primary bg-primary-fixed',
        enabled: true,
        isBuiltIn: true,
        version: '3.0.0',
        permissions: ['telemetry:read', 'notification'],
        settings: {
          screentime_limit: '3.5',
          screentime_nudge: true,
          screentime_blocker: false
        }
      },
      {
        id: 'mood',
        name: 'Mood & Energy',
        category: 'Sovereign Check-in',
        desc: 'A safe, calm emotional log with biometric vault privacy.',
        icon: 'psychology_alt',
        color: 'text-secondary bg-secondary-fixed',
        enabled: true,
        isBuiltIn: true,
        version: '3.0.0',
        permissions: ['storage:isolated'],
        settings: {
          mood_biometric: true,
          mood_breathing_sound: true,
          mood_morning_prompt: true
        }
      },
      {
        id: 'gita',
        name: 'Gita Wisdom',
        category: 'Wisdom & Philosophy',
        desc: 'Contemplative verses and philosophical clarity for daily life.',
        icon: 'auto_stories',
        color: 'text-tertiary bg-tertiary-fixed',
        enabled: true,
        isBuiltIn: true,
        version: '3.0.0',
        permissions: ['storage:isolated'],
        settings: {
          gita_devanagari: true,
          gita_translit: true,
          gita_morning_verse: true
        }
      },
      {
        id: 'weekly',
        name: 'Weekly Digest',
        category: 'Executive Briefing',
        desc: 'Comprehensive Sunday synthesis of your focus, reading, and habits.',
        icon: 'insights',
        color: 'text-primary bg-primary-fixed',
        enabled: true,
        isBuiltIn: true,
        version: '3.0.0',
        permissions: ['storage:isolated', 'telemetry:read'],
        settings: {
          weekly_depth: 'gentle',
          weekly_dispatch: true,
          weekly_telemetry: true
        }
      },
      // Starter Pack Extensions
      {
        id: 'gamify',
        name: 'Gamify Quests',
        category: 'Sanctuary Progression',
        desc: 'Earn mindful XP for completing rituals, protecting streaks, and leveling up.',
        icon: 'military_tech',
        color: 'text-[#4DE086] bg-[#4DE086]/15',
        enabled: false,
        isBuiltIn: true,
        version: '1.0.0',
        permissions: ['storage:isolated', 'notification'],
        settings: {
          gamify_xp_sound: true,
          gamify_streak_shield: true
        }
      },
      {
        id: 'tasks',
        name: 'Sovereign Tasks',
        category: 'Action Items',
        desc: 'Minimalist offline daily checklist and quiet focus priorities.',
        icon: 'checklist',
        color: 'text-primary bg-primary-fixed',
        enabled: false,
        isBuiltIn: true,
        version: '1.0.0',
        permissions: ['storage:isolated'],
        settings: {
          tasks_auto_clear: false,
          tasks_priority_sort: true
        }
      },
      {
        id: 'vitals',
        name: 'Body Rhythm & Vitals',
        category: 'Physical Presence',
        desc: 'Water intake tracker, posture cues, and resting cadence logs.',
        icon: 'monitor_heart',
        color: 'text-[#E06D53] bg-[#E06D53]/15',
        enabled: false,
        isBuiltIn: true,
        version: '1.0.0',
        permissions: ['storage:isolated'],
        settings: {
          vitals_water_target: '2000',
          vitals_posture_reminder: true
        }
      }
    ];

    function initPluginKit() {
      try {
        // Load custom plugins from localStorage
        const customSaved = localStorage.getItem('v07_custom_plugins');
        if (customSaved) {
          const parsed = JSON.parse(customSaved);
          if (Array.isArray(parsed)) {
            parsed.forEach(cp => {
              if (!sanctuaryPlugins.find(p => p.id === cp.id)) {
                sanctuaryPlugins.push(cp);
              }
            });
          }
        }

        // Load enabled/disabled states
        const savedStates = localStorage.getItem('v07_plugin_states');
        if (savedStates) {
          const statesMap = JSON.parse(savedStates);
          sanctuaryPlugins.forEach(p => {
            if (statesMap[p.id] !== undefined) {
              p.enabled = Boolean(statesMap[p.id]);
            }
          });
        }

        // Query native registry if available
        const nativeList = nativeInvoke('plugin:list');
        if (nativeList && nativeList.ok && nativeList.result && Array.isArray(nativeList.result.plugins)) {
          nativeList.result.plugins.forEach(np => {
            let existing = sanctuaryPlugins.find(p => p.id === np.id || ('com.v07.' + p.id) === np.id);
            if (existing) {
              if (np.state === 'DISABLED') existing.enabled = false;
              else if (np.state === 'ENABLED') existing.enabled = true;
            }
          });
        }
      } catch (e) {
        console.warn('initPluginKit load error:', e);
      }

      renderKitTiles();
    }

    function savePluginKitStates() {
      try {
        const states = {};
        sanctuaryPlugins.forEach(p => { states[p.id] = p.enabled; });
        localStorage.setItem('v07_plugin_states', JSON.stringify(states));
      } catch (e) {
        console.warn('savePluginKitStates error:', e);
      }
    }

    function getPluginStatusPreview(pluginId) {
      switch (pluginId) {
        case 'focus':
          const isTimer = focusMode === 'timer';
          return isTimer ? '∞ Open Flow • Ready' : (Math.floor(timerDuration / 60) + 'm Block • Ready');
        case 'reading':
          return 'Atomic Habits • p. 42 / 320';
        case 'habits':
          return '4 of 5 rituals met today';
        case 'screentime':
          return '1h 52m • Mindful range';
        case 'mood':
          return 'Grounded • Calm energy';
        case 'gita':
          return 'Chapter 2, Verse 47';
        case 'weekly':
          return 'Week 38 Briefing Ready';
        case 'gamify':
          return 'Level 7 • 1,420 XP';
        case 'tasks':
          return '3 pending focus priorities';
        case 'vitals':
          return '72 bpm • 1.8L hydration';
        default:
          return 'Ready in kit';
      }
    }

    function renderKitTiles() {
      const container = document.getElementById('kit-tiles-container');
      const countBadge = document.getElementById('kit-count-badge');
      const inactiveSection = document.getElementById('kit-inactive-section');
      const inactiveTiles = document.getElementById('kit-inactive-tiles');
      const inactiveBadge = document.getElementById('kit-inactive-badge');

      if (!container) return;

      const active = sanctuaryPlugins.filter(p => p.enabled);
      const inactive = sanctuaryPlugins.filter(p => !p.enabled);

      if (countBadge) countBadge.textContent = `${active.length} active`;

      // Render Active Cards
      if (active.length === 0) {
        container.innerHTML = `
          <div class="p-8 text-center bg-surface-container-lowest rounded-3xl border border-surface-container-high/40 flex flex-col items-center gap-3 animate-fade-in shadow-xs">
            <div class="w-14 h-14 rounded-2xl bg-primary-fixed text-primary flex items-center justify-center">
              <span class="material-symbols-outlined text-[30px]">spa</span>
            </div>
            <div class="flex flex-col">
              <h3 class="text-base font-bold text-on-surface">Your Kit is Serene & Empty</h3>
              <p class="text-xs text-on-surface-variant max-w-xs mt-1">No tools are currently active. Tap below to enable companions from the starter pack or import a custom tool.</p>
            </div>
            <button onclick="openPluginCatalogDrawer()" class="mt-2 px-5 py-2.5 bg-primary text-on-primary rounded-xl text-xs font-bold flex items-center gap-1.5 active:scale-95 transition-all shadow-xs cursor-pointer">
              <span class="material-symbols-outlined text-[18px]">add</span>
              <span>Explore Tool Catalog</span>
            </button>
          </div>
        `;
      } else {
        container.innerHTML = active.map(p => `
          <article class="bg-surface-container-lowest rounded-2xl p-4 shadow-sm border border-surface-container-high/40 flex flex-col gap-3 transition-all hover:border-primary/30" style="box-shadow: var(--card-shadow);" id="card-plugin-${p.id}">
            <div class="flex items-start justify-between gap-2">
              <div class="flex items-center gap-3 min-w-0 flex-1">
                <div class="w-11 h-11 rounded-xl ${p.color} flex items-center justify-center shrink-0 shadow-2xs">
                  <span class="material-symbols-outlined text-[22px]">${p.icon}</span>
                </div>
                <div class="flex flex-col min-w-0 flex-1">
                  <div class="flex items-center justify-between gap-1">
                    <h3 class="text-base font-semibold text-on-surface truncate">${escapeHtml(p.name)}</h3>
                    <span class="text-[10px] px-2 py-0.2 rounded-full bg-surface-container text-on-surface-variant font-medium shrink-0">${escapeHtml(p.category)}</span>
                  </div>
                  <p class="text-xs text-on-surface-variant mt-0.5 truncate-multi">${escapeHtml(p.desc)}</p>
                </div>
              </div>

              <!-- Manage Switch only in Manage Mode -->
              ${isManageKitMode ? `
                <div class="flex items-center shrink-0">
                  <label class="v07-switch" title="Toggle tool visibility in Kit">
                    <input type="checkbox" checked onchange="togglePluginEnabled('${p.id}', this.checked)">
                    <span class="v07-switch-slider"></span>
                  </label>
                </div>
              ` : ''}
            </div>

            <!-- Metric Preview & Primary Actions -->
            <div class="flex items-center justify-between gap-2 pt-2 border-t border-surface-container-high/30">
              <div class="flex items-center gap-1.5 min-w-0 flex-1">
                <span class="w-1.5 h-1.5 rounded-full bg-primary shrink-0"></span>
                <span class="text-[11px] font-mono font-medium text-primary truncate">${escapeHtml(getPluginStatusPreview(p.id))}</span>
              </div>
              <div class="flex items-center gap-1.5 shrink-0">
                <button onclick="openPluginSettingsModal('${p.id}')" class="w-8 h-8 rounded-xl bg-surface-container hover:bg-surface-container-high text-on-surface flex items-center justify-center active:scale-95 transition-all cursor-pointer border border-surface-container-high/40" title="Customize ${escapeHtml(p.name)}">
                  <span class="material-symbols-outlined text-[17px]">tune</span>
                </button>
                <button onclick="openPluginFullPage('${p.id}')" class="px-3.5 py-1.5 bg-primary text-on-primary rounded-xl text-xs font-bold flex items-center gap-1 active:scale-95 transition-all shadow-xs cursor-pointer">
                  <span>Open</span>
                  <span class="material-symbols-outlined text-[15px]">arrow_forward</span>
                </button>
              </div>
            </div>
          </article>
        `).join('');
      }

      // Render Inactive Section
      if (inactive.length > 0) {
        if (inactiveSection) inactiveSection.classList.remove('hidden');
        if (inactiveBadge) inactiveBadge.textContent = inactive.length;
        if (inactiveTiles) {
          inactiveTiles.innerHTML = inactive.map(p => `
            <div class="p-3 bg-surface-container-low/60 rounded-2xl border border-surface-container-high/40 flex items-center justify-between opacity-80 hover:opacity-100 transition-opacity">
              <div class="flex items-center gap-2.5">
                <div class="w-9 h-9 rounded-xl ${p.color} flex items-center justify-center shrink-0 opacity-75">
                  <span class="material-symbols-outlined text-[18px]">${p.icon}</span>
                </div>
                <div class="flex flex-col">
                  <span class="text-xs font-bold text-on-surface">${escapeHtml(p.name)}</span>
                  <span class="text-[10px] text-on-surface-variant">${escapeHtml(p.category)} • Disabled</span>
                </div>
              </div>
              <div class="flex items-center gap-2">
                ${!p.isBuiltIn ? `
                  <button onclick="uninstallCustomPlugin('${p.id}')" class="w-8 h-8 rounded-full flex items-center justify-center text-error hover:bg-error/10 cursor-pointer" title="Uninstall plugin">
                    <span class="material-symbols-outlined text-[18px]">delete</span>
                  </button>
                ` : ''}
                <button onclick="togglePluginEnabled('${p.id}', true)" class="px-3 py-1 bg-surface-container-high hover:bg-primary hover:text-on-primary text-on-surface rounded-xl text-xs font-bold transition-all cursor-pointer">
                  Enable
                </button>
              </div>
            </div>
          `).join('');
        }
      } else {
        if (inactiveSection) inactiveSection.classList.add('hidden');
      }
    }

    function toggleManageKitMode() {
      isManageKitMode = !isManageKitMode;
      const banner = document.getElementById('manage-kit-banner');
      const label = document.getElementById('manage-kit-label');
      const icon = document.getElementById('manage-kit-icon');
      const btn = document.getElementById('manage-kit-btn');

      if (banner) {
        if (isManageKitMode) banner.classList.remove('hidden');
        else banner.classList.add('hidden');
      }

      if (label) label.textContent = isManageKitMode ? 'Done' : 'Manage';
      if (icon) icon.textContent = isManageKitMode ? 'check' : 'tune';

      if (btn) {
        if (isManageKitMode) {
          btn.classList.add('bg-primary', 'text-on-primary');
          btn.classList.remove('bg-surface-container-low', 'text-on-surface');
          if (icon) icon.classList.remove('text-primary');
        } else {
          btn.classList.remove('bg-primary', 'text-on-primary');
          btn.classList.add('bg-surface-container-low', 'text-on-surface');
          if (icon) icon.classList.add('text-primary');
        }
      }

      renderKitTiles();
    }

    function toggleInactiveKitAccordion() {
      const tiles = document.getElementById('kit-inactive-tiles');
      const arrow = document.getElementById('arrow-inactive-kit');
      if (tiles) {
        tiles.classList.toggle('hidden');
        if (arrow) arrow.classList.toggle('rotate-180');
      }
    }

    function openPluginFullPage(pluginId) {
      const knownViews = ['focus', 'reading', 'habits', 'screentime', 'mood', 'gita', 'weekly'];
      if (knownViews.includes(pluginId)) {
        navigateTo(pluginId);
      } else {
        // Starter pack extension or custom plugin page
        showCustomModal({
          title: sanctuaryPlugins.find(p => p.id === pluginId)?.name || 'Plugin Sandbox',
          subtitle: 'Plugin Viewport',
          icon: 'extension',
          bodyHtml: `
            <div class="py-4 text-center flex flex-col items-center gap-2">
              <span class="material-symbols-outlined text-[36px] text-primary">hourglass_empty</span>
              <p class="text-xs text-on-surface-variant">The full sandboxed iframe viewport for <b>${escapeHtml(pluginId)}</b> is ready to run inside the V3 platform host.</p>
            </div>
          `,
          confirmText: 'Back to Kit',
          onConfirm: () => {}
        });
      }
    }

    function togglePluginEnabled(pluginId, enableState) {
      const plugin = sanctuaryPlugins.find(p => p.id === pluginId);
      if (!plugin) return;

      plugin.enabled = (enableState !== undefined) ? enableState : !plugin.enabled;
      savePluginKitStates();

      // RPC sync with native bridge
      if (plugin.enabled) {
        nativeInvoke('plugin:enable', { id: plugin.id });
        showToast('Tool Activated', `${plugin.name} is now active in your Kit`, 'success');
      } else {
        nativeInvoke('plugin:disable', { id: plugin.id });
        showToast('Tool Hidden', `${plugin.name} moved to inactive companions`, 'info');
      }

      renderKitTiles();
      if (typeof renderPluginCatalog === 'function') renderPluginCatalog();
    }

    // =========================================================================
    // DEDICATED CONTEXTUAL PLUGIN SETTINGS MODAL
    // =========================================================================
    function openPluginSettingsModal(pluginId) {
      const p = sanctuaryPlugins.find(item => item.id === pluginId);
      if (!p) return;

      let settingsHtml = '';

      switch (pluginId) {
        case 'focus':
          const currentLen = localStorage.getItem('v07_plugin_focus_len') || '25';
          const currentChime = localStorage.getItem('v07_plugin_focus_chime') !== 'false';
          const currentBreak = localStorage.getItem('v07_plugin_focus_autobreak') === 'true';
          const currentOpenFlow = localStorage.getItem('v07_plugin_focus_openflow') === 'true';

          settingsHtml = `
            <div class="flex flex-col space-y-3 pt-1">
              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Default Duration</span>
                  <span class="text-[10px] text-on-surface-variant">Standard countdown block size</span>
                </div>
                <select id="modal-cfg-focus-len" class="h-8 px-2 bg-surface-container rounded-lg text-xs font-bold border border-surface-container-high text-on-surface">
                  <option value="15" ${currentLen === '15' ? 'selected' : ''}>15 mins</option>
                  <option value="25" ${currentLen === '25' ? 'selected' : ''}>25 mins</option>
                  <option value="45" ${currentLen === '45' ? 'selected' : ''}>45 mins</option>
                  <option value="60" ${currentLen === '60' ? 'selected' : ''}>60 mins</option>
                </select>
              </div>

              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Tibetan Singing Bowl</span>
                  <span class="text-[10px] text-on-surface-variant">Soft resonance on start and completion</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-focus-chime" ${currentChime ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>

              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Auto-start Breaks</span>
                  <span class="text-[10px] text-on-surface-variant">Begin 5m pause immediately</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-focus-break" ${currentBreak ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>

              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Default to Open Flow</span>
                  <span class="text-[10px] text-on-surface-variant">Open timer from 0 to infinite</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-focus-openflow" ${currentOpenFlow ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>
            </div>
          `;
          break;

        case 'reading':
          const readTarget = localStorage.getItem('v07_plugin_reading_target') || '20';
          const readHaptics = localStorage.getItem('v07_plugin_reading_haptics') !== 'false';

          settingsHtml = `
            <div class="flex flex-col space-y-3 pt-1">
              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Daily Reading Goal</span>
                  <span class="text-[10px] text-on-surface-variant">Pages to turn each day</span>
                </div>
                <div class="flex items-center gap-1.5">
                  <input type="number" id="modal-cfg-reading-target" value="${readTarget}" min="5" max="150"
                         class="w-14 h-8 px-1 text-center bg-surface-container rounded-lg text-xs font-bold border border-surface-container-high text-on-surface"/>
                  <span class="text-xs text-on-surface-variant">pg</span>
                </div>
              </div>

              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Haptic Feedback</span>
                  <span class="text-[10px] text-on-surface-variant">Tactile micro-tick on page logs</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-reading-haptics" ${readHaptics ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>
            </div>
          `;
          break;

        case 'habits':
          const habitsFreeze = localStorage.getItem('v07_plugin_habits_freeze') || '1';
          const habitsChime = localStorage.getItem('v07_plugin_habits_chime') !== 'false';

          settingsHtml = `
            <div class="flex flex-col space-y-3 pt-1">
              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Weekly Streak Freeze</span>
                  <span class="text-[10px] text-on-surface-variant">Grace days without breaking streak</span>
                </div>
                <select id="modal-cfg-habits-freeze" class="h-8 px-2 bg-surface-container rounded-lg text-xs font-bold border border-surface-container-high text-on-surface">
                  <option value="1" ${habitsFreeze === '1' ? 'selected' : ''}>1 Day / Wk</option>
                  <option value="2" ${habitsFreeze === '2' ? 'selected' : ''}>2 Days / Wk</option>
                  <option value="0" ${habitsFreeze === '0' ? 'selected' : ''}>Disabled</option>
                </select>
              </div>

              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Celebration Chime</span>
                  <span class="text-[10px] text-on-surface-variant">Play gentle tone upon completing all rituals</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-habits-chime" ${habitsChime ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>
            </div>
          `;
          break;

        case 'screentime':
          const stLimit = localStorage.getItem('v07_plugin_screentime_limit') || '3.5';
          const stNudge = localStorage.getItem('v07_plugin_screentime_nudge') !== 'false';

          settingsHtml = `
            <div class="flex flex-col space-y-3 pt-1">
              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Daily Screen Ceiling</span>
                  <span class="text-[10px] text-on-surface-variant">Mindful daily allotment</span>
                </div>
                <select id="modal-cfg-screentime-limit" class="h-8 px-2 bg-surface-container rounded-lg text-xs font-bold border border-surface-container-high text-on-surface">
                  <option value="2.5" ${stLimit === '2.5' ? 'selected' : ''}>2h 30m</option>
                  <option value="3.0" ${stLimit === '3.0' ? 'selected' : ''}>3h 00m</option>
                  <option value="3.5" ${stLimit === '3.5' ? 'selected' : ''}>3h 30m</option>
                  <option value="4.0" ${stLimit === '4.0' ? 'selected' : ''}>4h 00m</option>
                </select>
              </div>

              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">80% Gentle Nudge</span>
                  <span class="text-[10px] text-on-surface-variant">Send quiet notification before ceiling</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-screentime-nudge" ${stNudge ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>
            </div>
          `;
          break;

        case 'mood':
          const moodBio = localStorage.getItem('v07_plugin_mood_biometric') !== 'false';
          const moodSound = localStorage.getItem('v07_plugin_mood_breathing_sound') !== 'false';

          settingsHtml = `
            <div class="flex flex-col space-y-3 pt-1">
              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Biometric Vault Encryption</span>
                  <span class="text-[10px] text-on-surface-variant">Lock panic & vulnerability entries</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-mood-bio" ${moodBio ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>

              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">4-7-8 Breathing Guide Audio</span>
                  <span class="text-[10px] text-on-surface-variant">Calming ocean breath guidance</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-mood-sound" ${moodSound ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>
            </div>
          `;
          break;

        case 'gita':
          const gitaDev = localStorage.getItem('v07_plugin_gita_devanagari') !== 'false';
          const gitaTrans = localStorage.getItem('v07_plugin_gita_translit') !== 'false';

          settingsHtml = `
            <div class="flex flex-col space-y-3 pt-1">
              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Sanskrit Devanagari</span>
                  <span class="text-[10px] text-on-surface-variant">Display original sacred glyphs</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-gita-dev" ${gitaDev ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>

              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Roman Transliteration</span>
                  <span class="text-[10px] text-on-surface-variant">Phonetic English pronunciation</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-gita-trans" ${gitaTrans ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>
            </div>
          `;
          break;

        case 'weekly':
          const wkDepth = localStorage.getItem('v07_plugin_weekly_depth') || 'gentle';
          const wkDispatch = localStorage.getItem('v07_plugin_weekly_dispatch') !== 'false';

          settingsHtml = `
            <div class="flex flex-col space-y-3 pt-1">
              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Synthesis Depth</span>
                  <span class="text-[10px] text-on-surface-variant">Complexity of Sunday summary</span>
                </div>
                <select id="modal-cfg-weekly-depth" class="h-8 px-2 bg-surface-container rounded-lg text-xs font-bold border border-surface-container-high text-on-surface">
                  <option value="gentle" ${wkDepth === 'gentle' ? 'selected' : ''}>Gentle Overview</option>
                  <option value="deep" ${wkDepth === 'deep' ? 'selected' : ''}>Deep Contemplation</option>
                </select>
              </div>

              <div class="flex items-center justify-between p-2.5 bg-surface-container-low rounded-xl border border-surface-container-high/40">
                <div class="flex flex-col">
                  <span class="text-xs font-semibold text-on-surface">Sunday 8:00 PM Dispatch</span>
                  <span class="text-[10px] text-on-surface-variant">Automated prompt when briefing is ready</span>
                </div>
                <label class="v07-switch">
                  <input type="checkbox" id="modal-cfg-weekly-dispatch" ${wkDispatch ? 'checked' : ''}>
                  <span class="v07-switch-slider"></span>
                </label>
              </div>
            </div>
          `;
          break;

        default:
          settingsHtml = `
            <div class="p-3 bg-surface-container-low rounded-xl text-xs text-on-surface-variant text-center">
              Standard local sandbox preferences are active for this plugin.
            </div>
          `;
          break;
      }

      const modalBody = `
        <div class="flex flex-col space-y-3">
          <div class="flex items-center justify-between pb-2 border-b border-surface-container-high/40">
            <span class="text-[10px] text-primary font-bold uppercase tracking-wider">Version ${p.version} • ${p.category}</span>
            <button onclick="togglePluginEnabled('${p.id}', false); closeCustomModal();" class="text-[11px] text-error font-semibold hover:underline cursor-pointer flex items-center gap-1">
              <span class="material-symbols-outlined text-[13px]">visibility_off</span>
              <span>Hide from Kit</span>
            </button>
          </div>

          ${settingsHtml}

          <div class="pt-2 flex items-center justify-between border-t border-surface-container-high/40">
            <button onclick="resetPluginSettings('${p.id}')" class="text-xs font-semibold text-on-surface-variant hover:text-on-surface cursor-pointer flex items-center gap-1">
              <span class="material-symbols-outlined text-[15px]">restart_alt</span>
              <span>Reset Defaults</span>
            </button>
            <span class="text-[10px] text-on-surface-variant font-mono">ID: ${escapeHtml(p.id)}</span>
          </div>
        </div>
      `;

      showCustomModal({
        title: `${p.name} Settings`,
        subtitle: 'Plugin preferences',
        icon: p.icon,
        bodyHtml: modalBody,
        confirmText: 'Save Settings',
        onConfirm: () => {
          savePluginSettingsFromModal(p.id);
        }
      });
    }

    function savePluginSettingsFromModal(pluginId) {
      try {
        switch (pluginId) {
          case 'focus':
            const fLen = document.getElementById('modal-cfg-focus-len')?.value;
            const fChime = document.getElementById('modal-cfg-focus-chime')?.checked;
            const fBreak = document.getElementById('modal-cfg-focus-break')?.checked;
            const fOpen = document.getElementById('modal-cfg-focus-openflow')?.checked;
            if (fLen) localStorage.setItem('v07_plugin_focus_len', fLen);
            if (fChime !== undefined) localStorage.setItem('v07_plugin_focus_chime', fChime);
            if (fBreak !== undefined) localStorage.setItem('v07_plugin_focus_autobreak', fBreak);
            if (fOpen !== undefined) localStorage.setItem('v07_plugin_focus_openflow', fOpen);
            break;

          case 'reading':
            const rTarget = document.getElementById('modal-cfg-reading-target')?.value;
            const rHaptics = document.getElementById('modal-cfg-reading-haptics')?.checked;
            if (rTarget) localStorage.setItem('v07_plugin_reading_target', rTarget);
            if (rHaptics !== undefined) localStorage.setItem('v07_plugin_reading_haptics', rHaptics);
            break;

          case 'habits':
            const hFreeze = document.getElementById('modal-cfg-habits-freeze')?.value;
            const hChime = document.getElementById('modal-cfg-habits-chime')?.checked;
            if (hFreeze) localStorage.setItem('v07_plugin_habits_freeze', hFreeze);
            if (hChime !== undefined) localStorage.setItem('v07_plugin_habits_chime', hChime);
            break;

          case 'screentime':
            const stLimit = document.getElementById('modal-cfg-screentime-limit')?.value;
            const stNudge = document.getElementById('modal-cfg-screentime-nudge')?.checked;
            if (stLimit) localStorage.setItem('v07_plugin_screentime_limit', stLimit);
            if (stNudge !== undefined) localStorage.setItem('v07_plugin_screentime_nudge', stNudge);
            break;

          case 'mood':
            const mBio = document.getElementById('modal-cfg-mood-bio')?.checked;
            const mSound = document.getElementById('modal-cfg-mood-sound')?.checked;
            if (mBio !== undefined) localStorage.setItem('v07_plugin_mood_biometric', mBio);
            if (mSound !== undefined) localStorage.setItem('v07_plugin_mood_breathing_sound', mSound);
            break;

          case 'gita':
            const gDev = document.getElementById('modal-cfg-gita-dev')?.checked;
            const gTrans = document.getElementById('modal-cfg-gita-trans')?.checked;
            if (gDev !== undefined) localStorage.setItem('v07_plugin_gita_devanagari', gDev);
            if (gTrans !== undefined) localStorage.setItem('v07_plugin_gita_translit', gTrans);
            break;

          case 'weekly':
            const wDepth = document.getElementById('modal-cfg-weekly-depth')?.value;
            const wDispatch = document.getElementById('modal-cfg-weekly-dispatch')?.checked;
            if (wDepth) localStorage.setItem('v07_plugin_weekly_depth', wDepth);
            if (wDispatch !== undefined) localStorage.setItem('v07_plugin_weekly_dispatch', wDispatch);
            break;
        }

        // Native isolated storage save
        nativeInvoke('storage:set', { key: `config_${pluginId}`, value: JSON.stringify({ updated: Date.now() }) });
        showToast('Settings Saved', `${pluginId} preferences updated`, 'success');
        renderKitTiles();
      } catch (e) {
        console.warn('savePluginSettingsFromModal error:', e);
      }
    }

    function resetPluginSettings(pluginId) {
      const keys = [
        `v07_plugin_${pluginId}_len`,
        `v07_plugin_${pluginId}_chime`,
        `v07_plugin_${pluginId}_autobreak`,
        `v07_plugin_${pluginId}_target`,
        `v07_plugin_${pluginId}_haptics`,
        `v07_plugin_${pluginId}_freeze`,
        `v07_plugin_${pluginId}_limit`,
        `v07_plugin_${pluginId}_nudge`,
        `v07_plugin_${pluginId}_biometric`,
        `v07_plugin_${pluginId}_breathing_sound`,
        `v07_plugin_${pluginId}_devanagari`,
        `v07_plugin_${pluginId}_translit`,
        `v07_plugin_${pluginId}_depth`,
        `v07_plugin_${pluginId}_dispatch`
      ];
      keys.forEach(k => localStorage.removeItem(k));
      closeCustomModal();
      showToast('Settings Reset', `${pluginId} options restored to factory defaults`, 'info');
      renderKitTiles();
    }

    // =========================================================================
    // PLUGIN CATALOG DRAWER (+ ADD TOOL TO KIT)
    // =========================================================================
    function openPluginCatalogDrawer() {
      const drawer = document.getElementById('v07-plugin-catalog-drawer');
      if (!drawer) return;
      drawer.classList.remove('pointer-events-none', 'opacity-0');
      const inner = drawer.querySelector('.relative');
      if (inner) {
        inner.classList.remove('translate-y-8', 'scale-95');
        inner.classList.add('translate-y-0', 'scale-100');
      }
      renderPluginCatalog();
    }

    function closePluginCatalogDrawer() {
      const drawer = document.getElementById('v07-plugin-catalog-drawer');
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

    function setCatalogFilter(filter) {
      catalogFilter = filter;
      document.querySelectorAll('.cat-filter-btn').forEach(btn => {
        if (btn.getAttribute('data-filter') === filter) {
          btn.className = 'cat-filter-btn px-3 py-1.5 rounded-full text-xs font-bold bg-primary text-on-primary transition-all cursor-pointer';
        } else {
          btn.className = 'cat-filter-btn px-3 py-1.5 rounded-full text-xs font-bold bg-surface-container text-on-surface-variant transition-all cursor-pointer';
        }
      });
      renderPluginCatalog();
    }

    function renderPluginCatalog() {
      const container = document.getElementById('catalog-plugin-list');
      if (!container) return;

      let list = sanctuaryPlugins;
      if (catalogFilter === 'available') {
        list = sanctuaryPlugins.filter(p => !p.enabled);
      } else if (catalogFilter === 'active') {
        list = sanctuaryPlugins.filter(p => p.enabled);
      }

      if (list.length === 0) {
        container.innerHTML = `
          <div class="py-8 text-center text-xs text-on-surface-variant flex flex-col items-center gap-1.5">
            <span class="material-symbols-outlined text-[26px]">category</span>
            <span>No tools match this filter.</span>
          </div>
        `;
        return;
      }

      container.innerHTML = list.map(p => `
        <div class="p-3.5 bg-surface-container-low rounded-2xl border border-surface-container-high/40 flex flex-col gap-2.5">
          <div class="flex items-start justify-between">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-xl ${p.color} flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[20px]">${p.icon}</span>
              </div>
              <div class="flex flex-col">
                <div class="flex items-center gap-1.5">
                  <h4 class="text-xs font-bold text-on-surface">${escapeHtml(p.name)}</h4>
                  <span class="text-[9px] px-1.5 py-0.2 rounded-full bg-surface-container text-on-surface-variant font-mono">v${p.version}</span>
                </div>
                <span class="text-[10px] text-primary font-medium">${escapeHtml(p.category)}</span>
              </div>
            </div>

            <!-- Add / Active Status Action -->
            <div>
              ${p.enabled ? `
                <span class="px-2.5 py-1 rounded-full bg-primary-fixed text-primary text-[10px] font-bold flex items-center gap-1">
                  <span class="material-symbols-outlined text-[13px]">check</span>
                  <span>In Kit</span>
                </span>
              ` : `
                <button onclick="togglePluginEnabled('${p.id}', true); renderPluginCatalog();" class="px-3.5 py-1.5 bg-primary text-on-primary rounded-xl text-xs font-bold flex items-center gap-1 active:scale-95 transition-all shadow-xs cursor-pointer hover:opacity-95">
                  <span class="material-symbols-outlined text-[14px]">add</span>
                  <span>Add to Kit</span>
                </button>
              `}
            </div>
          </div>

          <p class="text-xs text-on-surface-variant leading-relaxed">${escapeHtml(p.desc)}</p>

          <!-- Permissions pill badges -->
          <div class="flex items-center gap-1.5 flex-wrap pt-1 border-t border-surface-container-high/30">
            <span class="text-[9px] uppercase font-bold text-on-surface-variant tracking-wider">Permissions:</span>
            ${(p.permissions || []).map(perm => `
              <span class="text-[9px] px-1.5 py-0.2 rounded-md bg-surface-container text-on-surface-variant font-mono">${escapeHtml(perm)}</span>
            `).join('')}
          </div>
        </div>
      `).join('');
    }

    // =========================================================================
    // IMPORT CUSTOM PLUGIN MANIFEST FLOW
    // =========================================================================
    function promptImportCustomPlugin() {
      const bodyHtml = `
        <div class="flex flex-col space-y-3 pt-1">
          <p class="text-xs text-on-surface-variant">Sideload an external V3 plugin by entering its manifest specifications or JSON definition.</p>
          
          <div class="space-y-1">
            <label class="text-[10px] uppercase font-bold text-on-surface-variant tracking-wider">Plugin Name</label>
            <input type="text" id="import-plugin-name" placeholder="e.g. Water Sanctuary"
                   class="w-full px-3 py-2 rounded-xl bg-surface-container-low border border-surface-container-high text-xs font-medium text-on-surface focus:outline-none focus:border-primary"/>
          </div>

          <div class="space-y-1">
            <label class="text-[10px] uppercase font-bold text-on-surface-variant tracking-wider">Plugin ID</label>
            <input type="text" id="import-plugin-id" placeholder="e.g. com.sanctuary.water"
                   class="w-full px-3 py-2 rounded-xl bg-surface-container-low border border-surface-container-high text-xs font-mono text-on-surface focus:outline-none focus:border-primary"/>
          </div>

          <div class="grid grid-cols-2 gap-2">
            <div class="space-y-1">
              <label class="text-[10px] uppercase font-bold text-on-surface-variant tracking-wider">Material Icon</label>
              <input type="text" id="import-plugin-icon" placeholder="e.g. water_drop" value="extension"
                     class="w-full px-3 py-2 rounded-xl bg-surface-container-low border border-surface-container-high text-xs font-medium text-on-surface focus:outline-none focus:border-primary"/>
            </div>
            <div class="space-y-1">
              <label class="text-[10px] uppercase font-bold text-on-surface-variant tracking-wider">Category</label>
              <input type="text" id="import-plugin-category" placeholder="e.g. Wellness" value="Custom Companion"
                     class="w-full px-3 py-2 rounded-xl bg-surface-container-low border border-surface-container-high text-xs font-medium text-on-surface focus:outline-none focus:border-primary"/>
            </div>
          </div>

          <div class="space-y-1">
            <label class="text-[10px] uppercase font-bold text-on-surface-variant tracking-wider">Short Description</label>
            <input type="text" id="import-plugin-desc" placeholder="Brief explanation of this tool"
                   class="w-full px-3 py-2 rounded-xl bg-surface-container-low border border-surface-container-high text-xs font-medium text-on-surface focus:outline-none focus:border-primary"/>
          </div>
        </div>
      `;

      showCustomModal({
        title: 'Import Custom Plugin',
        subtitle: 'External Manifest',
        icon: 'file_download',
        bodyHtml,
        confirmText: 'Register Plugin',
        onConfirm: () => {
          const name = document.getElementById('import-plugin-name')?.value.trim();
          let id = document.getElementById('import-plugin-id')?.value.trim();
          const icon = document.getElementById('import-plugin-icon')?.value.trim() || 'extension';
          const category = document.getElementById('import-plugin-category')?.value.trim() || 'Custom Companion';
          const desc = document.getElementById('import-plugin-desc')?.value.trim() || 'Custom imported sanctuary tool.';

          if (!name) {
            showToast('Import Error', 'Plugin name is required', 'error');
            return;
          }
          if (!id) {
            id = 'custom.' + name.toLowerCase().replace(/[^a-z0-9]/g, '');
          }

          const newPlugin = {
            id,
            name,
            category,
            desc,
            icon,
            color: 'text-primary bg-primary-fixed',
            enabled: true,
            isBuiltIn: false,
            version: '1.0.0',
            permissions: ['storage:isolated'],
            settings: {}
          };

          sanctuaryPlugins.push(newPlugin);

          // Save custom plugins to localStorage
          try {
            const customs = sanctuaryPlugins.filter(p => !p.isBuiltIn);
            localStorage.setItem('v07_custom_plugins', JSON.stringify(customs));
          } catch (e) {}

          savePluginKitStates();

          // RPC register with native Kotlin registry
          const manifestJson = JSON.stringify({
            id: newPlugin.id,
            name: newPlugin.name,
            version: newPlugin.version,
            description: newPlugin.desc,
            permissions: newPlugin.permissions,
            navIcon: newPlugin.icon
          });
          nativeInvoke('plugin:register', { manifest: manifestJson });

          showToast('Plugin Registered', `${name} added to your Kit`, 'success');
          renderKitTiles();
          renderPluginCatalog();
        }
      });
    }

    function uninstallCustomPlugin(pluginId) {
      const idx = sanctuaryPlugins.findIndex(p => p.id === pluginId);
      if (idx === -1) return;
      const name = sanctuaryPlugins[idx].name;

      showCustomModal({
        title: 'Uninstall Plugin?',
        subtitle: 'Confirm Removal',
        icon: 'delete',
        bodyHtml: `<p class="text-xs text-on-surface-variant">Are you sure you want to remove <b>${escapeHtml(name)}</b>? This will purge its local state from your sanctuary kit.</p>`,
        confirmText: 'Uninstall',
        onConfirm: () => {
          sanctuaryPlugins.splice(idx, 1);
          try {
            const customs = sanctuaryPlugins.filter(p => !p.isBuiltIn);
            localStorage.setItem('v07_custom_plugins', JSON.stringify(customs));
          } catch (e) {}
          savePluginKitStates();
          nativeInvoke('plugin:unregister', { id: pluginId });
          showToast('Plugin Uninstalled', `${name} removed from your Kit`, 'info');
          renderKitTiles();
          renderPluginCatalog();
        }
      });
    }
'''

    # Insert kit_engine_js before // --- INITIALIZATION ---
    init_marker = '// --- INITIALIZATION ---'
    if 'V3 PLUGIN KIT ENGINE' not in html:
        if init_marker in html:
            html = html.replace(init_marker, kit_engine_js + '\n\n    ' + init_marker, 1)

    # In window DOMContentLoaded, call initPluginKit()
    if 'initPluginKit();' not in html:
        dom_loaded_marker = "window.addEventListener('DOMContentLoaded', () => {"
        if dom_loaded_marker in html:
            html = html.replace(
                dom_loaded_marker,
                dom_loaded_marker + "\n      initPluginKit();"
            )

    return html

def main():
    print("Applying Impeccable Kit Enhancements across targets...")
    for target in TARGET_FILES:
        if not os.path.exists(target):
            print(f"Target not found: {target}")
            continue
        with open(target, 'r', encoding='utf-8') as f:
            content = f.read()

        updated = enhance_kit(content)
        with open(target, 'w', encoding='utf-8') as f:
            f.write(updated)
        print(f"Updated: {target} ({len(updated)} bytes)")

    print("Impeccable Kit Enhancements successfully written!")

if __name__ == '__main__':
    main()
