with open('app/src/main/assets/index.html', encoding='utf-8') as f:
    html = f.read()

import re

# Check settings tab
idx = html.find('id="tab-settings"')
if idx != -1:
    end_idx = html.find('<!-- View:', idx)
    if end_idx == -1:
        end_idx = idx + 4000
    print("=== Tab Settings Content Snippet ===")
    print(html[idx:idx+2500])

# Check Toast functions
idx_toast = html.find('function showToast')
if idx_toast != -1:
    print("\n=== Toast Implementation ===")
    print(html[idx_toast:idx_toast+1200])
