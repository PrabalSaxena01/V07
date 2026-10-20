# -*- coding: utf-8 -*-
from cdp_client import eval_js

script = """
(function() {
  const scrollContainers = [];
  document.querySelectorAll('*').forEach(el => {
    const style = window.getComputedStyle(el);
    if (style.overflowY === 'auto' || style.overflowY === 'scroll') {
      scrollContainers.push({
        id: el.id,
        tag: el.tagName,
        className: el.className,
        scrollHeight: el.scrollHeight,
        clientHeight: el.clientHeight,
        canScroll: el.scrollHeight > el.clientHeight
      });
    }
  });

  return JSON.stringify({
    bodyScrollHeight: document.body.scrollHeight,
    bodyClientHeight: document.body.clientHeight,
    htmlScrollHeight: document.documentElement.scrollHeight,
    htmlClientHeight: document.documentElement.clientHeight,
    scrollContainers: scrollContainers
  });
})()
"""

print(eval_js(script))
