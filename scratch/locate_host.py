with open('D:/software/V07-Android/app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    lines = f.readlines()

for i, l in enumerate(lines):
    if 'modal-plugin-host' in l or 'plugin-viewport-iframe' in l:
        print(f'{i+1}: {l.strip()[:100]}')
