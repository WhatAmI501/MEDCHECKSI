package br.com.cesmac.medcheck.model.enums;

public enum PerfilAcesso {
    PACIENTE("Paciente", 1),
    MEDICO("Médico", 2),
    ENFERMEIRO("Enfermeiro", 3),
    ADMINISTRADOR("Administrador", 4);

    private final String descricao;
    private final int nivelPermissao;

    PerfilAcesso(String descricao, int nivelPermissao) {
        this.descricao = descricao;
        this.nivelPermissao = nivelPermissao;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getNivelPermissao() {
        return nivelPermissao;
    }
}
