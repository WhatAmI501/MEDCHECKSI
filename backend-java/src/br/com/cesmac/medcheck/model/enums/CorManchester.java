package br.com.cesmac.medcheck.model.enums;

public enum CorManchester {
    VERMELHO("Vermelho", "Emergência imediata", 0, "#E53E3E"),
    LARANJA("Laranja", "Muito urgente", 10, "#ED8936"),
    AMARELO("Amarelo", "Urgente", 50, "#ECC94B"),
    VERDE("Verde", "Pouco urgente", 120, "#38A169"),
    AZUL("Azul", "Não urgente", 240, "#3182CE");

    private final String rotulo;
    private final String descricao;
    private final int tempoAlvoMinutos;
    private final String hexCor;

    CorManchester(String rotulo, String descricao, int tempoAlvoMinutos, String hexCor) {
        this.rotulo = rotulo;
        this.descricao = descricao;
        this.tempoAlvoMinutos = tempoAlvoMinutos;
        this.hexCor = hexCor;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getTempoAlvoMinutos() {
        return tempoAlvoMinutos;
    }

    public String getHexCor() {
        return hexCor;
    }
}
