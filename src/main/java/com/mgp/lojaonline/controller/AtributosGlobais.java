package com.mgp.lojaonline.controller;

import com.mgp.lojaonline.service.CarrinhoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// disponibiliza a quantidade do carrinho para o cabeçalho de todas as páginas
@ControllerAdvice
@RequiredArgsConstructor
public class AtributosGlobais {

    private final CarrinhoService carrinhoService;

    @ModelAttribute("quantidadeCarrinho")
    public int quantidadeCarrinho() {
        return carrinhoService.contarItens();
    }
}
