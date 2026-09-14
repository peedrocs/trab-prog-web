# 6. Guia da apresentação

A apresentação tem de 10 a 15 minutos, e **todos os integrantes precisam falar**.

## Antes de sair de casa

- [ ] Testar o projeto no notebook que vai ser levado, **rodando de verdade**.
- [ ] Conferir que o notebook tem JDK 17 ou mais novo e o `JAVA_HOME` apontando para ele.
- [ ] Abrir o Docker Desktop e esperar aparecer "Engine running".
- [ ] Rodar o projeto uma vez com internet antes do dia: a primeira execução baixa as dependências
      do Maven e a imagem do MySQL, o que demora alguns minutos.
- [ ] Deixar aberto no navegador: o site, o código na IDE e os slides.
- [ ] Levar o `.zip` entregue, por segurança.

Comando para rodar (PowerShell, dentro da pasta do projeto):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-24'
.\mvnw.cmd spring-boot:run
```

Contas de teste: `admin@papelaria.com` / `admin123` e `cliente@papelaria.com` / `cliente123`.
Cupons: `BEMVINDO10`, `PAPEL5` e `VENCIDO20` (vencido de propósito).

## Divisão das falas

Seis integrantes, seis partes. Ajustem como preferirem; a ideia é que cada um domine a sua parte.

| Parte | Quem apresenta | O que mostrar | Onde estudar |
|---|---|---|---|
| 1. Tema, problema e regras de negócio | Guilherme Carvalho de Oliveira | O que a loja resolve, os perfis e as principais regras | [1-tema-e-regras.md](1-tema-e-regras.md) |
| 2. Casos de uso e atores | Marcus Vinícius Perez da Silva Balbuena | O diagrama, os três atores e o que cada um pode fazer | [2-casos-de-uso.md](2-casos-de-uso.md) |
| 3. Demonstração da loja | Sérgio Henrique Lamarque Silva | A compra ao vivo: busca, carrinho, cupom, frete, pedido e avaliação | [1-tema-e-regras.md](1-tema-e-regras.md) |
| 4. Arquitetura, camadas e banco | Gustavo de Freitas Cabrera | As pastas na IDE, uma entidade, um repository e o MySQL no Docker | [3-arquitetura.md](3-arquitetura.md) |
| 5. Segurança e acessibilidade | Lucas dos Reis Schweich | O `SecurityConfig`, o BCrypt e os cuidados de acessibilidade | [4-seguranca-e-acessibilidade.md](4-seguranca-e-acessibilidade.md) |
| 6. Área administrativa e diferenciais | Pedro Cavalcante Sanches | O CRUD de produtos, a mudança de status e os diferenciais | [5-diferenciais.md](5-diferenciais.md) |

## Roteiro sugerido (12 minutos)

| Tempo | Quem | O que fazer |
|---|---|---|
| 0:00 – 1:30 | Parte 1 | Apresentar o grupo e o tema: papelaria online, o problema e os dois perfis |
| 1:30 – 3:00 | Parte 1 | Regras principais: estoque, frete, cupom, status do pedido e avaliação |
| 3:00 – 4:30 | Parte 2 | Diagrama de casos de uso e os três atores |
| 4:30 – 7:00 | Parte 3 | **Demonstração ao vivo:** buscar produto, tentar comprar sem estoque, aplicar cupom, finalizar o pedido e avaliar |
| 7:00 – 8:30 | Parte 4 | Código na IDE: as camadas, uma entidade e o banco no Docker |
| 8:30 – 10:00 | Parte 5 | O `SecurityConfig`, as senhas com BCrypt e a acessibilidade |
| 10:00 – 12:00 | Parte 6 | Área administrativa ao vivo, diferenciais e o que falta para o trabalho final |

## Demonstração que impressiona (e é rápida)

1. **Estoque:** tentar comprar mais do que existe. O sistema recusa com mensagem clara.
2. **Cupom:** aplicar `BEMVINDO10` e mostrar o desconto; depois tentar usar de novo e ser recusado.
3. **Frete:** com o carrinho acima de R$ 150,00, o frete vira "Grátis" sozinho.
4. **Cancelamento:** cancelar um pedido no admin e mostrar o estoque do produto voltando.
5. **Avaliação:** tentar avaliar um produto que não foi comprado e ser recusado.
6. **Segurança:** logado como cliente, digitar `/admin` na barra de endereço e cair na página
   "Acesso negado".
7. **Acessibilidade:** navegar pela tela usando só o Tab e mostrar o link "Pular para o conteúdo".

## Perguntas que o professor pode fazer

- **Qual banco vocês usaram?** MySQL 8, rodando em container do Docker, criado pelo `compose.yaml`.
- **Onde ficam as regras de negócio?** Nas classes da pasta `service`. O controller só recebe a
  requisição e escolhe a página.
- **Como funciona o controle de acesso?** Pelo `SecurityConfig`: `/admin/**` exige `ROLE_ADMIN`,
  o catálogo é livre e o resto exige login.
- **As senhas ficam salvas como?** Criptografadas com BCrypt.
- **Como o preço do pedido não muda depois?** O `ItemPedido` guarda o preço no momento da compra.
- **O que é o Lombok?** Uma biblioteca que gera getters, setters e construtores na compilação, para
  a entidade não ficar cheia de código repetido.
