package com.mgp.lojaonline.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
public class Cupom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Informe o código")
    @Size(max = 30, message = "Use no máximo 30 caracteres")
    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @NotNull(message = "Escolha o tipo do desconto")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCupom tipo;

    @NotNull(message = "Informe o valor do desconto")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @NotNull(message = "Informe a validade")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(nullable = false)
    private LocalDate validade;

    @Column(nullable = false)
    private boolean ativo = true;

    public boolean estaVencido(LocalDate hoje) {
        return validade.isBefore(hoje);
    }

    public BigDecimal calcularDesconto(BigDecimal subtotal) {
        BigDecimal desconto;
        if (tipo == TipoCupom.PERCENTUAL) {
            desconto = subtotal.multiply(valor).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            desconto = valor;
        }
        // o desconto nunca passa do valor dos produtos
        return desconto.min(subtotal);
    }

    public String getDescricaoDesconto() {
        if (tipo == TipoCupom.PERCENTUAL) {
            return valor.stripTrailingZeros().toPlainString() + "%";
        }
        return "R$ " + valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public TipoCupom getTipo() {
        return tipo;
    }

    public void setTipo(TipoCupom tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getValidade() {
        return validade;
    }

    public void setValidade(LocalDate validade) {
        this.validade = validade;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
