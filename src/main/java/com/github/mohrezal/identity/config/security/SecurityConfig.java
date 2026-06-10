package com.github.mohrezal.identity.config.security;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthInvalidCredentialsException;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {
    private final ApplicationProperties applicationProperties;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String[] PUBLIC_GET_PATH = {
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        RouteConstants.build(RouteConstants.Auth.BASE, RouteConstants.Auth.CSRF),
        RouteConstants.build(RouteConstants.Auth.BASE, RouteConstants.Auth.VERIFY_EMAIL),
    };

    private static final String[] PUBLIC_POST_PATH = {
        RouteConstants.build(RouteConstants.User.BASE, RouteConstants.User.REGISTER),
        RouteConstants.build(
                RouteConstants.Auth.BASE, RouteConstants.Auth.RESEND_EMAIL_VERIFICATION),
        RouteConstants.build(RouteConstants.Auth.BASE, RouteConstants.Auth.LOGIN)
    };

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource,
            CookieCsrfTokenRepository csrfTokenRepository)
            throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.spa().csrfTokenRepository(csrfTokenRepository))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(
                        exception ->
                                exception
                                        .authenticationEntryPoint(
                                                (request, response, authException) ->
                                                        response.sendError(
                                                                HttpServletResponse
                                                                        .SC_UNAUTHORIZED))
                                        .accessDeniedHandler(
                                                (request, response, accessDeniedException) ->
                                                        response.sendError(
                                                                HttpServletResponse.SC_FORBIDDEN)))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_PATH)
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_PATH)
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username ->
                userRepository
                        .findByEmail(username)
                        .orElseThrow(AuthInvalidCredentialsException::new);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            ApplicationProperties applicationProperties) {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(applicationProperties.security().allowedOrigins());
        configuration.setAllowedMethods(
                List.of(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.PATCH.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.OPTIONS.name()));
        configuration.setAllowedHeaders(
                List.of(
                        HttpHeaders.CONTENT_TYPE,
                        HttpHeaders.ACCEPT,
                        HttpHeaders.ACCEPT_LANGUAGE,
                        "X-XSRF-TOKEN",
                        "X-Request-Id"));
        configuration.setExposedHeaders(List.of(HttpHeaders.LOCATION, "X-Request-Id"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3_600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public CookieCsrfTokenRepository csrfTokenRepository() {
        var csrfCookie = applicationProperties.security().cookie().csrf();
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath(csrfCookie.path());
        repository.setCookieCustomizer(
                cookie -> {
                    cookie.path(csrfCookie.path())
                            .secure(csrfCookie.secure())
                            .sameSite(csrfCookie.sameSite());
                });
        return repository;
    }
}
