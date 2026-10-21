import subprocess

cmd = """
$shell = New-Object -ComObject Shell.Application
$computer = $shell.Namespace(17) # ssfDRIVES
$phone = $computer.Items() | Where-Object { $_.Name -match 'iQOO|Z9x|Phone' }
if ($phone) {
    Write-Output "Found phone via MTP: $($phone.Name)"
    $phoneFolder = $phone.GetFolder
    $items = $phoneFolder.Items()
    foreach ($item in $items) {
        Write-Output "  Storage: $($item.Name)"
    }
} else {
    Write-Output "Phone not found in Shell drives"
}
"""
res = subprocess.run(["powershell", "-NoProfile", "-Command", cmd], capture_output=True, text=True)
print(res.stdout)
