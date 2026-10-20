from PIL import Image
im = Image.open('D:/software/V07-Android/output/03_settings_tab.png')

# Section 1 profile is near top (y=200..800)
# Section 2 Android device integration is near y=800..1400
# Let's crop y=700..1400
im_crop = im.crop((0, 700, 1080, 1500))
im_crop.save('D:/software/V07-Android/output/slice_settings_android.png')
print("Saved slice_settings_android.png")
