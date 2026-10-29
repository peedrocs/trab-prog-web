package com.mgp.lojaonline.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Embeddable
public class Endereco {

    @NotBlank(message = "Informe o CEP")
    @Pattern(regexp = "\\d{5}-?\\d{3}", message = "O CEP deve ter 8 números")
    @Column(length = 9)
    private String cep;

    @NotBlank(message = "Informe a rua")
    @Size(max = 150, message = "Use no máximo 150 caracteres")
    @Column(length = 150)
    private String rua;

    @NotBlank(message = "Informe o número")
    @Size(max = 20, message = "Use no máximo 20 caracteres")
    @Column(length = 20)
    private String numero;

    @Size(max = 100, message = "Use no máximo 100 caracteres")
    @Column(length = 100)
    private String complemento;

    @NotBlank(message = "Informe o bairro")
    @Size(max = 100, message = "Use no máximo 100 caracteres")
    @Column(length = 100)
    private String bairro;

    @NotBlank(message = "Informe a cidade")
    @Size(max = 100, message = "Use no máximo 100 caracteres")
    @Column(length = 100)
    private String cidade;

    @NotBlank(message = "Informe a UF")
    @Pattern(regexp = "[A-Za-z]{2}", message = "A UF deve ter 2 letras")
    @Column(length = 2)
    private String uf;

    public Endereco copiar() {
        Endereco copia = new Endereco();
        copia.setCep(cep);
        copia.setRua(rua);
        copia.setNumero(numero);
        copia.setComplemento(complemento);
        copia.setBairro(bairro);
        copia.setCidade(cidade);
        copia.setUf(uf);
        return copia;
    }

    public String getDescricao() {
        String descricao = rua + ", " + numero;
        if (complemento != null && !complemento.isBlank()) {
            descricao += ", " + complemento;
        }
        return descricao + " - " + bairro + ", " + cidade + "/" + uf + " - CEP " + cep;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf == null ? null : uf.toUpperCase();
    }
}
