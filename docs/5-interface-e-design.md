# 5. Interface e design

## Telas (design no Canva)

As telas do sistema foram desenhadas com o mesmo visual do site e estão na pasta
[`telas`](telas/), em PNG, na ordem em que aparecem no uso do sistema. São as imagens usadas no
design do Canva. Os dados são de exemplo. O arquivo
[`telas/telas-papelaria-mgp.pdf`](telas/telas-papelaria-mgp.pdf) junta todas as telas, uma por
página, para importar no Canva.

Todas as telas já têm a página Thymeleaf correspondente em `src/main/resources/templates`
(ver o documento [3. Arquitetura](3-arquitetura.md)). As telas de detalhe do pedido, o cadastro
de categorias e a listagem de usuários seguem o mesmo padrão das telas abaixo.

### Loja

A tela inicial é o catálogo, com a busca por nome, o filtro por categoria e um cartão para cada
produto. Produto sem estoque aparece como Indisponível.

![Catálogo](telas/01-catalogo.png)

Clicando no nome do produto o cliente vê a imagem, o preço, o estoque e a descrição, e escolhe a
quantidade antes de adicionar ao carrinho.

![Produto](telas/02-produto.png)

No carrinho dá para mudar a quantidade ou remover cada item. O resumo mostra o total e o botão
de finalizar, que pede login se o cliente ainda não entrou.

![Carrinho](telas/03-carrinho.png)

A tela de login tem só e-mail e senha.

![Entrar](telas/04-login.png)

Quem não tem conta cria uma com nome, e-mail e senha. A imagem mostra como fica a mensagem de
erro de validação.

![Criar conta](telas/05-cadastro.png)

Em Meus pedidos o cliente vê o histórico com a data, o status e o total de cada pedido.

![Meus pedidos](telas/06-meus-pedidos.png)

### Administração

O painel mostra os números da loja e o menu com as seções da administração.

![Painel](telas/07-admin-painel.png)

Na tela de pedidos o admin vê todos os pedidos e filtra por status. No sistema, a troca de
status fica na tela de detalhe de cada pedido, e pedido cancelado não muda mais.

![Pedidos](telas/12-admin-pedidos.png)

## Fluxo de CRUD (produtos)

O CRUD de produtos é o modelo para o cadastro de categorias. Para usuários há só a listagem e a
troca de perfil entre cliente e administrador (o admin não altera o próprio perfil).

A listagem é uma tabela com todos os produtos, os botões Editar e Excluir em cada linha e o
botão Novo produto. É para ela que o sistema volta depois de salvar ou excluir, mostrando a
mensagem de sucesso ou de erro.

![Listagem de produtos](telas/08-admin-produtos-listagem.png)

Novo produto abre o formulário em branco: nome, descrição, preço, estoque, categoria e imagem.

![Cadastro de produto](telas/09-admin-produtos-cadastro.png)

Editar abre o mesmo formulário já preenchido com os dados do produto.

![Edição de produto](telas/10-admin-produtos-edicao.png)

Excluir abre uma página de confirmação antes de apagar. Cancelar volta para a listagem.

![Exclusão de produto](telas/11-admin-produtos-exclusao.png)

Resumindo o encadeamento:

1. Na listagem, Novo produto abre o formulário vazio e Editar abre o formulário preenchido.
2. Salvar grava e volta para a listagem com a mensagem de sucesso.
3. Excluir abre a confirmação. Confirmar apaga e volta para a listagem; Cancelar só volta.
4. Se o produto já foi vendido, a listagem mostra a mensagem de erro e nada é apagado.

Rotas:

| Ação | Método e rota |
|---|---|
| Listar | `GET /admin/produtos` |
| Abrir o formulário de cadastro | `GET /admin/produtos/novo` |
| Abrir o formulário de edição | `GET /admin/produtos/{id}/editar` |
| Salvar (cadastro ou edição) | `POST /admin/produtos/salvar` |
| Confirmar exclusão | `GET /admin/produtos/{id}/excluir` |
| Excluir | `POST /admin/produtos/{id}/excluir` |

## Planejamento de diferenciais

| Diferencial | Como vai funcionar |
|---|---|
| Cupom de desconto | Percentual ou valor fixo, com data de validade e uso único por cliente |
| Cálculo de frete | Grátis a partir de R$ 150,00 em produtos. Abaixo disso, valor fixo por faixa de itens |
| Avaliações | Só quem comprou o produto avalia, com nota de 1 a 5 e comentário. A página do produto mostra a média |
| Carrinho para visitante | O carrinho fica na sessão e o login só é pedido ao finalizar a compra |
| Controle de estoque automático | O estoque cai quando o pedido é confirmado e volta se o admin cancelar |
| Painel administrativo | Totais da loja e atalhos para os cadastros |
| Acessibilidade WCAG AA | Contraste conferido, navegação por teclado, ARIA e tags semânticas |
| Páginas de erro próprias | 403 e 404 com o visual do site |
