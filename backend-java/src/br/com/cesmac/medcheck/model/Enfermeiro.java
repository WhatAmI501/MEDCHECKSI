package br.com.cesmac.medcheck.model;

import br.com.cesmac.medcheck.model.enums.PerfilAcesso;
import br.com.cesmac.medcheck.model.valueobjects.CPF;

/**
 * PILAR POO: HERANÇA E POLIMORFISMO
 * Representa o usuário Enfermeiro habilitado para triagens, acolhimento e monitoramento [RF010].
 */
public class Enfermeiro extends ProfissionalSaude {
    private static final long serialVersionUID = 1L;

    public Enfermeiro(Integer id, String nomeCompleto, CPF cpf, String email, String senhaHash,
                      String coren, String especialidade) {
        super(id, nomeCompleto, cpf, email, senhaHash, PerfilAcesso.ENFERMEIRO, coren, especialidade);
    }

    @Override
    public String obterDescricaoPerfil() {
        return "Enfermeiro(a) Triagista - " + getRegistroConselho() + " (" + getEspecialidade() + ")";
    }
}
