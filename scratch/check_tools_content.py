with open('D:/software/V07-Android/app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

pos_tools = text.find('id="tab-tools"')
end_tools = text.find('id="tab-journal"', pos_tools)
tools_content = text[pos_tools:end_tools]
tools_cards = [line.strip() for line in tools_content.splitlines() if '<h3' in line or 'onclick="open' in line or 'Core Companions' in line or 'Add-on' in line]
for c in tools_cards:
    print(" -", c)
