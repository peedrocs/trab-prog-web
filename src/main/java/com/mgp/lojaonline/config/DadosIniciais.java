package com.mgp.lojaonline.config;

import com.mgp.lojaonline.model.*;
import com.mgp.lojaonline.repository.CategoriaRepository;
import com.mgp.lojaonline.repository.CupomRepository;
import com.mgp.lojaonline.repository.ProdutoRepository;
import com.mgp.lojaonline.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

// popula o banco na primeira execução para facilitar os testes
@Component
@RequiredArgsConstructor
public class DadosIniciais implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;
    private final CupomRepository cupomRepository;
    private final PasswordEncoder codificadorDeSenha;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            criarUsuario("Administrador", "admin@papelaria.com", "admin123", Perfil.ADMIN);
            criarUsuario("Cliente Teste", "cliente@papelaria.com", "cliente123", Perfil.USER);
        }

        if (categoriaRepository.count() == 0) {
            Categoria canetas = criarCategoria("Canetas");
            Categoria cadernos = criarCategoria("Cadernos");
            Categoria escolar = criarCategoria("Material Escolar");
            Categoria escritorio = criarCategoria("Escritório");
            Categoria arte = criarCategoria("Arte");

            criarProduto("Caneta Esferográfica Azul", "Caneta de ponta média 1.0 mm com tinta azul de secagem rápida.",
                    "2.50", 200, "/imagens/caneta.svg", canetas);
            criarProduto("Kit Canetas Coloridas 12 Cores", "Canetas de ponta fina 0.4 mm, ideais para anotações e desenhos.",
                    "24.90", 40, "/imagens/caneta.svg", canetas);
            criarProduto("Marca-texto Amarelo", "Marca-texto com ponta chanfrada e tinta fluorescente.",
                    "4.90", 0, "/imagens/caneta.svg", canetas);

            criarProduto("Caderno Universitário 10 Matérias", "Capa dura, 200 folhas pautadas e divisórias coloridas.",
                    "29.90", 50, "/imagens/caderno.svg", cadernos);
            criarProduto("Caderno Brochura 96 Folhas", "Caderno com capa flexível, ideal para o dia a dia.",
                    "12.90", 80, "/imagens/caderno.svg", cadernos);
            criarProduto("Agenda 2027", "Agenda diária com capa dura e marcador de páginas.",
                    "39.90", 25, "/imagens/caderno.svg", cadernos);

            criarProduto("Lápis Preto HB (caixa com 12)", "Lápis de grafite HB, ótimos para escrita e desenho.",
                    "15.00", 60, "/imagens/escolar.svg", escolar);
            criarProduto("Borracha Branca", "Borracha macia que apaga sem manchar o papel.",
                    "1.99", 150, "/imagens/escolar.svg", escolar);
            criarProduto("Estojo Duplo com Zíper", "Estojo com dois compartimentos e espaço para 40 itens.",
                    "34.90", 20, "/imagens/escolar.svg", escolar);
            criarProduto("Régua 30 cm", "Régua de acrílico transparente com marcação em milímetros.",
                    "3.50", 100, "/imagens/escolar.svg", escolar);

            criarProduto("Grampeador de Mesa", "Grampeador de metal para até 20 folhas, usa grampos 26/6.",
                    "27.90", 15, "/imagens/escritorio.svg", escritorio);
            criarProduto("Resma de Papel A4 (500 folhas)", "Papel sulfite branco 75 g/m², ideal para impressão.",
                    "32.90", 30, "/imagens/escritorio.svg", escritorio);
            criarProduto("Bloco Adesivo 76x76 mm", "Bloco com 100 folhas adesivas amarelas para lembretes.",
                    "9.90", 70, "/imagens/escritorio.svg", escritorio);

            criarProduto("Tinta Guache 12 Cores", "Tinta guache lavável com cores vivas, potes de 15 ml.",
                    "18.90", 35, "/imagens/arte.svg", arte);
            criarProduto("Bloco de Desenho A4", "Bloco com 20 folhas de papel 140 g/m² para desenho e pintura.",
                    "16.50", 45, "/imagens/arte.svg", arte);
            criarProduto("Kit de Pincéis (5 unidades)", "Pincéis de cerdas sintéticas em tamanhos variados.",
                    "22.00", 3, "/imagens/arte.svg", arte);
        }

        if (cupomRepository.count() == 0) {
            criarCupom("BEMVINDO10", TipoDesconto.PERCENTUAL, "10", LocalDate.now().plusYears(1));
            criarCupom("PAPEL5", TipoDesconto.VALOR_FIXO, "5", LocalDate.now().plusMonths(6));
            criarCupom("VENCIDO20", TipoDesconto.PERCENTUAL, "20", LocalDate.now().minusDays(1));
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

    private void criarProduto(String nome, String descricao, String preco, int estoque, String imagemUrl,
                              Categoria categoria) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setPreco(new BigDecimal(preco));
        produto.setEstoque(estoque);
        produto.setImagemUrl(imagemUrl);
        produto.setCategoria(categoria);
        produtoRepository.save(produto);
    }

    private void criarCupom(String codigo, TipoDesconto tipo, String valor, LocalDate validade) {
        Cupom cupom = new Cupom();
        cupom.setCodigo(codigo);
        cupom.setTipo(tipo);
        cupom.setValor(new BigDecimal(valor));
        cupom.setValidade(validade);
        cupomRepository.save(cupom);
    }
}
