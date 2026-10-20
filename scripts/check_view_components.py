with open('app/src/main/assets/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

views = ['view-focus', 'view-reading', 'view-habits', 'view-screentime', 'view-mood', 'view-gita', 'view-weekly', 'view-write']

for i, v in enumerate(views):
    start = html.find(f'id="{v}"')
    if start != -1:
        if i + 1 < len(views):
            end = html.find(f'id="{views[i+1]}"')
        else:
            end = html.find('</main>')
        slice_len = end - start if end != -1 else 1000
        print(f"View {v}: start={start}, end={end}, len={slice_len}")
        chunk = html[start:end]
        import re
        headings = re.findall(r'<h[1-4][^>]*>(.*?)</h[1-4]>', chunk)
        print("  Headings:", [re.sub(r'<[^>]+>', '', h).strip() for h in headings[:6]])
