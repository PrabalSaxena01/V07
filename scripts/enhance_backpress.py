old_back = """    // --- HARDWARE BACK PRESS HANDLER ---
    window.handleBackPress = function() {
      if (activeToolView) {
        navigateBack();
        return true;
      }
      if (activeTab !== 'home') {
        switchTab('home');
        return true;
      }
      return false;
    };"""

new_back = """    // --- HARDWARE BACK PRESS HANDLER ---
    window.handleBackPress = function() {
      // 1. If custom modal is open, close it
      const modal = document.getElementById('v07-modal-container');
      if (modal && !modal.classList.contains('hidden') && !modal.classList.contains('opacity-0')) {
        closeCustomModal();
        return true;
      }
      // 2. If profile drawer is open, close it
      const profile = document.getElementById('v07-profile-drawer');
      if (profile && !profile.classList.contains('hidden') && !profile.classList.contains('opacity-0')) {
        closeProfileDrawer();
        return true;
      }
      // 3. If notifications drawer is open, close it
      const notif = document.getElementById('v07-notifications-drawer');
      if (notif && !notif.classList.contains('hidden') && !notif.classList.contains('opacity-0')) {
        closeNotificationDrawer();
        return true;
      }
      // 4. If catalog drawer is open, close it
      const catalog = document.getElementById('v07-plugin-catalog-drawer');
      if (catalog && !catalog.classList.contains('hidden') && !catalog.classList.contains('opacity-0')) {
        closePluginCatalogDrawer();
        return true;
      }
      // 5. If in plugin sub-view, navigate back to Tools tab
      if (activeToolView) {
        navigateBack();
        return true;
      }
      // 6. If not on home tab, return to home
      if (activeTab !== 'home') {
        switchTab('home');
        return true;
      }
      return false;
    };"""

targets = [
    'app/src/main/assets/v3/index.html',
    'app/src/main/assets/index.html'
]

for target in targets:
    with open(target, 'r', encoding='utf-8') as f:
        content = f.read()

    if old_back in content:
        content = content.replace(old_back, new_back)
        with open(target, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated handleBackPress in {target}")
    else:
        print(f"old_back pattern not found in {target}")
