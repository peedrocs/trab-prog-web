package com.mgp.lojaonline.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FreteServiceTest {

    private final FreteService freteService = new FreteService();

    @Test
    void freteGratisAPartirDe150Reais() {
        assertEquals(BigDecimal.ZERO, freteService.calcular(new BigDecimal("150.00"), 3));
        assertEquals(BigDecimal.ZERO, freteService.calcular(new BigDecimal("320.50"), 12));
    }

    @Test
    void freteDe15ReaisAte5Itens() {
        assertEquals(new BigDecimal("15.00"), freteService.calcular(new BigDecimal("40.00"), 1));
        assertEquals(new BigDecimal("15.00"), freteService.calcular(new BigDecimal("149.99"), 5));
    }

    @Test
    void freteDe25ReaisAcimaDe5Itens() {
        assertEquals(new BigDecimal("25.00"), freteService.calcular(new BigDecimal("12.00"), 6));
    }

    @Test
    void carrinhoVazioNaoPagaFrete() {
        assertEquals(BigDecimal.ZERO, freteService.calcular(BigDecimal.ZERO, 0));
    }
}
