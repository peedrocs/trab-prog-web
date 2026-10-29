package com.mgp.lojaonline.model;

import java.util.List;

public enum StatusPedido {
    AGUARDANDO_PAGAMENTO("Aguardando pagamento"),
    PAGO("Pago"),
    EM_SEPARACAO("Em separação"),
    ENVIADO("Enviado"),
    ENTREGUE("Entregue"),
    CANCELADO("Cancelado");

    public static final List<StatusPedido> PAGOS = List.of(PAGO, EM_SEPARACAO, ENVIADO, ENTREGUE);

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    // ordem que o admin segue depois do pagamento; o pagamento em si so o Mercado Pago confirma
    public StatusPedido getProximo() {
        return switch (this) {
            case PAGO -> EM_SEPARACAO;
            case EM_SEPARACAO -> ENVIADO;
            case ENVIADO -> ENTREGUE;
            default -> null;
        };
    }

    public boolean isPodeCancelar() {
        return this == AGUARDANDO_PAGAMENTO || this == PAGO || this == EM_SEPARACAO;
    }

    public boolean isPago() {
        return PAGOS.contains(this);
    }
}
