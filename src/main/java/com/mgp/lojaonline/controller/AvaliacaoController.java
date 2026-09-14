package com.mgp.lojaonline.controller;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.service.AvaliacaoService;
import com.mgp.lojaonline.service.ProdutoService;
import com.mgp.lojaonline.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;
    private final ProdutoService produtoService;
    private final UsuarioService usuarioService;

    @PostMapping("/avaliacoes")
    public String avaliar(@RequestParam Long produtoId,
                          @RequestParam(required = false) Integer nota,
                          @RequestParam(required = false) String comentario,
                          Principal principal,
                          RedirectAttributes atributos) {
        Produto produto = produtoService.buscarPorId(produtoId);
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName());

        try {
            avaliacaoService.avaliar(usuario, produto, nota, comentario);
            atributos.addFlashAttribute("sucesso", "Obrigado pela sua avaliação!");
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/produtos/" + produtoId;
    }
}
