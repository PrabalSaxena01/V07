import sys
sys.stdout.reconfigure(encoding='utf-8')
html = open(r'D:\software\V07-Android\app\src\main\assets\v3\index.html', encoding='utf-8').read()
pos = html.find('focusCurrentMode')
print(html[pos-100:pos+3500])
