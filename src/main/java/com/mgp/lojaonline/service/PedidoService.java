package com.mgp.lojaonline.service;

import com.mgp.lojaonline.dto.DadosCheckout;
import com.mgp.lojaonline.dto.ResumoCompra;
import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.FormaPagamento;
import com.mgp.lojaonline.model.ItemCarrinho;
import com.mgp.lojaonline.model.ItemPedido;
import com.mgp.lojaonline.model.Pagamento;
import com.mgp.lojaonline.model.Pedido;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.model.StatusPedido;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.repository.PedidoRepository;
import com.mgp.lojaonline.repository.ProdutoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final CarrinhoService carrinhoService;
    private final PagamentoService pagamentoService;
    private final UsuarioService usuarioService;
    private final NotificacaoService notificacaoService;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository,
                         CarrinhoService carrinhoService, PagamentoService pagamentoService,
                         UsuarioService usuarioService, NotificacaoService notificacaoService) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.carrinhoService = carrinhoService;
        this.pagamentoService = pagamentoService;
        this.usuarioService = usuarioService;
        this.notificacaoService = notificacaoService;
    }

    // se o Mercado Pago falhar no meio, a transacao desfaz tudo: o pedido some e o estoque volta
    @Transactional
    public Pedido finalizar(Usuario usuario, ResumoCompra resumo, DadosCheckout dados) {
        if (resumo.vazio()) {
            throw new RegraNegocioException("Seu carrinho está vazio");
        }

        LocalDateTime agora = LocalDateTime.now();
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setData(agora);
        pedido.setFormaPagamento(dados.getFormaPagamento());
        pedido.setPrazoPagamento(calcularPrazo(dados.getFormaPagamento(), agora));
        pedido.setEnderecoEntrega(dados.getEndereco());
        pedido.setCupom(resumo.cupom());
        pedido.setSubtotal(resumo.subtotal());
        pedido.setDesconto(resumo.desconto());
        pedido.setFrete(resumo.frete());
        pedido.setTotal(resumo.total());

        List<Produto> comEstoqueBaixo = new ArrayList<>();
        for (ItemCarrinho itemDoCarrinho : resumo.itens()) {
            Produto produto = itemDoCarrinho.getProduto();
            int quantidade = itemDoCarrinho.getQuantidade();
            carrinhoService.conferirEstoque(produto, quantidade);

            // o estoque fica reservado desde ja; se o pedido for cancelado, ele volta
            int estoqueAntes = produto.getEstoque();
            produto.baixarEstoque(quantidade);
            produtoRepository.save(produto);
            if (estoqueAntes > RelatorioService.LIMITE_ESTOQUE_BAIXO
                    && produto.getEstoque() <= RelatorioService.LIMITE_ESTOQUE_BAIXO) {
                comEstoqueBaixo.add(produto);
            }

            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setQuantidade(quantidade);
            // guarda o preco de agora; se o produto mudar de preco depois, o pedido continua igual
            item.setPrecoUnitario(produto.getPreco());
            pedido.getItens().add(item);
        }

        pedido.mudarStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedidoRepository.save(pedido);
        usuarioService.guardarEndereco(usuario, dados.getEndereco());

        Pagamento pagamento = null;
        if (pedido.getFormaPagamento() == FormaPagamento.CARTAO) {
            pagamentoService.criarLinkDoCartao(pedido);
        } else {
            pagamento = pagamentoService.criarPixOuBoleto(pedido);
        }

        notificacaoService.pedidoCriado(pedido, pagamento);
        for (Produto produto : comEstoqueBaixo) {
            notificacaoService.estoqueBaixo(produto);
        }
        return pedido;
    }

    // pix e cartao esperam 30 minutos; boleto espera 3 dias, que e o tempo de o banco compensar
    private LocalDateTime calcularPrazo(FormaPagamento forma, LocalDateTime agora) {
        return forma == FormaPagamento.BOLETO ? agora.plusDays(3) : agora.plusMinutes(30);
    }

    // chamado pelo webhook: o pedido muda para Pago sem ninguem precisar mexer
    @Transactional
    public void atualizarPagamento(Long idMercadoPago) {
        Pagamento pagamento = pagamentoService.atualizar(idMercadoPago);
        if (pagamento == null || !pagamento.isAprovado()) {
            return;
        }

        Pedido pedido = pagamento.getPedido();
        if (pedido.getStatus() == StatusPedido.AGUARDANDO_PAGAMENTO) {
            confirmarPagamento(pedido);
        } else if (pedido.getStatus() == StatusPedido.CANCELADO) {
            // pagou depois que o pedido ja tinha sido cancelado: o dinheiro volta para o cliente
            pagamentoService.estornar(pagamento);
        }
    }

    private void confirmarPagamento(Pedido pedido) {
        pedido.mudarStatus(StatusPedido.PAGO);
        pedidoRepository.save(pedido);

        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            produto.setQuantidadeVendida(produto.getQuantidadeVendida() + item.getQuantidade());
            produtoRepository.save(produto);
        }
        notificacaoService.pedidoPago(pedido);
    }

    @Transactional
    public void avancarStatus(Long id, String codigoRastreio) {
        Pedido pedido = buscar(id);
        StatusPedido proximo = pedido.getStatus().getProximo();
        if (proximo == null) {
            throw new RegraNegocioException("Este pedido não tem um próximo status");
        }
        if (proximo == StatusPedido.ENVIADO) {
            if (codigoRastreio == null || codigoRastreio.isBlank()) {
                throw new RegraNegocioException("Informe o código de rastreio para marcar o pedido como enviado");
            }
            pedido.setCodigoRastreio(codigoRastreio.trim().toUpperCase());
        }

        pedido.mudarStatus(proximo);
        pedidoRepository.save(pedido);

        if (proximo == StatusPedido.ENVIADO) {
            notificacaoService.pedidoEnviado(pedido);
        } else if (proximo == StatusPedido.ENTREGUE) {
            notificacaoService.pedidoEntregue(pedido);
        }
    }

    @Transactional
    public void cancelar(Long id) {
        cancelar(buscar(id), null);
    }

    @Transactional
    public void cancelarVencidos() {
        List<Pedido> vencidos = pedidoRepository.findByStatusAndPrazoPagamentoBefore(
                StatusPedido.AGUARDANDO_PAGAMENTO, LocalDateTime.now());
        for (Pedido pedido : vencidos) {
            cancelar(pedido, "o pagamento não foi feito dentro do prazo");
        }
    }

    private void cancelar(Pedido pedido, String motivo) {
        if (!pedido.getStatus().isPodeCancelar()) {
            throw new RegraNegocioException("Este pedido não pode mais ser cancelado");
        }

        boolean jaFoiPago = pedido.getStatus().isPago();
        if (jaFoiPago) {
            pagamentoService.estornar(pedido);
        }

        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            produto.devolverEstoque(item.getQuantidade());
            if (jaFoiPago) {
                produto.setQuantidadeVendida(produto.getQuantidadeVendida() - item.getQuantidade());
            }
            produtoRepository.save(produto);
        }

        pedido.mudarStatus(StatusPedido.CANCELADO);
        pedidoRepository.save(pedido);
        notificacaoService.pedidoCancelado(pedido, jaFoiPago, motivo);
    }

    public List<Pedido> listarDoCliente(Usuario usuario) {
        return pedidoRepository.findByUsuarioOrderByDataDesc(usuario);
    }

    public Pedido buscarDoCliente(Long id, Usuario usuario) {
        return pedidoRepository.findById(id)
                .filter(pedido -> pedido.getUsuario().getId().equals(usuario.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public List<Pedido> listar(StatusPedido status) {
        return status == null
                ? pedidoRepository.findAllByOrderByDataDesc()
                : pedidoRepository.findByStatusOrderByDataDesc(status);
    }

    public Pedido buscar(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
