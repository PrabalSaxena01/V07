BASE = 'D:/software/V07-Android/app/src/main/assets/index.html'
with open(BASE, 'r', encoding='utf-8') as f:
    lines = f.readlines()

for i in range(750, min(850, len(lines))):
    print(f"{i+1}: {lines[i].rstrip()}")
