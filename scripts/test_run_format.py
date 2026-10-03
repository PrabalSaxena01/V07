import json

with open(r'D:\software\V07-Android\legacy_data\Data\events\running.jsonl', 'r', encoding='utf-8') as f:
    raw_lines = [json.loads(l[l.find('{'):]) for l in f if '{' in l]

def format_pace_v3(dur_sec, dist_km):
    if not dist_km or dist_km <= 0 or not dur_sec or dur_sec <= 0:
        return "--'--\"/km"
    p_sec = dur_sec / dist_km
    if p_sec > 3600:
        return "--'--\"/km"
    mins = int(p_sec // 60)
    secs = int(round(p_sec % 60))
    if secs == 60:
        mins += 1
        secs = 0
    return f"{mins}'{secs:02d}\"/km"

def format_duration_v3(dur_sec):
    hrs = int(dur_sec // 3600)
    mins = int((dur_sec % 3600) // 60)
    secs = int(dur_sec % 60)
    if hrs > 0:
        return f"{hrs}h {mins}m {secs}s"
    if mins > 0:
        return f"{mins}m {secs:02d}s" if secs > 0 else f"{mins}m"
    return f"{secs}s"

formatted = []
for r in raw_lines:
    dist = float(r.get('distance_km') or r.get('km') or 0.0)
    mins_raw = float(r.get('minutes') or 0.0)
    dur_min_raw = float(r.get('duration_min') or 0.0)
    
    # duration in seconds
    if dur_min_raw >= 60.0 or dur_min_raw > mins_raw:
        dur_sec = int(dur_min_raw)
    elif mins_raw > 0:
        dur_sec = int(mins_raw * 60)
    else:
        dur_sec = int(dur_min_raw * 60) if dur_min_raw > 0 else 0
        
    pace_str = format_pace_v3(dur_sec, dist)
    
    # Notes / Title cleanup
    t = (r.get('title') or '').strip()
    n = (r.get('notes') or r.get('note') or '').strip()
    rtype = (r.get('run_type') or '').strip()
    
    # Combine title, run_type and notes meaningfully
    note_parts = []
    if t and t.lower() != 'run':
        note_parts.append(t)
    if rtype and rtype.lower() not in ['run', 'easy'] or (rtype and not note_parts):
        note_parts.append(f"{rtype.title()} run")
    if n and n != t and n != '':
        note_parts.append(n)
        
    final_note = " • ".join(note_parts)
    
    display_title = t if (t and t.lower() != 'run') else f"{dist:.2f} km Run"
    
    formatted.append({
        'id': r.get('id'),
        'date': r.get('date'),
        'distanceKm': round(dist, 2),
        'durationSec': dur_sec,
        'durationFormatted': format_duration_v3(dur_sec),
        'pace': pace_str,
        'title': display_title,
        'note': final_note,
        'runType': rtype or 'easy'
    })

print(f"Total formatted runs: {len(formatted)}")
for i, x in enumerate(formatted):
    print(f"{i+1:02d} | {x['date']} | {x['distanceKm']:>5.2f} km | {x['durationFormatted']:>8} | Pace: {x['pace']:>10} | Title: {x['title']:<22} | Note: {x['note']}")
