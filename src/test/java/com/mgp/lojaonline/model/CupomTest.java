package com.mgp.lojaonline.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CupomTest {

    private Cupom novoCupom(TipoCupom tipo, String valor) {
        Cupom cupom = new Cupom();
        cupom.setCodigo("TESTE");
        cupom.setTipo(tipo);
        cupom.setValor(new BigDecimal(valor));
        cupom.setValidade(LocalDate.of(2026, 12, 31));
        return cupom;
    }

    @Test
    void descontoPercentualSobreOsProdutos() {
        Cupom cupom = novoCupom(TipoCupom.PERCENTUAL, "10");
        assertEquals(new BigDecimal("8.99"), cupom.calcularDesconto(new BigDecimal("89.90")));
    }

    @Test
    void descontoDeValorFixo() {
        Cupom cupom = novoCupom(TipoCupom.VALOR_FIXO, "20.00");
        assertEquals(new BigDecimal("20.00"), cupom.calcularDesconto(new BigDecimal("100.00")));
    }

    @Test
    void descontoNuncaPassaDoValorDosProdutos() {
        Cupom cupom = novoCupom(TipoCupom.VALOR_FIXO, "20.00");
        assertEquals(new BigDecimal("12.50"), cupom.calcularDesconto(new BigDecimal("12.50")));
    }

    @Test
    void cupomValeAteODiaDaValidade() {
        Cupom cupom = novoCupom(TipoCupom.PERCENTUAL, "10");
        assertFalse(cupom.estaVencido(LocalDate.of(2026, 12, 31)));
        assertTrue(cupom.estaVencido(LocalDate.of(2027, 1, 1)));
    }
}
