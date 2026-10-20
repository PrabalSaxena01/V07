with open('D:/software/V07-Android/app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    lines = f.readlines()

for j in range(560, min(len(lines), 720)):
    print(f"{j+1}: {lines[j]}", end='')

