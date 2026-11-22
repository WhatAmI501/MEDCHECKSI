/* ====================================================================
   MEDCHECK (MedIA) — Gerenciador de Tema & Central de Notificações
   - Persistência em localStorage + Prevenção de FOUC
   - Controle do sino da Topbar e badge vermelho de alertas
   ==================================================================== */

function aplicarTema(tema) {
    document.documentElement.setAttribute('data-theme', tema);
    try {
        localStorage.setItem('medcheck_theme', tema);
    } catch (e) {}

    const isDark = tema === 'dark';

    // Atualizar ícones da Topbar (se existirem na página)
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

/* ====================================================================
   Controle do Sino de Notificações e Ponto Vermelho (Badge)
   ==================================================================== */

function obterUltimoLogVisto() {
    try {
        const val = localStorage.getItem('medcheck_ultimo_visto_log_id');
        return val !== null ? parseInt(val, 10) : null;
    } catch (e) {
        return null;
    }
}

function salvarUltimoLogVisto(id) {
    try {
        localStorage.setItem('medcheck_ultimo_visto_log_id', String(id));
    } catch (e) {}
}

function atualizarDotNotificacao(temNovas) {
    const dots = document.querySelectorAll('.notification-dot, #topbar-notification-dot');
    dots.forEach(dot => {
        dot.style.display = temNovas ? 'block' : 'none';
    });
}

// Consulta os logs do sistema e avalia se há novos eventos não visualizados
async function verificarNotificacoes() {
    try {
        const res = await fetch('/api/auditoria');
        if (!res.ok) return;
        const logs = await res.json();
        if (!Array.isArray(logs) || logs.length === 0) return;

        let maxId = 0;
        for (const l of logs) {
            const idNum = Number(l.id);
            if (!isNaN(idNum) && idNum > maxId) {
                maxId = idNum;
            }
        }

        const visto = obterUltimoLogVisto();
        // Se visto for nulo (primeira visita), exibimos o ponto vermelho como indicativo de atividade pendente
        if (visto === null) {
            atualizarDotNotificacao(true);
            return;
        }

        const temNovas = maxId > visto;
        atualizarDotNotificacao(temNovas);
    } catch (err) {
        // Falhas transitórias não afetam a UI
    }
}

// Marca todos os alertas atuais como visualizados
async function marcarNotificacoesVistas() {
    try {
        const res = await fetch('/api/auditoria');
        if (res.ok) {
            const logs = await res.json();
            if (Array.isArray(logs) && logs.length > 0) {
                let maxId = 0;
                for (const l of logs) {
                    const idNum = Number(l.id);
                    if (!isNaN(idNum) && idNum > maxId) {
                        maxId = idNum;
                    }
                }
                salvarUltimoLogVisto(maxId);
            }
        }
    } catch (e) {}
    atualizarDotNotificacao(false);
}

// Efeito de destaque no card Central de Alertas Rápidos
function destacarCardAlertas() {
    const el = document.getElementById('central-alertas');
    if (!el) return;
    el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    el.style.transition = 'box-shadow 0.4s ease, border-color 0.4s ease';
    const oldBorder = el.style.borderColor;
    const oldShadow = el.style.boxShadow;
    el.style.borderColor = '#0284c7';
    el.style.boxShadow = '0 0 0 4px rgba(2, 132, 199, 0.25), 0 10px 25px -5px rgba(0, 0, 0, 0.15)';
    setTimeout(() => {
        el.style.borderColor = oldBorder;
        el.style.boxShadow = oldShadow;
    }, 2800);
}

// Ação de clique no sino: marca como visualizado e leva à Central de Alertas Rápidos
function irParaAlertas() {
    marcarNotificacoesVistas();

    const path = window.location.pathname;
    const isIndex = path === '/' || path === '/index.html' || path.endsWith('/index.html') || path === '';

    if (isIndex && document.getElementById('central-alertas')) {
        destacarCardAlertas();
        if (history.pushState) {
            history.pushState(null, null, '#central-alertas');
        } else {
            window.location.hash = '#central-alertas';
        }
    } else {
        window.location.href = '/index.html#central-alertas';
    }
}

// Verifica se chegou na página inicial com o hash #central-alertas
function checarHashCentralAlertas() {
    if (window.location.hash === '#central-alertas') {
        setTimeout(() => {
            marcarNotificacoesVistas();
            destacarCardAlertas();
        }, 200);
    }
}

// Inicialização após carregamento do DOM
document.addEventListener('DOMContentLoaded', () => {
    const temaAtual = document.documentElement.getAttribute('data-theme') || 'light';
    aplicarTema(temaAtual);

    verificarNotificacoes();
    checarHashCentralAlertas();

    // Verificação periódica para reativar o ponto vermelho se novos pacientes ou dados forem adicionados
    setInterval(verificarNotificacoes, 4000);
});
