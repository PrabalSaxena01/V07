html = open(r'D:\software\V07-Android\app\src\main\assets\v3\index.html', encoding='utf-8').read()
for sym in ['openFocusModal', 'closeFocusModal', 'setFocusMode', 'toggleFocusTimer', 'resetFocusTimer', 'openScreenTimeModal', 'closeScreenTimeModal', 'loadScreenTimeLive']:
    found = f"window.{sym} =" in html
    print(f"window.{sym}: {'YES' if found else 'NO'}")
