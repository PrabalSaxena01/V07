import json

with open('scratch/v07_companion_export/appusage.json', encoding='utf-8') as f:
    appusage = json.load(f)

print(f"Total appusage docs: {len(appusage)}")
transformed_appusage = []
for doc in appusage:
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
            'packageName': p_name,
            'appName': a_name,
            'startTime': 0,
            'endTime': 0,
            'durationMs': dur_sec * 1000,
            'category': 'Neutral'
        })

print(f"Total appusage records: {len(transformed_appusage)} across {len(appusage)} days")

with open('scratch/v07_companion_export/daytracker.json', encoding='utf-8') as f:
    daytracker = json.load(f)

print(f"Total daytracker items: {len(daytracker)}")
transformed_daytracker = []
for d in daytracker:
    transformed_daytracker.append({
        'id': d.get('id') or d.get('_doc_id'),
        'title': d.get('title') or 'Milestone',
        'targetDate': d.get('date'),
        'hour': d.get('hour') or 0,
        'pinned': bool(d.get('pinned'))
    })
print("Daytracker items transformed:", transformed_daytracker)

with open('scratch/v07_companion_export/gita_reviews.json', encoding='utf-8') as f:
    gita_reviews = json.load(f)

print(f"Total gita_reviews: {len(gita_reviews)}")
for r in gita_reviews:
    print("Gita review:", r.get('verse_id'), "->", r.get('text'))
