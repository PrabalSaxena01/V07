from bs4 import BeautifulSoup
import json

ext_index = r"D:/software/V07-Android/output/safekeeping/v3_extracted_contents/assets/v3/index.html"
with open(ext_index, "r", encoding="utf-8") as f:
    content = f.read()

soup = BeautifulSoup(content, 'html.parser')

def dump_tab_structure(tab_id, filename):
    tab = soup.find('div', id=tab_id)
    if not tab:
        return
    with open(filename, 'w', encoding='utf-8') as f:
        f.write(tab.prettify())
    print(f"Dumped {tab_id} to {filename}")

dump_tab_structure('tab-home', r'D:/software/V07-Android/scripts/dump_tab_home.html')
dump_tab_structure('tab-tools', r'D:/software/V07-Android/scripts/dump_tab_tools.html')
dump_tab_structure('tab-journal', r'D:/software/V07-Android/scripts/dump_tab_journal.html')
dump_tab_structure('tab-day', r'D:/software/V07-Android/scripts/dump_tab_day.html')
dump_tab_structure('tab-settings', r'D:/software/V07-Android/scripts/dump_tab_settings.html')
dump_tab_structure('view-focus', r'D:/software/V07-Android/scripts/dump_view_focus.html')
dump_tab_structure('view-screentime', r'D:/software/V07-Android/scripts/dump_view_screentime.html')
