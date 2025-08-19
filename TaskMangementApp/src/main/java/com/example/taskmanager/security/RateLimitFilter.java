package com.example.taskmanager.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very simple IP-based limiter:
 * - 100 requests / 10 minutes burst
 * - 20 requests / 10 seconds (smaller burst)
 * You can tune per-path (e.g., stricter for write endpoints).
 */
@Component
public class RateLimitFilter implements Filter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket newBucket() {
        Bandwidth big = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(10)));
        Bandwidth small = Bandwidth.classic(20, Refill.greedy(20, Duration.ofSeconds(10)));
        return Bucket.builder().addLimit(big).addLimit(small).build();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String ip = request.getRemoteAddr();
        String key = ip; // you can combine path or user email for finer control

        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket());
        if (bucket.tryConsume(1)) {
            chain.doFilter(req, res);
        } else {
            response.setStatus(429);
            response.getWriter().write("Too Many Requests");
        }
    }
}