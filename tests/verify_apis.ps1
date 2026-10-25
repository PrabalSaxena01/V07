$ErrorActionPreference = "Continue"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "     V07 V3 SYSTEMATIC API & DATABASE VERIFICATION        " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 0. Port Forwarding Check
adb forward tcp:8080 tcp:8000
Start-Sleep -Milliseconds 500

$baseUrl = "http://127.0.0.1:8080"
$results = [System.Collections.Generic.List[PSCustomObject]]::new()

function Test-Endpoint {
    param(
        [string]$Category,
        [string]$Name,
        [string]$Method = "GET",
        [string]$Path,
        [string]$Body = $null,
        [hashtable]$Headers = @{},
        [scriptblock]$Validator
    )

    $url = "$baseUrl$Path"
    $headersObj = @{ "Accept" = "application/json" }
    foreach ($k in $Headers.Keys) { $headersObj[$k] = $Headers[$k] }
    if ($Body) { $headersObj["Content-Type"] = "application/json" }

    $status = 0
    $content = ""
    $json = $null
    $pass = $false
    $detail = ""

    try {
        $params = @{
            Uri = $url
            Method = $Method
            Headers = $headersObj
            TimeoutSec = 8
            UseBasicParsing = $true
        }
        if ($Body) { $params["Body"] = $Body }

        $res = Invoke-WebRequest @params
        $status = $res.StatusCode
        $content = $res.Content
        try { $json = $content | ConvertFrom-Json } catch {}

        if ($Validator) {
            $pass = & $Validator $json $status $content
            if ($pass) {
                $detail = "OK"
            } else {
                $detail = "Validation failed: $($content.Substring(0, [Math]::Min(120, $content.Length)))"
            }
        } else {
            $pass = ($status -ge 200 -and $status -lt 300)
            $detail = "HTTP $status"
        }
    } catch {
        $status = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { 0 }
        $detail = "Exception: $($_.Exception.Message)"
        $pass = $false
    }

    $color = if ($pass) { "Green" } else { "Red" }
    $symbol = if ($pass) { "[PASS]" } else { "[FAIL]" }
    Write-Host ("{0} {1,-18} | {2,-30} | {3}" -f $symbol, $Category, $Name, $detail) -ForegroundColor $color

    $results.Add([PSCustomObject]@{
        Category = $Category
        Name = $Name
        Method = $Method
        Path = $Path
        Status = $status
        Passed = $pass
        Detail = $detail
        Json = $json
    })

    return $json
}

# ─── 1. STATUS & TOKEN AUTO-DISCOVERY ────────────────────────────────────────
Write-Host "`n--- 1. STATUS & TOKEN AUTO-DISCOVERY ---" -ForegroundColor Yellow
$statusJson = Test-Endpoint -Category "Status" -Name "GET /api/v3/status" -Path "/api/v3/status" -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true -and $j.session_token -and $j.database -and $j.timer)
}

$sessionToken = if ($statusJson -and $statusJson.session_token) { $statusJson.session_token } else { "" }
Write-Host "Discovered Session Token: $sessionToken" -ForegroundColor Cyan
$authHeaders = @{ "X-V07-Token" = $sessionToken }

# ─── 2. PROFILE DATA & MULTI-USER ISOLATION ──────────────────────────────────
Write-Host "`n--- 2. PROFILE DATA & MULTI-USER ISOLATION ---" -ForegroundColor Yellow
Test-Endpoint -Category "Profile" -Name "GET /api/v3/admin/profiles" -Path "/api/v3/admin/profiles" -Headers $authHeaders -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true -and $j.active_profile -and $j.profiles)
}

Test-Endpoint -Category "Profile" -Name "POST /api/v3/admin/profiles/switch" -Method "POST" -Path "/api/v3/admin/profiles/switch" -Body '{"profileId":"guest"}' -Headers $authHeaders -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true -and $j.active_profile -eq "guest")
}

# ─── 3. CORE UTILITY: SESSIONS / FOCUS TIMER ──────────────────────────────────
Write-Host "`n--- 3. CORE UTILITY: SESSIONS / FOCUS TIMER ---" -ForegroundColor Yellow
Test-Endpoint -Category "Session" -Name "GET /session/active" -Path "/session/active" -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true -and $j.session)
}

Test-Endpoint -Category "Session" -Name "GET /session/recent-activities" -Path "/session/recent-activities" -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true -and $j.activities)
}

Test-Endpoint -Category "Session" -Name "POST /session/start" -Method "POST" -Path "/session/start" -Body '{"activity":"Coding","custom_label":"V3 API Test","timer_mode":"stopwatch","timer_seconds":0}' -Validator {
    param($j, $s)
    return ($s -eq 201 -and $j.ok -eq $true -and $j.action -eq "started")
}

Test-Endpoint -Category "Session" -Name "POST /session/pause" -Method "POST" -Path "/session/pause" -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true)
}

Test-Endpoint -Category "Session" -Name "POST /session/resume" -Method "POST" -Path "/session/resume" -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true)
}

Test-Endpoint -Category "Session" -Name "POST /session/stop" -Method "POST" -Path "/session/stop" -Body '{"notes":"Test session finished"}' -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true -and $j.action -eq "stopped")
}

Test-Endpoint -Category "Session" -Name "GET /api/v3/sessions" -Path "/api/v3/sessions?limit=5" -Headers $authHeaders -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true -and $j.sessions.Count -ge 1)
}

# ─── 4. CORE UTILITY: TASKS & CHECKLIST ──────────────────────────────────────
Write-Host "`n--- 4. CORE UTILITY: TASKS & CHECKLIST ---" -ForegroundColor Yellow
Test-Endpoint -Category "Task" -Name "GET /tasks" -Path "/tasks" -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true -and $j.tasks)
}

Test-Endpoint -Category "Task" -Name "GET /checklist/today" -Path "/checklist/today" -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true -and $j.items)
}

$taskCreate = Test-Endpoint -Category "Task" -Name "POST /checklist/today (create)" -Method "POST" -Path "/checklist/today" -Body '{"title":"Verify V3 Architecture","priority":"HIGH"}' -Validator {
    param($j, $s)
    return ($s -eq 201 -and $j.ok -eq $true -and $j.id)
}

if ($taskCreate -and $taskCreate.id) {
    $tid = $taskCreate.id
    Test-Endpoint -Category "Task" -Name "POST /checklist/$tid/toggle" -Method "POST" -Path "/checklist/$tid/toggle" -Validator {
        param($j, $s)
        return ($s -eq 200 -and $j.ok -eq $true)
    }

    Test-Endpoint -Category "Task" -Name "DELETE /task/$tid (soft-delete)" -Method "DELETE" -Path "/task/$tid" -Validator {
        param($j, $s)
        return ($s -eq 200 -and $j.ok -eq $true -and $j.recycled -eq $true)
    }
}

# ─── 5. CORE UTILITY: THOUGHTS & REFLECTIONS ─────────────────────────────────
Write-Host "`n--- 5. CORE UTILITY: THOUGHTS & REFLECTIONS ---" -ForegroundColor Yellow
Test-Endpoint -Category "Thought" -Name "GET /thoughts" -Path "/thoughts" -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true -and $j.thoughts)
}

$thoughtCreate = Test-Endpoint -Category "Thought" -Name "POST /thoughts (create)" -Method "POST" -Path "/thoughts" -Body '{"topic":"Architecture","content":"Sanctuary V3 is built on modularity."}' -Validator {
    param($j, $s)
    return ($s -eq 201 -and $j.ok -eq $true -and $j.id)
}

Test-Endpoint -Category "Thought" -Name "POST /thought/visit" -Method "POST" -Path "/thought/visit?thought_id=1&visited=true" -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true)
}

if ($thoughtCreate -and $thoughtCreate.id) {
    $thid = $thoughtCreate.id
    Test-Endpoint -Category "Thought" -Name "DELETE /event/thought/$thid" -Method "DELETE" -Path "/event/thought/$thid" -Validator {
        param($j, $s)
        return ($s -eq 200 -and $j.ok -eq $true)
    }
}

# ─── 6. CORE UTILITY: DAY TRACKER & TIMELINE ─────────────────────────────────
Write-Host "`n--- 6. CORE UTILITY: DAY TRACKER & TIMELINE ---" -ForegroundColor Yellow
Test-Endpoint -Category "Cadence" -Name "GET /daytracker" -Path "/daytracker" -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true -and $j.items -and $j.entries)
}

$todayStr = (Get-Date).ToString("yyyy-MM-dd")
Test-Endpoint -Category "Cadence" -Name "GET /day/$todayStr" -Path "/day/$todayStr" -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true -and $j.events)
}

Test-Endpoint -Category "Cadence" -Name "GET /timeline" -Path "/timeline" -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true -and $j.events)
}

Test-Endpoint -Category "Cadence" -Name "GET /database" -Path "/database?days=30" -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true -and $j.events)
}

Test-Endpoint -Category "Cadence" -Name "GET /meta/activities" -Path "/meta/activities" -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.Count -gt 0)
}

# ─── 7. CORE UTILITY: SCREEN TIME & APP LIMITS ───────────────────────────────
Write-Host "`n--- 7. CORE UTILITY: SCREEN TIME & APP LIMITS ---" -ForegroundColor Yellow
Test-Endpoint -Category "ScreenTime" -Name "GET /api/v3/screentime" -Path "/api/v3/screentime" -Validator {
    param($j, $s)
    return ($s -eq 200 -or $s -eq 403) # 403 if usage stats permission not toggled in Android OS
}

Test-Endpoint -Category "ScreenTime" -Name "GET /app-limits" -Path "/app-limits" -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true)
}

# ─── 8. CORE UTILITY: GITA WISDOM ───────────────────────────────────────────
Write-Host "`n--- 8. CORE UTILITY: GITA WISDOM ---" -ForegroundColor Yellow
Test-Endpoint -Category "Gita" -Name "GET /gita/today" -Path "/gita/today" -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.chapter -and $j.verse)
}

Test-Endpoint -Category "Gita" -Name "GET /gita/verse/2.47" -Path "/gita/verse/2.47" -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.chapter -eq 2 -and $j.verse -eq 47)
}

Test-Endpoint -Category "Gita" -Name "GET /gita/all" -Path "/gita/all" -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.chapters -and $j.total -ge 700)
}

# ─── 9. MODULAR PLUGINS PLATFORM ────────────────────────────────────────────
Write-Host "`n--- 9. MODULAR PLUGINS PLATFORM ---" -ForegroundColor Yellow
Test-Endpoint -Category "Plugins" -Name "GET /api/v3/plugins" -Path "/api/v3/plugins" -Headers $authHeaders -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true -and $j.plugins.Count -ge 1)
}

Test-Endpoint -Category "Plugins" -Name "GET /plugins/registry" -Path "/plugins/registry" -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true -and $j.plugins.Count -ge 1)
}

Test-Endpoint -Category "Plugins" -Name "POST /api/v3/plugins/com.v07.notes/data" -Method "POST" -Path "/api/v3/plugins/com.v07.notes/data" -Body '{"key":"draft_1","value":"Hello Plugin"}' -Headers $authHeaders -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true)
}

Test-Endpoint -Category "Plugins" -Name "GET /api/v3/plugins/com.v07.notes/data?key=draft_1" -Path "/api/v3/plugins/com.v07.notes/data?key=draft_1" -Headers $authHeaders -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true -and $j.value -eq "Hello Plugin")
}

Test-Endpoint -Category "Plugins" -Name "POST /api/v3/plugins/com.v07.vitals/events" -Method "POST" -Path "/api/v3/plugins/com.v07.vitals/events" -Body '{"category":"heartrate","payloadJson":"{\"bpm\":72}"}' -Headers $authHeaders -Validator {
    param($j, $s)
    return ($s -eq 201 -and $j.ok -eq $true)
}

# ─── 10. RECYCLE BIN & TOMBSTONES ───────────────────────────────────────────
Write-Host "`n--- 10. RECYCLE BIN & TOMBSTONES ---" -ForegroundColor Yellow
$rbList = Test-Endpoint -Category "RecycleBin" -Name "GET /recycle-bin" -Path "/recycle-bin" -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true -and $j.entries -ne $null)
}

Test-Endpoint -Category "RecycleBin" -Name "GET /api/v3/recyclebin" -Path "/api/v3/recyclebin" -Headers $authHeaders -Validator {
    param($j, $s)
    return ($s -eq 200 -and $j.ok -eq $true -and $j.items -ne $null)
}

if ($rbList -and $rbList.entries -and $rbList.entries.Count -gt 0) {
    $firstItem = $rbList.entries[0]
    $rid = $firstItem.recycle_id
    Test-Endpoint -Category "RecycleBin" -Name "POST /recycle-bin/$rid/restore" -Method "POST" -Path "/recycle-bin/$rid/restore" -Validator {
        param($j, $s)
        return ($s -eq 200 -and $j.ok -eq $true)
    }
}

# ─── SUMMARY REPORT ─────────────────────────────────────────────────────────
$total = $results.Count
$passed = ($results | Where-Object { $_.Passed }).Count
$failed = $total - $passed

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "                TEST VERIFICATION SUMMARY                " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "TOTAL TESTS : $total"
Write-Host "PASSED      : $passed" -ForegroundColor Green
Write-Host "FAILED      : $failed" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Red" })
Write-Host "==========================================================" -ForegroundColor Cyan
