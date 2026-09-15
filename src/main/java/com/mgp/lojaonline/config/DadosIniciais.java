package com.mgp.lojaonline.config;

import com.mgp.lojaonline.model.Categoria;
import com.mgp.lojaonline.model.Perfil;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.repository.CategoriaRepository;
import com.mgp.lojaonline.repository.ProdutoRepository;
import com.mgp.lojaonline.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// cadastra dados de exemplo na primeira execucao
@Component
public class DadosIniciais implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;
    private final PasswordEncoder codificadorDeSenha;

    public DadosIniciais(UsuarioRepository usuarioRepository, CategoriaRepository categoriaRepository,
                         ProdutoRepository produtoRepository, PasswordEncoder codificadorDeSenha) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
        this.codificadorDeSenha = codificadorDeSenha;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            criarUsuario("Administrador", "admin@papelaria.com", "admin123", Perfil.ADMIN);
            criarUsuario("Cliente Teste", "cliente@papelaria.com", "cliente123", Perfil.USER);
        }

        if (categoriaRepository.count() == 0) {
            Categoria canetas = criarCategoria("Canetas");
            Categoria cadernos = criarCategoria("Cadernos");
            Categoria escolar = criarCategoria("Material escolar");
            Categoria escritorio = criarCategoria("Escritório");

            criarProduto("Caneta esferográfica azul", "Tinta azul, ponta média de 1,0 mm e escrita macia.", "2.50", 120, "/imagens/caneta.svg", canetas);
            criarProduto("Caneta esferográfica preta", "Tinta preta, ponta média de 1,0 mm e escrita macia.", "2.50", 0, "/imagens/caneta.svg", canetas);
            criarProduto("Caderno universitário 10 matérias", "Capa dura, 200 folhas pautadas e espiral.", "24.90", 35, "/imagens/caderno.svg", cadernos);
            criarProduto("Caderno brochura 96 folhas", "Capa flexível, 96 folhas pautadas.", "9.90", 50, "/imagens/caderno.svg", cadernos);
            criarProduto("Lápis de cor 24 cores", "Estojo com 24 cores, mina macia e resistente.", "32.00", 18, "/imagens/escolar.svg", escolar);
            criarProduto("Borracha branca", "Borracha macia que não mancha o papel.", "1.80", 200, "/imagens/escolar.svg", escolar);
            criarProduto("Grampeador de mesa", "Grampeia até 25 folhas, usa grampos 26/6.", "29.90", 12, "/imagens/escritorio.svg", escritorio);
            criarProduto("Pasta catálogo 50 envelopes", "Pasta tamanho ofício com 50 envelopes plásticos.", "18.50", 9, "/imagens/escritorio.svg", escritorio);
        }
    }

    private void criarUsuario(String nome, String email, String senha, Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(codificadorDeSenha.encode(senha));
        usuario.setPerfil(perfil);
        usuarioRepository.save(usuario);
    }

    private Categoria criarCategoria(String nome) {
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        return categoriaRepository.save(categoria);
    }

    private void criarProduto(String nome, String descricao, String preco, int estoque, String imagem, Categoria categoria) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setPreco(new BigDecimal(preco));
        produto.setEstoque(estoque);
        produto.setImagemUrl(imagem);
        produto.setCategoria(categoria);
        produtoRepository.save(produto);
    }
}
