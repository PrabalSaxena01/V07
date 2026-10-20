with open('D:/software/V07-Android/scripts/generate_v3_stitch_complete.py', 'r', encoding='utf-8') as f:
    text = f.read()

import re
pos = text.find('id="view-screentime"')
if pos != -1:
    print("Found view-screentime at pos:", pos)
    print(text[pos-100:pos+1500])
else:
    print("view-screentime not found")
