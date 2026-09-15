# 5. Interface e design

## Mockups das telas

As telas do sistema estão desenhadas em HTML e CSS puros na pasta [`mockups`](mockups/), usando
a mesma folha de estilo do sistema (`estilo.css`). Estão aqui as telas que ainda não foram
codificadas e a versão planejada das que já existem no esqueleto (o catálogo com busca e filtro,
o login e o painel). Para ver, basta abrir [`mockups/index.html`](mockups/index.html) no
navegador. Os dados são de exemplo.

As telas de confirmação do pedido, de detalhe do pedido e os cadastros de categorias e usuários
seguem o mesmo padrão e serão desenhadas na próxima etapa.

### Loja

| Tela | Arquivo | O que mostra |
|---|---|---|
| Catálogo | [catalogo.html](mockups/catalogo.html) | Busca por nome, filtro por categoria e os cartões dos produtos |
| Produto | [produto.html](mockups/produto.html) | Imagem, preço, estoque, descrição e o botão de adicionar ao carrinho |
| Carrinho | [carrinho.html](mockups/carrinho.html) | Itens com quantidade, resumo do total e o botão de finalizar |
| Entrar | [login.html](mockups/login.html) | Formulário de login |
| Criar conta | [cadastro.html](mockups/cadastro.html) | Formulário de cadastro com as mensagens de erro |
| Meus pedidos | [meus-pedidos.html](mockups/meus-pedidos.html) | Histórico do cliente com o status de cada pedido |

### Administração

| Tela | Arquivo | O que mostra |
|---|---|---|
| Painel | [admin-painel.html](mockups/admin-painel.html) | Números da loja e o menu da administração |
| Pedidos | [admin-pedidos.html](mockups/admin-pedidos.html) | Todos os pedidos, com a troca de status |

## Fluxo de CRUD (produtos)

O CRUD de produtos é o modelo para o cadastro de categorias. Para usuários haverá só a listagem
e a promoção de um cliente a administrador.

| Etapa | Arquivo | Como funciona |
|---|---|---|
| Listagem | [admin-produtos-listagem.html](mockups/admin-produtos-listagem.html) | Tabela com todos os produtos e os botões Editar e Excluir em cada linha, mais o botão Novo produto |
| Cadastro | [admin-produtos-cadastro.html](mockups/admin-produtos-cadastro.html) | Formulário em branco: nome, descrição, preço, estoque, categoria e imagem |
| Edição | [admin-produtos-edicao.html](mockups/admin-produtos-edicao.html) | O mesmo formulário já preenchido com os dados do produto |
| Exclusão | [admin-produtos-exclusao.html](mockups/admin-produtos-exclusao.html) | Página de confirmação antes de excluir, com os botões Cancelar e Excluir |

Como as telas se encadeiam:

```
                 Novo produto                    Salvar
  Listagem  ------------------>  Cadastro  ------------------>  Listagem
  (tabela)                       (formulário vazio)             (mensagem de sucesso)
     |
     |  Editar                                   Salvar
     +---------------------->  Edição  ---------------------->  Listagem
     |                         (formulário preenchido)          (mensagem de sucesso)
     |
     |  Excluir                                  Excluir
     +---------------------->  Confirmação  ----------------->  Listagem
                               (Cancelar volta                  (mensagem de sucesso
                                para a listagem)                 ou de erro)
```

Rotas planejadas:

| Ação | Método e rota |
|---|---|
| Listar | `GET /admin/produtos` |
| Abrir o formulário de cadastro | `GET /admin/produtos/novo` |
| Abrir o formulário de edição | `GET /admin/produtos/{id}/editar` |
| Salvar (cadastro ou edição) | `POST /admin/produtos/salvar` |
| Confirmar exclusão | `GET /admin/produtos/{id}/excluir` |
| Excluir | `POST /admin/produtos/{id}/excluir` |

Depois de salvar ou excluir, o sistema volta para a listagem com uma mensagem de sucesso. Se uma
regra for violada (por exemplo, excluir um produto que já foi vendido), a listagem mostra a
mensagem de erro.

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
