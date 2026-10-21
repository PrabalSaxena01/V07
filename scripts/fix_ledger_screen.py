with open(r'D:\software\V07-Android\app\src\main\java\com\v07\v3\ui\compose\LedgerScreen.kt', 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('textColor = MaterialTheme.colorScheme.onSurface', 'focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface')

with open(r'D:\software\V07-Android\app\src\main\java\com\v07\v3\ui\compose\LedgerScreen.kt', 'w', encoding='utf-8') as f:
    f.write(text)

print('Updated LedgerScreen.kt')
