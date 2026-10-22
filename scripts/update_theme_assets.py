import os
import glob
import re

# 1. Update vector_plugins.css
css_paths = [
    r"D:\software\V07-Android\app\src\main\assets\vector_plugins.css",
    r"D:\software\V07-Android\app\src\main\assets\v3\vector_plugins.css"
]

for p in css_paths:
    with open(p, "r", encoding="utf-8") as f:
        content = f.read()

    # Make dark mode not match when light theme is active
    content = content.replace(
        ':root, :root[data-theme="dark"], html.dark, body.dark, .dark, [data-theme="dark"] {',
        ':root:not([data-theme="light"]):not(.light), :root[data-theme="dark"], html.dark, body.dark, .dark, [data-theme="dark"] {'
    )
    content = content.replace(
        ':root, .dark, body {\n  color-scheme: dark;\n}',
        ':root:not([data-theme="light"]):not(.light), .dark, body:not(.light) {\n  color-scheme: dark;\n}'
    )

    with open(p, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"Updated {p}")

# 2. Update all plugin HTML bootstrap scripts
plugin_dir = r"D:\software\V07-Android\app\src\main\assets\plugins"
html_files = glob.glob(os.path.join(plugin_dir, "**", "*.html"), recursive=True)

OLD_RESOLVE_PATTERN = re.compile(
    r"function resolveTheme\(\)\s*\{[\s\S]*?return 'dark';\s*\}",
    re.MULTILINE
)

NEW_RESOLVE = """function resolveTheme(){
      try {
        var local = localStorage.getItem('v07_theme') || localStorage.getItem('v07_app_theme');
        if (local === 'light' || local === 'dark') return local;
        var params = new URLSearchParams(window.location.search);
        var qTheme = params.get('theme');
        if (qTheme === 'light' || qTheme === 'dark') return qTheme;
        if (window.AndroidTheme && typeof window.AndroidTheme.isDark === 'function') {
          return window.AndroidTheme.isDark() ? 'dark' : 'light';
        }
        if (window.parent && window.parent !== window && window.parent.document && window.parent.document.documentElement.dataset.theme) {
          return window.parent.document.documentElement.dataset.theme;
        }
      } catch(e){}
      return 'dark';
    }"""

UPDATED_EVENT_LISTENERS_OLD = """    window.addEventListener('storage', function(){ apply(resolveTheme()); });
    window.addEventListener('v07-theme-changed', function(e){ if(e.detail && e.detail.theme) apply(e.detail.theme); });"""

UPDATED_EVENT_LISTENERS_NEW = """    window.addEventListener('storage', function(e){ if(!e || !e.key || e.key === 'v07_theme' || e.key === 'v07_app_theme') apply(resolveTheme()); });
    window.addEventListener('v07-theme-changed', function(e){
      if(e.detail && (e.detail.theme === 'light' || e.detail.theme === 'dark')) {
        try { localStorage.setItem('v07_theme', e.detail.theme); } catch(ex){}
        apply(e.detail.theme);
      }
    });"""

count = 0
for path in html_files:
    with open(path, "r", encoding="utf-8") as f:
        html = f.read()

    if "function resolveTheme()" in html:
        new_html = OLD_RESOLVE_PATTERN.sub(NEW_RESOLVE, html)
        if UPDATED_EVENT_LISTENERS_OLD in new_html:
            new_html = new_html.replace(UPDATED_EVENT_LISTENERS_OLD, UPDATED_EVENT_LISTENERS_NEW)
        elif "window.addEventListener('v07-theme-changed'" in new_html:
            # Replace variant spacing
            new_html = re.sub(
                r"window\.addEventListener\('storage',\s*function\(\)\{\s*apply\(resolveTheme\(\)\);\s*\}\);",
                "window.addEventListener('storage', function(e){ if(!e || !e.key || e.key === 'v07_theme' || e.key === 'v07_app_theme') apply(resolveTheme()); });",
                new_html
            )
            new_html = re.sub(
                r"window\.addEventListener\('v07-theme-changed',\s*function\(e\)\{\s*if\(e\.detail\s*&&\s*e\.detail\.theme\)\s*apply\(e\.detail\.theme\);\s*\}\);",
                """window.addEventListener('v07-theme-changed', function(e){
      if(e.detail && (e.detail.theme === 'light' || e.detail.theme === 'dark')) {
        try { localStorage.setItem('v07_theme', e.detail.theme); } catch(ex){}
        apply(e.detail.theme);
      }
    });""",
                new_html
            )
        with open(path, "w", encoding="utf-8") as f:
            f.write(new_html)
        count += 1
        print(f"Updated HTML: {os.path.relpath(path, plugin_dir)}")

print(f"Total HTML files updated: {count}")
