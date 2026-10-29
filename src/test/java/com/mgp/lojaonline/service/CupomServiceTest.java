package com.mgp.lojaonline.service;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Cupom;
import com.mgp.lojaonline.model.StatusPedido;
import com.mgp.lojaonline.model.TipoCupom;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.repository.CupomRepository;
import com.mgp.lojaonline.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CupomServiceTest {

    @Mock
    private CupomRepository cupomRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private CupomService cupomService;

    private final Usuario cliente = new Usuario();

    private Cupom cupomQueVenceEm(LocalDate validade) {
        Cupom cupom = new Cupom();
        cupom.setCodigo("BEMVINDO10");
        cupom.setTipo(TipoCupom.PERCENTUAL);
        cupom.setValor(new BigDecimal("10"));
        cupom.setValidade(validade);
        return cupom;
    }

    @Test
    void cupomValidoParaQuemAindaNaoUsou() {
        Cupom cupom = cupomQueVenceEm(LocalDate.now().plusDays(10));
        when(cupomRepository.findByCodigoIgnoreCase("BEMVINDO10")).thenReturn(Optional.of(cupom));
        when(pedidoRepository.existsByUsuarioAndCupomAndStatusNot(cliente, cupom, StatusPedido.CANCELADO)).thenReturn(false);

        assertSame(cupom, cupomService.validar("BEMVINDO10", cliente));
    }

    @Test
    void clienteNaoUsaOMesmoCupomDuasVezes() {
        Cupom cupom = cupomQueVenceEm(LocalDate.now().plusDays(10));
        when(cupomRepository.findByCodigoIgnoreCase("BEMVINDO10")).thenReturn(Optional.of(cupom));
        when(pedidoRepository.existsByUsuarioAndCupomAndStatusNot(cliente, cupom, StatusPedido.CANCELADO)).thenReturn(true);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> cupomService.validar("BEMVINDO10", cliente));
        assertEquals("Você já usou este cupom em outro pedido", erro.getMessage());
    }

    @Test
    void cupomVencidoNaoVale() {
        Cupom cupom = cupomQueVenceEm(LocalDate.now().minusDays(1));
        when(cupomRepository.findByCodigoIgnoreCase("BEMVINDO10")).thenReturn(Optional.of(cupom));

        assertThrows(RegraNegocioException.class, () -> cupomService.validar("BEMVINDO10", cliente));
    }

    @Test
    void cupomDesativadoNaoVale() {
        Cupom cupom = cupomQueVenceEm(LocalDate.now().plusDays(10));
        cupom.setAtivo(false);
        when(cupomRepository.findByCodigoIgnoreCase("BEMVINDO10")).thenReturn(Optional.of(cupom));

        assertThrows(RegraNegocioException.class, () -> cupomService.validar("BEMVINDO10", cliente));
    }

    @Test
    void percentualAcimaDe100NaoESalvo() {
        Cupom cupom = cupomQueVenceEm(LocalDate.now().plusDays(10));
        cupom.setValor(new BigDecimal("150"));

        assertThrows(RegraNegocioException.class, () -> cupomService.salvar(cupom));
    }
}
