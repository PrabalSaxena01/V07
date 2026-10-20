files = [r'D:\software\V07-Android\app\src\main\assets\v3\index.html', r'D:\software\V07-Android\app\src\main\assets\index.html']
for p in files:
    with open(p, 'r', encoding='utf-8') as f:
        s = f.read()
    s = s.replace(
        '<span class="text-sm font-semibold text-on-surface truncate">Database Cockpit</span>',
        '<span class="text-sm font-semibold text-on-surface whitespace-nowrap">Database Cockpit</span>'
    )
    old_close = """      if (host) {
        host.classList.add('translate-y-full', 'opacity-0', 'pointer-events-none');
        host.classList.remove('translate-y-0', 'opacity-100', 'pointer-events-auto');
      }"""
    new_close = """      if (host) {
        host.classList.add('translate-y-full', 'opacity-0', 'pointer-events-none');
        host.classList.remove('translate-y-0', 'opacity-100', 'pointer-events-auto');
        try {
          if (window.AndroidTheme && typeof window.AndroidTheme.setPluginFullscreen === 'function') {
            window.AndroidTheme.setPluginFullscreen(false);
          }
        } catch (e) {
          console.warn('AndroidTheme bridge call failed', e);
        }
      }"""
    s = s.replace(old_close, new_close)
    with open(p, 'w', encoding='utf-8') as f:
        f.write(s)
print('Successfully updated closePluginModal and Database Cockpit!')
