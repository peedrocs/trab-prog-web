package com.mgp.lojaonline.controller;

import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.repository.CategoriaRepository;
import com.mgp.lojaonline.service.AvaliacaoService;
import com.mgp.lojaonline.service.ProdutoService;
import com.mgp.lojaonline.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class CatalogoController {

    private final ProdutoService produtoService;
    private final CategoriaRepository categoriaRepository;
    private final AvaliacaoService avaliacaoService;
    private final UsuarioService usuarioService;

    public CatalogoController(ProdutoService produtoService, CategoriaRepository categoriaRepository,
                              AvaliacaoService avaliacaoService, UsuarioService usuarioService) {
        this.produtoService = produtoService;
        this.categoriaRepository = categoriaRepository;
        this.avaliacaoService = avaliacaoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/")
    public String catalogo(@RequestParam(defaultValue = "") String busca,
                           @RequestParam(required = false) Long categoriaId,
                           @RequestParam(defaultValue = "nome") String ordem,
                           @RequestParam(defaultValue = "1") int pagina, Model model) {
        model.addAttribute("produtos", produtoService.buscarNaLoja(busca, categoriaId, ordem, pagina));
        model.addAttribute("categorias", categoriaRepository.findByAtivaTrueOrderByNomeAsc());
        model.addAttribute("busca", busca);
        model.addAttribute("categoriaSelecionada", categoriaId);
        model.addAttribute("ordem", ordem);
        return "index";
    }

    @GetMapping("/produtos/{id}")
    public String detalhe(@PathVariable Long id, Principal principal, Model model) {
        Produto produto = produtoService.buscarNaLoja(id);
        Usuario usuario = usuarioService.usuarioLogado(principal);

        model.addAttribute("produto", produto);
        model.addAttribute("avaliacoes", avaliacaoService.listar(produto));
        model.addAttribute("media", avaliacaoService.media(produto));
        model.addAttribute("podeAvaliar", avaliacaoService.podeAvaliar(usuario, produto));
        return "produtos/detalhe";
    }
}
