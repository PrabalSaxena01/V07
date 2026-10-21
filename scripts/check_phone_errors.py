import subprocess

cmd = """
Get-CimInstance Win32_PnPEntity | Where-Object { $_.DeviceID -match 'VID_2D95' -or $_.ConfigManagerErrorCode -ne 0 } | Select-Object Name, DeviceID, Status, ConfigManagerErrorCode
"""
res = subprocess.run(["powershell", "-NoProfile", "-Command", cmd], capture_output=True, text=True)
print(res.stdout)
