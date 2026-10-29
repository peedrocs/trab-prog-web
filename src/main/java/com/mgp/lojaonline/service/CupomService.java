package com.mgp.lojaonline.service;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Cupom;
import com.mgp.lojaonline.model.StatusPedido;
import com.mgp.lojaonline.model.TipoCupom;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.repository.CupomRepository;
import com.mgp.lojaonline.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class CupomService {

    private final CupomRepository cupomRepository;
    private final PedidoRepository pedidoRepository;

    public CupomService(CupomRepository cupomRepository, PedidoRepository pedidoRepository) {
        this.cupomRepository = cupomRepository;
        this.pedidoRepository = pedidoRepository;
    }

    // usuario vem nulo quando o visitante ainda nao entrou; o uso unico e conferido de novo no checkout
    public Cupom validar(String codigo, Usuario usuario) {
        Cupom cupom = cupomRepository.findByCodigoIgnoreCase(codigo.trim())
                .orElseThrow(() -> new RegraNegocioException("Cupom não encontrado"));

        if (!cupom.isAtivo()) {
            throw new RegraNegocioException("Este cupom não está mais disponível");
        }
        if (cupom.estaVencido(LocalDate.now())) {
            String validade = cupom.getValidade().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            throw new RegraNegocioException("Este cupom venceu em " + validade);
        }
        // pedido cancelado libera o cupom para ser usado de novo
        if (usuario != null && pedidoRepository.existsByUsuarioAndCupomAndStatusNot(usuario, cupom, StatusPedido.CANCELADO)) {
            throw new RegraNegocioException("Você já usou este cupom em outro pedido");
        }
        return cupom;
    }

    public void salvar(Cupom cupom) {
        if (cupom.getTipo() == TipoCupom.PERCENTUAL && cupom.getValor().compareTo(new BigDecimal("100")) > 0) {
            throw new RegraNegocioException("O desconto percentual vai até 100%");
        }

        cupom.setCodigo(cupom.getCodigo().trim().toUpperCase());
        boolean codigoRepetido = cupomRepository.findByCodigoIgnoreCase(cupom.getCodigo())
                .filter(outro -> !outro.getId().equals(cupom.getId()))
                .isPresent();
        if (codigoRepetido) {
            throw new RegraNegocioException("Já existe um cupom com este código");
        }
        cupomRepository.save(cupom);
    }
}
