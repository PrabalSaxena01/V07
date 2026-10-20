with open('D:/software/V07-Android/scripts/generate_v3_stitch_complete.py', 'r', encoding='utf-8') as f:
    text = f.read()

pos_s = text.find('id="tab-settings"')
end_s = text.find('id="bottomNav"', pos_s)
sub = [line.strip() for line in text[pos_s:end_s].splitlines() if '<!-- Section' in line or '<h3' in line]
for s in sub:
    print(" -", s)
