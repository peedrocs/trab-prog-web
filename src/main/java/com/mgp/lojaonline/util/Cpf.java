package com.mgp.lojaonline.util;

public class Cpf {

    private Cpf() {
    }

    public static String somenteNumeros(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    // confere os dois digitos verificadores, nao so o formato
    public static boolean valido(String cpf) {
        String numeros = somenteNumeros(cpf);
        if (numeros.length() != 11 || numeros.chars().distinct().count() == 1) {
            return false;
        }
        return digitoVerificador(numeros, 9) == numeros.charAt(9) - '0'
                && digitoVerificador(numeros, 10) == numeros.charAt(10) - '0';
    }

    private static int digitoVerificador(String numeros, int quantidadeDeDigitos) {
        int soma = 0;
        for (int i = 0; i < quantidadeDeDigitos; i++) {
            int peso = quantidadeDeDigitos + 1 - i;
            soma += (numeros.charAt(i) - '0') * peso;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    public static String formatar(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }
}
