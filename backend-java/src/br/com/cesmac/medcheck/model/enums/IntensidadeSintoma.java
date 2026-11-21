package br.com.cesmac.medcheck.model.enums;

public enum IntensidadeSintoma {
    LEVE("Leve", 1),
    MODERADA("Moderada", 2),
    INTENSA("Intensa / Severa", 3);

    private final String rotulo;
    private final int pesoGravidade;

    IntensidadeSintoma(String rotulo, int pesoGravidade) {
        this.rotulo = rotulo;
        this.pesoGravidade = pesoGravidade;
    }

    public String getRotulo() {
        return rotulo;
    }

    public int getPesoGravidade() {
        return pesoGravidade;
    }
}
