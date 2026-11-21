package br.com.cesmac.medcheck.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Entidade Histórico Clínico do Paciente conforme [RF002] e [RF030].
 */
public class HistoricoClinico implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer pacienteId;
    private boolean possuiDoencaCronica;
    private String descricaoDoencasCronicas;
    private boolean possuiCardiopatia;
    private boolean possuiDiabetes;
    private String historicoFamiliar;
    private List<String> alergiasConhecidas;
    private String medicamentosEmUso;
    private LocalDateTime dataRegistro;

    public HistoricoClinico(Integer id, Integer pacienteId) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.alergiasConhecidas = new ArrayList<>();
        this.dataRegistro = LocalDateTime.now();
    }

    public boolean possuiAlergiaA(String substancia) {
        if (substancia == null || alergiasConhecidas == null) return false;
        String substanciaLower = substancia.trim().toLowerCase();
        for (String alergia : alergiasConhecidas) {
            if (substanciaLower.contains(alergia.toLowerCase()) || alergia.toLowerCase().contains(substanciaLower)) {
                return true;
            }
        }
        return false;
    }

    public void adicionarAlergia(String alergia) {
        if (alergia != null && !alergia.trim().isEmpty()) {
            this.alergiasConhecidas.add(alergia.trim());
        }
    }

    public void setAlergiasPorString(String alergiasTexto) {
        this.alergiasConhecidas.clear();
        if (alergiasTexto != null && !alergiasTexto.trim().isEmpty()) {
            String[] tokens = alergiasTexto.split("[,;]");
            for (String t : tokens) {
                if (!t.trim().isEmpty()) {
                    this.alergiasConhecidas.add(t.trim());
                }
            }
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPacienteId() {
        return pacienteId;
    }

    public boolean isPossuiDoencaCronica() {
        return possuiDoencaCronica;
    }

    public void setPossuiDoencaCronica(boolean possuiDoencaCronica) {
        this.possuiDoencaCronica = possuiDoencaCronica;
    }

    public String getDescricaoDoencasCronicas() {
        return descricaoDoencasCronicas;
    }

    public void setDescricaoDoencasCronicas(String descricaoDoencasCronicas) {
        this.descricaoDoencasCronicas = descricaoDoencasCronicas;
    }

    public boolean isPossuiCardiopatia() {
        return possuiCardiopatia;
    }

    public void setPossuiCardiopatia(boolean possuiCardiopatia) {
        this.possuiCardiopatia = possuiCardiopatia;
    }

    public boolean isPossuiDiabetes() {
        return possuiDiabetes;
    }

    public void setPossuiDiabetes(boolean possuiDiabetes) {
        this.possuiDiabetes = possuiDiabetes;
    }

    public String getHistoricoFamiliar() {
        return historicoFamiliar;
    }

    public void setHistoricoFamiliar(String historicoFamiliar) {
        this.historicoFamiliar = historicoFamiliar;
    }

    public List<String> getAlergiasConhecidas() {
        return alergiasConhecidas;
    }

    public String getMedicamentosEmUso() {
        return medicamentosEmUso;
    }

    public void setMedicamentosEmUso(String medicamentosEmUso) {
        this.medicamentosEmUso = medicamentosEmUso;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }
}
