package com.mgp.lojaonline.service;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.repository.ItemPedidoRepository;
import com.mgp.lojaonline.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public List<Produto> listarTodos() {
        return produtoRepository.findAllByOrderByNomeAsc();
    }

    public List<Produto> buscar(String busca, Long categoriaId) {
        String termo = busca == null ? "" : busca.trim();
        if (categoriaId == null) {
            return produtoRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc(termo);
        }
        return produtoRepository.findByCategoriaIdAndNomeContainingIgnoreCaseOrderByNomeAsc(categoriaId, termo);
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
    }

    public void salvar(Produto produto) {
        produtoRepository.save(produto);
    }

    public void excluir(Long id) {
        Produto produto = buscarPorId(id);
        if (itemPedidoRepository.existsByProduto(produto)) {
            throw new RegraNegocioException(
                    "Este produto já foi vendido e não pode ser excluído. Deixe o estoque em 0 para indisponibilizá-lo");
        }
        produtoRepository.delete(produto);
    }

    public long contar() {
        return produtoRepository.count();
    }

    public long contarSemEstoque() {
        return produtoRepository.countByEstoque(0);
    }
}
