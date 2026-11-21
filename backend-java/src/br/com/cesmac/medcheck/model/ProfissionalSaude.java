package br.com.cesmac.medcheck.model;

import br.com.cesmac.medcheck.model.enums.PerfilAcesso;
import br.com.cesmac.medcheck.model.valueobjects.CPF;

/**
 * PILAR POO: HERANÇA E ABSTRAÇÃO
 * Subclasse abstrata que estende Usuario, representando profissionais da área de saúde.
 */
public abstract class ProfissionalSaude extends Usuario {
    private static final long serialVersionUID = 1L;

    private String registroConselho; // CRM ou COREN
    private String especialidade;

    public ProfissionalSaude(Integer id, String nomeCompleto, CPF cpf, String email, String senhaHash,
                             PerfilAcesso perfil, String registroConselho, String especialidade) {
        super(id, nomeCompleto, cpf, email, senhaHash, perfil);
        if (registroConselho == null || registroConselho.trim().isEmpty()) {
            throw new IllegalArgumentException("Registro de conselho de classe é obrigatório para profissionais de saúde.");
        }
        this.registroConselho = registroConselho.trim();
        this.especialidade = especialidade != null ? especialidade.trim() : "Geral";
    }

    public String getRegistroConselho() {
        return registroConselho;
    }

    public void setRegistroConselho(String registroConselho) {
        this.registroConselho = registroConselho;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }
}
