with open('app/src/main/assets/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

pos_vw = html.find('id="view-write"')
pos_script = html.find('<script', pos_vw)
print(f"view-write pos: {pos_vw}, script pos: {pos_script}, length: {pos_script - pos_vw}")
print("view-write content snippet:")
print(html[pos_vw:pos_vw + 1500])
