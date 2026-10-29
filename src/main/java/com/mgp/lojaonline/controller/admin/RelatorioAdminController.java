package com.mgp.lojaonline.controller.admin;

import com.mgp.lojaonline.dto.ProdutoVendido;
import com.mgp.lojaonline.service.RelatorioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/relatorios")
public class RelatorioAdminController {

    private final RelatorioService relatorioService;

    public RelatorioAdminController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    // sem datas no filtro, mostra os ultimos 30 dias
    @GetMapping
    public String relatorios(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
                             Model model) {
        LocalDate dataFim = fim == null ? LocalDate.now() : fim;
        LocalDate dataInicio = inicio == null ? dataFim.minusDays(29) : inicio;
        if (dataInicio.isAfter(dataFim)) {
            model.addAttribute("erro", "A data inicial precisa ser antes da data final");
            dataInicio = dataFim;
        }

        model.addAttribute("inicio", dataInicio);
        model.addAttribute("fim", dataFim);
        model.addAttribute("faturamento", relatorioService.faturamento(dataInicio, dataFim));
        model.addAttribute("porQuantidade", relatorioService.maisVendidosPorQuantidade(dataInicio, dataFim));
        model.addAttribute("porFaturamento", relatorioService.maisVendidosPorFaturamento(dataInicio, dataFim));
        return "admin/relatorios";
    }

    @GetMapping("/mais-vendidos.csv")
    public ResponseEntity<String> exportarCsv(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
                                              @RequestParam(defaultValue = "quantidade") String ordem) {
        List<ProdutoVendido> produtos = ordem.equals("faturamento")
                ? relatorioService.maisVendidosPorFaturamento(inicio, fim)
                : relatorioService.maisVendidosPorQuantidade(inicio, fim);

        String nomeDoArquivo = "mais-vendidos-por-" + ordem + "-" + inicio + "-a-" + fim + ".csv";
        // o caractere 0xFEFF (marca BOM) no inicio faz o Excel reconhecer os acentos
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeDoArquivo + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body((char) 0xFEFF + relatorioService.gerarCsv(produtos));
    }
}
