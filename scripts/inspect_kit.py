# -*- coding: utf-8 -*-
import re

with open('app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

print("Searching for Tools/Kit sections...")
for m in re.finditer(r'(id=["\'](?:view-tools|tools|plugin|kit)[\w-]*["\'])|(switchTab\(["\']tools["\']\))', text, re.IGNORECASE):
    start = max(0, m.start() - 40)
    end = min(len(text), m.end() + 160)
    print("Match at:", m.group(0))
    print(text[start:end])
    print("-" * 50)
