package com.mgp.lojaonline.repository;

import com.mgp.lojaonline.model.Avaliacao;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    List<Avaliacao> findByProdutoOrderByDataDesc(Produto produto);

    boolean existsByUsuarioAndProduto(Usuario usuario, Produto produto);

    @Query("select avg(a.nota) from Avaliacao a where a.produto = :produto")
    Double calcularMedia(Produto produto);
}
