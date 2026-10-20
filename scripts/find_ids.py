import re

with open('scratch/parts_js.py', 'r', encoding='utf-8') as f:
    text = f.read()

ids = set(re.findall(r"getElementById\(['\"]([^'\"]+)['\"]\)", text))
print(f"Total getElementById calls: {len(ids)}")
for i in sorted(list(ids)):
    print(f"  {i}")
