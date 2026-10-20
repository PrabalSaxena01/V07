def update_file(path):
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 1. modal-focus header
    content = content.replace(
        '<div id="modal-focus" class="fixed inset-0 z-50 bg-surface flex flex-col w-full h-full transform translate-y-full opacity-0 pointer-events-none transition-all duration-300">\n    <!-- Full-View Navigation Header -->\n    <div class="h-14 px-4',
        '<div id="modal-focus" class="fixed inset-0 z-50 bg-surface flex flex-col w-full h-full transform translate-y-full opacity-0 pointer-events-none transition-all duration-300">\n    <!-- Full-View Navigation Header -->\n    <div class="pt-8 pb-2 px-4'
    )
    # also try with \r\n
    content = content.replace(
        '<div id="modal-focus" class="fixed inset-0 z-50 bg-surface flex flex-col w-full h-full transform translate-y-full opacity-0 pointer-events-none transition-all duration-300">\r\n    <!-- Full-View Navigation Header -->\r\n    <div class="h-14 px-4',
        '<div id="modal-focus" class="fixed inset-0 z-50 bg-surface flex flex-col w-full h-full transform translate-y-full opacity-0 pointer-events-none transition-all duration-300">\r\n    <!-- Full-View Navigation Header -->\r\n    <div class="pt-8 pb-2 px-4'
    )

    # 2. modal-screentime header
    content = content.replace(
        '<div id="modal-screentime" class="fixed inset-0 z-50 bg-surface flex flex-col w-full h-full transform translate-y-full opacity-0 pointer-events-none transition-all duration-300">\n    <!-- Full-View Navigation Header -->\n    <div class="h-14 px-4',
        '<div id="modal-screentime" class="fixed inset-0 z-50 bg-surface flex flex-col w-full h-full transform translate-y-full opacity-0 pointer-events-none transition-all duration-300">\n    <!-- Full-View Navigation Header -->\n    <div class="pt-8 pb-2 px-4'
    )
    content = content.replace(
        '<div id="modal-screentime" class="fixed inset-0 z-50 bg-surface flex flex-col w-full h-full transform translate-y-full opacity-0 pointer-events-none transition-all duration-300">\r\n    <!-- Full-View Navigation Header -->\r\n    <div class="h-14 px-4',
        '<div id="modal-screentime" class="fixed inset-0 z-50 bg-surface flex flex-col w-full h-full transform translate-y-full opacity-0 pointer-events-none transition-all duration-300">\r\n    <!-- Full-View Navigation Header -->\r\n    <div class="pt-8 pb-2 px-4'
    )

    # 3. modal-recycle-bin header
    content = content.replace(
        '<div id="modal-recycle-bin" class="fixed inset-0 z-50 bg-surface flex flex-col w-full h-full transform translate-y-full opacity-0 pointer-events-none transition-all duration-300">\n    <!-- Full-View Navigation Header -->\n    <div class="h-14 px-4',
        '<div id="modal-recycle-bin" class="fixed inset-0 z-50 bg-surface flex flex-col w-full h-full transform translate-y-full opacity-0 pointer-events-none transition-all duration-300">\n    <!-- Full-View Navigation Header -->\n    <div class="pt-8 pb-2 px-4'
    )
    content = content.replace(
        '<div id="modal-recycle-bin" class="fixed inset-0 z-50 bg-surface flex flex-col w-full h-full transform translate-y-full opacity-0 pointer-events-none transition-all duration-300">\r\n    <!-- Full-View Navigation Header -->\r\n    <div class="h-14 px-4',
        '<div id="modal-recycle-bin" class="fixed inset-0 z-50 bg-surface flex flex-col w-full h-full transform translate-y-full opacity-0 pointer-events-none transition-all duration-300">\r\n    <!-- Full-View Navigation Header -->\r\n    <div class="pt-8 pb-2 px-4'
    )

    # 4. modal-plugin-host header
    content = content.replace(
        '<div id="modal-plugin-host" class="fixed inset-0 z-50 bg-background flex flex-col w-full h-full transition-all duration-300 transform translate-y-full opacity-0 pointer-events-none" style="display: flex;">\n    <!-- Ergonomic M3 Full-View Header -->\n    <header class="h-14 px-4',
        '<div id="modal-plugin-host" class="fixed inset-0 z-50 bg-background flex flex-col w-full h-full transition-all duration-300 transform translate-y-full opacity-0 pointer-events-none" style="display: flex;">\n    <!-- Ergonomic M3 Full-View Header -->\n    <header class="pt-8 pb-2 px-4'
    )
    content = content.replace(
        '<div id="modal-plugin-host" class="fixed inset-0 z-50 bg-background flex flex-col w-full h-full transition-all duration-300 transform translate-y-full opacity-0 pointer-events-none" style="display: flex;">\r\n    <!-- Ergonomic M3 Full-View Header -->\r\n    <header class="h-14 px-4',
        '<div id="modal-plugin-host" class="fixed inset-0 z-50 bg-background flex flex-col w-full h-full transition-all duration-300 transform translate-y-full opacity-0 pointer-events-none" style="display: flex;">\r\n    <!-- Ergonomic M3 Full-View Header -->\r\n    <header class="pt-8 pb-2 px-4'
    )

    with open(path, 'wb') as f:
        f.write(content.encode('utf-8'))
    print('Updated', path)

update_file('D:/software/V07-Android/app/src/main/assets/v3/index.html')
update_file('D:/software/V07-Android/app/src/main/assets/index.html')
