import subprocess

cmd = """
Get-CimInstance Win32_PnPEntity | Where-Object { $_.PNPClass -in @('USB', 'AndroidUsbDeviceType', 'WPD') } | Select-Object Name, DeviceID, Status
"""
res = subprocess.run(["powershell", "-NoProfile", "-Command", cmd], capture_output=True, text=True)
print(res.stdout)
