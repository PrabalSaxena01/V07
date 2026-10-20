with open('D:/software/V07-Android/scripts/generate_v3_stitch_complete.py', 'r', encoding='utf-8') as f:
    text = f.read()

pos_s = text.find('id="tab-settings"')
if pos_s != -1:
    end_s = text.find('</section>', pos_s)
    print("Found tab-settings in generate_v3_stitch_complete.py!")
    sub = [line.strip() for line in text[pos_s:pos_s+10000].splitlines() if '<h3' in line or '<!-- Section' in line or 'id="setting' in line]
    for s in sub:
        print(" -", s)
else:
    print("tab-settings NOT found in generate_v3_stitch_complete.py")
