package com.mgp.lojaonline.repository;

import com.mgp.lojaonline.model.Pedido;
import com.mgp.lojaonline.model.StatusPedido;
import com.mgp.lojaonline.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioOrderByDataDesc(Usuario usuario);

    List<Pedido> findAllByOrderByDataDesc();

    List<Pedido> findByStatusOrderByDataDesc(StatusPedido status);
}
