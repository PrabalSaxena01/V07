with open('D:/software/V07-Android/scripts/generate_v3_stitch_complete.py', 'r', encoding='utf-8') as f:
    text = f.read()

pos_s5 = text.find('Android Device & Telemetry Access')
print(text[pos_s5:pos_s5+2500])
