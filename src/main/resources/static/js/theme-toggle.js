(function() {
    'use strict';
    
    const currentTheme = localStorage.getItem('theme') || 'light';
    
    document.documentElement.setAttribute('data-theme', currentTheme);
    
    function updateAllThemeIcons(theme) {
        const themeToggles = document.querySelectorAll('#themeToggle, .theme-toggle');
        themeToggles.forEach(toggle => {
            const icon = toggle.querySelector('i');
            const span = toggle.querySelector('span');
            if (icon) {
                icon.className = theme === 'dark' 
                    ? 'bi bi-sun-fill' 
                    : 'bi bi-moon-fill';
            }
            if (span && span.style.display !== 'none') {
                span.textContent = theme === 'dark' ? 'Light Mode' : 'Dark Mode';
            }
            toggle.setAttribute('aria-label', 
                theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode');
            toggle.setAttribute('title', 
                theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode');
        });
    }
    
    function toggleTheme() {
        const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        
        document.documentElement.setAttribute('data-theme', newTheme);
        localStorage.setItem('theme', newTheme);
        updateAllThemeIcons(newTheme);
    }
    
    document.addEventListener('DOMContentLoaded', function() {
        const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
        updateAllThemeIcons(currentTheme);
        
        const themeToggles = document.querySelectorAll('#themeToggle, .theme-toggle');
        themeToggles.forEach(toggle => {
            toggle.addEventListener('click', toggleTheme);
        });
    });
    
    window.toggleTheme = toggleTheme;
})();
