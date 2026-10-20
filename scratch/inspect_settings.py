BASE = 'D:/software/V07-Android/app/src/main/assets/index.html'
with open(BASE, 'r', encoding='utf-8') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if 'id="tab-settings"' in line:
        print("=== Settings Tab ===")
        for j in range(i, min(i+60, len(lines))):
            print(f"{j+1}: {lines[j].rstrip()}")
        break
