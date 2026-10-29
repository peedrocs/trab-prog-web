package com.mgp.lojaonline.dto;

import com.mgp.lojaonline.model.Cupom;
import com.mgp.lojaonline.model.ItemCarrinho;

import java.math.BigDecimal;
import java.util.List;

public record ResumoCompra(List<ItemCarrinho> itens, BigDecimal subtotal, BigDecimal desconto,
                           BigDecimal frete, BigDecimal total, Cupom cupom) {

    public boolean vazio() {
        return itens.isEmpty();
    }
}
