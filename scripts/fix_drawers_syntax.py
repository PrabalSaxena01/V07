targets = [
    'app/src/main/assets/v3/index.html',
    'app/src/main/assets/index.html'
]

for target in targets:
    with open(target, 'r', encoding='utf-8') as f:
        content = f.read()

    # Fix closeNotificationDrawer
    bad_notif = """      setTimeout(() => {
        drawer.classList.add('hidden');
      }, 250);
    });
    }"""
    good_notif = """      setTimeout(() => {
        drawer.classList.add('hidden');
      }, 250);
    }"""

    # Fix closeProfileDrawer
    bad_profile = """      setTimeout(() => {
        drawer.classList.add('hidden');
      }, 250);
    });
    }"""
    good_profile = """      setTimeout(() => {
        drawer.classList.add('hidden');
      }, 250);
    }"""

    content = content.replace(bad_notif, good_notif)
    # Also check if any closeCatalogDrawer has it
    content = content.replace("drawer.classList.add('hidden');\n      }, 250);\n    });\n    }", "drawer.classList.add('hidden');\n      }, 250);\n    }")
    
    with open(target, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Patched {target}")
