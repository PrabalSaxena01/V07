import re
from bs4 import BeautifulSoup

ext_index = r"D:/software/V07-Android/output/safekeeping/v3_extracted_contents/assets/v3/index.html"
with open(ext_index, "r", encoding="utf-8") as f:
    content = f.read()

soup = BeautifulSoup(content, 'html.parser')

tabs = ['tab-home', 'tab-tools', 'tab-journal', 'tab-day', 'tab-settings']
for tid in tabs:
    el = soup.find('div', id=tid)
    if el:
        print(f"==================================================")
        print(f"TAB: {tid}")
        print(f"==================================================")
        # Find headings and cards
        headings = el.find_all(['h1', 'h2', 'h3', 'h4'])
        for h in headings[:10]:
            print(f"  [{h.name}] {h.get_text(strip=True)}")
        
        # Find cards or main sections
        cards = el.find_all(class_=re.compile(r'card|pill|box|tile|item|section'))
        print(f"  Total card-like elements: {len(cards)}")
        for c in cards[:8]:
            classes = c.get('class', [])
            cid = c.get('id', '')
            text_snip = ' '.join(c.get_text(strip=True).split())[:70]
            print(f"    id='{cid}' class='{' '.join(classes[:3])}' -> {text_snip}")
        print()
