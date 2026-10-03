import sys

with open('app/src/main/assets/index.html', 'rb') as f:
    raw = f.read()

try:
    text = raw.decode('utf-8')
    print("SUCCESS: index.html decodes 100% cleanly as UTF-8! Size:", len(text), "chars,", len(raw), "bytes.")
except UnicodeDecodeError as e:
    print("ERROR:", e)
