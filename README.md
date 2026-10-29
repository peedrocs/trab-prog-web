# Papelaria MGP - Loja Online

Loja online de papelaria feita com Spring Boot (Spring MVC + Thymeleaf), Spring Security e MySQL.
O cliente navega pelo catálogo, monta o carrinho e paga pelo Mercado Pago (Pix, cartão ou boleto);
o administrador cuida de produtos, pedidos, cupons, usuários e relatórios de vendas.

## Documentação

- [Integrantes do grupo](INTEGRANTES.md)
- [1. Tema e regras de negócio](docs/1-tema-e-regras.md)
- [2. Diagrama de casos de uso](docs/2-casos-de-uso.md)
- [3. Arquitetura do projeto](docs/3-arquitetura.md)
- [4. Segurança e acessibilidade](docs/4-seguranca-e-acessibilidade.md)
- [5. Interface e design: telas, fluxo de CRUD e diferenciais](docs/5-interface-e-design.md)

## Tecnologias

- Java 17 e Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation, Mail e OAuth2 Client)
- Thymeleaf (páginas montadas no servidor) com HTML, CSS e JavaScript puros
- MySQL 8 rodando pelo Docker Compose
- API REST do Mercado Pago, chamada com o `RestClient` do próprio Spring (sem SDK)
- ViaCEP para preencher o endereço pelo CEP

## Como rodar

1. Tenha o JDK 17 ou mais novo instalado e a variável `JAVA_HOME` apontando para ele
   (no PowerShell, só para o terminal atual: `$env:JAVA_HOME='C:\Program Files\Java\jdk-24'`).
2. Abra o Docker Desktop.
3. Copie o arquivo `.env.exemplo` para `.env` e preencha as chaves (veja abaixo). O `.env` não vai para o Git.
4. Na pasta do projeto, rode:
   - Windows: `mvnw.cmd spring-boot:run`
   - Linux/Mac: `./mvnw spring-boot:run`
5. Acesse http://localhost:8080

O Spring sobe o container do MySQL definido no `compose.yaml` sozinho. Na primeira execução,
a classe `DadosIniciais` cadastra as categorias, os produtos, dois cupons e as contas de teste.

**Quem já rodou o esqueleto precisa apagar o banco antigo uma vez**, porque as tabelas mudaram
(CPF, endereço, novos status de pedido, colunas de ativo/desativado). Com a aplicação parada:

```
docker compose down -v
```

Isso apaga só o banco de teste da loja; na próxima execução ele é criado de novo com os dados de exemplo.

## Contas e cupons de teste

| Perfil | E-mail | Senha | CPF |
|---|---|---|---|
| ROLE_ADMIN | admin@papelaria.com | admin123 | 529.982.247-25 |
| ROLE_USER | cliente@papelaria.com | cliente123 | 111.444.777-35 |

Depois da senha, o sistema manda um código de 6 dígitos para o e-mail da conta (ele chega na
caixa de teste do Mailtrap). Cupons: `BEMVINDO10` (10%) e `MENOS20` (R$ 20,00), válidos por um ano.

## Chaves no arquivo .env

| Variável | Onde conseguir |
|---|---|
| `MERCADOPAGO_ACCESS_TOKEN` | Mercado Pago Developers > Suas integrações > Contas de teste: Access Token da conta de teste do vendedor (começa com `APP_USR-`) |
| `MERCADOPAGO_WEBHOOK_SECRET` | Mesma aplicação > Webhooks > Configurar notificações > assinatura secreta |
| `MAIL_USERNAME` e `MAIL_PASSWORD` | Mailtrap > Email Testing > Inbox > SMTP |
| `GOOGLE_CLIENT_ID` e `GOOGLE_CLIENT_SECRET` | Google Cloud Console > APIs e serviços > Credenciais > ID do cliente OAuth |

Sem as chaves a loja abre normalmente; só não geram pagamento, e-mail ou login pelo Google.
No Google, cadastre a URI de redirecionamento `http://localhost:8080/login/oauth2/code/google`.

### Webhook do Mercado Pago na apresentação

O Mercado Pago precisa de um endereço público para avisar a loja que o pagamento entrou. Rodando
no próprio computador, dá para expor a porta 8080 com um túnel (por exemplo, o ngrok) e cadastrar
`https://<endereco-do-tunel>/webhook/mercadopago` no painel, em Webhooks, marcando o evento Pagamentos.
O webhook confere a assinatura (`x-signature`) com a chave secreta e consulta o pagamento na API antes
de mudar o pedido para Pago.

Para pagar no ambiente de teste, entre no Mercado Pago com a conta de teste do comprador e use o
cartão Mastercard 5480 8328 0103 3311, CVV 123, validade 11/30, titular `APRO` e CPF 123.456.789-09
(o nome `APRO` faz o pagamento ser aprovado). A documentação da API de pagamentos não mostra como
aprovar um Pix de teste, então o caminho mais seguro para mostrar o pedido mudando para Pago sozinho
é o cartão. O Pix de teste gera o QR Code e o copia e cola normalmente.

## O que o sistema faz

- **Conta:** cadastro com CPF validado pelos dígitos verificadores e único, login com senha + código
  por e-mail, login com Google (pede o CPF na primeira entrada), "Minha conta" com endereço preenchido
  pelo CEP e recuperação de senha por link.
- **Catálogo:** busca, filtro por categoria, ordenação (menor preço, maior preço, mais vendidos),
  paginação e página do produto com avaliações e nota média.
- **Carrinho e checkout:** quantidade limitada ao estoque, frete grátis a partir de R$ 150,00 (abaixo
  disso R$ 15,00 até 5 itens ou R$ 25,00 acima de 5), cupom percentual ou fixo de uso único e resumo
  com endereço antes de pagar.
- **Pagamento:** o pedido nasce "Aguardando pagamento" com o estoque reservado; Pix com QR Code e
  copia e cola na própria loja (a tela atualiza sozinha), boleto e cartão pelo Checkout Pro do
  Mercado Pago; webhook que muda o pedido para Pago; pedido não pago no prazo é cancelado e o
  estoque volta; cancelar um pedido pago gera estorno automático.
- **Pedidos:** linha do tempo de status, ordem Pago, Em separação, Enviado (com código de rastreio)
  e Entregue, e avaliação dos produtos depois da entrega.
- **E-mails** em segundo plano: boas-vindas, código de acesso, troca de senha, cada mudança do pedido
  e estoque baixo para o admin.
- **Administração:** painel com faturamento do dia e do mês, pedidos por status, ticket médio e estoque
  baixo; produtos (com upload de imagem), categorias, cupons, pedidos, pagamentos e usuários; relatório
  de mais vendidos por período com exportação em CSV; uma página única de exclusão que não apaga
  registro com vínculo e oferece desativar.

## Testes

```
mvnw.cmd test
```

Os testes de unidade cobrem CPF, frete, cupom, estoque e mudança de status. O `LojaonlineApplicationTests`
sobe a aplicação inteira e precisa do Docker aberto.

## Estrutura

```
src/main/java/com/mgp/lojaonline
├── config        SecurityConfig (segurança e logins) e DadosIniciais (dados de exemplo)
├── controller    páginas da loja, do cliente e o webhook do Mercado Pago
│   └── admin     painel, produtos, categorias, cupons, pedidos, pagamentos, usuários, relatórios e exclusão
├── dto           dados dos formulários e linhas de relatório
├── exception     RegraNegocioException
├── model         entidades JPA, enums e ItemCarrinho
├── repository    interfaces Spring Data JPA
├── service       regras de negócio, Mercado Pago, e-mails e a tarefa que cancela pedidos vencidos
└── util          validação do CPF
src/main/resources
├── templates     páginas Thymeleaf (fragmentos.html tem o cabeçalho, os menus e os campos de endereço)
├── static        css, js (menu, CEP e Pix) e imagens
└── application.properties
```
