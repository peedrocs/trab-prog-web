# 1. Tema e regras de negócio

## O tema

O projeto é uma **papelaria online**: uma loja fechada, no estilo Americanas, em que o cliente
navega pelo catálogo, monta um carrinho e fecha o pedido pelo site.

**O problema que resolve:** uma papelaria de bairro só vende para quem entra na loja e anota o
estoque no papel. Com o sistema ela passa a vender pela internet, mostra o que tem em estoque em
tempo real, calcula frete e desconto sozinha e guarda o histórico de todos os pedidos.

## Perfis de acesso

O sistema tem dois perfis, controlados pelo Spring Security:

| Perfil | Quem é | O que pode fazer |
|---|---|---|
| (nenhum) | Visitante, ainda sem conta | Ver o catálogo, buscar, filtrar por categoria e montar o carrinho |
| `ROLE_USER` | Cliente cadastrado | Tudo do visitante, mais: finalizar a compra, ver os próprios pedidos e avaliar produtos que comprou |
| `ROLE_ADMIN` | Dono da papelaria | Tudo do cliente, mais: CRUD de produtos, categorias e cupons, mudar o status dos pedidos e gerenciar usuários |

O visitante pode montar o carrinho, mas o login é exigido na hora de finalizar a compra.

## Regras de negócio

### Catálogo
- Todo produto tem categoria, nome, descrição, preço, estoque e imagem.
- Produto sem estoque continua visível no catálogo, marcado como **Indisponível**, mas não pode ser
  adicionado ao carrinho.
- Só o admin cria, edita ou remove produtos e categorias.

### Carrinho e pedido
- A quantidade pedida nunca pode passar do estoque disponível.
- É preciso estar logado para finalizar a compra.
- Ao confirmar o pedido, o estoque é descontado. Se o admin cancelar o pedido, o estoque volta.
- O pedido passa pelos status: Aguardando pagamento, Pago, Em separação, Enviado, Entregue e
  Cancelado. Só o admin muda o status, e um pedido cancelado não muda mais.

### Preço, frete e cupom
- O pedido guarda o preço de cada item no momento da compra. Se o preço do produto mudar depois,
  o pedido antigo continua com o valor que o cliente pagou.
- Frete grátis quando os produtos somam R$ 150,00 ou mais (antes do desconto do cupom). Abaixo
  disso: R$ 15,00 até 5 itens, ou R$ 25,00 acima de 5 itens.
- O cupom pode ser percentual ou de valor fixo, tem data de validade e só pode ser usado uma vez
  por cliente. Se o pedido for cancelado, o cliente pode usar o cupom de novo.

### Usuários
- O e-mail é único: não dá para cadastrar duas contas com o mesmo e-mail.
- O CPF é opcional.
- A senha é guardada criptografada com BCrypt, nunca em texto puro.
- Quem se cadastra pelo site sempre vira `ROLE_USER`; só outro admin pode promover alguém.
- Usuário inativo não consegue entrar. Ao mudar o perfil ou a situação de alguém, a sessão dessa
  pessoa é encerrada na hora.

### Avaliações
- Só quem comprou o produto pode avaliar, com nota de 1 a 5 e um comentário opcional.
- Uma avaliação por produto por cliente.
- Pedido cancelado não dá direito de avaliar.

### Regras de exclusão
Para não deixar dados quebrados no banco, o sistema recusa três exclusões:
- categoria que tem produtos;
- produto que já foi vendido (nesse caso o admin deixa o estoque em 0);
- cupom que já foi usado em algum pedido (nesse caso o admin muda a validade).
