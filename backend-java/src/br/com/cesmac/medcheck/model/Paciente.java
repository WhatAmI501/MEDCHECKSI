package br.com.cesmac.medcheck.model;

import br.com.cesmac.medcheck.model.enums.PerfilAcesso;
import br.com.cesmac.medcheck.model.valueobjects.CPF;

import java.time.LocalDate;
import java.time.Period;

/**
 * PILAR POO: HERANÇA E ENCAPSULAMENTO
 * Entidade Paciente conforme Requisito [RF001].
 */
public class Paciente extends Usuario {
    private static final long serialVersionUID = 1L;

    private LocalDate dataNascimento;
    private String sexo;
    private String telefone;
    private Double peso;   // kg
    private Double altura; // metros
    private HistoricoClinico historicoClinico;

    public Paciente(Integer id, String nomeCompleto, CPF cpf, String email, String senhaHash,
                    LocalDate dataNascimento, String sexo, String telefone, Double peso, Double altura) {
        super(id, nomeCompleto, cpf, email, senhaHash, PerfilAcesso.PACIENTE);
        this.dataNascimento = dataNascimento;
        this.sexo = sexo != null ? sexo : "Outro";
        this.telefone = telefone;
        this.peso = peso;
        this.altura = altura;
    }

    @Override
    public String obterDescricaoPerfil() {
        return "Paciente (Idade: " + calcularIdade() + " anos)";
    }

    public int calcularIdade() {
        if (dataNascimento == null) return 0;
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    public Double calcularIMC() {
        if (peso == null || altura == null || altura <= 0) return null;
        return peso / (altura * altura);
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public Double getAltura() {
        return altura;
    }

    public void setAltura(Double altura) {
        this.altura = altura;
    }

    public HistoricoClinico getHistoricoClinico() {
        return historicoClinico;
    }

    public void setHistoricoClinico(HistoricoClinico historicoClinico) {
        this.historicoClinico = historicoClinico;
    }
}
