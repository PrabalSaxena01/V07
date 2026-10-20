with open('D:/software/V07-Android/app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    content = f.read()

idx = content.find('id="modal-plugin-host"')
print(content[idx+1500:idx+3000])


