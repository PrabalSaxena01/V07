with open('D:/software/V07-Android/app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    lines = f.readlines()
for line_no in [1566, 1793, 1845, 2006]:
    print(f"--- Line {line_no} ---")
    for j in range(line_no, line_no + 8):
        print(f"{j+1}: {lines[j].rstrip()}")
