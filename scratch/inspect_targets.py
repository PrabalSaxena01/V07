with open('app/src/main/assets/index.html', encoding='utf-8') as f:
    text = f.read()

import re

m_act = re.search(r'<select id="focus-activity-select"[\s\S]*?</select>', text)
if m_act:
    print('Found focus-activity-select:')
    print(repr(m_act.group(0)))

m_lbl = re.search(r'<select id="focus-label-select"[\s\S]*?</select>', text)
if m_lbl:
    print('\nFound focus-label-select:')
    print(repr(m_lbl.group(0)))

print('\nSettings comments:')
for c in re.findall(r'<!--\s*(?:Section|\d+)[^>]+-->', text):
    print(repr(c))
