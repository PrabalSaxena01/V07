import os

plugin_dir = 'D:/software/V07-Android/app/src/main/assets/v3/plugins'
plugins = [d for d in os.listdir(plugin_dir) if os.path.isdir(os.path.join(plugin_dir, d)) and not d.startswith('_') and d != 'v07-template']

for p in sorted(plugins):
    idx_path = os.path.join(plugin_dir, p, 'index.html')
    if os.path.exists(idx_path):
        size = os.path.getsize(idx_path)
        with open(idx_path, 'r', encoding='utf-8') as f:
            content = f.read()
        has_v07 = 'window.V07' in content or 'V07Platform' in content or 'v07' in content or 'V3' in content
        has_script = '<script>' in content or '<script ' in content
        title = ""
        for line in content.splitlines()[:20]:
            if '<title>' in line:
                title = line.strip()
        print(f"Plugin: {p:25} | Size: {size:6} bytes | Title: {title[:30]} | JS: {has_script} | V07: {has_v07}")
    else:
        print(f"Plugin: {p:25} | MISSING index.html")
