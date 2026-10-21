import subprocess

cmd = """
Get-CimInstance Win32_PnPEntity | Where-Object { $_.DeviceID -match '2D95' } | Select-Object Name, DeviceID, Status, HardwareID
"""
res = subprocess.run(["powershell", "-NoProfile", "-Command", cmd], capture_output=True, text=True)
print(res.stdout)
