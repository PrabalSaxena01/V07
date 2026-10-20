with open('app/src/main/assets/companion.html', encoding='utf-8') as f:
    text = f.read()

import re
for m in re.finditer(r'collection\(["\']events["\']\)', text):
    start = max(0, m.start() - 200)
    end = min(len(text), m.end() + 400)
    print("=== Found collection('events') ===")
    print(text[start:end])
    print("-" * 50)
