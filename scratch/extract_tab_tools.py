# -*- coding: utf-8 -*-
import re

with open('app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

m = re.search(r'<div[^>]+id=["\']tab-tools["\'][\s\S]*?(?=<div[^>]+id=["\']tab-|\Z)', text)
if m:
    content = m.group(0)
    print("tab-tools length:", len(content))
    print("tab-tools preview (first 1000 chars):")
    print(content[:1000])
    print("\n--- END OF PREVIEW ---")
else:
    print("tab-tools not found!")
