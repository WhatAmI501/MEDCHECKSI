# Spec 0007 — Dashboard de Indicadores e Trilha de Auditoria

## 1. Identificadores de Requisito
- **RF009:** Histórico Longitudinal de Triagens e Atendimentos
- **RF012:** Painel de Indicadores Clínicos (Dashboard)
- **RF032:** Log de Auditoria e Rastreabilidade LGPD
- **RS006:** Imutabilidade dos registros clínicos e de auditoria

## 2. Objetivo
Consolidar métricas operacionais e epidemiológicas para a gestão de saúde e exibir o histórico longitudinal completo do paciente, assegurando rastreabilidade irrefutável de todas as ações sensíveis realizadas no sistema.

## 3. Regras de Negócio e POO
- **Histórico Longitudinal:** Exibição cronológica decrescente de todas as sessões de triagem, classificações Manchester e receitas emitidas por paciente.
- **Métricas do Dashboard:** Total de triagens, pacientes atendidos, teleconsultas e distribuição de casos pelas 5 cores do Protocolo de Manchester com visual WCAG 2.1.
- **Trilha de Auditoria Imutável:** Entidade `LogAuditoria` persistindo `usuarioId`, `pacienteAfetadoId`, `moduloAcao`, `tipoOperacao`, `ipOrigem` e `timestamp`.
