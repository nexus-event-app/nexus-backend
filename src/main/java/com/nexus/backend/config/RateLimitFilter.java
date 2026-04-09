package com.nexus.backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        if (shouldRateLimit(path, method)) {
            String key = getClientKey(request, path);
            Bucket bucket = resolveBucket(key, path);

            if (bucket.tryConsume(1)) {
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean shouldRateLimit(String path, String method) {
        if (!method.equals("POST")) {
            return false;
        }
        
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/api/discussions") ||
               path.startsWith("/api/comments");
    }

    private String getClientKey(HttpServletRequest request, String path) {
        String ip = getClientIP(request);
        return ip + ":" + path;
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    private Bucket resolveBucket(String key, String path) {
        return cache.computeIfAbsent(key, k -> createBucket(path));
    }

    private Bucket createBucket(String path) {
        Bandwidth limit;
        
        if (path.startsWith("/api/auth/login")) {
            limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(15)));
        } else if (path.startsWith("/api/auth/register")) {
            limit = Bandwidth.classic(3, Refill.intervally(3, Duration.ofHours(1)));
        } else if (path.startsWith("/api/discussions")) {
            limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofHours(1)));
        } else if (path.startsWith("/api/comments")) {
            limit = Bandwidth.classic(30, Refill.intervally(30, Duration.ofHours(1)));
        } else {
            limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        }
        
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}