package com.mgp.lojaonline.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DadosNovaSenha {

    private String token;

    @NotBlank(message = "Informe a nova senha")
    @Size(min = 6, max = 72, message = "A senha deve ter de 6 a 72 caracteres")
    private String senha;

    @NotBlank(message = "Repita a nova senha")
    private String confirmacao;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getConfirmacao() {
        return confirmacao;
    }

    public void setConfirmacao(String confirmacao) {
        this.confirmacao = confirmacao;
    }
}
