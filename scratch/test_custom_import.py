# -*- coding: utf-8 -*-
from cdp_client import eval_js

script = """
(function() {
  const p = {
    id: 'custom_water',
    name: 'Water Rhythm',
    category: 'Hydration & Body',
    desc: 'Gentle hydration cues and mindful drinking intervals.',
    icon: 'water_drop',
    color: 'text-[#0288D1] bg-[#0288D1]/15',
    enabled: true,
    isBuiltIn: false,
    version: '1.0.0',
    permissions: ['storage:isolated'],
    settings: { target_ml: '2500' }
  };
  sanctuaryPlugins.push(p);
  renderKitTiles();
  const card = document.getElementById('card-plugin-custom_water');
  return JSON.stringify({
    cardCreated: !!card,
    cardTitle: card?.querySelector('h3')?.textContent,
    badgeText: document.getElementById('kit-count-badge')?.textContent
  });
})()
"""

res = eval_js(script)
print("Custom plugin import test (scope fix):")
print(res)
