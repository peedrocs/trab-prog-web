package com.mgp.lojaonline.controller;

import com.mgp.lojaonline.model.Perfil;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AutenticacaoController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder codificadorDeSenha;

    public AutenticacaoController(UsuarioRepository usuarioRepository, PasswordEncoder codificadorDeSenha) {
        this.usuarioRepository = usuarioRepository;
        this.codificadorDeSenha = codificadorDeSenha;
    }

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
    public String cadastrar(@ModelAttribute Usuario usuario, Model model, RedirectAttributes atributos) {
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            model.addAttribute("erro", "Já existe uma conta com este e-mail");
            return "cadastro";
        }

        usuario.setSenha(codificadorDeSenha.encode(usuario.getSenha()));
        usuario.setPerfil(Perfil.USER);
        usuarioRepository.save(usuario);

        atributos.addFlashAttribute("sucesso", "Cadastro realizado! Entre com seu e-mail e senha.");
        return "redirect:/login";
    }
}
