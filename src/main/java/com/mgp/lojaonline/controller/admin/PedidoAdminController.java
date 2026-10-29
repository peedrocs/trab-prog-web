package com.mgp.lojaonline.controller.admin;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.StatusPedido;
import com.mgp.lojaonline.service.PedidoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/pedidos")
public class PedidoAdminController {

    private final PedidoService pedidoService;

    public PedidoAdminController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) StatusPedido status, Model model) {
        model.addAttribute("pedidos", pedidoService.listar(status));
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("listaStatus", StatusPedido.values());
        return "admin/pedidos/lista";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("pedido", pedidoService.buscar(id));
        return "admin/pedidos/detalhe";
    }

    @PostMapping("/{id}/avancar")
    public String avancar(@PathVariable Long id, @RequestParam(required = false) String codigoRastreio,
                          RedirectAttributes atributos) {
        try {
            pedidoService.avancarStatus(id, codigoRastreio);
            atributos.addFlashAttribute("sucesso", "Status do pedido atualizado");
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/pedidos/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes atributos) {
        try {
            pedidoService.cancelar(id);
            atributos.addFlashAttribute("sucesso", "Pedido cancelado. Os produtos voltaram para o estoque.");
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/pedidos/" + id;
    }
}
