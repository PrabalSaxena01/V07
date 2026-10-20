import re

html = open(r'D:\software\V07-Android\app\src\main\assets\v3\index.html', encoding='utf-8').read()

print("--- Onclick with Screen ---")
for m in re.finditer(r'onclick=["\'][^"\']*screen[^"\']*["\']', html, re.I):
    # print context
    start = max(0, m.start() - 50)
    end = min(len(html), m.end() + 50)
    print(html[start:end].replace('\n', ' '))

print("\n--- Focus Timer in HTML ---")
for m in re.finditer(r'onclick=["\'][^"\']*(?:timer|focus|stopwatch)[^"\']*["\']', html, re.I):
    start = max(0, m.start() - 50)
    end = min(len(html), m.end() + 50)
    print(html[start:end].replace('\n', ' '))
