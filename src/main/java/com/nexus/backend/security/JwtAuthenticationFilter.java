package com.nexus.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Autentica la petición a partir del JWT si lo hay. Un token ausente, inválido o de una cuenta
 * desactivada deja la petición como anónima; la autorización la decide SecurityConfig.
 * No se registra como bean para que solo se ejecute dentro de la cadena de seguridad.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserDetailService userDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(request, header.substring(BEARER_PREFIX.length()).trim());
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, String token) {
        jwtUtil.parse(token).ifPresent(claims -> {
            try {
                UserDetail userDetail = userDetailService.loadUserByUsername(claims.email());
                if (!userDetail.isEnabled() || issuedBeforeCredentialsChange(userDetail, claims)) {
                    return;
                }
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetail, null, userDetail.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (UsernameNotFoundException ex) {
                // El usuario del token ya no existe: se trata como anónimo.
            }
        });
    }

    /** Tras cambiar la contraseña, los tokens emitidos antes dejan de valer (la fecha del JWT va en segundos). */
    private static boolean issuedBeforeCredentialsChange(UserDetail userDetail, JwtUtil.TokenClaims claims) {
        LocalDateTime changedAt = userDetail.getUser().getCredentialsChangedAt();
        if (changedAt == null) {
            return false;
        }
        Instant changed = changedAt.atZone(ZoneId.systemDefault()).toInstant().truncatedTo(ChronoUnit.SECONDS);
        return claims.issuedAt().isBefore(changed);
    }
}
