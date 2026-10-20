import json
from collections import defaultdict

with open('scratch/v07_companion_export/events.json', encoding='utf-8') as f:
    events = json.load(f)

cat_fields = defaultdict(lambda: defaultdict(set))
for e in events:
    cat = e.get('category', 'unknown')
    for k, v in e.items():
        cat_fields[cat][k].add(type(v).__name__)

for cat, fields in sorted(cat_fields.items()):
    cnt = sum(1 for e in events if e.get('category') == cat)
    print(f'=== Category: {cat} ({cnt} docs) ===')
    for f_name, types in sorted(fields.items()):
        type_str = ', '.join(types)
        print(f'   {f_name}: {type_str}')
