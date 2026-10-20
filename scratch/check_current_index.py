with open('D:/software/V07-Android/app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

import re
print("Length of current assets/v3/index.html:", len(text))
sections = re.findall(r'<section[^>]*id=[\'"]([^\'"]+)[\'"]', text)
print("Sections (tabs):", sections)

tool_views = re.findall(r'<div[^>]*id=[\'"](view-[^\'"]+)[\'"]', text)
print("Tool views:", tool_views)

modals = re.findall(r'<div[^>]*id=[\'"](modal-[^\'"]+)[\'"]', text)
print("Modals:", modals)

dialogs = re.findall(r'<dialog[^>]*id=[\'"]([^\'"]+)[\'"]', text)
print("Dialogs:", dialogs)

fns = re.findall(r'function\s+([a-zA-Z0-9_]+)\s*\(', text)
relevant_fns = [fn for fn in set(fns) if any(k in fn.lower() for k in ['screen', 'overlay', 'bubble', 'setting', 'profile', 'tool', 'kit', 'pill', 'gita', 'focus', 'habit', 'read'])]
print("Relevant JS functions in assets/v3/index.html:")
for fn in sorted(relevant_fns):
    print(" -", fn)
