package br.com.cesmac.medcheck.service;

import br.com.cesmac.medcheck.model.*;
import br.com.cesmac.medcheck.model.enums.CorManchester;
import br.com.cesmac.medcheck.model.enums.IntensidadeSintoma;
import br.com.cesmac.medcheck.model.enums.NivelPrioridade;
import br.com.cesmac.medcheck.model.enums.StatusAtendimento;
import br.com.cesmac.medcheck.model.enums.StatusTriagem;
import br.com.cesmac.medcheck.model.valueobjects.CPF;
import br.com.cesmac.medcheck.model.valueobjects.SinaisVitais;
import br.com.cesmac.medcheck.repository.MedCheckDataStore;
import br.com.cesmac.medcheck.service.manchester.MotorClassificacaoManchester;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço de Negócio Clínico do MEDCHECK conforme [RF001, RF004, RF005, RF006, RF009, RF012, RF021].
 */
public class ClinicaService {

    private final MedCheckDataStore dataStore;
    private final MotorClassificacaoManchester motorManchester;

    public ClinicaService() {
        this.dataStore = MedCheckDataStore.getInstance();
        this.motorManchester = new MotorClassificacaoManchester();
    }

    public Paciente cadastrarPaciente(String nome, String cpfStr, String email, String senha,
                                     LocalDate dataNasc, String sexo, String telefone,
                                     Double peso, Double altura, String alergias, String doencasCronicas,
                                     boolean cardiopatia, boolean diabetes, String historicoFamiliar) {
        return cadastrarPaciente(nome, cpfStr, email, senha, dataNasc, sexo, telefone, peso, altura,
                alergias, doencasCronicas, cardiopatia, diabetes, historicoFamiliar, null, null);
    }

    public Paciente cadastrarPaciente(String nome, String cpfStr, String email, String senha,
                                     LocalDate dataNasc, String sexo, String telefone,
                                     Double peso, Double altura, String alergias, String doencasCronicas,
                                     boolean cardiopatia, boolean diabetes, String historicoFamiliar,
                                     String prioridadeManchester, String queixaInformada) {
        CPF cpf = new CPF(cpfStr);
        if (dataStore.buscarUsuarioPorCpf(cpf.getValorLimpo()) != null) {
            throw new IllegalArgumentException("CPF já cadastrado no sistema.");
        }
        if (dataStore.buscarUsuarioPorEmail(email) != null) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        Paciente paciente = new Paciente(null, nome, cpf, email, senha, dataNasc, sexo, telefone, peso, altura);
        HistoricoClinico hist = new HistoricoClinico(null, null);
        hist.setAlergiasPorString(alergias);
        hist.setPossuiDoencaCronica(doencasCronicas != null && !doencasCronicas.trim().isEmpty());
        hist.setDescricaoDoencasCronicas(doencasCronicas);
        hist.setPossuiCardiopatia(cardiopatia);
        hist.setPossuiDiabetes(diabetes);
        hist.setHistoricoFamiliar(historicoFamiliar);

        dataStore.salvarPaciente(paciente, hist);
        dataStore.registrarLog(new LogAuditoria(null, paciente.getId(), paciente.getId(), "PACIENTE", "INSERCAO", "Cadastro realizado", "127.0.0.1"));

        // Enfileira o paciente automaticamente na Fila Priorizada com a cor e prioridade selecionadas
        if (prioridadeManchester != null && !prioridadeManchester.trim().isEmpty()) {
            enfileirarPacienteComPrioridade(paciente.getId(), prioridadeManchester.trim().toUpperCase(), queixaInformada);
        }

        return paciente;
    }

    private void enfileirarPacienteComPrioridade(Integer pacienteId, String corStr, String queixaInformada) {
        CorManchester cor;
        NivelPrioridade prioridade;
        int tempoAlvo;
        String orientacao;

        switch (corStr) {
            case "VERMELHO":
                cor = CorManchester.VERMELHO;
                prioridade = NivelPrioridade.EMERGENCIA;
                tempoAlvo = 0;
                orientacao = "Emergência imediata — Encaminhamento crítico ou suporte avançado SAMU 192.";
                break;
            case "LARANJA":
                cor = CorManchester.LARANJA;
                prioridade = NivelPrioridade.MUITO_URGENTE;
                tempoAlvo = 10;
                orientacao = "Caso muito urgente — Atendimento clínico prioritário em até 10 minutos.";
                break;
            case "AMARELO":
                cor = CorManchester.AMARELO;
                prioridade = NivelPrioridade.URGENTE;
                tempoAlvo = 50;
                orientacao = "Caso urgente — Teleconsulta prioritária em até 50 minutos.";
                break;
            case "VERDE":
                cor = CorManchester.VERDE;
                prioridade = NivelPrioridade.POUCO_URGENTE;
                tempoAlvo = 120;
                orientacao = "Caso pouco urgente — Atendimento ambulatorial em até 120 minutos.";
                break;
            case "AZUL":
            default:
                cor = CorManchester.AZUL;
                prioridade = NivelPrioridade.NAO_URGENTE;
                tempoAlvo = 240;
                orientacao = "Caso não urgente — Acolhimento e orientação em até 240 minutos.";
                break;
        }

        String queixaFinal = (queixaInformada != null && !queixaInformada.trim().isEmpty())
                ? queixaInformada.trim()
                : "Acolhimento clínico de triagem (" + cor.getRotulo() + ")";

        int triagemId = dataStore.gerarProximoIdTriagem();
        Triagem triagem = new Triagem(triagemId, pacienteId, queixaFinal);
        triagem.setLocalSintoma("Geral / Acolhimento cadastral");
        triagem.setIntensidade(IntensidadeSintoma.MODERADA);
        triagem.setDuracaoInicio("Início recente");

        ClassificacaoRisco classificacao = new ClassificacaoRisco(
                dataStore.gerarProximoIdClassificacao(),
                triagemId,
                prioridade,
                cor,
                orientacao,
                "Teleatendimento regulado conforme Protocolo de Manchester.",
                "Piora súbita dos sintomas ou alteração do estado de consciência."
        );
        classificacao.adicionarDiscriminador("Classificação atribuída no cadastro: " + cor.getRotulo());
        dataStore.salvarClassificacao(classificacao);
        triagem.setClassificacaoRisco(classificacao);
        triagem.concluirTriagem("Classificado no cadastro como " + cor.getRotulo());
        dataStore.salvarTriagem(triagem);

        // Não permitir o registro do mesmo paciente na fila enquanto ele já estiver lá [RF021]
        boolean jaNaFila = dataStore.listarTodosAtendimentos().stream()
                .anyMatch(a -> Objects.equals(a.getPacienteId(), pacienteId) &&
                        (a.getStatus() == StatusAtendimento.AGUARDANDO || a.getStatus() == StatusAtendimento.EM_ATENDIMENTO));
        if (!jaNaFila) {
            AtendimentoMedico atendimento = new AtendimentoMedico(dataStore.gerarProximoIdAtendimento(), triagemId, pacienteId);
            dataStore.salvarAtendimento(atendimento);
            dataStore.registrarLog(new LogAuditoria(null, pacienteId, pacienteId, "ATENDIMENTO", "INSERCAO",
                    "Paciente enfileirado na Fila Priorizada com prioridade " + cor.name(), "127.0.0.1"));
        }
    }

    public Triagem realizarTriagemEClassificacao(Integer pacienteId, String queixa, String local,
                                                String intensidadeStr, String duracao, String frequencia,
                                                List<String> sintomasAssociados, SinaisVitais vitais) {
        Paciente paciente = dataStore.buscarPacientePorId(pacienteId);
        if (paciente == null) {
            throw new IllegalArgumentException("Paciente não localizado para triagem.");
        }

        IntensidadeSintoma intensidade = IntensidadeSintoma.MODERADA;
        if (intensidadeStr != null) {
            try {
                intensidade = IntensidadeSintoma.valueOf(intensidadeStr.toUpperCase());
            } catch (Exception ignored) {}
        }

        int triagemId = dataStore.gerarProximoIdTriagem();
        Triagem triagem = new Triagem(triagemId, pacienteId, queixa);
        triagem.setLocalSintoma(local);
        triagem.setIntensidade(intensidade);
        triagem.setDuracaoInicio(duracao);
        triagem.setFrequencia(frequencia != null ? frequencia.toUpperCase() : "CONTINUA");
        if (sintomasAssociados != null) {
            for (String s : sintomasAssociados) triagem.adicionarSintomaAssociado(s);
        }
        triagem.setSinaisVitais(vitais);

        HistoricoClinico hist = dataStore.buscarHistoricoPorPaciente(pacienteId);

        // Disparo do Motor Clínico de Manchester (Polimórfico - RF005)
        ClassificacaoRisco classificacao = motorManchester.processarClassificacao(triagem, hist);
        classificacao.setId(dataStore.gerarProximoIdClassificacao());
        dataStore.salvarClassificacao(classificacao);
        triagem.setClassificacaoRisco(classificacao);

        // Regra de Emergência Vermelha [RF006 / RS005]
        if (classificacao.isEmergenciaVermelha()) {
            triagem.interromperPorEmergencia("Alerta imediato de emergência SAMU 192 ativado.");
            dataStore.registrarLog(new LogAuditoria(null, pacienteId, pacienteId, "EMERGENCIA", "INSERCAO", "Gatilho de Emergência Vermelha Manchester acionado", "127.0.0.1"));
        } else {
            triagem.concluirTriagem("Triagem avaliada e classificada pelo Protocolo de Manchester como " + classificacao.getCorManchester().getRotulo());
            
            // Se for Laranja ou Amarelo, enfileira automaticamente para teleatendimento médico [RF021 / RF007]
            if (classificacao.isPrioridadeTeleconsulta()) {
                // Não permite o registro do mesmo paciente na fila enquanto ele já estiver lá [RF021]
                boolean jaNaFila = dataStore.listarTodosAtendimentos().stream()
                        .anyMatch(a -> Objects.equals(a.getPacienteId(), pacienteId) &&
                                (a.getStatus() == StatusAtendimento.AGUARDANDO || a.getStatus() == StatusAtendimento.EM_ATENDIMENTO));

                if (!jaNaFila) {
                    AtendimentoMedico atendimento = new AtendimentoMedico(dataStore.gerarProximoIdAtendimento(), triagem.getId(), pacienteId);
                    dataStore.salvarAtendimento(atendimento);
                    dataStore.registrarLog(new LogAuditoria(null, pacienteId, pacienteId, "ATENDIMENTO", "INSERCAO",
                            "Paciente enfileirado na Fila Priorizada com prioridade " + classificacao.getCorManchester().name(), "127.0.0.1"));
                }
            }
        }

        dataStore.salvarTriagem(triagem);
        dataStore.registrarLog(new LogAuditoria(null, pacienteId, pacienteId, "TRIAGEM", "INSERCAO", "Triagem #" + triagem.getId() + " classificada: " + classificacao.getCorManchester().name(), "127.0.0.1"));
        return triagem;
    }

    /**
     * Fila de Espera Inteligente ordenada por prioridade do Protocolo de Manchester [RF021].
     * Casos LARANJA têm precedência estrita sobre AMARELO.
     * Não permite o mesmo paciente duas vezes na fila ativa.
     */
    public List<Map<String, Object>> obterFilaPriorizadaTeleatendimento() {
        List<Map<String, Object>> filaOrdenada = dataStore.listarTodosAtendimentos().stream()
                .filter(a -> a.getStatus() == StatusAtendimento.AGUARDANDO || a.getStatus() == StatusAtendimento.EM_ATENDIMENTO)
                .map(a -> {
                    Triagem t = dataStore.buscarTriagemPorId(a.getTriagemId());
                    Paciente p = dataStore.buscarPacientePorId(a.getPacienteId());
                    ClassificacaoRisco c = (t != null) ? t.getClassificacaoRisco() : null;
                    if (c == null && t != null) {
                        c = dataStore.buscarClassificacaoPorTriagem(t.getId());
                    }

                    Map<String, Object> item = new HashMap<>();
                    item.put("atendimentoId", a.getId());
                    item.put("triagemId", a.getTriagemId());
                    item.put("pacienteId", a.getPacienteId());
                    item.put("pacienteNome", p != null ? p.getNomeCompleto() : "Paciente");
                    item.put("pacienteIdade", p != null ? p.calcularIdade() : 0);
                    item.put("status", a.getStatus().name());
                    item.put("queixa", t != null ? t.getQueixaPrincipal() : "-");
                    item.put("corManchester", c != null ? c.getCorManchester().name() : "AZUL");
                    item.put("prioridadeRotulo", c != null ? c.getCorManchester().getRotulo() : "Azul");
                    item.put("prioridadeOrdem", c != null ? c.getNivelPrioridade().getOrdemPrecedencia() : 99);
                    item.put("tempoAlvoMinutos", c != null ? c.getTempoAlvoMinutos() : 240);
                    item.put("dataHoraInicio", a.getDataHoraInicio() != null ? a.getDataHoraInicio().toString() : "");
                    return item;
                })
                .sorted(Comparator.<Map<String, Object>>comparingInt(m -> (int) m.get("prioridadeOrdem"))
                        .thenComparingInt(m -> (int) m.get("atendimentoId")))
                .collect(Collectors.toList());

        // Deduplicação estrita: não permite o mesmo paciente duas vezes na fila
        Set<Integer> pacientesVistos = new HashSet<>();
        List<Map<String, Object>> filaSemDuplicatas = new ArrayList<>();
        for (Map<String, Object> item : filaOrdenada) {
            Integer pacId = (Integer) item.get("pacienteId");
            if (pacId != null && pacientesVistos.add(pacId)) {
                filaSemDuplicatas.add(item);
            }
        }
        return filaSemDuplicatas;
    }

    /**
     * Conclui o atendimento médico, removendo-o da fila ativa [RF021].
     */
    public boolean concluirAtendimento(Integer atendimentoId, String avaliacao, String conduta) {
        if (atendimentoId == null) return false;
        AtendimentoMedico a = dataStore.buscarAtendimentoPorId(atendimentoId);
        if (a != null) {
            a.finalizarAtendimento(
                avaliacao != null ? avaliacao : "Consulta médica concluída e registrada",
                conduta != null ? conduta : "Orientações e conduta prestadas"
            );
            dataStore.salvarAtendimento(a);

            // Finaliza também quaisquer outros atendimentos em aberto do mesmo paciente para evitar reaparecimento
            Integer pacId = a.getPacienteId();
            if (pacId != null) {
                for (AtendimentoMedico outro : dataStore.listarTodosAtendimentos()) {
                    if (Objects.equals(outro.getPacienteId(), pacId) &&
                        (outro.getStatus() == StatusAtendimento.AGUARDANDO || outro.getStatus() == StatusAtendimento.EM_ATENDIMENTO)) {
                        outro.finalizarAtendimento(
                            avaliacao != null ? avaliacao : "Consulta médica concluída e registrada",
                            conduta != null ? conduta : "Orientações e conduta prestadas"
                        );
                        dataStore.salvarAtendimento(outro);
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Histórico Longitudinal do Paciente [RF009].
     */
    public Map<String, Object> obterHistoricoLongitudinal(Integer pacienteId) {
        Paciente p = dataStore.buscarPacientePorId(pacienteId);
        HistoricoClinico h = dataStore.buscarHistoricoPorPaciente(pacienteId);
        List<Triagem> triagensPaciente = dataStore.listarTodasTriagens().stream()
                .filter(t -> Objects.equals(t.getPacienteId(), pacienteId))
                .sorted(Comparator.comparing(Triagem::getDataHoraInicio).reversed())
                .collect(Collectors.toList());

        List<ReceitaMedica> receitasPaciente = dataStore.listarTodasReceitas().stream()
                .filter(r -> Objects.equals(r.getPacienteId(), pacienteId))
                .sorted(Comparator.comparing(ReceitaMedica::getDataEmissao).reversed())
                .collect(Collectors.toList());

        Map<String, Object> map = new HashMap<>();
        map.put("paciente", p);
        map.put("historicoClinico", h);
        map.put("triagens", triagensPaciente);
        map.put("receitas", receitasPaciente);
        return map;
    }

    /**
     * Métricas Consolidadas do Dashboard Clínico [RF012].
     */
    public Map<String, Object> obterMetricasDashboard() {
        Collection<Triagem> todas = dataStore.listarTodasTriagens();
        long totalTriagens = todas.size();
        long totalPacientes = dataStore.listarTodosPacientes().size();
        long totalReceitas = dataStore.listarTodasReceitas().size();
        long totalAtendimentos = dataStore.listarTodosAtendimentos().size();

        Map<String, Long> contagemPorCor = new HashMap<>();
        for (CorManchester cor : CorManchester.values()) {
            contagemPorCor.put(cor.name(), 0L);
        }

        // Distribuição calculada em tempo real com base na fila ativa de atendimento
        for (Map<String, Object> item : obterFilaPriorizadaTeleatendimento()) {
            String cor = (String) item.get("corManchester");
            if (cor != null) {
                contagemPorCor.put(cor, contagemPorCor.getOrDefault(cor, 0L) + 1);
            }
        }

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalTriagens", totalTriagens);
        metrics.put("totalPacientes", totalPacientes);
        metrics.put("totalReceitas", totalReceitas);
        metrics.put("totalAtendimentos", totalAtendimentos);
        metrics.put("distribuicaoManchester", contagemPorCor);
        return metrics;
    }
}
