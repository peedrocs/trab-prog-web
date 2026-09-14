package com.mgp.lojaonline.controller.admin;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.StatusPedido;
import com.mgp.lojaonline.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/pedidos")
@RequiredArgsConstructor
public class PedidoAdminController {

    private final PedidoService pedidoService;

    @GetMapping
    public String listar(@RequestParam(required = false) StatusPedido status, Model model) {
        model.addAttribute("pedidos", pedidoService.listarPorStatus(status));
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("listaStatus", StatusPedido.values());
        return "admin/pedidos/lista";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("pedido", pedidoService.buscarPorId(id));
        model.addAttribute("listaStatus", StatusPedido.values());
        return "admin/pedidos/detalhe";
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id, @RequestParam StatusPedido status,
                                RedirectAttributes atributos) {
        try {
            pedidoService.alterarStatus(id, status);
            atributos.addFlashAttribute("sucesso", "Status do pedido atualizado");
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/pedidos/" + id;
    }
}
