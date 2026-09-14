package com.mgp.lojaonline.controller;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.service.CarrinhoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/carrinho")
@RequiredArgsConstructor
public class CarrinhoController {

    private final CarrinhoService carrinhoService;

    @GetMapping
    public String carrinho(Model model) {
        BigDecimal subtotal = carrinhoService.calcularSubtotal();
        BigDecimal frete = carrinhoService.calcularFrete();

        model.addAttribute("itens", carrinhoService.listarItens());
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("frete", frete);
        model.addAttribute("total", subtotal.add(frete));
        return "carrinho";
    }

    @PostMapping("/adicionar")
    public String adicionar(@RequestParam Long produtoId,
                            @RequestParam(defaultValue = "1") int quantidade,
                            RedirectAttributes atributos) {
        try {
            carrinhoService.adicionar(produtoId, quantidade);
            atributos.addFlashAttribute("sucesso", "Produto adicionado ao carrinho");
            return "redirect:/carrinho";
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
            return "redirect:/produtos/" + produtoId;
        }
    }

    @PostMapping("/atualizar")
    public String atualizar(@RequestParam Long produtoId, @RequestParam int quantidade,
                            RedirectAttributes atributos) {
        try {
            carrinhoService.atualizar(produtoId, quantidade);
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/carrinho";
    }

    @PostMapping("/remover")
    public String remover(@RequestParam Long produtoId) {
        carrinhoService.remover(produtoId);
        return "redirect:/carrinho";
    }
}
