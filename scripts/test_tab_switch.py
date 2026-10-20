# -*- coding: utf-8 -*-
from cdp_client import eval_js

print(eval_js("""
(function() {
  switchTab('tools');
  const t = document.getElementById('tab-tools');
  const h = document.getElementById('tab-home');
  return JSON.stringify({
    toolsClass: t?.className,
    homeClass: h?.className,
    toolsOffsetHeight: t?.offsetHeight,
    activeTab: window.activeTab
  });
})()
"""))
