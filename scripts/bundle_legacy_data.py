#!/usr/bin/env python3
"""
Bundle and Sanitize Legacy Data into a unified, machine-readable asset
for the V3 In-App Legacy Migration Studio.
"""
import os
import json
import re
from datetime import datetime, timezone, date

BASE_DIR = r"D:\software\V07-Android"
LEGACY_DIR = os.path.join(BASE_DIR, "legacy_data")
OUTPUT_PATH = os.path.join(BASE_DIR, "app", "src", "main", "assets", "v3", "legacy_bundle.json")

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
        # If in seconds (< 2000000000), convert to ms
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

def bundle_all():
    print(f"Reading legacy data from: {LEGACY_DIR}")
    events_dir = os.path.join(LEGACY_DIR, "Data", "events")

    # 1. Sessions
    sessions = []
    sess_file = os.path.join(events_dir, "sessions.jsonl")
    if os.path.exists(sess_file):
        raw_sessions = []
        with open(sess_file, "r", encoding="utf-8") as f:
            for i, line in enumerate(f):
                obj = parse_line(line)
                if obj:
                    raw_sessions.append((i, obj))

        # First collect all ended sessions
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
                # If this start had a corresponding end event, skip the start marker
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
                    # In legacy sessions.jsonl, duration was recorded in minutes (e.g. 5, 26, 44, 103)
                    dur_sec = int(dur * 60)
                elif end_ts and start_ts and end_ts > start_ts:
                    dur_sec = max(0, int((end_ts - start_ts) / 1000))
                else:
                    dur_sec = 0

            activity = (obj.get("activity") or "Focus").strip()
            title = (obj.get("title") or "").strip()
            notes = (obj.get("notes") or obj.get("note") or "").strip()
            if title and notes:
                full_notes = f"{title} - {notes}"
            elif title:
                full_notes = title
            else:
                full_notes = notes

            sid = obj.get("id") or f"sess_{start_ts}_{i}"
            sessions.append({
                "id": sid,
                "activity": activity,
                "title": title or activity,
                "date": d_str,
                "start_time": start_ts,
                "start_formatted": format_ts(start_ts),
                "end_time": end_ts,
                "duration_sec": dur_sec,
                "duration_min": round(dur_sec / 60.0, 1),
                "duration_formatted": format_seconds(dur_sec),
                "notes": full_notes,
                "selected": True
            })
    # Sort sessions newest first
    sessions.sort(key=lambda s: s["start_time"], reverse=True)

    # 2. Tasks
    tasks = []
    seen_task_ids = set()
    for t_file in ["tasks.jsonl", "tasks_ui.jsonl"]:
        path = os.path.join(events_dir, t_file)
        if os.path.exists(path):
            with open(path, "r", encoding="utf-8") as f:
                for i, line in enumerate(f):
                    obj = parse_line(line)
                    if not obj:
                        continue
                    tid = obj.get("id") or obj.get("task_id")
                    if tid and tid in seen_task_ids:
                        continue
                    title = obj.get("title") or obj.get("text") or ""
                    # Strip markdown checkboxes
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
                    status = "completed" if is_done else "pending"
                    created_ts = parse_timestamp(obj.get("ts") or obj.get("date") or obj.get("created_at"))
                    d_str = parse_date_str(obj.get("date") or obj.get("ts") or obj.get("created_at"))
                    deadline = obj.get("deadline") or None

                    if not tid:
                        tid = f"task_{created_ts}_{i}"
                    seen_task_ids.add(tid)

                    tasks.append({
                        "id": tid,
                        "title": title,
                        "date": d_str,
                        "priority": prio,
                        "status": status,
                        "created_at": created_ts,
                        "created_formatted": format_ts(created_ts),
                        "deadline": deadline,
                        "selected": True
                    })
    # Sort tasks: pending first, then newest
    tasks.sort(key=lambda t: (0 if t["status"] == "pending" else 1, -t["created_at"]))

    # 3. Running
    runs = []
    run_file = os.path.join(events_dir, "running.jsonl")
    if os.path.exists(run_file):
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

                # duration in seconds: in legacy, duration_min was recorded as minutes * 60
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

                # Notes, title and run type cleanup
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

                runs.append({
                    "id": rid,
                    "date": d_str,
                    "timestamp": ts,
                    "distanceKm": round(dist, 2),
                    "distance_km": round(dist, 2),
                    "durationSec": dur_sec,
                    "duration_sec": dur_sec,
                    "duration_min": round(dur_sec / 60.0, 1),
                    "durationFormatted": format_seconds(dur_sec),
                    "duration_formatted": format_seconds(dur_sec),
                    "pace": pace_str,
                    "pace_formatted": pace_str,
                    "pace_sec_km": pace_sec,
                    "title": display_title,
                    "note": final_note,
                    "notes": final_note,
                    "run_type": rtype or "easy",
                    "selected": True
                })
    runs.sort(key=lambda r: r["timestamp"], reverse=True)

    # 4. Habits
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

    habit_defs = {}
    habit_logs = []
    seen_log_keys = set()

    for h_file in ["habits.jsonl", "habits_ui.jsonl", "hbt.jsonl"]:
        path = os.path.join(events_dir, h_file)
        if not os.path.exists(path):
            continue
        with open(path, "r", encoding="utf-8") as f:
            for line in f:
                obj = parse_line(line)
                if not obj:
                    continue

                # Pattern A: Definition block with 'habits' list (hbt.jsonl)
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
                                "history": {},
                                "selected": True
                            }

                # Pattern B: habits.jsonl with 'activity' field
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
                            "history": {},
                            "selected": True
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
                                "habit_id": hid,
                                "habit_name": h_name,
                                "name": h_name,
                                "date": d_str,
                                "completed": True,
                                "value": "Done",
                                "timestamp": ts
                            })

                # Pattern C: hbt.jsonl with 'habit' field
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
                            "history": {},
                            "selected": True
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
                                "habit_id": hid,
                                "habit_name": h_name,
                                "name": h_name,
                                "date": d_str,
                                "completed": True,
                                "value": "Done",
                                "timestamp": ts
                            })

                # Pattern D: Generic def or title
                if obj.get("kind") == "def" or ("title" in obj and "completed" not in obj and not act and not h_key):
                    title = obj.get("title", "").strip()
                    hid = obj.get("id") or obj.get("habit_id") or (f"hbt_{title.lower().replace(' ', '_')}" if title else None)
                    if title and hid and hid not in habit_defs:
                        habit_defs[hid] = {
                            "id": hid,
                            "name": title,
                            "title": title,
                            "icon": obj.get("icon", "🔥"),
                            "emoji": obj.get("icon", "🔥"),
                            "cadence": obj.get("target_freq", "daily"),
                            "target_freq": obj.get("target_freq", "daily"),
                            "streak": int(obj.get("streak", 0)),
                            "history": {},
                            "selected": True
                        }

    # Calculate streaks for all habits
    for hid, h in habit_defs.items():
        hist = h.get("history", {})
        if hist:
            dates = sorted(hist.keys(), reverse=True)
            streak = len(dates)
            h["streak"] = streak

    habits_list = list(habit_defs.values())

    # 5. Thoughts
    thoughts = []
    seen_thought_ids = set()
    for th_file in ["thought.jsonl", "pinned_thoughts.jsonl", "thought_ui.jsonl"]:
        path = os.path.join(events_dir, th_file)
        if os.path.exists(path):
            is_pinned_file = "pinned" in th_file
            with open(path, "r", encoding="utf-8") as f:
                for i, line in enumerate(f):
                    obj = parse_line(line)
                    if not obj:
                        continue
                    th_id = obj.get("id")
                    if th_id and th_id in seen_thought_ids:
                        continue
                    content = (obj.get("text") or obj.get("content") or "").strip()
                    if not content:
                        continue
                    ts = parse_timestamp(obj.get("ts") or obj.get("date"))
                    d_str = parse_date_str(obj.get("date") or obj.get("ts"))
                    topic = (obj.get("topic") or "General").strip()
                    pinned = is_pinned_file or bool(obj.get("pinned", False))

                    if not th_id:
                        th_id = f"th_{ts}_{i}"
                    seen_thought_ids.add(th_id)

                    thoughts.append({
                        "id": th_id,
                        "timestamp": ts,
                        "date": d_str,
                        "topic": topic,
                        "content": content,
                        "pinned": pinned,
                        "formatted": format_ts(ts),
                        "selected": True
                    })
    thoughts.sort(key=lambda t: t["timestamp"], reverse=True)

    # 6. Milestones (Day Tracker)
    milestones = []
    dt_path = os.path.join(LEGACY_DIR, "day_tracker.jsonl")
    if os.path.exists(dt_path):
        with open(dt_path, "r", encoding="utf-8") as f:
            for i, line in enumerate(f):
                obj = parse_line(line)
                if not obj:
                    continue
                title = (obj.get("title") or "Milestone").strip()
                t_date = parse_date_str(obj.get("date") or obj.get("target_date"))
                mid = obj.get("id") or f"dt_{i}"
                created_ts = parse_timestamp(obj.get("ts") or obj.get("created_at"))
                pinned = bool(obj.get("pinned", False))
                hour = int(obj.get("hour", 0))

                milestones.append({
                    "id": mid,
                    "title": title,
                    "target_date": t_date,
                    "hour": hour,
                    "pinned": pinned,
                    "created_at": created_ts,
                    "selected": True
                })

    # 7. Vitals (Mood & Energy)
    vitals = []
    for v_file, v_type in [("mood.jsonl", "mood"), ("energy.jsonl", "energy")]:
        path = os.path.join(events_dir, v_file)
        if os.path.exists(path):
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
                    note = (obj.get("note") or obj.get("text") or "").strip()

                    vitals.append({
                        "id": vid,
                        "type": v_type,
                        "score": round(score, 1),
                        "date": d_str,
                        "timestamp": ts,
                        "note": note,
                        "formatted": format_ts(ts),
                        "selected": True
                    })
    vitals.sort(key=lambda v: v["timestamp"], reverse=True)

    # 8. Recycle Bin
    recycle_items = []
    rb_path = os.path.join(events_dir, "recycle_bin.jsonl")
    if os.path.exists(rb_path):
        with open(rb_path, "r", encoding="utf-8") as f:
            for i, line in enumerate(f):
                obj = parse_line(line)
                if not obj:
                    continue
                rid = obj.get("recycle_id") or f"rec_{i}"
                data_obj = obj.get("data")
                orig_id = (data_obj.get("id") if isinstance(data_obj, dict) else None) or obj.get("id") or f"orig_{i}"
                cat = obj.get("category") or "general"
                del_at = parse_timestamp(obj.get("deleted_at") or obj.get("ts"))
                d_json = json.dumps(data_obj) if data_obj else json.dumps(obj)
                summary_text = ""
                if isinstance(data_obj, dict):
                    summary_text = data_obj.get("title") or data_obj.get("text") or data_obj.get("content") or ""
                elif isinstance(obj, dict):
                    summary_text = obj.get("title") or obj.get("text") or ""

                recycle_items.append({
                    "recycle_id": rid,
                    "original_id": orig_id,
                    "category": cat,
                    "data_json": d_json,
                    "summary": summary_text[:80],
                    "deleted_at": del_at,
                    "deleted_formatted": format_ts(del_at),
                    "selected": False  # Soft-deleted items default to unchecked for safety
                })
    recycle_items.sort(key=lambda r: r["deleted_at"], reverse=True)

    # 9. App Usage / Screen Time
    app_usage_summary = []
    usage_dir = os.path.join(LEGACY_DIR, "app_usage")
    if os.path.exists(usage_dir) and os.path.isdir(usage_dir):
        files = sorted(os.listdir(usage_dir))
        for fname in files:
            if not fname.endswith(".json"):
                continue
            date_match = re.search(r"\d{4}-\d{2}-\d{2}", fname)
            d_str = date_match.group(0) if date_match else fname.replace(".json", "")
            fpath = os.path.join(usage_dir, fname)
            try:
                with open(fpath, "r", encoding="utf-8") as f:
                    u_data = json.load(f)
                    total_sec = 0
                    apps = []
                    hourly_mins = [0] * 24

                    if isinstance(u_data, dict):
                        # Extract hourly usage if available
                        raw_hourly = u_data.get("hourly_usage")
                        if isinstance(raw_hourly, list):
                            for h_item in raw_hourly:
                                if isinstance(h_item, dict):
                                    h_idx = int(h_item.get("hour", 0))
                                    if 0 <= h_idx < 24:
                                        h_sec = h_item.get("duration_sec") or (h_item.get("duration_min", 0) * 60)
                                        hourly_mins[h_idx] = int(min(60, round(h_sec / 60.0)))

                        # Extract app usage
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
                                    "package_name": pkg,
                                    "app_name": name,
                                    "duration_ms": dur_ms,
                                    "duration_sec": dur_sec,
                                    "formatted": format_seconds(dur_sec),
                                    "launch_count": launches,
                                    "percentage": pct,
                                    "category": cat
                                })
                            summary_obj = u_data.get("summary") or {}
                            total_sec = summary_obj.get("total_screen_time_sec") or sum(a["duration_sec"] for a in apps)
                        else:
                            # Fallback if other schema (e.g. apps dict)
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
                                        "package_name": pkg,
                                        "app_name": name,
                                        "duration_ms": int(dur_sec * 1000),
                                        "duration_sec": int(dur_sec),
                                        "formatted": format_seconds(int(dur_sec)),
                                        "launch_count": 1,
                                        "percentage": 0.0,
                                        "category": cat
                                    })
                                total_sec = sum(a["duration_sec"] for a in apps)

                    apps.sort(key=lambda a: a["duration_ms"], reverse=True)
                    total_ms = int(total_sec * 1000)
                    app_usage_summary.append({
                        "date": d_str,
                        "total_duration_ms": total_ms,
                        "total_duration_sec": int(total_sec),
                        "formatted_duration": format_seconds(total_sec),
                        "app_count": len(apps),
                        "top_apps": apps[:5],
                        "all_apps": apps,
                        "hourly_minutes": hourly_mins,
                        "selected": True
                    })
            except Exception as e:
                pass
    app_usage_summary.sort(key=lambda u: u["date"], reverse=True)

    # 10. Shares
    shares = []
    shares_file = os.path.join(events_dir, "shares.jsonl")
    if os.path.exists(shares_file):
        with open(shares_file, "r", encoding="utf-8") as f:
            for i, line in enumerate(f):
                obj = parse_line(line)
                if not obj:
                    continue
                url = obj.get("url") or obj.get("link") or obj.get("text") or ""
                if url:
                    ts = parse_timestamp(obj.get("ts") or obj.get("date"))
                    shares.append({
                        "id": obj.get("id") or f"sh_{ts}_{i}",
                        "title": obj.get("title") or url[:50],
                        "url": url,
                        "timestamp": ts,
                        "formatted": format_ts(ts),
                        "selected": True
                    })

    # Summary
    total_records = (
        len(sessions) + len(tasks) + len(runs) + len(habits_list) +
        len(thoughts) + len(milestones) + len(vitals) + len(recycle_items) +
        len(app_usage_summary) + len(shares)
    )

    bundle = {
        "meta": {
            "version": "1.0.0",
            "bundled_at": datetime.now(timezone.utc).isoformat(),
            "total_items": total_records,
            "counts": {
                "sessions": len(sessions),
                "tasks": len(tasks),
                "running": len(runs),
                "habits": len(habits_list),
                "habit_logs": len(habit_logs),
                "thoughts": len(thoughts),
                "milestones": len(milestones),
                "vitals": len(vitals),
                "recycle_bin": len(recycle_items),
                "screentime_days": len(app_usage_summary),
                "shares": len(shares)
            }
        },
        "sessions": sessions,
        "tasks": tasks,
        "running": runs,
        "habits": {
            "definitions": habits_list,
            "logs": habit_logs
        },
        "thoughts": thoughts,
        "milestones": milestones,
        "vitals": vitals,
        "recycle_bin": recycle_items,
        "screentime": app_usage_summary,
        "shares": shares
    }

    os.makedirs(os.path.dirname(OUTPUT_PATH), exist_ok=True)
    with open(OUTPUT_PATH, "w", encoding="utf-8") as f:
        json.dump(bundle, f, indent=2, ensure_ascii=False)

    size_kb = os.path.getsize(OUTPUT_PATH) / 1024.0
    print(f"Successfully generated legacy bundle at: {OUTPUT_PATH}")
    print(f"Size: {size_kb:.2f} KB | Total Records: {total_records}")
    print("Counts breakdown:", json.dumps(bundle["meta"]["counts"], indent=2))
    return bundle

if __name__ == "__main__":
    bundle_all()
