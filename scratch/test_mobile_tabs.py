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
    '--window-size=390,844',
    'file:///D:/software/V07-Android/app/src/main/assets/companion.html'
])
time.sleep(5)
try:
    # 1. Focus Tab
    cdp_client.eval_js("setMobileCockpitTab('focus');")
    time.sleep(0.3)
    focus_metrics = cdp_client.eval_js("""({
        tab: 'focus',
        chronoVisible: !document.getElementById('chronoSection').classList.contains('hidden'),
        obligationsVisible: !document.getElementById('obligationsSection').classList.contains('hidden'),
        toolsVisible: !document.getElementById('toolsSection').classList.contains('hidden'),
        cardHeight: document.getElementById('chronoSection').offsetHeight,
        contentHeight: document.getElementById('contentContainer').offsetHeight,
        contentScrollHeight: document.getElementById('contentContainer').scrollHeight
    })""")
    print("Focus Tab Metrics:", json.dumps(focus_metrics, indent=2))

    # 2. Click Tasks
    cdp_client.eval_js("setMobileCockpitTab('obligations');")
    time.sleep(0.3)
    tasks_metrics = cdp_client.eval_js("""({
        tab: 'obligations',
        chronoVisible: !document.getElementById('chronoSection').classList.contains('hidden'),
        obligationsVisible: !document.getElementById('obligationsSection').classList.contains('hidden'),
        toolsVisible: !document.getElementById('toolsSection').classList.contains('hidden'),
        cardHeight: document.getElementById('obligationsSection').offsetHeight,
        contentHeight: document.getElementById('contentContainer').offsetHeight,
        contentScrollHeight: document.getElementById('contentContainer').scrollHeight
    })""")
    print("Tasks Tab Metrics:", json.dumps(tasks_metrics, indent=2))

    # 3. Click Tools
    cdp_client.eval_js("setMobileCockpitTab('tools');")
    time.sleep(0.3)
    tools_metrics = cdp_client.eval_js("""({
        tab: 'tools',
        chronoVisible: !document.getElementById('chronoSection').classList.contains('hidden'),
        obligationsVisible: !document.getElementById('obligationsSection').classList.contains('hidden'),
        toolsVisible: !document.getElementById('toolsSection').classList.contains('hidden'),
        cardHeight: document.getElementById('toolsSection').offsetHeight,
        contentHeight: document.getElementById('contentContainer').offsetHeight,
        contentScrollHeight: document.getElementById('contentContainer').scrollHeight
    })""")
    print("Tools Tab Metrics:", json.dumps(tools_metrics, indent=2))
finally:
    p.terminate()
