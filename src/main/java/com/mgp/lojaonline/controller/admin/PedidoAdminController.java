package com.mgp.lojaonline.controller.admin;

import com.mgp.lojaonline.model.Pedido;
import com.mgp.lojaonline.model.StatusPedido;
import com.mgp.lojaonline.repository.PedidoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/pedidos")
public class PedidoAdminController {

    private final PedidoRepository pedidoRepository;

    public PedidoAdminController(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) StatusPedido status, Model model) {
        model.addAttribute("pedidos", status == null
                ? pedidoRepository.findAllByOrderByDataDesc()
                : pedidoRepository.findByStatusOrderByDataDesc(status));
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("listaStatus", StatusPedido.values());
        return "admin/pedidos/lista";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("pedido", buscar(id));
        model.addAttribute("listaStatus", StatusPedido.values());
        return "admin/pedidos/detalhe";
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id, @RequestParam StatusPedido status,
                                RedirectAttributes atributos) {
        Pedido pedido = buscar(id);
        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            atributos.addFlashAttribute("erro", "Pedido cancelado não muda mais de status");
        } else {
            pedido.setStatus(status);
            pedidoRepository.save(pedido);
            atributos.addFlashAttribute("sucesso", "Status do pedido atualizado");
        }
        return "redirect:/admin/pedidos/" + id;
    }

    private Pedido buscar(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
