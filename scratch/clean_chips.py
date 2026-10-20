def remove_chips_and_clean():
    paths = [
        r"D:\software\V07-Android\app\src\main\assets\v3\index.html",
        r"D:\software\V07-Android\app\src\main\assets\index.html"
    ]

    for p in paths:
        with open(p, "r", encoding="utf-8") as f:
            content = f.read()

        # 1. Remove Quick Category Filters
        old_chips = '''          <!-- Quick Category Filters -->
          <div class="flex items-center gap-1 overflow-x-auto pb-0.5 no-scrollbar text-xs">
            <button onclick="filterPluginsCategory('all', this)" class="plugin-filter-chip px-2.5 py-0.5 rounded-full bg-primary text-on-primary font-medium text-[11px] cursor-pointer">All (10)</button>
            <button onclick="filterPluginsCategory('mindful', this)" class="plugin-filter-chip px-2.5 py-0.5 rounded-full bg-surface-container-low text-on-surface-variant font-medium text-[11px] cursor-pointer hover:bg-surface-container">Mindfulness</button>
            <button onclick="filterPluginsCategory('productivity', this)" class="plugin-filter-chip px-2.5 py-0.5 rounded-full bg-surface-container-low text-on-surface-variant font-medium text-[11px] cursor-pointer hover:bg-surface-container">Productivity</button>
            <button onclick="filterPluginsCategory('body', this)" class="plugin-filter-chip px-2.5 py-0.5 rounded-full bg-surface-container-low text-on-surface-variant font-medium text-[11px] cursor-pointer hover:bg-surface-container">Body & Health</button>
          </div>'''

        if old_chips in content:
            content = content.replace(old_chips, "")
            print(f"Removed Quick Category Filters from {p}")
        else:
            print(f"Warning: old_chips not found in {p}")

        # 2. Remove duplicate lower openScreenTimeModal
        old_lower_screentime = '''    function openScreenTimeModal() {
      const modal = document.getElementById('modal-screentime');
      if (modal) modal.showModal();
    }'''

        if old_lower_screentime in content:
            content = content.replace(old_lower_screentime, "")
            print(f"Removed duplicate openScreenTimeModal in {p}")
        else:
            print(f"Warning: old_lower_screentime not found in {p}")

        # 3. Remove duplicate lower openRecycleBin
        old_lower_recycle = '''    function openRecycleBin() {
      const modal = document.getElementById('modal-recycle-bin');
      if (modal) {
        renderRecycleBin();
        modal.showModal();
      }
    }'''
        new_lower_recycle = '''    function openRecycleBin() {
      openRecycleBinModal();
    }'''
        if old_lower_recycle in content:
            content = content.replace(old_lower_recycle, new_lower_recycle)
            print(f"Replaced openRecycleBin with openRecycleBinModal in {p}")

        # 4. Remove duplicate lower wrap
        old_wrap = '''    // Wrap openScreenTimeModal to load live data
    const originalOpenScreenTime = window.openScreenTimeModal;
    window.openScreenTimeModal = function() {
      const modal = document.getElementById('modal-screentime');
      if (modal) {
        modal.showModal();
        loadLiveScreenTime();
      }
    };'''
        new_wrap = '''    // Ensure openScreenTimeModal loads live data
    window.openScreenTimeModal = openScreenTimeModal;'''
        if old_wrap in content:
            content = content.replace(old_wrap, new_wrap)
            print(f"Updated live data wrapper in {p}")

        with open(p, "w", encoding="utf-8") as f:
            f.write(content)

    print("Finished remove_chips_and_clean.")

if __name__ == "__main__":
    remove_chips_and_clean()
