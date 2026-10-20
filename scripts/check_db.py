import sqlite3

conn = sqlite3.connect('scratch/v07_phone.db')
c = conn.cursor()
tables = [r[0] for r in c.execute("SELECT name FROM sqlite_master WHERE type='table'").fetchall()]
print("Tables:", tables)
for t in tables:
    try:
        cnt = c.execute(f"SELECT count(*) FROM {t}").fetchone()[0]
        print(f"{t}: {cnt}")
    except Exception as e:
        print(f"{t}: ERROR {e}")
