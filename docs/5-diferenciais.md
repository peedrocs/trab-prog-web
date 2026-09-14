# 5. Diferenciais

## O que já está pronto

| Diferencial | Como funciona |
|---|---|
| **Cupons de desconto** | Percentual ou valor fixo, com validade e uso único por cliente. Pedido cancelado libera o cupom de novo |
| **Cálculo de frete** | Grátis a partir de R$ 150,00 em produtos; abaixo disso muda conforme a quantidade de itens |
| **Avaliações com nota e comentário** | Só quem comprou avalia, uma vez por produto, e a página mostra a nota média |
| **Carrinho para visitante** | Dá para montar o carrinho sem conta; o login só é pedido na hora de fechar o pedido |
| **Preço histórico no pedido** | O pedido guarda quanto o cliente pagou, mesmo que o preço do produto mude depois |
| **Painel administrativo** | Mostra total de produtos, produtos sem estoque, pedidos aguardando pagamento e usuários |
| **Controle de estoque automático** | O estoque cai quando o pedido é confirmado e volta quando o admin cancela |
| **Gestão de usuários com sessão** | Ao desativar ou mudar o perfil de alguém, a sessão dessa pessoa é encerrada na hora |
| **Acessibilidade WCAG AA** | Contraste conferido por cálculo, navegação por teclado, ARIA e tags semânticas |
| **Mensagens de erro amigáveis** | Regras de negócio viram aviso na tela, e não erro do sistema. Páginas próprias para 404 e 403 |
| **Dados de exemplo automáticos** | Na primeira execução o sistema cria categorias, 16 produtos, cupons e contas de teste |
| **Banco em container** | O MySQL sobe junto com o projeto pelo Docker Compose, sem instalação manual |

## O que pretendemos acrescentar até o trabalho final

- **Upload de imagem do produto**, no lugar de informar a URL.
- **Paginação e ordenação** no catálogo (por preço e por mais vendidos).
- **Tela "Minha conta"**, para o cliente editar os próprios dados e trocar a senha.
- **Endereço de entrega** no pedido, com busca do CEP.
- **Relatório de vendas** no painel, com total por período e produtos mais vendidos.
- **Testes automatizados** das regras de negócio (estoque, frete e cupom).
