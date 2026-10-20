from PIL import Image

im_tools = Image.open('D:/software/V07-Android/output/02_tools_tab.png')
im_tools.crop((0, 250, 1080, 1600)).save('D:/software/V07-Android/output/slice_tools_core.png')
print("Saved slice_tools_core.png")
