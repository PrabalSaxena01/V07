with open('D:/software/V07-Android/scripts/generate_v3_stitch_complete.py', 'r', encoding='utf-8') as f:
    text = f.read()

import re
fns = re.findall(r'function\s+([a-zA-Z0-9_]+)\s*\([^\)]*\)', text)
relevant_fns = [fn for fn in set(fns) if any(k in fn.lower() for k in ['screen', 'time', 'overlay', 'bubble', 'setting', 'profile', 'tool', 'kit', 'pill'])]
print("Relevant JS functions in generate_v3_stitch_complete.py:")
for fn in sorted(relevant_fns):
    print(" -", fn)
