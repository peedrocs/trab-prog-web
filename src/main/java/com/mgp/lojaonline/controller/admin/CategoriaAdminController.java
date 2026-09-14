package com.mgp.lojaonline.controller.admin;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Categoria;
import com.mgp.lojaonline.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categorias")
@RequiredArgsConstructor
public class CategoriaAdminController {

    private final CategoriaService categoriaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "admin/categorias/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "admin/categorias/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("categoria", categoriaService.buscarPorId(id));
        return "admin/categorias/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Categoria categoria, BindingResult resultado,
                         Model model, RedirectAttributes atributos) {
        if (resultado.hasErrors()) {
            return "admin/categorias/formulario";
        }

        try {
            categoriaService.salvar(categoria);
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return "admin/categorias/formulario";
        }

        atributos.addFlashAttribute("sucesso", "Categoria salva com sucesso");
        return "redirect:/admin/categorias";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes atributos) {
        try {
            categoriaService.excluir(id);
            atributos.addFlashAttribute("sucesso", "Categoria excluída com sucesso");
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/categorias";
    }
}
