BASE = 'D:/software/V07-Android/app/src/main/assets/index.html'
with open(BASE, 'r', encoding='utf-8') as f:
    lines = f.readlines()

for i, line in enumerate(lines[:120]):
    if '--' in line or 'shadow' in line or 'font-' in line or '<style' in line:
        print(f"{i+1}: {line.rstrip()}")
