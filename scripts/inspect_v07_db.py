with open(r"D:/software/V07-Android/app/src/main/java/com/v07/v3/core/data/V07Database.kt", "r", encoding="utf-8") as f:
    text = f.read()

import re
tables = re.findall(r'CREATE TABLE[^(]+\((.*?)\)', text, re.DOTALL)
table_names = re.findall(r'CREATE TABLE\s+(?:IF NOT EXISTS\s+)?([a-zA-Z0-9_]+)', text)
print("=== V07Database Tables ===")
print(table_names)
