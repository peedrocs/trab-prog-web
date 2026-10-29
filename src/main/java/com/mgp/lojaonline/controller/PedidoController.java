package com.mgp.lojaonline.controller;

import com.mgp.lojaonline.model.Pedido;
import com.mgp.lojaonline.model.StatusPedido;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.service.PagamentoService;
import com.mgp.lojaonline.service.PedidoService;
import com.mgp.lojaonline.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final PagamentoService pagamentoService;
    private final UsuarioService usuarioService;

    public PedidoController(PedidoService pedidoService, PagamentoService pagamentoService,
                            UsuarioService usuarioService) {
        this.pedidoService = pedidoService;
        this.pagamentoService = pagamentoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String meusPedidos(Principal principal, Model model) {
        model.addAttribute("pedidos", pedidoService.listarDoCliente(usuarioLogado(principal)));
        return "pedidos/lista";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("pedido", pedidoService.buscarDoCliente(id, usuarioLogado(principal)));
        return "pedidos/detalhe";
    }

    @GetMapping("/{id}/pagamento")
    public String pagamento(@PathVariable Long id, Principal principal, Model model) {
        Pedido pedido = pedidoService.buscarDoCliente(id, usuarioLogado(principal));
        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            return "redirect:/pedidos/" + id;
        }
        model.addAttribute("pedido", pedido);
        model.addAttribute("pagamento", pagamentoService.ultimoDoPedido(pedido));
        return "pedidos/pagamento";
    }

    // a pagina do Pix pergunta aqui a cada poucos segundos se o pagamento ja entrou
    @GetMapping("/{id}/status")
    @ResponseBody
    public Map<String, String> status(@PathVariable Long id, Principal principal) {
        Pedido pedido = pedidoService.buscarDoCliente(id, usuarioLogado(principal));
        return Map.of("status", pedido.getStatus().name());
    }

    private Usuario usuarioLogado(Principal principal) {
        return usuarioService.buscarPorEmail(principal.getName());
    }
}
