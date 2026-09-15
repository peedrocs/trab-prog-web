package com.mgp.lojaonline.controller.admin;

import com.mgp.lojaonline.model.Perfil;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioAdminController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAdminController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAllByOrderByNomeAsc());
        return "admin/usuarios/lista";
    }

    // troca o perfil entre USER e ADMIN
    @PostMapping("/{id}/perfil")
    public String alterarPerfil(@PathVariable Long id, Principal principal, RedirectAttributes atributos) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (usuario.getEmail().equals(principal.getName())) {
            atributos.addFlashAttribute("erro", "Você não pode alterar o próprio perfil");
        } else {
            usuario.setPerfil(usuario.getPerfil() == Perfil.ADMIN ? Perfil.USER : Perfil.ADMIN);
            usuarioRepository.save(usuario);
            atributos.addFlashAttribute("sucesso", "Perfil do usuário atualizado");
        }
        return "redirect:/admin/usuarios";
    }
}
