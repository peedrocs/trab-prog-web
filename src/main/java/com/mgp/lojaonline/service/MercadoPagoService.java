package com.mgp.lojaonline.service;

import com.mgp.lojaonline.model.Endereco;
import com.mgp.lojaonline.model.FormaPagamento;
import com.mgp.lojaonline.model.Pedido;
import com.mgp.lojaonline.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MercadoPagoService {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private final RestClient api;
    private final String segredoWebhook;
    private final String urlLoja;

    public MercadoPagoService(@Value("${mercadopago.access-token}") String accessToken,
                              @Value("${mercadopago.webhook-secret}") String segredoWebhook,
                              @Value("${loja.url}") String urlLoja) {
        this.api = RestClient.builder()
                .baseUrl("https://api.mercadopago.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .build();
        this.segredoWebhook = segredoWebhook;
        this.urlLoja = urlLoja;
    }

    // Pix ou boleto, criado direto na API de pagamentos
    public JsonNode criarPagamento(Pedido pedido) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("transaction_amount", pedido.getTotal());
        corpo.put("description", descricao(pedido));
        corpo.put("payment_method_id", pedido.getFormaPagamento() == FormaPagamento.PIX ? "pix" : "bolbradesco");
        corpo.put("external_reference", pedido.getId().toString());
        // o Mercado Pago so aceita vencimento de 30 minutos ou mais a partir de quando recebe o pedido,
        // entao vai um minuto de folga; quem pagar nesse minuto depois do cancelamento recebe o estorno
        corpo.put("date_of_expiration", formatarData(pedido.getPrazoPagamento().plusMinutes(1)));
        corpo.put("payer", pagador(pedido));

        return api.post()
                .uri("/v1/payments")
                .header("X-Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .body(corpo)
                .retrieve()
                .body(JsonNode.class);
    }

    // cartao usa o Checkout Pro: o formulario do cartao e o do Mercado Pago, entao o cartao nunca passa por aqui
    public String criarPreferenciaDeCartao(Pedido pedido) {
        String paginaDoPedido = urlLoja + "/pedidos/" + pedido.getId();

        Map<String, Object> corpo = new HashMap<>();
        corpo.put("items", List.of(Map.of(
                "title", descricao(pedido),
                "quantity", 1,
                "unit_price", pedido.getTotal(),
                "currency_id", "BRL")));
        corpo.put("payer", Map.of(
                "email", pedido.getUsuario().getEmail(),
                "identification", identificacao(pedido.getUsuario())));
        corpo.put("external_reference", pedido.getId().toString());
        corpo.put("back_urls", Map.of("success", paginaDoPedido, "pending", paginaDoPedido, "failure", paginaDoPedido));
        corpo.put("notification_url", urlLoja + "/webhook/mercadopago?source_news=webhooks");
        // so cartao de credito: tira boleto, Pix, debito e as outras formas
        corpo.put("payment_methods", Map.of(
                "excluded_payment_types", List.of(
                        Map.of("id", "ticket"),
                        Map.of("id", "bank_transfer"),
                        Map.of("id", "atm"),
                        Map.of("id", "debit_card"),
                        Map.of("id", "prepaid_card")),
                "installments", 12));
        corpo.put("expires", true);
        corpo.put("expiration_date_to", formatarData(pedido.getPrazoPagamento()));

        JsonNode preferencia = api.post()
                .uri("/checkout/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .body(corpo)
                .retrieve()
                .body(JsonNode.class);
        return preferencia.path("init_point").asString();
    }

    public JsonNode buscarPagamento(Long idPagamento) {
        return api.get()
                .uri("/v1/payments/{id}", idPagamento)
                .retrieve()
                .body(JsonNode.class);
    }

    // corpo vazio = estorno do valor total
    public void estornar(Long idPagamento) {
        api.post()
                .uri("/v1/payments/{id}/refunds", idPagamento)
                .header("X-Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of())
                .retrieve()
                .toBodilessEntity();
    }

    // o cabecalho x-signature traz ts=...,v1=...; o v1 e um HMAC SHA256 que so quem tem a chave secreta consegue gerar
    public boolean assinaturaValida(String assinatura, String idRequisicao, String idDoDado) {
        if (segredoWebhook.isBlank() || assinatura == null || idRequisicao == null || idDoDado == null) {
            return false;
        }

        String ts = null;
        String v1 = null;
        for (String parte : assinatura.split(",")) {
            String[] chaveEValor = parte.trim().split("=", 2);
            if (chaveEValor.length == 2 && chaveEValor[0].equals("ts")) {
                ts = chaveEValor[1];
            } else if (chaveEValor.length == 2 && chaveEValor[0].equals("v1")) {
                v1 = chaveEValor[1];
            }
        }
        if (ts == null || v1 == null) {
            return false;
        }

        String manifesto = "id:" + idDoDado.toLowerCase() + ";request-id:" + idRequisicao + ";ts:" + ts + ";";
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(segredoWebhook.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String esperado = HexFormat.of().formatHex(hmac.doFinal(manifesto.getBytes(StandardCharsets.UTF_8)));
            return MessageDigest.isEqual(esperado.getBytes(StandardCharsets.UTF_8), v1.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    private Map<String, Object> pagador(Pedido pedido) {
        Usuario usuario = pedido.getUsuario();
        String[] partesDoNome = usuario.getNome().trim().split("\\s+", 2);
        Endereco endereco = pedido.getEnderecoEntrega();

        Map<String, Object> pagador = new HashMap<>();
        pagador.put("email", usuario.getEmail());
        pagador.put("first_name", partesDoNome[0]);
        pagador.put("last_name", partesDoNome.length > 1 ? partesDoNome[1] : partesDoNome[0]);
        pagador.put("identification", identificacao(usuario));
        // o boleto exige o endereco de quem paga
        pagador.put("address", Map.of(
                "zip_code", endereco.getCep().replace("-", ""),
                "street_name", endereco.getRua(),
                "street_number", endereco.getNumero(),
                "neighborhood", endereco.getBairro(),
                "city", endereco.getCidade(),
                "federal_unit", endereco.getUf()));
        return pagador;
    }

    // o CPF do cliente identifica quem paga; o Mercado Pago exige no Pix e no boleto
    private Map<String, Object> identificacao(Usuario usuario) {
        return Map.of("type", "CPF", "number", usuario.getCpf());
    }

    private String descricao(Pedido pedido) {
        return "Pedido #" + pedido.getId() + " - Papelaria MGP";
    }

    private String formatarData(LocalDateTime data) {
        return data.atZone(ZoneId.systemDefault()).format(FORMATO_DATA);
    }
}
