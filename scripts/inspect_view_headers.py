# -*- coding: utf-8 -*-
import re

with open('app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

for v_id in ['view-focus', 'view-reading', 'view-habits', 'view-screentime', 'view-mood', 'view-gita', 'view-weekly']:
    pos = text.find(f'id="{v_id}"')
    if pos != -1:
        chunk = text[pos:pos+400]
        first_line = chunk.splitlines()[0] if chunk.splitlines() else ""
        print(f"Found {v_id} at pos {pos}: {first_line}")
        # Find header buttons in this view
        btn_matches = re.findall(r'<button[^>]*>[\s\S]*?</button>', chunk)
        for b in btn_matches[:3]:
            print("   Header btn:", re.sub(r'\s+', ' ', b)[:80])
    else:
        print(f"NOT FOUND: {v_id}")
