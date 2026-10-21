import subprocess

cmd = """
Get-CimInstance Win32_PnPEntity | Where-Object { $_.Present -eq $true -and ($_.Name -match 'iQOO|Vivo|Phone|Android' -or $_.DeviceID -match 'USB') } | Select-Object Name, DeviceID, Present
"""
res = subprocess.run(["powershell", "-NoProfile", "-Command", cmd], capture_output=True, text=True)
print(res.stdout)
