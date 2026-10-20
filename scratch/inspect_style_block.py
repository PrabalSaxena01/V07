BASE = 'D:/software/V07-Android/app/src/main/assets/index.html'
with open(BASE, 'r', encoding='utf-8') as f:
    text = f.read()

import re
style_match = re.search(r'<style>(.*?)</style>', text, re.DOTALL)
if style_match:
    style_content = style_match.group(1)
    print(f"Style content length: {len(style_content)}")
    # Print lines that define font or shadow or spacing
    for line in style_content.splitlines():
        if any(k in line for k in ['font-', 'text-', 'shadow', 'spacing', '--', '.font-']):
            print(line[:100])
