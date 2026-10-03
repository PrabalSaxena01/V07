# -*- coding: utf-8 -*-
with open('app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

import re

for fn in ['showCustomModal', 'closeCustomModal', 'openProfileDrawer', 'closeProfileDrawer', 'openNotificationDrawer', 'closeNotificationDrawer', 'openPluginCatalogDrawer', 'closePluginCatalogDrawer']:
    m = re.search(r'function ' + fn + r'\b[\s\S]*?\{[\s\S]*?\}', text)
    if m:
        print(f"=== {fn} ===")
        print(m.group(0)[:300])
