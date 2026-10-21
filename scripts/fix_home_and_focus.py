# Fix HomeScreen.kt
with open(r'D:\software\V07-Android\app\src\main\java\com\v07\v3\ui\compose\HomeScreen.kt', 'r', encoding='utf-8') as f:
    text = f.read()

old_block = '''                    hasUsagePermission = ScreenTimeTracker.hasUsageStatsPermission(context)
                    if (hasUsagePermission) {
                        val usage = ScreenTimeTracker.collectForDate(context, System.currentTimeMillis())
                        val totalMs = usage.sumOf { it.usageDurationMs }
                        val hrs = totalMs / (1000 * 60 * 60)
                        val mins = (totalMs % (1000 * 60 * 60)) / (1000 * 60)
                        todayScreenTimeFormatted = if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
                    }'''

new_block = '''                    hasUsagePermission = screenTimeTracker.hasUsageStatsPermission()
                    if (hasUsagePermission) {
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                        val summary = screenTimeTracker.collectForDate(sdf.format(java.util.Date()))
                        val totalMs = summary.totalDurationMs
                        val hrs = totalMs / (1000 * 60 * 60)
                        val mins = (totalMs % (1000 * 60 * 60)) / (1000 * 60)
                        todayScreenTimeFormatted = if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
                    }'''

text = text.replace(old_block, new_block)

with open(r'D:\software\V07-Android\app\src\main\java\com\v07\v3\ui\compose\HomeScreen.kt', 'w', encoding='utf-8') as f:
    f.write(text)

print('Updated HomeScreen.kt')

# Fix FocusSanctuaryScreen.kt
with open(r'D:\software\V07-Android\app\src\main\java\com\v07\v3\ui\compose\FocusSanctuaryScreen.kt', 'r', encoding='utf-8') as f:
    f_text = f.read()

f_text = f_text.replace(
    'val timerManager = remember { TimerManager.getInstance(context) }\n    val repository = remember { EventRepository.getInstance(context) }',
    'val repository = remember { EventRepository.getInstance(context) }\n    val timerManager = remember { TimerManager.getInstance(context, repository) }'
)
f_text = f_text.replace(
    'val timerManager = remember { TimerManager.getInstance(context) }',
    'val repository = remember { EventRepository.getInstance(context) }\n    val timerManager = remember { TimerManager.getInstance(context, repository) }'
)

f_text = f_text.replace('targetDurationMs = targetMs', 'targetMinutes = selectedPreset.minutes')
f_text = f_text.replace('finishSession(notes = intentionText)', 'finishSession(finalNotes = intentionText)')

with open(r'D:\software\V07-Android\app\src\main\java\com\v07\v3\ui\compose\FocusSanctuaryScreen.kt', 'w', encoding='utf-8') as f:
    f.write(f_text)

print('Updated FocusSanctuaryScreen.kt')
