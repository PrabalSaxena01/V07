BASE = 'D:/software/V07-Android/app/src/main/assets/index.html'
with open(BASE, 'r', encoding='utf-8') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if 'shadow-[' in line:
        print(f"{i+1}: {line.strip()[:120]}")
