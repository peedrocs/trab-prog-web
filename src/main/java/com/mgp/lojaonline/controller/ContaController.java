package com.mgp.lojaonline.controller;

import com.mgp.lojaonline.dto.DadosConta;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/conta")
public class ContaController {

    private final UsuarioService usuarioService;

    public ContaController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String minhaConta(Principal principal, Model model) {
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName());
        model.addAttribute("usuario", usuario);
        model.addAttribute("dados", usuarioService.dadosDaConta(usuario));
        return "conta";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("dados") DadosConta dados, BindingResult resultado,
                         Principal principal, Model model, RedirectAttributes atributos) {
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName());
        if (resultado.hasErrors()) {
            model.addAttribute("usuario", usuario);
            return "conta";
        }

        usuarioService.atualizarConta(usuario, dados);
        atributos.addFlashAttribute("sucesso", "Seus dados foram atualizados");
        return "redirect:/conta";
    }
}
