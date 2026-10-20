import socket
import base64
import os
import json
import urllib.request
import struct
import subprocess

def ensure_adb_forward():
    try:
        res = subprocess.run(['adb', 'shell', 'pidof', 'com.v07.dashboard.v3'], capture_output=True, text=True)
        pid = res.stdout.strip().split()[0] if res.stdout.strip() else None
        if pid:
            subprocess.run(['adb', 'forward', 'tcp:9222', f'localabstract:webview_devtools_remote_{pid}'], capture_output=True)
    except Exception:
        pass

def get_ws_url():
    try:
        req = urllib.request.urlopen('http://127.0.0.1:9222/json', timeout=2)
    except Exception:
        ensure_adb_forward()
        req = urllib.request.urlopen('http://127.0.0.1:9222/json', timeout=3)
    targets = json.loads(req.read().decode('utf-8'))
    for t in targets:
        if 'devtools/page' in t.get('webSocketDebuggerUrl', ''):
            return t['webSocketDebuggerUrl']
    raise Exception("No page target found")

def ws_handshake(sock, host, path):
    key = base64.b64encode(os.urandom(16)).decode('utf-8')
    req = (
        f"GET {path} HTTP/1.1\r\n"
        f"Host: {host}\r\n"
        f"Upgrade: websocket\r\n"
        f"Connection: Upgrade\r\n"
        f"Sec-WebSocket-Key: {key}\r\n"
        f"Sec-WebSocket-Version: 13\r\n\r\n"
    )
    sock.sendall(req.encode('utf-8'))
    res = sock.recv(4096)
    if b"101 " not in res:
        raise Exception("Handshake failed: " + res.decode('utf-8', errors='ignore'))

def send_frame(sock, data):
    msg = data.encode('utf-8')
    length = len(msg)
    mask = os.urandom(4)
    masked_data = bytearray(b ^ mask[i % 4] for i, b in enumerate(msg))
    
    if length <= 125:
        header = struct.pack('!BB', 0x81, 0x80 | length)
    elif length <= 65535:
        header = struct.pack('!BBH', 0x81, 0x80 | 126, length)
    else:
        header = struct.pack('!BBQ', 0x81, 0x80 | 127, length)
        
    sock.sendall(header + mask + masked_data)

def recv_frame(sock):
    head = sock.recv(2)
    if len(head) < 2:
        return None
    b1, b2 = head
    length = b2 & 0x7F
    if length == 126:
        length = struct.unpack('!H', sock.recv(2))[0]
    elif length == 127:
        length = struct.unpack('!Q', sock.recv(8))[0]
        
    data = bytearray()
    while len(data) < length:
        chunk = sock.recv(length - len(data))
        if not chunk:
            break
        data.extend(chunk)
    return data.decode('utf-8', errors='ignore')

def eval_js(expression):
    ws_url = get_ws_url()
    # ws://127.0.0.1:9222/devtools/page/...
    parts = ws_url.replace("ws://", "").split("/", 1)
    host, port = parts[0].split(":")
    path = "/" + parts[1]
    
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect((host, int(port)))
    ws_handshake(s, host, path)
    
    call_id = 1
    cmd = {
        "id": call_id,
        "method": "Runtime.evaluate",
        "params": {
            "expression": expression,
            "returnByValue": True
        }
    }
    send_frame(s, json.dumps(cmd))
    
    while True:
        resp = recv_frame(s)
        if not resp:
            break
        data = json.loads(resp)
        if data.get("id") == call_id:
            s.close()
            res_obj = data.get("result", {})
            if "exceptionDetails" in res_obj:
                return {"exception": res_obj["exceptionDetails"]}
            return res_obj.get("result", {}).get("value")

if __name__ == '__main__':
    import sys
    sys.stdout.reconfigure(encoding='utf-8')
    expr = sys.argv[1] if len(sys.argv) > 1 else "document.title"
    res = eval_js(expr)
    print(json.dumps(res, ensure_ascii=False) if isinstance(res, (dict, list)) else res)
