package com.mgp.lojaonline.controller;

import com.mgp.lojaonline.repository.ProdutoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CatalogoController {

    private final ProdutoRepository produtoRepository;

    public CatalogoController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @GetMapping("/")
    public String catalogo(Model model) {
        model.addAttribute("produtos", produtoRepository.findAllByOrderByNomeAsc());
        return "index";
    }
}
