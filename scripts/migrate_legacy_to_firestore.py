#!/usr/bin/env python3
"""
Migrate all Legacy Data from legacy_data/ to Firestore for user:
prabal.pks@gmail.com (UID: 5fwVgpra4pRzB1QqMWb9LSZSqrp2)

Architecture:
- Core: sessions, tasks, thoughts, daytracker, appusage
- Plugins: plugins/{pluginId}/events & plugin_storage/{pluginId}
- Sovereign Ledger: ledger
- Dual-read compatibility: events (tagged with _v3_migrated: True)
- Dual-alias compatibility: com.v07.running & com.v07.fitness
- System: system/sync_meta, system/plugins
"""

import os
import sys
import json
import re
import time
import argparse
from datetime import datetime, timezone, date
import requests

BASE_DIR = r"D:\software\V07-Android"
LEGACY_DIR = os.path.join(BASE_DIR, "legacy_data")
EVENTS_DIR = os.path.join(LEGACY_DIR, "Data", "events")
APP_USAGE_DIR = os.path.join(LEGACY_DIR, "app_usage")
GITA_DIR = os.path.join(LEGACY_DIR, "Data", "gita")

TARGET_EMAIL = "prabal.pks@gmail.com"
TARGET_UID = "5fwVgpra4pRzB1QqMWb9LSZSqrp2"
FIREBASE_PROJECT = "v07-dashboard"

# Known habit metadata
HABIT_KNOWN_META = {
    "ssb": ("SSB Practice", "⚔️"),
    "running": ("Running", "🏃"),
    "dsa": ("DSA Problem Solving", "💻"),
    "content_creation": ("Content Creation", "✍️"),
    "chess": ("Chess Training", "♟️"),
    "development": ("Development", "⚡"),
    "meditation": ("Meditation", "🧘"),
    "h_meditation": ("Meditation", "🧘"),
    "h_dsa": ("DSA Problem Solving", "💻"),
    "pushups": ("Pushups", "💪"),
    "Pushups": ("Pushups", "💪"),
    "hmudx8p39": ("Pushups", "💪"),
    "hmudmiwt3": ("Daily Routine", "✨"),
    "hmud3qf88": ("Mindfulness", "🌿"),
    "hmud3qz2i": ("Deep Study", "📚")
}

def parse_line(line):
    line = line.strip()
    if not line:
        return None
    idx = line.find('{')
    if idx == -1:
        return None
    try:
        return json.loads(line[idx:])
    except Exception:
        return None

def parse_timestamp(val):
    if not val:
        return int(datetime.now().timestamp() * 1000)
    if isinstance(val, (int, float)):
        if val < 2000000000:
            return int(val * 1000)
        return int(val)
    s = str(val).strip()
    if s.isdigit():
        num = int(s)
        if num < 2000000000:
            return num * 1000
        return num
    try:
        s_clean = s.replace('Z', '+00:00')
        dt = datetime.fromisoformat(s_clean)
        return int(dt.timestamp() * 1000)
    except Exception:
        try:
            dt = datetime.strptime(s[:10], "%Y-%m-%d")
            return int(dt.timestamp() * 1000)
        except Exception:
            return int(datetime.now().timestamp() * 1000)

def parse_date_str(val):
    if not val:
        return date.today().isoformat()
    s = str(val).strip()
    if re.match(r'^\d{4}-\d{2}-\d{2}$', s):
        return s
    ts = parse_timestamp(val)
    return datetime.fromtimestamp(ts / 1000.0).strftime("%Y-%m-%d")

def format_ts(ts):
    try:
        return datetime.fromtimestamp(ts / 1000.0).strftime("%Y-%m-%d %H:%M")
    except Exception:
        return ""

def format_seconds(sec):
    sec = max(0, int(sec))
    hrs = sec // 3600
    mins = (sec % 3600) // 60
    s = sec % 60
    if hrs > 0:
        return f"{hrs}h {mins:02d}m"
    if mins > 0:
        return f"{mins}m {s:02d}s"
    return f"{s}s"

def format_pace(pace_sec_km):
    if not pace_sec_km or pace_sec_km <= 0 or pace_sec_km > 3600:
        return "--'--\"/km"
    mins = int(pace_sec_km // 60)
    secs = int(round(pace_sec_km % 60))
    if secs == 60:
        mins += 1
        secs = 0
    return f"{mins}'{secs:02d}\"/km"

def to_firestore_value(val):
    if val is None:
        return {"nullValue": None}
    elif isinstance(val, bool):
        return {"booleanValue": val}
    elif isinstance(val, int):
        return {"integerValue": str(val)}
    elif isinstance(val, float):
        return {"doubleValue": val}
    elif isinstance(val, str):
        return {"stringValue": val}
    elif isinstance(val, list):
        return {"arrayValue": {"values": [to_firestore_value(v) for v in val]}}
    elif isinstance(val, dict):
        return {"mapValue": {"fields": {k: to_firestore_value(v) for k, v in val.items()}}}
    else:
        return {"stringValue": str(val)}

def to_firestore_doc(data):
    return {k: to_firestore_value(v) for k, v in data.items()}

class LegacyDataCollector:
    def __init__(self):
        self.now_ms = int(time.time() * 1000)
        self.iso_now = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
        self.sessions = []
        self.tasks = []
        self.thoughts = []
        self.milestones = []
        self.appusage = []
        self.running = []
        self.habits_events = []
        self.habits_storage = {}
        self.vitals = []
        self.gita = []
        self.reader = []
        self.gamify = []
        self.other_events = []
        self.ledger = []
        self.legacy_events = []

    def collect_all(self):
        print("Parsing legacy data files from:", LEGACY_DIR)
        self._collect_sessions()
        self._collect_tasks()
        self._collect_thoughts()
        self._collect_milestones()
        self._collect_appusage()
        self._collect_running()
        self._collect_habits()
        self._collect_vitals()
        self._collect_gita()
        self._collect_reader()
        self._collect_gamify()
        self._collect_other()
        self._build_ledger_and_legacy_mirror()

    def _collect_sessions(self):
        sess_file = os.path.join(EVENTS_DIR, "sessions.jsonl")
        if not os.path.exists(sess_file):
            return
        raw_sessions = []
        with open(sess_file, "r", encoding="utf-8") as f:
            for i, line in enumerate(f):
                obj = parse_line(line)
                if obj:
                    raw_sessions.append((i, obj))

        ended_starts = set()
        for i, obj in raw_sessions:
            if obj.get("type") == "end":
                s_ts_str = obj.get("start_ts")
                if s_ts_str:
                    ended_starts.add(s_ts_str)

        for i, obj in raw_sessions:
            t = obj.get("type")
            start_ts = parse_timestamp(obj.get("start_ts") or obj.get("start_time") or obj.get("ts"))
            d_str = parse_date_str(obj.get("date") or obj.get("start_ts") or obj.get("ts"))

            if t == "start":
                raw_s_ts = obj.get("ts")
                if raw_s_ts and raw_s_ts in ended_starts:
                    continue
                end_ts = None
                dur_sec = 0
            else:
                end_ts = parse_timestamp(obj.get("end_time") or obj.get("ts"))
                dur = obj.get("duration_sec") or obj.get("duration") or 0
                if "duration_sec" in obj:
                    dur_sec = int(obj["duration_sec"])
                elif dur > 0:
                    dur_sec = int(dur * 60)
                elif end_ts and start_ts and end_ts > start_ts:
                    dur_sec = max(0, int((end_ts - start_ts) / 1000))
                else:
                    dur_sec = 0

            activity = (obj.get("activity") or "Focus").strip()
            title = (obj.get("title") or "").strip()
            notes = (obj.get("notes") or obj.get("note") or "").strip()
            full_notes = f"{title} - {notes}" if title and notes else (title or notes)

            sid = obj.get("id") or f"s_{start_ts}_{i}"
            if not sid.startswith("s_"):
                sid = f"s_{sid}"

            self.sessions.append({
                "id": sid,
                "activity": activity,
                "title": title or activity,
                "date": d_str,
                "startTime": start_ts,
                "endTime": end_ts or (start_ts + dur_sec * 1000 if dur_sec > 0 else start_ts),
                "durationSec": dur_sec,
                "duration_sec": dur_sec,
                "duration_min": round(dur_sec / 60.0, 1),
                "notes": full_notes,
                "updated_at": self.now_ms,
                "_syncedAt": self.iso_now
            })

    def _collect_tasks(self):
        seen_task_ids = set()
        for t_file in ["tasks.jsonl", "tasks_ui.jsonl", "dsa.jsonl"]:
            path = os.path.join(EVENTS_DIR, t_file)
            if not os.path.exists(path):
                continue
            is_dsa = "dsa" in t_file
            with open(path, "r", encoding="utf-8") as f:
                for i, line in enumerate(f):
                    obj = parse_line(line)
                    if not obj:
                        continue
                    tid = obj.get("id") or obj.get("task_id")
                    if is_dsa:
                        topic = obj.get("topic", "DSA")
                        prob = obj.get("problem", "Problem")
                        desc = obj.get("description", "")
                        title = f"DSA: {topic} - {prob}" + (f" ({desc})" if desc else "")
                        prio = "HIGH"
                        is_done = bool(obj.get("completed", False))
                        created_ts = parse_timestamp(obj.get("ts") or obj.get("date"))
                        d_str = parse_date_str(obj.get("date") or obj.get("ts"))
                        deadline = None
                    else:
                        title = obj.get("title") or obj.get("text") or ""
                        title = re.sub(r"^-\s*\[[ xX]\]\s*", "", title).strip()
                        if not title:
                            continue
                        prio_raw = (obj.get("priority") or "MEDIUM").upper()
                        if prio_raw in ["HIGH", "P1", "RED"]:
                            prio = "HIGH"
                        elif prio_raw in ["LOW", "P3", "GRAY"]:
                            prio = "LOW"
                        else:
                            prio = "MEDIUM"
                        is_done = (
                            obj.get("done") is True or
                            obj.get("status") in ["completed", "done"] or
                            obj.get("action") == "done"
                        )
                        created_ts = parse_timestamp(obj.get("ts") or obj.get("date") or obj.get("created_at"))
                        d_str = parse_date_str(obj.get("date") or obj.get("ts") or obj.get("created_at"))
                        deadline = obj.get("deadline") or None

                    if not tid:
                        tid = f"t_{created_ts}_{i}"
                    elif not tid.startswith("t_"):
                        tid = f"t_{tid}"

                    if tid in seen_task_ids:
                        continue
                    seen_task_ids.add(tid)

                    status = "completed" if is_done else "pending"
                    self.tasks.append({
                        "id": tid,
                        "title": title,
                        "date": d_str,
                        "priority": prio,
                        "status": status,
                        "createdAt": created_ts,
                        "completedAt": created_ts if is_done else None,
                        "deadline": deadline,
                        "updated_at": self.now_ms,
                        "_syncedAt": self.iso_now
                    })

    def _collect_thoughts(self):
        seen_thought_ids = set()
        for th_file in ["thought.jsonl", "pinned_thoughts.jsonl", "thought_ui.jsonl"]:
            path = os.path.join(EVENTS_DIR, th_file)
            if not os.path.exists(path):
                continue
            is_pinned_file = "pinned" in th_file
            with open(path, "r", encoding="utf-8") as f:
                for i, line in enumerate(f):
                    obj = parse_line(line)
                    if not obj:
                        continue
                    th_id = obj.get("id")
                    content = (obj.get("text") or obj.get("content") or "").strip()
                    if not content:
                        continue
                    ts = parse_timestamp(obj.get("ts") or obj.get("date"))
                    d_str = parse_date_str(obj.get("date") or obj.get("ts"))
                    topic = (obj.get("topic") or "General").strip()
                    pinned = is_pinned_file or bool(obj.get("pinned", False))

                    if not th_id:
                        th_id = f"th_{ts}_{i}"
                    elif not th_id.startswith("th_") and not th_id.startswith("t_"):
                        th_id = f"th_{th_id}"

                    if th_id in seen_thought_ids:
                        continue
                    seen_thought_ids.add(th_id)

                    self.thoughts.append({
                        "id": th_id,
                        "timestamp": ts,
                        "date": d_str,
                        "topic": topic,
                        "content": content,
                        "pinned": pinned,
                        "updated_at": self.now_ms,
                        "_syncedAt": self.iso_now
                    })

    def _collect_milestones(self):
        dt_path = os.path.join(LEGACY_DIR, "day_tracker.jsonl")
        if not os.path.exists(dt_path):
            return
        with open(dt_path, "r", encoding="utf-8") as f:
            for i, line in enumerate(f):
                obj = parse_line(line)
                if not obj:
                    continue
                title = (obj.get("title") or "Milestone").strip()
                t_date = parse_date_str(obj.get("date") or obj.get("target_date"))
                mid = obj.get("id") or f"dt_{i}"
                if not mid.startswith("dt_") and not mid.startswith("mrk"):
                    mid = f"dt_{mid}"
                created_ts = parse_timestamp(obj.get("ts") or obj.get("created_at"))
                pinned = bool(obj.get("pinned", False))
                hour = int(obj.get("hour", 0))

                self.milestones.append({
                    "id": mid,
                    "title": title,
                    "targetDate": t_date,
                    "date": t_date,
                    "hour": hour,
                    "pinned": pinned,
                    "createdAt": created_ts,
                    "updated_at": self.now_ms,
                    "_syncedAt": self.iso_now
                })

    def _collect_appusage(self):
        if not os.path.exists(APP_USAGE_DIR):
            return
        files = sorted(os.listdir(APP_USAGE_DIR))
        for fname in files:
            if not fname.endswith(".json"):
                continue
            date_match = re.search(r"\d{4}-\d{2}-\d{2}", fname)
            d_str = date_match.group(0) if date_match else fname.replace(".json", "")
            fpath = os.path.join(APP_USAGE_DIR, fname)
            try:
                with open(fpath, "r", encoding="utf-8") as f:
                    u_data = json.load(f)
                    total_sec = 0
                    apps = []
                    hourly_mins = [0] * 24

                    if isinstance(u_data, dict):
                        raw_hourly = u_data.get("hourly_usage")
                        if isinstance(raw_hourly, list):
                            for h_item in raw_hourly:
                                if isinstance(h_item, dict):
                                    h_idx = int(h_item.get("hour", 0))
                                    if 0 <= h_idx < 24:
                                        h_sec = h_item.get("duration_sec") or (h_item.get("duration_min", 0) * 60)
                                        hourly_mins[h_idx] = int(min(60, round(h_sec / 60.0)))

                        usage_list = u_data.get("usage")
                        if isinstance(usage_list, list):
                            for item in usage_list:
                                if not isinstance(item, dict):
                                    continue
                                pkg = item.get("package") or item.get("package_name") or "unknown"
                                name = item.get("app") or item.get("app_name") or pkg.split(".")[-1].title()
                                dur_sec = item.get("total_duration_sec") or item.get("duration_sec") or 0
                                dur_ms = int(dur_sec * 1000)
                                launches = int(item.get("launch_count") or 1)
                                pct = float(item.get("percentage") or 0.0)
                                cat = item.get("category", "Productivity")
                                apps.append({
                                    "package": pkg,
                                    "package_name": pkg,
                                    "app": name,
                                    "app_name": name,
                                    "duration_sec": int(dur_sec),
                                    "duration_ms": dur_ms,
                                    "launch_count": launches,
                                    "percentage": pct,
                                    "category": cat
                                })
                            summary_obj = u_data.get("summary") or {}
                            total_sec = summary_obj.get("total_screen_time_sec") or sum(a["duration_sec"] for a in apps)
                        else:
                            app_dict = u_data.get("apps")
                            if isinstance(app_dict, dict):
                                for pkg, info in app_dict.items():
                                    if isinstance(info, dict):
                                        dur_sec = (info.get("total_time_ms") or info.get("duration_ms") or 0) / 1000.0
                                        name = info.get("app_name") or pkg.split(".")[-1].title()
                                        cat = info.get("category", "Productivity")
                                    elif isinstance(info, (int, float)):
                                        dur_sec = float(info) / 1000.0 if info > 100000 else float(info)
                                        name = pkg.split(".")[-1].title()
                                        cat = "Productivity"
                                    else:
                                        continue
                                    apps.append({
                                        "package": pkg,
                                        "package_name": pkg,
                                        "app": name,
                                        "app_name": name,
                                        "duration_sec": int(dur_sec),
                                        "duration_ms": int(dur_sec * 1000),
                                        "launch_count": 1,
                                        "percentage": 0.0,
                                        "category": cat
                                    })
                                total_sec = sum(a["duration_sec"] for a in apps)

                    apps.sort(key=lambda a: a["duration_sec"], reverse=True)
                    hourly_objects = [{"hour": h, "duration_sec": mins * 60} for h, mins in enumerate(hourly_mins) if mins > 0]

                    self.appusage.append({
                        "id": d_str,
                        "date": d_str,
                        "total_duration_sec": int(total_sec),
                        "total_duration_ms": int(total_sec * 1000),
                        "summary": {
                            "total_screen_time_sec": int(total_sec),
                            "unique_apps_used": len(apps),
                            "date": d_str
                        },
                        "usage": apps,
                        "all_apps": apps,
                        "hourly_usage": hourly_objects,
                        "updated_at": self.now_ms,
                        "_syncedAt": self.iso_now
                    })
            except Exception as e:
                pass

    def _collect_running(self):
        run_file = os.path.join(EVENTS_DIR, "running.jsonl")
        if not os.path.exists(run_file):
            return
        with open(run_file, "r", encoding="utf-8") as f:
            for i, line in enumerate(f):
                obj = parse_line(line)
                if not obj:
                    continue
                dist = float(obj.get("distance_km") or obj.get("km") or 0.0)
                if dist <= 0:
                    continue
                mins_raw = float(obj.get("minutes") or 0.0)
                dur_min_raw = float(obj.get("duration_min") or 0.0)

                if "duration_sec" in obj:
                    dur_sec = int(obj["duration_sec"])
                elif dur_min_raw >= 60.0 or dur_min_raw > mins_raw:
                    dur_sec = int(dur_min_raw)
                elif mins_raw > 0:
                    dur_sec = int(mins_raw * 60)
                else:
                    dur_sec = int(dur_min_raw * 60) if dur_min_raw > 0 else 0

                pace_str = format_pace(dur_sec / dist if (dist > 0 and dur_sec > 0) else 0)
                pace_sec = int(dur_sec / dist) if (dist > 0 and dur_sec > 0) else 0
                ts = parse_timestamp(obj.get("ts") or obj.get("date"))
                d_str = parse_date_str(obj.get("date") or obj.get("ts"))
                rid = obj.get("id") or f"run_{ts}_{i}"
                if not rid.startswith("run_") and not rid.startswith("r_"):
                    rid = f"run_{rid}"

                t = (obj.get("title") or "").strip()
                n = (obj.get("notes") or obj.get("note") or "").strip()
                rtype = (obj.get("run_type") or "").strip()

                note_parts = []
                if t and t.lower() != "run":
                    note_parts.append(t)
                if rtype and rtype.lower() not in ["run", "easy"] or (rtype and not note_parts):
                    note_parts.append(f"{rtype.title()} run")
                if n and n != t and n != "":
                    note_parts.append(n)

                final_note = " - ".join(note_parts)
                display_title = t if (t and t.lower() != "run") else f"{dist:.2f} km Run"

                run_payload = {
                    "distance": round(dist, 2),
                    "duration": round(dur_sec / 60.0, 1),
                    "notes": final_note,
                    "date": d_str,
                    "ts": ts,
                    "pace": pace_str
                }
                self.running.append({
                    "id": rid,
                    "pluginId": "com.v07.running",
                    "category": "running",
                    "date": d_str,
                    "timestamp": ts,
                    "distance": round(dist, 2),
                    "distanceKm": round(dist, 2),
                    "distance_km": round(dist, 2),
                    "duration": round(dur_sec / 60.0, 1),
                    "durationSec": dur_sec,
                    "duration_sec": dur_sec,
                    "duration_min": round(dur_sec / 60.0, 1),
                    "pace": pace_str,
                    "pace_sec_km": pace_sec,
                    "title": display_title,
                    "note": final_note,
                    "notes": final_note,
                    "run_type": rtype or "easy",
                    "payloadJson": json.dumps(run_payload),
                    "updated_at": self.now_ms,
                    "_syncedAt": self.iso_now
                })

    def _collect_habits(self):
        habit_defs = {}
        habit_logs = []
        seen_log_keys = set()

        for h_file in ["habits.jsonl", "habits_ui.jsonl", "hbt.jsonl"]:
            path = os.path.join(EVENTS_DIR, h_file)
            if not os.path.exists(path):
                continue
            with open(path, "r", encoding="utf-8") as f:
                for line in f:
                    obj = parse_line(line)
                    if not obj:
                        continue

                    # Pattern A: Definition block with 'habits' list
                    if obj.get("kind") == "def" and isinstance(obj.get("habits"), list):
                        for h_sub in obj["habits"]:
                            h_sub_id = h_sub.get("id") or f"hbt_{h_sub.get('name', 'habit').lower()}"
                            raw_name = h_sub.get("name") or h_sub_id
                            h_name, h_icon = HABIT_KNOWN_META.get(raw_name, HABIT_KNOWN_META.get(h_sub_id, (raw_name, "🔥")))
                            if h_sub_id not in habit_defs:
                                habit_defs[h_sub_id] = {
                                    "id": h_sub_id,
                                    "name": h_name,
                                    "title": h_name,
                                    "icon": h_icon,
                                    "emoji": h_icon,
                                    "cadence": "daily",
                                    "target_freq": "daily",
                                    "streak": 0,
                                    "history": {}
                                }

                    # Pattern B: habits.jsonl with 'activity'
                    act = obj.get("activity")
                    if act:
                        h_name, h_icon = HABIT_KNOWN_META.get(act, (act.replace("_", " ").title(), "🔥"))
                        hid = f"hbt_{act.lower()}"
                        if hid not in habit_defs:
                            habit_defs[hid] = {
                                "id": hid,
                                "name": h_name,
                                "title": h_name,
                                "icon": h_icon,
                                "emoji": h_icon,
                                "cadence": "daily",
                                "target_freq": "daily",
                                "streak": 0,
                                "history": {}
                            }
                        d_str = parse_date_str(obj.get("date") or obj.get("ts"))
                        is_on = obj.get("on") is not False
                        if d_str and is_on:
                            habit_defs[hid]["history"][d_str] = True
                            log_key = f"{hid}_{d_str}"
                            if log_key not in seen_log_keys:
                                seen_log_keys.add(log_key)
                                ts = parse_timestamp(obj.get("ts") or obj.get("date"))
                                habit_logs.append({
                                    "id": f"hlog_{hid}_{d_str}",
                                    "pluginId": "com.v07.habits",
                                    "category": "habit",
                                    "habit_id": hid,
                                    "habit_name": h_name,
                                    "date": d_str,
                                    "completed": True,
                                    "value": "Done",
                                    "timestamp": ts,
                                    "updated_at": self.now_ms,
                                    "_syncedAt": self.iso_now
                                })

                    # Pattern C: hbt.jsonl with 'habit'
                    h_key = obj.get("habit")
                    if h_key:
                        clean_k = h_key.replace("h_", "")
                        h_name, h_icon = HABIT_KNOWN_META.get(h_key, HABIT_KNOWN_META.get(clean_k, (clean_k.replace("_", " ").title(), "🔥")))
                        hid = h_key if h_key.startswith("hbt_") else (f"hbt_{clean_k.lower()}" if not clean_k.startswith("hmud") else clean_k)
                        if hid not in habit_defs:
                            habit_defs[hid] = {
                                "id": hid,
                                "name": h_name,
                                "title": h_name,
                                "icon": h_icon,
                                "emoji": h_icon,
                                "cadence": "daily",
                                "target_freq": "daily",
                                "streak": 0,
                                "history": {}
                            }
                        d_str = parse_date_str(obj.get("date") or obj.get("ts"))
                        is_on = obj.get("on") is not False
                        if d_str and is_on:
                            habit_defs[hid]["history"][d_str] = True
                            log_key = f"{hid}_{d_str}"
                            if log_key not in seen_log_keys:
                                seen_log_keys.add(log_key)
                                ts = parse_timestamp(obj.get("ts") or obj.get("date"))
                                habit_logs.append({
                                    "id": f"hlog_{hid}_{d_str}",
                                    "pluginId": "com.v07.habits",
                                    "category": "habit",
                                    "habit_id": hid,
                                    "habit_name": h_name,
                                    "date": d_str,
                                    "completed": True,
                                    "value": "Done",
                                    "timestamp": ts,
                                    "updated_at": self.now_ms,
                                    "_syncedAt": self.iso_now
                                })

        for hid, h in habit_defs.items():
            hist = h.get("history", {})
            h["streak"] = len(hist)

        self.habits_events = habit_logs
        self.habits_storage = {
            "definitions": habit_defs,
            "list": list(habit_defs.values()),
            "updated_at": self.now_ms
        }

    def _collect_vitals(self):
        for v_file, v_type in [("mood.jsonl", "mood"), ("mood_ui.jsonl", "mood"), ("energy.jsonl", "energy")]:
            path = os.path.join(EVENTS_DIR, v_file)
            if not os.path.exists(path):
                continue
            with open(path, "r", encoding="utf-8") as f:
                for i, line in enumerate(f):
                    obj = parse_line(line)
                    if not obj:
                        continue
                    raw_score = float(obj.get(f"{v_type}_score") or obj.get("score") or obj.get("val") or 3.0)
                    score = raw_score / 2.0 if raw_score > 5.0 else raw_score
                    score = max(1.0, min(5.0, score))
                    ts = parse_timestamp(obj.get("ts") or obj.get("date"))
                    d_str = parse_date_str(obj.get("date") or obj.get("ts"))
                    vid = obj.get("id") or f"vit_{v_type}_{ts}_{i}"
                    if not vid.startswith("vit_"):
                        vid = f"vit_{vid}"
                    note = (obj.get("note") or obj.get("text") or "").strip()

                    self.vitals.append({
                        "id": vid,
                        "pluginId": "com.v07.vitals",
                        "category": "vital",
                        "type": v_type,
                        "score": round(score, 1),
                        "note": note,
                        "date": d_str,
                        "timestamp": ts,
                        "updated_at": self.now_ms,
                        "_syncedAt": self.iso_now
                    })

    def _collect_gita(self):
        # 1. gita.jsonl
        gita_event_file = os.path.join(EVENTS_DIR, "gita.jsonl")
        if os.path.exists(gita_event_file):
            with open(gita_event_file, "r", encoding="utf-8") as f:
                for i, line in enumerate(f):
                    obj = parse_line(line)
                    if not obj:
                        continue
                    gid = obj.get("id") or f"g_{i}"
                    vid = obj.get("verse_id") or "1.1"
                    ts = parse_timestamp(obj.get("ts") or obj.get("date"))
                    d_str = parse_date_str(obj.get("date") or obj.get("ts"))
                    self.gita.append({
                        "id": gid,
                        "pluginId": "com.v07.gita",
                        "category": "gita",
                        "verse_id": vid,
                        "chapter": int(obj.get("chapter", 1)),
                        "verse": int(obj.get("verse", 1)),
                        "type": "read",
                        "date": d_str,
                        "timestamp": ts,
                        "updated_at": self.now_ms,
                        "_syncedAt": self.iso_now
                    })

        # 2. gita_read.jsonl
        gr_file = os.path.join(GITA_DIR, "gita_read.jsonl")
        if os.path.exists(gr_file):
            with open(gr_file, "r", encoding="utf-8") as f:
                for i, line in enumerate(f):
                    obj = parse_line(line)
                    if not obj:
                        continue
                    gid = obj.get("id") or f"gr_{i}"
                    vid = obj.get("verse_id") or "1.1"
                    ts = parse_timestamp(obj.get("ts") or obj.get("date"))
                    d_str = parse_date_str(obj.get("date") or obj.get("ts"))
                    self.gita.append({
                        "id": gid,
                        "pluginId": "com.v07.gita",
                        "category": "gita",
                        "verse_id": vid,
                        "type": "read",
                        "date": d_str,
                        "timestamp": ts,
                        "updated_at": self.now_ms,
                        "_syncedAt": self.iso_now
                    })

        # 3. gita_reviews.jsonl
        grev_file = os.path.join(GITA_DIR, "gita_reviews.jsonl")
        if os.path.exists(grev_file):
            with open(grev_file, "r", encoding="utf-8") as f:
                for i, line in enumerate(f):
                    obj = parse_line(line)
                    if not obj:
                        continue
                    gid = obj.get("id") or f"gv_{i}"
                    vid = obj.get("verse_id") or "1.1"
                    txt = obj.get("text") or ""
                    rating = int(obj.get("rating", 3))
                    ts = parse_timestamp(obj.get("ts") or obj.get("date"))
                    d_str = parse_date_str(obj.get("date") or obj.get("ts"))
                    self.gita.append({
                        "id": gid,
                        "pluginId": "com.v07.gita",
                        "category": "gita",
                        "verse_id": vid,
                        "rating": rating,
                        "text": txt,
                        "reflection": txt,
                        "type": "reflection",
                        "date": d_str,
                        "timestamp": ts,
                        "updated_at": self.now_ms,
                        "_syncedAt": self.iso_now
                    })
                    self.thoughts.append({
                        "id": gid,
                        "topic": "Gita Reflection",
                        "content": f"Gita Verse {vid}:\n{txt}",
                        "pinned": False,
                        "date": d_str,
                        "timestamp": ts,
                        "updated_at": self.now_ms,
                        "_syncedAt": self.iso_now
                    })

    def _collect_reader(self):
        shares_file = os.path.join(EVENTS_DIR, "shares.jsonl")
        if not os.path.exists(shares_file):
            return
        with open(shares_file, "r", encoding="utf-8") as f:
            for i, line in enumerate(f):
                obj = parse_line(line)
                if not obj:
                    continue
                url = obj.get("url") or obj.get("link") or obj.get("text") or ""
                if not url:
                    continue
                ts = parse_timestamp(obj.get("ts") or obj.get("date"))
                d_str = parse_date_str(obj.get("date") or obj.get("ts"))
                sid = obj.get("id") or f"sh_{ts}_{i}"
                if not sid.startswith("sh_"):
                    sid = f"sh_{sid}"
                title = obj.get("title") or url[:50]
                self.reader.append({
                    "id": sid,
                    "pluginId": "com.v07.reader",
                    "category": "reader",
                    "url": url,
                    "title": title,
                    "date": d_str,
                    "timestamp": ts,
                    "updated_at": self.now_ms,
                    "_syncedAt": self.iso_now
                })

    def _collect_gamify(self):
        gamify_file = os.path.join(EVENTS_DIR, "gamify.jsonl")
        if not os.path.exists(gamify_file):
            return
        with open(gamify_file, "r", encoding="utf-8") as f:
            for i, line in enumerate(f):
                obj = parse_line(line)
                if not obj:
                    continue
                gid = obj.get("id") or f"gam_{i}"
                ts = parse_timestamp(obj.get("ts") or obj.get("date"))
                d_str = parse_date_str(obj.get("date") or obj.get("ts"))
                kind = obj.get("kind") or "login"
                self.gamify.append({
                    "id": gid,
                    "pluginId": "com.v07.gamify",
                    "category": "gamify",
                    "kind": kind,
                    "date": d_str,
                    "timestamp": ts,
                    "updated_at": self.now_ms,
                    "_syncedAt": self.iso_now
                })

    def _collect_other(self):
        c_file = os.path.join(EVENTS_DIR, "chess.jsonl")
        if os.path.exists(c_file):
            with open(c_file, "r", encoding="utf-8") as f:
                for i, line in enumerate(f):
                    obj = parse_line(line)
                    if not obj:
                        continue
                    cid = obj.get("id") or f"c_{i}"
                    ts = parse_timestamp(obj.get("ts") or obj.get("date"))
                    d_str = parse_date_str(obj.get("date") or obj.get("ts"))
                    opp = obj.get("opponent", "Opponent")
                    res = obj.get("result", "match")
                    color = obj.get("color", "white")
                    self.other_events.append({
                        "id": cid,
                        "category": "chess",
                        "title": f"Chess vs {opp} ({res})",
                        "opponent": opp,
                        "result": res,
                        "color": color,
                        "notes": obj.get("notes", ""),
                        "date": d_str,
                        "ts": ts,
                        "updated_at": self.now_ms,
                        "_syncedAt": self.iso_now
                    })

        p_file = os.path.join(EVENTS_DIR, "panics.jsonl")
        if os.path.exists(p_file):
            with open(p_file, "r", encoding="utf-8") as f:
                for i, line in enumerate(f):
                    obj = parse_line(line)
                    if not obj:
                        continue
                    pid = obj.get("id") or f"p_{i}"
                    ts = parse_timestamp(obj.get("ts") or obj.get("date"))
                    d_str = parse_date_str(obj.get("date") or obj.get("ts"))
                    self.other_events.append({
                        "id": pid,
                        "category": "panic",
                        "title": "Panic State Triggered",
                        "note": obj.get("note", ""),
                        "time": obj.get("time", ""),
                        "time_since_last": obj.get("time_since_last", ""),
                        "date": d_str,
                        "ts": ts,
                        "updated_at": self.now_ms,
                        "_syncedAt": self.iso_now
                    })

        for m_file in ["metrics.jsonl", "metrics_ui.jsonl"]:
            path = os.path.join(EVENTS_DIR, m_file)
            if not os.path.exists(path):
                continue
            with open(path, "r", encoding="utf-8") as f:
                for i, line in enumerate(f):
                    obj = parse_line(line)
                    if not obj:
                        continue
                    mid = obj.get("id") or f"m_{i}"
                    ts = parse_timestamp(obj.get("ts") or obj.get("date"))
                    d_str = parse_date_str(obj.get("date") or obj.get("ts"))
                    note = obj.get("note", "")
                    self.other_events.append({
                        "id": mid,
                        "category": "metric",
                        "title": f"Daily Check-in: {obj.get('time', '')}",
                        "note": note,
                        "date": d_str,
                        "ts": ts,
                        "updated_at": self.now_ms,
                        "_syncedAt": self.iso_now
                    })

        for v_file in ["visited.jsonl", "visited_ui.jsonl"]:
            path = os.path.join(EVENTS_DIR, v_file)
            if not os.path.exists(path):
                continue
            with open(path, "r", encoding="utf-8") as f:
                for i, line in enumerate(f):
                    obj = parse_line(line)
                    if not obj:
                        continue
                    vid = obj.get("id") or f"v_{i}"
                    ts = parse_timestamp(obj.get("ts") or obj.get("date"))
                    d_str = parse_date_str(obj.get("date") or obj.get("ts"))
                    self.other_events.append({
                        "id": vid,
                        "category": "visited",
                        "title": "Visited Thought",
                        "thought_id": obj.get("thought_id", ""),
                        "date": d_str,
                        "ts": ts,
                        "updated_at": self.now_ms,
                        "_syncedAt": self.iso_now
                    })

    def _build_ledger_and_legacy_mirror(self):
        # 1. Sessions
        for s in self.sessions:
            lid = f"led_{s['id']}"
            title = s.get("title") or s.get("activity") or "Focus Session"
            dur_m = s.get("duration_min", 0)
            notes = s.get("notes", "")
            sub = f"{dur_m}m session" + (f" • {notes}" if notes else "")
            self.ledger.append({
                "id": lid,
                "type": "session",
                "title": title,
                "content": sub,
                "date": s["date"],
                "timestamp": s["startTime"],
                "ref_id": s["id"],
                "ref_collection": "sessions",
                "source": "legacy_migration",
                "updated_at": self.now_ms,
                "_syncedAt": self.iso_now
            })
            self.legacy_events.append({
                "id": s["id"],
                "category": "session",
                "title": title,
                "date": s["date"],
                "ts": s["startTime"],
                "durationSec": s["durationSec"],
                "meta_activity": s["activity"],
                "notes": notes,
                "_v3_migrated": True,
                "_syncedAt": self.iso_now
            })

        # 2. Tasks
        for t in self.tasks:
            lid = f"led_{t['id']}"
            prio = t.get("priority", "MEDIUM")
            status = t.get("status", "pending")
            sub = f"Priority: {prio} • Status: {status}"
            self.ledger.append({
                "id": lid,
                "type": "task",
                "title": t["title"],
                "content": sub,
                "date": t["date"],
                "timestamp": t["createdAt"],
                "ref_id": t["id"],
                "ref_collection": "tasks",
                "source": "legacy_migration",
                "updated_at": self.now_ms,
                "_syncedAt": self.iso_now
            })
            self.legacy_events.append({
                "id": t["id"],
                "category": "task",
                "title": t["title"],
                "date": t["date"],
                "ts": t["createdAt"],
                "priority": prio,
                "status": status,
                "_v3_migrated": True,
                "_syncedAt": self.iso_now
            })

        # 3. Thoughts
        for th in self.thoughts:
            lid = f"led_{th['id']}"
            self.ledger.append({
                "id": lid,
                "type": "thought",
                "title": f"Thought ({th.get('topic', 'General')})",
                "content": th["content"][:100],
                "date": th["date"],
                "timestamp": th["timestamp"],
                "ref_id": th["id"],
                "ref_collection": "thoughts",
                "source": "legacy_migration",
                "updated_at": self.now_ms,
                "_syncedAt": self.iso_now
            })
            self.legacy_events.append({
                "id": th["id"],
                "category": "thought",
                "title": f"Thought ({th.get('topic', 'General')})",
                "content": th["content"],
                "date": th["date"],
                "ts": th["timestamp"],
                "pinned": th.get("pinned", False),
                "_v3_migrated": True,
                "_syncedAt": self.iso_now
            })

        # 4. Day Tracker / Milestones
        for m in self.milestones:
            lid = f"led_{m['id']}"
            self.ledger.append({
                "id": lid,
                "type": "milestone",
                "title": m["title"],
                "content": f"Target: {m.get('targetDate', '')}",
                "date": m["date"],
                "timestamp": m["createdAt"],
                "ref_id": m["id"],
                "ref_collection": "daytracker",
                "source": "legacy_migration",
                "updated_at": self.now_ms,
                "_syncedAt": self.iso_now
            })
            self.legacy_events.append({
                "id": m["id"],
                "category": "daytracker",
                "title": m["title"],
                "date": m["date"],
                "targetDate": m.get("targetDate", ""),
                "ts": m["createdAt"],
                "_v3_migrated": True,
                "_syncedAt": self.iso_now
            })

        # 5. Running
        for r in self.running:
            lid = f"led_{r['id']}"
            sub = f"{r['distanceKm']} km • {r['pace']} • {r.get('note', '')}"
            self.ledger.append({
                "id": lid,
                "type": "run",
                "title": r["title"],
                "content": sub,
                "date": r["date"],
                "timestamp": r["timestamp"],
                "ref_id": r["id"],
                "ref_collection": "plugins/com.v07.running/events",
                "source": "legacy_migration",
                "updated_at": self.now_ms,
                "_syncedAt": self.iso_now
            })
            self.legacy_events.append({
                "id": r["id"],
                "category": "running",
                "title": r["title"],
                "date": r["date"],
                "ts": r["timestamp"],
                "distanceKm": r["distanceKm"],
                "durationSec": r["durationSec"],
                "pace": r["pace"],
                "_v3_migrated": True,
                "_syncedAt": self.iso_now
            })

        # 6. Habits
        for h in self.habits_events:
            lid = f"led_{h['id']}"
            self.ledger.append({
                "id": lid,
                "type": "habit",
                "title": f"Habit: {h['habit_name']}",
                "content": "Completed",
                "date": h["date"],
                "timestamp": h["timestamp"],
                "ref_id": h["id"],
                "ref_collection": "plugins/com.v07.habits/events",
                "source": "legacy_migration",
                "updated_at": self.now_ms,
                "_syncedAt": self.iso_now
            })
            self.legacy_events.append({
                "id": h["id"],
                "category": "habit",
                "title": f"Habit: {h['habit_name']}",
                "date": h["date"],
                "ts": h["timestamp"],
                "_v3_migrated": True,
                "_syncedAt": self.iso_now
            })

        # 7. Vitals
        for v in self.vitals:
            lid = f"led_{v['id']}"
            self.ledger.append({
                "id": lid,
                "type": "vital",
                "title": f"Vital {v['type'].title()} ({v['score']}/5)",
                "content": v.get("note", ""),
                "date": v["date"],
                "timestamp": v["timestamp"],
                "ref_id": v["id"],
                "ref_collection": "plugins/com.v07.vitals/events",
                "source": "legacy_migration",
                "updated_at": self.now_ms,
                "_syncedAt": self.iso_now
            })
            self.legacy_events.append({
                "id": v["id"],
                "category": v["type"],
                "title": f"Vital {v['type'].title()} ({v['score']}/5)",
                "date": v["date"],
                "ts": v["timestamp"],
                "score": v["score"],
                "_v3_migrated": True,
                "_syncedAt": self.iso_now
            })

        # 8. Gita
        for g in self.gita:
            lid = f"led_{g['id']}"
            vid = g.get("verse_id", "1.1")
            sub = g.get("text", "") or "Gita recitation / read"
            self.ledger.append({
                "id": lid,
                "type": "gita",
                "title": f"Gita Verse {vid}",
                "content": sub[:100],
                "date": g["date"],
                "timestamp": g["timestamp"],
                "ref_id": g["id"],
                "ref_collection": "plugins/com.v07.gita/events",
                "source": "legacy_migration",
                "updated_at": self.now_ms,
                "_syncedAt": self.iso_now
            })
            self.legacy_events.append({
                "id": g["id"],
                "category": "gita",
                "title": f"Gita Verse {vid}",
                "date": g["date"],
                "ts": g["timestamp"],
                "verse_id": vid,
                "_v3_migrated": True,
                "_syncedAt": self.iso_now
            })

        # 9. Reader
        for rd in self.reader:
            lid = f"led_{rd['id']}"
            self.ledger.append({
                "id": lid,
                "type": "reader",
                "title": rd["title"],
                "content": rd["url"],
                "date": rd["date"],
                "timestamp": rd["timestamp"],
                "ref_id": rd["id"],
                "ref_collection": "plugins/com.v07.reader/events",
                "source": "legacy_migration",
                "updated_at": self.now_ms,
                "_syncedAt": self.iso_now
            })
            self.legacy_events.append({
                "id": rd["id"],
                "category": "reader",
                "title": rd["title"],
                "url": rd["url"],
                "date": rd["date"],
                "ts": rd["timestamp"],
                "_v3_migrated": True,
                "_syncedAt": self.iso_now
            })

        # 10. Gamify
        for gm in self.gamify:
            lid = f"led_{gm['id']}"
            self.ledger.append({
                "id": lid,
                "type": "gamify",
                "title": f"Gamify: {gm['kind'].title()}",
                "content": "Daily Streak Check",
                "date": gm["date"],
                "timestamp": gm["timestamp"],
                "ref_id": gm["id"],
                "ref_collection": "plugins/com.v07.gamify/events",
                "source": "legacy_migration",
                "updated_at": self.now_ms,
                "_syncedAt": self.iso_now
            })

        # 11. Other (Chess, Panics, Metrics, Visited)
        for ot in self.other_events:
            lid = f"led_{ot['id']}"
            self.ledger.append({
                "id": lid,
                "type": ot["category"],
                "title": ot["title"],
                "content": ot.get("notes") or ot.get("note") or "",
                "date": ot["date"],
                "timestamp": ot["ts"],
                "ref_id": ot["id"],
                "ref_collection": "events",
                "source": "legacy_migration",
                "updated_at": self.now_ms,
                "_syncedAt": self.iso_now
            })
            self.legacy_events.append({
                **ot,
                "_v3_migrated": True,
                "_syncedAt": self.iso_now
            })


class FirestoreUploader:
    def __init__(self, uid=TARGET_UID, project=FIREBASE_PROJECT):
        self.uid = uid
        self.project = project
        self.access_token = self._get_access_token()
        self.headers = {
            "Authorization": f"Bearer {self.access_token}",
            "Content-Type": "application/json"
        }
        self.base_doc_prefix = f"projects/{self.project}/databases/(default)/documents/users/{self.uid}"
        self.batch_url = f"https://firestore.googleapis.com/v1/projects/{self.project}/databases/(default)/documents:batchWrite"

    def _get_access_token(self):
        cfg_path = os.path.expanduser('~/.config/configstore/firebase-tools.json')
        tokens = json.load(open(cfg_path))['tokens']
        exp = tokens.get('expires_at', 0)
        now_ms = time.time() * 1000
        if exp and exp < (now_ms + 60000):
            print("Access token near expiry, refreshing via refresh_token...")
            r = requests.post("https://oauth2.googleapis.com/token", data={
                "client_id": "563584335869-fgrhgmd47bqnekij5i8b5pr03ho85qd6.apps.googleusercontent.com",
                "grant_type": "refresh_token",
                "refresh_token": tokens["refresh_token"]
            })
            if r.status_code == 200:
                new_tokens = r.json()
                tokens["access_token"] = new_tokens["access_token"]
                tokens["expires_at"] = int((time.time() + new_tokens["expires_in"]) * 1000)
                full_cfg = json.load(open(cfg_path))
                full_cfg['tokens'] = tokens
                with open(cfg_path, 'w') as fp:
                    json.dump(full_cfg, fp)
                print("Token refreshed successfully.")
        return tokens['access_token']

    def upload_batch(self, writes_list, max_chunk=200):
        total = len(writes_list)
        success = 0
        failed = 0
        print(f"Beginning upload of {total} operations in chunks of {max_chunk}...")
        for i in range(0, total, max_chunk):
            chunk = writes_list[i : i + max_chunk]
            payload = {"writes": chunk}
            try:
                r = requests.post(self.batch_url, headers=self.headers, json=payload, timeout=60)
                if r.status_code == 200:
                    success += len(chunk)
                    print(f"  Chunk [{i+1:4d} - {min(i+len(chunk), total):4d} / {total}]: HTTP 200 OK")
                else:
                    print(f"  Batch write failed chunk [{i}:{i+len(chunk)}], status {r.status_code}: {r.text[:200]}")
                    failed += len(chunk)
            except Exception as e:
                print(f"  Batch write exception chunk [{i}:{i+len(chunk)}]: {e}")
                failed += len(chunk)
            time.sleep(0.05)
        return success, failed

def main():
    parser = argparse.ArgumentParser(description="Migrate legacy data to Firestore")
    parser.add_argument("--dry-run", action="store_true", help="Print summary without writing to Firestore")
    args = parser.parse_args()

    collector = LegacyDataCollector()
    collector.collect_all()

    summary_counts = {
        "sessions": len(collector.sessions),
        "tasks": len(collector.tasks),
        "thoughts": len(collector.thoughts),
        "milestones": len(collector.milestones),
        "appusage_days": len(collector.appusage),
        "running": len(collector.running),
        "habits_events": len(collector.habits_events),
        "vitals": len(collector.vitals),
        "gita": len(collector.gita),
        "reader": len(collector.reader),
        "gamify": len(collector.gamify),
        "other_events": len(collector.other_events),
        "total_ledger": len(collector.ledger),
        "legacy_events_mirror": len(collector.legacy_events)
    }

    print("\n" + "=" * 60)
    print(f"MIGRATION PLAN FOR: {TARGET_EMAIL}")
    print(f"Target UID: {TARGET_UID}")
    print(f"Firebase Project: {FIREBASE_PROJECT}")
    print("=" * 60)
    print(json.dumps(summary_counts, indent=2))
    print("=" * 60)

    if args.dry_run:
        print("\n[DRY RUN COMPLETED] No changes written to Firestore.")
        return

    uploader = FirestoreUploader(uid=TARGET_UID, project=FIREBASE_PROJECT)

    writes = []

    # 1. Sessions -> users/{uid}/sessions
    for s in collector.sessions:
        doc_path = f"{uploader.base_doc_prefix}/sessions/{s['id']}"
        writes.append({"update": {"name": doc_path, "fields": to_firestore_doc(s)}})

    # 2. Tasks -> users/{uid}/tasks
    for t in collector.tasks:
        doc_path = f"{uploader.base_doc_prefix}/tasks/{t['id']}"
        writes.append({"update": {"name": doc_path, "fields": to_firestore_doc(t)}})

    # 3. Thoughts -> users/{uid}/thoughts
    for th in collector.thoughts:
        doc_path = f"{uploader.base_doc_prefix}/thoughts/{th['id']}"
        writes.append({"update": {"name": doc_path, "fields": to_firestore_doc(th)}})

    # 4. Day Tracker -> users/{uid}/daytracker
    for dt in collector.milestones:
        doc_path = f"{uploader.base_doc_prefix}/daytracker/{dt['id']}"
        writes.append({"update": {"name": doc_path, "fields": to_firestore_doc(dt)}})

    # 5. App Usage -> users/{uid}/appusage
    for u in collector.appusage:
        doc_path = f"{uploader.base_doc_prefix}/appusage/{u['id']}"
        writes.append({"update": {"name": doc_path, "fields": to_firestore_doc(u)}})

    # 6. Plugins Events
    # Running (both com.v07.running and com.v07.fitness alias)
    for r in collector.running:
        doc_path_run = f"{uploader.base_doc_prefix}/plugins/com.v07.running/events/{r['id']}"
        writes.append({"update": {"name": doc_path_run, "fields": to_firestore_doc(r)}})

        r_fit = dict(r)
        r_fit["pluginId"] = "com.v07.fitness"
        doc_path_fit = f"{uploader.base_doc_prefix}/plugins/com.v07.fitness/events/{r['id']}"
        writes.append({"update": {"name": doc_path_fit, "fields": to_firestore_doc(r_fit)}})

    # Habits events
    for h in collector.habits_events:
        doc_path = f"{uploader.base_doc_prefix}/plugins/com.v07.habits/events/{h['id']}"
        writes.append({"update": {"name": doc_path, "fields": to_firestore_doc(h)}})

    # Habits Storage (both plugins/{id}/storage and plugin_storage/{id})
    doc_path_hs1 = f"{uploader.base_doc_prefix}/plugins/com.v07.habits/storage/definitions"
    writes.append({"update": {"name": doc_path_hs1, "fields": to_firestore_doc(collector.habits_storage)}})

    doc_path_hs2 = f"{uploader.base_doc_prefix}/plugin_storage/com.v07.habits"
    writes.append({"update": {"name": doc_path_hs2, "fields": to_firestore_doc(collector.habits_storage)}})

    # Vitals
    for v in collector.vitals:
        doc_path = f"{uploader.base_doc_prefix}/plugins/com.v07.vitals/events/{v['id']}"
        writes.append({"update": {"name": doc_path, "fields": to_firestore_doc(v)}})

    # Gita
    for g in collector.gita:
        doc_path = f"{uploader.base_doc_prefix}/plugins/com.v07.gita/events/{g['id']}"
        writes.append({"update": {"name": doc_path, "fields": to_firestore_doc(g)}})

    # Reader
    for rd in collector.reader:
        doc_path = f"{uploader.base_doc_prefix}/plugins/com.v07.reader/events/{rd['id']}"
        writes.append({"update": {"name": doc_path, "fields": to_firestore_doc(rd)}})

    # Gamify
    for gm in collector.gamify:
        doc_path = f"{uploader.base_doc_prefix}/plugins/com.v07.gamify/events/{gm['id']}"
        writes.append({"update": {"name": doc_path, "fields": to_firestore_doc(gm)}})

    # 7. Sovereign System Ledger
    for led in collector.ledger:
        doc_path = f"{uploader.base_doc_prefix}/ledger/{led['id']}"
        writes.append({"update": {"name": doc_path, "fields": to_firestore_doc(led)}})

    # 8. Dual-Read Legacy Events Mirror
    for ev in collector.legacy_events:
        doc_path = f"{uploader.base_doc_prefix}/events/{ev['id']}"
        writes.append({"update": {"name": doc_path, "fields": to_firestore_doc(ev)}})

    # 9. System metadata & plugins
    sync_meta = {
        "schema_version": 3,
        "legacy_migrated": True,
        "migrated_email": TARGET_EMAIL,
        "migrated_at": collector.iso_now,
        "counts": summary_counts
    }
    writes.append({
        "update": {
            "name": f"{uploader.base_doc_prefix}/system/sync_meta",
            "fields": to_firestore_doc(sync_meta)
        }
    })

    system_plugins = {
        "plugins": [
            {"id": "com.v07.running", "name": "Running Tracker", "enabled": True},
            {"id": "com.v07.habits", "name": "Habits & Streaks", "enabled": True},
            {"id": "com.v07.vitals", "name": "Vitals & Mood", "enabled": True},
            {"id": "com.v07.gita", "name": "Bhagavad Gita", "enabled": True},
            {"id": "com.v07.reader", "name": "Reader & Pocket", "enabled": True},
            {"id": "com.v07.gamify", "name": "Gamification & RPG", "enabled": True},
            {"id": "com.v07.tasks", "name": "Task Studio", "enabled": True},
            {"id": "com.v07.timetracker", "name": "Chronos Timeline", "enabled": True},
            {"id": "com.v07.chronosynth", "name": "Chronosynth Analytics", "enabled": True}
        ],
        "updated_at": collector.now_ms,
        "_syncedAt": collector.iso_now
    }
    writes.append({
        "update": {
            "name": f"{uploader.base_doc_prefix}/system/plugins",
            "fields": to_firestore_doc(system_plugins)
        }
    })

    # Deduplicate writes by document name to ensure Firestore batchWrite compliance
    deduped_writes = {}
    for w in writes:
        doc_name = w["update"]["name"]
        deduped_writes[doc_name] = w
    writes = list(deduped_writes.values())

    print(f"\nExecuting batch writes ({len(writes)} total unique documents to Firestore)...")
    success, failed = uploader.upload_batch(writes, max_chunk=200)
    print("\n" + "=" * 60)
    print(f"MIGRATION COMPLETE!")
    print(f"Total Unique Documents: {len(writes)}")
    print(f"Success: {success}")
    print(f"Failed:  {failed}")
    print("=" * 60)

if __name__ == "__main__":
    main()
