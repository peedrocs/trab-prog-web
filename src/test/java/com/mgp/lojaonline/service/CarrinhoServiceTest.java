package com.mgp.lojaonline.service;

import com.mgp.lojaonline.dto.ResumoCompra;
import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Categoria;
import com.mgp.lojaonline.model.Cupom;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.model.TipoCupom;
import com.mgp.lojaonline.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarrinhoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private CupomService cupomService;

    private CarrinhoService carrinhoService;
    private final MockHttpSession sessao = new MockHttpSession();

    @BeforeEach
    void prepararCarrinho() {
        carrinhoService = new CarrinhoService(produtoRepository, new FreteService(), cupomService);
    }

    private Produto produtoComEstoque(Long id, String preco, int estoque) {
        Categoria categoria = new Categoria();
        categoria.setNome("Cadernos");

        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome("Caderno");
        produto.setPreco(new BigDecimal(preco));
        produto.setEstoque(estoque);
        produto.setCategoria(categoria);
        when(produtoRepository.findById(id)).thenReturn(Optional.of(produto));
        return produto;
    }

    @Test
    void quantidadeNoCarrinhoNuncaPassaDoEstoque() {
        produtoComEstoque(1L, "24.90", 3);

        carrinhoService.adicionar(sessao, 1L, 2);
        assertThrows(RegraNegocioException.class, () -> carrinhoService.adicionar(sessao, 1L, 2));
        assertThrows(RegraNegocioException.class, () -> carrinhoService.atualizar(sessao, 1L, 4));

        assertEquals(2, carrinhoService.carrinho(sessao).get(1L));
    }

    @Test
    void produtoSemEstoqueNaoEntraNoCarrinho() {
        produtoComEstoque(1L, "2.50", 0);

        assertThrows(RegraNegocioException.class, () -> carrinhoService.adicionar(sessao, 1L, 1));
        assertEquals(0, carrinhoService.carrinho(sessao).size());
    }

    @Test
    void resumoSomaProdutosDescontoEFrete() {
        produtoComEstoque(1L, "24.90", 10);
        carrinhoService.adicionar(sessao, 1L, 2);

        Cupom cupom = new Cupom();
        cupom.setCodigo("MENOS20");
        cupom.setTipo(TipoCupom.VALOR_FIXO);
        cupom.setValor(new BigDecimal("20.00"));
        cupom.setValidade(LocalDate.now().plusDays(1));
        sessao.setAttribute("cupom", "MENOS20");
        when(cupomService.validar("MENOS20", null)).thenReturn(cupom);

        ResumoCompra resumo = carrinhoService.resumo(sessao, null);

        assertEquals(new BigDecimal("49.80"), resumo.subtotal());
        assertEquals(new BigDecimal("20.00"), resumo.desconto());
        assertEquals(new BigDecimal("15.00"), resumo.frete());
        assertEquals(new BigDecimal("44.80"), resumo.total());
    }
}
