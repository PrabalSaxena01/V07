with open('app/src/main/assets/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

import re
matches = re.findall(r'\.toast-[^{]+{[^}]+}', html)
for m in matches:
    print(m)
