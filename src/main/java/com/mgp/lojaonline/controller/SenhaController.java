package com.mgp.lojaonline.controller;

import com.mgp.lojaonline.dto.DadosNovaSenha;
import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/senha")
public class SenhaController {

    private final UsuarioService usuarioService;

    public SenhaController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/esqueci")
    public String esqueci() {
        return "senha/esqueci";
    }

    // a resposta e a mesma tendo conta ou nao, para ninguem descobrir quais e-mails sao cadastrados
    @PostMapping("/esqueci")
    public String enviarLink(@RequestParam String email, RedirectAttributes atributos) {
        usuarioService.enviarLinkDeSenha(email);
        atributos.addFlashAttribute("sucesso",
                "Se este e-mail tiver conta na loja, você vai receber um link para criar uma senha nova.");
        return "redirect:/login";
    }

    @GetMapping("/redefinir")
    public String redefinir(@RequestParam(required = false) String token, Model model, RedirectAttributes atributos) {
        try {
            usuarioService.buscarPorTokenDeSenha(token);
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
            return "redirect:/senha/esqueci";
        }
        DadosNovaSenha dados = new DadosNovaSenha();
        dados.setToken(token);
        model.addAttribute("dados", dados);
        return "senha/redefinir";
    }

    @PostMapping("/redefinir")
    public String salvarSenha(@Valid @ModelAttribute("dados") DadosNovaSenha dados, BindingResult resultado,
                              RedirectAttributes atributos) {
        if (!resultado.hasFieldErrors("confirmacao") && !dados.getConfirmacao().equals(dados.getSenha())) {
            resultado.rejectValue("confirmacao", "senha.diferente", "As duas senhas não são iguais");
        }
        if (resultado.hasErrors()) {
            return "senha/redefinir";
        }

        try {
            usuarioService.redefinirSenha(dados.getToken(), dados.getSenha());
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
            return "redirect:/senha/esqueci";
        }
        atributos.addFlashAttribute("sucesso", "Senha alterada! Entre com a senha nova.");
        return "redirect:/login";
    }
}
