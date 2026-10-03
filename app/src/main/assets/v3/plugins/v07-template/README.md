# V07 Modular Companion Plugin Template

This folder is the authoritative boilerplate template for creating and sideloading custom modular companions into **V07 Sanctuary V3**.

---

## 📁 Directory Structure
```text
my-companion.zip (or folder)
│
├── manifest.json       <-- Required: Plugin identity, permissions & entrypoints
├── index.html          <-- Required: Main UI view
├── v07-sdk.js          <-- Recommended: Universal V3 platform bridge
├── plugin.css          <-- Recommended: Sanctuary Design System tokens
├── settings.html       <-- Optional: Custom preferences view
└── widget.html         <-- Optional: Dashboard mini-card widget
```

---

## ⚡ How to Create Your Own Companion

### Step 1: Configure `manifest.json`
Open `manifest.json` and change:
- `id`: Must be a unique reverse-DNS format containing at least one dot (e.g. `com.username.water`).
- `name`: Display name shown in the Tool Kit.
- `nav`: Choose an icon from [Google Material Symbols](https://fonts.google.com/icons) and a highlight color.

### Step 2: Build Your View in `index.html`
- Use standard HTML, CSS, and modern JavaScript.
- Use `v07.storage.get(key)` and `v07.storage.set(key, value)` for persistent encrypted SQLite storage.
- Use `v07.ledger.addEntry({ category, title, content })` to post updates into Sanctuary Ledger.
- Use `v07.ui.showToast(title, message, type)` to trigger native notifications.
- Use `v07.ui.close()` to dismiss the companion modal.

### Step 3: Test Directly in Your Browser
Double-click `index.html` in any browser (Chrome, Edge, Firefox). The included `v07-sdk.js` automatically detects standalone browser mode and simulates persistent SQLite storage in `localStorage`.

### Step 4: Package into a `.zip`
Compress all files into a `.zip` archive:
```bash
# From within the plugin folder:
zip -r my-companion.zip manifest.json index.html v07-sdk.js plugin.css settings.html widget.html
```
*(Note: You can zip the contents directly or zip the parent directory. Both are automatically supported by the V3 unzipper.)*

### Step 5: Sideload into Sanctuary on Android
1. Transfer `my-companion.zip` to your Android device (or download from GitHub/browser).
2. Open the **Sanctuary** app.
3. Tap on the **Tool Kit** tab in the navigation.
4. Tap the **`Add`** button (with the `.zip` folder icon) in the header.
5. Select `my-companion.zip` from your phone's file picker.
6. The app extracts, validates, registers, and mounts your companion immediately!
