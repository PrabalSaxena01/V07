import re

ext_index = r"D:/software/V07-Android/output/safekeeping/v3_extracted_contents/assets/v3/index.html"
with open(ext_index, "r", encoding="utf-8") as f:
    content = f.read()

# Find main view containers / tabs
tab_containers = re.findall(r'<(div|main|section)[^>]*id="(tab-[^"]+|view-[^"]+|screen-[^"]+|page-[^"]+)"[^>]*>', content)
print("=== Tab / View Containers ===")
for tag, tid in tab_containers:
    print(f"<{tag} id='{tid}'>")

# Search for bottom navigation bar or floating dock in the extracted html
nav_bars = re.findall(r'<nav[^>]*class="([^"]*)"[^>]*>(.*?)</nav>', content, re.DOTALL)
print(f"\n=== Found {len(nav_bars)} <nav> elements ===")
for cls, body in nav_bars:
    print(f"Class: {cls}")
    buttons = re.findall(r'<button[^>]*>(.*?)</button>', body, re.DOTALL)
    for b in buttons:
        clean = re.sub(r'<[^>]+>', ' ', b).strip()
        print(f"  Button: '{clean}'")

# Look for footer or mobile bar or navigation dock
dock_matches = re.findall(r'<div[^>]*class="[^"]*(dock|nav|bar|tabbar|bottom)[^"]*"[^>]*>(.*?)</div>', content, re.DOTALL)
print(f"\n=== Found {len(dock_matches)} dock-like containers ===")
for cls, body in dock_matches[:5]:
    print(f"Dock class: {cls}")
    btns = re.findall(r'<button[^>]*id="([^"]*)"[^>]*class="([^"]*)"[^>]*>(.*?)</button>', body, re.DOTALL)
    for bid, bcls, btext in btns:
        clean = re.sub(r'<[^>]+>', ' ', btext).strip()
        print(f"  id='{bid}' | class='{bcls}' | text='{clean}'")
