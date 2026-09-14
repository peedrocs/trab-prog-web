# Papelaria MGP - Loja Online

Loja online de papelaria feita com Spring Boot (Spring MVC + Thymeleaf), Spring Security e MySQL.

## Documentação

- [Integrantes do grupo](INTEGRANTES.md)
- [1. Tema e regras de negócio](docs/1-tema-e-regras.md)
- [2. Diagrama de casos de uso](docs/2-casos-de-uso.md)
- [3. Arquitetura do projeto](docs/3-arquitetura.md)
- [4. Segurança e acessibilidade](docs/4-seguranca-e-acessibilidade.md)
- [5. Diferenciais](docs/5-diferenciais.md)
- [6. Guia da apresentação](docs/6-apresentacao.md)
- Slides: [slides/apresentacao-papelaria.pptx](slides/apresentacao-papelaria.pptx)

## Tecnologias

- Java 17 e Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation)
- Thymeleaf (views renderizadas no servidor) com HTML, CSS e JavaScript puros
- MySQL 8 rodando pelo Docker Compose
- Lombok, que gera getters, setters e construtores na compilação (`@Getter`, `@Setter`, `@RequiredArgsConstructor`)

## Como rodar

1. Tenha o JDK 17 ou mais novo instalado e a variável `JAVA_HOME` apontando para ele
   (no PowerShell, só para o terminal atual: `$env:JAVA_HOME='C:\Program Files\Java\jdk-24'`).
2. Abra o Docker Desktop.
3. Na pasta do projeto, rode:
   - Windows: `mvnw.cmd spring-boot:run`
   - Linux/Mac: `./mvnw spring-boot:run`
4. Acesse http://localhost:8080

O Spring sobe o container do MySQL definido no `compose.yaml` sozinho. Na primeira execução,
a classe `DadosIniciais` cadastra categorias, produtos, cupons e as contas de teste.

## Contas de teste

| Perfil | E-mail | Senha |
|---|---|---|
| ROLE_ADMIN | admin@papelaria.com | admin123 |
| ROLE_USER | cliente@papelaria.com | cliente123 |

Cupons: `BEMVINDO10` (10%), `PAPEL5` (R$ 5,00) e `VENCIDO20` (vencido, para testar a regra de validade).

## Estrutura

```
src/main/java/com/mgp/lojaonline
├── config        segurança (SecurityConfig) e dados iniciais (DadosIniciais)
├── controller    controllers da loja (catálogo, carrinho, pedidos, avaliações, login/cadastro)
│   │             e AtributosGlobais (contador do carrinho no cabeçalho)
│   └── admin     controllers da área administrativa
├── exception     RegraNegocioException
├── model         entidades JPA, enums e ItemCarrinho (item do carrinho em sessão, não vira tabela)
├── repository    interfaces Spring Data JPA
└── service       regras de negócio
src/main/resources
├── templates     páginas Thymeleaf (fragmentos.html tem o cabeçalho, rodapé e partes reaproveitadas)
└── static        css, js e imagens
```

## Perfis de acesso

| Ação | Visitante | ROLE_USER | ROLE_ADMIN |
|---|---|---|---|
| Ver catálogo e montar carrinho | ✔ | ✔ | ✔ |
| Finalizar compra e ver os próprios pedidos | | ✔ | ✔ |
| Avaliar produto comprado | | ✔ | ✔ |
| CRUD de produtos, categorias e cupons | | | ✔ |
| Alterar status de pedidos e gerenciar usuários | | | ✔ |

## Regras de negócio

- Produto sem estoque aparece como "Indisponível" e não pode ir para o carrinho.
- A quantidade no carrinho não pode passar do estoque.
- Ao confirmar o pedido o estoque é descontado; se o admin cancelar, o estoque volta.
- Status do pedido: Aguardando pagamento, Pago, Em separação, Enviado, Entregue e Cancelado. Só o admin altera, e pedido cancelado não muda mais.
- O pedido guarda o preço de cada item no momento da compra.
- Frete grátis quando os produtos somam R$ 150,00 ou mais (antes do desconto do cupom); abaixo disso, R$ 15,00 até 5 itens ou R$ 25,00 acima de 5 itens.
- Cupom percentual ou de valor fixo, com validade e uso único por cliente (pedido cancelado libera o cupom).
- E-mail único no cadastro; CPF opcional.
- Só quem comprou o produto (pedido não cancelado) pode avaliar, uma vez por produto.
- Categoria com produtos, produto já vendido e cupom já usado não podem ser excluídos.
- Quando o admin desativa ou muda o perfil de um usuário, a sessão desse usuário é encerrada.
