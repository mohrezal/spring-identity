package com.github.mohrezal.identity.config.security;

import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.constant.CookieConstant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null
                && request.getCookies() != null) {
            var accessToken =
                    Arrays.stream(request.getCookies())
                            .filter(cookie -> CookieConstant.ACCESS_TOKEN.equals(cookie.getName()))
                            .map(Cookie::getValue)
                            .filter(StringUtils::hasText)
                            .findFirst();

            accessToken
                    .flatMap(jwtTokenProvider::extractUserId)
                    .flatMap(userRepository::findById)
                    .filter(
                            user ->
                                    user.isEnabled()
                                            && user.isAccountNonExpired()
                                            && user.isAccountNonLocked()
                                            && user.isCredentialsNonExpired())
                    .ifPresent(
                            user -> {
                                var authentication =
                                        UsernamePasswordAuthenticationToken.authenticated(
                                                user, null, user.getAuthorities());
                                authentication.setDetails(
                                        new WebAuthenticationDetailsSource().buildDetails(request));

                                var context = SecurityContextHolder.createEmptyContext();
                                context.setAuthentication(authentication);
                                SecurityContextHolder.setContext(context);
                            });
        }

        filterChain.doFilter(request, response);
    }
}
