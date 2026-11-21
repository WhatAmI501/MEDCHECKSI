-- ====================================================================
-- DADOS INICIAIS (SEED) PARA TESTE E HOMOLOGAÇÃO DO MVP MEDCHECK
-- ====================================================================

-- 1. Usuários de Acesso
INSERT INTO usuarios (id, nome_completo, cpf, email, senha_hash, perfil, ativo) VALUES
(1, 'Administrador MedCheck', '000.000.000-00', 'admin@medcheck.com.br', 'admin123', 'ADMINISTRADOR', 1),
(2, 'Dr. Roberto Carlos da Silva', '111.222.333-44', 'roberto.med@medcheck.com.br', 'medico123', 'MEDICO', 1),
(3, 'Enfª Juliana Paes Mendes', '555.666.777-88', 'juliana.enf@medcheck.com.br', 'enfermeira123', 'ENFERMEIRO', 1),
(4, 'Mariana Oliveira Costa', '123.456.789-01', 'mariana@email.com', 'paciente123', 'PACIENTE', 1),
(5, 'José Ferreira dos Santos', '234.567.890-12', 'jose@email.com', 'paciente123', 'PACIENTE', 1),
(6, 'Ana Beatriz Lima', '345.678.901-23', 'ana@email.com', 'paciente123', 'PACIENTE', 1);

-- 2. Profissionais de Saúde
INSERT INTO profissionais_saude (usuario_id, registro_conselho, especialidade) VALUES
(2, 'CRM/AL 14892', 'Clínica Geral e Emergência'),
(3, 'COREN/AL 98765', 'Enfermagem Geral e Triagem');

-- 3. Pacientes
INSERT INTO pacientes (usuario_id, data_nascimento, sexo, telefone, peso, altura) VALUES
(4, '1995-04-12', 'Feminino', '(82) 98888-1111', 62.5, 1.65),
(5, '1968-11-20', 'Masculino', '(82) 99999-2222', 84.0, 1.74),
(6, '2001-08-30', 'Feminino', '(82) 97777-3333', 58.0, 1.60);

-- 4. Histórico Clínico e Alergias Conhecidas (RF002 / RF030)
INSERT INTO historicos_clinicos (paciente_id, possui_doenca_cronica, descricao_doencas_cronicas, possui_cardiopatia, possui_diabetes, historico_familiar, alergias, medicamentos_em_uso) VALUES
(4, 0, 'Nenhuma doença crônica', 0, 0, 'Hipertensão materna', 'Dipirona, Anti-inflamatórios não esteroides (AINE)', 'Nenhum de uso contínuo'),
(5, 1, 'Hipertensão Arterial Sistêmica e Diabetes Tipo 2', 1, 1, 'Infarto agudo do miocárdio (pai)', 'Penicilina, Amoxicilina', 'Losartana 50mg, Metformina 850mg'),
(6, 0, 'Asma leve controlada', 0, 0, 'Diabetes paterno', 'Nenhuma alergia conhecida', 'Salbutamol spray sob demanda');

-- 5. Triagens Registradas para alimentar Fila e Dashboard
INSERT INTO triagens (id, paciente_id, queixa_principal, local_sintoma, intensidade, duracao_inicio, frequencia, sintomas_associados, temperatura, pressao_sistolica, pressao_diastolica, frequencia_cardiaca, saturacao_o2, glicemia, resumo_estruturado, status_triagem) VALUES
(1, 5, 'Dor Torácica', 'Precordial com irradiação para o braço esquerdo', 'INTENSA', 'Há 40 minutos', 'CONTINUA', 'Sudorese fria, falta de ar e náuseas', 36.8, 160, 100, 115, 93, 140, 'Paciente masculino, 58 anos, cardiopata e hipertenso, relata dor torácica opressiva súbita há 40 min, sudorese fria e dispneia associada.', 'CONCLUIDA'),
(2, 4, 'Cefaleia Intensa', 'Região frontal e retro-orbitária', 'MODERADA', 'Há 2 dias', 'CONTINUA', 'Fotofobia e febre', 38.6, 120, 80, 88, 98, 95, 'Paciente feminina, 31 anos, queixa-se de dor de cabeça contínua com febre de 38.6ºC e fotofobia há 2 dias. Nega rigidez de nuca.', 'CONCLUIDA'),
(3, 6, 'Dor de Garganta', 'Orofaringe', 'LEVE', 'Há 3 dias', 'INTERMITENTE', 'Coriza e espirros', 37.1, 110, 70, 74, 99, 88, 'Paciente feminina, 25 anos, com odinofagia leve afebril associada a sintomas gripais altos.', 'CONCLUIDA');

-- 6. Classificações Manchester
INSERT INTO classificacoes_manchester (id, triagem_id, nivel_prioridade, cor_manchester, tempo_alvo_minutos, discriminadores_identificados, orientacao_paciente, encaminhamento_recomendado, sinais_alerta) VALUES
(1, 1, 'MUITO_URGENTE', 'LARANJA', 10, 'Dor torácica típica com características anginosas; Sudorese e taquicardia; Histórico de cardiopatia prévia.', 'Procure imediatamente atendimento de emergência ou aguarde conexão com médico de plantão.', 'Teleconsulta prioritária imediata com orientação de encaminhamento a serviço de pronto atendimento cardiológico.', 'Piora súbita da dor, perda de consciência ou piora severa da falta de ar.'),
(2, 2, 'URGENTE', 'AMARELO', 50, 'Cefaleia persistente moderada acompanhada de febre mensurada (38.6°C).', 'Mantenha repouso em ambiente escuro, hidrate-se e aguarde avaliação médica telepresencial.', 'Teleconsulta médica com foco em investigação sindrômica febril.', 'Confusão mental, rigidez de nuca ou vômitos em jato.'),
(3, 3, 'POUCO_URGENTE', 'VERDE', 120, 'Odinofagia leve isolada com estabilidade hemodinâmica e sem sinais de gravidade.', 'Ingestão de líquidos, repouso e acompanhamento ambulatorial.', 'Consulta em unidade básica de saúde ou teleatendimento em horário comercial.', 'Febre alta persistente por mais de 48 horas ou dificuldade de deglutição/respiração.');

-- 7. Fila de Teleatendimento Ativa (RF021 / RF007)
INSERT INTO atendimentos_medicos (id, triagem_id, paciente_id, medico_id, status_atendimento) VALUES
(1, 1, 5, 2, 'AGUARDANDO'),
(2, 2, 4, NULL, 'AGUARDANDO');

-- 8. Auditoria de Demonstração (RF032)
INSERT INTO logs_auditoria (usuario_id, paciente_afetado_id, modulo_acao, tipo_operacao, detalhes, ip_origem) VALUES
(1, 5, 'TRIAGEM', 'INSERCAO', 'Classificação Manchester calculada: LARANJA (Muito Urgente)', '127.0.0.1'),
(1, 4, 'TRIAGEM', 'INSERCAO', 'Classificação Manchester calculada: AMARELO (Urgente)', '127.0.0.1');
