BASE = 'D:/software/V07-Android/app/src/main/assets/index.html'
with open(BASE, 'r', encoding='utf-8') as f:
    text = f.read()

import re
modals = re.findall(r'<dialog id="([^"]+)"([^>]*)>', text)
for m_id, m_attrs in modals:
    print(f"Modal: {m_id} -> {m_attrs[:80]}")
