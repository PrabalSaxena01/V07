with open('D:/software/V07-Android/app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    lines = f.readlines()

for i, l in enumerate(lines):
    if 'function switchTab' in l:
        for j in range(i, min(i + 50, len(lines))):
            print(f'{j+1}: {lines[j].rstrip()}')
        break
