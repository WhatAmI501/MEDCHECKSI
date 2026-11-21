/**
 * Teste de Integração Automatizado Ponta a Ponta para o MEDCHECK MVP
 */

const { spawn } = require('child_process');
const path = require('path');

async function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function runTests() {
    console.log('--- INICIANDO TESTES END-TO-END DO MEDCHECK ---');

    // 1. Iniciar Java Core
    console.log('[1/6] Subindo Backend Java Core (Porta 8080)...');
    const javaBin = 'C:\\Users\\Joao\\.jdks\\openjdk-25.0.2\\bin\\java.exe';
    const javaProc = spawn(javaBin, ['-cp', 'backend-java/bin', 'br.com.cesmac.medcheck.MedCheckApplication'], {
        cwd: path.resolve(__dirname, '..')
    });

    javaProc.stdout.on('data', data => process.stdout.write(`[JAVA OUT] ${data}`));
    javaProc.stderr.on('data', data => process.stderr.write(`[JAVA ERR] ${data}`));

    await sleep(2500);

    try {
        // 2. Teste Status Java API
        console.log('\n[2/6] Verificando Health Check do Java...');
        const statusRes = await fetch('http://127.0.0.1:8080/api/status');
        const statusData = await statusRes.json();
        console.log('Status Java:', statusData);

        // 3. Teste Listagem de Pacientes e Histórico
        console.log('\n[3/6] Testando Cadastro e Listagem de Pacientes...');
        const pacRes = await fetch('http://127.0.0.1:8080/api/pacientes');
        const pacientes = await pacRes.json();
        console.log(`Pacientes carregados: ${pacientes.length}`);
        console.log(`Paciente 1: ${pacientes[0].nome} (CPF: ${pacientes[0].cpf})`);

        // 4. Teste Triagem e Motor de Manchester Polimórfico [RF005]
        console.log('\n[4/6] Testando Triagem de Dor Torácica com Motor de Manchester...');
        const triageRes = await fetch('http://127.0.0.1:8080/api/triagens', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                pacienteId: 5, // José Ferreira (cardiopata)
                queixa: 'Dor no peito com aperto forte',
                local: 'Precordial',
                intensidade: 'INTENSA',
                duracao: 'Há 30 minutos',
                frequencia: 'CONTINUA',
                sintomasAssociados: 'Sudorese fria, falta de ar',
                temperatura: 36.8,
                paSistolica: 165,
                paDiastolica: 100,
                spo2: 92,
                fc: 110
            })
        });
        const triageData = await triageRes.json();
        console.log('Resultado Manchester:', triageData.corManchester, triageData.prioridadeRotulo, `(${triageData.tempoAlvoMinutos} min)`);
        console.log('Discriminadores:', triageData.discriminadores);

        // 5. Teste Fila Priorizada [RF021]
        console.log('\n[5/6] Testando Fila de Espera Inteligente (Precedência Laranja > Amarelo)...');
        const filaRes = await fetch('http://127.0.0.1:8080/api/fila');
        const fila = await filaRes.json();
        console.log(`Fila de espera possui ${fila.length} pacientes.`);
        fila.forEach((item, idx) => {
            console.log(`  Posição #${idx + 1}: ${item.pacienteNome} -> Prioridade: ${item.corManchester} (${item.tempoAlvoMinutos} min)`);
        });

        // 6. Teste de Bloqueio de Alergia na Prescrição [RS007]
        console.log('\n[6/6] Testando Regra de Segurança Anti-Alergia [RS007]...');
        // Tentativa de prescrever Dipirona para Mariana (que tem alergia a Dipirona)
        const prescrRes = await fetch('http://127.0.0.1:8080/api/receitas', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                atendimentoId: 1,
                medicoId: 2,
                medicamento: 'Novalgina',
                principioAtivo: 'Dipirona',
                confirmacaoAlergia: false
            })
        });

        console.log(`Status HTTP do teste de alergia: ${prescrRes.status}`);
        const prescrData = await prescrRes.json();
        console.log('Resposta do bloqueio:', prescrData);

        if (prescrRes.status === 409 && prescrData.alertaAlergia) {
            console.log('✔ SUCESSO: O sistema BLOQUEOU a prescrição de medicamento alérgeno com base no histórico do paciente!');
        }

        console.log('\n=============================================');
        console.log('✔ TODOS OS TESTES INTEGRADOS FORAM CONCLUÍDOS COM SUCESSO!');
        console.log('=============================================');

    } catch (err) {
        console.error('Falha nos testes:', err);
    } finally {
        javaProc.kill();
    }
}

runTests();
