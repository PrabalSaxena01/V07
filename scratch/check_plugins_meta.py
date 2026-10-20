import os, re

plugins_dir = 'D:/software/V07-Android/app/src/main/assets/plugins'
plugins = [d for d in os.listdir(plugins_dir) if d.startswith('com.v07.')]

for p in sorted(plugins):
    idx = os.path.join(plugins_dir, p, 'index.html')
    with open(idx, 'r', encoding='utf-8') as f:
        c = f.read()
    print(f"=== {p} ===")
    title_m = re.search(r'<title>(.*?)</title>', c, re.IGNORECASE)
    viewport_m = re.search(r'<meta\s+name=["\']viewport["\'][^>]*>', c, re.IGNORECASE)
    print("  Title:", title_m.group(1) if title_m else "None")
    print("  Viewport:", viewport_m.group(0) if viewport_m else "None")
