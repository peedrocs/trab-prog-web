package com.mgp.lojaonline.repository;

import com.mgp.lojaonline.model.Cupom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CupomRepository extends JpaRepository<Cupom, Long> {

    Optional<Cupom> findByCodigoIgnoreCase(String codigo);

    List<Cupom> findAllByOrderByValidadeDesc();

    List<Cupom> findByCodigoContainingIgnoreCaseOrderByCodigoAsc(String codigo);
}
