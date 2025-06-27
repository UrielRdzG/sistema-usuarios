// dark-mode.js - JavaScript compartido para modo oscuro

class DarkModeManager {
    constructor() {
        this.init();
    }

    init() {
        this.createToggleButton();
        this.applyTheme(this.getPreferredTheme());
        this.setupEventListeners();
    }

    // Crear botón de toggle si no existe
    createToggleButton() {
        if (!document.getElementById('themeToggle')) {
            const toggleButton = document.createElement('button');
            toggleButton.id = 'themeToggle';
            toggleButton.className = 'theme-toggle';
            toggleButton.title = 'Cambiar tema';
            toggleButton.innerHTML = '<i class="fas fa-moon" id="themeIcon"></i>';
            document.body.appendChild(toggleButton);
        }
    }

    // Obtener tema preferido
    getPreferredTheme() {
        const savedTheme = localStorage.getItem('theme');
        if (savedTheme) {
            return savedTheme;
        }
        return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }

    // Aplicar tema
    applyTheme(theme) {
        const body = document.body;
        const themeIcon = document.getElementById('themeIcon');

        if (theme === 'dark') {
            body.setAttribute('data-theme', 'dark');
            if (themeIcon) {
                themeIcon.classList.remove('fa-moon');
                themeIcon.classList.add('fa-sun');
            }
        } else {
            body.removeAttribute('data-theme');
            if (themeIcon) {
                themeIcon.classList.remove('fa-sun');
                themeIcon.classList.add('fa-moon');
            }
        }

        localStorage.setItem('theme', theme);
        this.updateBootstrapComponents(theme);
    }

    // Actualizar componentes de Bootstrap
    updateBootstrapComponents(theme) {
        // Actualizar clases de Bootstrap según el tema
        const elementsToUpdate = [
            { selector: '.navbar', darkClass: 'navbar-dark', lightClass: 'navbar-light' },
            { selector: '.card', darkClass: 'bg-dark', lightClass: 'bg-white' },
            { selector: '.modal-content', darkClass: 'bg-dark', lightClass: 'bg-white' },
            { selector: '.dropdown-menu', darkClass: 'dropdown-menu-dark', lightClass: '' }
        ];

        elementsToUpdate.forEach(({ selector, darkClass, lightClass }) => {
            const elements = document.querySelectorAll(selector);
            elements.forEach(element => {
                if (theme === 'dark') {
                    element.classList.remove(lightClass);
                    if (darkClass) element.classList.add(darkClass);
                } else {
                    element.classList.remove(darkClass);
                    if (lightClass) element.classList.add(lightClass);
                }
            });
        });
    }

    // Configurar event listeners
    setupEventListeners() {
        const themeToggle = document.getElementById('themeToggle');
        if (themeToggle) {
            themeToggle.addEventListener('click', () => {
                const currentTheme = document.body.getAttribute('data-theme');
                const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
                this.applyTheme(newTheme);

                // Añadir efecto visual al botón
                themeToggle.style.transform = 'scale(0.9)';
                setTimeout(() => {
                    themeToggle.style.transform = 'scale(1)';
                }, 150);
            });
        }

        // Detectar cambios en preferencia del sistema
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
            if (!localStorage.getItem('theme')) {
                this.applyTheme(e.matches ? 'dark' : 'light');
            }
        });
    }

    // Método público para cambiar tema programáticamente
    toggleTheme() {
        const currentTheme = document.body.getAttribute('data-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        this.applyTheme(newTheme);
    }

    // Método público para obtener tema actual
    getCurrentTheme() {
        return document.body.getAttribute('data-theme') || 'light';
    }

    // Método público para aplicar tema específico
    setTheme(theme) {
        if (theme === 'dark' || theme === 'light') {
            this.applyTheme(theme);
        }
    }

    // Método para inicializar componentes específicos de página
    initPageComponents() {
        // Configurar alerts auto-hide
        this.setupAutoHideAlerts();

        // Configurar tooltips y popovers
        this.initBootstrapComponents();
    }

    // Auto-hide alerts después de 5 segundos
    setupAutoHideAlerts() {
        setTimeout(() => {
            const alerts = document.querySelectorAll('.alert');
            alerts.forEach(alert => {
                if (window.bootstrap && window.bootstrap.Alert) {
                    const bootstrapAlert = new window.bootstrap.Alert(alert);
                    bootstrapAlert.close();
                }
            });
        }, 5000);
    }

    // Inicializar componentes de Bootstrap
    initBootstrapComponents() {
        // Inicializar tooltips
        if (window.bootstrap && window.bootstrap.Tooltip) {
            const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
            tooltipTriggerList.map(tooltipTriggerEl => {
                return new window.bootstrap.Tooltip(tooltipTriggerEl);
            });
        }

        // Inicializar popovers
        if (window.bootstrap && window.bootstrap.Popover) {
            const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
            popoverTriggerList.map(popoverTriggerEl => {
                return new window.bootstrap.Popover(popoverTriggerEl);
            });
        }
    }

    // Añadir clase de animación a elementos
    addFadeInAnimation(selector) {
        const elements = document.querySelectorAll(selector);
        elements.forEach((element, index) => {
            setTimeout(() => {
                element.classList.add('fade-in');
            }, index * 100);
        });
    }
}

// Inicializar Dark Mode Manager cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
    window.darkModeManager = new DarkModeManager();

    // Añadir animaciones a las cards
    window.darkModeManager.addFadeInAnimation('.card');

    // Inicializar componentes de página
    window.darkModeManager.initPageComponents();
});

// Utilidades adicionales para modo oscuro
const DarkModeUtils = {
    // Verificar si está en modo oscuro
    isDarkMode() {
        return document.body.getAttribute('data-theme') === 'dark';
    },

    // Obtener color según el tema
    getThemedColor(lightColor, darkColor) {
        return this.isDarkMode() ? darkColor : lightColor;
    },

    // Aplicar estilos condicionales según el tema
    applyThemedStyles(element, lightStyles, darkStyles) {
        const styles = this.isDarkMode() ? darkStyles : lightStyles;
        Object.assign(element.style, styles);
    },

    // Cambiar src de imagen según el tema
    updateThemedImage(img, lightSrc, darkSrc) {
        img.src = this.isDarkMode() ? darkSrc : lightSrc;
    }
};

// Hacer disponibles las utilidades globalmente
window.DarkModeUtils = DarkModeUtils;