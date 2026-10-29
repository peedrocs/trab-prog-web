package com.mgp.lojaonline.repository;

import com.mgp.lojaonline.model.Categoria;
import com.mgp.lojaonline.model.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findAllByOrderByNomeAsc();

    List<Produto> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);

    List<Produto> findByAtivoTrueAndEstoqueLessThanEqualOrderByEstoqueAsc(int estoque);

    long countByCategoria(Categoria categoria);

    // so produtos ativos de categorias ativas; a ordem vem no Pageable
    @Query("""
            select p from Produto p
            where p.ativo = true and p.categoria.ativa = true
              and lower(p.nome) like lower(concat('%', :busca, '%'))
              and (:categoriaId is null or p.categoria.id = :categoriaId)
            """)
    Page<Produto> buscarNaLoja(String busca, Long categoriaId, Pageable paginacao);
}
