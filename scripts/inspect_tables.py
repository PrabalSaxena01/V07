with open(r"D:/software/V07-Android/app/src/main/java/com/v07/v3/core/data/DatabaseManager.kt", "r", encoding="utf-8") as f:
    text = f.read()

import re
tables = re.findall(r'CREATE TABLE IF NOT EXISTS\s+([a-zA-Z0-9_]+)', text)
print("=== DatabaseManager Tables ===")
print(tables)
