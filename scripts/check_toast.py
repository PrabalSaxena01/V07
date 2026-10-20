with open('app/src/main/assets/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

pos_toast = html.find('id="v07-toast-container"')
print("=== Toast HTML ===")
print(html[pos_toast:pos_toast + 1200])

pos_fn = html.find('function showToast')
print("\n=== Toast JS ===")
print(html[pos_fn:pos_fn + 1500])
