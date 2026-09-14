package com.mgp.lojaonline.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Cupom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Informe o código do cupom")
    @Size(max = 30, message = "O código deve ter no máximo 30 caracteres")
    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @NotNull(message = "Selecione o tipo de desconto")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDesconto tipo;

    @NotNull(message = "Informe o valor do desconto")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Use no máximo 8 dígitos antes da vírgula e 2 casas decimais")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @NotNull(message = "Informe a validade")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(nullable = false)
    private LocalDate validade;

    public boolean isVencido() {
        return LocalDate.now().isAfter(validade);
    }
}
