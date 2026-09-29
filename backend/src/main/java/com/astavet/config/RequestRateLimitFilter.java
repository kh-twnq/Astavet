package com.astavet.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestRateLimitFilter extends OncePerRequestFilter {

    private static final Duration WINDOW = Duration.ofMinutes(10);
    private static final int ORDER_LIMIT = 20;
    private static final int LOGIN_LIMIT = 10;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final Clock clock = Clock.systemUTC();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        int limit = limitFor(request);
        if (limit > 0 && !allow(request.getRemoteAddr() + ':' + request.getRequestURI(), limit)) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.getWriter().write("{\"title\":\"Too Many Requests\",\"status\":429,"
                    + "\"detail\":\"Bạn thao tác quá nhanh. Vui lòng thử lại sau.\",\"code\":\"RATE_LIMITED\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private int limitFor(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return 0;
        }
        return switch (request.getRequestURI()) {
            case "/api/v1/orders" -> ORDER_LIMIT;
            case "/api/v1/admin/auth/login" -> LOGIN_LIMIT;
            default -> 0;
        };
    }

    private boolean allow(String key, int limit) {
        Instant now = clock.instant();
        Window updated = windows.compute(key, (ignored, current) -> {
            if (current == null || Duration.between(current.startedAt(), now).compareTo(WINDOW) >= 0) {
                return new Window(now, 1);
            }
            return new Window(current.startedAt(), current.count() + 1);
        });
        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(entry -> Duration.between(entry.getValue().startedAt(), now).compareTo(WINDOW) >= 0);
        }
        return updated.count() <= limit;
    }

    private record Window(Instant startedAt, int count) {
    }
}
