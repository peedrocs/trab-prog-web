package com.mgp.lojaonline.controller.admin;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
public class UsuarioAdminController {

    private final UsuarioService usuarioService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "admin/usuarios/lista";
    }

    @PostMapping("/{id}/perfil")
    public String alterarPerfil(@PathVariable Long id, Principal principal, RedirectAttributes atributos) {
        try {
            usuarioService.alterarPerfil(id, principal.getName());
            atributos.addFlashAttribute("sucesso", "Perfil do usuário atualizado");
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/ativo")
    public String alterarAtivo(@PathVariable Long id, Principal principal, RedirectAttributes atributos) {
        try {
            usuarioService.alterarAtivo(id, principal.getName());
            atributos.addFlashAttribute("sucesso", "Situação do usuário atualizada");
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}
