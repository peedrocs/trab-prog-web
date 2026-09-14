package com.mgp.lojaonline.service;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.ItemCarrinho;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// uma instância por sessão: cada visitante tem o seu próprio carrinho
@Service
@SessionScope
@RequiredArgsConstructor
public class CarrinhoService {

    private static final BigDecimal VALOR_FRETE_GRATIS = new BigDecimal("150.00");
    private static final BigDecimal FRETE_ATE_5_ITENS = new BigDecimal("15.00");
    private static final BigDecimal FRETE_ACIMA_DE_5_ITENS = new BigDecimal("25.00");

    private final ProdutoRepository produtoRepository;

    // chave: id do produto, valor: quantidade
    private final Map<Long, Integer> itens = new LinkedHashMap<>();

    public void adicionar(Long produtoId, int quantidade) {
        Produto produto = buscarProduto(produtoId);
        if (!produto.isDisponivel()) {
            throw new RegraNegocioException("Produto indisponível no momento");
        }
        int novaQuantidade = itens.getOrDefault(produtoId, 0) + quantidade;
        validarQuantidade(produto, novaQuantidade);
        itens.put(produtoId, novaQuantidade);
    }

    public void atualizar(Long produtoId, int quantidade) {
        Produto produto = buscarProduto(produtoId);
        validarQuantidade(produto, quantidade);
        itens.put(produtoId, quantidade);
    }

    public void remover(Long produtoId) {
        itens.remove(produtoId);
    }

    public void limpar() {
        itens.clear();
    }

    public List<ItemCarrinho> listarItens() {
        List<ItemCarrinho> lista = new ArrayList<>();
        for (Map.Entry<Long, Integer> item : itens.entrySet()) {
            produtoRepository.findById(item.getKey())
                    .ifPresent(produto -> lista.add(new ItemCarrinho(produto, item.getValue())));
        }
        return lista;
    }

    public int contarItens() {
        int total = 0;
        for (ItemCarrinho item : listarItens()) {
            total += item.getQuantidade();
        }
        return total;
    }

    public BigDecimal calcularSubtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemCarrinho item : listarItens()) {
            subtotal = subtotal.add(item.getSubtotal());
        }
        return subtotal;
    }

    public BigDecimal calcularFrete() {
        int quantidade = contarItens();
        if (quantidade == 0 || calcularSubtotal().compareTo(VALOR_FRETE_GRATIS) >= 0) {
            return BigDecimal.ZERO;
        }
        return quantidade <= 5 ? FRETE_ATE_5_ITENS : FRETE_ACIMA_DE_5_ITENS;
    }

    public void validarQuantidade(Produto produto, int quantidade) {
        if (quantidade < 1) {
            throw new RegraNegocioException("A quantidade deve ser de pelo menos 1 unidade");
        }
        if (quantidade > produto.getEstoque()) {
            throw new RegraNegocioException(
                    "Estoque insuficiente para " + produto.getNome() + ". Máximo no carrinho: " + produto.getEstoque());
        }
    }

    private Produto buscarProduto(Long produtoId) {
        return produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
    }
}
