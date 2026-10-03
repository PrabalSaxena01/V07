targets = [
    'app/src/main/assets/v3/index.html',
    'app/src/main/assets/index.html'
]

bad_catalog = """      setTimeout(() => {
        drawer.classList.add('hidden');
      }, 250);
    });
      renderPluginCatalog();
    }"""

good_catalog = """      setTimeout(() => {
        drawer.classList.add('hidden');
      }, 250);
    }"""

for target in targets:
    with open(target, 'r', encoding='utf-8') as f:
        content = f.read()

    content = content.replace(bad_catalog, good_catalog)
    with open(target, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Fixed catalog in {target}")
