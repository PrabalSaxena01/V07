with open('D:/software/V07-Android/app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

checks = [
    ('id="tab-tools"', 'tab-tools present'),
    ('id="tab-settings"', 'tab-settings present'),
    ('id="modal-screentime"', 'modal-screentime present'),
    ('id="modal-recycle-bin"', 'modal-recycle-bin present'),
    ('data-tab="settings"', 'bottomNav settings present'),
    ('toggleOverlayFromJs', 'toggleOverlayFromJs present'),
    ('openAdminCockpit', 'openAdminCockpit present'),
    ('openRecycleBinModal', 'openRecycleBinModal present'),
    ('loadScreenTimeLive', 'loadScreenTimeLive present'),
]

for needle, desc in checks:
    print(f"[{'PASS' if needle in text else 'FAIL'}] {desc}")
