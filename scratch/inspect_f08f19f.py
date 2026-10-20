import subprocess
import re

out = subprocess.check_output(['git', 'show', 'f08f19f:app/src/main/assets/v3/index.html'], text=True, encoding='utf-8')
print("Length of f08f19f index.html:", len(out))

tabs = re.findall(r'<section[^>]*id=[\'"](tab-[^\'"]+)[\'"]', out)
print("Tabs found:", tabs)

views = re.findall(r'id=[\'"](view-[^\'"]+)[\'"]', out)
print("Views found:", views)

nav_items = re.findall(r'data-tab=[\'"]([^\'"]+)[\'"]', out)
print("Nav items:", nav_items)

floating = [line.strip() for line in out.splitlines() if any(k in line.lower() for k in ['bubble', 'overlay', 'screentime', 'pill', 'floating'])]
print(f"Total matching floating lines: {len(floating)}")
for line in floating[:20]:
    print("  ", line[:100])
