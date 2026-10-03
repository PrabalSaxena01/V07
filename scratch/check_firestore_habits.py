import requests, json

url = "https://firestore.googleapis.com/v1/projects/v07-dashboard/databases/(default)/documents/users/5fwVgpra4pRzB1QqMWb9LSZSqrp2/plugins/com.v07.habits/storage/definitions"
r = requests.get(url)
print("=== HABIT STORAGE DEFINITIONS STATUS:", r.status_code, "===")
if r.status_code == 200:
    print(json.dumps(r.json(), indent=2))

events_url = "https://firestore.googleapis.com/v1/projects/v07-dashboard/databases/(default)/documents/users/5fwVgpra4pRzB1QqMWb9LSZSqrp2/plugins/com.v07.habits/events?pageSize=100"
r2 = requests.get(events_url)
print("\n=== HABIT EVENTS STATUS:", r2.status_code, "===")
if r2.status_code == 200:
    docs = r2.json().get('documents', [])
    print(f"Total habit docs: {len(docs)}")
    seen = set()
    for d in docs:
        fields = d.get('fields', {})
        h_id = fields.get('habit_id', {}).get('stringValue')
        h_name = fields.get('habit_name', {}).get('stringValue')
        seen.add((h_id, h_name))
    print("Unique habit_id & habit_name in events:")
    for s in sorted(seen):
        print(s)
