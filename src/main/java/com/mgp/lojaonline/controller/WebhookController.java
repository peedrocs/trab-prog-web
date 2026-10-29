package com.mgp.lojaonline.controller;

import com.mgp.lojaonline.service.MercadoPagoService;
import com.mgp.lojaonline.service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// o Mercado Pago chama este endereco quando um pagamento e criado ou muda de status
@RestController
public class WebhookController {

    private final MercadoPagoService mercadoPagoService;
    private final PedidoService pedidoService;

    public WebhookController(MercadoPagoService mercadoPagoService, PedidoService pedidoService) {
        this.mercadoPagoService = mercadoPagoService;
        this.pedidoService = pedidoService;
    }

    @PostMapping("/webhook/mercadopago")
    public ResponseEntity<Void> receber(@RequestParam(name = "data.id", required = false) String idDoDado,
                                        @RequestParam(required = false) String type,
                                        @RequestHeader(name = "x-signature", required = false) String assinatura,
                                        @RequestHeader(name = "x-request-id", required = false) String idRequisicao) {
        // outros avisos (como merchant_order) nao interessam: responde 200 para o Mercado Pago nao repetir
        if (!"payment".equals(type) || idDoDado == null || !idDoDado.matches("\\d+")) {
            return ResponseEntity.ok().build();
        }
        if (!mercadoPagoService.assinaturaValida(assinatura, idRequisicao, idDoDado)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        pedidoService.atualizarPagamento(Long.valueOf(idDoDado));
        return ResponseEntity.ok().build();
    }
}
