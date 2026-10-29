package com.mgp.lojaonline.model;

public enum FormaPagamento {
    PIX("Pix"),
    CARTAO("Cartão de crédito"),
    BOLETO("Boleto");

    private final String descricao;

    FormaPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    // o Mercado Pago informa o tipo do pagamento com estes nomes
    public static FormaPagamento doTipoMercadoPago(String tipo) {
        return switch (tipo) {
            case "bank_transfer" -> PIX;
            case "ticket" -> BOLETO;
            default -> CARTAO;
        };
    }
}
