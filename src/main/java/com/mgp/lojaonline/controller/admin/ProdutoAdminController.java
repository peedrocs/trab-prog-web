package com.mgp.lojaonline.controller.admin;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.service.CategoriaService;
import com.mgp.lojaonline.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/produtos")
@RequiredArgsConstructor
public class ProdutoAdminController {

    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("produtos", produtoService.listarTodos());
        return "admin/produtos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        return abrirFormulario(new Produto(), model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return abrirFormulario(produtoService.buscarPorId(id), model);
    }

    // o Spring Data converte o id enviado pelo <select> de categoria em um objeto Categoria
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Produto produto, BindingResult resultado,
                         Model model, RedirectAttributes atributos) {
        if (resultado.hasErrors()) {
            return abrirFormulario(produto, model);
        }

        produtoService.salvar(produto);
        atributos.addFlashAttribute("sucesso", "Produto salvo com sucesso");
        return "redirect:/admin/produtos";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes atributos) {
        try {
            produtoService.excluir(id);
            atributos.addFlashAttribute("sucesso", "Produto excluído com sucesso");
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/produtos";
    }

    private String abrirFormulario(Produto produto, Model model) {
        model.addAttribute("produto", produto);
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "admin/produtos/formulario";
    }
}
