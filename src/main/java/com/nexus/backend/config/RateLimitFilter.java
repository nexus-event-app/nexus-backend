package com.nexus.backend.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nexus.backend.exception.ApiErrorWriter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Limita por IP las peticiones POST sensibles. Usa {@code request.getRemoteAddr()}: detrás de un proxy
 * de confianza hay que activar {@code server.forward-headers-strategy} para que Spring resuelva la IP real.
 * No se registra como bean para que solo se ejecute dentro de la cadena de seguridad.
 */
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private record Rule(String name, String path, int capacity, Duration period) {
    }

    private static final List<Rule> RULES = List.of(
            new Rule("login", "/api/auth/login", 10, Duration.ofMinutes(15)),
            new Rule("register", "/api/auth/register", 5, Duration.ofHours(1)),
            new Rule("forgot-password", "/api/auth/forgot-password", 5, Duration.ofHours(1)),
            new Rule("reset-password", "/api/auth/reset-password", 10, Duration.ofHours(1)),
            new Rule("verify-email", "/api/auth/verify-email", 20, Duration.ofHours(1)),
            new Rule("resend-verification", "/api/auth/resend-verification", 5, Duration.ofHours(1)),
            new Rule("discussion", "/api/discussions", 10, Duration.ofHours(1)),
            new Rule("comment", "/api/comments", 30, Duration.ofHours(1)),
            new Rule("testimonial", "/api/testimonials", 5, Duration.ofHours(1)));

    private final ApiErrorWriter apiErrorWriter;

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .maximumSize(100_000)
            .expireAfterAccess(Duration.ofHours(2))
            .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Optional<Rule> rule = "POST".equals(request.getMethod()) ? findRule(request.getServletPath()) : Optional.empty();
        if (rule.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        Rule matched = rule.get();
        Bucket bucket = buckets.get(matched.name() + ":" + request.getRemoteAddr(), key -> newBucket(matched));
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(1L, (long) Math.ceil(probe.getNanosToWaitForRefill() / 1_000_000_000.0));
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
        apiErrorWriter.write(request, response, HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS",
                "Demasiadas peticiones. Inténtalo de nuevo más tarde");
    }

    private Optional<Rule> findRule(String path) {
        return RULES.stream().filter(rule -> rule.path().equals(path)).findFirst();
    }

    private Bucket newBucket(Rule rule) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(rule.capacity())
                .refillIntervally(rule.capacity(), rule.period())
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
