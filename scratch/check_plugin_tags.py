import os
import re

plugin_base = 'D:/software/V07-Android/app/src/main/assets/v3/plugins'
plugins = sorted([d for d in os.listdir(plugin_base) if os.path.isdir(os.path.join(plugin_base, d)) and not d.startswith('_') and d != 'v07-template'])

for p in plugins:
    p_path = os.path.join(plugin_base, p, 'index.html')
    with open(p_path, 'r', encoding='utf-8') as f:
        html = f.read()
    
    scripts = re.findall(r'<script.*?>', html, re.IGNORECASE)
    links = re.findall(r'<link.*?>', html, re.IGNORECASE)
    print(f"\nPlugin: {p}")
    for l in links:
        print(f"  LINK: {l}")
    for s in scripts:
        print(f"  SCRIPT: {s}")
