import re

ROOT_PATH = 'D:/software/V07-Android/app/src/main/assets/index.html'
with open(ROOT_PATH, 'r', encoding='utf-8') as f:
    html = f.read()

checks = [
    ("modal-plugin-host in DOM", '<div id="modal-plugin-host"' in html),
    ("plugin-viewport-iframe in DOM", '<iframe id="plugin-viewport-iframe"' in html),
    ("tab-tools in DOM", '<div id="tab-tools"' in html),
    ("plugin-search-input in DOM", 'id="plugin-search-input"' in html),
    ("btn-toggle-manage in DOM", 'id="btn-toggle-manage"' in html),
    ("filterPluginsCategory chips in DOM", 'filterPluginsCategory' in html),
    ("10 plugin cards present", html.count('class="plugin-item-card') == 10),
    ("7 core engines present", 'Core 1: Focus Sanctuary' in html and 'Core 7: Recycle Bin' in html),
    ("handleBackPress defined", 'window.handleBackPress = function()' in html),
    ("openPlugin updated", 'iframe.src = `/plugins/${pluginId}/index.html`' in html),
    ("No hardcoded muddy shadow", 'shadow-[0_24px_48px_rgba(21,29,25,0.25)]' not in html),
    ("Typography fallbacks in style", '.text-headline-lg' in html and '.gap-space-xs' in html),
]

all_passed = True
for name, passed in checks:
    print(f"[{'PASS' if passed else 'FAIL'}] {name}")
    if not passed:
        all_passed = False

print(f"\nOverall Result: {'ALL CHECKS PASSED' if all_passed else 'SOME CHECKS FAILED'}")
