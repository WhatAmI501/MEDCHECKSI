# Spec 0003 — Triagem Conversacional com IA e Sinais Vitais

## 1. Identificadores de Requisito
- **RF004:** Triagem Conversacional com IA (MedIA)
- **RF019:** Avaliação de Sinais Vitais Autoinformados
- **RS004 / RNF18:** Anonimização de PII

## 2. Objetivo
Conduzir uma entrevista de saúde empática e estruturada via interface de chat inteligente, investigando localização, intensidade, duração, sintomas associados e parâmetros vitais (temperatura, pressão arterial, frequência cardíaca e saturação de O2).

## 3. Regras de Negócio e POO
- **POO:** Value Object `SinaisVitais` encapsula a leitura clínica dos parâmetros com métodos como `isDessaturandoCritico()`, `isFebreAlta()`, `isCriseHipertensiva()`.
- **Anonimização [RS004]:** Antes do processamento de diálogo, dados PII (Nome, CPF) são suprimidos, trafegando exclusivamente o contexto clínico.
- **Resiliência [RNF11]:** Persistência contínua do estado do diálogo.
