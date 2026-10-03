with open('app/src/main/assets/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

pos_sw = html.find('function switchTab')
print(html[pos_sw+600:pos_sw + 1600])
