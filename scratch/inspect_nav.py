with open('app/src/main/assets/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

pos = html.find('id="app-bottom-nav"')
print(html[pos:pos+1500])
