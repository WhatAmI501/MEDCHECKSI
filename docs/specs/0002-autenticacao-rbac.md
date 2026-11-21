# Spec 0002 — Autenticação e Perfis de Acesso (RBAC)

## 1. Identificadores de Requisito
- **RF003:** Gerenciamento de Usuários e Permissões
- **RF014:** Autenticação e Login de Usuários
- **RS008:** Proteção contra força bruta e controle de sessão

## 2. Objetivo
Autenticar usuários no sistema com base em login (CPF ou e-mail) e senha, validando o perfil de acesso (`PACIENTE`, `MEDICO`, `ENFERMEIRO`, `ADMINISTRADOR`).

## 3. Regras de Negócio e POO
- **POO:** Hierarquia de classes `Usuario` $\rightarrow$ `ProfissionalSaude` $\rightarrow$ `Medico`, `Enfermeiro`.
- **RBAC:** Apenas usuários com `PerfilAcesso.MEDICO` e CRM válido possuem autorização para emissão de prescrições e finalização de consultas.
- **Segurança [RS008]:** Bloqueio temporário da conta após 5 tentativas incorretas consecutivas.
