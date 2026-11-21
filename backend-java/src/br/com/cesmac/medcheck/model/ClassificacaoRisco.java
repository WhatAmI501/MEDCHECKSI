package br.com.cesmac.medcheck.model;

import br.com.cesmac.medcheck.model.enums.CorManchester;
import br.com.cesmac.medcheck.model.enums.NivelPrioridade;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade Classificação de Risco pelo Protocolo de Manchester [RF005 / RF006].
 */
public class ClassificacaoRisco implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer triagemId;
    private NivelPrioridade nivelPrioridade;
    private CorManchester corManchester;
    private int tempoAlvoMinutos;
    private List<String> discriminadoresIdentificados;
    private String orientacaoPaciente;
    private String encaminhamentoRecomendado;
    private String sinaisAlerta;
    private LocalDateTime dataHoraClassificacao;

    public ClassificacaoRisco(Integer id, Integer triagemId, NivelPrioridade nivelPrioridade,
                              CorManchester corManchester, String orientacaoPaciente,
                              String encaminhamentoRecomendado, String sinaisAlerta) {
        this.id = id;
        this.triagemId = triagemId;
        this.nivelPrioridade = nivelPrioridade;
        this.corManchester = corManchester;
        this.tempoAlvoMinutos = corManchester.getTempoAlvoMinutos();
        this.orientacaoPaciente = orientacaoPaciente;
        this.encaminhamentoRecomendado = encaminhamentoRecomendado;
        this.sinaisAlerta = sinaisAlerta;
        this.discriminadoresIdentificados = new ArrayList<>();
        this.dataHoraClassificacao = LocalDateTime.now();
    }

    public boolean isEmergenciaVermelha() {
        return this.corManchester == CorManchester.VERMELHO;
    }

    public boolean isPrioridadeTeleconsulta() {
        return this.corManchester == CorManchester.LARANJA || this.corManchester == CorManchester.AMARELO;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTriagemId() {
        return triagemId;
    }

    public NivelPrioridade getNivelPrioridade() {
        return nivelPrioridade;
    }

    public CorManchester getCorManchester() {
        return corManchester;
    }

    public int getTempoAlvoMinutos() {
        return tempoAlvoMinutos;
    }

    public List<String> getDiscriminadoresIdentificados() {
        return discriminadoresIdentificados;
    }

    public void adicionarDiscriminador(String discriminador) {
        if (discriminador != null && !discriminador.trim().isEmpty()) {
            this.discriminadoresIdentificados.add(discriminador.trim());
        }
    }

    public String getOrientacaoPaciente() {
        return orientacaoPaciente;
    }

    public String getEncaminhamentoRecomendado() {
        return encaminhamentoRecomendado;
    }

    public String getSinaisAlerta() {
        return sinaisAlerta;
    }

    public LocalDateTime getDataHoraClassificacao() {
        return dataHoraClassificacao;
    }
}
