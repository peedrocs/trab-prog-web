package com.mgp.lojaonline.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Informe o nome do produto")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
    @Column(length = 1000)
    private String descricao;

    @NotNull(message = "Informe o preço")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Use no máximo 8 dígitos antes da vírgula e 2 casas decimais")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @NotNull(message = "Informe o estoque")
    @Min(value = 0, message = "O estoque não pode ser negativo")
    @Column(nullable = false)
    private Integer estoque;

    @Size(max = 255, message = "A URL da imagem deve ter no máximo 255 caracteres")
    private String imagemUrl;

    @NotNull(message = "Selecione uma categoria")
    @ManyToOne(optional = false)
    private Categoria categoria;

    public boolean isDisponivel() {
        return estoque != null && estoque > 0;
    }

    public String getImagem() {
        return imagemUrl == null || imagemUrl.isBlank() ? "/imagens/sem-imagem.svg" : imagemUrl;
    }
}
