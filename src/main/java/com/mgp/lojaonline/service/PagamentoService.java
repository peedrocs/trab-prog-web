package com.mgp.lojaonline.service;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.FormaPagamento;
import com.mgp.lojaonline.model.Pagamento;
import com.mgp.lojaonline.model.Pedido;
import com.mgp.lojaonline.repository.PagamentoRepository;
import com.mgp.lojaonline.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PagamentoService {

    private static final Logger log = LoggerFactory.getLogger(PagamentoService.class);

    private final MercadoPagoService mercadoPago;
    private final PagamentoRepository pagamentoRepository;
    private final PedidoRepository pedidoRepository;

    public PagamentoService(MercadoPagoService mercadoPago, PagamentoRepository pagamentoRepository,
                            PedidoRepository pedidoRepository) {
        this.mercadoPago = mercadoPago;
        this.pagamentoRepository = pagamentoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public Pagamento criarPixOuBoleto(Pedido pedido) {
        JsonNode dados;
        try {
            dados = mercadoPago.criarPagamento(pedido);
        } catch (RestClientException e) {
            log.error("O Mercado Pago recusou o pagamento do pedido {}: {}", pedido.getId(), e.getMessage());
            throw new RegraNegocioException("Não foi possível gerar o pagamento no Mercado Pago agora. Tente de novo em alguns minutos.");
        }

        Pagamento pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        preencher(pagamento, dados);
        pedido.getPagamentos().add(pagamento);
        return pagamentoRepository.save(pagamento);
    }

    // no cartao o cliente vai para a pagina do Mercado Pago; o pagamento so aparece aqui quando o webhook avisar
    public void criarLinkDoCartao(Pedido pedido) {
        try {
            pedido.setLinkMercadoPago(mercadoPago.criarPreferenciaDeCartao(pedido));
        } catch (RestClientException e) {
            log.error("O Mercado Pago recusou a preferência do pedido {}: {}", pedido.getId(), e.getMessage());
            throw new RegraNegocioException("Não foi possível gerar o pagamento no Mercado Pago agora. Tente de novo em alguns minutos.");
        }
    }

    // consulta o pagamento na API do Mercado Pago (nao confia so no aviso) e grava o status atual.
    // devolve null quando o pagamento nao e de nenhum pedido da loja
    public Pagamento atualizar(Long idMercadoPago) {
        JsonNode dados = mercadoPago.buscarPagamento(idMercadoPago);

        Pagamento pagamento = pagamentoRepository.findByIdMercadoPago(idMercadoPago).orElse(null);
        if (pagamento == null) {
            Optional<Pedido> pedido = buscarPedido(texto(dados.path("external_reference")));
            if (pedido.isEmpty()) {
                return null;
            }
            pagamento = new Pagamento();
            pagamento.setPedido(pedido.get());
        }
        preencher(pagamento, dados);
        return pagamentoRepository.save(pagamento);
    }

    public void estornar(Pedido pedido) {
        Pagamento pagamento = pagamentoRepository.findFirstByPedidoAndStatusOrderByDataDesc(pedido, "approved")
                .orElseThrow(() -> new RegraNegocioException("Não encontramos o pagamento aprovado deste pedido para estornar"));
        estornar(pagamento);
    }

    public void estornar(Pagamento pagamento) {
        try {
            mercadoPago.estornar(pagamento.getIdMercadoPago());
        } catch (RestClientException e) {
            log.error("O Mercado Pago recusou o estorno do pagamento {}: {}", pagamento.getIdMercadoPago(), e.getMessage());
            throw new RegraNegocioException("O Mercado Pago não aceitou o estorno agora. Tente de novo em alguns minutos.");
        }
        pagamento.setStatus("refunded");
        pagamento.setAtualizadoEm(LocalDateTime.now());
        pagamentoRepository.save(pagamento);
    }

    public Pagamento ultimoDoPedido(Pedido pedido) {
        return pagamentoRepository.findFirstByPedidoOrderByDataDesc(pedido).orElse(null);
    }

    public List<Pagamento> listar() {
        return pagamentoRepository.findAllByOrderByDataDesc();
    }

    private void preencher(Pagamento pagamento, JsonNode dados) {
        pagamento.setIdMercadoPago(dados.path("id").asLong());
        pagamento.setForma(FormaPagamento.doTipoMercadoPago(texto(dados.path("payment_type_id"))));
        pagamento.setValor(dados.path("transaction_amount").asDecimal());
        pagamento.setStatus(texto(dados.path("status")));
        if (pagamento.getData() == null) {
            pagamento.setData(LocalDateTime.now());
        }
        pagamento.setAtualizadoEm(LocalDateTime.now());

        JsonNode pix = dados.path("point_of_interaction").path("transaction_data");
        if (texto(pix.path("qr_code")) != null) {
            pagamento.setPixCopiaECola(texto(pix.path("qr_code")));
            pagamento.setPixQrCode(texto(pix.path("qr_code_base64")));
        }

        String linkBoleto = texto(dados.path("transaction_details").path("external_resource_url"));
        if (linkBoleto != null) {
            pagamento.setLinkBoleto(linkBoleto);
        }
    }

    private Optional<Pedido> buscarPedido(String referencia) {
        if (referencia == null || !referencia.matches("\\d+")) {
            return Optional.empty();
        }
        return pedidoRepository.findById(Long.valueOf(referencia));
    }

    private String texto(JsonNode campo) {
        if (campo.isMissingNode() || campo.isNull() || campo.asString().isBlank()) {
            return null;
        }
        return campo.asString();
    }
}
