import re
from bs4 import BeautifulSoup

ext_index = r"D:/software/V07-Android/output/safekeeping/v3_extracted_contents/assets/v3/index.html"
with open(ext_index, "r", encoding="utf-8") as f:
    content = f.read()

# Find all section ids or div with id
ids = re.findall(r'id="([^"]+)"', content)
print("=== Top unique IDs in extracted APK index.html ===")
print([x for x in set(ids) if 'panel' in x or 'tab' in x or 'nav' in x or 'modal' in x or 'screen' in x or 'btn' in x][:50])

# Check tabs and navigation buttons
nav_matches = re.findall(r'<button[^>]*data-tab="([^"]+)"[^>]*>(.*?)</button>', content, re.DOTALL)
print("\n=== Navigation tabs detected in extracted APK ===")
for t, h in nav_matches:
    text = re.sub(r'<[^>]+>', ' ', h).strip()
    print(f"Tab: '{t}' -> text: '{text}'")

# Check all sections
sections = re.findall(r'<section[^>]*id="([^"]+)"[^>]*>', content)
print("\n=== Sections in extracted APK ===")
print(sections)
