package com.mgp.lojaonline.repository;

import com.mgp.lojaonline.dto.ProdutoVendido;
import com.mgp.lojaonline.model.ItemPedido;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.model.StatusPedido;
import com.mgp.lojaonline.model.Usuario;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    long countByProduto(Produto produto);

    boolean existsByPedidoUsuarioAndProdutoAndPedidoStatus(Usuario usuario, Produto produto, StatusPedido status);

    @Query("""
            select new com.mgp.lojaonline.dto.ProdutoVendido(i.produto.nome, sum(i.quantidade), sum(i.precoUnitario * i.quantidade))
            from ItemPedido i
            where i.pedido.status in :status and i.pedido.data >= :inicio and i.pedido.data < :fim
            group by i.produto.id, i.produto.nome
            order by sum(i.quantidade) desc
            """)
    List<ProdutoVendido> maisVendidosPorQuantidade(List<StatusPedido> status, LocalDateTime inicio,
                                                   LocalDateTime fim, Limit limite);

    @Query("""
            select new com.mgp.lojaonline.dto.ProdutoVendido(i.produto.nome, sum(i.quantidade), sum(i.precoUnitario * i.quantidade))
            from ItemPedido i
            where i.pedido.status in :status and i.pedido.data >= :inicio and i.pedido.data < :fim
            group by i.produto.id, i.produto.nome
            order by sum(i.precoUnitario * i.quantidade) desc
            """)
    List<ProdutoVendido> maisVendidosPorFaturamento(List<StatusPedido> status, LocalDateTime inicio,
                                                    LocalDateTime fim, Limit limite);
}
