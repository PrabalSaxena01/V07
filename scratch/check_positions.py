with open('app/src/main/assets/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

import re

for tag in ['<main', '</main>', '<nav', '</nav>', 'id="tab-home"', 'id="tab-settings"', 'id="view-focus"', 'id="view-write"']:
    pos = html.find(tag)
    print(f"{tag}: pos {pos}")
