package com.mgp.lojaonline.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// a cada minuto cancela os pedidos que passaram do prazo sem pagamento e devolve o estoque
@Component
public class ExpiracaoDePedidos {

    private final PedidoService pedidoService;

    public ExpiracaoDePedidos(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void cancelarPedidosVencidos() {
        pedidoService.cancelarVencidos();
    }
}
