package com.mgp.lojaonline.controller.admin;

import com.mgp.lojaonline.model.StatusPedido;
import com.mgp.lojaonline.repository.CategoriaRepository;
import com.mgp.lojaonline.repository.PedidoRepository;
import com.mgp.lojaonline.repository.ProdutoRepository;
import com.mgp.lojaonline.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PainelAdminController {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;

    public PainelAdminController(ProdutoRepository produtoRepository, CategoriaRepository categoriaRepository,
                                 PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/admin")
    public String painel(Model model) {
        model.addAttribute("totalProdutos", produtoRepository.count());
        model.addAttribute("totalCategorias", categoriaRepository.count());
        model.addAttribute("pedidosAguardando",
                pedidoRepository.findByStatusOrderByDataDesc(StatusPedido.AGUARDANDO_PAGAMENTO).size());
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        return "admin/painel";
    }
}
