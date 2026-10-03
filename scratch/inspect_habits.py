import sys, os
sys.path.append(r'D:\software\V07-Android\scripts')
from migrate_legacy_to_firestore import LegacyDataCollector
c = LegacyDataCollector()
c.collect_all()

print('=== HABIT DEFINITIONS ===')
for k, v in c.habits_storage.get('definitions', {}).items():
    print(f"ID: {k} | Name: {v.get('name')} | Streak: {v.get('streak')}")

print('\n=== UNIQUE HABIT NAMES IN HABIT EVENTS ===')
names = set()
for h in c.habits_events:
    names.add((h.get('habit_id'), h.get('habit_name'), h.get('category')))
for item in sorted(names):
    print(item)
