package br.com.cesmac.medcheck.service;

import br.com.cesmac.medcheck.model.*;
import br.com.cesmac.medcheck.model.enums.CorManchester;
import br.com.cesmac.medcheck.model.enums.IntensidadeSintoma;
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
        return paciente;
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
                AtendimentoMedico atendimento = new AtendimentoMedico(dataStore.gerarProximoIdAtendimento(), triagem.getId(), pacienteId);
                dataStore.salvarAtendimento(atendimento);
            }
        }

        dataStore.salvarTriagem(triagem);
        dataStore.registrarLog(new LogAuditoria(null, pacienteId, pacienteId, "TRIAGEM", "INSERCAO", "Triagem #" + triagem.getId() + " classificada: " + classificacao.getCorManchester().name(), "127.0.0.1"));
        return triagem;
    }

    /**
     * Fila de Espera Inteligente ordenada por prioridade do Protocolo de Manchester [RF021].
     * Casos LARANJA têm precedência estrita sobre AMARELO.
     */
    public List<Map<String, Object>> obterFilaPriorizadaTeleatendimento() {
        return dataStore.listarTodosAtendimentos().stream()
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
                .sorted(Comparator.comparingInt(m -> (int) m.get("prioridadeOrdem")))
                .collect(Collectors.toList());
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

        for (Triagem t : todas) {
            ClassificacaoRisco c = t.getClassificacaoRisco();
            if (c == null) c = dataStore.buscarClassificacaoPorTriagem(t.getId());
            if (c != null) {
                String cor = c.getCorManchester().name();
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
