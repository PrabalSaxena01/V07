with open('app/src/main/assets/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

pos_bp = html.find('handleBackPress')
print(html[pos_bp-50:pos_bp+300])
