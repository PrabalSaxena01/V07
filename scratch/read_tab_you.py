with open('D:/software/V07-Android/app/src/main/assets/v3/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

pos_you = text.find('id="tab-you"')
end_you = text.find('</section>', pos_you)
if end_you == -1: end_you = pos_you + 6000
print("Content of tab-you:")
print(text[pos_you:end_you])
