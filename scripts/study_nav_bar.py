import re

ext_index = r"D:/software/V07-Android/output/safekeeping/v3_extracted_contents/assets/v3/index.html"
with open(ext_index, "r", encoding="utf-8") as f:
    content = f.read()

nav_bar = re.search(r'<nav[^>]*>(.*?)</nav>', content, re.DOTALL)
if nav_bar:
    print("=== NAV BAR HTML ===")
    print(nav_bar.group(0))

# Also search for tab switching functions in JS inside index.html or companion.html
scripts = re.findall(r'<script[^>]*>(.*?)</script>', content, re.DOTALL)
print(f"\n=== Found {len(scripts)} inline script tags ===")
for i, s in enumerate(scripts):
    if "switchTab" in s or "openTab" in s or "navigateTo" in s or "tab-home" in s:
        print(f"Script #{i} length: {len(s)} bytes contains tab navigation logic")
        # find function switchTab or similar
        fn_matches = re.findall(r'function\s+([a-zA-Z0-9_]+)\s*\([^\)]*\)', s)
        print("  Functions:", fn_matches[:30])
