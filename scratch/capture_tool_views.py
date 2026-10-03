from cdp_client import eval_js
import subprocess
import time

plugins = ['focus', 'reading', 'habits', 'screentime', 'mood', 'gita', 'weekly']

for p in plugins:
    eval_js(f"openPluginFullPage('{p}'); window.scrollTo(0, 0);")
    time.sleep(0.4)
    subprocess.run(['adb', 'shell', 'screencap', '-p', f'/sdcard/tool_{p}.png'], check=True)
    subprocess.run(['adb', 'pull', f'/sdcard/tool_{p}.png', f'scratch/tool_{p}.png'], check=True)
    eval_js("navigateBack();")
    time.sleep(0.3)

print("All 7 tool views captured!")
