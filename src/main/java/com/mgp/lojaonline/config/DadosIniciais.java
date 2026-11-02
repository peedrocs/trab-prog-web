package com.mgp.lojaonline.config;

import com.mgp.lojaonline.model.Categoria;
import com.mgp.lojaonline.model.Cupom;
import com.mgp.lojaonline.model.Endereco;
import com.mgp.lojaonline.model.Perfil;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.model.TipoCupom;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.repository.CategoriaRepository;
import com.mgp.lojaonline.repository.CupomRepository;
import com.mgp.lojaonline.repository.ProdutoRepository;
import com.mgp.lojaonline.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

// cadastra dados de exemplo na primeira execucao
@Component
public class DadosIniciais implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;
    private final CupomRepository cupomRepository;
    private final PasswordEncoder codificadorDeSenha;

    public DadosIniciais(UsuarioRepository usuarioRepository, CategoriaRepository categoriaRepository,
                         ProdutoRepository produtoRepository, CupomRepository cupomRepository,
                         PasswordEncoder codificadorDeSenha) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
        this.cupomRepository = cupomRepository;
        this.codificadorDeSenha = codificadorDeSenha;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            criarUsuario("Administrador", "admin@papelaria.com", "52998224725", "admin123", Perfil.ADMIN);
            criarUsuario("Cliente Teste", "cliente@papelaria.com", "11144477735", "cliente123", Perfil.USER);
        }

        if (categoriaRepository.count() == 0) {
            Categoria canetas = criarCategoria("Canetas");
            Categoria cadernos = criarCategoria("Cadernos");
            Categoria escolar = criarCategoria("Material escolar");
            Categoria escritorio = criarCategoria("Escritório");

            criarProduto("Caneta esferográfica azul", "Tinta azul, ponta média de 1,0 mm e escrita macia.", "2.50", 120, "/imagens/caneta-azul.jpg", canetas);
            criarProduto("Caneta esferográfica preta", "Tinta preta, ponta média de 1,0 mm e escrita macia.", "2.50", 0, "/imagens/caneta-preta.jpg", canetas);
            criarProduto("Caderno universitário 10 matérias", "Capa dura, 200 folhas pautadas e espiral.", "24.90", 35, "/imagens/caderno-universitario.jpg", cadernos);
            criarProduto("Caderno brochura 96 folhas", "Capa flexível, 96 folhas pautadas.", "9.90", 50, "/imagens/caderno-brochura.jpg", cadernos);
            criarProduto("Lápis de cor 24 cores", "Estojo com 24 cores, mina macia e resistente.", "32.00", 18, "/imagens/lapis-de-cor.jpg", escolar);
            criarProduto("Borracha branca", "Borracha macia que não mancha o papel.", "1.80", 200, "/imagens/borracha.jpg", escolar);
            criarProduto("Grampeador de mesa", "Grampeia até 25 folhas, usa grampos 26/6.", "29.90", 12, "/imagens/grampeador.jpg", escritorio);
            criarProduto("Pasta catálogo 50 envelopes", "Pasta tamanho ofício com 50 envelopes plásticos.", "18.50", 9, "/imagens/pasta-catalogo.jpg", escritorio);
        }

        if (cupomRepository.count() == 0) {
            criarCupom("BEMVINDO10", TipoCupom.PERCENTUAL, "10.00");
            criarCupom("MENOS20", TipoCupom.VALOR_FIXO, "20.00");
        }
    }

    private void criarUsuario(String nome, String email, String cpf, String senha, Perfil perfil) {
        Endereco endereco = new Endereco();
        endereco.setCep("79070-900");
        endereco.setRua("Avenida Costa e Silva");
        endereco.setNumero("100");
        endereco.setBairro("Universitário");
        endereco.setCidade("Campo Grande");
        endereco.setUf("MS");

        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setCpf(cpf);
        usuario.setSenha(codificadorDeSenha.encode(senha));
        usuario.setPerfil(perfil);
        usuario.setEndereco(endereco);
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

    private void criarCupom(String codigo, TipoCupom tipo, String valor) {
        Cupom cupom = new Cupom();
        cupom.setCodigo(codigo);
        cupom.setTipo(tipo);
        cupom.setValor(new BigDecimal(valor));
        cupom.setValidade(LocalDate.now().plusYears(1));
        cupomRepository.save(cupom);
    }
}
