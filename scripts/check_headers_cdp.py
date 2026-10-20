# -*- coding: utf-8 -*-
from cdp_client import eval_js
import json

res = eval_js("""
JSON.stringify({
  hasSettingsBtnInFocus: !!document.querySelector('#view-focus button[onclick*="openPluginSettingsModal"]'),
  hasSettingsBtnInReading: !!document.querySelector('#view-reading button[onclick*="openPluginSettingsModal"]'),
  hasSettingsBtnInHabits: !!document.querySelector('#view-habits button[onclick*="openPluginSettingsModal"]'),
  hasSettingsBtnInScreenTime: !!document.querySelector('#view-screentime button[onclick*="openPluginSettingsModal"]'),
  hasSettingsBtnInMood: !!document.querySelector('#view-mood button[onclick*="openPluginSettingsModal"]'),
  hasSettingsBtnInGita: !!document.querySelector('#view-gita button[onclick*="openPluginSettingsModal"]'),
  hasSettingsBtnInWeekly: !!document.querySelector('#view-weekly button[onclick*="openPluginSettingsModal"]')
})
""")

print("In-page view header results:")
print(res)
