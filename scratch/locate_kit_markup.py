# -*- coding: utf-8 -*-
with open('app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

import re

print("=== TAB TOOLS START ===")
pos = text.find('id="tab-tools"')
print(text[pos:pos+400])

print("\n=== DRAWER INSERTS ===")
pos_drawer = text.find('id="v07-profile-drawer"')
print(text[pos_drawer:pos_drawer+300])
