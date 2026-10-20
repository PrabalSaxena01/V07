BASE = 'D:/software/V07-Android/app/src/main/assets/index.html'
with open(BASE, 'r', encoding='utf-8') as f:
    text = f.read()

import re
tabs = re.findall(r'<div id="(tab-[^"]+)"[^>]*>', text)
print("Tabs found:", tabs)

# Check for font-* custom classes vs tailwind classes
font_classes = set(re.findall(r'font-([a-zA-Z0-9_-]+)', text))
print("Font classes used:", sorted(list(font_classes))[:30])

# Check for weird height/width classes
weird_hw = set(re.findall(r'\b(?:w|h)-\[[^\]]+\]', text))
print("Arbitrary w/h used:", sorted(list(weird_hw))[:30])
