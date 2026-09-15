package com.mgp.lojaonline.controller.admin;

import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.repository.CategoriaRepository;
import com.mgp.lojaonline.repository.ProdutoRepository;
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
@RequestMapping("/admin/produtos")
public class ProdutoAdminController {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProdutoAdminController(ProdutoRepository produtoRepository, CategoriaRepository categoriaRepository) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("produtos", produtoRepository.findAllByOrderByNomeAsc());
        return "admin/produtos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        return abrirFormulario(new Produto(), model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return abrirFormulario(buscar(id), model);
    }

    // o id enviado pelo select de categoria vira um objeto Categoria
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Produto produto, RedirectAttributes atributos) {
        if (produto.getImagemUrl() != null && produto.getImagemUrl().isBlank()) {
            produto.setImagemUrl(null);
        }
        produtoRepository.save(produto);
        atributos.addFlashAttribute("sucesso", "Produto salvo com sucesso");
        return "redirect:/admin/produtos";
    }

    @GetMapping("/{id}/excluir")
    public String confirmarExclusao(@PathVariable Long id, Model model) {
        model.addAttribute("produto", buscar(id));
        return "admin/produtos/excluir";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes atributos) {
        try {
            produtoRepository.deleteById(id);
            atributos.addFlashAttribute("sucesso", "Produto excluído com sucesso");
        } catch (DataIntegrityViolationException e) {
            atributos.addFlashAttribute("erro", "Produto que já foi vendido não pode ser excluído. Deixe o estoque em zero.");
        }
        return "redirect:/admin/produtos";
    }

    private String abrirFormulario(Produto produto, Model model) {
        model.addAttribute("produto", produto);
        model.addAttribute("categorias", categoriaRepository.findAllByOrderByNomeAsc());
        return "admin/produtos/formulario";
    }

    private Produto buscar(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
