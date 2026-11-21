# Spec 0006 — Receita Médica com QR Code e Bloqueio Anti-Alergia

## 1. Identificadores de Requisito
- **RF008:** Emissão e Registro de Receitas Médicas
- **RF024:** Validação de Receita Médica por QR Code
- **RF030:** Registro de Alergias e Interações Medicamentosas
- **RS002:** Bloqueio da IA na emissão de receitas
- **RS007:** Alerta e bloqueio de medicamento conflitante com alergia

## 2. Objetivo
Prover emissão segura de prescrições médicas digitais assinadas por profissionais habilitados, garantindo validação prévia de reações adversas e autenticidade pública por QR Code e código Hash SHA-256.

## 3. Regras de Negócio e POO
- **Bloqueio Absoluto da IA [RS002]:** Apenas usuários com `PerfilAcesso.MEDICO` e CRM ativo podem instanciar e assinar a entidade `ReceitaMedica`.
- **Cruzamento Anti-Alergia [RS007]:** O método `ReceitaService.emitirReceita` cruza o princípio ativo e nome do medicamento com a lista de alergias no `HistoricoClinico` do paciente. Se houver correspondência (ex.: prescrever Dipirona para alérgico a Dipirona), o sistema lança exceção com HTTP 409 exigindo confirmação explícita do médico.
- **Validação Pública [RF024]:** Código Hash SHA-256 único permitindo que qualquer farmácia ou paciente consulte a validade da receita em rota pública.
