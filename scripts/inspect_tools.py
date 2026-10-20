import re

with open('app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

idx = text.find('id="tab-tools"')
if idx == -1:
    idx = text.find("id='tab-tools'")

print("Found tab-tools at index", idx)
print(text[idx:idx+4000])
