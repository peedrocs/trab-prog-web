# 3. Arquitetura do projeto

## Arquitetura escolhida

Spring MVC + Thymeleaf: as páginas são montadas no servidor e o navegador recebe HTML pronto.
Não é API REST com front separado.

Banco relacional: MySQL 8, que roda em um container do Docker descrito no `compose.yaml`.
O Spring sobe esse container sozinho quando o projeto inicia, e o Hibernate cria as tabelas a
partir das entidades (`spring.jpa.hibernate.ddl-auto=update`).

## Tecnologias

| Camada | O que usamos |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 4.1 |
| Web | Spring MVC + Thymeleaf |
| Persistência | Spring Data JPA (Hibernate) + MySQL 8 |
| Segurança | Spring Security |
| Front-end | HTML, CSS e JavaScript puros |

## As camadas

```
Navegador
   |
   v
Controller   recebe a requisição, busca ou grava os dados e escolhe a página
   |
   v
Repository   interfaces do Spring Data JPA que conversam com o banco
   |
   v
Model        entidades JPA que viram tabelas no MySQL
```

As views ficam em `templates/`, escritas em Thymeleaf, e recebem os dados que o controller
colocou no `Model`.

Nesta etapa o controller chama o repository direto. Quando as regras de negócio forem
implementadas (baixa de estoque, frete, cupom, validações), entra a camada service entre o
controller e o repository.

## Pastas

```
src/main/java/com/mgp/lojaonline
├── LojaonlineApplication.java   classe que inicia o Spring Boot
├── config
│   ├── SecurityConfig.java      quem pode acessar o quê, login e BCrypt
│   └── DadosIniciais.java       cadastra dados de exemplo na primeira execução
├── controller
│   ├── CatalogoController.java       GET /  e  GET /produtos/{id}
│   ├── CarrinhoController.java       /carrinho: ver, adicionar, atualizar e remover
│   ├── PedidoController.java         /pedidos: meus pedidos, detalhe e finalizar compra
│   ├── AutenticacaoController.java   /login e /cadastro
│   └── admin
│       ├── PainelAdminController.java     GET /admin
│       ├── ProdutoAdminController.java    /admin/produtos: listar, cadastrar, editar e excluir
│       ├── CategoriaAdminController.java  /admin/categorias: listar, cadastrar, editar e excluir
│       ├── PedidoAdminController.java     /admin/pedidos: listar, ver e mudar o status
│       └── UsuarioAdminController.java    /admin/usuarios: listar e mudar o perfil
├── model
│   ├── Usuario.java, Categoria.java, Produto.java, Pedido.java, ItemPedido.java   entidades
│   ├── Perfil.java, StatusPedido.java   enums
│   └── ItemCarrinho.java                item do carrinho em sessão, não vira tabela
└── repository
    └── UsuarioRepository, CategoriaRepository, ProdutoRepository, PedidoRepository

src/main/resources
├── templates
│   ├── fragmentos.html      cabeçalho, menu, mensagens, menu da administração e rodapé
│   ├── index.html           catálogo
│   ├── produtos/detalhe.html
│   ├── carrinho.html
│   ├── login.html e cadastro.html
│   ├── pedidos/lista.html e pedidos/detalhe.html
│   ├── admin/painel.html
│   ├── admin/produtos/     lista.html, formulario.html e excluir.html
│   ├── admin/categorias/   lista.html, formulario.html e excluir.html
│   ├── admin/pedidos/      lista.html e detalhe.html
│   ├── admin/usuarios/     lista.html
│   └── error.html           páginas de erro (403, 404 e 500)
├── static
│   ├── css/estilo.css
│   ├── js/script.js
│   └── imagens/          ilustrações dos produtos
└── application.properties   banco, Thymeleaf e idioma

docs
├── telas                       imagens das telas (design no Canva)
├── diagrama-casos-de-uso.asta  diagrama no Astah
└── imagens/diagrama-casos-de-uso.png
```

## Entidades e tabelas

| Entidade | Tabela | Relacionamentos |
|---|---|---|
| `Usuario` | usuario | tem vários pedidos |
| `Categoria` | categoria | tem vários produtos |
| `Produto` | produto | pertence a uma categoria |
| `Pedido` | pedido | pertence a um usuário e tem vários itens |
| `ItemPedido` | item_pedido | liga um pedido a um produto e guarda quantidade e preço da compra |

Os enums `Perfil` (USER e ADMIN) e `StatusPedido` são salvos pelo nome do valor
(`EnumType.STRING`), e não pelo número.

O carrinho não vira tabela: é um mapa produto e quantidade guardado na sessão HTTP
(`CarrinhoController.carrinho`). Ele só vira `Pedido` e `ItemPedido` quando o cliente clica em
Finalizar compra.

## Exemplo de caminho completo

Quando o cliente clica em Adicionar ao carrinho:

1. O formulário envia `POST /carrinho/adicionar` com o id do produto e a quantidade.
2. `CarrinhoController.adicionar()` soma a quantidade no mapa do carrinho da sessão e redireciona
   para `/carrinho`.
3. `CarrinhoController.carrinho()` busca cada produto do mapa no `ProdutoRepository`, monta a
   lista de `ItemCarrinho` com os subtotais e calcula o total.
4. O controller coloca a lista e o total no `Model` e devolve o nome da página, `carrinho`.
5. O Thymeleaf monta o `carrinho.html` com uma linha para cada item.
