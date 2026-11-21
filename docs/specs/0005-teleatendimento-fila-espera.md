# Spec 0005 — Teleatendimento e Fila de Espera Inteligente

## 1. Identificadores de Requisito
- **RF007:** Atendimento Médico Remoto (Teleconsulta)
- **RF021:** Fila de Espera Inteligente para Teleatendimento

## 2. Objetivo
Organizar os pacientes direcionados para consulta médica remota em fila ordenada automaticamente pela severidade do Protocolo de Manchester e tempo de espera.

## 3. Regras de Negócio e POO
- **Precedência Clínica:** Casos classificados como **MUITO URGENTE (Laranja)** possuem precedência absoluta sobre **URGENTE (Amarelo)** e **POUCO URGENTE (Verde)**, independentemente da ordem cronológica de chegada na fila.
- **Painel Médico:** O médico visualiza imediatamente o Resumo Estruturado da Triagem, histórico de comorbidades e alergias antes de iniciar o diálogo.
- **Registro de Conduta:** O médico registra hipótese diagnóstica, avaliação e conduta clínica de fechamento.
