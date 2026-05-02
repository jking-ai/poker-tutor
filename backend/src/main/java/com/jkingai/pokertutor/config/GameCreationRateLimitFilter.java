package com.jkingai.pokertutor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Per-IP rate limiter for game creation. Caps how many new games a single
 * client IP can spawn per hour, complementing the existing per-game and
 * per-hand caps in {@link com.jkingai.pokertutor.service.RateLimitService}.
 *
 * <p>Only applied to {@code POST /api/v1/games}. GETs on existing games and
 * the health endpoint are left untouched.
 */
@Component
public class GameCreationRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(GameCreationRateLimitFilter.class);
    private static final String GAME_CREATION_PATH = "/api/v1/games";

    private final int gamesPerHourPerIp;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public GameCreationRateLimitFilter(
            @Value("${app.limits.games-created-per-hour-per-ip:3}") int gamesPerHourPerIp,
            ObjectMapper objectMapper) {
        this.gamesPerHourPerIp = gamesPerHourPerIp > 0 ? gamesPerHourPerIp : 3;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = resolveClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(clientIp, k -> createBucket());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            long retryAfterSeconds = Math.max(1L, probe.getNanosToWaitForRefill() / 1_000_000_000L);
            log.warn("Game creation rate limit hit for IP {} (limit={}/hour, retryAfter={}s)",
                    clientIp, gamesPerHourPerIp, retryAfterSeconds);
            writeRateLimitResponse(response, retryAfterSeconds);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Skip everything that is not a POST to {@code /api/v1/games}. The path
     * must match exactly — sub-paths like {@code /api/v1/games/{id}/actions}
     * and {@code /api/v1/games/health} are intentionally excluded.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return !GAME_CREATION_PATH.equals(path);
    }

    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(gamesPerHourPerIp)
                        .refillIntervally(gamesPerHourPerIp, Duration.ofHours(1))
                        .build())
                .build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // X-Forwarded-For may contain a comma-separated chain; the
            // left-most entry is the original client.
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeRateLimitResponse(HttpServletResponse response, long retryAfterSeconds)
            throws IOException {
        response.setStatus(429); // HTTP 429 Too Many Requests
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

        Map<String, Object> body = Map.of(
                "error", "GAME_CREATION_RATE_LIMITED",
                "message", "Too many games created from this IP. Try again later.",
                "retryAfterSeconds", retryAfterSeconds
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
