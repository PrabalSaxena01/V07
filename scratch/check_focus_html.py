with open('app/src/main/assets/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

pos = html.find('id="view-focus"')
print(html[pos:pos+1500])
