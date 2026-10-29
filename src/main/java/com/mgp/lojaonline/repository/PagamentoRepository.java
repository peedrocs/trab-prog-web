package com.mgp.lojaonline.repository;

import com.mgp.lojaonline.model.Pagamento;
import com.mgp.lojaonline.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    Optional<Pagamento> findByIdMercadoPago(Long idMercadoPago);

    Optional<Pagamento> findFirstByPedidoOrderByDataDesc(Pedido pedido);

    Optional<Pagamento> findFirstByPedidoAndStatusOrderByDataDesc(Pedido pedido, String status);

    List<Pagamento> findAllByOrderByDataDesc();
}
