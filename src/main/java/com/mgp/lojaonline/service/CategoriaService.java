package com.mgp.lojaonline.service;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Categoria;
import com.mgp.lojaonline.repository.CategoriaRepository;
import com.mgp.lojaonline.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAllByOrderByNomeAsc();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
    }

    public void salvar(Categoria categoria) {
        categoria.setNome(categoria.getNome().trim());

        Optional<Categoria> existente = categoriaRepository.findByNomeIgnoreCase(categoria.getNome());
        if (existente.isPresent() && !existente.get().getId().equals(categoria.getId())) {
            throw new RegraNegocioException("Já existe uma categoria com este nome");
        }

        categoriaRepository.save(categoria);
    }

    public void excluir(Long id) {
        Categoria categoria = buscarPorId(id);
        if (produtoRepository.existsByCategoria(categoria)) {
            throw new RegraNegocioException("Não é possível excluir uma categoria que possui produtos");
        }
        categoriaRepository.delete(categoria);
    }
}
