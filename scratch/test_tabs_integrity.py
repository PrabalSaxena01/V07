import subprocess, time, sys, urllib.request, json
sys.path.insert(0, 'scratch')
import cdp_client

def get_page_ws():
    req = urllib.request.urlopen('http://127.0.0.1:9222/json')
    targets = json.loads(req.read().decode('utf-8'))
    for t in targets:
        if t.get('type') == 'page' and 'companion' in t.get('url', ''):
            return t['webSocketDebuggerUrl']
    for t in targets:
        if t.get('type') == 'page':
            return t['webSocketDebuggerUrl']
    raise Exception('No page target')

cdp_client.get_ws_url = get_page_ws

edge = r'C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe'
p = subprocess.Popen([
    edge, '--headless', '--remote-debugging-port=9222', '--disable-gpu', '--no-sandbox',
    '--window-size=1280,800',
    'file:///D:/software/V07-Android/app/src/main/assets/companion.html'
])
time.sleep(5)
try:
    tabs = ['cockpit', 'analytics', 'ledger', 'plugins', 'vault']
    results = {}
    for tab in tabs:
        res = cdp_client.eval_js(f"""(function() {{
            try {{
                switchTab('{tab}');
                var panel = document.getElementById('panel-{tab}');
                return {{
                    tab: '{tab}',
                    activeTab: window._activeTab,
                    panelVisible: panel ? !panel.classList.contains('hidden') : false,
                    error: null
                }};
            }} catch(e) {{
                return {{ tab: '{tab}', error: e.message }};
            }}
        }})()""")
        results[tab] = res
    print("Tab Switching Verification:")
    print(json.dumps(results, indent=2))
finally:
    p.terminate()
