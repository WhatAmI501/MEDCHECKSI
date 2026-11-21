package br.com.cesmac.medcheck.model.enums;

public enum StatusAtendimento {
    AGUARDANDO("Aguardando Atendimento"),
    EM_ATENDIMENTO("Em Atendimento"),
    FINALIZADO("Finalizado");

    private final String descricao;

    StatusAtendimento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
