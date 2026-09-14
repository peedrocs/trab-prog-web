package com.mgp.lojaonline.controller;

import com.mgp.lojaonline.model.Avaliacao;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.service.AvaliacaoService;
import com.mgp.lojaonline.service.CategoriaService;
import com.mgp.lojaonline.service.ProdutoService;
import com.mgp.lojaonline.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CatalogoController {

    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;
    private final AvaliacaoService avaliacaoService;
    private final UsuarioService usuarioService;

    @GetMapping("/")
    public String catalogo(@RequestParam(required = false) String busca,
                           @RequestParam(required = false) Long categoriaId,
                           Model model) {
        model.addAttribute("produtos", produtoService.buscar(busca, categoriaId));
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("busca", busca);
        model.addAttribute("categoriaSelecionada", categoriaId);
        return "index";
    }

    @GetMapping("/produtos/{id}")
    public String detalhe(@PathVariable Long id, Principal principal, Model model) {
        Produto produto = produtoService.buscarPorId(id);
        List<Avaliacao> avaliacoes = avaliacaoService.listarDoProduto(produto);

        boolean podeAvaliar = false;
        if (principal != null) {
            Usuario usuario = usuarioService.buscarPorEmail(principal.getName());
            podeAvaliar = avaliacaoService.podeAvaliar(usuario, produto);
        }

        model.addAttribute("produto", produto);
        model.addAttribute("avaliacoes", avaliacoes);
        model.addAttribute("media", avaliacaoService.calcularMedia(avaliacoes));
        model.addAttribute("podeAvaliar", podeAvaliar);
        return "produtos/detalhe";
    }
}
