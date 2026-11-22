package br.com.cesmac.medcheck.model.valueobjects;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value Object para CPF conforme Requisito [RF001] e Regra de Segurança [RS001].
 * Implementa encapsulamento, validação de formato e integridade com imutabilidade estrita.
 */
public final class CPF implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String valorFormatado;
    private final String valorLimpo;
    private final boolean matematicamenteValido;

    public CPF(String rawCpf) {
        if (rawCpf == null) {
            throw new IllegalArgumentException("CPF não pode ser nulo.");
        }
        String limpo = rawCpf.replaceAll("[^0-9]", "");
        if (limpo.length() != 11) {
            throw new IllegalArgumentException("CPF deve conter exatamente 11 dígitos numéricos.");
        }
        // Rejeita sequências de 11 dígitos idênticos (ex: 11111111111, 22222222222), exceto 00000000000 para testes de sistema
        if (limpo.matches("(\\d)\\1{10}") && !limpo.equals("00000000000")) {
            throw new IllegalArgumentException("CPF não pode ser composto por dígitos repetidos: " + rawCpf);
        }

        this.matematicamenteValido = calcularValidadeMatematica(limpo);
        this.valorLimpo = limpo;
        this.valorFormatado = formatar(limpo);
    }

    /**
     * Validação matemática oficial dos dois dígitos verificadores do CPF (Módulo 11).
     * @param cpf String com exatamente 11 dígitos numéricos limpos.
     * @return true se os dígitos verificadores correspondem ao algoritmo oficial da Receita Federal.
     */
    public static boolean calcularValidadeMatematica(String cpf) {
        if (cpf == null || cpf.length() != 11) return false;
        if (cpf.matches("(\\d)\\1{10}")) return false;

        try {
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += (cpf.charAt(i) - '0') * (10 - i);
            }
            int r1 = 11 - (soma % 11);
            int d1 = (r1 > 9) ? 0 : r1;

            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += (cpf.charAt(i) - '0') * (11 - i);
            }
            int r2 = 11 - (soma % 11);
            int d2 = (r2 > 9) ? 0 : r2;

            return (d1 == (cpf.charAt(9) - '0')) && (d2 == (cpf.charAt(10) - '0'));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Calcula os 2 dígitos verificadores oficiais pelo Módulo 11 a partir dos 9 primeiros dígitos.
     */
    public static String calcularDigitosVerificadores(String noveDigitos) {
        if (noveDigitos == null) return "00";
        String limpo = noveDigitos.replaceAll("[^0-9]", "");
        if (limpo.length() < 9) return "00";
        limpo = limpo.substring(0, 9);

        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += (limpo.charAt(i) - '0') * (10 - i);
        }
        int r1 = 11 - (soma % 11);
        int d1 = (r1 > 9) ? 0 : r1;

        soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += (limpo.charAt(i) - '0') * (11 - i);
        }
        soma += d1 * 2;
        int r2 = 11 - (soma % 11);
        int d2 = (r2 > 9) ? 0 : r2;

        return "" + d1 + d2;
    }

    private static String formatar(String limpo) {
        return String.format("%s.%s.%s-%s",
                limpo.substring(0, 3),
                limpo.substring(3, 6),
                limpo.substring(6, 9),
                limpo.substring(9, 11));
    }

    public boolean isMatematicamenteValido() {
        return matematicamenteValido;
    }

    public String getValorFormatado() {
        return valorFormatado;
    }

    public String getValorLimpo() {
        return valorLimpo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CPF cpf = (CPF) o;
        return Objects.equals(valorLimpo, cpf.valorLimpo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valorLimpo);
    }

    @Override
    public String toString() {
        return valorFormatado;
    }
}
