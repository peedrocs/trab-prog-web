package com.mgp.lojaonline.controller.admin;

import com.mgp.lojaonline.model.StatusPedido;
import com.mgp.lojaonline.repository.PedidoRepository;
import com.mgp.lojaonline.repository.ProdutoRepository;
import com.mgp.lojaonline.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PainelAdminController {

    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;

    public PainelAdminController(ProdutoRepository produtoRepository, PedidoRepository pedidoRepository,
                                 UsuarioRepository usuarioRepository) {
        this.produtoRepository = produtoRepository;
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/admin")
    public String painel(Model model) {
        model.addAttribute("totalProdutos", produtoRepository.count());
        model.addAttribute("produtosSemEstoque", produtoRepository.countByEstoque(0));
        model.addAttribute("pedidosAguardando",
                pedidoRepository.findByStatusOrderByDataDesc(StatusPedido.AGUARDANDO_PAGAMENTO).size());
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        return "admin/painel";
    }
}
