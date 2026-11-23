-- ====================================================================
-- SISTEMA MEDCHECK (MedIA) — SCRIPT DE INTEGRAÇÃO DE BANCO DE DADOS
-- Triagem Clínica Inteligente & Protocolo de Manchester
-- Conformidade: CFM, LGPD (Art. 37) e Padrões de Segurança Clínica
-- ====================================================================

BEGIN;

-- ====================================================================
-- BLOCO 1: DDL — CRIAÇÃO DE TABELAS COM AS 4 INTEGRIDADES RELACIONAIS
-- 1. Entidade (PKs únicas e não nulas)
-- 2. Referencial (FKs consistentes com regras ON DELETE)
-- 3. Domínio (Tipos semânticos e CHECK constraints regulamentares)
-- 4. Vazio (NOT NULL explícito nos campos mandatórios)
-- ====================================================================

-- 1.1 Tabela de Usuários (Base de Acesso)
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome_completo VARCHAR(150) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_usuarios_perfil CHECK (perfil IN ('ADMINISTRADOR', 'MEDICO', 'ENFERMEIRO', 'PACIENTE')),
    CONSTRAINT chk_usuarios_cpf_formato CHECK (cpf ~ '^[0-9]{3}\.[0-9]{3}\.[0-9]{3}\-[0-9]{2}$')
);

-- 1.2 Tabela de Profissionais de Saúde (Médicos e Enfermeiros)
CREATE TABLE IF NOT EXISTS profissionais_saude (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE,
    registro_conselho VARCHAR(30) NOT NULL UNIQUE, -- CRM/AL, COREN/AL
    especialidade VARCHAR(100) NOT NULL,
    CONSTRAINT fk_profissionais_usuario FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) ON DELETE CASCADE
);

-- 1.3 Tabela de Pacientes
CREATE TABLE IF NOT EXISTS pacientes (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE,
    data_nascimento DATE NOT NULL,
    sexo VARCHAR(20) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    peso NUMERIC(5,2),
    altura NUMERIC(3,2),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pacientes_usuario FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT chk_pacientes_sexo CHECK (sexo IN ('Masculino', 'Feminino', 'Outro')),
    CONSTRAINT chk_pacientes_peso CHECK (peso IS NULL OR (peso > 0.0 AND peso <= 450.0)),
    CONSTRAINT chk_pacientes_altura CHECK (altura IS NULL OR (altura > 0.30 AND altura <= 2.80))
);

-- 1.4 Histórico Clínico e Relação de Alergias (Guardrail RS007 / RF002)
CREATE TABLE IF NOT EXISTS historicos_clinicos (
    id BIGSERIAL PRIMARY KEY,
    paciente_id BIGINT NOT NULL UNIQUE,
    possui_doenca_cronica BOOLEAN NOT NULL DEFAULT FALSE,
    descricao_doencas_cronicas TEXT,
    possui_cardiopatia BOOLEAN NOT NULL DEFAULT FALSE,
    possui_diabetes BOOLEAN NOT NULL DEFAULT FALSE,
    historico_familiar TEXT,
    alergias TEXT, -- Relação de medicamentos/alérgenos para checagem da IA
    medicamentos_em_uso TEXT,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_historicos_paciente FOREIGN KEY (paciente_id) 
        REFERENCES pacientes(id) ON DELETE CASCADE
);

-- 1.5 Tabela de Triagens Clínicas (Acolhimento & Sinais Vitais)
CREATE TABLE IF NOT EXISTS triagens (
    id BIGSERIAL PRIMARY KEY,
    paciente_id BIGINT NOT NULL,
    queixa_principal VARCHAR(150) NOT NULL,
    local_sintoma VARCHAR(100),
    intensidade VARCHAR(20) NOT NULL,
    duracao_inicio VARCHAR(100) NOT NULL,
    frequencia VARCHAR(30) NOT NULL,
    sintomas_associados TEXT,
    temperatura NUMERIC(4,1),
    pressao_sistolica INTEGER,
    pressao_diastolica INTEGER,
    frequencia_cardiaca INTEGER,
    saturacao_o2 INTEGER,
    glicemia INTEGER,
    resumo_estruturado TEXT NOT NULL,
    status_triagem VARCHAR(20) NOT NULL DEFAULT 'EM_ANDAMENTO',
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_triagens_paciente FOREIGN KEY (paciente_id) 
        REFERENCES pacientes(id) ON DELETE RESTRICT,
    CONSTRAINT chk_triagens_intensidade CHECK (intensidade IN ('LEVE', 'MODERADA', 'INTENSA', 'SEVERA')),
    CONSTRAINT chk_triagens_frequencia CHECK (frequencia IN ('CONTINUA', 'INTERMITENTE', 'SUBITA', 'PROGRESSIVA')),
    CONSTRAINT chk_triagens_status CHECK (status_triagem IN ('EM_ANDAMENTO', 'CONCLUIDA', 'CANCELADA')),
    CONSTRAINT chk_sinais_temperatura CHECK (temperatura IS NULL OR (temperatura >= 30.0 AND temperatura <= 45.0)),
    CONSTRAINT chk_sinais_pa_sistolica CHECK (pressao_sistolica IS NULL OR (pressao_sistolica >= 40 AND pressao_sistolica <= 300)),
    CONSTRAINT chk_sinais_pa_diastolica CHECK (pressao_diastolica IS NULL OR (pressao_diastolica >= 20 AND pressao_diastolica <= 200)),
    CONSTRAINT chk_sinais_fc CHECK (frequencia_cardiaca IS NULL OR (frequencia_cardiaca >= 25 AND frequencia_cardiaca <= 260)),
    CONSTRAINT chk_sinais_spo2 CHECK (saturacao_o2 IS NULL OR (saturacao_o2 >= 50 AND saturacao_o2 <= 100)),
    CONSTRAINT chk_sinais_glicemia CHECK (glicemia IS NULL OR (glicemia >= 20 AND glicemia <= 800))
);

-- 1.6 Classificação de Risco pelo Protocolo de Manchester
CREATE TABLE IF NOT EXISTS classificacoes_manchester (
    id BIGSERIAL PRIMARY KEY,
    triagem_id BIGINT NOT NULL UNIQUE,
    nivel_prioridade VARCHAR(30) NOT NULL,
    cor_manchester VARCHAR(15) NOT NULL,
    tempo_alvo_minutos INTEGER NOT NULL,
    discriminadores_identificados TEXT NOT NULL,
    orientacao_paciente TEXT NOT NULL,
    encaminhamento_recomendado TEXT NOT NULL,
    sinais_alerta TEXT,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_classificacao_triagem FOREIGN KEY (triagem_id) 
        REFERENCES triagens(id) ON DELETE CASCADE,
    CONSTRAINT chk_manchester_cor CHECK (cor_manchester IN ('VERMELHO', 'LARANJA', 'AMARELO', 'VERDE', 'AZUL')),
    CONSTRAINT chk_manchester_prioridade CHECK (nivel_prioridade IN ('EMERGENCIA', 'MUITO_URGENTE', 'URGENTE', 'POUCO_URGENTE', 'NAO_URGENTE')),
    CONSTRAINT chk_manchester_tempo CHECK (tempo_alvo_minutos >= 0)
);

-- 1.7 Atendimentos Médicos (Fila de Teleconsulta)
CREATE TABLE IF NOT EXISTS atendimentos_medicos (
    id BIGSERIAL PRIMARY KEY,
    triagem_id BIGINT NOT NULL UNIQUE,
    paciente_id BIGINT NOT NULL,
    medico_id BIGINT,
    status_atendimento VARCHAR(20) NOT NULL DEFAULT 'AGUARDANDO',
    iniciado_em TIMESTAMP WITH TIME ZONE,
    finalizado_em TIMESTAMP WITH TIME ZONE,
    observacoes_clinicas TEXT,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_atendimentos_triagem FOREIGN KEY (triagem_id) 
        REFERENCES triagens(id) ON DELETE RESTRICT,
    CONSTRAINT fk_atendimentos_paciente FOREIGN KEY (paciente_id) 
        REFERENCES pacientes(id) ON DELETE RESTRICT,
    CONSTRAINT fk_atendimentos_medico FOREIGN KEY (medico_id) 
        REFERENCES profissionais_saude(id) ON DELETE SET NULL,
    CONSTRAINT chk_atendimento_status CHECK (status_atendimento IN ('AGUARDANDO', 'EM_ATENDIMENTO', 'FINALIZADO', 'CANCELADO'))
);

-- 1.8 Receitas Médicas Emitidas (RF024)
CREATE TABLE IF NOT EXISTS receitas_medicas (
    id BIGSERIAL PRIMARY KEY,
    atendimento_id BIGINT NOT NULL,
    medico_id BIGINT NOT NULL,
    paciente_id BIGINT NOT NULL,
    medicamentos_prescritos TEXT NOT NULL,
    instrucoes_posologia TEXT NOT NULL,
    hash_assinatura_sha256 VARCHAR(64) NOT NULL UNIQUE,
    status_receita VARCHAR(20) NOT NULL DEFAULT 'EMITIDA',
    emitida_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_receitas_atendimento FOREIGN KEY (atendimento_id) 
        REFERENCES atendimentos_medicos(id) ON DELETE RESTRICT,
    CONSTRAINT fk_receitas_medico FOREIGN KEY (medico_id) 
        REFERENCES profissionais_saude(id) ON DELETE RESTRICT,
    CONSTRAINT fk_receitas_paciente FOREIGN KEY (paciente_id) 
        REFERENCES pacientes(id) ON DELETE RESTRICT,
    CONSTRAINT chk_receita_status CHECK (status_receita IN ('EMITIDA', 'DISPENSADA', 'CANCELADA')),
    CONSTRAINT chk_receita_hash_len CHECK (LENGTH(hash_assinatura_sha256) = 64)
);

-- 1.9 Trilha de Auditoria & Conformidade Regulatória (LGPD Art. 37 / CFM)
CREATE TABLE IF NOT EXISTS logs_auditoria (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT,
    paciente_afetado_id BIGINT,
    modulo_acao VARCHAR(50) NOT NULL,
    tipo_operacao VARCHAR(20) NOT NULL,
    detalhes TEXT NOT NULL,
    ip_origem VARCHAR(45) NOT NULL DEFAULT '127.0.0.1',
    data_hora TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_logs_usuario FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT fk_logs_paciente FOREIGN KEY (paciente_afetado_id) 
        REFERENCES pacientes(id) ON DELETE SET NULL,
    CONSTRAINT chk_logs_operacao CHECK (tipo_operacao IN ('INSERCAO', 'ATUALIZACAO', 'EXCLUSAO', 'CONSULTA', 'BLOQUEIO', 'LOGIN'))
);

-- ====================================================================
-- BLOCO 2: TRIGGERS — AUTOMAÇÃO, AUDITORIA E GUARDRAIL CLÍNICO
-- ====================================================================

-- 2.1 Função e Trigger de Auditoria Automática para Triagens e Receitas
CREATE OR REPLACE FUNCTION fn_auditar_triagem_e_receita()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_TABLE_NAME = 'triagens') THEN
        INSERT INTO logs_auditoria (usuario_id, paciente_afetado_id, modulo_acao, tipo_operacao, detalhes, ip_origem)
        VALUES (
            NULL,
            NEW.paciente_id,
            'TRIAGEM',
            'INSERCAO',
            CONCAT('Triagem #', NEW.id, ' cadastrada. Queixa: ', NEW.queixa_principal, ' | Status: ', NEW.status_triagem),
            '127.0.0.1'
        );
    ELSIF (TG_TABLE_NAME = 'receitas_medicas') THEN
        INSERT INTO logs_auditoria (usuario_id, paciente_afetado_id, modulo_acao, tipo_operacao, detalhes, ip_origem)
        VALUES (
            NEW.medico_id,
            NEW.paciente_id,
            'RECEITA',
            'INSERCAO',
            CONCAT('Prescrição gerada #', NEW.id, ' com Hash SHA-256: ', SUBSTRING(NEW.hash_assinatura_sha256 FROM 1 FOR 16), '...'),
            '127.0.0.1'
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_auditoria_triagens
AFTER INSERT ON triagens
FOR EACH ROW EXECUTE FUNCTION fn_auditar_triagem_e_receita();

CREATE TRIGGER trg_auditoria_receitas
AFTER INSERT ON receitas_medicas
FOR EACH ROW EXECUTE FUNCTION fn_auditar_triagem_e_receita();

-- 2.2 Guardrail Clínico: Bloqueio Preventivo Anti-Alergia (RS007)
CREATE OR REPLACE FUNCTION fn_guardrail_alergia_prescricao()
RETURNS TRIGGER AS $$
DECLARE
    v_alergias TEXT;
    v_termo TEXT;
BEGIN
    -- Busca as alergias cadastradas no histórico clínico do paciente
    SELECT LOWER(COALESCE(alergias, ''))
      INTO v_alergias
      FROM historicos_clinicos
     WHERE paciente_id = NEW.paciente_id;

    -- Se o paciente possuir histórico de alergias cadastrado
    IF v_alergias IS NOT NULL AND LENGTH(TRIM(v_alergias)) > 0 THEN
        -- Itera sobre medicamentos comuns (ex: dipirona, penicilina, aine, aspirina, amoxicilina)
        FOR v_termo IN SELECT unnest(string_to_array(LOWER(v_alergias), ',')) LOOP
            v_termo := TRIM(v_termo);
            IF LENGTH(v_termo) > 2 AND POSITION(v_termo IN LOWER(NEW.medicamentos_prescritos)) > 0 THEN
                -- Grava evento crítico de BLOQUEIO na auditoria regulatória
                INSERT INTO logs_auditoria (usuario_id, paciente_afetado_id, modulo_acao, tipo_operacao, detalhes, ip_origem)
                VALUES (
                    NEW.medico_id,
                    NEW.paciente_id,
                    'PRESCRICAO_GUARDRAIL',
                    'BLOQUEIO',
                    CONCAT('INTERRUPÇÃO DE SEGURANÇA [RS007]: Conflito alérgico detectado para o fármaco: ', UPPER(v_termo)),
                    '127.0.0.1'
                );

                RAISE EXCEPTION 'BLOQUEIO DE SEGURANÇA CLÍNICA [RS007]: O paciente possui histórico de hipersensibilidade grave/alergia registrada a (%)!', UPPER(v_termo);
            END IF;
        END LOOP;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validar_alergia_prescricao
BEFORE INSERT OR UPDATE ON receitas_medicas
FOR EACH ROW EXECUTE FUNCTION fn_guardrail_alergia_prescricao();

-- 2.3 Garantia de Imutabilidade da Trilha de Auditoria (Append-Only)
CREATE OR REPLACE FUNCTION fn_proteger_trilha_auditoria()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'VIOLAÇÃO DE CONFORMIDADE REGULATÓRIA: Registros de auditoria são estritamente imutáveis (Append-Only) e não podem sofrer UPDATE ou DELETE.';
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_bloquear_modificacao_auditoria
BEFORE UPDATE OR DELETE ON logs_auditoria
FOR EACH ROW EXECUTE FUNCTION fn_proteger_trilha_auditoria();

-- ====================================================================
-- BLOCO 3: ÍNDICES (INDEX) — OTIMIZAÇÃO DE CONSULTAS E FILA
-- ====================================================================

-- 3.1 Índice Único para Busca Instantânea por CPF de Pacientes e Usuários
CREATE UNIQUE INDEX IF NOT EXISTS idx_usuarios_cpf ON usuarios (cpf);

-- 3.2 Índice Único para Validação e Rastreio Público de Receitas por Hash
CREATE UNIQUE INDEX IF NOT EXISTS idx_receitas_hash_sha256 ON receitas_medicas (hash_assinatura_sha256);

-- 3.3 Índice Composto e Parcial para a Fila Ativa Ordenada por Gravidade e Chegada
CREATE INDEX IF NOT EXISTS idx_fila_atendimentos_ativos 
ON atendimentos_medicos (status_atendimento, criado_em)
WHERE status_atendimento = 'AGUARDANDO';

-- 3.4 Índice para Acelerar a Recuperação do Histórico de Triagens por Paciente
CREATE INDEX IF NOT EXISTS idx_triagens_paciente_criacao 
ON triagens (paciente_id, criado_em DESC);

-- 3.5 Índice Temporal para Filtragem de Auditoria Regulatória
CREATE INDEX IF NOT EXISTS idx_logs_auditoria_data 
ON logs_auditoria (data_hora DESC, modulo_acao);

-- ====================================================================
-- BLOCO 4: VIEWS — VISÕES CLÍNICAS E OPERACIONAIS CONSOLIDADAS
-- ====================================================================

-- 4.1 Fila Priorizada do Protocolo de Manchester (Ordenação Clínica Determinística)
CREATE OR REPLACE VIEW vw_fila_priorizada_manchester AS
SELECT 
    am.id AS atendimento_id,
    am.status_atendimento,
    u.nome_completo AS nome_paciente,
    u.cpf AS cpf_paciente,
    t.queixa_principal,
    cm.cor_manchester,
    cm.nivel_prioridade,
    cm.tempo_alvo_minutos,
    ROUND(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - am.criado_em)) / 60) AS minutos_em_espera,
    CASE 
        WHEN ROUND(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - am.criado_em)) / 60) > cm.tempo_alvo_minutos THEN TRUE
        ELSE FALSE
    END AS sla_estourado,
    am.criado_em AS data_hora_entrada
FROM atendimentos_medicos am
INNER JOIN pacientes p ON am.paciente_id = p.id
INNER JOIN usuarios u ON p.usuario_id = u.id
INNER JOIN triagens t ON am.triagem_id = t.id
INNER JOIN classificacoes_manchester cm ON cm.triagem_id = t.id
WHERE am.status_atendimento = 'AGUARDANDO'
ORDER BY 
    CASE cm.cor_manchester
        WHEN 'VERMELHO' THEN 1
        WHEN 'LARANJA'  THEN 2
        WHEN 'AMARELO'  THEN 3
        WHEN 'VERDE'    THEN 4
        WHEN 'AZUL'     THEN 5
        ELSE 6
    END ASC,
    am.criado_em ASC;

-- 4.2 Prontuário Consolidado e Alergias Ativas do Paciente
CREATE OR REPLACE VIEW vw_prontuario_consolidado AS
SELECT 
    p.id AS paciente_id,
    u.nome_completo,
    u.cpf,
    p.data_nascimento,
    hc.possui_cardiopatia,
    hc.possui_diabetes,
    hc.alergias,
    hc.medicamentos_em_uso,
    t_recente.queixa_principal AS ultima_queixa,
    cm_recente.cor_manchester AS ultimo_risco_manchester,
    COUNT(DISTINCT rm.id) AS total_receitas_emitidas
FROM pacientes p
INNER JOIN usuarios u ON p.usuario_id = u.id
LEFT JOIN historicos_clinicos hc ON hc.paciente_id = p.id
LEFT JOIN LATERAL (
    SELECT id, queixa_principal
      FROM triagens
     WHERE paciente_id = p.id
     ORDER BY criado_em DESC
     LIMIT 1
) t_recente ON TRUE
LEFT JOIN classificacoes_manchester cm_recente ON cm_recente.triagem_id = t_recente.id
LEFT JOIN receitas_medicas rm ON rm.paciente_id = p.id
GROUP BY 
    p.id, u.nome_completo, u.cpf, p.data_nascimento, 
    hc.possui_cardiopatia, hc.possui_diabetes, hc.alergias, 
    hc.medicamentos_em_uso, t_recente.queixa_principal, cm_recente.cor_manchester;

-- ====================================================================
-- BLOCO 5: DCL — CONTROLE DE ACESSO E PRINCÍPIO DO MENOR PRIVILÉGIO
-- ====================================================================

-- 5.1 Definição Segura de Roles no Sistema
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'medcheck_app') THEN
        CREATE ROLE medcheck_app WITH LOGIN PASSWORD 'app_medcheck_sec_2026';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'medcheck_medico') THEN
        CREATE ROLE medcheck_medico;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'medcheck_enfermeiro') THEN
        CREATE ROLE medcheck_enfermeiro;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'medcheck_auditor') THEN
        CREATE ROLE medcheck_auditor;
    END IF;
END
$$;

-- 5.2 Revogação de Acessos Públicos Padrão (Hardening)
REVOKE ALL ON ALL TABLES IN SCHEMA public FROM PUBLIC;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM PUBLIC;

-- 5.3 Perfil Aplicação (medcheck_app): Acesso Operacional Geral
GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA public TO medcheck_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO medcheck_app;
REVOKE DELETE ON logs_auditoria FROM medcheck_app;

-- 5.4 Perfil Médico (medcheck_medico): Teleconsultas e Prescrições
GRANT SELECT ON usuarios, pacientes, historicos_clinicos, triagens, classificacoes_manchester TO medcheck_medico;
GRANT SELECT, UPDATE ON atendimentos_medicos TO medcheck_medico;
GRANT SELECT, INSERT ON receitas_medicas TO medcheck_medico;
GRANT SELECT ON vw_fila_priorizada_manchester, vw_prontuario_consolidado TO medcheck_medico;

-- 5.5 Perfil Enfermeiro (medcheck_enfermeiro): Acolhimento e Triagem
GRANT SELECT, INSERT, UPDATE ON pacientes, historicos_clinicos TO medcheck_enfermeiro;
GRANT SELECT, INSERT ON triagens, classificacoes_manchester, atendimentos_medicos TO medcheck_enfermeiro;
GRANT SELECT ON vw_fila_priorizada_manchester, vw_prontuario_consolidado TO medcheck_enfermeiro;

-- 5.6 Perfil Auditor / DPO (medcheck_auditor): Auditoria e Conformidade LGPD
GRANT SELECT ON logs_auditoria TO medcheck_auditor;
GRANT SELECT ON vw_fila_priorizada_manchester, vw_prontuario_consolidado TO medcheck_auditor;
-- Garantia de que o auditor não tem nenhuma permissão de escrita/modificação
REVOKE INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public FROM medcheck_auditor;

COMMIT;
-- ====================================================================
-- FIM DO SCRIPT DE INTEGRAÇÃO MEDCHECK
-- ====================================================================