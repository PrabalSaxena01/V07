with open('D:/software/V07-Android/app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

pos_you = text.find('id="tab-you"')
end_you = text.find('id="bottomNav"', pos_you)
print("Rest of tab-you:")
print(text[pos_you+2500:pos_you+7500])
