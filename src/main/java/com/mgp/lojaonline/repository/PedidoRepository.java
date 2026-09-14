package com.mgp.lojaonline.repository;

import com.mgp.lojaonline.model.Cupom;
import com.mgp.lojaonline.model.Pedido;
import com.mgp.lojaonline.model.StatusPedido;
import com.mgp.lojaonline.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioOrderByDataCriacaoDesc(Usuario usuario);

    List<Pedido> findAllByOrderByDataCriacaoDesc();

    List<Pedido> findByStatusOrderByDataCriacaoDesc(StatusPedido status);

    boolean existsByUsuarioAndCupomAndStatusNot(Usuario usuario, Cupom cupom, StatusPedido status);

    boolean existsByCupom(Cupom cupom);

    long countByStatus(StatusPedido status);
}
