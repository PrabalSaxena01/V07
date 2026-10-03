with open('scratch/generate_v3_stitch_complete.py', 'r', encoding='utf-8') as f:
    text = f.read()

import re

# Check tool-view styles and containers
print("=== Tool view class definitions in CSS ===")
for m in re.finditer(r'\.tool-view\s*\{[^}]*\}', text):
    print(m.group(0))

print("\n=== Tool view headers and top containers ===")
for m in re.finditer(r'<div id="(view-[^"]+)"[^>]*>([\s\S]*?<header[^>]*>[\s\S]*?</header>)', text):
    print(m.group(1), "->", m.group(2).replace('\n', ' ')[:150])
