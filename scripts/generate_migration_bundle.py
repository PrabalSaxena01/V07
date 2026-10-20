import json, os
from test_transform import transformed_events, transformed_sessions, transformed_tasks, transformed_thoughts

with open('scratch/v07_companion_export/daytracker.json', encoding='utf-8') as f:
    daytracker_raw = json.load(f)

transformed_daytracker = []
for d in daytracker_raw:
    transformed_daytracker.append({
        'id': d.get('id') or d.get('_doc_id'),
        'title': d.get('title') or 'Milestone',
        'targetDate': d.get('date'),
        'hour': d.get('hour') or 0,
        'pinned': bool(d.get('pinned'))
    })

with open('scratch/v07_companion_export/gita_reviews.json', encoding='utf-8') as f:
    gita_raw = json.load(f)

transformed_gita = []
for r in gita_raw:
    transformed_gita.append({
        'id': r.get('id') or r.get('_doc_id'),
        'verse_id': r.get('verse_id') or '1.1',
        'text': r.get('text') or '',
        'date': r.get('date') or '2026-07-26',
        'rating': r.get('rating') or 3
    })

with open('scratch/v07_companion_export/appusage.json', encoding='utf-8') as f:
    appusage_raw = json.load(f)

transformed_appusage = []
for doc in appusage_raw:
    date = doc.get('date') or doc.get('_doc_id')
    summary = doc.get('summary', {})
    tot_sec = summary.get('total_screen_time_sec') or 0
    app_cnt = summary.get('unique_apps_used') or summary.get('session_count') or 0
    usage_list = doc.get('usage', [])
    for u in usage_list:
        p_name = u.get('package') or 'unknown'
        a_name = u.get('app') or p_name
        dur_sec = u.get('duration_sec') or 0
        transformed_appusage.append({
            'id': f"u_{date}_{p_name}",
            'date': date,
            'packageName': p_name,
            'appName': a_name,
            'durationSec': dur_sec
        })

migration_package = {
    'version': '1.0',
    'source': 'v07-dashboard (Firestore)',
    'uid': '5fwVgpra4pRzB1QqMWb9LSZSqrp2',
    'exportedAt': '2026-09-26T23:05:00Z',
    'events': transformed_events,
    'sessions': transformed_sessions,
    'tasks': transformed_tasks,
    'thoughts': transformed_thoughts,
    'daytracker': transformed_daytracker,
    'gita_reviews': transformed_gita,
    'appusage_records': transformed_appusage
}

out_path = 'app/src/main/assets/v3/v07_companion_migration.json'
with open(out_path, 'w', encoding='utf-8') as f:
    json.dump(migration_package, f, indent=2, ensure_ascii=False)

print(f"Migration bundle written to {out_path} ({os.path.getsize(out_path)} bytes)")
print(f"Events: {len(transformed_events)}")
print(f"Sessions: {len(transformed_sessions)}")
print(f"Tasks: {len(transformed_tasks)}")
print(f"Thoughts: {len(transformed_thoughts)}")
print(f"Daytracker: {len(transformed_daytracker)}")
print(f"Gita Reviews: {len(transformed_gita)}")
print(f"App Usage Records: {len(transformed_appusage)}")
