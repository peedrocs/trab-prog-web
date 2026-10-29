package com.mgp.lojaonline.model;

import com.mgp.lojaonline.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Informe o nome")
    @Size(max = 100, message = "Use no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @Size(max = 1000, message = "Use no máximo 1000 caracteres")
    @Column(length = 1000)
    private String descricao;

    @NotNull(message = "Informe o preço")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @NotNull(message = "Informe o estoque")
    @Min(value = 0, message = "O estoque não pode ser negativo")
    @Column(nullable = false)
    private Integer estoque;

    private String imagemUrl;

    @NotNull(message = "Escolha uma categoria")
    @ManyToOne(optional = false)
    private Categoria categoria;

    @Column(nullable = false)
    private boolean ativo = true;

    // soma das unidades de pedidos pagos, usada para ordenar por mais vendidos
    @Column(nullable = false)
    private int quantidadeVendida;

    public boolean isVisivelNaLoja() {
        return ativo && categoria.isAtiva();
    }

    public boolean isDisponivel() {
        return isVisivelNaLoja() && estoque > 0;
    }

    public void baixarEstoque(int quantidade) {
        if (quantidade > estoque) {
            throw new RegraNegocioException("Não temos estoque suficiente de " + nome);
        }
        estoque -= quantidade;
    }

    public void devolverEstoque(int quantidade) {
        estoque += quantidade;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Integer getEstoque() {
        return estoque;
    }

    public void setEstoque(Integer estoque) {
        this.estoque = estoque;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public int getQuantidadeVendida() {
        return quantidadeVendida;
    }

    public void setQuantidadeVendida(int quantidadeVendida) {
        this.quantidadeVendida = quantidadeVendida;
    }
}
