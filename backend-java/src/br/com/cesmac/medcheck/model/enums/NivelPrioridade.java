package br.com.cesmac.medcheck.model.enums;

public enum NivelPrioridade {
    EMERGENCIA(1, "Emergência"),
    MUITO_URGENTE(2, "Muito Urgente"),
    URGENTE(3, "Urgente"),
    POUCO_URGENTE(4, "Pouco Urgente"),
    NAO_URGENTE(5, "Não Urgente");

    private final int ordemPrecedencia;
    private final String rotulo;

    NivelPrioridade(int ordemPrecedencia, String rotulo) {
        this.ordemPrecedencia = ordemPrecedencia;
        this.rotulo = rotulo;
    }

    public int getOrdemPrecedencia() {
        return ordemPrecedencia;
    }

    public String getRotulo() {
        return rotulo;
    }
}
