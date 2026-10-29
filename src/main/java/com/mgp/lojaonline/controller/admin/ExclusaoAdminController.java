package com.mgp.lojaonline.controller.admin;

import com.mgp.lojaonline.dto.TipoRegistro;
import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.service.ExclusaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/admin/exclusao")
public class ExclusaoAdminController {

    private final ExclusaoService exclusaoService;

    public ExclusaoAdminController(ExclusaoService exclusaoService) {
        this.exclusaoService = exclusaoService;
    }

    @GetMapping
    public String exclusao(@RequestParam(required = false) TipoRegistro tipo,
                           @RequestParam(defaultValue = "") String busca,
                           @RequestParam(required = false) Long id, Principal principal, Model model) {
        model.addAttribute("tipos", TipoRegistro.values());
        model.addAttribute("tipo", tipo);
        model.addAttribute("busca", busca);

        if (tipo != null) {
            model.addAttribute("resultados", exclusaoService.buscar(tipo, busca, principal.getName()));
            if (id != null) {
                try {
                    model.addAttribute("selecionado", exclusaoService.detalhar(tipo, id, principal.getName()));
                } catch (RegraNegocioException e) {
                    model.addAttribute("erro", e.getMessage());
                }
            }
        }
        return "admin/exclusao";
    }

    @PostMapping("/excluir")
    public String excluir(@RequestParam TipoRegistro tipo, @RequestParam Long id,
                          @RequestParam(defaultValue = "") String busca, Principal principal,
                          RedirectAttributes atributos) {
        try {
            exclusaoService.excluir(tipo, id, principal.getName());
            atributos.addFlashAttribute("sucesso", "Registro excluído com sucesso");
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
        }
        return voltar(tipo, busca, atributos);
    }

    @PostMapping("/desativar")
    public String desativar(@RequestParam TipoRegistro tipo, @RequestParam Long id,
                            @RequestParam(defaultValue = "") String busca, Principal principal,
                            RedirectAttributes atributos) {
        try {
            exclusaoService.desativar(tipo, id, principal.getName());
            atributos.addFlashAttribute("sucesso", "Registro desativado com sucesso");
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
        }
        return voltar(tipo, busca, atributos);
    }

    private String voltar(TipoRegistro tipo, String busca, RedirectAttributes atributos) {
        atributos.addAttribute("tipo", tipo);
        atributos.addAttribute("busca", busca);
        return "redirect:/admin/exclusao";
    }
}
