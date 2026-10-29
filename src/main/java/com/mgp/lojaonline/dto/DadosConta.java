package com.mgp.lojaonline.dto;

import com.mgp.lojaonline.model.Endereco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// so os campos que o cliente pode mudar: CPF e e-mail ficam de fora
public class DadosConta {

    @NotBlank(message = "Informe o nome")
    @Size(max = 100, message = "Use no máximo 100 caracteres")
    private String nome;

    @Size(max = 20, message = "Use no máximo 20 caracteres")
    private String telefone;

    private Endereco endereco = new Endereco();

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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
}
