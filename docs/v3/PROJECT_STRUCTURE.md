# V3 Project Structure

## 1. Directory Tree Overview

```text
v3/
├── app/                        # Android Native Host Shell
│   ├── src/main/java/com/v07/
│   │   ├── host/               # MainActivity, WebView host, loopback server
│   │   ├── services/           # ScreenTimeService, FocusTimerService, OverlayService
│   │   ├── widgets/            # Native AppWidget provider & remote adapters
│   │   └── data/               # SQLite database helper & Room entities
│   └── src/main/res/           # Native layouts, drawables, vector assets
│
├── core/                       # Core Platform Domain & Runtime (Kotlin / Shared)
│   ├── domain/                 # Pure business models (Event, Session, Metric, Goal)
│   ├── eventbus/               # Asynchronous event bus & reactive flow definitions
│   ├── plugin/                 # Plugin lifecycle engine, manifest parser, capability guard
│   ├── ai/                     # Local AI Gateway, model adapters, response cache
│   └── sync/                   # VaultSyncProjector (SQLite -> Obsidian Markdown/JSONL)
│
├── ui/                         # Modular Single-Page Web Interface (Shared Mobile/Desktop)
│   ├── src/
│   │   ├── app.ts              # UI Shell bootstrapper & global state store
│   │   ├── components/         # Design system primitives (Button, Card, HUD, Modal, Tile)
│   │   ├── views/              # Primary views (Dashboard, Toolkit, Synthesis, Settings)
│   │   ├── slot/               # Extension slot renderer (Cards, Kit tiles, Full-page frames)
│   │   └── styles/             # Design tokens & vector theme variables
│   ├── index.html              # Clean single entrypoint
│   └── package.json            # Zero-bloat client build config (Vite / TypeScript)
│
├── plugins/                    # Built-in Starter Pack Plugins
│   ├── com.v07.habits/         # Habits Matrix plugin
│   ├── com.v07.tasks/          # Task Discipline plugin
│   ├── com.v07.timetracker/    # Focus & Stopwatch timer plugin
│   ├── com.v07.vitals/         # Neurodynamics & Vitals plugin
│   ├── com.v07.reader/         # Book Reader plugin
│   ├── com.v07.gamify/         # Gamification & Ranks plugin
│   └── com.v07.chronosynth/    # Ambient Audio plugin
│
├── sdk/                        # Plugin Developer Kit
│   ├── manifest.schema.json    # JSON Schema for manifest validation
│   ├── plugin-api.d.ts         # TypeScript definition of window.V07Platform
│   └── test-harness/           # Standalone browser harness for validating plugins
│
└── tests/                      # Automated Test Suite
    ├── unit/                   # Core domain unit tests
    ├── plugin-contract/        # Manifest and capability compliance test runner
    └── e2e/                    # Headless end-to-end integration tests
```

---

## 2. Directory Responsibilities & Boundary Constraints

### `v3/core/`
- **Responsibility**: Pure domain business logic, data persistence, event distribution, AI gateway, and plugin lifecycle governance.
- **Allowed Contents**: Pure Kotlin / Java classes, SQLite queries, coroutines, JSON parsers, interface definitions.
- **Forbidden Contents**: Android UI classes (`View`, `Activity`, `RemoteViews`), DOM manipulation code, plugin-specific implementations.
- **Dependencies**: Depends on SQLite and core serialization libraries only. Never depends on `app/` or `ui/`.

### `v3/app/`
- **Responsibility**: Android platform integration shell. Provides hardware access, foreground services, system overlays, and native AppWidgets.
- **Allowed Contents**: Android SDK classes, services, broadcast receivers, widget remote views, system alert window controllers.
- **Forbidden Contents**: Core business math, plugin manifests, direct SQLite schema manipulation.
- **Dependencies**: Depends on `core/`.

### `v3/ui/`
- **Responsibility**: High-density vector interface rendered in Android WebView and desktop browsers.
- **Allowed Contents**: TypeScript, clean Web Components, CSS tokens, canvas charting, reactive UI state management.
- **Forbidden Contents**: Direct filesystem I/O, raw Android bridge invocations (must call `V07Platform` RPC client).
- **Dependencies**: Interacts with `core/` via loopback HTTP/WebSocket RPC.

### `v3/plugins/`
- **Responsibility**: Isolated functional modules delivering specific user workflows.
- **Allowed Contents**: HTML, CSS, JavaScript/TypeScript, plugin assets, `manifest.json`.
- **Forbidden Contents**: Direct access to `window.parent`, access to other plugins' storage, native Android bridges.
- **Dependencies**: Depends strictly on `sdk/plugin-api.d.ts`.
