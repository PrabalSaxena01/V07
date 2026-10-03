# -*- coding: utf-8 -*-
import re

with open('app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

for v_id in ['view-focus', 'view-reading', 'view-habits', 'view-screentime', 'view-mood', 'view-gita', 'view-weekly']:
    pos = text.find(f'id="{v_id}"')
    if pos != -1:
        chunk = text[pos:pos+1500]
        header_m = re.search(r'<header[\s\S]*?</header>', chunk)
        if header_m:
            print(f"=== {v_id} HEADER ===")
            print(header_m.group(0))
        else:
            # find first 500 chars
            print(f"=== {v_id} TOP CHUNK ===")
            print(chunk[:400])
