import re

with open('scratch/parts_panels.py', 'r', encoding='utf-8') as f:
    text = f.read()

for m in re.finditer(r'id=["\'](panel[a-zA-Z0-9_]*)["\']', text):
    print(m.group(1))
