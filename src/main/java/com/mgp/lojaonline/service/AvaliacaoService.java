package com.mgp.lojaonline.service;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Avaliacao;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.model.StatusPedido;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.repository.AvaliacaoRepository;
import com.mgp.lojaonline.repository.ItemPedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public List<Avaliacao> listarDoProduto(Produto produto) {
        return avaliacaoRepository.findByProdutoOrderByDataCriacaoDesc(produto);
    }

    public double calcularMedia(List<Avaliacao> avaliacoes) {
        int soma = 0;
        for (Avaliacao avaliacao : avaliacoes) {
            soma += avaliacao.getNota();
        }
        return avaliacoes.isEmpty() ? 0 : (double) soma / avaliacoes.size();
    }

    public boolean podeAvaliar(Usuario usuario, Produto produto) {
        return comprou(usuario, produto) && !avaliacaoRepository.existsByUsuarioAndProduto(usuario, produto);
    }

    public void avaliar(Usuario usuario, Produto produto, Integer nota, String comentario) {
        if (!comprou(usuario, produto)) {
            throw new RegraNegocioException("Só quem comprou o produto pode avaliá-lo");
        }
        if (avaliacaoRepository.existsByUsuarioAndProduto(usuario, produto)) {
            throw new RegraNegocioException("Você já avaliou este produto");
        }
        if (nota == null || nota < 1 || nota > 5) {
            throw new RegraNegocioException("A nota deve ser de 1 a 5");
        }
        if (comentario != null && comentario.length() > 500) {
            throw new RegraNegocioException("O comentário deve ter no máximo 500 caracteres");
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setUsuario(usuario);
        avaliacao.setProduto(produto);
        avaliacao.setNota(nota);
        avaliacao.setComentario(comentario);
        avaliacao.setDataCriacao(LocalDateTime.now());
        avaliacaoRepository.save(avaliacao);
    }

    private boolean comprou(Usuario usuario, Produto produto) {
        return itemPedidoRepository.existsByProdutoAndPedidoUsuarioAndPedidoStatusNot(
                produto, usuario, StatusPedido.CANCELADO);
    }
}
