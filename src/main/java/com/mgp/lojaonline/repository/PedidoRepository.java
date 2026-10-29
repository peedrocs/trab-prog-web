package com.mgp.lojaonline.repository;

import com.mgp.lojaonline.model.Cupom;
import com.mgp.lojaonline.model.Pedido;
import com.mgp.lojaonline.model.StatusPedido;
import com.mgp.lojaonline.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioOrderByDataDesc(Usuario usuario);

    List<Pedido> findAllByOrderByDataDesc();

    List<Pedido> findByStatusOrderByDataDesc(StatusPedido status);

    List<Pedido> findByStatusAndPrazoPagamentoBefore(StatusPedido status, LocalDateTime horario);

    boolean existsByUsuarioAndCupomAndStatusNot(Usuario usuario, Cupom cupom, StatusPedido status);

    long countByStatus(StatusPedido status);

    long countByUsuario(Usuario usuario);

    long countByCupom(Cupom cupom);

    @Query("""
            select coalesce(sum(p.total), 0) from Pedido p
            where p.status in :status and p.data >= :inicio and p.data < :fim
            """)
    BigDecimal somarTotal(List<StatusPedido> status, LocalDateTime inicio, LocalDateTime fim);

    @Query("""
            select count(p) from Pedido p
            where p.status in :status and p.data >= :inicio and p.data < :fim
            """)
    long contar(List<StatusPedido> status, LocalDateTime inicio, LocalDateTime fim);
}
