package com.mgp.lojaonline.controller.admin;

import com.mgp.lojaonline.model.Categoria;
import com.mgp.lojaonline.repository.CategoriaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categorias")
public class CategoriaAdminController {

    private final CategoriaRepository categoriaRepository;

    public CategoriaAdminController(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaRepository.findAllByOrderByNomeAsc());
        return "admin/categorias/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "admin/categorias/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("categoria", buscar(id));
        return "admin/categorias/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Categoria categoria, RedirectAttributes atributos) {
        categoriaRepository.save(categoria);
        atributos.addFlashAttribute("sucesso", "Categoria salva com sucesso");
        return "redirect:/admin/categorias";
    }

    @GetMapping("/{id}/excluir")
    public String confirmarExclusao(@PathVariable Long id, Model model) {
        model.addAttribute("categoria", buscar(id));
        return "admin/categorias/excluir";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes atributos) {
        try {
            categoriaRepository.deleteById(id);
            atributos.addFlashAttribute("sucesso", "Categoria excluída com sucesso");
        } catch (DataIntegrityViolationException e) {
            atributos.addFlashAttribute("erro", "Categoria que tem produtos não pode ser excluída");
        }
        return "redirect:/admin/categorias";
    }

    private Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
