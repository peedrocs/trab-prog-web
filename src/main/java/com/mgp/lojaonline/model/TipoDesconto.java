package com.mgp.lojaonline.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoDesconto {
    PERCENTUAL("Percentual (%)"),
    VALOR_FIXO("Valor fixo (R$)");

    private final String descricao;
}
