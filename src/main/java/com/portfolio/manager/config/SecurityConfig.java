package com.portfolio.manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança da aplicação.
 *
 * Por se tratar de um desafio técnico com escopo definido, optei por HTTP Basic Auth
 * em vez de JWT/OAuth2 pelos seguintes motivos:
 *
 * 1. Simplicidade: reduz complexidade e permite focar no domínio de negócio (projetos/membros)
 * 2. Documentação: HTTP Basic é facilmente testável via Swagger UI integrado
 * 3. Tempo: JWT/OAuth2 adicionaria overhead desnecessário para o escopo
 *
 * IMPORTANTE: Em produção, substituir por:
 * - JWT com refresh/access tokens
 * - OAuth2 com integração a provedor (AD, Google, etc.)
 * - HTTPS obrigatório (já que Basic Auth é codificado em base64)
 * - Rate limiting por usuário/IP
 */
@Configuration
public class SecurityConfig {

    /**
     * Define usuários padrão para testes e validação da API.
     * Credenciais em memória apenas para ambiente de desenvolvimento.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .roles("ADMIN")
                .build();

        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder.encode("user"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }

    /**
     * Cadeia de filtros de segurança.
     * 
     * Configuração:
     * - CSRF desabilitado: API stateless (REST) não é vulnerável a CSRF
     * - Swagger/OpenAPI liberado: documentação interativa deve ser acessível sem auth
     * - Todos outros endpoints: requerem autenticação HTTP Basic
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)  // Desabilitado pois API é stateless
                .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                ).permitAll()
                .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    /**
     * Codificador de senha usando delegação automática do Spring Security.
     * Suporta múltiplos formatos ({bcrypt}, {scrypt}, etc.) para compatibilidade.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}

