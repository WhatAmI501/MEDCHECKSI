# Estado Atual — MEDCHECK (MedIA)

Atualizado em: 2026-11-21

## Concluído no MVP (AV03 & Requisitos MEDCHECK)
- **Repositório GitHub:**
  - Código fonte, schemas e documentação publicados em: https://github.com/WhatAmI501/MEDCHECKSI
  - `.gitignore` atualizado para proteger variáveis de ambiente, binários compilados (`.class`, `bin/`), ambientes virtuais e chaves.
- **Documentação & Especificações:**
  - `docs/prd.md` atualizado com o escopo completo do MEDCHECK (MedIA).
  - `docs/arquitetura.md` atualizado detalhando a integração Java 25 + Node.JS v24, separação RNF13 e os 4 Pilares de POO (60% da nota AV03).
  - 7 especificações técnicas criadas em `docs/specs/` (0001 a 0007).
  - Scripts SQL do banco relacional criados em `database/schema.sql` e `database/seed.sql`.
- **Backend Core Java (POO & Motor Manchester):**
  - Implementação dos 4 Pilares de POO (Abstração, Encapsulamento com Value Objects CPF e SinaisVitais, Herança de Usuários e Polimorfismo com Strategy Pattern no Protocolo de Manchester).
  - Regra crítica de bloqueio anti-alergia [RS007] e bloqueio de prescrição autônoma da IA [RS002].
  - Servidor REST HTTP Java nativo na porta 8080 compilado com sucesso.
  - Suíte de 8 testes automatizados aprovada com 100% de sucesso (`MedCheckCoreTest.java`).
- **Frontend & BFF Node.JS:**
  - Servidor Web e BFF na porta 3000 em `frontend-node/server.js`.
  - Chat conversacional com IA e anonimização de PII [RF004 / RS004].
  - Fila de espera inteligente por Manchester com precedência de gravidade [RF021].
  - Emissão de receitas com QR Code, validação de Hash SHA-256 e visualização de conformidade.
  - Dashboard de indicadores clínicos com acessibilidade WCAG 2.1 [RF012 / RNF04].

## Em Andamento
- Concluído.

## Próximos Passos
- Apresentação da AV03.
