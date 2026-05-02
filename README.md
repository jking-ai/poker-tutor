# "The Nut" Poker Tutor

**One-line summary:** An interactive Texas Hold'em game where an LLM opponent plays against you while a coaching agent provides real-time pot odds, probabilities, and GTO-based advice to help you identify "the nut."

---

## Quick Start

### Prerequisites

- **Java 21** (backend)
- **Node.js 20+** (frontend)
- **Docker** (for containerized deployment)
- **GCP Service Account** with Vertex AI permissions (for AI features)

### Local Development

```bash
# Backend (terminal 1)
cd backend
./gradlew bootRun                          # Starts on :8080 (default)
SERVER_PORT=9090 ./gradlew bootRun         # Custom port

# Frontend (terminal 2)
cd frontend
npm install
npm run dev                                # Starts on :5173, proxies /api to :8080
VITE_DEV_PORT=3001 npm run dev             # Custom port
VITE_API_TARGET=http://localhost:9090 npm run dev  # Custom backend target
```

By default AI is disabled locally (`APP_AI_ENABLED=false`) and the opponent plays randomly. To enable AI locally, set `APP_AI_ENABLED=true` and provide GCP credentials:

```bash
export GOOGLE_APPLICATION_CREDENTIALS=~/.gcp/poker-tutor-sa.json
APP_AI_ENABLED=true ./gradlew bootRun
```

### Docker Deployment (Single Container)

The app packages as a **single Docker container** — the frontend is baked into the backend's static resources during the multi-stage build. No separate web server needed.

```bash
# Quick deploy to remote server
./deploy.sh

# Or build and run locally with Docker Compose
docker compose up --build
```

Browse to `http://<host>:8080` (or whatever `HOST_PORT` you configured).

---

## Architecture

### Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Spring Boot 3.4, Java 21, Spring AI, Spring Security |
| **Frontend** | React 19, Material UI, Vite |
| **AI** | Vertex AI Gemini 3.1 Flash-Lite via Spring AI ChatClient |
| **Packaging** | Multi-stage Dockerfile (Node + JDK + JRE) |
| **Security** | CSRF protection (cookie-based tokens), configurable CORS |

### Multi-Agent Design

Two specialized LLM agents operate on shared game state:

- **Opponent Agent** — Plays poker with personality and bluffing. Receives visible game state (community cards, bet sizes, pot) and decides actions. Falls back to random play when AI is disabled or rate-limited.
- **Coach Agent** — Provides real-time mathematical analysis and plain-language coaching. Combines deterministic calculations (pot odds, hand equity, outs) with LLM-generated explanations.

Deterministic math (`OddsCalculatorService`, `HandEvaluatorService`) ensures accuracy. The LLM adds personality and explanations on top.

### Request Flow

```
Browser → Spring Boot (same-origin) → GameController / CoachingController
  → GameService → OpponentAgentService (Gemini) + OddsCalculatorService (math)
  → CoachAgentService (Gemini + math)
```

In Docker, the frontend is served directly by Spring Boot — no separate hosting or CORS needed.

### API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/games` | Start a new game |
| `GET` | `/api/v1/games/{id}` | Get current game state |
| `POST` | `/api/v1/games/{id}/actions` | Submit player action |
| `GET` | `/api/v1/games/{id}/coaching` | Get coaching advice |
| `GET` | `/api/v1/games/{id}/odds` | Get pot odds and probabilities |
| `GET` | `/api/v1/games/{id}/history` | Get hand history |
| `POST` | `/api/v1/games/{id}/next-hand` | Deal next hand |
| `GET` | `/api/v1/games/health` | Health check |

All `POST` endpoints require a valid CSRF token (see [Security](#security)).

### Game State Model

The core `Game` object tracks: game ID, players (human + AI), deck, community cards, current phase, pot, betting round state, hand history, dealer position, AI call count, coaching calls per hand, and creation timestamp. Each player has: name, chip stack, hole cards, current bet, and folded status.

---

## Security

### CSRF Protection

Spring Security enforces CSRF on all state-changing requests (`POST`, `PUT`, `DELETE`):

1. The backend sets an `XSRF-TOKEN` cookie (readable by JavaScript) on every response
2. The frontend reads this cookie and sends it back as an `X-XSRF-TOKEN` header
3. Requests without a valid token receive HTTP 403

This prevents cross-site request forgery. The health endpoint (`/api/v1/games/health`) is exempt.

### No Hardcoded Credentials

- GCP credentials are injected at runtime via volume mount — never baked into the image
- No API keys or secrets in frontend code
- CSRF tokens are server-generated per session, not static secrets

### CORS

In Docker (same-origin), CORS is disabled (`CORS_ALLOWED_ORIGINS=`). For local dev with separate frontend/backend servers, CORS is enabled for the Vite dev server origin.

---

## Cost Protection

Three layers of in-memory rate limiting prevent runaway AI costs:

| Limit | Default | Env Var | Behavior When Hit |
|-------|---------|---------|-------------------|
| **Concurrent games** | 15 | `MAX_CONCURRENT_GAMES` | New game creation returns HTTP 429 |
| **AI calls per game** | 200 | `MAX_AI_CALLS_PER_GAME` | Opponent silently falls back to random; coaching returns deterministic math only |
| **Coaching per hand** | 3 | `MAX_COACHING_PER_HAND` | Returns HTTP 429; frontend disables "Ask Coach" button |

Stale games (older than 2 hours) are automatically cleaned up every 10 minutes.

---

## Deployment

### Deploy Script (`deploy.sh`)

The deploy script handles the full pipeline: sync source, copy credentials, build Docker image on the remote server, swap the container, and verify health.

```bash
# Default deploy
./deploy.sh

# Custom settings
HOST_PORT=9090 ./deploy.sh                      # Different port
REMOTE_HOST=user@myserver ./deploy.sh            # Different server
APP_AI_ENABLED=false ./deploy.sh                 # Disable AI
MAX_CONCURRENT_GAMES=5 ./deploy.sh               # Tighter limits
```

#### Deploy Script Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `REMOTE_HOST` | `user@your-server` | SSH target |
| `REMOTE_APP_DIR` | `/home/king/apps/poker-tutor` | Remote install directory |
| `HOST_PORT` | `8080` | Port exposed on the host |
| `GCP_CREDENTIALS_PATH` | `~/.gcp/poker-tutor-sa.json` | Local path to GCP service account key |
| `APP_AI_ENABLED` | `true` | Enable Gemini AI opponent and coach |
| `GCP_PROJECT_ID` | `your-gcp-project-id` | GCP project for Vertex AI |
| `GCP_REGION` | `us-central1` | GCP region |
| `AI_MODEL` | `gemini-3.1-flash-lite-preview` | Gemini model ID |
| `IMAGE_TAG` | `latest` | Docker image tag |

#### What the script does

1. `rsync` syncs the source tree to the remote server (excludes build artifacts)
2. `scp` copies GCP credentials securely (chmod 600)
3. Builds the Docker image on the remote server (multi-stage: Node → JDK → JRE)
4. Stops and removes the existing container
5. Starts a new container with `--restart unless-stopped` (survives reboots)
6. Polls the health endpoint until it responds (up to 60s)

### Docker Compose (Alternative)

For local Docker testing or environments where Docker is available on the build machine:

```bash
# Requires GCP credentials at ~/.gcp/poker-tutor-sa.json (or set GCP_CREDENTIALS_PATH)
docker compose up --build

# Custom port
HOST_PORT=9090 docker compose up --build
```

### Dockerfile

Three-stage multi-stage build:

1. **Node 20 Alpine** — `npm ci` + `npm run build` (produces `frontend/dist/`)
2. **JDK 21** — Copies frontend dist into `backend/src/main/resources/static/`, runs `./gradlew bootJar`
3. **JRE 21 Jammy** — Copies the fat JAR, exposes port 8080, runs `java -jar app.jar`

> **Note:** The runtime stage uses `eclipse-temurin:21-jre-jammy` (glibc) instead of Alpine (musl) because the gRPC/Netty native library (`tcnative`) used by Vertex AI crashes with SIGSEGV on musl-based systems.

### Server Operations

```bash
# View logs
ssh user@your-server docker logs -f poker-tutor

# Restart
ssh user@your-server docker restart poker-tutor

# Check status
ssh user@your-server docker ps --filter name=poker-tutor

# Health check
curl http://your.server.ip:8080/api/v1/games/health
```

---

## Configuration Reference

All configuration is via environment variables with sensible defaults:

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8080` | Backend server port |
| `APP_AI_ENABLED` | `false` | Enable AI opponent and coach (requires GCP credentials) |
| `GCP_PROJECT_ID` | `your-gcp-project-id` | GCP project for Vertex AI |
| `GCP_REGION` | `us-central1` | GCP region for Vertex AI |
| `AI_MODEL` | `gemini-3.1-flash-lite-preview` | Gemini model to use |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Allowed CORS origins (blank = same-origin mode) |
| `MAX_AI_CALLS_PER_GAME` | `200` | Max LLM calls per game (opponent + coach combined) |
| `MAX_CONCURRENT_GAMES` | `15` | Max simultaneous active games |
| `MAX_COACHING_PER_HAND` | `3` | Max coaching requests per hand |
| `GOOGLE_APPLICATION_CREDENTIALS` | — | Path to GCP service account JSON (set automatically in Docker) |

Frontend dev server variables (set in shell, not in Spring):

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_DEV_PORT` | `5173` | Vite dev server port |
| `VITE_API_TARGET` | `http://localhost:8080` | Backend URL for dev proxy |
| `VITE_API_URL` | `""` (empty) | API base URL override (empty = same-origin / proxy) |

---

## Build & Test

```bash
# Backend tests (68 tests)
cd backend && ./gradlew test

# Frontend build
cd frontend && npm run build

# Full Docker build (no server needed)
docker build -t poker-tutor .
```

---

## Project Structure

```
poker-tutor/
├── backend/
│   └── src/main/java/com/jkingai/pokertutor/
│       ├── config/          # WebConfig, SecurityConfig, SpaForwardingConfig, VertexAiConfig
│       ├── controller/      # GameController, CoachingController
│       ├── dto/             # Request/response DTOs
│       ├── exception/       # GlobalExceptionHandler, RateLimitException, etc.
│       ├── model/           # Game, Player, Card, Deck, GamePhase, etc.
│       └── service/         # GameService, CoachAgentService, OpponentAgentService,
│                            # RateLimitService, HandEvaluatorService, OddsCalculatorService
├── frontend/
│   └── src/
│       ├── api/             # client.js (API client with CSRF handling)
│       ├── components/      # GameTable, CoachingPanel, ActionLog, etc.
│       ├── hooks/           # useGame, useCoaching
│       └── pages/           # LobbyPage, GamePage
├── Dockerfile               # Multi-stage build (Node → JDK → JRE)
├── docker-compose.yml       # Local Docker Compose config
├── deploy.sh                # Remote deployment script
└── docs/                    # Architecture, API contracts, milestones
```

---

## Documentation

| Document | Description |
|----------|-------------|
| [AGENTS.md](AGENTS.md) | AI agent coding instructions, build commands, architecture |
| [Architecture](docs/architecture.md) | System design, multi-agent flow, and design decisions |
| [API Contracts](docs/api-contracts.md) | Endpoint specs, request/response examples, Java DTOs |
| [Milestones](docs/milestones.md) | Development plan with acceptance criteria |
| [Local Development Guide](docs/local-dev-guide.md) | Local setup and environment |
| [Local Testing Guide](docs/local-testing-guide.md) | Backend tests, API testing, manual testing |
| [Production Deployment](docs/production-deployment.md) | GCP deployment guide |

---

## Problem Statement

Learning poker strategy requires understanding two fundamentally different skill sets: the mathematical foundations (pot odds, expected value, hand probabilities) and the psychological dimensions (reading opponents, bluffing, table image). Most learning resources teach these in isolation -- probability calculators lack game context, and play-money apps offer no coaching. New players need a safe environment where they can play hands against a realistic opponent while receiving real-time mathematical coaching that explains *why* a decision is correct, not just *what* the correct decision is.

## Target User Persona

**Name:** Marcus, aspiring recreational poker player

- Has watched poker on TV and plays casually with friends
- Understands the basic rules of Texas Hold'em but struggles with bet sizing and knowing when to fold
- Knows pot odds exist but cannot calculate them in real time during a hand
- Wants to understand *why* a call is profitable, not just be told "call here"
- Wants to practice heads-up play against a challenging but beatable opponent
