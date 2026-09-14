package com.mgp.lojaonline.repository;

import com.mgp.lojaonline.model.ItemPedido;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.model.StatusPedido;
import com.mgp.lojaonline.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    boolean existsByProduto(Produto produto);

    boolean existsByProdutoAndPedidoUsuarioAndPedidoStatusNot(Produto produto, Usuario usuario, StatusPedido status);
}
