import os
import re

plugin_base = 'D:/software/V07-Android/app/src/main/assets/v3/plugins'
plugins = [
    'com.v07.chronosynth',
    'com.v07.reader',
    'com.v07.habits',
    'com.v07.tasks',
    'com.v07.gamify',
    'com.v07.timetracker',
    'com.v07.vitals',
    'com.v07.gita',
    'com.v07.running'
]

for p in plugins:
    p_path = os.path.join(plugin_base, p, 'index.html')
    if not os.path.exists(p_path):
        print(f"[{p}] NOT FOUND")
        continue
    with open(p_path, 'r', encoding='utf-8') as f:
        html = f.read()
    
    # Check if it has Calm Space Editorial colors (#F4F7F5, #2E7D6A, #3F9A83, #8C5E58, #182230)
    has_teal = '#2E7D6A' in html or '2E7D6A' in html or '#2e7d6a' in html
    has_canvas = '#F4F7F5' in html or '#f4f7f5' in html
    has_slate = '#182230' in html or '#182230' in html
    has_sdk = 'v07-sdk.js' in html
    
    # Extract headers or sections
    headers = re.findall(r'<h[1-4].*?>(.*?)</h[1-4]>', html, re.DOTALL | re.IGNORECASE)
    clean_headers = [re.sub(r'<.*?>', '', h).strip() for h in headers[:5]]
    
    print(f"[{p}] {len(html)} bytes | SDK: {has_sdk} | Teal: {has_teal} | Canvas: {has_canvas} | Headers: {clean_headers}")
