with open('D:/software/V07-Android/app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

pos = text.find('id="modal-screentime"')
if pos != -1:
    print("Found modal-screentime at pos:", pos)
    print(text[pos:pos+2500])
else:
    print("modal-screentime not found")
