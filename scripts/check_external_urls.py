# -*- coding: utf-8 -*-
with open('app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

import re
urls = re.findall(r'https?://[^\s"\'<>]+', text)
print("External URLs found in HTML head/scripts:")
from collections import Counter
for u, c in Counter(urls).items():
    print(f"  {u} (count: {c})")
