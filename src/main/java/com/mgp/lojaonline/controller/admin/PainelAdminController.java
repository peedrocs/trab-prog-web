package com.mgp.lojaonline.controller.admin;

import com.mgp.lojaonline.model.StatusPedido;
import com.mgp.lojaonline.service.PedidoService;
import com.mgp.lojaonline.service.ProdutoService;
import com.mgp.lojaonline.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class PainelAdminController {

    private final ProdutoService produtoService;
    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String painel(Model model) {
        model.addAttribute("totalProdutos", produtoService.contar());
        model.addAttribute("produtosSemEstoque", produtoService.contarSemEstoque());
        model.addAttribute("pedidosAguardando", pedidoService.contarPorStatus(StatusPedido.AGUARDANDO_PAGAMENTO));
        model.addAttribute("totalUsuarios", usuarioService.contar());
        return "admin/painel";
    }
}
