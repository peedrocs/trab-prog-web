# Papelaria MGP - Loja Online

Loja online de papelaria feita com Spring Boot (Spring MVC + Thymeleaf), Spring Security e MySQL.

Este repositório contém a entrega do protótipo (esqueleto): o projeto inicializado com as camadas
da arquitetura, a configuração inicial do Spring Security, o banco de dados real, as páginas de
todas as telas e a documentação do tema, das regras de negócio, dos casos de uso e do design.

## Documentação

- [Integrantes do grupo](INTEGRANTES.md)
- [1. Tema e regras de negócio](docs/1-tema-e-regras.md)
- [2. Diagrama de casos de uso](docs/2-casos-de-uso.md)
- [3. Arquitetura do projeto](docs/3-arquitetura.md)
- [4. Segurança e acessibilidade](docs/4-seguranca-e-acessibilidade.md)
- [5. Interface e design: telas, fluxo de CRUD e diferenciais](docs/5-interface-e-design.md)

## Tecnologias

- Java 17 e Spring Boot 4.1 (Web MVC, Data JPA, Security)
- Thymeleaf (páginas montadas no servidor) com HTML, CSS e JavaScript puros
- MySQL 8 rodando pelo Docker Compose

## Como rodar

1. Tenha o JDK 17 ou mais novo instalado e a variável `JAVA_HOME` apontando para ele
   (no PowerShell, só para o terminal atual: `$env:JAVA_HOME='C:\Program Files\Java\jdk-24'`).
2. Abra o Docker Desktop.
3. Na pasta do projeto, rode:
   - Windows: `mvnw.cmd spring-boot:run`
   - Linux/Mac: `./mvnw spring-boot:run`
4. Acesse http://localhost:8080

O Spring sobe o container do MySQL definido no `compose.yaml` sozinho. Na primeira execução,
a classe `DadosIniciais` cadastra as categorias, alguns produtos e as contas de teste.

## Contas de teste

| Perfil | E-mail | Senha |
|---|---|---|
| ROLE_ADMIN | admin@papelaria.com | admin123 |
| ROLE_USER | cliente@papelaria.com | cliente123 |

## O que está no esqueleto

- Entidades JPA (`Usuario`, `Categoria`, `Produto`, `Pedido` e `ItemPedido`) que viram tabelas no MySQL.
- Repositories do Spring Data JPA para Usuario, Categoria, Produto e Pedido (o ItemPedido é salvo junto com o pedido).
- Controllers e páginas Thymeleaf de todas as telas: catálogo com busca e filtro, página do
  produto, carrinho, cadastro, login, pedidos do cliente, painel do administrador, CRUD de
  produtos e de categorias, pedidos (com troca de status) e usuários.
- Spring Security com login por formulário, senhas em BCrypt e a área `/admin` restrita ao `ROLE_ADMIN`.
- Design das telas no Canva, com as imagens em `docs/telas`.

As regras de negócio (baixa de estoque, frete, cupom, avaliações e validação dos formulários)
serão implementadas na próxima etapa, em uma camada de service.

## Estrutura

```
src/main/java/com/mgp/lojaonline
├── config        SecurityConfig (segurança) e DadosIniciais (dados de exemplo)
├── controller    catálogo, carrinho, pedidos, login e cadastro
│   └── admin     painel, produtos, categorias, pedidos e usuários
├── model         entidades JPA, enums e ItemCarrinho (item montado a partir do carrinho da sessão)
└── repository    interfaces Spring Data JPA
src/main/resources
├── templates     páginas Thymeleaf (fragmentos.html tem o cabeçalho, o menu e o rodapé)
├── static        css, js e imagens
└── application.properties   banco, Thymeleaf e idioma
docs
├── telas         imagens das telas (design no Canva)
├── diagrama-casos-de-uso.asta e imagens/diagrama-casos-de-uso.png
└── *.md          documentação da entrega
```
