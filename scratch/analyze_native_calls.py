# -*- coding: utf-8 -*-
with open('app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

import re
matches = re.findall(r"nativeInvoke\(['\"]([^'\"]+)['\"]", text)
from collections import Counter
print("All nativeInvoke calls in index.html:")
for k, v in Counter(matches).items():
    print(f"  {k}: {v}")
