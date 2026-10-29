package com.mgp.lojaonline.model;

import com.mgp.lojaonline.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProdutoTest {

    private Produto novoProduto(int estoque) {
        Categoria categoria = new Categoria();
        categoria.setNome("Canetas");

        Produto produto = new Produto();
        produto.setNome("Caneta azul");
        produto.setPreco(new BigDecimal("2.50"));
        produto.setEstoque(estoque);
        produto.setCategoria(categoria);
        return produto;
    }

    @Test
    void baixarEstoqueTiraAsUnidadesVendidas() {
        Produto produto = novoProduto(10);
        produto.baixarEstoque(3);
        assertEquals(7, produto.getEstoque());
    }

    @Test
    void naoBaixaMaisDoQueTemNoEstoque() {
        Produto produto = novoProduto(2);
        assertThrows(RegraNegocioException.class, () -> produto.baixarEstoque(3));
        assertEquals(2, produto.getEstoque());
    }

    @Test
    void devolverEstoqueSomaAsUnidades() {
        Produto produto = novoProduto(0);
        produto.devolverEstoque(4);
        assertEquals(4, produto.getEstoque());
    }

    @Test
    void produtoSemEstoqueFicaIndisponivel() {
        assertTrue(novoProduto(1).isDisponivel());
        assertFalse(novoProduto(0).isDisponivel());
    }

    @Test
    void produtoDesativadoOuDeCategoriaDesativadaFicaIndisponivel() {
        Produto desativado = novoProduto(5);
        desativado.setAtivo(false);
        assertFalse(desativado.isDisponivel());

        Produto daCategoriaDesativada = novoProduto(5);
        daCategoriaDesativada.getCategoria().setAtiva(false);
        assertFalse(daCategoriaDesativada.isDisponivel());
    }
}
