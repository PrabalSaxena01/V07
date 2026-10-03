import sys
sys.stdout.reconfigure(encoding='utf-8')
from bs4 import BeautifulSoup

with open('app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

soup = BeautifulSoup(html, 'html.parser')

print("=== CHECKING ALL BUTTONS IN ALL VIEWS ===")
tool_views = soup.find_all('div', class_='tool-view')
print(f"Found {len(tool_views)} tool views.")

for tv in tool_views:
    tv_id = tv.get('id', 'unknown')
    buttons = tv.find_all(['button', 'a'])
    print(f"\n--- View: {tv_id} ({len(buttons)} buttons) ---")
    for b in buttons:
        onclick = b.get('onclick', None)
        text = b.get_text(strip=True)[:35]
        classes = " ".join(b.get('class', []))[:30]
        if not onclick:
            print(f"  [MISSING ONCLICK] tag={b.name} text='{text}' class='{classes}'")
        else:
            print(f"  [OK] onclick='{onclick[:40]}' text='{text}'")

print("\n=== CHECKING BUTTONS IN MAIN TABS ===")
tabs = ['tab-home', 'tab-tools', 'tab-journal', 'tab-day', 'tab-settings']
for tid in tabs:
    t_el = soup.find('div', id=tid)
    if t_el:
        buttons = t_el.find_all(['button', 'a'])
        print(f"\n--- Tab: {tid} ({len(buttons)} buttons) ---")
        for b in buttons:
            onclick = b.get('onclick', None)
            text = b.get_text(strip=True)[:35]
            if not onclick:
                href = b.get('href', None)
                print(f"  [MISSING ONCLICK] tag={b.name} text='{text}' href='{href}'")
            else:
                print(f"  [OK] onclick='{onclick[:40]}' text='{text}'")
