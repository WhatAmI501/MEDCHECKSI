package br.com.cesmac.medcheck.model;

import br.com.cesmac.medcheck.model.enums.StatusAtendimento;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidade Atendimento Médico Remoto / Teleconsulta [RF007 / RF021].
 */
public class AtendimentoMedico implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer triagemId;
    private Integer pacienteId;
    private Integer medicoId;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private String avaliacaoClinica;
    private String condutaEncaminhamento;
    private StatusAtendimento status;

    public AtendimentoMedico(Integer id, Integer triagemId, Integer pacienteId) {
        this.id = id;
        this.triagemId = triagemId;
        this.pacienteId = pacienteId;
        this.status = StatusAtendimento.AGUARDANDO;
    }

    public void assumirAtendimento(Integer medicoId) {
        this.medicoId = medicoId;
        this.dataHoraInicio = LocalDateTime.now();
        this.status = StatusAtendimento.EM_ATENDIMENTO;
    }

    public void finalizarAtendimento(String avaliacaoClinica, String condutaEncaminhamento) {
        this.avaliacaoClinica = avaliacaoClinica;
        this.condutaEncaminhamento = condutaEncaminhamento;
        this.dataHoraFim = LocalDateTime.now();
        this.status = StatusAtendimento.FINALIZADO;
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

    public Integer getPacienteId() {
        return pacienteId;
    }

    public Integer getMedicoId() {
        return medicoId;
    }

    public void setMedicoId(Integer medicoId) {
        this.medicoId = medicoId;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }

    public String getAvaliacaoClinica() {
        return avaliacaoClinica;
    }

    public void setAvaliacaoClinica(String avaliacaoClinica) {
        this.avaliacaoClinica = avaliacaoClinica;
    }

    public String getCondutaEncaminhamento() {
        return condutaEncaminhamento;
    }

    public void setCondutaEncaminhamento(String condutaEncaminhamento) {
        this.condutaEncaminhamento = condutaEncaminhamento;
    }

    public StatusAtendimento getStatus() {
        return status;
    }

    public void setStatus(StatusAtendimento status) {
        this.status = status;
    }
}
