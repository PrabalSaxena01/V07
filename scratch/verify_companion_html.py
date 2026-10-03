import re
import subprocess

html_path = r'D:\companion-site\index.html'
with open(html_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Extract scripts
scripts = re.findall(r'<script(?:\s+[^>]*)?>(.*?)</script>', content, re.DOTALL)
print(f"Total script tags found: {len(scripts)}")

# Check last main inline script with Node
main_js = scripts[-1]
with open('scratch/temp_check.js', 'w', encoding='utf-8') as f:
    f.write(main_js)

try:
    res = subprocess.run(['node', '--check', 'scratch/temp_check.js'], capture_output=True, text=True)
    if res.returncode == 0:
        print("PASS: Node.js syntax check succeeded with 0 errors!")
    else:
        print("FAIL: Node.js syntax check error:")
        print(res.stderr)
except Exception as e:
    print(f"Node execution check note: {e}")

# 2. Check getElementById references
dom_ids = set(re.findall(r'id=["\']([^"\']+)["\']', content))
js_ids = set(re.findall(r'getElementById\(["\']([^"\']+)["\']\)', main_js))

missing_in_dom = js_ids - dom_ids
print(f"Total DOM IDs: {len(dom_ids)}")
print(f"Total JS getElementById IDs: {len(js_ids)}")
if missing_in_dom:
    print(f"WARNING: The following {len(missing_in_dom)} IDs are queried by JS but missing in DOM:")
    for mid in sorted(list(missing_in_dom)):
        print(f"  - {mid}")
else:
    print("PASS: 100% of getElementById references exist in DOM!")
