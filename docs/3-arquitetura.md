# 3. Arquitetura do projeto

## Arquitetura escolhida

**Spring MVC + Thymeleaf**, ou seja, as páginas são montadas no servidor e o navegador recebe
HTML pronto. Não é API REST com front separado.

**Banco relacional: MySQL 8**, que roda em um container do Docker descrito no `compose.yaml`.
O Spring sobe esse container sozinho quando o projeto inicia.

## Tecnologias

| Camada | O que usamos |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 4.1 |
| Web | Spring MVC + Thymeleaf |
| Persistência | Spring Data JPA (Hibernate) + MySQL 8 |
| Segurança | Spring Security |
| Validação | Bean Validation (`@NotBlank`, `@Min`, `@Email`...) |
| Front-end | HTML, CSS e JavaScript puros |
| Apoio | Lombok, que gera getters, setters e construtores na compilação |

## As camadas

```
Navegador
   |
   v
Controller   recebe a requisição, chama o service e escolhe a página
   |
   v
Service      onde ficam as regras de negócio (estoque, frete, cupom, status)
   |
   v
Repository   interfaces do Spring Data JPA que conversam com o banco
   |
   v
Model        entidades JPA que viram tabelas no MySQL
```

As **Views** ficam em `templates/`, escritas em Thymeleaf, e recebem os dados que o controller
colocou no `Model`.

## Pastas

```
src/main/java/com/mgp/lojaonline
├── config        SecurityConfig (segurança) e DadosIniciais (dados de exemplo)
├── controller    controllers da loja: catálogo, carrinho, pedidos, avaliações, login e cadastro
│   └── admin     controllers da área administrativa
├── exception     RegraNegocioException, usada quando uma regra é violada
├── model         entidades JPA, enums e ItemCarrinho (item do carrinho, não vira tabela)
├── repository    interfaces do Spring Data JPA
└── service       regras de negócio

src/main/resources
├── templates     páginas Thymeleaf (fragmentos.html tem cabeçalho, menu e rodapé)
├── static        css/estilo.css, js/script.js e imagens/ (ilustrações dos produtos)
├── application.properties   configuração do banco, do Thymeleaf e do idioma
└── messages.properties      mensagens de erro em português
```

## Entidades e tabelas

| Entidade | Tabela | Relacionamentos |
|---|---|---|
| `Usuario` | usuario | tem vários pedidos e avaliações |
| `Categoria` | categoria | tem vários produtos |
| `Produto` | produto | pertence a uma categoria |
| `Pedido` | pedido | pertence a um usuário, tem vários itens e pode ter um cupom |
| `ItemPedido` | item_pedido | liga um pedido a um produto e guarda quantidade e preço da compra |
| `Cupom` | cupom | pode estar em vários pedidos |
| `Avaliacao` | avaliacao | liga um usuário a um produto (um por par) |

Os enums `Perfil`, `StatusPedido` e `TipoDesconto` são salvos como texto nas tabelas.

`ItemCarrinho` fica só na memória: o carrinho vive na sessão do navegador (`@SessionScope`) e só
vira `ItemPedido` quando o cliente confirma a compra.

## Exemplo de caminho completo

Quando o cliente clica em **Adicionar ao carrinho**:

1. `CarrinhoController.adicionar()` recebe o `produtoId` e a quantidade.
2. Chama `CarrinhoService.adicionar()`, que busca o produto no `ProdutoRepository` e confere as
   regras: o produto tem estoque? A quantidade cabe no estoque?
3. Se alguma regra falha, o service lança `RegraNegocioException`, o controller transforma isso em
   mensagem na tela e devolve o cliente para a página do produto.
4. Dando tudo certo, o item entra no carrinho da sessão e o cliente vai para `/carrinho`.
