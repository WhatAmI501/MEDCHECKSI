# MEDCHECK — Sistema de Triagem Médica com Inteligência Artificial (MedIA)

---

O **MEDCHECK (MedIA)** é uma plataforma digital projetada para auxiliar pacientes na identificação inicial de sintomas, avaliação de prioridade clínica fundamentada na árvore de decisão do **Protocolo de Manchester** e direcionamento adequado para a rede de saúde antes do deslocamento presencial.

---

### Pré-requisitos
- OpenJDK 21+ ou Java 25
- Node.js v20+ ou v24

---

## Como Rodar o Projeto (Passo a Passo)

### Passo 1: Clonar o Repositório do GitHub

Abra o terminal em uma pasta de sua preferência e execute:

```bash
git clone https://github.com/WhatAmI501/MEDCHECKSI.git
cd MEDCHECKSI
```

---

### Passo 2: Executar a Aplicação (2 Terminais)

Como o sistema conta com dois servidores contínuos e independentes trabalhando integrados (o Core Java na porta **8080** e o Gateway Web Node.js na porta **3000**), você deve utilizar **duas janelas de terminal**:

#### 1. No Terminal 1 — Compilar e Iniciar o Backend Core Java (Porta 8080):

- **No Windows (PowerShell):**
  ```powershell
  javac -encoding UTF-8 -d backend-java/bin (Get-ChildItem -Path backend-java/src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
  java -cp backend-java/bin br.com.cesmac.medcheck.MedCheckApplication
  ```

- **No Windows (Prompt de Comando / CMD):**
  ```cmd
  dir /s /b backend-java\src\*.java > sources.txt && javac -encoding UTF-8 -d backend-java/bin @sources.txt && del sources.txt
  java -cp backend-java/bin br.com.cesmac.medcheck.MedCheckApplication
  ```

- **No Linux / macOS:**
  ```bash
  javac -encoding UTF-8 -d backend-java/bin $(find backend-java/src -name "*.java")
  java -cp backend-java/bin br.com.cesmac.medcheck.MedCheckApplication
  ```

> *Mantenha o Terminal 1 aberto. Ele confirmará que o servidor REST Java está ativo na porta 8080 e os pilares de POO foram carregados.*

#### 2. No Terminal 2 — Iniciar o Frontend & Gateway Node.js (Porta 3000):

Abra uma **segunda janela de terminal**, navegue até a pasta do projeto e execute:

```bash
cd frontend-node
node server.js
```

> *O servidor web iniciará e exibirá: `MEDCHECK (MedIA) - Frontend Web & BFF Node.JS ativo`.*

#### 3. Acessar no Navegador:

Abra o seu navegador e acesse:  
**[http://localhost:3000](http://localhost:3000)**

---

## Módulos e Funcionalidades do Sistema

A aplicação conta com navegação ergonômica via menu lateral adaptativo e barra superior com alternador de tema claro/escuro:

| Módulo | Rota | Descrição |
| :--- | :--- | :--- |
| **Painel Geral** | `/` | Dashboard executivo com novos KPIs operacionais (TME, Resolutividade, Segurança), gráficos analíticos de evolução semanal e rosca de desfechos clínicos. |
| **Triagem Virtual** | `/triagem.html` | Acolhimento interativo com a assistente de IA *MedIA*, categorização determinística pelo Protocolo de Manchester (Vermelho, Laranja, Amarelo, Verde e Azul) e acionamento de SAMU 192. |
| **Fila Priorizada** | `/fila-medica.html` | Gestão médica com fila ordenada estritamente por gravidade clínica e tempo-alvo assistencial. |
| **Prescrição & QR** | `/receita.html` | Emissão de receituário médico digital com assinatura eletrônica e validação pública por **QR Code e Hash SHA-256**. Possui bloqueio automático anti-alergia [RS007]. |
| **Gestão de Pacientes** | `/pacientes.html` | Cadastro e prontuário longitudinal com chave única e histórico de consultas. |
| **Auditoria & Logs** | `/auditoria.html` | Livro eletrônico de eventos com trilha imutável, endereço IP, carimbo temporal, filtros avançados e exportação de relatórios em JSON (Conformidade LGPD Art. 37 & CFM). |

---

## Como Executar a Suíte de Testes Automatizados

O projeto inclui suíte de testes unitários que valida 100% das regras invariantes de negócio e os 4 pilares de POO:

```bash
java -cp backend-java/bin br.com.cesmac.medcheck.MedCheckCoreTest
```

**Cobertura da Suíte:**
- **[TESTE 1]** Encapsulamento de CPF e Validação Invariante com cálculo dos dígitos verificadores.
- **[TESTE 2]** Encapsulamento de Sinais Vitais (Pressão, SpO2, Frequência Cardíaca, Temperatura).
- **[TESTE 3]** Herança e Abstração de Usuários (`Usuario` $\rightarrow$ `Paciente`, `ProfissionalSaude` $\rightarrow$ `Medico`, `Enfermeiro`).
- **[TESTE 4]** Polimorfismo e Strategy Pattern (Caso Cardiovascular: Manchester Laranja - 10 min).
- **[TESTE 5]** Polimorfismo em Emergência Extrema (Caso Respiratório: Manchester Vermelho - Interrupção imediata e acionamento SAMU 192).
- **[TESTE 6]** Fila de Espera Inteligente (Ordenação estrita por gravidade clínica antes do tempo cronológico).
- **[TESTE 7]** Bloqueio Preventivo de Medicamento Conflitante com Alergia prévia do paciente [RS007].
- **[TESTE 8]** Emissão de Receita Digital com Hash Criptográfico SHA-256 e verificação pública de integridade [RF024].

---

## Arquitetura da Solução & 4 Pilares de POO

O Core Java foi projetado seguindo as melhores práticas da Orientação a Objetos:

```
MEDCHECKSI/
├── backend-java/                  # Core em Java 25 (Motor Clínico & Regras de Negócio)
│   ├── src/br/com/cesmac/medcheck/
│   │   ├── api/                   # Servidor HTTP REST Nativo (Porta 8080)
│   │   ├── model/                 # Entidades de Domínio (Usuario, Paciente, Medico, Triagem, Receita)
│   │   │   ├── enums/             # CorManchester, NivelPrioridade, PerfilAcesso, StatusAtendimento
│   │   │   └── valueobjects/      # Value Objects Imutáveis (CPF com validação, SinaisVitais)
│   │   ├── repository/            # Camada de Persistência em Memória Thread-Safe (Singleton)
│   │   ├── service/               # Serviços de Negócio (Auth, Clinica, Receita com Hash SHA-256)
│   │   │   └── manchester/        # Strategy Pattern (Classificadores Cefaleia, Dispneia, DorToracica)
│   │   ├── MedCheckApplication.java # Ponto de entrada do Core Java
│   │   └── MedCheckCoreTest.java    # Suíte de Testes Automatizados
├── frontend-node/                 # Gateway BFF & Web em Node.js (Porta 3000)
│   ├── public/                    # Telas estáticas, estilos CSS e scripts
│   │   ├── index.html             # Painel Geral Executivo (com gráficos e KPIs)
│   │   ├── triagem.html           # Chat com IA MedIA e Classificação Manchester
│   │   ├── fila-medica.html       # Fila Priorizada de Atendimento
│   │   ├── receita.html           # Prescrição Digital & Leitor QR Code
│   │   ├── pacientes.html         # Gestão de Prontuários Clínicos
│   │   ├── auditoria.html         # Módulo Dedicado de Trilha de Auditoria
│   │   ├── styles.css             # Design System Clínico com suporte Claro/Escuro
│   │   └── theme.js               # Gerenciador de Temas com prevenção de FOUC
│   └── server.js                  # Gateway Web com proxy reverso para a porta 8080
├── database/                      # Modelagem Relacional (SQL DDL & Cargas Iniciais)
│   ├── schema.sql
│   └── seed.sql
├── docs/                          # Documentação de Arquitetura, PRDs e Decisões Técnicas (ADR)
└── README.md
```

---

## Licença & Créditos

Código aberto sob a licença [MIT](LICENSE).
