with open('app/src/main/assets/companion.html', encoding='utf-8') as f:
    text = f.read()

import re
colls = re.findall(r'collection\(["\']([^"\']+)["\']\)', text)
print('Collections:', set(colls))

methods = re.findall(r'function (?:sync|login|signup|auth)[^\n(]*\([^)]*\)', text, re.IGNORECASE)
print('Auth/Sync functions:', methods)

for m in re.finditer(r'function (?:doLogin|syncToFirebase|pushSync|pullSync|syncEvents)[^{]*\{', text):
    start = m.start()
    print(text[start:start+400])
