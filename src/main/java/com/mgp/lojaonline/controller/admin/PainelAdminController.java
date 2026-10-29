package com.mgp.lojaonline.controller.admin;

import com.mgp.lojaonline.service.RelatorioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class PainelAdminController {

    private final RelatorioService relatorioService;

    public PainelAdminController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/admin")
    public String painel(Model model) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioDoMes = hoje.withDayOfMonth(1);

        model.addAttribute("faturamentoHoje", relatorioService.faturamento(hoje, hoje));
        model.addAttribute("faturamentoMes", relatorioService.faturamento(inicioDoMes, hoje));
        model.addAttribute("ticketMedio", relatorioService.ticketMedio(inicioDoMes, hoje));
        model.addAttribute("pedidosPorStatus", relatorioService.pedidosPorStatus());
        model.addAttribute("estoqueBaixo", relatorioService.estoqueBaixo());
        model.addAttribute("limiteEstoque", RelatorioService.LIMITE_ESTOQUE_BAIXO);
        return "admin/painel";
    }
}
