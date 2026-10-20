import os, json

base = r'app/src/main/assets/plugins'
plugins = []
for item in sorted(os.listdir(base)):
    p = os.path.join(base, item, 'manifest.json')
    if os.path.exists(p):
        with open(p, 'r', encoding='utf-8-sig') as f:
            d = json.load(f)
            plugins.append({
                'id': d.get('id', item),
                'name': d.get('name', item),
                'version': d.get('version', '1.0.0'),
                'description': d.get('description', ''),
                'author': d.get('author', 'V07'),
                'entry': d.get('entry', 'index.html'),
                'icon': d.get('nav', {}).get('icon') or d.get('icon', 'extension'),
                'label': d.get('nav', {}).get('label') or d.get('label', item.split('.')[-1].upper()),
                'color': d.get('nav', {}).get('color') or d.get('color', '#4F8DF5'),
                'widgets': d.get('widgets', []),
                'settings': d.get('settings', 'settings.html')
            })

print(f"Total plugins: {len(plugins)}")
for pl in plugins:
    print(f"- {pl['id']} ({pl['name']}): entry={pl['entry']}, icon={pl['icon']}, color={pl['color']}")
