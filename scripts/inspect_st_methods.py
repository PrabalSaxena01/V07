with open(r"D:/software/V07-Android/app/src/main/java/com/v07/v3/app/telemetry/ScreenTimeTracker.kt", "r", encoding="utf-8") as f:
    text = f.read()

import re
methods = re.findall(r'fun\s+([a-zA-Z0-9_]+)\s*\(', text)
print("=== ScreenTimeTracker Methods ===")
print(methods)
