package com.mgp.lojaonline.controller;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AutenticacaoController {

    private final UsuarioService usuarioService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute Usuario usuario, BindingResult resultado,
                            Model model, RedirectAttributes atributos) {
        if (resultado.hasErrors()) {
            return "cadastro";
        }

        try {
            usuarioService.cadastrar(usuario);
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return "cadastro";
        }

        atributos.addFlashAttribute("sucesso", "Cadastro realizado! Entre com seu e-mail e senha.");
        return "redirect:/login";
    }
}
