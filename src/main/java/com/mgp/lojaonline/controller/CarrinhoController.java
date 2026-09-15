package com.mgp.lojaonline.controller;

import com.mgp.lojaonline.model.ItemCarrinho;
import com.mgp.lojaonline.repository.ProdutoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/carrinho")
public class CarrinhoController {

    private final ProdutoRepository produtoRepository;

    public CarrinhoController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @GetMapping
    public String carrinho(HttpSession sessao, Model model) {
        List<ItemCarrinho> itens = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entrada : carrinho(sessao).entrySet()) {
            produtoRepository.findById(entrada.getKey())
                    .ifPresent(produto -> itens.add(new ItemCarrinho(produto, entrada.getValue())));
        }
        for (ItemCarrinho item : itens) {
            total = total.add(item.getSubtotal());
        }

        model.addAttribute("itens", itens);
        model.addAttribute("total", total);
        return "carrinho";
    }

    @PostMapping("/adicionar")
    public String adicionar(@RequestParam Long produtoId, @RequestParam(defaultValue = "1") int quantidade,
                            HttpSession sessao) {
        carrinho(sessao).merge(produtoId, quantidade, Integer::sum);
        return "redirect:/carrinho";
    }

    @PostMapping("/atualizar")
    public String atualizar(@RequestParam Long produtoId, @RequestParam int quantidade, HttpSession sessao) {
        carrinho(sessao).put(produtoId, quantidade);
        return "redirect:/carrinho";
    }

    @PostMapping("/remover")
    public String remover(@RequestParam Long produtoId, HttpSession sessao) {
        carrinho(sessao).remove(produtoId);
        return "redirect:/carrinho";
    }

    // o carrinho e um mapa produto -> quantidade guardado na sessao
    static Map<Long, Integer> carrinho(HttpSession sessao) {
        Map<Long, Integer> carrinho = (Map<Long, Integer>) sessao.getAttribute("carrinho");
        if (carrinho == null) {
            carrinho = new LinkedHashMap<>();
            sessao.setAttribute("carrinho", carrinho);
        }
        return carrinho;
    }
}
