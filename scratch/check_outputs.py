import os

files = [
    'D:/software/V07-Android/output/01_home_dock.png',
    'D:/software/V07-Android/output/02_tools_tab.png',
    'D:/software/V07-Android/output/03_settings_tab.png',
]

for f in files:
    if os.path.exists(f):
        print(f"File {f}: {os.path.getsize(f)} bytes")
    else:
        print(f"File {f}: MISSING")
