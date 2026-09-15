# 4. Segurança e acessibilidade

## Spring Security

Toda a configuração inicial está em um arquivo só:
[`config/SecurityConfig.java`](../src/main/java/com/mgp/lojaonline/config/SecurityConfig.java).

### Quem pode acessar o quê

```java
.requestMatchers("/", "/login", "/css/**", "/js/**", "/imagens/**", "/error").permitAll()
.requestMatchers("/admin/**").hasRole("ADMIN")
.anyRequest().authenticated()
```

- Catálogo, login e os arquivos de CSS, JS e imagens ficam livres para todos.
- Tudo que começa com `/admin/` é só para o admin. Um cliente logado que tenta entrar recebe a
  página de acesso negado (erro 403).
- Qualquer outra rota exige login. É assim que as rotas do carrinho fechado e dos pedidos vão
  ficar protegidas quando forem criadas. Por causa dessa regra, um visitante que digita um
  endereço inexistente é levado ao login; a página 404 própria aparece para quem já está logado.

### Login

- Formulário próprio em `/login`, com os campos `email` e `senha`.
- O bean `UserDetailsService` do `SecurityConfig` busca o usuário no banco pelo e-mail
  (`UsuarioRepository.findByEmail`) e entrega ao Spring Security a senha criptografada e o perfil
  (`ROLE_USER` ou `ROLE_ADMIN`).
- Depois do login o usuário volta para o catálogo. O menu mostra o link **Administração** só
  para quem tem `ROLE_ADMIN` (`sec:authorize="hasRole('ADMIN')"` no Thymeleaf).

### Senhas

As senhas são guardadas com BCrypt, nunca em texto puro:

```java
@Bean
public PasswordEncoder codificadorDeSenha() {
    return new BCryptPasswordEncoder();
}
```

A classe `DadosIniciais` usa esse bean para criar as contas de teste já criptografadas.

### Proteção contra CSRF

Vem ligada por padrão no Spring Security. Como todo formulário usa `th:action`, o Thymeleaf
insere sozinho o campo escondido com o token.

### O que vem na próxima etapa

- Cadastro público em `/cadastro`, que criptografa a senha e força o perfil `ROLE_USER`,
  ignorando qualquer perfil enviado pelo formulário.
- Rotas do carrinho, dos pedidos e do CRUD, seguindo as mesmas regras acima.

## Acessibilidade (WCAG 2.1 AA)

As diretrizes abaixo já estão aplicadas nas páginas prontas e nos mockups, e valem para todas as
telas que ainda serão codificadas.

### Contraste

O mínimo exigido é 4,5:1 para texto normal e 3:1 para bordas e contornos. As cores do
`estilo.css` foram conferidas com cálculo de contraste:

| Combinação | Contraste |
|---|---|
| Texto do site sobre o fundo cinza claro | 13,3:1 |
| Verde dos links sobre o branco | 5,0:1 |
| Verde dos links sobre o fundo cinza claro | 4,6:1 |
| Branco sobre o verde dos botões | 5,0:1 |
| Branco sobre o verde escuro do cabeçalho | 9,1:1 |
| Branco sobre o vermelho do botão Excluir | 6,5:1 |
| Texto de apoio (cinza) sobre o branco | 7,6:1 |
| Borda dos campos de formulário sobre o branco | 4,8:1 |

Nenhuma informação depende só da cor: o estoque e o status do pedido vêm sempre escritos por
extenso.

### Tags semânticas

As páginas usam `header`, `nav`, `main`, `section`, `article` e `footer`, com um único `h1` por
página e títulos em ordem (`h1`, depois `h2`). Todo campo de formulário tem `label`, e as tabelas
têm `caption` e `th scope="col"`.

### Atributos ARIA

- `aria-label` nos menus e nos botões que se repetem em listas, por exemplo
  "Editar Caneta esferográfica azul", para o leitor de tela saber de qual produto se trata.
- `aria-current` marca o item de menu da página atual.
- `role="alert"` e `role="status"` fazem o leitor de tela anunciar os avisos de erro e de sucesso.
- `aria-invalid` e `aria-describedby` vão ligar cada campo com erro à mensagem correspondente
  nos formulários de cadastro.
- `aria-hidden="true"` no ícone do menu, que é só enfeite.

### Teclado

- Link **Pular para o conteúdo**, que aparece ao apertar Tab e leva direto ao conteúdo.
- Contorno de foco visível em todos os botões, links e campos (`:focus-visible`).
- O menu usa `<details>` e `<summary>`, que já funcionam pelo teclado, e fecha com Esc.

### Outros cuidados

- `lang="pt-BR"` no HTML.
- Todas as imagens têm `alt`. As que são só enfeite ficam com `alt=""`.
- O layout funciona em telas de 400px de largura, sem rolagem horizontal na página.
