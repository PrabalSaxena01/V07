import json, re
from datetime import datetime

def parse_iso_ts(ts_str):
    if not ts_str:
        return int(datetime.now().timestamp() * 1000)
    if isinstance(ts_str, (int, float)):
        return int(ts_str)
    # clean iso strings
    s = str(ts_str).strip()
    # Try common formats
    formats = [
        "%Y-%m-%d'T'%H:%M:%S.%f%z",
        "%Y-%m-%d'T'%H:%M:%S%z",
        "%Y-%m-%d'T'%H:%M:%S.%fZ",
        "%Y-%m-%d'T'%H:%M:%SZ",
        "%Y-%m-%d'T'%H:%M:%S",
        "%Y-%m-%d %H:%M:%S",
        "%Y-%m-%d"
    ]
    for fmt in formats:
        try:
            # Handle ISO with colon in timezone e.g. +05:30 -> +0530 for python strptime
            cleaned = re.sub(r'([+-]\d{2}):(\d{2})$', r'\1\2', s)
            cleaned = cleaned.replace("'T'", "T")
            dt = datetime.fromisoformat(cleaned)
            return int(dt.timestamp() * 1000)
        except Exception:
            pass
    try:
        dt = datetime.fromisoformat(s)
        return int(dt.timestamp() * 1000)
    except Exception:
        pass
    return int(datetime.now().timestamp() * 1000)

with open('scratch/v07_companion_export/events.json', encoding='utf-8') as f:
    events = json.load(f)

print(f"Total events to transform: {len(events)}")

transformed_events = []
transformed_sessions = []
transformed_tasks = []
transformed_thoughts = []

for doc in events:
    doc_id = doc.get('id') or doc.get('_doc_id')
    cat = (doc.get('category') or 'unknown').lower().strip()
    raw_ts = doc.get('ts') or doc.get('timestamp') or doc.get('started_at')
    epoch_ms = parse_iso_ts(raw_ts)
    dt = datetime.fromtimestamp(epoch_ms / 1000)
    date_str = doc.get('date') or dt.strftime('%Y-%m-%d')
    iso_ts = dt.strftime('%Y-%m-%dT%H:%M:%S')

    title = None
    content = None
    score = 0
    meta = {}

    if cat == 'sessions':
        act = doc.get('activity') or doc.get('title') or 'Focus Session'
        dur_min = doc.get('duration') or doc.get('duration_min') or doc.get('duration_minutes') or 0
        dur_sec = doc.get('duration_sec') or (dur_min * 60)
        notes = doc.get('notes') or doc.get('custom_label') or ''
        start_time = doc.get('started_at') or epoch_ms
        end_time = doc.get('ended_at') or (start_time + (dur_sec * 1000))
        
        transformed_sessions.append({
            'id': doc_id,
            'activity': act,
            'startTime': start_time,
            'endTime': end_time,
            'durationSec': dur_sec,
            'notes': notes
        })
        title = act.replace('_', ' ').title()
        content = f"{dur_sec // 60}m focus session" + (f" • {notes}" if notes else "")
        cat_mapped = 'session'

    elif cat in ('tasks', 'tasks_ui'):
        t_title = doc.get('title') or doc.get('task_id') or 'Task'
        action = doc.get('action') or ''
        new_status = doc.get('new_status') or doc.get('status') or ''
        is_done = (new_status in ('done', 'completed') or action == 'completed')
        prio = doc.get('priority') or 'MEDIUM'
        deadline = doc.get('deadline')
        
        transformed_tasks.append({
            'id': doc_id,
            'title': t_title,
            'priority': prio,
            'status': 'completed' if is_done else 'pending',
            'createdAt': epoch_ms,
            'completedAt': epoch_ms if is_done else None,
            'deadline': deadline
        })
        title = t_title
        content = f"Priority: {prio}" + (f" • Due: {deadline}" if deadline else "")
        cat_mapped = 'task'

    elif cat == 'checklist':
        lbl = doc.get('label') or 'Checklist item'
        is_done = bool(doc.get('done'))
        recur = bool(doc.get('recurring'))
        freq = doc.get('frequency') or 'D'

        transformed_tasks.append({
            'id': doc_id,
            'title': lbl,
            'priority': 'MEDIUM',
            'status': 'completed' if is_done else 'pending',
            'createdAt': epoch_ms,
            'completedAt': epoch_ms if is_done else None,
            'deadline': date_str
        })
        title = lbl
        content = f"{'Daily' if freq == 'D' else 'Recurring'} Checklist • {'Done ✓' if is_done else 'Pending'}"
        cat_mapped = 'task'

    elif cat in ('thought', 'thought_ui'):
        text = doc.get('text') or doc.get('content') or ''
        topic = doc.get('topic') or 'Reflection'
        info = doc.get('info') or ''

        transformed_thoughts.append({
            'id': doc_id,
            'timestamp': epoch_ms,
            'date': date_str,
            'topic': topic,
            'content': text,
            'pinned': False,
            'tags': info
        })
        title = topic.replace('_', ' ').title()
        content = text
        cat_mapped = 'thought'

    elif cat in ('mood', 'mood_ui'):
        m_val = doc.get('mood') or 'calm'
        m_score = int(doc.get('mood_score') or 3)
        note = doc.get('note') or ''
        title = f"Mood: {m_val.capitalize()}"
        content = note if note else f"Logged {m_val} mood (intensity: {m_score}/5)"
        score = m_score
        cat_mapped = 'mood'

    elif cat == 'energy':
        e_val = doc.get('energy') or 'balanced'
        e_score = int(doc.get('energy_score') or 3)
        note = doc.get('note') or ''
        title = f"Energy: {e_val.capitalize()}"
        content = note if note else f"Logged {e_val} energy ({e_score}/5)"
        score = e_score
        cat_mapped = 'energy'

    elif cat == 'running':
        km = float(doc.get('distance_km') or 0.0)
        dur_min = doc.get('duration_min') or doc.get('duration_minutes') or doc.get('minutes') or 0
        pace = doc.get('pace') or ''
        notes = doc.get('notes') or ''
        title = f"Run: {km:.2f} km"
        content = f"{dur_min}m" + (f" • Pace: {pace}" if pace else "") + (f" • {notes}" if notes else "")
        cat_mapped = 'run'

    elif cat in ('habits', 'habits_ui', 'hbt'):
        act = doc.get('activity') or doc.get('habit') or 'Daily Rhythm'
        is_on = bool(doc.get('on'))
        title = f"Habit: {act.replace('_', ' ').title()}"
        content = "Completed ✓" if is_on else "Missed ✗"
        cat_mapped = 'habit'

    elif cat in ('metrics', 'metrics_ui'):
        m_type = doc.get('type') or 'metrics'
        note = doc.get('note') or ''
        pushups = int(doc.get('pushups') or 0)
        if m_type == 'checkin':
            title = "Morning Check-In"
            content = note or "Started the day in presence"
            cat_mapped = 'checkin'
        elif m_type == 'checkout':
            title = "Evening Check-Out"
            content = note or "Day concluded peacefully"
            cat_mapped = 'checkout'
        elif pushups > 0:
            title = f"Pushups: {pushups}"
            content = f"{pushups} repetitions completed"
            score = pushups
            cat_mapped = 'pushups'
        else:
            title = m_type.replace('_', ' ').title()
            content = note
            cat_mapped = 'metrics'

    elif cat == 'chess':
        opp = doc.get('opponent') or 'Opponent'
        res = doc.get('result') or 'played'
        color = doc.get('color') or 'white'
        notes = doc.get('notes') or ''
        title = f"Chess vs {opp} ({res})"
        content = f"Played as {color}. {notes}".strip()
        cat_mapped = 'session'

    elif cat == 'dsa':
        prob = doc.get('problem') or 'Problem'
        top = doc.get('topic') or 'Algorithm'
        desc = doc.get('description') or ''
        diff = doc.get('difficulty') or 'medium'
        done = bool(doc.get('completed'))
        transformed_tasks.append({
            'id': doc_id,
            'title': f"DSA: {prob} ({top})",
            'priority': 'HIGH' if diff.lower() == 'hard' else 'MEDIUM',
            'status': 'completed' if done else 'pending',
            'createdAt': epoch_ms,
            'completedAt': epoch_ms if done else None,
            'deadline': date_str
        })
        title = f"DSA: {prob}"
        content = f"{top} • {diff.capitalize()} • {desc}"
        cat_mapped = 'task'

    elif cat == 'panics':
        note = doc.get('note') or ''
        interval = doc.get('time_since_last') or ''
        title = "Mindful Pause (Panic Anchor)"
        content = note + (f" (Interval: {interval})" if interval else "")
        cat_mapped = 'panic'

    elif cat == 'shares':
        s_title = doc.get('title') or 'Saved Link'
        s_text = doc.get('text') or doc.get('info') or ''
        tags = doc.get('tags') or ''
        transformed_thoughts.append({
            'id': doc_id,
            'timestamp': epoch_ms,
            'date': date_str,
            'topic': 'Link / Share',
            'content': f"{s_title}\n{s_text}",
            'pinned': False,
            'tags': tags
        })
        title = f"Saved: {s_title}"
        content = s_text
        cat_mapped = 'thought'

    elif cat == 'gita':
        v_id = doc.get('verse_id') or f"{doc.get('chapter', 1)}.{doc.get('verse', 1)}"
        title = f"Gita Verse {v_id}"
        content = f"Read and reflected on Bhagavad Gita {v_id}"
        cat_mapped = 'gita'

    else:
        title = cat.capitalize()
        content = doc.get('text') or doc.get('content') or doc.get('note') or str(doc)
        cat_mapped = cat

    transformed_events.append({
        'id': doc_id,
        'category': cat_mapped,
        'date': date_str,
        'ts': iso_ts,
        'timestamp': epoch_ms,
        'title': title,
        'content': content,
        'score': score
    })

print(f"Successfully transformed:")
print(f" - Events: {len(transformed_events)}")
print(f" - Sessions: {len(transformed_sessions)}")
print(f" - Tasks: {len(transformed_tasks)}")
print(f" - Thoughts: {len(transformed_thoughts)}")
