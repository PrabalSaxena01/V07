import subprocess, time, sys, urllib.request, json
sys.path.insert(0, 'scratch')
import cdp_client

def get_page_ws():
    req = urllib.request.urlopen('http://127.0.0.1:9222/json')
    targets = json.loads(req.read().decode('utf-8'))
    for t in targets:
        if t.get('type') == 'page' and 'companion' in t.get('url', ''):
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
    print('HTML length:', cdp_client.eval_js('document.body.innerHTML.length'))
    print('chronoSection exists?', cdp_client.eval_js('!!document.getElementById("chronoSection")'))
    print('sections:', cdp_client.eval_js('Array.from(document.querySelectorAll("section")).map(s => s.id)'))
    print('activeTab:', cdp_client.eval_js('window._activeTab'))
    print('cockpit innerHTML preview:', cdp_client.eval_js('document.getElementById("panel-cockpit") ? document.getElementById("panel-cockpit").innerHTML.slice(0, 300) : "null"'))
finally:
    p.terminate()
