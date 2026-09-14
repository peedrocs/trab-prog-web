package com.mgp.lojaonline.service;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Cupom;
import com.mgp.lojaonline.model.StatusPedido;
import com.mgp.lojaonline.model.TipoDesconto;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.repository.CupomRepository;
import com.mgp.lojaonline.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CupomService {

    private final CupomRepository cupomRepository;
    private final PedidoRepository pedidoRepository;

    public List<Cupom> listarTodos() {
        return cupomRepository.findAllByOrderByValidadeDesc();
    }

    public Cupom buscarPorId(Long id) {
        return cupomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cupom não encontrado"));
    }

    public void salvar(Cupom cupom) {
        cupom.setCodigo(cupom.getCodigo().trim().toUpperCase());

        if (cupom.getTipo() == TipoDesconto.PERCENTUAL && cupom.getValor().compareTo(new BigDecimal("100")) > 0) {
            throw new RegraNegocioException("O desconto percentual não pode passar de 100%");
        }

        Optional<Cupom> existente = cupomRepository.findByCodigoIgnoreCase(cupom.getCodigo());
        if (existente.isPresent() && !existente.get().getId().equals(cupom.getId())) {
            throw new RegraNegocioException("Já existe um cupom com este código");
        }

        cupomRepository.save(cupom);
    }

    public void excluir(Long id) {
        Cupom cupom = buscarPorId(id);
        if (pedidoRepository.existsByCupom(cupom)) {
            throw new RegraNegocioException(
                    "Este cupom já foi usado em pedidos e não pode ser excluído. Altere a validade para desativá-lo");
        }
        cupomRepository.delete(cupom);
    }

    public Cupom validar(String codigo, Usuario usuario) {
        Cupom cupom = cupomRepository.findByCodigoIgnoreCase(codigo.trim())
                .orElseThrow(() -> new RegraNegocioException("Cupom não encontrado"));

        if (cupom.isVencido()) {
            throw new RegraNegocioException("Este cupom está vencido");
        }
        // pedido cancelado devolve o direito de usar o cupom
        if (pedidoRepository.existsByUsuarioAndCupomAndStatusNot(usuario, cupom, StatusPedido.CANCELADO)) {
            throw new RegraNegocioException("Você já usou este cupom");
        }
        return cupom;
    }

    public BigDecimal calcularDesconto(Cupom cupom, BigDecimal subtotal) {
        BigDecimal desconto;
        if (cupom.getTipo() == TipoDesconto.PERCENTUAL) {
            desconto = subtotal.multiply(cupom.getValor()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            desconto = cupom.getValor();
        }
        return desconto.min(subtotal);
    }
}
