import re

with open('app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

dialogs = re.findall(r'<dialog[^>]*id=["\']([^"\']+)["\'][^>]*>', text)
print("Dialog IDs:")
for d in dialogs:
    print(" -", d)
