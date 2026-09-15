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
Controller   recebe a requisição, busca os dados e escolhe a página
   |
   v
Repository   interfaces do Spring Data JPA que conversam com o banco
   |
   v
Model        entidades JPA que viram tabelas no MySQL
```

As views ficam em `templates/`, escritas em Thymeleaf, e recebem os dados que o controller
colocou no `Model`.

Nesta etapa o controller chama o repository direto, porque ainda não há regras de negócio no
código. Quando as regras forem implementadas, entra a camada service entre o controller e o
repository.

## Pastas

```
src/main/java/com/mgp/lojaonline
├── LojaonlineApplication.java   classe que inicia o Spring Boot
├── config
│   ├── SecurityConfig.java      quem pode acessar o quê, login e BCrypt
│   └── DadosIniciais.java       cadastra dados de exemplo na primeira execução
├── controller
│   ├── CatalogoController.java  GET /        lista os produtos
│   ├── LoginController.java     GET /login   abre a tela de login
│   └── AdminController.java     GET /admin   painel do administrador
├── model
│   ├── Usuario.java, Categoria.java, Produto.java, Pedido.java, ItemPedido.java
│   └── Perfil.java, StatusPedido.java   enums
└── repository
    └── UsuarioRepository, CategoriaRepository, ProdutoRepository, PedidoRepository

src/main/resources
├── templates
│   ├── fragmentos.html   cabeçalho, menu e rodapé reaproveitados
│   ├── index.html        catálogo
│   ├── login.html        login
│   ├── error.html        páginas de erro (403, 404 e 500)
│   └── admin/painel.html painel do administrador
├── static
│   ├── css/estilo.css
│   ├── js/script.js
│   └── imagens/          ilustrações dos produtos
└── application.properties   banco, Thymeleaf e idioma

docs
├── mockups                     telas planejadas, em HTML e CSS puros
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

## Exemplo de caminho completo

Quando alguém abre a página inicial:

1. O Spring MVC chama `CatalogoController.catalogo()`, mapeado em `GET /`.
2. O controller pede os produtos ao `ProdutoRepository` (`findAllByOrderByNomeAsc()`).
3. O Spring Data JPA monta o SQL e o MySQL devolve as linhas, que viram objetos `Produto`.
4. O controller coloca a lista no `Model` e devolve o nome da página, `index`.
5. O Thymeleaf monta o `index.html` com um cartão para cada produto.
