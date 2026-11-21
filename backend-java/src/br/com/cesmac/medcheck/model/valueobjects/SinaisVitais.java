package br.com.cesmac.medcheck.model.valueobjects;

import java.io.Serializable;

/**
 * Value Object para Sinais Vitais Autoinformados conforme Requisito [RF019].
 * Encapsula validação clínica e métodos semânticos para identificação de gravidade.
 */
public final class SinaisVitais implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Double temperatura;          // °C
    private final Integer pressaoSistolica;     // mmHg
    private final Integer pressaoDiastolica;    // mmHg
    private final Integer frequenciaCardiaca;   // bpm
    private final Integer saturacaoO2;          // %
    private final Integer glicemia;             // mg/dL

    public SinaisVitais(Double temperatura, Integer pressaoSistolica, Integer pressaoDiastolica,
                        Integer frequenciaCardiaca, Integer saturacaoO2, Integer glicemia) {
        this.temperatura = temperatura;
        this.pressaoSistolica = pressaoSistolica;
        this.pressaoDiastolica = pressaoDiastolica;
        this.frequenciaCardiaca = frequenciaCardiaca;
        this.saturacaoO2 = saturacaoO2;
        this.glicemia = glicemia;
    }

    public static SinaisVitais vazio() {
        return new SinaisVitais(null, null, null, null, null, null);
    }

    // Regras Clínicas Semânticas de Gravidade
    public boolean isDessaturandoCritico() {
        return saturacaoO2 != null && saturacaoO2 < 90;
    }

    public boolean isDessaturandoModerado() {
        return saturacaoO2 != null && saturacaoO2 >= 90 && saturacaoO2 <= 94;
    }

    public boolean isFebreAlta() {
        return temperatura != null && temperatura >= 39.5;
    }

    public boolean isFebreModerada() {
        return temperatura != null && temperatura >= 38.0 && temperatura < 39.5;
    }

    public boolean isHipotermia() {
        return temperatura != null && temperatura < 35.0;
    }

    public boolean isCriseHipertensiva() {
        return (pressaoSistolica != null && pressaoSistolica >= 180) ||
               (pressaoDiastolica != null && pressaoDiastolica >= 110);
    }

    public boolean isTaquicardiaSevera() {
        return frequenciaCardiaca != null && frequenciaCardiaca > 130;
    }

    public boolean isBradicardiaSevera() {
        return frequenciaCardiaca != null && frequenciaCardiaca < 45;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public Integer getPressaoSistolica() {
        return pressaoSistolica;
    }

    public Integer getPressaoDiastolica() {
        return pressaoDiastolica;
    }

    public Integer getFrequenciaCardiaca() {
        return frequenciaCardiaca;
    }

    public Integer getSaturacaoO2() {
        return saturacaoO2;
    }

    public Integer getGlicemia() {
        return glicemia;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (temperatura != null) sb.append("Temp: ").append(temperatura).append("°C; ");
        if (pressaoSistolica != null && pressaoDiastolica != null) {
            sb.append("PA: ").append(pressaoSistolica).append("/").append(pressaoDiastolica).append(" mmHg; ");
        }
        if (frequenciaCardiaca != null) sb.append("FC: ").append(frequenciaCardiaca).append(" bpm; ");
        if (saturacaoO2 != null) sb.append("SpO2: ").append(saturacaoO2).append("%; ");
        if (glicemia != null) sb.append("Glicemia: ").append(glicemia).append(" mg/dL;");
        return sb.length() > 0 ? sb.toString() : "Sinais vitais não informados";
    }
}
