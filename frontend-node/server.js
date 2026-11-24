/**
 * MEDCHECK (MedIA) - Gateway BFF & Servidor Web em Node.JS
 * Orquestra a Interface Web e o Chat Conversacional com IA MedIA
 * Integra-se ao Core POO Java (Porta 8080)
 */

const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const JAVA_API_BASE = 'http://127.0.0.1:8080';
const PUBLIC_DIR = path.join(__dirname, 'public');

// Tipos MIME para arquivos estáticos
const MIME_TYPES = {
    '.html': 'text/html; charset=UTF-8',
    '.css': 'text/css; charset=UTF-8',
    '.js': 'application/javascript; charset=UTF-8',
    '.json': 'application/json; charset=UTF-8',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.svg': 'image/svg+xml'
};

// Validação estrita de duração dos sintomas [RF028 / Chat IA]
function validarRespostaDuracao(resposta) {
    if (!resposta || typeof resposta !== 'string') return false;
    const limpo = resposta
        .trim()
        .toLowerCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, ''); // Remove acentos (há -> ha, manhã -> manha)

    // 1. Sugestões textuais pré-definidas (case e acento insensíveis)
    const sugestoesValidas = [
        '30 minutos',
        '2 horas',
        'hoje pela manha',
        'desde ontem',
        'desde a semana passada',
        'desde semana passada'
    ];
    if (sugestoesValidas.includes(limpo)) {
        return true;
    }

    // 2. Padrões numéricos: "X minutos", "Há X minutos", "X horas", "Há X horas", "X dias", "Há X dias", "X semanas", "Há X semanas"
    // Case-insensitive e sem exigir acento no "Há" (aceita "ha" ou "há", ou sem "ha/há")
    // X sendo qualquer número (ex: 1, 10, 45, 1.5, 2,5) e aceitando singular ou plural
    const padraoTempo = /^(?:ha\s+)?\d+(?:[.,]\d+)?\s*(?:minutos?|horas?|dias?|semanas?)$/;
    return padraoTempo.test(limpo);
}

const server = http.createServer(async (req, res) => {
    // Adicionar CORS
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

    if (req.method === 'OPTIONS') {
        res.writeHead(204);
        res.end();
        return;
    }

    const parsedUrl = new URL(req.url, `http://${req.headers.host}`);
    let pathname = parsedUrl.pathname;

    // 1. Orquestrador Conversacional Inteligente com IA MedIA
    if (pathname === '/api/chat-ia' && req.method === 'POST') {
        let body = '';
        req.on('data', chunk => { body += chunk; });
        req.on('end', () => {
            try {
                const data = JSON.parse(body || '{}');
                const step = data.step || 0;
                const message = (data.message || '').trim();
                const context = data.context || {};

                // Anonimização de Dados Sensíveis: Omitir identificadores pessoais do paciente
                const contextoAnonimizado = {
                    idade: context.idade || 30,
                    sexo: context.sexo || 'Não informado',
                    alergias: context.alergias || 'Nenhuma',
                    doencasCronicas: context.doencasCronicas || 'Nenhuma'
                };

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
                        // Validação estrita de intensidade: número de 1 a 10 ou leve, moderada/moderado, intensa/intenso (case-insensitive)
                        const msgLimpa = message.trim().toLowerCase();
                        const validaNumero = /\b(?:10|[1-9])\b/.test(msgLimpa);
                        const validaPalavra = /\b(leve|moderada|moderado|intensa|intenso)\b/i.test(msgLimpa);

                        if (!validaNumero && !validaPalavra) {
                            res.writeHead(200, { 'Content-Type': 'application/json' });
                            res.end(JSON.stringify({
                                sucesso: true,
                                resposta: 'Por favor, informe uma intensidade válida: um número de 1 a 10 ou Leve, Moderada (ou moderado), Intensa (ou intenso).',
                                step: 2,
                                finalizado: false,
                                context: context,
                                erroValidacao: true
                            }));
                            return;
                        }

                        context.localEIntensidade = message;
                        respostaIA = 'Há quanto tempo esse sintoma começou?';
                        proximoPasso = 3;
                        break;
                    case 3:
                        // Validação estrita de duração dos sintomas: sugestões ou "Há X minutos", "X minutos", "Há X horas", "X horas", "Há X dias", "X dias", "Há X semanas", "X semanas"
                        if (!validarRespostaDuracao(message)) {
                            res.writeHead(200, { 'Content-Type': 'application/json' });
                            res.end(JSON.stringify({
                                sucesso: true,
                                resposta: 'Desculpe, não entendi. Por favor, informe a duração de um jeito mais claro ou utilize uma das sugestões.',
                                step: 3,
                                finalizado: false,
                                context: context,
                                erroValidacao: true
                            }));
                            return;
                        }

                        context.duracao = message;
                        respostaIA = 'A sensação é contínua ou vai e volta (intermitente)?';
                        proximoPasso = 4;
                        break;
                    case 4:
                        context.frequencia = message;
                        context.duracaoEFrequencia = `${context.duracao || ''} (Frequência: ${message})`;
                        respostaIA = 'Você está sentindo algum outro sintoma associado, como febre, falta de ar, tontura, suor frio ou náuseas?';
                        proximoPasso = 5;
                        break;
                    case 5:
                        context.sintomasAssociados = message;
                        respostaIA = 'Perfeito. Para refinarmos a classificação clínica de risco, você possui em mãos aparelhos para medir seus sinais vitais (termômetro, oxímetro ou aparelho de pressão)? Se sim, pode me informar os valores ou confirmar para concluir a triagem.';
                        proximoPasso = 6;
                        break;
                    case 6:
                    default:
                        context.sinaisVitaisInformados = message;
                        respostaIA = 'Obrigado por todas as informações! Nossos guardrails clínicos compilaram seus relatos em um Resumo Estruturado e estão submetendo os dados ao Motor Clínico do Protocolo de Manchester para determinar seu nível de risco.';
                        finalizado = true;
                        break;
                }

                res.writeHead(200, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({
                    sucesso: true,
                    resposta: respostaIA,
                    step: proximoPasso,
                    finalizado: finalizado,
                    context: context
                }));
            } catch (err) {
                res.writeHead(400, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ sucesso: false, erro: err.message }));
            }
        });
        return;
    }

    // 2. Proxy reverso para a API Core Java (Porta 8080)
    if (pathname.startsWith('/api/')) {
        const javaUrl = `${JAVA_API_BASE}${req.url}`;

        let bodyBuffer = [];
        req.on('data', chunk => bodyBuffer.push(chunk));
        req.on('end', async () => {
            const body = Buffer.concat(bodyBuffer);
            try {
                const javaRes = await fetch(javaUrl, {
                    method: req.method,
                    headers: {
                        'Content-Type': req.headers['content-type'] || 'application/json'
                    },
                    body: ['POST', 'PUT', 'PATCH'].includes(req.method) ? body : undefined,
                    signal: AbortSignal.timeout(3000)
                });

                const responseText = await javaRes.text();
                res.writeHead(javaRes.status, { 'Content-Type': 'application/json; charset=UTF-8' });
                res.end(responseText);
            } catch (error) {
                res.writeHead(502, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({
                    sucesso: false,
                    erro: 'Backend Core Java (Porta 8080) indisponível. Certifique-se de que o servidor Java está em execução.',
                    detalhes: error.message
                }));
            }
        });
        return;
    }

    // 3. Servir arquivos estáticos (HTML, CSS, JS)
    if (pathname === '/') {
        pathname = '/index.html';
    }

    const safePath = path.normalize(pathname).replace(/^(\.\.[\/\\])+/, '');
    const filePath = path.join(PUBLIC_DIR, safePath);

    fs.readFile(filePath, (err, content) => {
        if (err) {
            if (err.code === 'ENOENT') {
                res.writeHead(404, { 'Content-Type': 'text/html; charset=UTF-8' });
                res.end('<h1>404 Não Encontrado - MEDCHECK</h1>');
            } else {
                res.writeHead(500);
                res.end(`Erro no servidor: ${err.code}`);
            }
        } else {
            const ext = path.extname(filePath).toLowerCase();
            const contentType = MIME_TYPES[ext] || 'application/octet-stream';
            res.writeHead(200, { 'Content-Type': contentType });
            res.end(content);
        }
    });
});

server.listen(PORT, () => {
    console.log(`====================================================================`);
    console.log(`  MEDCHECK (MedIA) - Frontend Web & BFF Node.JS ativo`);
    console.log(`  Acesse no navegador: http://localhost:${PORT}`);
    console.log(`  Conectado ao Backend Java em: ${JAVA_API_BASE}`);
    console.log(`====================================================================`);
});
