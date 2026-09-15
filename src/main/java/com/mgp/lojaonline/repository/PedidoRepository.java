package com.mgp.lojaonline.repository;

import com.mgp.lojaonline.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
