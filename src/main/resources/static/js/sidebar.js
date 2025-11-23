document.addEventListener('DOMContentLoaded', () => {
    const sidebar = document.getElementById('sidebar');
    const sidebarToggle = document.getElementById('sidebarToggle');
    const mobileSidebarToggle = document.getElementById('mobileSidebarToggle');
    const sidebarOverlay = document.getElementById('sidebarOverlay');
    
    if (!sidebar) return;
    
    if (mobileSidebarToggle) {
        mobileSidebarToggle.addEventListener('click', () => {
            sidebar.classList.add('active');
            sidebarOverlay.classList.add('active');
            document.body.style.overflow = 'hidden';
        });
    }
    
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', () => {
            closeSidebar();
        });
    }
    
    if (sidebarOverlay) {
        sidebarOverlay.addEventListener('click', () => {
            closeSidebar();
        });
    }
    
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && sidebar.classList.contains('active')) {
            closeSidebar();
        }
    });
    
    const currentPath = window.location.pathname;
    const navItems = sidebar.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        const href = item.getAttribute('href');
        if (href && currentPath.startsWith(href) && href !== '/dashboard') {
            item.classList.add('active');
        } else if (href === '/dashboard' && (currentPath === '/' || currentPath === '/dashboard')) {
            item.classList.add('active');
        }
    });
    
    function closeSidebar() {
        sidebar.classList.remove('active');
        sidebarOverlay.classList.remove('active');
        document.body.style.overflow = '';
    }
    
    window.addEventListener('resize', () => {
        if (window.innerWidth >= 992) {
            closeSidebar();
        }
    });
});
