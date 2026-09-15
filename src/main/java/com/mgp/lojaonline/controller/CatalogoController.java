package com.mgp.lojaonline.controller;

import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.repository.CategoriaRepository;
import com.mgp.lojaonline.repository.ProdutoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class CatalogoController {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;

    public CatalogoController(ProdutoRepository produtoRepository, CategoriaRepository categoriaRepository) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping("/")
    public String catalogo(@RequestParam(defaultValue = "") String busca,
                           @RequestParam(required = false) Long categoriaId, Model model) {
        List<Produto> produtos = categoriaId == null
                ? produtoRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc(busca)
                : produtoRepository.findByCategoriaIdAndNomeContainingIgnoreCaseOrderByNomeAsc(categoriaId, busca);

        model.addAttribute("produtos", produtos);
        model.addAttribute("categorias", categoriaRepository.findAllByOrderByNomeAsc());
        model.addAttribute("busca", busca);
        model.addAttribute("categoriaSelecionada", categoriaId);
        return "index";
    }

    @GetMapping("/produtos/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("produto", produto);
        return "produtos/detalhe";
    }
}
