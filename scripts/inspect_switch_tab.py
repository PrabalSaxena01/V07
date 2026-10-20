# -*- coding: utf-8 -*-
import re

with open('app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

m = re.search(r'function switchTab\([^)]*\)\s*\{[\s\S]*?\}', text)
if m:
    print('switchTab definition:')
    print(m.group(0))

# Search for all elements that switchTab toggles or shows
views = re.findall(r'id=["\']([a-zA-Z0-9_-]*(?:tab|view|content|page)[a-zA-Z0-9_-]*)["\']', text, re.IGNORECASE)
print('IDs matching tab/view/content/page:')
print(set(views))
