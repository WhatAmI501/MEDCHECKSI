# Spec 0004 — Motor do Protocolo de Manchester e Alerta de Emergência

## 1. Identificadores de Requisito
- **RF005:** Classificação de Prioridade Clínica (Manchester)
- **RF006:** Fluxo de Alerta e Encaminhamento de Emergência
- **RNF13:** Separação Arquitetural entre IA e Motor Clínico
- **RS005:** Prevenção de Atraso em Casos Críticos de Emergência

## 2. Objetivo
Processar os dados coletados na triagem em uma árvore de decisão determinística e validada baseada no Protocolo de Manchester, classificando o paciente em uma das 5 categorias (Vermelho, Laranja, Amarelo, Verde, Azul).

## 3. Regras de Negócio e POO
- **POO (Polimorfismo & Strategy Pattern):**
  - Interface `IClassificadorManchester` com implementações polimórficas:
    - `ClassificadorDorToracica`
    - `ClassificadorDispneia`
    - `ClassificadorCefaleia`
    - `ClassificadorGeral`
  - O `MotorClassificacaoManchester` seleciona dinamicamente a estratégia aplicável.
- **Gatilho de Emergência Vermelha [RF006 / RS005]:**
  - Se a classificação resultar em **VERMELHO**, a teleconsulta e fila são imediatamente interrompidas e um alerta de tela de alto contraste é disparado orientando acionamento imediato do SAMU 192 ou ida à UPA mais próxima.
