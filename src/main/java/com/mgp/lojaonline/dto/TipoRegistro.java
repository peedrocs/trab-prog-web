package com.mgp.lojaonline.dto;

public enum TipoRegistro {
    PRODUTO("Produto"),
    CATEGORIA("Categoria"),
    CUPOM("Cupom"),
    USUARIO("Usuário");

    private final String descricao;

    TipoRegistro(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
