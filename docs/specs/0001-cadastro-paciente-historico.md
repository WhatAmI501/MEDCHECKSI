# Spec 0001 — Cadastro de Paciente e Histórico Clínico

## 1. Identificadores de Requisito
- **RF001:** Cadastro de Paciente
- **RF002:** Histórico Clínico do Paciente
- **RS001:** CPF como identificador único e imutável

## 2. Objetivo
Permitir a identificação individual e inequívoca do paciente no MEDCHECK, armazenando dados antropométricos (peso, altura), comorbidades (cardiopatia, diabetes) e lista de alergias conhecidas.

## 3. Regras de Negócio e POO
- **POO:** O paciente é representado pela entidade `Paciente` herdando de `Usuario`.
- **Value Object `CPF`:** Validação rigorosa dos 11 dígitos numéricos e dígitos verificadores. O CPF é imutável após cadastrado.
- **Histórico Clínico:** Entidade `HistoricoClinico` associada ao paciente contendo métodos como `possuiAlergiaA(substancia)` para proteção durante a prescrição.
