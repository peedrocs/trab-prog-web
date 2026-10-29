package com.mgp.lojaonline.config;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.repository.UsuarioRepository;
import com.mgp.lojaonline.service.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filtroDeSeguranca(HttpSecurity http, UsuarioService usuarioService) throws Exception {
        http
                .authorizeHttpRequests(autorizacao -> autorizacao
                        .requestMatchers("/", "/produtos/**", "/carrinho/**", "/login", "/cadastro", "/senha/**",
                                "/css/**", "/js/**", "/imagens/**", "/error", "/webhook/mercadopago").permitAll()
                        .requestMatchers("/login/codigo/**").hasAuthority("CODIGO_PENDENTE")
                        .requestMatchers("/cadastro/completar").hasAuthority("CADASTRO_INCOMPLETO")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().hasAnyRole("USER", "ADMIN"))
                .formLogin(login -> login
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("senha")
                        // senha certa ainda nao libera a conta: manda o codigo por e-mail e pede para digitar
                        .successHandler((request, response, autenticacao) -> {
                            usuarioService.enviarCodigoDeLogin(autenticacao.getName());
                            response.sendRedirect(request.getContextPath() + "/login/codigo");
                        })
                        .permitAll())
                .oauth2Login(google -> google
                        .loginPage("/login")
                        .failureUrl("/login?erroGoogle")
                        .userInfoEndpoint(dados -> dados.oidcUserService(usuarioDoGoogle(usuarioService)))
                        .successHandler((request, response, autenticacao) -> {
                            boolean faltaCpf = autenticacao.getAuthorities().stream()
                                    .anyMatch(permissao -> permissao.getAuthority().equals("CADASTRO_INCOMPLETO"));
                            response.sendRedirect(request.getContextPath() + (faltaCpf ? "/cadastro/completar" : "/"));
                        }))
                .logout(logout -> logout.permitAll())
                // o Mercado Pago nao tem como mandar o token CSRF; o webhook confere a assinatura dele
                .csrf(csrf -> csrf.ignoringRequestMatchers("/webhook/mercadopago"));

        return http.build();
    }

    // busca o usuario pelo e-mail para conferir a senha; o perfil de verdade so vem depois do codigo
    @Bean
    public UserDetailsService detalhesDoUsuario(UsuarioRepository usuarioRepository) {
        return email -> {
            Usuario usuario = usuarioRepository.findByEmail(email.trim().toLowerCase())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

            return User.withUsername(usuario.getEmail())
                    .password(usuario.getSenha())
                    .authorities("CODIGO_PENDENTE")
                    .disabled(!usuario.isAtivo())
                    .build();
        };
    }

    // no primeiro login pelo Google a conta e criada aqui, ainda sem CPF
    private OAuth2UserService<OidcUserRequest, OidcUser> usuarioDoGoogle(UsuarioService usuarioService) {
        OidcUserService servicoPadrao = new OidcUserService();
        return requisicao -> {
            OidcUser usuarioGoogle = servicoPadrao.loadUser(requisicao);
            if (!Boolean.TRUE.equals(usuarioGoogle.getEmailVerified())) {
                throw new OAuth2AuthenticationException(new OAuth2Error("email_nao_verificado"));
            }
            try {
                Usuario usuario = usuarioService.entrarComGoogle(usuarioGoogle.getEmail(), usuarioGoogle.getFullName());
                return new DefaultOidcUser(usuarioService.permissoes(usuario), usuarioGoogle.getIdToken(),
                        usuarioGoogle.getUserInfo(), "email");
            } catch (RegraNegocioException e) {
                throw new OAuth2AuthenticationException(new OAuth2Error("conta_desativada"), e.getMessage());
            }
        };
    }

    @Bean
    public PasswordEncoder codificadorDeSenha() {
        return new BCryptPasswordEncoder();
    }
}
