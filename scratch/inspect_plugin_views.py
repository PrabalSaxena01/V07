# -*- coding: utf-8 -*-
import re

with open('app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

m = re.search(r'<div id="tool-views-container"[\s\S]*?(?=\s*<!-- Reusable Dialogs|\s*<div id="v07-modal-container")', text)
if m:
    container = m.group(0)
    views = re.findall(r'<div[^>]+id=["\']view-([\w-]+)["\'][\s\S]*?(?=<div[^>]+id=["\']view-|\Z)', container)
    print(f"Total views in tool-views-container: {len(views)}")
    for v_id in ['focus', 'reading', 'habits', 'screentime', 'mood', 'gita', 'weekly']:
        vm = re.search(r'<div[^>]+id=["\']view-' + v_id + r'["\'][\s\S]*?(?=<div[^>]+id=["\']view-|\Z)', container)
        if vm:
            v_content = vm.group(0)[:600]
            # search for settings, customize, config in this view
            has_settings = 'setting' in v_content.lower() or 'config' in v_content.lower() or 'customize' in v_content.lower()
            header_buttons = re.findall(r'<button[^>]*>([\s\S]*?)</button>', v_content)
            print(f"\n--- View: {v_id} (has_settings_btn: {has_settings}) ---")
            for b in header_buttons[:3]:
                clean_b = re.sub(r'<[^>]+>', ' ', b).strip()
                print(f"   Button: {clean_b}")
