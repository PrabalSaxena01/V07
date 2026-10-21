import socket

for i in range(1, 35):
    ip = f"192.168.1.{i}"
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.settimeout(0.05)
    res = s.connect_ex((ip, 5555))
    if res == 0:
        print(f"Port 5555 open on {ip}")
    s.close()
