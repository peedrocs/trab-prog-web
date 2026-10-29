package com.mgp.lojaonline.controller.admin;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.repository.CategoriaRepository;
import com.mgp.lojaonline.repository.ProdutoRepository;
import com.mgp.lojaonline.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/produtos")
public class ProdutoAdminController {

    private final ProdutoService produtoService;
    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProdutoAdminController(ProdutoService produtoService, ProdutoRepository produtoRepository,
                                  CategoriaRepository categoriaRepository) {
        this.produtoService = produtoService;
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
        return abrirFormulario(produtoService.buscar(id), model);
    }

    // o id enviado pelo select de categoria vira um objeto Categoria
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Produto produto, BindingResult resultado,
                         @RequestParam(required = false) MultipartFile imagem, Model model,
                         RedirectAttributes atributos) {
        if (resultado.hasErrors()) {
            return abrirFormulario(produto, model);
        }

        try {
            produtoService.salvar(produto, imagem);
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return abrirFormulario(produto, model);
        }
        atributos.addFlashAttribute("sucesso", "Produto salvo com sucesso");
        return "redirect:/admin/produtos";
    }

    private String abrirFormulario(Produto produto, Model model) {
        model.addAttribute("produto", produto);
        model.addAttribute("categorias", categoriaRepository.findAllByOrderByNomeAsc());
        return "admin/produtos/formulario";
    }
}
