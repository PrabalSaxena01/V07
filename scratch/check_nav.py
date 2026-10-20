with open('D:/software/V07-Android/app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    lines = f.readlines()

for i, l in enumerate(lines):
    if any(k in l for k in ['bottomNav', 'data-tab="you"', 'data-tab="settings"']):
        print(f'{i+1}: {l.strip()[:100]}')
