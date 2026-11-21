package br.com.cesmac.medcheck.model;

import br.com.cesmac.medcheck.model.enums.IntensidadeSintoma;
import br.com.cesmac.medcheck.model.enums.StatusTriagem;
import br.com.cesmac.medcheck.model.valueobjects.SinaisVitais;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade Triagem Conversacional de Saúde conforme [RF004] e [RF019].
 */
public class Triagem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer pacienteId;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private String queixaPrincipal;
    private String localSintoma;
    private IntensidadeSintoma intensidade;
    private String duracaoInicio;
    private String frequencia;
    private List<String> sintomasAssociados;
    private SinaisVitais sinaisVitais;
    private String resumoEstruturado;
    private StatusTriagem status;
    private ClassificacaoRisco classificacaoRisco;

    public Triagem(Integer id, Integer pacienteId, String queixaPrincipal) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.queixaPrincipal = queixaPrincipal != null ? queixaPrincipal.trim() : "Queixa não informada";
        this.dataHoraInicio = LocalDateTime.now();
        this.sintomasAssociados = new ArrayList<>();
        this.sinaisVitais = SinaisVitais.vazio();
        this.status = StatusTriagem.EM_ANDAMENTO;
    }

    public void concluirTriagem(String resumoEstruturado) {
        this.resumoEstruturado = resumoEstruturado;
        this.dataHoraFim = LocalDateTime.now();
        this.status = StatusTriagem.CONCLUIDA;
    }

    public void interromperPorEmergencia(String motivo) {
        this.resumoEstruturado = "[INTERRUPÇÃO IMEDIATA POR EMERGÊNCIA] " + motivo;
        this.dataHoraFim = LocalDateTime.now();
        this.status = StatusTriagem.INTERROMPIDA_EMERGENCIA;
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

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }

    public String getQueixaPrincipal() {
        return queixaPrincipal;
    }

    public void setQueixaPrincipal(String queixaPrincipal) {
        this.queixaPrincipal = queixaPrincipal;
    }

    public String getLocalSintoma() {
        return localSintoma;
    }

    public void setLocalSintoma(String localSintoma) {
        this.localSintoma = localSintoma;
    }

    public IntensidadeSintoma getIntensidade() {
        return intensidade;
    }

    public void setIntensidade(IntensidadeSintoma intensidade) {
        this.intensidade = intensidade;
    }

    public String getDuracaoInicio() {
        return duracaoInicio;
    }

    public void setDuracaoInicio(String duracaoInicio) {
        this.duracaoInicio = duracaoInicio;
    }

    public String getFrequencia() {
        return frequencia;
    }

    public void setFrequencia(String frequencia) {
        this.frequencia = frequencia;
    }

    public List<String> getSintomasAssociados() {
        return sintomasAssociados;
    }

    public void adicionarSintomaAssociado(String sintoma) {
        if (sintoma != null && !sintoma.trim().isEmpty()) {
            this.sintomasAssociados.add(sintoma.trim());
        }
    }

    public SinaisVitais getSinaisVitais() {
        return sinaisVitais;
    }

    public void setSinaisVitais(SinaisVitais sinaisVitais) {
        this.sinaisVitais = sinaisVitais != null ? sinaisVitais : SinaisVitais.vazio();
    }

    public String getResumoEstruturado() {
        return resumoEstruturado;
    }

    public void setResumoEstruturado(String resumoEstruturado) {
        this.resumoEstruturado = resumoEstruturado;
    }

    public StatusTriagem getStatus() {
        return status;
    }

    public void setStatus(StatusTriagem status) {
        this.status = status;
    }

    public ClassificacaoRisco getClassificacaoRisco() {
        return classificacaoRisco;
    }

    public void setClassificacaoRisco(ClassificacaoRisco classificacaoRisco) {
        this.classificacaoRisco = classificacaoRisco;
    }
}
