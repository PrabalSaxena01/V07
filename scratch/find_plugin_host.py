with open('D:/software/V07-Android/app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    lines = f.readlines()

found = False
for i, l in enumerate(lines):
    if 'id="modal-plugin-host"' in l:
        found = True
        for j in range(max(0, i-5), min(i + 40, len(lines))):
            print(f'{j+1}: {lines[j].rstrip()}')
        break

if not found:
    print("modal-plugin-host NOT FOUND!")
