bad_chunk = """    function closeCustomModal() {
      const container = document.getElementById('v07-modal-container');
      const card = document.getElementById('v07-modal-card');
      if (card) {
        card.classList.remove('scale-100');
        card.classList.add('scale-95');
      }
      if (container) {
        container.classList.add('opacity-0', 'pointer-events-none');
        setTimeout(() => {
          container.classList.add('hidden');
        }, 220);
      }
    });
      } else {
        currentActivity = selectEl.value;
        updateFocusDetails();
      }
    }"""

good_chunk = """    function closeCustomModal() {
      const container = document.getElementById('v07-modal-container');
      const card = document.getElementById('v07-modal-card');
      if (card) {
        card.classList.remove('scale-100');
        card.classList.add('scale-95');
      }
      if (container) {
        container.classList.add('opacity-0', 'pointer-events-none');
        setTimeout(() => {
          container.classList.add('hidden');
        }, 220);
      }
      modalConfirmCallback = null;
    }

    // ==========================================
    // CUSTOM ACTIVITIES & LABELS
    // ==========================================
    let currentActivity = 'Deep Work';
    let currentLabel = '#urgent';

    function onActivitySelectChanged(selectEl) {
      if (selectEl.value === '__add_custom__') {
        selectEl.value = currentActivity;
        showCustomPrompt({
          title: 'Add Custom Activity',
          subtitle: 'Focus Sanctuary',
          icon: 'psychology',
          placeholder: 'e.g. Research, Meditation, Design',
          confirmText: 'Add Activity',
          onConfirm: (val) => {
            if (!val) return;
            addCustomActivity(val);
          }
        });
      } else {
        currentActivity = selectEl.value;
        updateFocusDetails();
      }
    }"""

targets = [
    'app/src/main/assets/v3/index.html',
    'app/src/main/assets/index.html'
]

import re

for target in targets:
    with open(target, 'r', encoding='utf-8') as f:
        content = f.read()
    
    if bad_chunk in content:
        content = content.replace(bad_chunk, good_chunk, 1)
        with open(target, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed {target} successfully!")
    else:
        # Try normalizing whitespace if direct match failed
        print(f"Direct match not found in {target}, trying regex...")
        # Pattern matching the bad pattern
        pattern = r"function closeCustomModal\(\)\s*\{[\s\S]*?\}\s*\}\);\s*\}\s*else\s*\{[\s\S]*?currentActivity = selectEl\.value;[\s\S]*?updateFocusDetails\(\);[\s\S]*?\}\s*\}"
        match = re.search(pattern, content)
        if match:
            content = content[:match.start()] + good_chunk + content[match.end():]
            with open(target, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Regex fixed {target} successfully!")
        else:
            print(f"FAILED to find pattern in {target}")
