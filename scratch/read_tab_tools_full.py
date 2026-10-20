with open('D:/software/V07-Android/app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

pos_tools = text.find('id="tab-tools"')
end_tools = text.find('id="tab-journal"', pos_tools)
print("tab-tools section:")
print(text[pos_tools:pos_tools+4000])
