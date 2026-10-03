from bs4 import BeautifulSoup
import re

with open('app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

soup = BeautifulSoup(html, 'html.parser')
script_tags = soup.find_all('script')
main_script = script_tags[-1].string or ''

# Find all onclick attributes
elements_with_onclick = soup.find_all(lambda tag: tag.has_attr('onclick'))
print(f"Total onclick handlers found: {len(elements_with_onclick)}")

missing_functions = set()
checked_functions = set()

for el in elements_with_onclick:
    onclick_code = el['onclick']
    # Extract function names like foo(...) or foo()
    fn_matches = re.findall(r'([a-zA-Z0-9_$]+)\s*\(', onclick_code)
    for fn in fn_matches:
        if fn in ['void', 'alert', 'confirm', 'prompt', 'parseInt', 'parseFloat', 'encodeURIComponent', 'decodeURIComponent']:
            continue
        checked_functions.add(fn)
        # Check if defined in main_script: function fn( or window.fn = or const fn = or let fn = or var fn =
        pattern = rf'(function\s+{fn}\s*\(|window\.{fn}\s*=|const\s+{fn}\s*=|let\s+{fn}\s*=|var\s+{fn}\s*=)'
        if not re.search(pattern, main_script):
            missing_functions.add((fn, onclick_code))

print(f"Total distinct functions checked: {len(checked_functions)}")
if missing_functions:
    print(f"WARNING: Potential missing functions: {missing_functions}")
else:
    print("ALL onclick functions are defined in the script!")
