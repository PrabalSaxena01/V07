with open('app/src/main/assets/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

pos = html.find('function navigateBack')
print(html[pos-500:pos])
