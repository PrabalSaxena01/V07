import os

files = ['app/src/main/assets/v3/index.html', 'app/src/main/assets/index.html']

for fpath in files:
    with open(fpath, 'r', encoding='utf-8') as f:
        content = f.read()

    target_tag = '<script src="https://cdn.tailwindcss.com"></script>'
    replacement = """<style>
    /* Offline Material Symbols Outlined Font */
    @font-face {
      font-family: 'Material Symbols Outlined';
      font-style: normal;
      font-weight: 100 700;
      font-display: block;
      src: url('material-symbols.woff2') format('woff2');
    }
    /* Baseline layout rules: never display hidden modals, drawers, or dialogs */
    .hidden {
      display: none !important;
    }
  </style>
  <script src="tailwind.min.js"></script>"""

    if target_tag in content:
        content = content.replace(target_tag, replacement, 1)
        print(f"Replaced CDN in {fpath}")
    else:
        print(f"CDN tag not found in {fpath}")

    if 'tailwind.config = {' in content and 'window.tailwind = window.tailwind || {};' not in content:
        content = content.replace('tailwind.config = {', 'window.tailwind = window.tailwind || {};\n    window.tailwind.config = {', 1)
        print(f"Guarded tailwind config in {fpath}")

    with open(fpath, 'w', encoding='utf-8') as f:
        f.write(content)

print("Done bundling offline assets!")
