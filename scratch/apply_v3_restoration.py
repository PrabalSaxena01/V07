import re

def update_index_html(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        html = f.read()

    print(f"Read {file_path}: {len(html)} bytes")

    # 1. Update switchTab in JS to support 'settings' as first-class target
    old_switch_tab = """      const pages = {
        home: document.getElementById('tab-home'),
        tools: document.getElementById('tab-tools'),
        journal: document.getElementById('tab-journal'),
        day: document.getElementById('tab-day'),
        you: document.getElementById('tab-you')
      };"""

    new_switch_tab = """      const pages = {
        home: document.getElementById('tab-home'),
        tools: document.getElementById('tab-tools'),
        journal: document.getElementById('tab-journal'),
        day: document.getElementById('tab-day'),
        you: document.getElementById('tab-settings') || document.getElementById('tab-you'),
        settings: document.getElementById('tab-settings') || document.getElementById('tab-you')
      };"""
    if old_switch_tab in html:
        html = html.replace(old_switch_tab, new_switch_tab)
        print("Updated pages map in switchTab")
    else:
        print("Notice: old_switch_tab pattern not found exactly, checking regex...")

    # Update titles in switchTab
    old_titles = """      const titles = {
        home: 'Home',
        tools: 'Tools',
        journal: 'Journal',
        day: 'Day',
        you: 'You'
      };"""
    new_titles = """      const titles = {
        home: 'Home',
        tools: 'Tools',
        journal: 'Journal',
        day: 'Day',
        you: 'Settings',
        settings: 'Settings'
      };"""
    if old_titles in html:
        html = html.replace(old_titles, new_titles)
        print("Updated titles map in switchTab")

    # Update icons in switchTab
    old_icons = """      const icons = {
        home: 'spa',
        tools: 'grid_view',
        journal: 'edit_note',
        day: 'calendar_today',
        you: 'person_outline'
      };"""
    new_icons = """      const icons = {
        home: 'spa',
        tools: 'grid_view',
        journal: 'edit_note',
        day: 'calendar_today',
        you: 'settings',
        settings: 'settings'
      };"""
    if old_icons in html:
        html = html.replace(old_icons, new_icons)
        print("Updated icons map in switchTab")

    # Update bottomNav 5th item if present
    old_nav_you = """      <a class="nav-item flex flex-col items-center justify-center min-w-[56px] h-12 rounded-full text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" data-tab="you" href="javascript:void(0)" onclick="switchTab('you')">
        <span class="material-symbols-outlined text-[20px]">person_outline</span>
        <span class="font-label-sm text-label-sm">You</span>
      </a>"""
    new_nav_settings = """      <a class="nav-item flex flex-col items-center justify-center min-w-[56px] h-12 rounded-full text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer" data-tab="settings" href="javascript:void(0)" onclick="switchTab('settings')">
        <span class="material-symbols-outlined text-[20px]">settings</span>
        <span class="font-label-sm text-label-sm">Settings</span>
      </a>"""
    if old_nav_you in html:
        html = html.replace(old_nav_you, new_nav_settings)
        print("Updated bottomNav 5th item to Settings")

    return html

# Test on v3
res = update_index_html('D:/software/V07-Android/app/src/main/assets/v3/index.html')
print("Completed dry run")
