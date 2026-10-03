"""
assemble_companion.py
Concatenates all modular parts and deploys to Android assets and D:\companion-site.
"""
import os
import sys

# Ensure scratch directory is on sys.path
sys.path.insert(0, os.path.dirname(__file__))

from parts_head import HEAD_HTML
from parts_nav import NAV_HTML
from parts_panels import PANELS_HTML
from parts_modals import MODALS_HTML
from parts_js import JS_HTML

FULL_HTML = (HEAD_HTML.strip() + "\n" +
             NAV_HTML.strip() + "\n" +
             PANELS_HTML.strip() + "\n" +
             MODALS_HTML.strip() + "\n" +
             JS_HTML.strip() + "\n")

def main():
    target_app = r'D:\software\V07-Android\app\src\main\assets\companion.html'
    target_v3 = r'D:\software\V07-Android\app\src\main\assets\v3\companion.html'
    target_site = r'D:\companion-site\index.html'

    # 1. Write app asset
    with open(target_app, 'w', encoding='utf-8') as f:
        f.write(FULL_HTML)
    print(f"Written to {target_app} ({len(FULL_HTML)} bytes)")

    # 2. Write v3 asset
    os.makedirs(os.path.dirname(target_v3), exist_ok=True)
    with open(target_v3, 'w', encoding='utf-8') as f:
        f.write(FULL_HTML)
    print(f"Written to {target_v3} ({len(FULL_HTML)} bytes)")

    # 3. Write companion-site for Firebase Hosting and sync plugins
    if os.path.exists(r'D:\companion-site'):
        with open(target_site, 'w', encoding='utf-8') as f:
            f.write(FULL_HTML)
        print(f"Written to {target_site} ({len(FULL_HTML)} bytes)")

        # Sync plugins folder to companion site
        import shutil
        src_plugins = r'D:\software\V07-Android\app\src\main\assets\plugins'
        dst_plugins = r'D:\companion-site\plugins'
        if os.path.exists(src_plugins):
            if os.path.exists(dst_plugins):
                shutil.rmtree(dst_plugins)
            shutil.copytree(src_plugins, dst_plugins)
            print(f"Synchronized plugins directory to {dst_plugins}")

if __name__ == '__main__':
    main()
