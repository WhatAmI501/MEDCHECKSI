# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

- **Pacientes**: Pessoas buscando orientação inicial ou atendimento em serviços de pronto atendimento. Precisam relatar sintomas de forma natural, rápida e humanizada, compreendendo seu grau de urgência sem jargões médicos indecifráveis.
- **Enfermeiros Triagistas**: Profissionais que revisam os dados coletados na pré-triagem guiada por IA, aferem e validam sinais vitais e confirmam a classificação de risco Manchester.
- **Médicos Plantonistas**: Profissionais que realizam o atendimento clínico ordenado por gravidade de Manchester, registram anamnese, prescrevem digitalmente e necessitam de bloqueio preventivo automático contra alergias medicamentosas conhecidas do paciente.
- **Administradores e Auditores Hospitalares**: Gestores que acompanham indicadores operacionais em tempo real (tempo médio de espera, distribuição de risco, taxa de adesão aos tempos do Protocolo de Manchester) e registros imutáveis de auditoria clínica.

## Product Purpose

O MEDCHECK é uma plataforma clínica inteligente de pré-triagem e acolhimento hospitalar projetada para otimizar o fluxo de atendimento em pronto-socorro e unidades de emergência. Ele existe para reduzir o tempo de espera no atendimento emergencial, eliminar a subjetividade na triagem por meio da aplicação rigorosa e auditável do Protocolo de Manchester, e resguardar a segurança do paciente com bloqueios automáticos de alergias e rastreabilidade total. O sucesso é medido pela priorização precisa e sem atrasos de casos graves (Vermelho e Laranja), cumprimento dos tempos-alvo por classificação de risco e zero incidentes de prescrição com medicamentos contraindicados por alergia.

## Positioning

Diferente de chatbots de telemedicina genéricos que arriscam diagnósticos automatizados alucinados ou de sistemas legados de prontuário eletrônico rígidos e lentos, o MEDCHECK adota uma **arquitetura híbrida com separação estrita de responsabilidades (RNF13)**:
1. Uma camada conversacional fluida em Node.js para acolhimento humanizado, coleta amigável de sintomas e anonimização de dados pessoais (LGPD).
2. Um motor de decisão clínica em Java 25 Core POO estritamente determinístico, auditável e imutável baseado no Protocolo de Manchester, com vedação regulatória absoluta de diagnóstico e prescrição autônoma por IA (Resolução CFM 2.314/2022, RS002 e RNF14).

## Operating Context

- Ambiente hospitalar de pronto-socorro, unidades de pronto atendimento (UPA) e triagem pré-hospitalar web.
- Terminais de autoatendimento, dispositivos móveis de pacientes ou tablets/estações de enfermagem e consultórios médicos.
- Fluxo de trabalho de alta pressão e tempo crítico, onde clareza visual imediata, legibilidade das cores de prioridade (Vermelho a Azul) e velocidade de resposta operacional são essenciais.

## Capabilities and Constraints

- **Entrevista de Pré-triagem com IA Humanizada**: Condução de anamnese guiada, coleta de queixa principal, tempo de evolução e sinais vitais.
- **Motor Determinístico Manchester (Java Core POO)**: Aplicação dos discriminadores clínicos (vias aéreas, choque, dor torácica típica, dor severa, cefaleia súbita, etc.) categorizando em 5 níveis com tempos-alvo:
  - Vermelho (Emergência: 0 min)
  - Laranja (Muito Urgente: 10 min)
  - Amarelo (Urgente: 60 min)
  - Verde (Pouco Urgente: 120 min)
  - Azul (Não Urgente: 240 min)
- **Fila Dinâmica Prioritária**: Ordenação estrita por gravidade (cor de Manchester) e tempo de chegada, garantindo atendimento equânime e transparente.
- **Prescrição Digital com Bloqueio Ativo de Alergias**: Checagem automática em tempo real cruzando medicamentos prescritos com o histórico de alergias do paciente, com assinatura digital médica e validação pública por QR Code.
- **Restrições Regulatórias e Éticas**: Proibição estrita de prescrição ou diagnóstico automático por inteligência artificial (RS002 / RNF14 / Resoluções CFM).
- **Rastreabilidade e Auditoria**: Registro imutável de todas as ações clínicas com carimbo de data/hora e identificação do profissional (RNF16).

## Brand Commitments

- **Nome**: MEDCHECK (Plataforma Clínica de Triagem Inteligente).
- **Tom de Voz**: Clínico, seguro, objetivo, acolhedor e transparente. Informações de risco apresentadas de maneira serena, sem alarmismo desnecessário, mas com autoridade clara quando se tratar de emergência imediata.
- **Identidade Visual**: Design limpo e moderno (Operate Mode Impeccable), paleta médica refinada (azul petróleo `#0284c7`, ardósia `#0f172a`), tipografia de alta legibilidade (Plus Jakarta Sans e JetBrains Mono para dados clínicos), alto contraste WCAG 2.1 AA nas cores normatizadas do Protocolo de Manchester com apoio de indicadores geométricos para acessibilidade de daltonismo.

## Evidence on Hand

- `AV03.pdf`: Avaliação acadêmica de Programação Orientada a Objetos 2026.2 detalhando requisitos de modelagem OO (Herança, Encapsulamento, Abstração, Polimorfismo).
- `Doc. Requisitos MEDCHECK 11.2026.pdf`: Documento de engenharia de software com 7 requisitos funcionais (RF001 a RF007), 16 requisitos não funcionais e regras de segurança clínica (RS001 a RS007).
- `docs/prd.md`: Documento de requisitos de produto formalizado.
- `docs/arquitetura.md`: Arquitetura do sistema dividida em Java 25 Core POO e Node.js BFF/Web.
- `docs/specs/`: Especificações técnicas detalhadas 0001 a 0007.
- `database/schema.sql` e `database/seed.sql`: Estrutura relacional e dados de demonstração clínica.

## Product Principles

1. **A Vida e a Segurança Vêm em Primeiro Lugar**: Nenhum recurso de conveniência ou automação pode se sobrepor à segurança do paciente ou ignorar checagens de alergias e sinais vitais.
2. **IA Acolhe, o Motor Manchester Decide, o Médico Valida**: A IA nunca é a autoridade diagnóstica final. A classificação de urgência é determinística e a conduta terapêutica é responsabilidade exclusiva do médico.
3. **Clareza Imediata em Situações de Estresse**: Em telas de emergência e triagem, cada segundo conta. A tipografia, o contraste e a hierarquia visual devem comunicar a gravidade em milissegundos sem sobrecarga cognitiva.
4. **Auditabilidade e Transparência Absoluta**: Todo evento clínico, alteração de fila e emissão de receita deve ser rastreável com carimbo de tempo e autoria para garantia ética e legal.

## Accessibility & Inclusion

- Conformidade com diretrizes WCAG 2.1 nível AA.
- Acessibilidade nas cores do Protocolo de Manchester: o status nunca é comunicado exclusivamente por cor; utiliza-se sempre rótulo textual legível, nível de prioridade e tempo-alvo em conjunto com badges de alto contraste e indicadores geométricos.
- Tipografia limpa com suporte a números tabulares (`font-variant-numeric: tabular-nums`) em sinais vitais, horários e cronômetros de fila.
