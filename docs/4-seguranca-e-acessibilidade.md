# 4. Segurança e acessibilidade

## Spring Security

Toda a configuração está em um arquivo só:
[`config/SecurityConfig.java`](../src/main/java/com/mgp/lojaonline/config/SecurityConfig.java).

### Quem pode acessar o quê

```java
.requestMatchers("/", "/produtos/**", "/carrinho/**", "/cadastro", "/login",
        "/css/**", "/js/**", "/imagens/**", "/error").permitAll()
.requestMatchers("/admin/**").hasRole("ADMIN")
.anyRequest().authenticated()
```

- **Livre para todos:** catálogo, página do produto, carrinho, login, cadastro e os arquivos de
  CSS, JS e imagens.
- **Só para o admin:** tudo que começa com `/admin/`.
- **Qualquer outra rota exige login:** é o caso de `/pedidos/**` e `/avaliacoes`, ou seja,
  finalizar a compra, ver o histórico e avaliar.

### Login

- Formulário próprio em `/login`, com os campos `email` e `senha`.
- A classe [`AutenticacaoService`](../src/main/java/com/mgp/lojaonline/service/AutenticacaoService.java)
  implementa `UserDetailsService`: ela busca o usuário pelo e-mail, entrega a senha criptografada
  ao Spring Security e diz qual é o perfil dele (`ROLE_USER` ou `ROLE_ADMIN`).
- Usuário inativo não entra, porque o `AutenticacaoService` o marca como `disabled`.

### Senhas

As senhas são guardadas com **BCrypt**, nunca em texto puro. O bean está no `SecurityConfig`:

```java
@Bean
public PasswordEncoder codificadorDeSenha() {
    return new BCryptPasswordEncoder();
}
```

No cadastro, o `UsuarioService` criptografa a senha antes de salvar, força o perfil `ROLE_USER` e
ignora qualquer id ou perfil que venha do formulário. Sem isso, alguém poderia forjar o formulário
e se cadastrar direto como administrador.

### Proteção contra CSRF

Vem ligada por padrão no Spring Security. Como todo formulário usa `th:action`, o Thymeleaf insere
sozinho o campo escondido com o token, e um site externo não consegue enviar formulários no lugar
do usuário.

### Sessões

O `SecurityConfig` registra as sessões abertas em um `SessionRegistry`. Quando o admin desativa um
usuário ou muda o perfil dele, o `UsuarioService` encerra a sessão dessa pessoa na hora, e ela é
mandada de volta para o login. Sem isso, a mudança só valeria no próximo login.

## Acessibilidade (WCAG 2.1 AA)

### Contraste
Todas as cores foram escolhidas e conferidas com cálculo de contraste. O mínimo exigido é 4,5:1
para texto normal e 3:1 para bordas e contornos. Exemplos do que usamos:

| Combinação | Contraste |
|---|---|
| Texto do site sobre o fundo claro | 13,3:1 |
| Verde dos links sobre o branco | 5,0:1 |
| Branco sobre o verde dos botões | 5,0:1 |
| Branco sobre o verde escuro do cabeçalho | 9,1:1 |
| "Em estoque" em verde sobre o branco | 7,1:1 |

Nenhuma informação depende só da cor: o status do pedido, o estoque e a situação do usuário vêm
sempre escritos por extenso.

### Tags semânticas
As páginas usam `header`, `nav`, `main`, `section`, `article` e `footer`, com um único
`h1` por página e títulos em ordem (`h1`, depois `h2`, depois `h3`). As tabelas têm `caption` e
`th scope="col"`.

### Atributos ARIA
- `aria-label` nos menus e nos botões que se repetem em listas, por exemplo
  "Excluir Caneta Esferográfica Azul", para o leitor de tela saber de qual produto se trata.
- `aria-invalid` e `aria-describedby` ligam cada campo com erro à mensagem correspondente.
- `aria-current` marca o item de menu da página atual.
- `role="alert"` e `role="status"` fazem o leitor de tela anunciar os avisos de erro e de sucesso.
- `aria-hidden="true"` no ícone ☰, que é enfeite: o botão já se chama "Menu".

### Teclado
- Link **Pular para o conteúdo**, que aparece ao apertar Tab e leva direto ao conteúdo da página.
- Contorno de foco visível em todos os botões, links e campos.
- O menu sanfona usa `<details>`/`<summary>`, que já funciona pelo teclado, e fecha com Esc.

### Outros cuidados
- `lang="pt-BR"` no HTML, para o leitor de tela usar a pronúncia certa.
- Todas as imagens têm `alt`, e as que são só enfeite ficam com `alt=""`.
- O layout funciona em telas de 400px de largura, sem rolagem horizontal na página.
