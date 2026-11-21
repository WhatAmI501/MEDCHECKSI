package br.com.cesmac.medcheck.model;

import br.com.cesmac.medcheck.model.enums.PerfilAcesso;
import br.com.cesmac.medcheck.model.valueobjects.CPF;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * PILAR POO: ABSTRAÇÃO E ENCAPSULAMENTO
 * Classe abstrata base para todos os usuários do sistema [RF003 / RF014].
 */
public abstract class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String nomeCompleto;
    private CPF cpf;
    private String email;
    private String senhaHash;
    private PerfilAcesso perfil;
    private boolean ativo;
    private LocalDateTime dataCadastro;

    public Usuario(Integer id, String nomeCompleto, CPF cpf, String email, String senhaHash, PerfilAcesso perfil) {
        if (nomeCompleto == null || nomeCompleto.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome completo é obrigatório.");
        }
        if (cpf == null) {
            throw new IllegalArgumentException("CPF é obrigatório.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
        this.id = id;
        this.nomeCompleto = nomeCompleto.trim();
        this.cpf = cpf;
        this.email = email.trim().toLowerCase();
        this.senhaHash = senhaHash;
        this.perfil = perfil;
        this.ativo = true;
        this.dataCadastro = LocalDateTime.now();
    }

    /**
     * PILAR POO: POLIMORFISMO / ABSTRAÇÃO
     * Método abstrato implementado de forma específica por cada subclasse.
     */
    public abstract String obterDescricaoPerfil();

    public boolean autenticar(String senhaCandidata) {
        if (senhaCandidata == null || !ativo) return false;
        // Validação da senha (em produção utiliza BCrypt)
        return this.senhaHash != null && this.senhaHash.equals(senhaCandidata);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public CPF getCpf() {
        return cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public PerfilAcesso getPerfil() {
        return perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
}
