from cdp_client import eval_js
import subprocess
import time

def snap(name):
    subprocess.run(['adb', 'shell', 'screencap', '-p', f'/sdcard/{name}.png'], check=True)
    subprocess.run(['adb', 'pull', f'/sdcard/{name}.png', f'scratch/{name}.png'], check=True)

# 1. Home Top
eval_js("switchTab('home'); window.scrollTo(0, 0);")
time.sleep(0.3)
snap('audit_home_top')

# 2. Home Bottom
eval_js("window.scrollTo(0, 800);")
time.sleep(0.3)
snap('audit_home_bottom')

# 3. Tools Top
eval_js("switchTab('tools'); window.scrollTo(0, 0);")
time.sleep(0.3)
snap('audit_tools_top')

# 4. Tools Bottom
eval_js("window.scrollTo(0, 800);")
time.sleep(0.3)
snap('audit_tools_bottom')

# 5. Journal Top
eval_js("switchTab('journal'); window.scrollTo(0, 0);")
time.sleep(0.3)
snap('audit_journal_top')

# 6. Journal Bottom
eval_js("window.scrollTo(0, 800);")
time.sleep(0.3)
snap('audit_journal_bottom')

# 7. Day
eval_js("switchTab('day'); window.scrollTo(0, 0);")
time.sleep(0.3)
snap('audit_day')

# 8. Settings Top
eval_js("switchTab('settings'); window.scrollTo(0, 0);")
time.sleep(0.3)
snap('audit_settings_top')

# 9. Settings Bottom
eval_js("window.scrollTo(0, 800);")
time.sleep(0.3)
snap('audit_settings_bottom')

print("All screenshots captured!")
