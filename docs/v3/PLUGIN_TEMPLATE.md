# V3 Modular Plugin Template & Sideloading Specification

This specification defines the authoritative standard for developing, packaging, validating, and sideloading custom modular companions into **V07 Sanctuary V3**.

---

## 1. Principles & Architecture

Sanctuary V3 is fundamentally a **plugin platform**. 
- Core modules (Data Center, Recycle Bin) provide runtime infrastructure, lifecycle state machines, isolated storage partitions, and security boundaries.
- Modular companions (Reading, Mood, Gita, Weekly Digest, Runtime Tracker, Tasks, and user-sideloaded tools) run on top of an identical plugin contract.
- Sideloaded plugins are treated as first-class citizens. They require zero recompilation of the host APK and are installed dynamically via standard `.zip` archives.

```text
┌────────────────────────────────────────────────────────┐
│                   V07 SANCTUARY V3                     │
├──────────────────────────┬─────────────────────────────┤
│     Core Foundations     │     Modular Companions      │
│  - Data Center Hub       │  - Reading Companion        │
│  - Recycle Bin Vault     │  - Mood & Sovereign Checkin │
│  - Ledger Journal        │  - Runtime Tracker          │
│  - Stillness Telemetry   │  - Gita Wisdom              │
│  - Focus Engine          │  - Sideloaded Plugins (.zip)│
└──────────────────────────┴─────────────────────────────┘
```

---

## 2. Directory Layout of a Plugin Archive

A sideloadable plugin archive must be a valid `.zip` file containing a root `manifest.json` or a single root directory enclosing `manifest.json`:

```text
my-companion.zip
│
├── manifest.json        [REQUIRED]  Machine-readable manifest & metadata
├── index.html           [REQUIRED]  Main user interface & primary workflow
├── v07-sdk.js           [RECOMMENDED] Universal platform bridge SDK
├── plugin.css           [RECOMMENDED] Sanctuary design system CSS tokens
├── settings.html        [OPTIONAL]  Preferences & configuration page
├── widget.html          [OPTIONAL]  Dashboard mini-card widget
└── assets/              [OPTIONAL]  Local icons, sounds, images, or data
```

---

## 3. Machine-Readable Manifest (`manifest.json`)

The manifest defines the identity, capabilities, permissions, and entrypoints of the companion.

### Complete Schema Reference
```json
{
  "$schema": "https://v07.app/schemas/v3/plugin-manifest.json",
  "id": "com.developer.companion",
  "name": "Custom Companion",
  "version": "1.0.0",
  "apiVersion": "3.0",
  "description": "Clear 1-2 sentence description of what the companion accomplishes.",
  "publisher": {
    "name": "Developer or Team Name",
    "url": "https://example.com"
  },
  "permissions": [
    "storage:isolated",
    "events:write",
    "events:read"
  ],
  "entry": "index.html",
  "settings": "settings.html",
  "nav": {
    "icon": "extension",
    "label": "COMPANION",
    "color": "#4DE086"
  },
  "widgets": [
    {
      "id": "main_widget",
      "title": "COMPANION",
      "entry": "widget.html",
      "icon": "extension",
      "color": "#4DE086",
      "supportedSizes": ["small", "medium"],
      "defaultSize": "medium"
    }
  ]
}
```

### Manifest Fields Description
| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `id` | `string` | **Yes** | Stable unique identifier formatted as a reverse-DNS domain (e.g. `com.author.tool`). **Must contain at least one dot (`.`)**. |
| `name` | `string` | **Yes** | Human-readable title displayed in the Tool Kit and headers. |
| `version` | `string` | **Yes** | Semantic version string (e.g. `1.0.0`). |
| `apiVersion` | `string` | **No** | Target platform API version (default: `"3.0"`). |
| `description` | `string` | **No** | Short summary of the plugin's purpose. |
| `publisher` | `object` or `string` | **No** | Publisher / author details. |
| `permissions` | `array<string>` | **No** | Requested permissions: `storage:isolated`, `events:write`, `events:read`, `telemetry:read`. |
| `entry` | `string` | **Yes** | Path to the main HTML view relative to package root (default: `"index.html"`). |
| `settings` | `string` | **No** | Relative path to dedicated configuration view (e.g. `"settings.html"`). |
| `nav.icon` | `string` | **No** | Material Symbols icon name (e.g. `"spa"`, `"timer"`, `"sprint"`, `"menu_book"`). |
| `nav.label` | `string` | **No** | Short chip label (e.g. `"COMPANION"`). |
| `nav.color` | `string` | **No** | Hex color code or CSS class for the kit tile avatar (default: `"#4DE086"`). |
| `widgets` | `array<object>` | **No** | Optional dashboard widget definitions (`id`, `title`, `entry`, `supportedSizes`). |

---

## 4. Platform JavaScript API (`v07-sdk.js`)

Plugins communicate with the host via `window.v07` (or `window.V07Platform`). The included `v07-sdk.js` handles:
1. Direct Android WebView injection
2. Iframe modal bridge communication
3. Local browser storage simulation for desktop development

### Key Methods

#### 1. Persistent Storage (SQLite `plugin_storage` Table)
Each plugin receives a strictly isolated key-value partition inside the host SQLite database:
```javascript
// Store data (automatically stringified)
await v07.storage.set('streak_count', 7);
await v07.storage.set('user_profile', { goal: 10, notify: true });

// Retrieve data (automatically JSON parsed)
const streak = await v07.storage.get('streak_count'); // returns 7
const profile = await v07.storage.get('user_profile'); // returns { goal: 10, notify: true }

// Delete a key
await v07.storage.delete('temp_draft');

// List all keys
const keys = await v07.storage.keys();
```

#### 2. Sanctuary Ledger Logging
Commit notes, reflections, or metrics directly into the unified Sanctuary Ledger stream:
```javascript
await v07.ledger.addEntry({
  category: 'hydration',            // Category or custom tag
  title: 'Morning Water Logged',    // Optional title
  content: 'Drank 500ml with lemon',// Note content
  timestamp: Date.now()             // Epoch timestamp
});
```

#### 3. Native UI & Navigation
```javascript
// Display native toast notification
v07.ui.showToast('Hydration Logged', '+1 glass recorded', 'success');

// Close / dismiss plugin modal
v07.ui.close();

// Detect theme ('dark' or 'light')
const theme = v07.ui.getTheme();
```

---

## 5. Sanctuary Design System & Styling

Plugins should respect Sanctuary's visual design language:
- **Typography:** System sans-serif (`-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Inter", sans-serif`).
- **Surfaces:** Dark-first palettes with clean elevation:
  - Background: `--surface` (`#0f1216`)
  - Elevated Container: `--surface-container-lowest` (`#14171d`)
  - Higher Container: `--surface-container` (`#21262f`)
  - Text: `--on-surface` (`#edf2f7`)
  - Subtext: `--on-surface-variant` (`#94a3b8`)
- **Corners:** Rounded corners (`14px` - `18px`).
- **Icons:** [Google Material Symbols Outlined](https://fonts.google.com/icons).

---

## 6. Sideloading Step-by-Step Workflow

### Step 1: Package
From inside your plugin directory, zip all files:
```powershell
Compress-Archive -Path manifest.json, index.html, v07-sdk.js, plugin.css, settings.html, widget.html -DestinationPath my-plugin.zip
```
*(On macOS/Linux: `zip -r my-plugin.zip manifest.json index.html v07-sdk.js plugin.css settings.html widget.html`)*

### Step 2: Sideload onto Device
1. Open **V07 Sanctuary** on your Android device.
2. Navigate to the **Tool Kit** tab in the bottom navigation bar.
3. Tap the **`Add`** button in the top right header (bearing the `folder_zip` icon).
4. Select `my-plugin.zip` from your phone's file picker.

### Step 3: Installation & Verification
- The native engine checks for zip-slip vulnerabilities, extracts the archive into `/data/user/0/com.v07.dashboard.v2/files/plugins/<plugin-id>/`, and validates `manifest.json`.
- The plugin is automatically registered in SQLite, enabled, and mounted directly into the Tool Kit feed stream.
- Tapping **Open** launches the companion in the sandboxed viewport.

---

## 7. Managing & Uninstalling Custom Plugins

- Tap **`Manage`** in the Tool Kit header to reorder companions or disable them.
- Custom sideloaded plugins display a trash icon (`delete`) in Manage mode to completely purge the plugin files and remove it from the device.
