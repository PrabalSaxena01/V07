# -*- coding: utf-8 -*-
from cdp_client import eval_js

script = """
(function() {
  const idx = sanctuaryPlugins.findIndex(p => p.id === 'custom_water');
  if (idx !== -1) sanctuaryPlugins.splice(idx, 1);
  renderKitTiles();
  const card = document.getElementById('card-plugin-custom_water');
  return JSON.stringify({
    cardRemoved: !card,
    badgeText: document.getElementById('kit-count-badge')?.textContent
  });
})()
"""

res = eval_js(script)
print("Custom plugin removal test:")
print(res)
