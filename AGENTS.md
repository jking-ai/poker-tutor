# AGENTS.md

Guidance for AI coding agents working in this repository. See [README.md](README.md) for full project details, architecture, deployment, configuration, and security.

## Quick Reference

- **Backend:** Spring Boot 3.4 + Spring AI + Spring Security (Java 21)
- **Frontend:** React 19 + MUI + Vite SPA
- **LLM:** Vertex AI Gemini 2.0 Flash via Spring AI ChatClient
- **Packaging:** Single Docker container — frontend baked into backend static resources
- **Monorepo:** `backend/` (Java/Gradle) and `frontend/` (React/npm)

## Build & Run Commands

```bash
# Backend
cd backend
./gradlew bootRun                # Dev server on :8080
./gradlew test                   # Run all tests (68 tests)
./gradlew build                  # Full build

# Frontend
cd frontend
npm install                      # Install dependencies
npm run dev                      # Dev server on :5173, proxies /api to :8080
npm run build                    # Production build to ./dist/

# Deploy
./deploy.sh                      # Build + deploy to remote server (user@your-server)
docker compose up --build        # Local Docker build + run
```

## Conventions for Code Changes

### Backend (`com.jkingai.pokertutor`)
- **Game model:** Human = `players[0]`, AI = `players[1]`. Heads-up: dealer = small blind.
- **Pre-flop:** Action starts with dealer (SB). Post-flop: non-dealer acts first.
- **Betting round complete:** Both players acted AND bets equalized (or all-in).
- **AI fallback:** If AI is disabled or rate-limited, opponent silently falls back to random play.
- **CSRF:** All POST endpoints require a valid `X-XSRF-TOKEN` header. Tests must use `.with(csrf())`. Health endpoint is exempt.
- **Rate limiting:** `RateLimitService` enforces limits. Throws `RateLimitException` (HTTP 429) for game/coaching limits. Returns boolean for AI call limit (graceful degradation, no exception).
- **Scheduled cleanup:** `GameService.cleanupStaleGames()` runs every 10 min, removes games older than 2 hours.
- **Spring AI BOM:** Version `1.0.0-M5` (not `1.0.0`). Auto-config excluded: `VertexAiGeminiAutoConfiguration`.
- **Gradle wrapper:** 8.12, located in `backend/`.

### Frontend (`frontend/src/`)
- **API client** (`api/client.js`): Reads `XSRF-TOKEN` cookie and sends as `X-XSRF-TOKEN` header. Base URL defaults to `''` (same-origin).
- **Coaching errors:** `useCoaching` hook returns structured error `{ message, code, status }`. `CoachingPanel` disables button when `code === 'COACHING_LIMIT'`.
- **Vite proxy:** `/api` requests proxy to backend in dev mode. Configured in `vite.config.js`.

### Docker
- Runtime image: `eclipse-temurin:21-jre-jammy` (not Alpine — gRPC/Netty tcnative crashes on musl).
- `CORS_ALLOWED_ORIGINS` is set to empty string in Docker (same-origin mode).
- Container uses `--restart unless-stopped` for reboot survival.

## Key Files

| Area | Files |
|------|-------|
| Game engine | `model/Game.java`, `service/GameService.java` |
| AI agents | `service/OpponentAgentService.java`, `service/CoachAgentService.java` |
| Math | `service/HandEvaluatorService.java`, `service/OddsCalculatorService.java` |
| Rate limiting | `service/RateLimitService.java`, `exception/RateLimitException.java` |
| Security | `config/SecurityConfig.java`, `config/WebConfig.java` |
| SPA serving | `config/SpaForwardingConfig.java` |
| API | `controller/GameController.java`, `controller/CoachingController.java` |
| Frontend API | `api/client.js`, `hooks/useGame.js`, `hooks/useCoaching.js` |
| Prompts | `src/main/resources/prompts/` |
| Deploy | `deploy.sh`, `Dockerfile`, `docker-compose.yml` |
