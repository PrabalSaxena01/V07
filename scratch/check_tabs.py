with open('D:/software/V07-Android/app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

import re
tabs = re.findall(r'id=[\'"](tab-[^\'"]+)[\'"]', text)
print("tab-* elements in index.html:", tabs)

nav = re.findall(r'data-tab=[\'"]([^\'"]+)[\'"]', text)
print("data-tab elements in index.html:", nav)

# Let's inspect what is inside tab-tools and tab-you
for t in tabs:
    pos = text.find(f'id="{t}"')
    print(f"\n--- {t} (pos {pos}) ---")
    print(text[pos:pos+300])
