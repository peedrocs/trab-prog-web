package com.mgp.lojaonline.service;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.*;
import com.mgp.lojaonline.repository.PedidoRepository;
import com.mgp.lojaonline.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final CarrinhoService carrinhoService;
    private final CupomService cupomService;

    // monta o pedido a partir do carrinho, sem salvar (usado também na tela de confirmação)
    public Pedido montarPedido(Usuario usuario, String codigoCupom) {
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);

        for (ItemCarrinho item : carrinhoService.listarItens()) {
            carrinhoService.validarQuantidade(item.getProduto(), item.getQuantidade());
            pedido.adicionarItem(new ItemPedido(item.getProduto(), item.getQuantidade()));
        }

        if (pedido.getItens().isEmpty()) {
            throw new RegraNegocioException("Seu carrinho está vazio");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemPedido item : pedido.getItens()) {
            subtotal = subtotal.add(item.getSubtotal());
        }
        pedido.setSubtotal(subtotal);
        pedido.setFrete(carrinhoService.calcularFrete());

        if (codigoCupom != null && !codigoCupom.isBlank()) {
            Cupom cupom = cupomService.validar(codigoCupom, usuario);
            pedido.setCupom(cupom);
            pedido.setDesconto(cupomService.calcularDesconto(cupom, subtotal));
        }

        pedido.setTotal(subtotal.subtract(pedido.getDesconto()).add(pedido.getFrete()));
        return pedido;
    }

    @Transactional
    public Pedido finalizar(Usuario usuario, String codigoCupom) {
        Pedido pedido = montarPedido(usuario, codigoCupom);

        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            produto.setEstoque(produto.getEstoque() - item.getQuantidade());
            produtoRepository.save(produto);
        }

        pedido.setDataCriacao(LocalDateTime.now());
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedidoRepository.save(pedido);
        carrinhoService.limpar();
        return pedido;
    }

    public List<Pedido> listarDoUsuario(Usuario usuario) {
        return pedidoRepository.findByUsuarioOrderByDataCriacaoDesc(usuario);
    }

    public Pedido buscarDoUsuario(Long id, Usuario usuario) {
        Pedido pedido = buscarPorId(id);
        if (!pedido.getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado");
        }
        return pedido;
    }

    // status nulo lista todos os pedidos
    public List<Pedido> listarPorStatus(StatusPedido status) {
        if (status == null) {
            return pedidoRepository.findAllByOrderByDataCriacaoDesc();
        }
        return pedidoRepository.findByStatusOrderByDataCriacaoDesc(status);
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));
    }

    @Transactional
    public void alterarStatus(Long id, StatusPedido novoStatus) {
        Pedido pedido = buscarPorId(id);
        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new RegraNegocioException("Um pedido cancelado não pode mudar de status");
        }

        if (novoStatus == StatusPedido.CANCELADO) {
            for (ItemPedido item : pedido.getItens()) {
                Produto produto = item.getProduto();
                produto.setEstoque(produto.getEstoque() + item.getQuantidade());
                produtoRepository.save(produto);
            }
        }

        pedido.setStatus(novoStatus);
        pedidoRepository.save(pedido);
    }

    public long contarPorStatus(StatusPedido status) {
        return pedidoRepository.countByStatus(status);
    }
}
