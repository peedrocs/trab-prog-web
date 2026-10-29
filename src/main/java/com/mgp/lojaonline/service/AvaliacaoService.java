package com.mgp.lojaonline.service;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Avaliacao;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.model.StatusPedido;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.repository.AvaliacaoRepository;
import com.mgp.lojaonline.repository.ItemPedidoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, ItemPedidoRepository itemPedidoRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    public List<Avaliacao> listar(Produto produto) {
        return avaliacaoRepository.findByProdutoOrderByDataDesc(produto);
    }

    public Double media(Produto produto) {
        return avaliacaoRepository.calcularMedia(produto);
    }

    // so avalia quem comprou e ja recebeu o produto, e uma vez so
    public boolean podeAvaliar(Usuario usuario, Produto produto) {
        return usuario != null
                && itemPedidoRepository.existsByPedidoUsuarioAndProdutoAndPedidoStatus(usuario, produto, StatusPedido.ENTREGUE)
                && !avaliacaoRepository.existsByUsuarioAndProduto(usuario, produto);
    }

    public void avaliar(Usuario usuario, Produto produto, int nota, String comentario) {
        if (nota < 1 || nota > 5) {
            throw new RegraNegocioException("A nota vai de 1 a 5");
        }
        if (comentario != null && comentario.length() > 500) {
            throw new RegraNegocioException("O comentário pode ter no máximo 500 caracteres");
        }
        if (!podeAvaliar(usuario, produto)) {
            throw new RegraNegocioException("Você só pode avaliar produtos que comprou e já recebeu, uma vez por produto");
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setUsuario(usuario);
        avaliacao.setProduto(produto);
        avaliacao.setNota(nota);
        avaliacao.setComentario(comentario == null ? null : comentario.trim());
        avaliacao.setData(LocalDateTime.now());
        avaliacaoRepository.save(avaliacao);
    }
}
