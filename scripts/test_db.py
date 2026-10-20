import sqlite3

con = sqlite3.connect('scratch/v07_phone.db')
cur = con.cursor()
cur.execute("CREATE TABLE IF NOT EXISTS deleted_tombstones (id TEXT PRIMARY KEY, category TEXT, deleted_at INTEGER NOT NULL);")
con.commit()

SAFE_EXCLUDE_CLAUSE = "id NOT IN (SELECT original_id FROM recycle_bin WHERE original_id IS NOT NULL AND original_id != '') AND id NOT IN (SELECT id FROM deleted_tombstones WHERE id IS NOT NULL AND id != '')"

for table in ['events', 'sessions', 'thoughts', 'tasks', 'day_tracker', 'app_usage']:
    try:
        cur.execute(f"SELECT count(*) FROM {table} WHERE {SAFE_EXCLUDE_CLAUSE}")
        print(f"{table}: {cur.fetchone()[0]}")
    except Exception as e:
        print(f"{table} FAILED: {e}")
con.close()
