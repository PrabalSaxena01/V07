import os

for root, dirs, files in os.walk('D:/software/V07-Android/app/src/main/assets'):
    for f in files:
        if f.endswith('.html'):
            p = os.path.join(root, f)
            with open(p, 'r', encoding='utf-8', errors='ignore') as fp:
                txt = fp.read()
                if 'modal-plugin-host' in txt or 'plugin-viewport-iframe' in txt:
                    print(f"Found in {p}")
