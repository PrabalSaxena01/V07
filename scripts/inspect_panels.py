import re

with open(r"D:/software/V07-Android/app/src/main/assets/v3/index.html", "r", encoding="utf-8") as f:
    html = f.read()

panels = re.findall(r'<section\s+id="(panel-[^"]+)"[^>]*>(.*?)</section>', html, re.DOTALL)
for pid, pcontent in panels:
    print(f"=== PANEL: {pid} (length {len(pcontent)}) ===")
    lines = [line.strip() for line in pcontent.split('\n') if line.strip() and not line.strip().startswith('<!--')]
    for line in lines[:15]:
        print("  ", line[:100])
    print()
