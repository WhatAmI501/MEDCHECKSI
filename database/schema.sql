-- ====================================================================
-- SISTEMA MEDCHECK (MedIA) - ESQUEMA DO BANCO DE DADOS RELACIONAL
-- Projeto Integrado de POO (AV03) & Requisitos Clínicos CESMAC 2026.2
-- ====================================================================

-- 1. Tabela Base de Usuários (RBAC / RF003 / RF014)
CREATE TABLE IF NOT EXISTS usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome_completo VARCHAR(250) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL CHECK(perfil IN ('PACIENTE', 'MEDICO', 'ENFERMEIRO', 'ADMINISTRADOR')),
    ativo BOOLEAN NOT NULL DEFAULT 1,
    data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabela de Profissionais de Saúde (Herança de Usuario)
CREATE TABLE IF NOT EXISTS profissionais_saude (
    usuario_id INTEGER PRIMARY KEY,
    registro_conselho VARCHAR(50) NOT NULL, -- CRM ou COREN
    especialidade VARCHAR(100),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- 3. Tabela de Pacientes (RF001 / Herança de Usuario)
CREATE TABLE IF NOT EXISTS pacientes (
    usuario_id INTEGER PRIMARY KEY,
    data_nascimento DATE NOT NULL,
    sexo VARCHAR(20) NOT NULL,
    telefone VARCHAR(20),
    peso REAL,
    altura REAL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- 4. Tabela de Histórico Clínico e Alergias (RF002 / RF030)
CREATE TABLE IF NOT EXISTS historicos_clinicos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    paciente_id INTEGER NOT NULL,
    possui_doenca_cronica BOOLEAN DEFAULT 0,
    descricao_doencas_cronicas TEXT,
    possui_cardiopatia BOOLEAN DEFAULT 0,
    possui_diabetes BOOLEAN DEFAULT 0,
    historico_familiar TEXT,
    alergias TEXT, -- Alergias conhecidas (ex: Dipirona, Penicilina)
    medicamentos_em_uso TEXT,
    data_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (paciente_id) REFERENCES pacientes(usuario_id)
);

-- 5. Tabela de Sessões de Triagem (RF004 / RF017 / RF019)
CREATE TABLE IF NOT EXISTS triagens (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    paciente_id INTEGER NOT NULL,
    data_hora_inicio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_hora_fim TIMESTAMP,
    queixa_principal VARCHAR(250) NOT NULL,
    local_sintoma VARCHAR(150),
    intensidade VARCHAR(20) CHECK(intensidade IN ('LEVE', 'MODERADA', 'INTENSA')),
    duracao_inicio VARCHAR(100),
    frequencia VARCHAR(30) CHECK(frequencia IN ('CONTINUA', 'INTERMITENTE', 'REPENTINA', 'GRADUAL')),
    sintomas_associados TEXT,
    
    -- Sinais Vitais Autoinformados (RF019)
    temperatura REAL,
    pressao_sistolica INTEGER,
    pressao_diastolica INTEGER,
    frequencia_cardiaca INTEGER,
    saturacao_o2 INTEGER,
    glicemia INTEGER,
    
    resumo_estruturado TEXT,
    status_triagem VARCHAR(30) NOT NULL DEFAULT 'EM_ANDAMENTO' CHECK(status_triagem IN ('EM_ANDAMENTO', 'CONCLUIDA', 'INTERROMPIDA_EMERGENCIA')),
    FOREIGN KEY (paciente_id) REFERENCES pacientes(usuario_id)
);

-- 6. Tabela de Classificação de Risco - Protocolo de Manchester (RF005 / RF006)
CREATE TABLE IF NOT EXISTS classificacoes_manchester (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    triagem_id INTEGER NOT NULL UNIQUE,
    nivel_prioridade VARCHAR(30) NOT NULL CHECK(nivel_prioridade IN ('EMERGENCIA', 'MUITO_URGENTE', 'URGENTE', 'POUCO_URGENTE', 'NAO_URGENTE')),
    cor_manchester VARCHAR(20) NOT NULL CHECK(cor_manchester IN ('VERMELHO', 'LARANJA', 'AMARELO', 'VERDE', 'AZUL')),
    tempo_alvo_minutos INTEGER NOT NULL,
    discriminadores_identificados TEXT NOT NULL,
    orientacao_paciente TEXT NOT NULL,
    encaminhamento_recomendado TEXT NOT NULL,
    sinais_alerta TEXT,
    data_hora_classificacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (triagem_id) REFERENCES triagens(id)
);

-- 7. Tabela de Ocorrências de Emergência (RF006)
CREATE TABLE IF NOT EXISTS alertas_emergencia (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    classificacao_id INTEGER NOT NULL,
    alerta_exibido BOOLEAN DEFAULT 1,
    data_hora_alerta TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sinais_criticos TEXT NOT NULL,
    visualizado_equipe_clinica BOOLEAN DEFAULT 0,
    FOREIGN KEY (classificacao_id) REFERENCES classificacoes_manchester(id)
);

-- 8. Tabela de Atendimentos Médicos Remotos e Teleconsulta (RF007 / RF021)
CREATE TABLE IF NOT EXISTS atendimentos_medicos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    triagem_id INTEGER NOT NULL,
    paciente_id INTEGER NOT NULL,
    medico_id INTEGER,
    data_hora_inicio TIMESTAMP,
    data_hora_fim TIMESTAMP,
    avaliacao_clinica TEXT,
    conduta_encaminhamento TEXT,
    status_atendimento VARCHAR(30) NOT NULL DEFAULT 'AGUARDANDO' CHECK(status_atendimento IN ('AGUARDANDO', 'EM_ATENDIMENTO', 'FINALIZADO')),
    FOREIGN KEY (triagem_id) REFERENCES triagens(id),
    FOREIGN KEY (paciente_id) REFERENCES pacientes(usuario_id),
    FOREIGN KEY (medico_id) REFERENCES profissionais_saude(usuario_id)
);

-- 9. Tabela de Mensagens do Chat Teleconsulta (RF007 / RNF08)
CREATE TABLE IF NOT EXISTS mensagens_atendimento (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    atendimento_id INTEGER NOT NULL,
    remetente_id INTEGER NOT NULL,
    tipo_remetente VARCHAR(20) NOT NULL CHECK(tipo_remetente IN ('PACIENTE', 'MEDICO', 'SISTEMA')),
    mensagem TEXT NOT NULL,
    data_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (atendimento_id) REFERENCES atendimentos_medicos(id),
    FOREIGN KEY (remetente_id) REFERENCES usuarios(id)
);

-- 10. Tabela de Receitas Médicas Digitais (RF008 / RF024 / RF030)
CREATE TABLE IF NOT EXISTS receitas_medicas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    atendimento_id INTEGER NOT NULL,
    paciente_id INTEGER NOT NULL,
    medico_id INTEGER NOT NULL,
    observacoes TEXT,
    data_emissao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    hash_validacao VARCHAR(64) NOT NULL UNIQUE,
    url_qrcode VARCHAR(255) NOT NULL,
    status_receita VARCHAR(20) NOT NULL DEFAULT 'ATIVA' CHECK(status_receita IN ('ATIVA', 'DISPENSADA', 'CANCELADA')),
    FOREIGN KEY (atendimento_id) REFERENCES atendimentos_medicos(id),
    FOREIGN KEY (paciente_id) REFERENCES pacientes(usuario_id),
    FOREIGN KEY (medico_id) REFERENCES profissionais_saude(usuario_id)
);

-- 11. Itens da Prescrição Médica
CREATE TABLE IF NOT EXISTS itens_receita (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    receita_id INTEGER NOT NULL,
    medicamento VARCHAR(150) NOT NULL,
    principio_ativo VARCHAR(150) NOT NULL,
    dosagem VARCHAR(50) NOT NULL,
    posologia VARCHAR(150) NOT NULL,
    quantidade VARCHAR(50) NOT NULL,
    via_administracao VARCHAR(50) NOT NULL,
    FOREIGN KEY (receita_id) REFERENCES receitas_medicas(id) ON DELETE CASCADE
);

-- 12. Trilha de Auditoria Imutável (RF032 / RS006 / LGPD)
CREATE TABLE IF NOT EXISTS logs_auditoria (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id INTEGER,
    paciente_afetado_id INTEGER,
    modulo_acao VARCHAR(50) NOT NULL,
    tipo_operacao VARCHAR(30) NOT NULL CHECK(tipo_operacao IN ('LEITURA', 'INSERCAO', 'ATUALIZACAO', 'EXPORTACAO')),
    detalhes TEXT,
    ip_origem VARCHAR(45) NOT NULL,
    data_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (paciente_afetado_id) REFERENCES pacientes(usuario_id)
);
