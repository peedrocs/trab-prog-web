package com.mgp.lojaonline.dto;

import com.mgp.lojaonline.model.Endereco;
import com.mgp.lojaonline.model.FormaPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class DadosCheckout {

    @Valid
    private Endereco endereco = new Endereco();

    @NotNull(message = "Escolha a forma de pagamento")
    private FormaPagamento formaPagamento;

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }
}
