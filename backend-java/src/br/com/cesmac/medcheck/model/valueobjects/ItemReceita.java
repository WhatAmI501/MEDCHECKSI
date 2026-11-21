package br.com.cesmac.medcheck.model.valueobjects;

import java.io.Serializable;

/**
 * Value Object para Itens de Prescrição Médica conforme [RF008].
 */
public final class ItemReceita implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String medicamento;
    private final String principioAtivo;
    private final String dosagem;
    private final String posologia;
    private final String quantidade;
    private final String viaAdministracao;

    public ItemReceita(String medicamento, String principioAtivo, String dosagem,
                       String posologia, String quantidade, String viaAdministracao) {
        if (medicamento == null || medicamento.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do medicamento é obrigatório.");
        }
        if (principioAtivo == null || principioAtivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Princípio ativo é obrigatório para conferência de alergia.");
        }
        this.medicamento = medicamento.trim();
        this.principioAtivo = principioAtivo.trim();
        this.dosagem = dosagem != null ? dosagem.trim() : "";
        this.posologia = posologia != null ? posologia.trim() : "";
        this.quantidade = quantidade != null ? quantidade.trim() : "";
        this.viaAdministracao = viaAdministracao != null ? viaAdministracao.trim() : "Oral";
    }

    public String getMedicamento() {
        return medicamento;
    }

    public String getPrincipioAtivo() {
        return principioAtivo;
    }

    public String getDosagem() {
        return dosagem;
    }

    public String getPosologia() {
        return posologia;
    }

    public String getQuantidade() {
        return quantidade;
    }

    public String getViaAdministracao() {
        return viaAdministracao;
    }
}
