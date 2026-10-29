package com.mgp.lojaonline.service;

import com.mgp.lojaonline.dto.ProdutoVendido;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.model.StatusPedido;
import com.mgp.lojaonline.repository.ItemPedidoRepository;
import com.mgp.lojaonline.repository.PedidoRepository;
import com.mgp.lojaonline.repository.ProdutoRepository;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// os relatorios saem de consultas no proprio banco, somando os itens dos pedidos pagos
@Service
public class RelatorioService {

    public static final int LIMITE_ESTOQUE_BAIXO = 5;

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ProdutoRepository produtoRepository;

    public RelatorioService(PedidoRepository pedidoRepository, ItemPedidoRepository itemPedidoRepository,
                            ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.produtoRepository = produtoRepository;
    }

    // o periodo inclui o dia final inteiro
    public BigDecimal faturamento(LocalDate inicio, LocalDate fim) {
        return pedidoRepository.somarTotal(StatusPedido.PAGOS, inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay());
    }

    public BigDecimal ticketMedio(LocalDate inicio, LocalDate fim) {
        long pedidosPagos = pedidoRepository.contar(StatusPedido.PAGOS, inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay());
        if (pedidosPagos == 0) {
            return BigDecimal.ZERO;
        }
        return faturamento(inicio, fim).divide(BigDecimal.valueOf(pedidosPagos), 2, RoundingMode.HALF_UP);
    }

    public Map<StatusPedido, Long> pedidosPorStatus() {
        Map<StatusPedido, Long> quantidades = new LinkedHashMap<>();
        for (StatusPedido status : StatusPedido.values()) {
            quantidades.put(status, pedidoRepository.countByStatus(status));
        }
        return quantidades;
    }

    public List<Produto> estoqueBaixo() {
        return produtoRepository.findByAtivoTrueAndEstoqueLessThanEqualOrderByEstoqueAsc(LIMITE_ESTOQUE_BAIXO);
    }

    public List<ProdutoVendido> maisVendidosPorQuantidade(LocalDate inicio, LocalDate fim) {
        return itemPedidoRepository.maisVendidosPorQuantidade(StatusPedido.PAGOS, inicio.atStartOfDay(),
                fim.plusDays(1).atStartOfDay(), Limit.of(10));
    }

    public List<ProdutoVendido> maisVendidosPorFaturamento(LocalDate inicio, LocalDate fim) {
        return itemPedidoRepository.maisVendidosPorFaturamento(StatusPedido.PAGOS, inicio.atStartOfDay(),
                fim.plusDays(1).atStartOfDay(), Limit.of(10));
    }

    // separado por ponto e virgula para abrir direto no Excel em portugues
    public String gerarCsv(List<ProdutoVendido> produtos) {
        StringBuilder csv = new StringBuilder("Posição;Produto;Quantidade;Faturamento\n");
        int posicao = 1;
        for (ProdutoVendido produto : produtos) {
            csv.append(posicao++).append(';')
                    .append('"').append(produto.nome().replace("\"", "\"\"")).append('"').append(';')
                    .append(produto.quantidade()).append(';')
                    .append(produto.faturamento().setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ','))
                    .append('\n');
        }
        return csv.toString();
    }
}
