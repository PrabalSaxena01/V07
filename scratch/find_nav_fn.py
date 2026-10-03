with open('app/src/main/assets/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

pos = html.find('navigateBack')
print(html[pos-300:pos+600])
