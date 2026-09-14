package com.mgp.lojaonline.controller;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Pedido;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.service.PedidoService;
import com.mgp.lojaonline.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String meusPedidos(Principal principal, Model model) {
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName());
        model.addAttribute("pedidos", pedidoService.listarDoUsuario(usuario));
        return "pedidos/lista";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Principal principal, Model model) {
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName());
        model.addAttribute("pedido", pedidoService.buscarDoUsuario(id, usuario));
        return "pedidos/detalhe";
    }

    @GetMapping("/checkout")
    public String checkout(@RequestParam(required = false) String cupom, Principal principal,
                           Model model, RedirectAttributes atributos) {
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName());
        try {
            model.addAttribute("pedido", pedidoService.montarPedido(usuario, cupom));
            return "pedidos/checkout";
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
            return "redirect:/carrinho";
        }
    }

    @PostMapping("/finalizar")
    public String finalizar(@RequestParam(required = false) String cupom, Principal principal,
                            RedirectAttributes atributos) {
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName());
        try {
            Pedido pedido = pedidoService.finalizar(usuario, cupom);
            atributos.addFlashAttribute("sucesso", "Pedido realizado com sucesso!");
            return "redirect:/pedidos/" + pedido.getId();
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
            return "redirect:/carrinho";
        }
    }
}
