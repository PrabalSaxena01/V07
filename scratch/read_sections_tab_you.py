with open('D:/software/V07-Android/app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

pos_you = text.find('id="tab-you"')
end_you = text.find('id="bottomNav"', pos_you)
print("Sections in tab-you:")
sub_sections = [line.strip() for line in text[pos_you:end_you].splitlines() if '<!-- Section' in line or '<h3' in line]
for s in sub_sections:
    print(" -", s)
