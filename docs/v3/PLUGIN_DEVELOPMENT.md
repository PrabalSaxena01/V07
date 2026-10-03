# V3 Plugin Development Guide

This guide walks third-party and core developers through building, testing, and packaging an extension for the V3 platform.

---

## 1. Quick Start: Hello World Plugin

### Step 1: Create Directory Structure
```text
com.myname.water/
├── manifest.json
├── widget.html
├── index.html
└── icon.svg
```

### Step 2: Define `manifest.json`
```json
{
  "id": "com.myname.water",
  "name": "Hydration Tracker",
  "version": "1.0.0",
  "apiVersion": "3.0",
  "description": "Log daily water intake with 1-tap logging.",
  "permissions": ["storage:isolated"],
  "capabilities": ["ui:widget", "ui:kitTile"],
  "extensionPoints": {
    "widgets": [
      {
        "id": "water_counter",
        "title": "HYDRATION",
        "entry": "widget.html",
        "supportedSizes": ["small", "medium"],
        "defaultSize": "small"
      }
    ],
    "kitTile": {
      "label": "WATER",
      "icon": "water_drop",
      "color": "#5CE1E6"
    }
  }
}
```

### Step 3: Implement `widget.html`
```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <style>
    body {
      margin: 0;
      padding: 8px;
      font-family: 'Space Mono', monospace;
      color: #EDF2F7;
      background: transparent;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .count { font-size: 16px; font-weight: 700; color: #5CE1E6; }
    .btn {
      background: #191D28;
      border: 1px solid #262C3E;
      color: #EDF2F7;
      padding: 4px 10px;
      border-radius: 4px;
      cursor: pointer;
    }
  </style>
</head>
<body>
  <div>
    <div style="font-size: 9px; color: #718096">INTAKE TODAY</div>
    <div class="count" id="countVal">0 / 8 GL</div>
  </div>
  <button class="btn" id="addBtn">+1 GLASS</button>

  <script>
    let glasses = 0;
    async function init() {
      glasses = (await V07Platform.storage.get('today_glasses')) || 0;
      render();
    }
    function render() {
      document.getElementById('countVal').textContent = `${glasses} / 8 GL`;
    }
    document.getElementById('addBtn').addEventListener('click', async () => {
      glasses++;
      await V07Platform.storage.set('today_glasses', glasses);
      V07Platform.ui.toast('+1 Glass logged', 'success');
      render();
    });
    window.addEventListener('DOMContentLoaded', init);
  </script>
</body>
</html>
```

---

## 2. Packaging & Validation

### Packaging
Plugins are packaged as standard `.v07zip` archives (ZIP format containing `manifest.json` at root).
```bash
zip -r com.myname.water.v07zip manifest.json widget.html index.html icon.svg
```

### Testing Harness
Run the standalone test harness to validate your manifest and verify API calls without installing onto a physical device:
```bash
npx @v07/plugin-test-harness --dir ./com.myname.water
```
Checks performed:
- Manifest schema validation
- Permission boundary adherence
- Widget viewport responsiveness
- Storage persistence cycle test
