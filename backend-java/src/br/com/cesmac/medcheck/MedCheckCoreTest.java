package br.com.cesmac.medcheck;

import br.com.cesmac.medcheck.model.*;
import br.com.cesmac.medcheck.model.enums.*;
import br.com.cesmac.medcheck.model.valueobjects.CPF;
import br.com.cesmac.medcheck.model.valueobjects.ItemReceita;
import br.com.cesmac.medcheck.model.valueobjects.SinaisVitais;
import br.com.cesmac.medcheck.service.ClinicaService;
import br.com.cesmac.medcheck.service.ReceitaService;

import java.time.LocalDate;
import java.util.*;

/**
 * Suite de Verificação e Homologação dos 4 Pilares de POO e Regras de Negócio do MEDCHECK.
 * Executa sem dependência de rede, validando diretamente todas as regras de domínio.
 */
public class MedCheckCoreTest {

    public static void main(String[] args) {
        System.out.println("====================================================================");
        System.out.println("  HOMOLOGAÇÃO DE DOMÍNIO E 4 PILARES DE POO — MEDCHECK (MedIA)     ");
        System.out.println("  Avaliação Integrada POO (AV03) - CESMAC 2026.2                    ");
        System.out.println("====================================================================");

        int totalTestes = 0;
        int testesAprovados = 0;

        // -----------------------------------------------------------------
        // PILAR 1: ENCAPSULAMENTO & VALUE OBJECTS (CPF e SinaisVitais)
        // -----------------------------------------------------------------
        totalTestes++;
        System.out.print("[TESTE 1] Encapsulamento de CPF e Validação Invariante: ");
        try {
            CPF cpf = new CPF("123.456.789-01");
            if (cpf.getValorLimpo().equals("12345678901") && cpf.getValorFormatado().equals("123.456.789-01")) {
                testesAprovados++;
                System.out.println("✔ APROVADO");
            } else {
                System.out.println("❌ FALHA");
            }
        } catch (Exception e) {
            System.out.println("❌ ERRO: " + e.getMessage());
        }

        totalTestes++;
        System.out.print("[TESTE 2] Encapsulamento de Sinais Vitais e Regras Semânticas: ");
        try {
            SinaisVitais svCritico = new SinaisVitais(39.8, 185, 115, 135, 88, 140);
            if (svCritico.isDessaturandoCritico() && svCritico.isFebreAlta() && svCritico.isCriseHipertensiva()) {
                testesAprovados++;
                System.out.println("✔ APROVADO");
            } else {
                System.out.println("❌ FALHA nas verificações semânticas");
            }
        } catch (Exception e) {
            System.out.println("❌ ERRO: " + e.getMessage());
        }

        // -----------------------------------------------------------------
        // PILAR 2: HERANÇA E ABSTRAÇÃO (Hierarquia de Usuários)
        // -----------------------------------------------------------------
        totalTestes++;
        System.out.print("[TESTE 3] Herança e Abstração em Usuários (Paciente, Médico, Enfermeiro): ");
        try {
            Usuario pac = new Paciente(4, "Mariana Costa", new CPF("123.456.789-01"), "mariana@email.com", "senha",
                    LocalDate.of(1995, 4, 12), "Feminino", "(82) 98888-1111", 62.5, 1.65);
            Usuario med = new Medico(2, "Dr. Roberto", new CPF("111.222.333-44"), "med@medcheck.com", "senha", "CRM/AL 14892", "Clínica Médica");
            Usuario enf = new Enfermeiro(3, "Enfª Juliana", new CPF("555.666.777-88"), "enf@medcheck.com", "senha", "COREN/AL 98765", "Triagem");

            boolean herancaOk = (pac instanceof Usuario) && (med instanceof ProfissionalSaude) && (enf instanceof ProfissionalSaude);
            boolean polimorfismoDescricao = pac.obterDescricaoPerfil().contains("Paciente") &&
                                            med.obterDescricaoPerfil().contains("CRM/AL 14892") &&
                                            enf.obterDescricaoPerfil().contains("COREN/AL 98765");

            if (herancaOk && polimorfismoDescricao) {
                testesAprovados++;
                System.out.println("✔ APROVADO");
            } else {
                System.out.println("❌ FALHA na hierarquia de herança");
            }
        } catch (Exception e) {
            System.out.println("❌ ERRO: " + e.getMessage());
        }

        // -----------------------------------------------------------------
        // PILAR 3: POLIMORFISMO — MOTOR DO PROTOCOLO DE MANCHESTER
        // -----------------------------------------------------------------
        totalTestes++;
        System.out.print("[TESTE 4] Polimorfismo e Strategy Pattern (Manchester Cardiovascular - Laranja): ");
        ClinicaService clinicaService = new ClinicaService();
        try {
            // Paciente 5 é José Ferreira (cardiopata) com dor torácica severa e sudorese
            Triagem triagemCardio = clinicaService.realizarTriagemEClassificacao(
                    5, "Dor Torácica e aperto forte", "Precordial", "INTENSA", "30 minutos", "CONTINUA",
                    Arrays.asList("Sudorese fria", "Falta de ar"),
                    new SinaisVitais(36.7, 160, 95, 110, 93, 130)
            );

            ClassificacaoRisco c = triagemCardio.getClassificacaoRisco();
            if (c != null && c.getCorManchester() == CorManchester.LARANJA && c.getTempoAlvoMinutos() == 10) {
                testesAprovados++;
                System.out.println("✔ APROVADO (" + c.getCorManchester().getRotulo() + " - " + c.getTempoAlvoMinutos() + " min)");
            } else {
                System.out.println("❌ FALHA: esperado LARANJA, obtido " + (c != null ? c.getCorManchester() : "null"));
            }
        } catch (Exception e) {
            System.out.println("❌ ERRO: " + e.getMessage());
        }

        totalTestes++;
        System.out.print("[TESTE 5] Polimorfismo (Manchester Respiratório - Vermelho/Emergência SAMU 192): ");
        try {
            // Paciente com dessaturação grave SpO2 < 90%
            Triagem triagemDispneia = clinicaService.realizarTriagemEClassificacao(
                    6, "Falta de ar intensa e sufocamento", "Tórax", "INTENSA", "15 minutos", "REPENTINA",
                    Arrays.asList("Cianose", "Incapacidade de falar"),
                    new SinaisVitais(37.0, 140, 90, 135, 87, 100) // SpO2 = 87% (Crítico!)
            );

            ClassificacaoRisco c = triagemDispneia.getClassificacaoRisco();
            boolean interrompidaEmergencia = triagemDispneia.getStatus() == StatusTriagem.INTERROMPIDA_EMERGENCIA;

            if (c != null && c.getCorManchester() == CorManchester.VERMELHO && interrompidaEmergencia) {
                testesAprovados++;
                System.out.println("✔ APROVADO (VERMELHO - 0 min - Interrupção Imediata [RF006 / RS005])");
            } else {
                System.out.println("❌ FALHA: esperado VERMELHO de emergência");
            }
        } catch (Exception e) {
            System.out.println("❌ ERRO: " + e.getMessage());
        }

        // -----------------------------------------------------------------
        // REQUISITO DE FILA INTELIGENTE (RF021)
        // -----------------------------------------------------------------
        totalTestes++;
        System.out.print("[TESTE 6] Fila de Espera Inteligente (Precedência Laranja > Amarelo): ");
        try {
            List<Map<String, Object>> fila = clinicaService.obterFilaPriorizadaTeleatendimento();
            boolean ordenacaoCorreta = true;
            int ordemAnterior = 0;
            for (Map<String, Object> item : fila) {
                int ordemAtual = (int) item.get("prioridadeOrdem");
                if (ordemAtual < ordemAnterior) {
                    ordenacaoCorreta = false;
                    break;
                }
                ordemAnterior = ordemAtual;
            }

            if (ordenacaoCorreta && !fila.isEmpty()) {
                testesAprovados++;
                System.out.println("✔ APROVADO (" + fila.size() + " pacientes ordenados estritamente por gravidade clínica)");
            } else {
                System.out.println("❌ FALHA na ordenação da fila");
            }
        } catch (Exception e) {
            System.out.println("❌ ERRO: " + e.getMessage());
        }

        // -----------------------------------------------------------------
        // REQUISITO CRÍTICO DE SEGURANÇA: ANTI-ALERGIA (RF030 / RS007)
        // -----------------------------------------------------------------
        totalTestes++;
        System.out.print("[TESTE 7] Bloqueio de Medicamento Conflitante com Alergia [RS007]: ");
        ReceitaService receitaService = new ReceitaService();
        try {
            // Mariana Costa (Paciente 4 no atendimento 2) possui alergia a Dipirona declarada
            List<ItemReceita> itensConflitantes = Collections.singletonList(
                    new ItemReceita("Novalgina 500mg", "Dipirona", "500mg", "1 comp 8/8h", "1 cx", "Oral")
            );

            // Sem confirmação explícita -> deve lançar exceção bloqueante
            try {
                receitaService.emitirReceita(2, 2, itensConflitantes, "Orientações", false);
                System.out.println("❌ FALHA: sistema não bloqueou prescrição com alérgeno!");
            } catch (IllegalStateException e) {
                if (e.getMessage().contains("ALERTA CRÍTICO DE ALERGIA")) {
                    testesAprovados++;
                    System.out.println("✔ APROVADO (Bloqueio preventivo disparado com sucesso)");
                } else {
                    System.out.println("❌ FALHA: mensagem inesperada " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("❌ ERRO: " + e.getMessage());
        }

        // -----------------------------------------------------------------
        // REQUISITO DE PRESCRIÇÃO E VALIDAÇÃO QR CODE / HASH (RF008 / RF024)
        // -----------------------------------------------------------------
        totalTestes++;
        System.out.print("[TESTE 8] Emissão de Receita com Hash SHA-256 e Validação Pública [RF024]: ");
        try {
            List<ItemReceita> itensSeguros = Collections.singletonList(
                    new ItemReceita("Paracetamol", "Paracetamol", "750mg", "1 comp se dor", "1 cx", "Oral")
            );
            ReceitaMedica rec = receitaService.emitirReceita(2, 2, itensSeguros, "Uso sob demanda", false);
            ReceitaMedica validada = receitaService.validarReceitaPublica(rec.getHashValidacao());

            if (validada != null && validada.getId().equals(rec.getId()) && rec.getHashValidacao().length() >= 30) {
                testesAprovados++;
                System.out.println("✔ APROVADO (Hash: " + rec.getHashValidacao() + ")");
            } else {
                System.out.println("❌ FALHA na validação pública do hash");
            }
        } catch (Exception e) {
            System.out.println("❌ ERRO: " + e.getMessage());
        }

        // -----------------------------------------------------------------
        // RESULTADO FINAL DA HOMOLOGAÇÃO
        // -----------------------------------------------------------------
        System.out.println("\n====================================================================");
        System.out.println("RESULTADO DA AVALIAÇÃO INTEGRADA: " + testesAprovados + "/" + totalTestes + " TESTES PASSARAM");
        if (testesAprovados == totalTestes) {
            System.out.println("✔ SUCESSO ABSOLUTO: 100% DE CONFORMIDADE COM A AV03 E REQUISITOS MEDCHECK!");
        } else {
            System.out.println("⚠ ALGUNS TESTES FALHARAM. REVISAR IMPLEMENTAÇÃO.");
        }
        System.out.println("====================================================================");
    }
}
