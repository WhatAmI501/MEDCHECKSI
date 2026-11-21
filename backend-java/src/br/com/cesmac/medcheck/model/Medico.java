package br.com.cesmac.medcheck.model;

import br.com.cesmac.medcheck.model.enums.PerfilAcesso;
import br.com.cesmac.medcheck.model.valueobjects.CPF;

/**
 * PILAR POO: HERANÇA E POLIMORFISMO
 * Representa o usuário Médico habilitado para prescrições [RF008] e atendimentos [RF007].
 */
public class Medico extends ProfissionalSaude {
    private static final long serialVersionUID = 1L;

    public Medico(Integer id, String nomeCompleto, CPF cpf, String email, String senhaHash,
                  String crm, String especialidade) {
        super(id, nomeCompleto, cpf, email, senhaHash, PerfilAcesso.MEDICO, crm, especialidade);
    }

    @Override
    public String obterDescricaoPerfil() {
        return "Médico(a) Clínico - " + getRegistroConselho() + " (" + getEspecialidade() + ")";
    }

    public boolean podePrescreverReceitas() {
        return isAtivo() && getRegistroConselho() != null && !getRegistroConselho().isEmpty();
    }
}
