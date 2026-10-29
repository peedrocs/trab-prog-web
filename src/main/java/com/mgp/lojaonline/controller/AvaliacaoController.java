package com.mgp.lojaonline.controller;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.service.AvaliacaoService;
import com.mgp.lojaonline.service.ProdutoService;
import com.mgp.lojaonline.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;
    private final ProdutoService produtoService;
    private final UsuarioService usuarioService;

    public AvaliacaoController(AvaliacaoService avaliacaoService, ProdutoService produtoService,
                               UsuarioService usuarioService) {
        this.avaliacaoService = avaliacaoService;
        this.produtoService = produtoService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/avaliacoes")
    public String avaliar(@RequestParam Long produtoId, @RequestParam int nota,
                          @RequestParam(required = false) String comentario, Principal principal,
                          RedirectAttributes atributos) {
        Produto produto = produtoService.buscarNaLoja(produtoId);
        try {
            avaliacaoService.avaliar(usuarioService.buscarPorEmail(principal.getName()), produto, nota, comentario);
            atributos.addFlashAttribute("sucesso", "Obrigado pela avaliação!");
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/produtos/" + produtoId;
    }
}
