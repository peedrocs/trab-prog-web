package com.mgp.lojaonline.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusPedidoTest {

    @Test
    void adminSegueAOrdemDepoisDoPagamento() {
        assertEquals(StatusPedido.EM_SEPARACAO, StatusPedido.PAGO.getProximo());
        assertEquals(StatusPedido.ENVIADO, StatusPedido.EM_SEPARACAO.getProximo());
        assertEquals(StatusPedido.ENTREGUE, StatusPedido.ENVIADO.getProximo());
    }

    @Test
    void soOMercadoPagoTiraDoAguardandoPagamento() {
        assertNull(StatusPedido.AGUARDANDO_PAGAMENTO.getProximo());
    }

    @Test
    void entregueECanceladoNaoMudamMais() {
        assertNull(StatusPedido.ENTREGUE.getProximo());
        assertNull(StatusPedido.CANCELADO.getProximo());
    }

    @Test
    void cancelaSoAteASeparacao() {
        assertTrue(StatusPedido.AGUARDANDO_PAGAMENTO.isPodeCancelar());
        assertTrue(StatusPedido.PAGO.isPodeCancelar());
        assertTrue(StatusPedido.EM_SEPARACAO.isPodeCancelar());
        assertFalse(StatusPedido.ENVIADO.isPodeCancelar());
        assertFalse(StatusPedido.ENTREGUE.isPodeCancelar());
        assertFalse(StatusPedido.CANCELADO.isPodeCancelar());
    }

    @Test
    void cadaMudancaDeStatusEntraNaLinhaDoTempo() {
        Pedido pedido = new Pedido();
        pedido.mudarStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.mudarStatus(StatusPedido.PAGO);

        assertEquals(StatusPedido.PAGO, pedido.getStatus());
        assertEquals(2, pedido.getHistorico().size());
        assertEquals(StatusPedido.AGUARDANDO_PAGAMENTO, pedido.getHistorico().get(0).getStatus());
    }
}
