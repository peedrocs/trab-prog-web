package com.mgp.lojaonline.controller.admin;

import com.mgp.lojaonline.service.PagamentoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PagamentoAdminController {

    private final PagamentoService pagamentoService;

    public PagamentoAdminController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @GetMapping("/admin/pagamentos")
    public String listar(Model model) {
        model.addAttribute("pagamentos", pagamentoService.listar());
        return "admin/pagamentos/lista";
    }
}
