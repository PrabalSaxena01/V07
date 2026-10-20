with open('app/src/main/assets/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

import re
matches = [(m.start(), m.group(0)) for m in re.finditer(r'switchTab\([^)]+\)', html)]
for pos, m in matches:
    line_no = html[:pos].count('\n') + 1
    # print context
    print(f"Line {line_no}: {m}")
