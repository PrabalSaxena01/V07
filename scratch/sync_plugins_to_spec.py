import os
import json
import shutil
import zipfile

BASE_DIR = r"D:\software\V07-Android"
SDK_SOURCE = os.path.join(BASE_DIR, r"app\src\main\assets\plugins\v07-template\v07-sdk.js")
with open(SDK_SOURCE, 'r', encoding='utf-8') as f:
    SDK_CODE = f.read()

PLUGIN_ROOTS = [
    os.path.join(BASE_DIR, r"app\src\main\assets\plugins"),
    os.path.join(BASE_DIR, r"app\src\main\assets\v3\plugins")
]

# Standard specifications for all 9 plugins
SPECS = {
    "com.v07.habits": {
        "id": "com.v07.habits",
        "name": "Habits Matrix",
        "version": "3.0.0",
        "apiVersion": "3.0",
        "description": "Daily recurring consistency checklist and streak tracker on a calendar heatmap.",
        "publisher": {"name": "V07 Core Team", "url": "https://v07.app"},
        "permissions": ["storage:isolated", "events:listen", "events:emit", "notifications:schedule"],
        "capabilities": ["ui:widget", "ui:kitTile", "ui:page"],
        "dependencies": {"v07-platform": ">=3.0.0"},
        "extensionPoints": {
            "widgets": [
                {
                    "id": "habits_matrix_card",
                    "title": "HABITS MATRIX",
                    "entry": "widget.html",
                    "icon": "local_fire_department",
                    "color": "#FFB454",
                    "supportedSizes": ["compact", "small", "medium", "large"],
                    "defaultSize": "medium"
                }
            ],
            "kitTile": {
                "label": "HABITS",
                "icon": "local_fire_department",
                "color": "#FFB454"
            },
            "page": {
                "entry": "index.html",
                "title": "Habits Matrix"
            },
            "settings": {
                "entry": "settings.html",
                "schema": "settings.schema.json"
            }
        },
        "config": {
            "dailyReminderTime": {
                "type": "string",
                "format": "time",
                "default": "21:00",
                "description": "Daily notification time to complete habits"
            }
        }
    },
    "com.v07.vitals": {
        "id": "com.v07.vitals",
        "name": "Mood & Energy",
        "version": "3.0.0",
        "apiVersion": "3.0",
        "description": "Log and track daily mental and physical state with quick ratings and vitals telemetry.",
        "publisher": {"name": "V07 Core Team", "url": "https://v07.app"},
        "permissions": ["storage:isolated", "events:listen", "events:emit", "notifications:schedule"],
        "capabilities": ["ui:widget", "ui:kitTile", "ui:page"],
        "dependencies": {"v07-platform": ">=3.0.0"},
        "extensionPoints": {
            "widgets": [
                {
                    "id": "vitals_card",
                    "title": "MOOD & ENERGY",
                    "entry": "widget.html",
                    "icon": "battery_charging_full",
                    "color": "#4DE086",
                    "supportedSizes": ["compact", "small", "medium", "large"],
                    "defaultSize": "medium"
                }
            ],
            "kitTile": {
                "label": "VITALS",
                "icon": "battery_charging_full",
                "color": "#4DE086"
            },
            "page": {
                "entry": "index.html",
                "title": "Mood & Energy"
            },
            "settings": {
                "entry": "settings.html"
            }
        }
    },
    "com.v07.reader": {
        "id": "com.v07.reader",
        "name": "Reading Companion",
        "version": "3.0.0",
        "apiVersion": "3.0",
        "description": "Book collection and reading tracker. Manage library shelves, log pacing, and track reading milestones.",
        "publisher": {"name": "V07 Core Team", "url": "https://v07.app"},
        "permissions": ["storage:isolated", "events:listen", "events:emit", "notifications:schedule"],
        "capabilities": ["ui:widget", "ui:kitTile", "ui:page"],
        "dependencies": {"v07-platform": ">=3.0.0"},
        "extensionPoints": {
            "widgets": [
                {
                    "id": "reader_active",
                    "title": "READING TRACKER",
                    "entry": "widget.html",
                    "icon": "auto_stories",
                    "color": "#8D6CFF",
                    "supportedSizes": ["compact", "small", "medium", "large"],
                    "defaultSize": "medium"
                }
            ],
            "kitTile": {
                "label": "READING",
                "icon": "auto_stories",
                "color": "#8D6CFF"
            },
            "page": {
                "entry": "index.html",
                "title": "Reading Companion"
            },
            "settings": {
                "entry": "settings.html"
            }
        }
    },
    "com.v07.running": {
        "id": "com.v07.running",
        "name": "Athletics & Running",
        "version": "3.0.0",
        "apiVersion": "3.0",
        "description": "Log running sessions, track pace, cadence, notes, and visual progression across weekly and monthly horizons.",
        "publisher": {"name": "V07 Core Team", "url": "https://v07.app"},
        "permissions": ["storage:isolated", "events:listen", "events:emit"],
        "capabilities": ["ui:widget", "ui:kitTile", "ui:page"],
        "dependencies": {"v07-platform": ">=3.0.0"},
        "extensionPoints": {
            "widgets": [
                {
                    "id": "running_weekly_stats",
                    "title": "RUNNING DISTANCE",
                    "entry": "widget.html",
                    "icon": "directions_run",
                    "color": "#10B981",
                    "supportedSizes": ["small", "medium"],
                    "defaultSize": "medium"
                }
            ],
            "kitTile": {
                "label": "RUNNING",
                "icon": "directions_run",
                "color": "#10B981"
            },
            "page": {
                "entry": "index.html",
                "title": "Athletics & Running"
            },
            "settings": {
                "entry": "settings.html"
            }
        }
    },
    "com.v07.gita": {
        "id": "com.v07.gita",
        "name": "Gita Wisdom",
        "version": "3.0.0",
        "apiVersion": "3.0",
        "description": "Contemplative verses and philosophical clarity for daily life. 700+ verses with search, reflections, and study tracking.",
        "publisher": {"name": "V07 Core Team", "url": "https://v07.app"},
        "permissions": ["storage:isolated", "events:listen", "events:emit"],
        "capabilities": ["ui:widget", "ui:kitTile", "ui:page"],
        "dependencies": {"v07-platform": ">=3.0.0"},
        "extensionPoints": {
            "widgets": [
                {
                    "id": "gita_daily_verse",
                    "title": "DAILY GITA CONTEMPLATION",
                    "entry": "widget.html",
                    "icon": "auto_stories",
                    "color": "#FF9F43",
                    "supportedSizes": ["small", "medium"],
                    "defaultSize": "medium"
                }
            ],
            "kitTile": {
                "label": "GITA",
                "icon": "auto_stories",
                "color": "#FF9F43"
            },
            "page": {
                "entry": "index.html",
                "title": "Gita Wisdom"
            },
            "settings": {
                "entry": "settings.html"
            }
        }
    },
    "com.v07.gamify": {
        "id": "com.v07.gamify",
        "name": "Gamify Quests",
        "version": "3.0.0",
        "apiVersion": "3.0",
        "description": "Earn XP for conscious daily focus and logging. Track levels, unlock mastery badges, and build streaks.",
        "publisher": {"name": "V07 Core Team", "url": "https://v07.app"},
        "permissions": ["storage:isolated", "events:listen", "events:emit", "notifications:schedule"],
        "capabilities": ["ui:widget", "ui:kitTile", "ui:page"],
        "dependencies": {"v07-platform": ">=3.0.0"},
        "extensionPoints": {
            "widgets": [
                {
                    "id": "gamify_stats",
                    "title": "GAMIFY PROGRESS",
                    "entry": "widget.html",
                    "icon": "military_tech",
                    "color": "#4DE086",
                    "supportedSizes": ["compact", "small", "medium", "large"],
                    "defaultSize": "medium"
                }
            ],
            "kitTile": {
                "label": "GAMIFY",
                "icon": "military_tech",
                "color": "#4DE086"
            },
            "page": {
                "entry": "index.html",
                "title": "Gamify Quests"
            },
            "settings": {
                "entry": "settings.html"
            }
        }
    },
    "com.v07.chronosynth": {
        "id": "com.v07.chronosynth",
        "name": "Weekly Digest",
        "version": "3.0.0",
        "apiVersion": "3.0",
        "description": "Weekly intelligence engine aggregating focus, tasks, habits, athletics, and screen time into executive briefings.",
        "publisher": {"name": "V07 Core Team", "url": "https://v07.app"},
        "permissions": ["storage:isolated", "events:listen", "events:emit", "telemetry:read", "notifications:schedule"],
        "capabilities": ["ui:widget", "ui:kitTile", "ui:page"],
        "dependencies": {"v07-platform": ">=3.0.0"},
        "extensionPoints": {
            "widgets": [
                {
                    "id": "weekly_briefing",
                    "title": "WEEKLY DIGEST",
                    "entry": "widget.html",
                    "icon": "psychology",
                    "color": "#4DE086",
                    "supportedSizes": ["compact", "small", "medium", "large"],
                    "defaultSize": "medium"
                }
            ],
            "kitTile": {
                "label": "DIGEST",
                "icon": "psychology",
                "color": "#4DE086"
            },
            "page": {
                "entry": "index.html",
                "title": "Weekly Digest"
            },
            "settings": {
                "entry": "settings.html"
            }
        }
    },
    "com.v07.tasks": {
        "id": "com.v07.tasks",
        "name": "Sovereign Tasks",
        "version": "3.0.0",
        "apiVersion": "3.0",
        "description": "Priority queue, tag filtering, deadlines, and unhurried daily step checklists.",
        "publisher": {"name": "V07 Core Team", "url": "https://v07.app"},
        "permissions": ["storage:isolated", "events:listen", "events:emit", "notifications:schedule"],
        "capabilities": ["ui:widget", "ui:kitTile", "ui:page"],
        "dependencies": {"v07-platform": ">=3.0.0"},
        "extensionPoints": {
            "widgets": [
                {
                    "id": "tasks_today",
                    "title": "TASKS & DIRECTIVES",
                    "entry": "widget.html",
                    "icon": "checklist",
                    "color": "#5CE1E6",
                    "supportedSizes": ["compact", "small", "medium", "large"],
                    "defaultSize": "medium"
                }
            ],
            "kitTile": {
                "label": "TASKS",
                "icon": "checklist",
                "color": "#5CE1E6"
            },
            "page": {
                "entry": "index.html",
                "title": "Sovereign Tasks"
            },
            "settings": {
                "entry": "settings.html"
            }
        }
    },
    "com.v07.timetracker": {
        "id": "com.v07.timetracker",
        "name": "Focus Flow",
        "version": "3.0.0",
        "apiVersion": "3.0",
        "description": "Stopwatch, Pomodoro intervals, category tags, and historical focus telemetry.",
        "publisher": {"name": "V07 Core Team", "url": "https://v07.app"},
        "permissions": ["storage:isolated", "events:listen", "events:emit", "telemetry:read"],
        "capabilities": ["ui:widget", "ui:kitTile", "ui:page"],
        "dependencies": {"v07-platform": ">=3.0.0"},
        "extensionPoints": {
            "widgets": [
                {
                    "id": "timer_widget",
                    "title": "FOCUS STOPWATCH",
                    "entry": "widget.html",
                    "icon": "timer",
                    "color": "#5CE1E6",
                    "supportedSizes": ["compact", "small", "medium", "large"],
                    "defaultSize": "medium"
                }
            ],
            "kitTile": {
                "label": "FOCUS",
                "icon": "timer",
                "color": "#5CE1E6"
            },
            "page": {
                "entry": "index.html",
                "title": "Focus Flow"
            },
            "settings": {
                "entry": "settings.html"
            }
        }
    },
    "v07-template": {
        "id": "com.v07.template",
        "name": "Companion Template",
        "version": "3.0.0",
        "apiVersion": "3.0",
        "description": "Standard starter template for building high-craft offline V07 Sanctuary plugins.",
        "publisher": {"name": "V07 Core Team", "url": "https://v07.app"},
        "permissions": ["storage:isolated", "events:listen", "events:emit"],
        "capabilities": ["ui:widget", "ui:kitTile", "ui:page"],
        "dependencies": {"v07-platform": ">=3.0.0"},
        "extensionPoints": {
            "widgets": [
                {
                    "id": "template_widget",
                    "title": "COMPANION WIDGET",
                    "entry": "widget.html",
                    "icon": "extension",
                    "color": "#4DE086",
                    "supportedSizes": ["small", "medium"],
                    "defaultSize": "medium"
                }
            ],
            "kitTile": {
                "label": "TEMPLATE",
                "icon": "extension",
                "color": "#4DE086"
            },
            "page": {
                "entry": "index.html",
                "title": "Companion Template"
            },
            "settings": {
                "entry": "settings.html"
            }
        }
    }
}

for root_dir in PLUGIN_ROOTS:
    if not os.path.exists(root_dir):
        continue
    for p_folder, spec in SPECS.items():
        folder_path = os.path.join(root_dir, p_folder)
        if not os.path.exists(folder_path):
            os.makedirs(folder_path, exist_ok=True)

        # 1. Write compliant manifest.json
        manifest_data = {
            "$schema": "https://v07.app/schemas/v3/plugin-manifest.json",
            "id": spec["id"],
            "name": spec["name"],
            "version": spec["version"],
            "apiVersion": spec["apiVersion"],
            "description": spec["description"],
            "publisher": spec["publisher"],
            "permissions": spec["permissions"],
            "capabilities": spec["capabilities"],
            "dependencies": spec["dependencies"],
            "extensionPoints": spec["extensionPoints"],
            # Root backwards-compatibility aliases
            "entry": spec["extensionPoints"]["page"]["entry"],
            "settings": spec["extensionPoints"].get("settings", {}).get("entry", "settings.html"),
            "nav": {
                "icon": spec["extensionPoints"]["kitTile"]["icon"],
                "label": spec["extensionPoints"]["kitTile"]["label"],
                "color": spec["extensionPoints"]["kitTile"]["color"]
            },
            "widgets": [
                {
                    "id": w["id"],
                    "title": w["title"],
                    "path": w["entry"],
                    "entry": w["entry"],
                    "icon": w.get("icon", spec["extensionPoints"]["kitTile"]["icon"]),
                    "color": w.get("color", spec["extensionPoints"]["kitTile"]["color"]),
                    "supportedSizes": w.get("supportedSizes", ["compact", "small", "medium", "large"]),
                    "defaultSize": w.get("defaultSize", "medium")
                }
                for w in spec["extensionPoints"].get("widgets", [])
            ]
        }
        if "config" in spec:
            manifest_data["config"] = spec["config"]

        manifest_file = os.path.join(folder_path, "manifest.json")
        with open(manifest_file, 'w', encoding='utf-8') as f:
            json.dump(manifest_data, f, indent=2)

        # 2. Write v07-sdk.js to plugin folder
        sdk_target = os.path.join(folder_path, "v07-sdk.js")
        with open(sdk_target, 'w', encoding='utf-8') as f:
            f.write(SDK_CODE)

# Also update _template
for root_dir in PLUGIN_ROOTS:
    template_folder = os.path.join(root_dir, "_template")
    if os.path.exists(template_folder):
        with open(os.path.join(template_folder, "v07-sdk.js"), 'w', encoding='utf-8') as f:
            f.write(SDK_CODE)

# Re-create v07-template.zip with latest spec
for root_dir in PLUGIN_ROOTS:
    src_folder = os.path.join(root_dir, "v07-template")
    zip_path = os.path.join(root_dir, "v07-template.zip")
    if os.path.exists(src_folder):
        with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
            for root, _, files in os.walk(src_folder):
                for file in files:
                    full_p = os.path.join(root, file)
                    rel_p = os.path.relpath(full_p, src_folder)
                    zipf.write(full_p, rel_p)

print("Synchronized all plugins to V3 Plugin Specification!")
