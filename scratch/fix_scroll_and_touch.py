# -*- coding: utf-8 -*-
"""
fix_scroll_and_touch.py
Fixes the root causes of touch blocking and non-responsiveness:
1. Adds 'hidden' class to all 4 modals/drawers when closed so they are 100% excluded from hit-testing.
2. Updates open/close JS functions to toggle 'hidden' with requestAnimationFrame.
3. Fixes html/body scroll hierarchy: html overflow-y: scroll, body overflow-y: visible, touch-action: pan-y.
4. Removes select-none from body so touch gesture tracking is never suppressed.
5. Adds touch-action: manipulation to interactive elements to eliminate the 300ms mobile delay.
"""

import os
import re

TARGET_FILES = [
    r"D:\software\V07-Android\app\src\main\assets\index.html",
    r"D:\software\V07-Android\app\src\main\assets\v3\index.html"
]

def apply_fixes(html):
    # =========================================================================
    # 1. HTML/BODY SCROLL & TOUCH STYLES
    # =========================================================================
    old_body_tag = '<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen select-none">'
    new_body_tag = '<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">'
    if old_body_tag in html:
        html = html.replace(old_body_tag, new_body_tag)

    scroll_css = '''
    /* Impeccable Smooth Touch & Scroll Architecture */
    html {
      height: 100%;
      overflow-x: hidden;
      overflow-y: auto !important;
      -webkit-overflow-scrolling: touch;
      touch-action: pan-y;
    }
    body {
      min-height: 100%;
      overflow-x: hidden;
      overflow-y: visible !important;
      touch-action: pan-y;
      -webkit-overflow-scrolling: touch;
    }
    button, a, input, select, label, [role="button"], .cursor-pointer {
      touch-action: manipulation !important;
      -webkit-tap-highlight-color: transparent;
    }
    '''

    if '/* Impeccable Smooth Touch & Scroll Architecture */' not in html:
        style_marker = '</style>'
        html = html.replace(style_marker, scroll_css + '\n  ' + style_marker, 1)

    # =========================================================================
    # 2. MODALS & DRAWERS: Add 'hidden' to HTML tags by default
    # =========================================================================
    # 2a. v07-modal-container
    html = re.sub(
        r'<div id="v07-modal-container" class="([^"]*)"',
        lambda m: f'<div id="v07-modal-container" class="{m.group(1)} hidden"' if 'hidden' not in m.group(1) else m.group(0),
        html
    )

    # 2b. v07-profile-drawer
    html = re.sub(
        r'<div id="v07-profile-drawer" class="([^"]*)"',
        lambda m: f'<div id="v07-profile-drawer" class="{m.group(1)} hidden"' if 'hidden' not in m.group(1) else m.group(0),
        html
    )

    # 2c. v07-notifications-drawer
    html = re.sub(
        r'<div id="v07-notifications-drawer" class="([^"]*)"',
        lambda m: f'<div id="v07-notifications-drawer" class="{m.group(1)} hidden"' if 'hidden' not in m.group(1) else m.group(0),
        html
    )

    # 2d. v07-plugin-catalog-drawer
    html = re.sub(
        r'<div id="v07-plugin-catalog-drawer" class="([^"]*)"',
        lambda m: f'<div id="v07-plugin-catalog-drawer" class="{m.group(1)} hidden"' if 'hidden' not in m.group(1) else m.group(0),
        html
    )

    # =========================================================================
    # 3. JAVASCRIPT: Update modal & drawer open/close functions with 'hidden'
    # =========================================================================
    # showCustomModal & closeCustomModal
    old_modal_js = re.search(r'function showCustomModal\([\s\S]*?function closeCustomModal\(\)\s*\{[\s\S]*?\}\s*\}', html)
    
    new_modal_open_close = '''    function showCustomModal({
      title,
      subtitle = '',
      icon = 'spa',
      iconBg = 'bg-primary-fixed text-primary',
      bodyHtml = '',
      confirmText = 'Confirm',
      cancelText = 'Cancel',
      isDestructive = false,
      onConfirm = null
    }) {
      const container = document.getElementById('v07-modal-container');
      const card = document.getElementById('v07-modal-card');
      const titleEl = document.getElementById('modal-title');
      const subEl = document.getElementById('modal-subtitle');
      const iconEl = document.getElementById('modal-icon');
      const iconBgEl = document.getElementById('modal-icon-bg');
      const bodyEl = document.getElementById('modal-body');
      const confirmBtn = document.getElementById('modal-confirm-btn');
      const cancelBtn = document.getElementById('modal-cancel-btn');

      if (!container || !card) return;

      if (titleEl) titleEl.textContent = title;
      if (subEl) subEl.textContent = subtitle;
      if (iconEl) iconEl.textContent = icon;
      if (iconBgEl) iconBgEl.className = `w-10 h-10 rounded-2xl ${iconBg} flex items-center justify-center shrink-0 shadow-xs`;
      if (bodyEl) bodyEl.innerHTML = bodyHtml;

      if (confirmBtn) {
        confirmBtn.textContent = confirmText;
        confirmBtn.className = isDestructive
          ? 'px-5 py-2 rounded-xl text-xs font-bold bg-error text-on-error hover:opacity-95 active:scale-95 transition-all shadow-xs cursor-pointer'
          : 'px-5 py-2 rounded-xl text-xs font-bold bg-primary text-on-primary hover:opacity-95 active:scale-95 transition-all shadow-xs cursor-pointer';

        confirmBtn.onclick = () => {
          if (onConfirm) onConfirm();
          closeCustomModal();
        };
      }

      if (cancelBtn) {
        cancelBtn.textContent = cancelText;
      }

      // Show container and animate in
      container.classList.remove('hidden');
      requestAnimationFrame(() => {
        container.classList.remove('pointer-events-none', 'opacity-0');
        card.classList.remove('scale-95');
        card.classList.add('scale-100');
      });
    }

    function closeCustomModal() {
      const container = document.getElementById('v07-modal-container');
      const card = document.getElementById('v07-modal-card');
      if (card) {
        card.classList.remove('scale-100');
        card.classList.add('scale-95');
      }
      if (container) {
        container.classList.add('opacity-0', 'pointer-events-none');
        setTimeout(() => {
          container.classList.add('hidden');
        }, 220);
      }
    }'''

    # Replace showCustomModal & closeCustomModal
    html = re.sub(
        r'function showCustomModal\([\s\S]*?function closeCustomModal\(\)\s*\{[\s\S]*?\}\s*\}',
        new_modal_open_close,
        html,
        count=1
    )

    # Replace openProfileDrawer & closeProfileDrawer
    new_profile_drawer_js = '''    function openProfileDrawer() {
      const drawer = document.getElementById('v07-profile-drawer');
      if (!drawer) return;
      drawer.classList.remove('hidden');
      requestAnimationFrame(() => {
        drawer.classList.remove('pointer-events-none', 'opacity-0');
        const inner = drawer.querySelector('.relative');
        if (inner) {
          inner.classList.remove('translate-y-8', 'scale-95');
          inner.classList.add('translate-y-0', 'scale-100');
        }
      });
    }

    function closeProfileDrawer() {
      const drawer = document.getElementById('v07-profile-drawer');
      if (!drawer) return;
      const inner = drawer.querySelector('.relative');
      if (inner) {
        inner.classList.remove('translate-y-0', 'scale-100');
        inner.classList.add('translate-y-8', 'scale-95');
      }
      drawer.classList.add('opacity-0', 'pointer-events-none');
      setTimeout(() => {
        drawer.classList.add('hidden');
      }, 250);
    }'''

    html = re.sub(
        r'function openProfileDrawer\(\)\s*\{[\s\S]*?function closeProfileDrawer\(\)\s*\{[\s\S]*?\}\s*\}',
        new_profile_drawer_js,
        html,
        count=1
    )

    # Replace openNotificationDrawer & closeNotificationDrawer
    new_notif_drawer_js = '''    function openNotificationDrawer() {
      const drawer = document.getElementById('v07-notifications-drawer');
      if (!drawer) return;
      drawer.classList.remove('hidden');
      requestAnimationFrame(() => {
        drawer.classList.remove('pointer-events-none', 'opacity-0');
        const inner = drawer.querySelector('.relative');
        if (inner) {
          inner.classList.remove('translate-y-8', 'scale-95');
          inner.classList.add('translate-y-0', 'scale-100');
        }
      });
      const dot = document.getElementById('notif-badge-dot');
      if (dot) dot.classList.add('hidden');
    }

    function closeNotificationDrawer() {
      const drawer = document.getElementById('v07-notifications-drawer');
      if (!drawer) return;
      const inner = drawer.querySelector('.relative');
      if (inner) {
        inner.classList.remove('translate-y-0', 'scale-100');
        inner.classList.add('translate-y-8', 'scale-95');
      }
      drawer.classList.add('opacity-0', 'pointer-events-none');
      setTimeout(() => {
        drawer.classList.add('hidden');
      }, 250);
    }'''

    html = re.sub(
        r'function openNotificationDrawer\(\)\s*\{[\s\S]*?function closeNotificationDrawer\(\)\s*\{[\s\S]*?\}\s*\}',
        new_notif_drawer_js,
        html,
        count=1
    )

    # Replace openPluginCatalogDrawer & closePluginCatalogDrawer
    new_catalog_drawer_js = '''    function openPluginCatalogDrawer() {
      const drawer = document.getElementById('v07-plugin-catalog-drawer');
      if (!drawer) return;
      drawer.classList.remove('hidden');
      requestAnimationFrame(() => {
        drawer.classList.remove('pointer-events-none', 'opacity-0');
        const inner = drawer.querySelector('.relative');
        if (inner) {
          inner.classList.remove('translate-y-8', 'scale-95');
          inner.classList.add('translate-y-0', 'scale-100');
        }
      });
      renderPluginCatalog();
    }

    function closePluginCatalogDrawer() {
      const drawer = document.getElementById('v07-plugin-catalog-drawer');
      if (!drawer) return;
      const inner = drawer.querySelector('.relative');
      if (inner) {
        inner.classList.remove('translate-y-0', 'scale-100');
        inner.classList.add('translate-y-8', 'scale-95');
      }
      drawer.classList.add('opacity-0', 'pointer-events-none');
      setTimeout(() => {
        drawer.classList.add('hidden');
      }, 250);
    }'''

    html = re.sub(
        r'function openPluginCatalogDrawer\(\)\s*\{[\s\S]*?function closePluginCatalogDrawer\(\)\s*\{[\s\S]*?\}\s*\}',
        new_catalog_drawer_js,
        html,
        count=1
    )

    return html

def main():
    print("Applying Scroll & Touch Responsiveness fixes...")
    for target in TARGET_FILES:
        if not os.path.exists(target):
            continue
        with open(target, 'r', encoding='utf-8') as f:
            content = f.read()

        updated = apply_fixes(content)
        with open(target, 'w', encoding='utf-8') as f:
            f.write(updated)
        print(f"Updated: {target} ({len(updated)} bytes)")

    print("Scroll and Touch fixes applied successfully!")

if __name__ == '__main__':
    main()
