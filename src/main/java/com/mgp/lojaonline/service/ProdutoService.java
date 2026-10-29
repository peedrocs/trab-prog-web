package com.mgp.lojaonline.service;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class ProdutoService {

    private static final int PRODUTOS_POR_PAGINA = 12;

    private final ProdutoRepository produtoRepository;
    private final Path pastaDeImagens;

    public ProdutoService(ProdutoRepository produtoRepository, @Value("${loja.pasta-uploads}") String pastaDeUploads) {
        this.produtoRepository = produtoRepository;
        this.pastaDeImagens = Path.of(pastaDeUploads, "imagens", "produtos").toAbsolutePath();
    }

    public Page<Produto> buscarNaLoja(String busca, Long categoriaId, String ordem, int pagina) {
        Sort ordenacao = switch (ordem) {
            case "menor-preco" -> Sort.by(Sort.Order.asc("preco"), Sort.Order.asc("nome"));
            case "maior-preco" -> Sort.by(Sort.Order.desc("preco"), Sort.Order.asc("nome"));
            case "mais-vendidos" -> Sort.by(Sort.Order.desc("quantidadeVendida"), Sort.Order.asc("nome"));
            default -> Sort.by("nome");
        };
        // na tela a primeira pagina e 1; no Spring Data e 0
        PageRequest paginacao = PageRequest.of(Math.max(pagina, 1) - 1, PRODUTOS_POR_PAGINA, ordenacao);
        return produtoRepository.buscarNaLoja(busca.trim(), categoriaId, paginacao);
    }

    public Produto buscarNaLoja(Long id) {
        return produtoRepository.findById(id)
                .filter(Produto::isVisivelNaLoja)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public Produto buscar(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // o formulario manda so os campos editaveis; o resto (vendidos, imagem atual) continua o que estava no banco
    public void salvar(Produto dados, MultipartFile imagem) {
        Produto produto = dados.getId() == null ? new Produto() : buscar(dados.getId());
        produto.setNome(dados.getNome().trim());
        produto.setDescricao(dados.getDescricao());
        produto.setPreco(dados.getPreco());
        produto.setEstoque(dados.getEstoque());
        produto.setCategoria(dados.getCategoria());
        produto.setAtivo(dados.isAtivo());

        if (imagem != null && !imagem.isEmpty()) {
            produto.setImagemUrl(guardarImagem(imagem));
        }
        produtoRepository.save(produto);
    }

    private String guardarImagem(MultipartFile imagem) {
        String extensao = switch (String.valueOf(imagem.getContentType())) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            default -> throw new RegraNegocioException("A imagem precisa ser PNG, JPG ou WEBP");
        };

        String nomeDoArquivo = UUID.randomUUID() + extensao;
        try (InputStream conteudo = imagem.getInputStream()) {
            Files.createDirectories(pastaDeImagens);
            Files.copy(conteudo, pastaDeImagens.resolve(nomeDoArquivo));
        } catch (IOException e) {
            throw new RegraNegocioException("Não foi possível salvar a imagem. Tente de novo.");
        }
        // a pasta de uploads e servida junto com os arquivos de static (ver application.properties)
        return "/imagens/produtos/" + nomeDoArquivo;
    }
}
