package com.mgp.lojaonline.model;

import com.mgp.lojaonline.util.Cpf;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    // so os numeros; fica vazio ate quem entrou pelo Google informar o CPF
    @Column(unique = true, length = 11)
    private String cpf;

    @Column(length = 20)
    private String telefone;

    @Embedded
    private Endereco endereco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Perfil perfil = Perfil.USER;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(length = 6)
    private String codigoLogin;

    private LocalDateTime validadeCodigoLogin;

    @Column(length = 36)
    private String tokenSenha;

    private LocalDateTime validadeTokenSenha;

    public String getCpfFormatado() {
        return Cpf.formatar(cpf);
    }

    public String getPrimeiroNome() {
        return nome.trim().split(" ")[0];
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getCodigoLogin() {
        return codigoLogin;
    }

    public void setCodigoLogin(String codigoLogin) {
        this.codigoLogin = codigoLogin;
    }

    public LocalDateTime getValidadeCodigoLogin() {
        return validadeCodigoLogin;
    }

    public void setValidadeCodigoLogin(LocalDateTime validadeCodigoLogin) {
        this.validadeCodigoLogin = validadeCodigoLogin;
    }

    public String getTokenSenha() {
        return tokenSenha;
    }

    public void setTokenSenha(String tokenSenha) {
        this.tokenSenha = tokenSenha;
    }

    public LocalDateTime getValidadeTokenSenha() {
        return validadeTokenSenha;
    }

    public void setValidadeTokenSenha(LocalDateTime validadeTokenSenha) {
        this.validadeTokenSenha = validadeTokenSenha;
    }
}
