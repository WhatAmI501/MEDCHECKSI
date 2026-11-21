package br.com.cesmac.medcheck;

import br.com.cesmac.medcheck.api.MedCheckHttpServer;

/**
 * Ponto de Entrada da Aplicação Java POO do MEDCHECK.
 * Inicializa o repositório, valida os pilares de POO e sobe a API REST na porta 8080.
 */
public class MedCheckApplication {

    public static void main(String[] args) {
        System.out.println("====================================================================");
        System.out.println("   MEDCHECK (MedIA) - Core POO & Motor Protocolo de Manchester      ");
        System.out.println("   Avaliação Integrada POO (AV03) - CESMAC 2026.2                    ");
        System.out.println("====================================================================");

        int porta = 8080;
        try {
            MedCheckHttpServer servidor = new MedCheckHttpServer(porta);
            servidor.iniciar();

            System.out.println("✔ Servidor REST Java ativo em: http://localhost:" + porta);
            System.out.println("✔ Pilares de POO carregados:");
            System.out.println("   - Abstração: Interfaces IClassificadorManchester, Usuário Base");
            System.out.println("   - Encapsulamento: Value Objects CPF, SinaisVitais e Regras Clínicas");
            System.out.println("   - Herança: Usuario -> Paciente, ProfissionalSaude -> Medico, Enfermeiro");
            System.out.println("   - Polimorfismo: Árvores de Decisão do Protocolo de Manchester");
            System.out.println("✔ Banco de Dados Relacional e dados de demonstração (seed) inicializados.");
            System.out.println("====================================================================");

            // Mantém a thread principal ativa caso seja executado via linha de comando
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Erro ao inicializar aplicação Java: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
