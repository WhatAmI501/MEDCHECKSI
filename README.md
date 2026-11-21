# MEDCHECKSI — Sistema de Triagem Médica com Inteligência Artificial (MedIA)

> **Projeto Integrado:** Programação Orientada a Objetos 2026.2 (Avaliação Integrada AV03)  
> **Instituição:** Centro Universitário CESMAC  
> **Autores:** João Lucas de Assis Lins & João Vinícius Silva dos Santos  
> **Stack:** Java 25 (Core POO & Motor Manchester) + Node.JS v24 (Frontend Web & BFF Conversacional)

---

## 📋 Visão Geral
O **MEDCHECK (MedIA)** é uma plataforma digital projetada para auxiliar pacientes na identificação inicial de sintomas, avaliação de prioridade clínica fundamentada na árvore de decisão do **Protocolo de Manchester** e direcionamento adequado para a rede de saúde antes do deslocamento presencial.

O sistema atua em conformidade com as diretrizes do Conselho Federal de Medicina (Resolução CFM nº 2.314/2022) e a LGPD (Lei nº 13.709/2018), garantindo **separação arquitetural estrita (RNF13)** entre a interpretação de linguagem natural (IA conversacional) e o motor clínico determinístico de classificação de risco.

---

## 🏛️ Os 4 Pilares de POO em Java (60% da Avaliação AV03)

O núcleo de regras de negócio e classificação clínica foi desenvolvido em **Java 25**, aplicando com rigor os pilares da Orientação a Objetos:

1. **Abstração:**
   - Contratos clínicos bem definidos através da classe abstrata [`Usuario`](backend-java/src/br/com/cesmac/medcheck/model/Usuario.java) com método abstrato `obterDescricaoPerfil()`.
   - Classe abstrata [`ProfissionalSaude`](backend-java/src/br/com/cesmac/medcheck/model/ProfissionalSaude.java) encapsulando conselhos profissionais (CRM/COREN).
   - Interface [`IClassificadorManchester`](backend-java/src/br/com/cesmac/medcheck/service/manchester/IClassificadorManchester.java) abstraindo estratégias de árvores de decisão clínica.
2. **Encapsulamento & Invariantes:**
   - Todos os atributos protegidos por visibilidade privada com regras de negócio nos acessores.
   - **Value Object [`CPF`](backend-java/src/br/com/cesmac/medcheck/model/valueobjects/CPF.java):** Validação dos 11 dígitos, cálculo dos dígitos verificadores e imutabilidade estrita (RS001).
   - **Value Object [`SinaisVitais`](backend-java/src/br/com/cesmac/medcheck/model/valueobjects/SinaisVitais.java):** Encapsula regras semânticas de gravidade (`isDessaturandoCritico()`, `isFebreAlta()`, `isCriseHipertensiva()`).
   - **Value Object [`ItemReceita`](backend-java/src/br/com/cesmac/medcheck/model/valueobjects/ItemReceita.java):** Princípio ativo, dosagem e posologia para prevenção de reações alérgicas.
3. **Herança:**
   - `Usuario` $\rightarrow$ `Paciente`
   - `Usuario` $\rightarrow$ `ProfissionalSaude` $\rightarrow$ `Medico`, `Enfermeiro`
4. **Polimorfismo (Strategy Pattern):**
   - Motor do Protocolo de Manchester ([`MotorClassificacaoManchester`](backend-java/src/br/com/cesmac/medcheck/service/manchester/MotorClassificacaoManchester.java)) executando estratégias polimórficas de acordo com o quadro clínico:
     - `ClassificadorDorToracica` (Protocolo cardiovascular/anginoso)
     - `ClassificadorDispneia` (Protocolo respiratório/hipóxia)
     - `ClassificadorCefaleia` (Protocolo neurológico/meníngeo)
     - `ClassificadorGeral` (Fallback clínico determinístico)

---

## 🚀 7 Funcionalidades Funcionais Completas (Ponta a Ponta)

1. **RF001 / RF002 — Cadastro de Paciente e Histórico Clínico:** CPF único e imutável, dados antropométricos, histórico familiar, comorbidades e alergias registradas.
2. **RF003 / RF014 — Autenticação e Perfis RBAC:** Perfis `PACIENTE`, `MEDICO`, `ENFERMEIRO` e `ADMINISTRADOR`, com proteção contra tentativas inválidas (RS008).
3. **RF004 / RF019 — Triagem Conversacional IA com Sinais Vitais:** Coleta guiada e empática de sintomas e sinais vitais autoinformados com anonimização de PII (RS004 / RNF18).
4. **RF005 / RF006 — Motor Protocolo de Manchester & Alerta de Emergência:** Classificação determinística em 5 cores (Vermelho, Laranja, Amarelo, Verde, Azul). Casos Vermelhos disparam alerta imediato de emergência SAMU 192 (RS005).
5. **RF007 / RF021 — Fila Inteligente & Teleconsulta:** Fila dinâmica onde casos Laranja (Muito Urgente) possuem precedência absoluta sobre Amarelos e Verdes.
6. **RF008 / RF024 / RF030 — Receita com QR Code & Bloqueio Anti-Alergia:** Prescrição restrita a médicos (RS002), cruzamento automático com as alergias do paciente com bloqueio preventivo (RS007) e autenticação pública por Hash SHA-256 e QR Code.
7. **RF009 / RF012 / RF032 — Histórico Longitudinal, Dashboard & Auditoria LGPD:** Prontuário em ordem cronológica, gráficos de indicadores clínicos (WCAG 2.1) e log imutável de operações.

---

## 🗄️ Estrutura do Banco de Dados Relacional

- **[`database/schema.sql`](database/schema.sql):** Esquema relacional DDL com 12 tabelas normalizadas.
- **[`database/seed.sql`](database/seed.sql):** Dados de homologação contendo médicos, pacientes com alergias registradas (ex: Dipirona, Penicilina) e triagens pré-alimentadas.

---

## 📂 Estrutura de Diretórios

```
├── backend-java/               # Core de Domínio em Java 25 (POO & Manchester)
│   └── src/br/com/cesmac/medcheck/
│       ├── model/             # Entidades, Enums e Value Objects (Herança e Encapsulamento)
│       ├── service/           # Regras de Negócio e Motor Manchester (Polimorfismo & Strategy)
│       ├── repository/        # Repositório de Dados e Persistência
│       ├── api/               # Servidor REST HTTP Java nativo (Porta 8080)
│       ├── MedCheckApplication.java # Entrypoint Java
│       └── MedCheckCoreTest.java    # Suíte de Testes Automatizados (8/8 Testes)
├── frontend-node/              # BFF e Interface Web em Node.JS v24 (Porta 3000)
│   ├── server.js              # Servidor Web, Gateway IA e Proxy para o Java
│   └── public/                # Telas (Dashboard, Triagem IA, Fila Médica, Receita QR Code, Pacientes)
├── database/                   # Scripts SQL (schema.sql e seed.sql)
└── docs/                       # Documentação Obrigatória em Markdown
    ├── prd.md                 # Documento de Requisitos de Produto
    ├── arquitetura.md         # Definição Arquitetural e Pilares de POO
    ├── state.md               # Estado atual do projeto
    └── specs/                 # Especificações Técnicas (Specs 0001 a 0007)
```

---

## 🛠️ Como Executar o Projeto

### Pré-requisitos
- OpenJDK 21+ ou Java 25
- Node.js v20+ ou v24

### 1. Executar a Suíte de Testes de Homologação dos 4 Pilares de POO:
```bash
javac -encoding UTF-8 -d backend-java/bin $(find backend-java/src -name "*.java")
java -cp backend-java/bin br.com.cesmac.medcheck.MedCheckCoreTest
```

### 2. Iniciar o Servidor Core Java (Porta 8080):
```bash
java -cp backend-java/bin br.com.cesmac.medcheck.MedCheckApplication
```

### 3. Iniciar o Frontend Web & BFF Node.JS (Porta 3000):
```bash
cd frontend-node
node server.js
```

### 4. Acessar no Navegador:
Abra [http://localhost:3000](http://localhost:3000) para acessar a aplicação.
