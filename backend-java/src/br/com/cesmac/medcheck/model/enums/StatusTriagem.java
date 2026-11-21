package br.com.cesmac.medcheck.model.enums;

public enum StatusTriagem {
    EM_ANDAMENTO("Em Andamento"),
    CONCLUIDA("Concluída"),
    INTERROMPIDA_EMERGENCIA("Interrompida por Emergência");

    private final String descricao;

    StatusTriagem(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
