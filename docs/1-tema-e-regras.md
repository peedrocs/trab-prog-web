# 1. Tema e regras de negócio

## O tema

O projeto é uma **papelaria online**: uma loja em que o cliente navega pelo catálogo, monta um
carrinho e fecha o pedido pelo site.

Hoje uma papelaria de bairro só vende para quem entra na loja e controla o estoque no papel.
Com o sistema ela passa a vender pela internet, mostra o que tem em estoque e guarda o histórico
de todos os pedidos.

## Perfis de acesso

O sistema tem dois perfis, controlados pelo Spring Security, além do visitante sem conta:

| Perfil | Quem é | O que pode fazer |
|---|---|---|
| (nenhum) | Visitante, ainda sem conta | Ver o catálogo e os detalhes dos produtos, montar o carrinho, criar conta e entrar |
| `ROLE_USER` | Cliente cadastrado | Tudo do visitante, mais: finalizar o pedido e ver os próprios pedidos |
| `ROLE_ADMIN` | Dono da papelaria | Tudo do cliente, mais: CRUD completo de produtos e categorias, mudar o status dos pedidos e gerenciar usuários |

Resumindo: `ROLE_USER` é o perfil de visualização e compra. `ROLE_ADMIN` é o único que cria,
edita e exclui cadastros.

## Regras de negócio

Parte das regras já está no esqueleto (e-mail único, senha com BCrypt, quem se cadastra vira
`ROLE_USER`, pedido cancelado não muda e as exclusões protegidas). As demais serão implementadas
na próxima etapa, em uma camada de service.

### Catálogo
- Todo produto tem categoria, nome, preço e estoque. A descrição e a imagem são opcionais; sem
  imagem, o catálogo mostra uma ilustração padrão.
- Produto sem estoque continua no catálogo, marcado como **Indisponível**, mas não pode ir para
  o carrinho.
- Só o admin cria, edita e exclui produtos e categorias.

### Carrinho e pedido
- A quantidade no carrinho não pode passar do estoque disponível.
- É preciso estar logado para finalizar a compra.
- Ao confirmar o pedido, o estoque é descontado. Se o admin cancelar o pedido, o estoque volta.
- O pedido guarda o preço de cada item no momento da compra (campo `precoUnitario` de
  `ItemPedido`). Se o preço do produto mudar depois, o pedido antigo continua com o valor pago.
- O pedido passa pelos status: Aguardando pagamento, Pago, Enviado, Entregue e Cancelado. Só o
  admin muda o status, e um pedido cancelado não muda mais.

### Usuários
- O e-mail é único: não dá para cadastrar duas contas com o mesmo e-mail.
- A senha é guardada criptografada com BCrypt, nunca em texto puro.
- Quem se cadastra pelo site sempre vira `ROLE_USER`. Só um admin pode promover alguém a
  `ROLE_ADMIN`.

### Exclusões
- Categoria que tem produtos não pode ser excluída.
- Produto que já foi vendido não pode ser excluído. Nesse caso o admin deixa o estoque em zero.
