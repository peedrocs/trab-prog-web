package com.mgp.lojaonline.service;

import com.mgp.lojaonline.dto.ResumoCompra;
import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Cupom;
import com.mgp.lojaonline.model.ItemCarrinho;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.repository.ProdutoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CarrinhoService {

    private final ProdutoRepository produtoRepository;
    private final FreteService freteService;
    private final CupomService cupomService;

    public CarrinhoService(ProdutoRepository produtoRepository, FreteService freteService, CupomService cupomService) {
        this.produtoRepository = produtoRepository;
        this.freteService = freteService;
        this.cupomService = cupomService;
    }

    // o carrinho e um mapa id do produto -> quantidade guardado na sessao de cada visitante
    @SuppressWarnings("unchecked")
    public Map<Long, Integer> carrinho(HttpSession sessao) {
        Map<Long, Integer> carrinho = (Map<Long, Integer>) sessao.getAttribute("carrinho");
        if (carrinho == null) {
            carrinho = new LinkedHashMap<>();
            sessao.setAttribute("carrinho", carrinho);
        }
        return carrinho;
    }

    public void adicionar(HttpSession sessao, Long produtoId, int quantidade) {
        Map<Long, Integer> carrinho = carrinho(sessao);
        int novaQuantidade = carrinho.getOrDefault(produtoId, 0) + quantidade;
        conferirEstoque(buscarProduto(produtoId), novaQuantidade);
        carrinho.put(produtoId, novaQuantidade);
    }

    public void atualizar(HttpSession sessao, Long produtoId, int quantidade) {
        conferirEstoque(buscarProduto(produtoId), quantidade);
        carrinho(sessao).put(produtoId, quantidade);
    }

    public void remover(HttpSession sessao, Long produtoId) {
        carrinho(sessao).remove(produtoId);
    }

    public void esvaziar(HttpSession sessao) {
        carrinho(sessao).clear();
        sessao.removeAttribute("cupom");
    }

    public void conferirEstoque(Produto produto, int quantidade) {
        if (!produto.isDisponivel()) {
            throw new RegraNegocioException(produto.getNome() + " está indisponível no momento");
        }
        if (quantidade < 1) {
            throw new RegraNegocioException("A quantidade mínima é 1 unidade");
        }
        if (quantidade > produto.getEstoque()) {
            throw new RegraNegocioException("Temos só " + produto.getEstoque() + " unidade(s) de " + produto.getNome());
        }
    }

    public void aplicarCupom(HttpSession sessao, String codigo, Usuario usuario) {
        Cupom cupom = cupomService.validar(codigo, usuario);
        sessao.setAttribute("cupom", cupom.getCodigo());
    }

    public void removerCupom(HttpSession sessao) {
        sessao.removeAttribute("cupom");
    }

    public ResumoCompra resumo(HttpSession sessao, Usuario usuario) {
        List<ItemCarrinho> itens = itens(sessao);
        BigDecimal subtotal = BigDecimal.ZERO;
        int quantidadeDeItens = 0;
        for (ItemCarrinho item : itens) {
            subtotal = subtotal.add(item.getSubtotal());
            quantidadeDeItens += item.getQuantidade();
        }

        Cupom cupom = cupomAplicado(sessao, usuario);
        BigDecimal desconto = cupom == null ? BigDecimal.ZERO : cupom.calcularDesconto(subtotal);
        BigDecimal frete = freteService.calcular(subtotal, quantidadeDeItens);
        BigDecimal total = subtotal.subtract(desconto).add(frete);

        return new ResumoCompra(itens, subtotal, desconto, frete, total, cupom);
    }

    private List<ItemCarrinho> itens(HttpSession sessao) {
        Map<Long, Integer> carrinho = carrinho(sessao);
        List<ItemCarrinho> itens = new ArrayList<>();

        for (Long produtoId : new ArrayList<>(carrinho.keySet())) {
            Optional<Produto> produto = produtoRepository.findById(produtoId).filter(Produto::isVisivelNaLoja);
            if (produto.isPresent()) {
                itens.add(new ItemCarrinho(produto.get(), carrinho.get(produtoId)));
            } else {
                // produto excluido ou desativado depois de entrar no carrinho sai dele
                carrinho.remove(produtoId);
            }
        }
        return itens;
    }

    // cupom que deixou de valer (venceu ou ja foi usado) sai do carrinho sem travar a compra
    private Cupom cupomAplicado(HttpSession sessao, Usuario usuario) {
        String codigo = (String) sessao.getAttribute("cupom");
        if (codigo == null) {
            return null;
        }
        try {
            return cupomService.validar(codigo, usuario);
        } catch (RegraNegocioException e) {
            sessao.removeAttribute("cupom");
            return null;
        }
    }

    private Produto buscarProduto(Long produtoId) {
        return produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RegraNegocioException("Produto não encontrado"));
    }
}
