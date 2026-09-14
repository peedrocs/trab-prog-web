package com.mgp.lojaonline.controller.admin;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Cupom;
import com.mgp.lojaonline.model.TipoDesconto;
import com.mgp.lojaonline.service.CupomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/cupons")
@RequiredArgsConstructor
public class CupomAdminController {

    private final CupomService cupomService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("cupons", cupomService.listarTodos());
        return "admin/cupons/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        return abrirFormulario(new Cupom(), model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return abrirFormulario(cupomService.buscarPorId(id), model);
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Cupom cupom, BindingResult resultado,
                         Model model, RedirectAttributes atributos) {
        if (resultado.hasErrors()) {
            return abrirFormulario(cupom, model);
        }

        try {
            cupomService.salvar(cupom);
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return abrirFormulario(cupom, model);
        }

        atributos.addFlashAttribute("sucesso", "Cupom salvo com sucesso");
        return "redirect:/admin/cupons";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes atributos) {
        try {
            cupomService.excluir(id);
            atributos.addFlashAttribute("sucesso", "Cupom excluído com sucesso");
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/cupons";
    }

    private String abrirFormulario(Cupom cupom, Model model) {
        model.addAttribute("cupom", cupom);
        model.addAttribute("tipos", TipoDesconto.values());
        return "admin/cupons/formulario";
    }
}
