package com.mgp.lojaonline.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filtroDeSeguranca(HttpSecurity http, SessionRegistry registroDeSessoes) throws Exception {
        http
                .authorizeHttpRequests(autorizacao -> autorizacao
                        .requestMatchers("/", "/produtos/**", "/carrinho/**", "/cadastro", "/login",
                                "/css/**", "/js/**", "/imagens/**", "/error").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("senha")
                        .defaultSuccessUrl("/")
                        .permitAll())
                .logout(logout -> logout.permitAll())
                // registra as sessões para o admin conseguir encerrar a de um usuário alterado
                .sessionManagement(sessao -> sessao.sessionConcurrency(concorrencia -> concorrencia
                        .maximumSessions(-1)
                        .sessionRegistry(registroDeSessoes)
                        .expiredUrl("/login?expirou")));

        return http.build();
    }

    @Bean
    public SessionRegistry registroDeSessoes() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher publicadorDeEventosDeSessao() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public PasswordEncoder codificadorDeSenha() {
        return new BCryptPasswordEncoder();
    }
}
