package com.mgp.lojaonline.repository;

import com.mgp.lojaonline.model.Categoria;
import com.mgp.lojaonline.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findAllByOrderByNomeAsc();

    List<Produto> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);

    List<Produto> findByCategoriaIdAndNomeContainingIgnoreCaseOrderByNomeAsc(Long categoriaId, String nome);

    boolean existsByCategoria(Categoria categoria);

    long countByEstoque(Integer estoque);
}
