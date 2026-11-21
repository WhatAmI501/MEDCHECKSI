package br.com.cesmac.medcheck.model.enums;

public enum StatusReceita {
    ATIVA("Ativa"),
    DISPENSADA("Dispensada"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusReceita(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
