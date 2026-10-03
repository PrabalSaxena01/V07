import sqlite3
import json
from datetime import datetime

conn = sqlite3.connect('scratch/v07_phone.db')
c = conn.cursor()

# 1. events
events = c.execute("SELECT * FROM events").fetchall()
print(f"events table rows: {len(events)}")

# 2. thoughts
thoughts = c.execute("SELECT * FROM thoughts").fetchall()
print(f"thoughts table rows: {len(thoughts)}")

# 3. sessions
sessions = c.execute("SELECT * FROM sessions").fetchall()
print(f"sessions table rows: {len(sessions)}")

# 4. tasks
tasks = c.execute("SELECT * FROM tasks").fetchall()
print(f"tasks table rows: {len(tasks)}")

# 5. plugin_storage
ps = c.execute("SELECT * FROM plugin_storage").fetchall()
print(f"plugin_storage table rows: {len(ps)}")
