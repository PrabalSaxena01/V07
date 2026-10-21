with open(r'D:\software\V07-Android\app\src\main\java\com\v07\v3\ui\compose\SettingsScreen.kt', 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('val tracker = remember { ScreenTimeTracker(context) }', 'val tracker = remember { ScreenTimeTracker(context, repository) }')
text = text.replace('val syncManager = remember { V3CompanionSyncManager.getInstance(context) }', 'val syncManager = remember { V3CompanionSyncManager.getInstance(context, repository, tracker) }')

old_call = 'syncManager.syncNow(context)'
new_call = '''syncManager.syncNow { success, msg ->
                    scope.launch(Dispatchers.Main) {
                        syncStatusText = msg ?: if (success) "Synchronized just now" else "Sync completed locally"
                        Toast.makeText(context, syncStatusText, Toast.LENGTH_SHORT).show()
                    }
                }'''

text = text.replace(old_call, new_call)

with open(r'D:\software\V07-Android\app\src\main\java\com\v07\v3\ui\compose\SettingsScreen.kt', 'w', encoding='utf-8') as f:
    f.write(text)

print('Updated SettingsScreen.kt')
