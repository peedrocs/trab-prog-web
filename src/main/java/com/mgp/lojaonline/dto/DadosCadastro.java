package com.mgp.lojaonline.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DadosCadastro {

    @NotBlank(message = "Informe o nome")
    @Size(max = 100, message = "Use no máximo 100 caracteres")
    private String nome;

    @NotBlank(message = "Informe o e-mail")
    @Email(message = "Informe um e-mail válido")
    private String email;

    @NotBlank(message = "Informe o CPF")
    private String cpf;

    @NotBlank(message = "Informe a senha")
    @Size(min = 6, max = 72, message = "A senha deve ter de 6 a 72 caracteres")
    private String senha;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
