package com.mgp.lojaonline.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FreteService {

    private static final BigDecimal VALOR_FRETE_GRATIS = new BigDecimal("150.00");
    private static final BigDecimal FRETE_ATE_5_ITENS = new BigDecimal("15.00");
    private static final BigDecimal FRETE_ACIMA_DE_5_ITENS = new BigDecimal("25.00");

    // frete gratis a partir de R$ 150,00 em produtos (antes do cupom); abaixo disso, depende da quantidade
    public BigDecimal calcular(BigDecimal subtotal, int quantidadeDeItens) {
        if (quantidadeDeItens == 0 || subtotal.compareTo(VALOR_FRETE_GRATIS) >= 0) {
            return BigDecimal.ZERO;
        }
        return quantidadeDeItens <= 5 ? FRETE_ATE_5_ITENS : FRETE_ACIMA_DE_5_ITENS;
    }
}
