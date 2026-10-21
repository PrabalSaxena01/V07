import re
from bs4 import BeautifulSoup

ext_index = r"D:/software/V07-Android/output/safekeeping/v3_extracted_contents/assets/v3/index.html"
with open(ext_index, "r", encoding="utf-8") as f:
    content = f.read()

soup = BeautifulSoup(content, 'html.parser')

out_lines = []
tabs = ['tab-home', 'tab-tools', 'tab-journal', 'tab-day', 'tab-settings']
for tid in tabs:
    el = soup.find('div', id=tid)
    if el:
        out_lines.append(f"==================================================")
        out_lines.append(f"TAB: {tid}")
        out_lines.append(f"==================================================")
        # Find headings
        headings = el.find_all(['h1', 'h2', 'h3', 'h4'])
        for h in headings[:15]:
            out_lines.append(f"  [{h.name}] {h.get_text(strip=True)}")
        
        # Subsections or major interactive blocks
        blocks = el.find_all(['button', 'input', 'textarea', 'select'])
        out_lines.append(f"  Interactive Controls ({len(blocks)} total):")
        for b in blocks[:20]:
            bid = b.get('id', '')
            btype = b.name
            btext = b.get_text(strip=True) or b.get('placeholder', '') or b.get('title', '')
            onclick = b.get('onclick', '')
            out_lines.append(f"    <{btype} id='{bid}' onclick='{onclick[:40]}'> {btext[:50]}")
        out_lines.append("")

# Also check tool views
tool_views = [
    'view-focus', 'view-reading', 'view-habits', 'view-screentime', 
    'view-mood', 'view-gita', 'view-weekly', 'view-write', 
    'view-datacenter', 'view-running', 'view-timetracker', 'view-tasks'
]
out_lines.append("==================================================")
out_lines.append("TOOL VIEWS (SUB-SCREENS)")
out_lines.append("==================================================")
for vid in tool_views:
    el = soup.find('div', id=vid)
    if el:
        headings = [h.get_text(strip=True) for h in el.find_all(['h1', 'h2', 'h3', 'h4'])[:3]]
        buttons = [b.get_text(strip=True) for b in el.find_all('button')[:5] if b.get_text(strip=True)]
        out_lines.append(f"VIEW: {vid} | Headings: {headings} | Key actions: {buttons}")

with open(r"D:/software/V07-Android/scripts/tabs_summary_utf8.txt", "w", encoding="utf-8") as f:
    f.write("\n".join(out_lines))

print("Wrote tabs summary to tabs_summary_utf8.txt")
