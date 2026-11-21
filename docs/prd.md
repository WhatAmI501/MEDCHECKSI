# PRD — MEDCHECK (MedIA)
## Sistema de Triagem Médica com Inteligência Artificial

**Autores Originais:** João Lucas de Assis Lins, João Vinícius Silva dos Santos (Centro Universitário CESMAC)  
**Disciplina:** Programação Orientada a Objetos 2026.2 (Avaliação Integrada AV03)  
**Versão:** 1.0 (MVP Operacional)  
**Data:** Novembro de 2026  

---

### 1. Visão Geral do Produto
O **MEDCHECK (MedIA)** é uma plataforma clínica voltada a apoiar pacientes na identificação inicial de sintomas, avaliação determinística de gravidade fundamentada na árvore de decisão do **Protocolo de Manchester** e direcionamento seguro para a rede de saúde. O sistema atua como ferramenta de acolhimento e triagem, respeitando a Resolução CFM nº 2.314/2022 e a LGPD (Lei nº 13.709/2018).

---

### 2. Problema e Proposta de Valor
- **Superlotação de Prontos-Socorros:** Pacientes com casos não urgentes sobrecarregam emergências hospitalares.
- **Atraso em Emergências Reais:** Pacientes críticos (ex.: infarto, AVC) podem demorar a receber socorro.
- **Proposta MEDCHECK:** Triagem ágil em casa antes do deslocamento, detecção imediata de emergências com alerta para o SAMU 192 (0 min de espera), fila inteligente de telemedicina priorizada por risco clínico e emissão rastreada de receitas médicas com checagem anti-alergia e QR Code.

---

### 3. Escopo Funcional do MVP (7 Funcionalidades Essenciais)
1. **RF001 / RF002 — Cadastro de Paciente e Histórico Clínico:** CPF único e imutável [RS001], peso, altura, histórico familiar, doenças crônicas e alergias declaradas [RF030].
2. **RF003 / RF014 — Autenticação e Perfis de Acesso (RBAC):** Paciente, Médico (com validação de CRM), Enfermeiro e Administrador com proteção contra força bruta [RS008].
3. **RF004 / RF019 — Triagem Conversacional com IA e Sinais Vitais:** Investigação guiada com anonimização de PII [RS004] e coleta opcional de dados vitais (SpO2, PA, temperatura, FC).
4. **RF005 / RF006 — Motor de Classificação Manchester e Alerta de Emergência:** Separação arquitetural estrita [RNF13] entre IA e o motor determinístico em Java (5 cores: Vermelho, Laranja, Amarelo, Verde, Azul). Casos Vermelhos acionam alerta imediato sem retenção em fila [RS005].
5. **RF007 / RF021 — Fila de Espera Inteligente e Teleconsulta:** Fila ordenada por severidade clínica onde Laranja tem precedência absoluta sobre Amarelo. Visualização imediata do resumo pelo médico.
6. **RF008 / RF024 / RF030 — Emissão de Receitas com QR Code e Bloqueio Anti-Alergia:** Prescrição exclusiva por médicos [RS002]; checagem automática com alérgenos do histórico [RS007]; validação pública por Hash SHA-256 e QR Code.
7. **RF009 / RF012 / RF032 — Histórico Longitudinal, Dashboard e Auditoria LGPD:** Linha do tempo das triagens do paciente; painel com distribuição dos casos por cor de Manchester; trilha de auditoria imutável.

---

### 4. Requisitos Não Funcionais (RNFs)
- **[RNF01 / RNF02] Usabilidade:** Interface clara, tempos-alvo explícitos e orientações em até 3 passos.
- **[RNF04] Acessibilidade:** Conformidade WCAG 2.1 — cores de Manchester acompanhadas de texto e ícones distintivos para daltonismo.
- **[RNF06] Desempenho do Motor:** Classificação e alerta de emergência processados em menos de 2 segundos.
- **[RNF13] Separação Arquitetural:** Separação estrita entre a IA conversacional e o motor determinístico Manchester.
- **[RNF14 / RS002] Governança Clínica:** Bloqueio arquitetural absoluto de diagnóstico e prescrição autônoma pela IA.
- **[RNF18] Conformidade LGPD:** Coleta de consentimento e anonimização de PII antes de envio ao orquestrador de diálogo.
