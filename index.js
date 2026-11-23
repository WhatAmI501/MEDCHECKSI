/**
 * MEDCHECK (MedIA) — Vercel Serverless Backend & API Gateway
 * Fornece todos os endpoints clínicos para o Frontend na Vercel:
 * - /api/pacientes e /api/pacientes/:id/historico
 * - /api/fila (Fila priorizada Manchester)
 * - /api/triagens (Motor de Classificação de Risco Manchester)
 * - /api/receitas (Emissão com SHA-256 e Guardrail Anti-alergia [RS007])
 * - /api/dashboard (KPIs e Distribuição Manchester)
 * - /api/auditoria (Trilha imutável LGPD Art. 37 & CFM)
 * - /api/chat-ia (Triagem conversacional com IA MedIA)
 * - Proxy opcional via process.env.BACKEND_URL
 */

const crypto = require('crypto');

// ====================================================================
// BANCO DE DADOS EM MEMÓRIA / SEED (Alinhado com MedCheckDataStore.java)
// ====================================================================
const store = {
    pacientes: [
        {
            id: 4,
            nome: 'Mariana Oliveira Costa',
            cpf: '123.456.789-01',
            email: 'mariana@email.com',
            telefone: '(82) 98888-1111',
            dataNascimento: '1995-04-12',
            idade: 29,
            sexo: 'Feminino',
            peso: 62.5,
            altura: 1.65,
            alergias: 'Dipirona, Anti-inflamatórios',
            doencasCronicas: 'Nenhuma doença crônica',
            cardiopatia: false,
            diabetes: false,
            historicoFamiliar: 'Hipertensão materna',
            medicamentosEmUso: 'Nenhum de uso contínuo'
        },
        {
            id: 5,
            nome: 'José Ferreira dos Santos',
            cpf: '234.567.890-12',
            email: 'jose@email.com',
            telefone: '(82) 99999-2222',
            dataNascimento: '1968-11-20',
            idade: 56,
            sexo: 'Masculino',
            peso: 84.0,
            altura: 1.74,
            alergias: 'Penicilina, Amoxicilina',
            doencasCronicas: 'Hipertensão Arterial e Diabetes Tipo 2',
            cardiopatia: true,
            diabetes: true,
            historicoFamiliar: 'Infarto agudo do miocárdio (pai)',
            medicamentosEmUso: 'Losartana 50mg, Metformina 850mg'
        },
        {
            id: 6,
            nome: 'Ana Beatriz Lima',
            cpf: '345.678.901-23',
            email: 'ana@email.com',
            telefone: '(82) 97777-3333',
            dataNascimento: '2001-08-30',
            idade: 23,
            sexo: 'Feminino',
            peso: 58.0,
            altura: 1.60,
            alergias: '',
            doencasCronicas: 'Nenhuma',
            cardiopatia: false,
            diabetes: false,
            historicoFamiliar: 'Sem histórico relevante',
            medicamentosEmUso: 'Nenhum'
        }
    ],

    triagens: [
        {
            id: 1,
            pacienteId: 5,
            queixa: 'Dor Torácica',
            local: 'Precordial com irradiação para braço esquerdo',
            intensidade: 'INTENSA',
            duracao: 'Há 40 minutos',
            frequencia: 'CONTINUA',
            sintomasAssociados: ['Sudorese fria', 'Falta de ar'],
            sinaisVitais: { temp: 36.8, paSistolica: 160, paDiastolica: 100, fc: 115, spo2: 93, glicemia: 140 },
            corManchester: 'LARANJA',
            prioridadeRotulo: 'Muito Urgente',
            tempoAlvoMinutos: 10,
            dataHoraInicio: new Date(Date.now() - 35 * 60000).toISOString(),
            status: 'CONCLUIDA',
            resumo: 'Paciente hipertenso e cardiopata com dor torácica típica com características anginosas.',
            orientacaoPaciente: 'Procure atendimento imediato ou aguarde teleconsulta prioritária.',
            encaminhamentoRecomendado: 'Teleconsulta prioritária em até 10 minutos com regulação para emergência.',
            sinaisAlerta: 'Piora rápida da dor, sudorese intensa ou perda de consciência.',
            discriminadores: ['Dor típica precordial com irradiação', 'Sudorese e taquicardia']
        },
        {
            id: 2,
            pacienteId: 4,
            queixa: 'Cefaleia Intensa',
            local: 'Frontal e retro-orbitária',
            intensidade: 'MODERADA',
            duracao: 'Há 2 dias',
            frequencia: 'CONTINUA',
            sintomasAssociados: ['Fotofobia', 'Febre'],
            sinaisVitais: { temp: 38.6, paSistolica: 120, paDiastolica: 80, fc: 88, spo2: 98, glicemia: 95 },
            corManchester: 'AMARELO',
            prioridadeRotulo: 'Urgente',
            tempoAlvoMinutos: 50,
            dataHoraInicio: new Date(Date.now() - 20 * 60000).toISOString(),
            status: 'CONCLUIDA',
            resumo: 'Cefaleia aguda com febre de 38.6°C, sem rigidez de nuca.',
            orientacaoPaciente: 'Mantenha repouso e hidratação oral. Aguarde atendimento médico.',
            encaminhamentoRecomendado: 'Teleconsulta em até 50 minutos.',
            sinaisAlerta: 'Confusão mental, vômitos em jato ou rigidez de nuca.',
            discriminadores: ['Cefaleia moderada com temperatura de 38.6°C']
        }
    ],

    fila: [
        {
            atendimentoId: 1,
            triagemId: 1,
            pacienteId: 5,
            pacienteNome: 'José Ferreira dos Santos',
            pacienteIdade: 56,
            queixa: 'Dor Torácica',
            corManchester: 'LARANJA',
            prioridadeRotulo: 'Muito Urgente',
            tempoAlvoMinutos: 10,
            status: 'AGUARDANDO_ATENDIMENTO'
        },
        {
            atendimentoId: 2,
            triagemId: 2,
            pacienteId: 4,
            pacienteNome: 'Mariana Oliveira Costa',
            pacienteIdade: 29,
            queixa: 'Cefaleia Intensa',
            corManchester: 'AMARELO',
            prioridadeRotulo: 'Urgente',
            tempoAlvoMinutos: 50,
            status: 'AGUARDANDO_ATENDIMENTO'
        }
    ],

    receitas: [
        {
            id: 1,
            atendimentoId: 1,
            pacienteId: 5,
            medicoId: 2,
            medicoNome: 'Dr. Roberto Carlos da Silva',
            crm: 'CRM/AL 14892',
            dataEmissao: new Date().toISOString(),
            status: 'ATIVA',
            observacoes: 'Paciente orientado quanto a cuidados imediatos.',
            hashValidacao: 'd41d8cd98f00b204e9800998ecf8427e028b031b268f76037746cb98f62f3a12',
            itens: [
                { medicamento: 'Isordil Sublingual', principioAtivo: 'Dinitrato de Isossorbida', dosagem: '5mg', posologia: '1 comp sublingual se dor', quantidade: '1 caixa', via: 'Sublingual' },
                { medicamento: 'AAS', principioAtivo: 'Ácido Acetilsalicílico', dosagem: '100mg', posologia: '1 comp após almoço', quantidade: '1 caixa', via: 'Oral' }
            ]
        }
    ],

    logsAuditoria: [
        { id: 1, usuarioId: 2, pacienteId: 5, modulo: 'RECEITA', operacao: 'CREATE', descricao: 'Emissão de Receita Digital #1 com hash SHA-256', ip: '127.0.0.1', dataHora: new Date(Date.now() - 5000).toISOString() },
        { id: 2, usuarioId: 3, pacienteId: 5, modulo: 'TRIAGEM', operacao: 'CONSULTA', descricao: 'Triagem classificada no Manchester: LARANJA', ip: '127.0.0.1', dataHora: new Date(Date.now() - 15000).toISOString() },
        { id: 3, usuarioId: 2, pacienteId: 4, modulo: 'ATENDIMENTO', operacao: 'UPDATE', descricao: 'Atendimento médico assumido na fila prioritária', ip: '127.0.0.1', dataHora: new Date(Date.now() - 30000).toISOString() },
        { id: 4, usuarioId: 1, pacienteId: 5, modulo: 'PACIENTE', operacao: 'BLOQUEIO', descricao: 'Guardrail [RS007]: Alerta anti-alergia verificado com sucesso', ip: '127.0.0.1', dataHora: new Date(Date.now() - 60000).toISOString() }
    ]
};

// ====================================================================
// FUNÇÕES AUXILIARES E MOTOR CLÍNICO MANCHESTER
// ====================================================================
function calcularIdade(dataNasc) {
    if (!dataNasc) return 30;
    const nasc = new Date(dataNasc);
    const hoje = new Date();
    let idade = hoje.getFullYear() - nasc.getFullYear();
    const m = hoje.getMonth() - nasc.getMonth();
    if (m < 0 || (m === 0 && hoje.getDate() < nasc.getDate())) idade--;
    return idade;
}

function classificarManchester(queixa, local, intensidade, duracao, sintomas, vitais) {
    const queixaNorm = (queixa || '').toLowerCase();
    const localNorm = (local || '').toLowerCase();
    const intNorm = (intensidade || '').toUpperCase();
    const spo2 = vitais && vitais.spo2 ? Number(vitais.spo2) : 98;
    const paSis = vitais && vitais.paSistolica ? Number(vitais.paSistolica) : 120;
    const temp = vitais && vitais.temp ? Number(vitais.temp) : 36.5;

    // Regra Vermelha: Parada, choque, SpO2 crítica < 90%
    if (spo2 < 90 || queixaNorm.includes('inconsci') || queixaNorm.includes('parada')) {
        return {
            cor: 'VERMELHO',
            rotulo: 'Emergência',
            minutos: 0,
            orientacao: 'Condição com risco iminente de morte! Acione imediatamente o SAMU 192.',
            encaminhamento: 'Regulação médica imediata para SAMU 192 e equipe de ressuscitação.',
            sinaisAlerta: 'Perda de consciência, ausência de pulso, respiração agônica.',
            discriminadores: ['Comprometimento grave de vias aéreas ou SpO2 < 90%']
        };
    }

    // Regra Laranja: Dor torácica típica com irradiação / dor severa / dispneia aguda
    if (queixaNorm.includes('torác') || queixaNorm.includes('peito') || localNorm.includes('precordial') || (intNorm === 'INTENSA' && queixaNorm.includes('dor'))) {
        return {
            cor: 'LARANJA',
            rotulo: 'Muito Urgente',
            minutos: 10,
            orientacao: 'Procure atendimento imediato ou aguarde teleconsulta prioritária.',
            encaminhamento: 'Teleconsulta prioritária em até 10 minutos com regulação para emergência.',
            sinaisAlerta: 'Piora rápida da dor, sudorese intensa ou perda de consciência.',
            discriminadores: ['Dor típica precordial com irradiação ou dor severa súbita']
        };
    }

    // Regra Amarela: Febre alta, dor moderada, alterações pressóricas
    if (temp >= 38.5 || paSis >= 160 || intNorm === 'MODERADA' || queixaNorm.includes('febre') || queixaNorm.includes('cefaleia')) {
        return {
            cor: 'AMARELO',
            rotulo: 'Urgente',
            minutos: 50,
            orientacao: 'Mantenha repouso e hidratação oral. Aguarde atendimento médico.',
            encaminhamento: 'Teleconsulta em até 50 minutos.',
            sinaisAlerta: 'Confusão mental, vômitos em jato ou rigidez de nuca.',
            discriminadores: ['Temperatura >= 38.5°C ou dor moderada persistente']
        };
    }

    // Regra Verde: Sintomas leves ou subagudos
    if (intNorm === 'LEVE' || queixaNorm.includes('tosse') || queixaNorm.includes('resfriado') || queixaNorm.includes('coriza')) {
        return {
            cor: 'VERDE',
            rotulo: 'Pouco Urgente',
            minutos: 120,
            orientacao: 'Quadro estável. Realize hidratação e repouso.',
            encaminhamento: 'Atendimento ambulatorial ou teleconsulta em até 120 minutos.',
            sinaisAlerta: 'Aparecimento de febre persistente ou falta de ar.',
            discriminadores: ['Sintoma leve sem sinais de alarme']
        };
    }

    // Regra Azul: Não urgente
    return {
        cor: 'AZUL',
        rotulo: 'Não Urgente',
        minutos: 240,
        orientacao: 'Quadro crônico ou de rotina. Agende atendimento eletivo.',
        encaminhamento: 'Encaminhamento para consulta básica ou UBS.',
        sinaisAlerta: 'Qualquer piora aguda do estado geral.',
        discriminadores: ['Queixa crônica sem alteração aguda']
    };
}

// ====================================================================
// PARSER DO CORPO DA REQUISIÇÃO
// ====================================================================
function parseBody(req) {
    return new Promise((resolve) => {
        if (req.body) {
            resolve(typeof req.body === 'object' ? req.body : JSON.parse(req.body || '{}'));
            return;
        }
        let data = '';
        req.on('data', chunk => { data += chunk; });
        req.on('end', () => {
            try {
                resolve(JSON.parse(data || '{}'));
            } catch (e) {
                resolve({});
            }
        });
    });
}

// ====================================================================
// HANDLER PRINCIPAL (Vercel Serverless Function)
// ====================================================================
module.exports = async function handler(req, res) {
    // 1. Cabeçalhos CORS e JSON
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

    if (req.method === 'OPTIONS') {
        res.writeHead(204);
        res.end();
        return;
    }

    // 2. Se houver BACKEND_URL configurada nas variáveis de ambiente da Vercel, atua como proxy reverso
    if (process.env.BACKEND_URL) {
        try {
            const remoteUrl = `${process.env.BACKEND_URL.replace(/\/$/, '')}${req.url}`;
            const bodyData = ['POST', 'PUT', 'PATCH'].includes(req.method) ? JSON.stringify(await parseBody(req)) : undefined;
            const remoteRes = await fetch(remoteUrl, {
                method: req.method,
                headers: { 'Content-Type': 'application/json' },
                body: bodyData
            });
            const text = await remoteRes.text();
            res.writeHead(remoteRes.status, { 'Content-Type': 'application/json; charset=UTF-8' });
            res.end(text);
            return;
        } catch (err) {
            // Em caso de falha no proxy remoto, continua para o backend nativo embutido
            console.warn('Proxy remoto falhou, utilizando backend embutido:', err.message);
        }
    }

    const parsedUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
    const pathname = parsedUrl.pathname;

    // Resposta padrão JSON helper
    const sendJson = (status, data) => {
        res.writeHead(status, { 'Content-Type': 'application/json; charset=UTF-8' });
        res.end(JSON.stringify(data));
    };

    // ----------------------------------------------------------------
    // ROTA: /api/status (Health Check)
    // ----------------------------------------------------------------
    if (pathname === '/api/status') {
        return sendJson(200, {
            status: 'ONLINE',
            sistema: 'MEDCHECK Core Serverless API (Vercel)',
            versao: '1.0.0',
            timestamp: new Date().toISOString()
        });
    }

    // ----------------------------------------------------------------
    // ROTA: /api/auth/login
    // ----------------------------------------------------------------
    if (pathname === '/api/auth/login' && req.method === 'POST') {
        const body = await parseBody(req);
        const { login, senha } = body;
        if (login && senha) {
            return sendJson(200, {
                sucesso: true,
                id: 2,
                nome: 'Dr. Roberto Carlos da Silva',
                perfil: 'MEDICO',
                descricaoPerfil: 'Médico Regulador e Plantonista'
            });
        }
        return sendJson(401, { sucesso: false, erro: 'Credenciais inválidas.' });
    }

    // ----------------------------------------------------------------
    // ROTA: /api/pacientes e /api/pacientes/:id/historico
    // ----------------------------------------------------------------
    if (pathname.startsWith('/api/pacientes')) {
        // GET /api/pacientes/:id/historico
        const histMatch = pathname.match(/^\/api\/pacientes\/(\d+)\/historico$/);
        if (histMatch && req.method === 'GET') {
            const pacId = parseInt(histMatch[1], 10);
            const p = store.pacientes.find(item => item.id === pacId);
            if (!p) {
                return sendJson(404, { erro: 'Paciente não encontrado.' });
            }

            const triagensPac = store.triagens.filter(t => t.pacienteId === pacId);
            const receitasPac = store.receitas.filter(r => r.pacienteId === pacId);

            return sendJson(200, {
                paciente: {
                    id: p.id,
                    nome: p.nome,
                    cpf: p.cpf,
                    idade: p.idade,
                    telefone: p.telefone,
                    sexo: p.sexo,
                    peso: p.peso,
                    altura: p.altura,
                    alergias: p.alergias,
                    doencasCronicas: p.doencasCronicas
                },
                historico: {
                    alergias: p.alergias,
                    doencasCronicas: p.doencasCronicas,
                    cardiopatia: p.cardiopatia,
                    diabetes: p.diabetes,
                    historicoFamiliar: p.historicoFamiliar,
                    medicamentosEmUso: p.medicamentosEmUso
                },
                triagens: triagensPac.map(t => ({
                    id: t.id,
                    data: t.dataHoraInicio,
                    queixa: t.queixa,
                    intensidade: t.intensidade,
                    corManchester: t.corManchester,
                    prioridadeRotulo: t.prioridadeRotulo,
                    condutaRecomendada: t.encaminhamentoRecomendado,
                    discriminadores: t.discriminadores || [],
                    resumo: t.resumo
                })),
                receitas: receitasPac.map(r => ({
                    id: r.id,
                    data: r.dataEmissao,
                    hash: r.hashValidacao,
                    status: r.status,
                    medicamentos: (r.itens || []).map(i => `${i.medicamento} (${i.principioAtivo}) - ${i.dosagem}`).join('; '),
                    itens: r.itens || []
                }))
            });
        }

        // GET /api/pacientes (Listar todos)
        if (req.method === 'GET') {
            const lista = store.pacientes.map(p => ({
                id: p.id,
                nome: p.nome,
                cpf: p.cpf,
                email: p.email,
                telefone: p.telefone,
                idade: p.idade,
                sexo: p.sexo,
                peso: p.peso,
                altura: p.altura,
                alergias: p.alergias
            }));
            return sendJson(200, lista);
        }

        // POST /api/pacientes (Cadastrar novo)
        if (req.method === 'POST') {
            const b = await parseBody(req);
            const novoId = store.pacientes.length + 10;
            const novoPac = {
                id: novoId,
                nome: b.nome || 'Paciente Sem Nome',
                cpf: b.cpf || '000.000.000-00',
                email: b.email || `paciente${novoId}@medcheck.com`,
                telefone: b.telefone || '',
                dataNascimento: b.dataNascimento || '1990-01-01',
                idade: calcularIdade(b.dataNascimento),
                sexo: b.sexo || 'Não informado',
                peso: parseFloat(b.peso || '70'),
                altura: parseFloat(b.altura || '1.70'),
                alergias: b.alergias || '',
                doencasCronicas: b.doencasCronicas || '',
                cardiopatia: Boolean(b.cardiopatia),
                diabetes: Boolean(b.diabetes),
                historicoFamiliar: b.historicoFamiliar || '',
                medicamentosEmUso: ''
            };
            store.pacientes.push(novoPac);

            // Log de auditoria
            store.logsAuditoria.unshift({
                id: store.logsAuditoria.length + 1,
                usuarioId: 1,
                pacienteId: novoId,
                modulo: 'PACIENTE',
                operacao: 'CREATE',
                descricao: `Cadastro do paciente ${novoPac.nome} (CPF: ${novoPac.cpf})`,
                ip: req.headers['x-forwarded-for'] || '127.0.0.1',
                dataHora: new Date().toISOString()
            });

            return sendJson(201, {
                sucesso: true,
                id: novoId,
                mensagem: 'Paciente cadastrado com sucesso!'
            });
        }
    }

    // ----------------------------------------------------------------
    // ROTA: /api/fila (Fila Priorizada de Atendimento)
    // ----------------------------------------------------------------
    if (pathname === '/api/fila' && req.method === 'GET') {
        return sendJson(200, store.fila);
    }

    // ----------------------------------------------------------------
    // ROTA: /api/triagens (Classificação de Risco Manchester)
    // ----------------------------------------------------------------
    if (pathname === '/api/triagens' && req.method === 'POST') {
        const b = await parseBody(req);
        const pacienteId = parseInt(b.pacienteId || '4', 10);
        const pac = store.pacientes.find(p => p.id === pacienteId) || store.pacientes[0];

        const classificacao = classificarManchester(
            b.queixa,
            b.local,
            b.intensidade,
            b.duracao,
            b.sintomasAssociados,
            {
                temp: b.temperatura,
                paSistolica: b.paSistolica,
                paDiastolica: b.paDiastolica,
                fc: b.fc,
                spo2: b.spo2,
                glicemia: b.glicemia
            }
        );

        const novaTriagemId = store.triagens.length + 10;
        const novaTriagem = {
            id: novaTriagemId,
            pacienteId: pac.id,
            queixa: b.queixa || 'Queixa não informada',
            local: b.local || '',
            intensidade: b.intensidade || 'MODERADA',
            duracao: b.duracao || '',
            frequencia: b.frequencia || 'CONTINUA',
            sintomasAssociados: Array.isArray(b.sintomasAssociados) ? b.sintomasAssociados : (b.sintomasAssociados ? b.sintomasAssociados.split(',') : []),
            sinaisVitais: { temp: b.temperatura, paSistolica: b.paSistolica, paDiastolica: b.paDiastolica, fc: b.fc, spo2: b.spo2, glicemia: b.glicemia },
            corManchester: classificacao.cor,
            prioridadeRotulo: classificacao.rotulo,
            tempoAlvoMinutos: classificacao.minutos,
            dataHoraInicio: new Date().toISOString(),
            status: 'CONCLUIDA',
            resumo: `Triagem concluída para ${pac.nome}. Queixa: ${b.queixa}. Classificação: ${classificacao.rotulo}.`,
            orientacaoPaciente: classificacao.orientacao,
            encaminhamentoRecomendado: classificacao.encaminhamento,
            sinaisAlerta: classificacao.sinaisAlerta,
            discriminadores: classificacao.discriminadores
        };

        store.triagens.unshift(novaTriagem);

        // Adiciona ou reposiciona na fila com base no Manchester
        const novoItemFila = {
            atendimentoId: store.fila.length + 10,
            triagemId: novaTriagemId,
            pacienteId: pac.id,
            pacienteNome: pac.nome,
            pacienteIdade: pac.idade,
            queixa: b.queixa,
            corManchester: classificacao.cor,
            prioridadeRotulo: classificacao.rotulo,
            tempoAlvoMinutos: classificacao.minutos,
            status: 'AGUARDANDO_ATENDIMENTO'
        };

        // Ordenação por prioridade: VERMELHO > LARANJA > AMARELO > VERDE > AZUL
        const prioridadePeso = { VERMELHO: 5, LARANJA: 4, AMARELO: 3, VERDE: 2, AZUL: 1 };
        store.fila.unshift(novoItemFila);
        store.fila.sort((a, b) => (prioridadePeso[b.corManchester] || 0) - (prioridadePeso[a.corManchester] || 0));

        // Registro de auditoria
        store.logsAuditoria.unshift({
            id: store.logsAuditoria.length + 1,
            usuarioId: 3,
            pacienteId: pac.id,
            modulo: 'TRIAGEM',
            operacao: 'CREATE',
            descricao: `Triagem finalizada com Manchester: ${classificacao.rotulo} (${classificacao.cor})`,
            ip: req.headers['x-forwarded-for'] || '127.0.0.1',
            dataHora: new Date().toISOString()
        });

        return sendJson(200, {
            sucesso: true,
            triagemId: novaTriagemId,
            status: 'CONCLUIDA',
            corManchester: classificacao.cor,
            prioridadeRotulo: classificacao.rotulo,
            tempoAlvoMinutos: classificacao.minutos,
            orientacaoPaciente: classificacao.orientacao,
            encaminhamentoRecomendado: classificacao.encaminhamento,
            sinaisAlerta: classificacao.sinaisAlerta,
            discriminadores: classificacao.discriminadores
        });
    }

    // ----------------------------------------------------------------
    // ROTA: /api/receitas e /api/receitas/validar
    // ----------------------------------------------------------------
    if (pathname.startsWith('/api/receitas')) {
        // GET /api/receitas/validar?hash=...
        if (pathname.includes('/validar') && req.method === 'GET') {
            const hash = parsedUrl.searchParams.get('hash');
            const receita = store.receitas.find(r => r.hashValidacao === hash);
            if (receita) {
                const pac = store.pacientes.find(p => p.id === receita.pacienteId);
                return sendJson(200, {
                    valida: true,
                    id: receita.id,
                    hash: receita.hashValidacao,
                    pacienteNome: pac ? pac.nome : 'Paciente',
                    medicoNome: receita.medicoNome || 'Dr. Roberto Carlos da Silva',
                    crm: receita.crm || 'CRM/AL 14892',
                    dataEmissao: receita.dataEmissao,
                    status: receita.status
                });
            }
            return sendJson(404, { valida: false, erro: 'Receita não localizada ou hash inválido.' });
        }

        // POST /api/receitas (Emissão com Guardrail Anti-alergia [RS007])
        if (req.method === 'POST') {
            const b = await parseBody(req);
            const pacienteId = parseInt(b.pacienteId || '0', 10);
            const pac = store.pacientes.find(p => p.id === pacienteId);
            const medNome = (b.medicamento || '').toLowerCase();
            const principio = (b.principioAtivo || '').toLowerCase();
            const confirmacaoAlergia = Boolean(b.confirmacaoAlergia);

            // Guardrail Clínico Anti-alergia [RS007]
            if (pac && pac.alergias && !confirmacaoAlergia) {
                const alergiasLista = pac.alergias.toLowerCase().split(',').map(s => s.trim());
                for (const alergia of alergiasLista) {
                    if (alergia && (medNome.includes(alergia) || principio.includes(alergia) || alergia.includes(principio))) {
                        // Log de auditoria da tentativa de bloqueio
                        store.logsAuditoria.unshift({
                            id: store.logsAuditoria.length + 1,
                            usuarioId: 2,
                            pacienteId: pac.id,
                            modulo: 'RECEITA',
                            operacao: 'BLOQUEIO',
                            descricao: `Guardrail [RS007] bloqueou prescrição de ${b.medicamento}. Alergia do paciente: ${alergia}`,
                            ip: req.headers['x-forwarded-for'] || '127.0.0.1',
                            dataHora: new Date().toISOString()
                        });

                        return sendJson(409, {
                            sucesso: false,
                            alertaAlergia: true,
                            erro: `ALERTA DE SEGURANÇA FARMACOLÓGICA [RS007]: O paciente possui histórico de hipersensibilidade/alergia a "${alergia}". Prescrição de "${b.medicamento}" suspensa.`
                        });
                    }
                }
            }

            // Geração de Hash Criptográfico SHA-256 [RF024]
            const receitaId = store.receitas.length + 10;
            const hashValidacao = crypto.createHash('sha256')
                .update(`MEDCHECK-REC-${receitaId}-${pacienteId}-${Date.now()}`)
                .digest('hex');

            const novaReceita = {
                id: receitaId,
                atendimentoId: parseInt(b.atendimentoId || '1', 10),
                pacienteId: pacienteId,
                medicoId: 2,
                medicoNome: 'Dr. Roberto Carlos da Silva',
                crm: 'CRM/AL 14892',
                dataEmissao: new Date().toISOString(),
                status: 'ATIVA',
                observacoes: b.observacoes || 'Prescrição digital emitida pelo MEDCHECK.',
                hashValidacao: hashValidacao,
                itens: [
                    {
                        medicamento: b.medicamento || 'Medicamento',
                        principioAtivo: b.principioAtivo || '',
                        dosagem: b.dosagem || '1 comp',
                        posologia: b.posologia || '1x ao dia',
                        quantidade: b.quantidade || '1 caixa',
                        via: b.via || 'Oral'
                    }
                ]
            };

            store.receitas.unshift(novaReceita);

            // Log de auditoria
            store.logsAuditoria.unshift({
                id: store.logsAuditoria.length + 1,
                usuarioId: 2,
                pacienteId: pacienteId,
                modulo: 'RECEITA',
                operacao: 'CREATE',
                descricao: `Receita #${receitaId} emitida com sucesso (Hash: ${hashValidacao.substring(0, 16)}...)`,
                ip: req.headers['x-forwarded-for'] || '127.0.0.1',
                dataHora: new Date().toISOString()
            });

            return sendJson(201, {
                sucesso: true,
                receitaId: receitaId,
                hash: hashValidacao,
                urlQrCode: `https://medcheck.cesmac.edu.br/validar?hash=${hashValidacao}`,
                mensagem: 'Receita emitida e assinada digitalmente com sucesso!'
            });
        }
    }

    // ----------------------------------------------------------------
    // ROTA: /api/dashboard (Métricas e Distribuição Manchester)
    // ----------------------------------------------------------------
    if (pathname === '/api/dashboard' && req.method === 'GET') {
        const distribuicao = {
            VERMELHO: store.triagens.filter(t => t.corManchester === 'VERMELHO').length,
            LARANJA: store.triagens.filter(t => t.corManchester === 'LARANJA').length,
            AMARELO: store.triagens.filter(t => t.corManchester === 'AMARELO').length,
            VERDE: store.triagens.filter(t => t.corManchester === 'VERDE').length,
            AZUL: store.triagens.filter(t => t.corManchester === 'AZUL').length
        };

        return sendJson(200, {
            totalTriagens: store.triagens.length,
            totalPacientes: store.pacientes.length,
            totalReceitas: store.receitas.length,
            totalAtendimentos: store.triagens.length + 1,
            distribuicao: distribuicao
        });
    }

    // ----------------------------------------------------------------
    // ROTA: /api/auditoria (Livro Eletrônico Imutável)
    // ----------------------------------------------------------------
    if (pathname === '/api/auditoria' && req.method === 'GET') {
        return sendJson(200, store.logsAuditoria);
    }

    // ----------------------------------------------------------------
    // ROTA: /api/chat-ia (Triagem Conversacional com MedIA)
    // ----------------------------------------------------------------
    if (pathname === '/api/chat-ia' && req.method === 'POST') {
        const data = await parseBody(req);
        const step = data.step || 0;
        const message = (data.message || '').trim();
        const context = data.context || {};

        let respostaIA = '';
        let proximoPasso = step + 1;
        let finalizado = false;

        switch (step) {
            case 0:
                respostaIA = 'Olá! Sou a MedIA, assistente de triagem médica do MEDCHECK. Para iniciarmos sua avaliação, por favor, me descreva o que você está sentindo e qual é sua queixa principal?';
                proximoPasso = 1;
                break;
            case 1:
                context.queixa = message;
                respostaIA = `Entendi. Você relatou "${message}". Em qual local do corpo você sente esse sintoma e qual a intensidade de 1 a 10 (ou Leve, Moderada, Intensa)?`;
                proximoPasso = 2;
                break;
            case 2:
                context.localEIntensidade = message;
                respostaIA = 'Há quanto tempo esse sintoma começou? A sensação é contínua ou vai e volta (intermitente)?';
                proximoPasso = 3;
                break;
            case 3:
                context.duracaoEFrequencia = message;
                respostaIA = 'Você está sentindo algum outro sintoma associado, como febre, falta de ar, tontura, suor frio ou náuseas?';
                proximoPasso = 4;
                break;
            case 4:
                context.sintomasAssociados = message;
                respostaIA = 'Perfeito. Para refinarmos a classificação clínica de risco, você possui em mãos aparelhos para medir seus sinais vitais (termômetro, oxímetro ou aparelho de pressão)? Se sim, pode me informar os valores ou confirmar para concluir a triagem.';
                proximoPasso = 5;
                break;
            case 5:
            default:
                context.sinaisVitaisInformados = message;
                respostaIA = 'Obrigado por todas as informações! Nossos guardrails clínicos compilaram seus relatos em um Resumo Estruturado e estão submetendo os dados ao Motor Clínico do Protocolo de Manchester para determinar seu nível de risco.';
                finalizado = true;
                break;
        }

        return sendJson(200, {
            sucesso: true,
            resposta: respostaIA,
            step: proximoPasso,
            finalizado: finalizado,
            context: context
        });
    }

    // Caso a rota /api não seja reconhecida
    return sendJson(404, { erro: `Endpoint ${pathname} não encontrado no backend.` });
};
