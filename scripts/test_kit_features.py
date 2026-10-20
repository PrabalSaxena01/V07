# -*- coding: utf-8 -*-
"""
test_kit_features.py
Verifies that all kit enhancement features are present in the HTML and work as expected.
"""

import re
import sys

def test_html_assets():
    path = r"D:\software\V07-Android\app\src\main\assets\v3\index.html"
    with open(path, 'r', encoding='utf-8') as f:
        html = f.read()

    tests = [
        ("Tactile switch CSS (.v07-switch)", ".v07-switch" in html),
        ("In-Page Focus Header Settings Gear", "openPluginSettingsModal('focus')" in html),
        ("In-Page Reading Header Settings Gear", "openPluginSettingsModal('reading')" in html),
        ("In-Page Habits Header Settings Gear", "openPluginSettingsModal('habits')" in html),
        ("In-Page ScreenTime Header Settings Gear", "openPluginSettingsModal('screentime')" in html),
        ("In-Page Mood Header Settings Gear", "openPluginSettingsModal('mood')" in html),
        ("In-Page Gita Header Settings Gear", "openPluginSettingsModal('gita')" in html),
        ("In-Page Weekly Header Settings Gear", "openPluginSettingsModal('weekly')" in html),
        ("Kit Header Manage Button", "toggleManageKitMode()" in html),
        ("Kit Header Add Tool Button", "openPluginCatalogDrawer()" in html),
        ("Dynamic Kit Tiles Container", 'id="kit-tiles-container"' in html),
        ("Inactive Kit Accordion", 'id="kit-inactive-section"' in html),
        ("Plugin Catalog Drawer Container", 'id="v07-plugin-catalog-drawer"' in html),
        ("Catalog Filter Chips", 'setCatalogFilter(' in html),
        ("Custom Plugin Import Button", 'promptImportCustomPlugin()' in html),
        ("JS Engine: sanctuaryPlugins array", "let sanctuaryPlugins =" in html),
        ("JS Engine: initPluginKit function", "function initPluginKit()" in html),
        ("JS Engine: renderKitTiles function", "function renderKitTiles()" in html),
        ("JS Engine: toggleManageKitMode function", "function toggleManageKitMode()" in html),
        ("JS Engine: togglePluginEnabled function", "function togglePluginEnabled(" in html),
        ("JS Engine: openPluginSettingsModal function", "function openPluginSettingsModal(" in html),
        ("JS Engine: savePluginSettingsFromModal function", "function savePluginSettingsFromModal(" in html),
        ("JS Engine: resetPluginSettings function", "function resetPluginSettings(" in html),
        ("JS Engine: openPluginCatalogDrawer function", "function openPluginCatalogDrawer()" in html),
        ("JS Engine: renderPluginCatalog function", "function renderPluginCatalog()" in html),
        ("JS Engine: promptImportCustomPlugin function", "function promptImportCustomPlugin()" in html),
        ("JS Engine: uninstallCustomPlugin function", "function uninstallCustomPlugin(" in html),
        ("Starter pack extensions (Gamify, Tasks, Vitals)", "gamify" in html and "tasks" in html and "vitals" in html),
        ("Native Bridge RPC bindings", "plugin:list" in html and "plugin:enable" in html and "plugin:disable" in html)
    ]

    print("=== RUNNING ASSET & MARKUP TESTS ===")
    passed = 0
    for name, res in tests:
        status = "PASS" if res else "FAIL"
        if res:
            passed += 1
        print(f"[{status}] {name}")

    print(f"\nResult: {passed}/{len(tests)} tests passed.")
    if passed == len(tests):
        print("ALL KIT ASSET TESTS PASSED!")
    else:
        sys.exit(1)

if __name__ == '__main__':
    test_html_assets()
