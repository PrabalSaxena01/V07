with open('app/src/main/assets/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

import re
matches = re.findall(r'function \w*view\w*\(.*?\)', html, re.IGNORECASE)
print(matches)

matches2 = re.findall(r'onclick="[^"]*view-[^"]*"', html)
print(matches2[:10])
