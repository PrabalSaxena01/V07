import subprocess, time, sys, urllib.request, json
sys.path.insert(0, 'scratch')
import cdp_client

edge = r'C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe'

# 1. Capture Screenshots
print("Capturing desktop screenshot...")
subprocess.run([
    edge, '--headless', '--disable-gpu', '--no-sandbox',
    '--window-size=1280,800',
    r'--screenshot=D:\software\V07-Android\scratch\screen_desktop_zero_scroll.png',
    'file:///D:/software/V07-Android/app/src/main/assets/companion.html'
], check=True)

print("Capturing mobile screenshot...")
subprocess.run([
    edge, '--headless', '--disable-gpu', '--no-sandbox',
    '--window-size=390,844',
    r'--screenshot=D:\software\V07-Android\scratch\screen_mobile_zero_scroll.png',
    'file:///D:/software/V07-Android/app/src/main/assets/companion.html'
], check=True)

# 2. Layout Metrics via CDP
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

def get_metrics(width, height):
    p = subprocess.Popen([
        edge, '--headless', '--remote-debugging-port=9222', '--disable-gpu', '--no-sandbox',
        f'--window-size={width},{height}',
        'file:///D:/software/V07-Android/app/src/main/assets/companion.html'
    ])
    time.sleep(2.5)
    try:
        js = """(function() {
            var cockpit = document.getElementById('panel-cockpit');
            var chrono = document.getElementById('chronoSection');
            var obligations = document.getElementById('obligationsSection');
            var tools = document.getElementById('toolsSection');
            var content = document.getElementById('contentContainer');
            return {
                windowWidth: window.innerWidth,
                windowHeight: window.innerHeight,
                cockpitHeight: cockpit ? cockpit.offsetHeight : null,
                contentHeight: content ? content.offsetHeight : null,
                contentScrollHeight: content ? content.scrollHeight : null,
                chronoWidth: chrono ? chrono.offsetWidth : null,
                chronoHeight: chrono ? chrono.offsetHeight : null,
                obligationsWidth: obligations ? obligations.offsetWidth : null,
                obligationsHeight: obligations ? obligations.offsetHeight : null,
                toolsWidth: tools ? tools.offsetWidth : null,
                toolsHeight: tools ? tools.offsetHeight : null
            };
        })()"""
        metrics = cdp_client.eval_js(js)
        return metrics
    finally:
        p.terminate()
        time.sleep(1)

print("Testing Desktop metrics:")
print(json.dumps(get_metrics(1280, 800), indent=2))

print("Testing Mobile metrics:")
print(json.dumps(get_metrics(390, 844), indent=2))
