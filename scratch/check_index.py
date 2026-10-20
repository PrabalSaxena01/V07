with open('D:/software/V07-Android/app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

print('Length:', len(text))
print('Has switchTab:', 'function switchTab' in text)
print('Has bottomNav:', 'bottomNav' in text)
print('Has tab-tools:', 'id="tab-tools"' in text)
print('Has tab-you:', 'id="tab-you"' in text)
print('Has tab-settings:', 'id="tab-settings"' in text)
print('Has modal-screentime:', 'id="modal-screentime"' in text)
print('Has modal-recycle-bin:', 'id="modal-recycle-bin"' in text)
