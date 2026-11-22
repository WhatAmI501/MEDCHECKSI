/* ====================================================================
   MEDCHECK (MedIA) — Gerenciador de Tema Claro / Escuro
   Persistência em localStorage + Prevenção de FOUC
   ==================================================================== */

function aplicarTema(tema) {
    document.documentElement.setAttribute('data-theme', tema);
    try {
        localStorage.setItem('medcheck_theme', tema);
    } catch (e) {}

    const isDark = tema === 'dark';

    // Atualizar ícones da Topbar
    const moon = document.getElementById('theme-icon-moon');
    const sun = document.getElementById('theme-icon-sun');
    if (moon && sun) {
        moon.style.display = isDark ? 'none' : 'block';
        sun.style.display = isDark ? 'block' : 'none';
    }

    // Atualizar ícones e texto da Sidebar
    const sbMoon = document.getElementById('sidebar-icon-moon');
    const sbSun = document.getElementById('sidebar-icon-sun');
    const sbLabel = document.getElementById('sidebar-theme-text') || document.getElementById('theme-label-text');
    if (sbMoon && sbSun) {
        sbMoon.style.display = isDark ? 'none' : 'block';
        sbSun.style.display = isDark ? 'block' : 'none';
    }
    if (sbLabel) {
        sbLabel.innerText = isDark ? 'Modo Claro' : 'Modo Escuro';
    }
}

function toggleTheme() {
    const temaAtual = document.documentElement.getAttribute('data-theme') ||
        (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
    const novoTema = temaAtual === 'dark' ? 'light' : 'dark';
    aplicarTema(novoTema);
}

function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebar-overlay');
    if (sidebar) sidebar.classList.toggle('open');
    if (overlay) overlay.classList.toggle('active');
}

// Execução imediata no carregamento para evitar flash de tela
(function() {
    let temaInicial = 'light';
    try {
        const salvo = localStorage.getItem('medcheck_theme');
        if (salvo) {
            temaInicial = salvo;
        } else if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
            temaInicial = 'dark';
        }
    } catch (e) {}
    document.documentElement.setAttribute('data-theme', temaInicial);
})();

// Sincroniza ícones assim que o DOM estiver pronto
document.addEventListener('DOMContentLoaded', () => {
    const temaAtual = document.documentElement.getAttribute('data-theme') || 'light';
    aplicarTema(temaAtual);
});
