package com.mgp.lojaonline.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfTest {

    @Test
    void aceitaCpfComDigitosVerificadoresCorretos() {
        assertTrue(Cpf.valido("52998224725"));
        assertTrue(Cpf.valido("111.444.777-35"));
    }

    @Test
    void recusaCpfComDigitoVerificadorErrado() {
        assertFalse(Cpf.valido("529.982.247-26"));
        assertFalse(Cpf.valido("111.444.777-53"));
    }

    @Test
    void recusaCpfComTodosOsNumerosIguais() {
        // 111.111.111-11 passa na conta dos digitos, mas nao e um CPF de verdade
        assertFalse(Cpf.valido("111.111.111-11"));
        assertFalse(Cpf.valido("00000000000"));
    }

    @Test
    void recusaCpfComTamanhoErradoOuVazio() {
        assertFalse(Cpf.valido("5299822472"));
        assertFalse(Cpf.valido("529982247251"));
        assertFalse(Cpf.valido(""));
        assertFalse(Cpf.valido(null));
    }

    @Test
    void formataSoOsNumeros() {
        assertEquals("52998224725", Cpf.somenteNumeros("529.982.247-25"));
        assertEquals("529.982.247-25", Cpf.formatar("52998224725"));
    }
}
