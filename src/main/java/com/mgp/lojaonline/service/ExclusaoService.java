package com.mgp.lojaonline.service;

import com.mgp.lojaonline.dto.RegistroExclusao;
import com.mgp.lojaonline.dto.TipoRegistro;
import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Categoria;
import com.mgp.lojaonline.model.Cupom;
import com.mgp.lojaonline.model.Perfil;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.repository.CategoriaRepository;
import com.mgp.lojaonline.repository.CupomRepository;
import com.mgp.lojaonline.repository.ItemPedidoRepository;
import com.mgp.lojaonline.repository.PedidoRepository;
import com.mgp.lojaonline.repository.ProdutoRepository;
import com.mgp.lojaonline.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ExclusaoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final CupomRepository cupomRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public ExclusaoService(ProdutoRepository produtoRepository, CategoriaRepository categoriaRepository,
                           CupomRepository cupomRepository, UsuarioRepository usuarioRepository,
                           PedidoRepository pedidoRepository, ItemPedidoRepository itemPedidoRepository) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.cupomRepository = cupomRepository;
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    public List<RegistroExclusao> buscar(TipoRegistro tipo, String busca, String emailDoAdmin) {
        String termo = busca.trim();
        return switch (tipo) {
            case PRODUTO -> produtoRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc(termo).stream()
                    .map(this::registroDoProduto).toList();
            case CATEGORIA -> categoriaRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc(termo).stream()
                    .map(this::registroDaCategoria).toList();
            case CUPOM -> cupomRepository.findByCodigoContainingIgnoreCaseOrderByCodigoAsc(termo).stream()
                    .map(this::registroDoCupom).toList();
            case USUARIO -> usuarioRepository.findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByNomeAsc(termo, termo)
                    .stream().map(usuario -> registroDoUsuario(usuario, emailDoAdmin)).toList();
        };
    }

    public RegistroExclusao detalhar(TipoRegistro tipo, Long id, String emailDoAdmin) {
        return switch (tipo) {
            case PRODUTO -> registroDoProduto(buscarProduto(id));
            case CATEGORIA -> registroDaCategoria(buscarCategoria(id));
            case CUPOM -> registroDoCupom(buscarCupom(id));
            case USUARIO -> registroDoUsuario(buscarUsuario(id), emailDoAdmin);
        };
    }

    @Transactional
    public void excluir(TipoRegistro tipo, Long id, String emailDoAdmin) {
        RegistroExclusao registro = detalhar(tipo, id, emailDoAdmin);
        // confere de novo aqui porque o vinculo pode ter surgido depois que a tela abriu
        if (!registro.podeExcluir()) {
            throw new RegraNegocioException(registro.motivoBloqueio());
        }
        switch (tipo) {
            case PRODUTO -> produtoRepository.deleteById(id);
            case CATEGORIA -> categoriaRepository.deleteById(id);
            case CUPOM -> cupomRepository.deleteById(id);
            case USUARIO -> usuarioRepository.deleteById(id);
        }
    }

    @Transactional
    public void desativar(TipoRegistro tipo, Long id, String emailDoAdmin) {
        switch (tipo) {
            case PRODUTO -> {
                Produto produto = buscarProduto(id);
                produto.setAtivo(false);
                produtoRepository.save(produto);
            }
            case CATEGORIA -> {
                Categoria categoria = buscarCategoria(id);
                categoria.setAtiva(false);
                categoriaRepository.save(categoria);
            }
            case CUPOM -> {
                Cupom cupom = buscarCupom(id);
                cupom.setAtivo(false);
                cupomRepository.save(cupom);
            }
            case USUARIO -> {
                Usuario usuario = buscarUsuario(id);
                if (usuario.getEmail().equals(emailDoAdmin)) {
                    throw new RegraNegocioException("Você não pode desativar a própria conta");
                }
                usuario.setAtivo(false);
                usuarioRepository.save(usuario);
            }
        }
    }

    private RegistroExclusao registroDoProduto(Produto produto) {
        String detalhe = produto.getCategoria().getNome() + ", " + dinheiro(produto) + ", estoque " + produto.getEstoque();
        long vendas = itemPedidoRepository.countByProduto(produto);
        String motivo = vendas > 0
                ? "Este produto aparece em " + vendas + " pedido(s), por isso não pode ser excluído. Desative para tirá-lo da loja."
                : null;
        return new RegistroExclusao(produto.getId(), produto.getNome(), detalhe, produto.isAtivo(), motivo,
                produto.isAtivo());
    }

    private RegistroExclusao registroDaCategoria(Categoria categoria) {
        long produtos = produtoRepository.countByCategoria(categoria);
        String motivo = produtos > 0
                ? "Esta categoria tem " + produtos + " produto(s), por isso não pode ser excluída. Desative para tirá-la da loja junto com os produtos."
                : null;
        return new RegistroExclusao(categoria.getId(), categoria.getNome(), produtos + " produto(s)",
                categoria.isAtiva(), motivo, categoria.isAtiva());
    }

    private RegistroExclusao registroDoCupom(Cupom cupom) {
        String detalhe = cupom.getDescricaoDesconto() + " de desconto, válido até "
                + cupom.getValidade().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        long usos = pedidoRepository.countByCupom(cupom);
        String motivo = usos > 0
                ? "Este cupom foi usado em " + usos + " pedido(s), por isso não pode ser excluído. Desative para ninguém mais usar."
                : null;
        return new RegistroExclusao(cupom.getId(), cupom.getCodigo(), detalhe, cupom.isAtivo(), motivo, cupom.isAtivo());
    }

    private RegistroExclusao registroDoUsuario(Usuario usuario, String emailDoAdmin) {
        String detalhe = usuario.getEmail() + (usuario.getPerfil() == Perfil.ADMIN ? ", administrador" : ", cliente");
        long pedidos = pedidoRepository.countByUsuario(usuario);
        boolean propriaConta = usuario.getEmail().equals(emailDoAdmin);
        String motivo = null;
        if (propriaConta) {
            motivo = "Você não pode excluir a própria conta.";
        } else if (pedidos > 0) {
            motivo = "Este usuário tem " + pedidos + " pedido(s), por isso não pode ser excluído. Desative para bloquear o acesso.";
        }
        return new RegistroExclusao(usuario.getId(), usuario.getNome(), detalhe, usuario.isAtivo(), motivo,
                usuario.isAtivo() && !propriaConta);
    }

    private String dinheiro(Produto produto) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(produto.getPreco());
    }

    private Produto buscarProduto(Long id) {
        return produtoRepository.findById(id).orElseThrow(() -> new RegraNegocioException("Produto não encontrado"));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id).orElseThrow(() -> new RegraNegocioException("Categoria não encontrada"));
    }

    private Cupom buscarCupom(Long id) {
        return cupomRepository.findById(id).orElseThrow(() -> new RegraNegocioException("Cupom não encontrado"));
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new RegraNegocioException("Usuário não encontrado"));
    }
}
