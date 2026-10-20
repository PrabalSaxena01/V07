import os, json

base = r'D:\software\V07-Android\app\src\main\assets\plugins'
for d in sorted(os.listdir(base)):
    p = os.path.join(base, d)
    if os.path.isdir(p) and not d.startswith('_'):
        mf = os.path.join(p, 'manifest.json')
        idx = os.path.join(p, 'index.html')
        name = d
        if os.path.exists(mf):
            try:
                data = json.load(open(mf, 'r', encoding='utf-8'))
                name = f"{data.get('name')} [{d}] (v{data.get('version', '?')})"
            except Exception as e:
                name = f"{d} (manifest error: {e})"
        idx_len = os.path.getsize(idx) if os.path.exists(idx) else 0
        print(f"{name}: index.html size = {idx_len} bytes")
