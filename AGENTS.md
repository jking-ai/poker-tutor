# AGENTS.md

This file provides guidance to AI coding agents when working with code in this repository.

## Project Overview

"The Nut" Poker Tutor is an interactive Texas Hold'em game where an LLM opponent plays against the user while a coaching agent provides real-time pot odds, probabilities, and GTO-based strategy advice. The system uses multi-agent orchestration via Spring AI to run two specialized agents -- an Opponent Agent (plays poker with personality and bluffing) and a Coach Agent (calculates odds and explains optimal play) -- on shared game state.

- **Backend:** Spring Boot 3.4+ + Spring AI (Java 21) on Cloud Run
- **Frontend:** React 19 + MUI + Vite SPA on Firebase Hosting
- **Real-Time Sync:** Firebase Realtime Database for game state synchronization
- **LLM:** Vertex AI Gemini via Spring AI ChatClient
- **Agents:** Multi-agent setup -- Agent A (Opponent), Agent B (Coach)
- **Monorepo:** `backend/` (Java/Gradle) and `frontend/` (React/npm) in same repo

**Status:** Pre-build (planning docs only, no implementation yet).

## Build & Run Commands

### Backend (`backend/` directory)
```bash
cd backend
# TODO: Add commands once backend is scaffolded
./gradlew bootRun                # Local dev server on :8080 (uses 'local' profile)
./gradlew test                   # Run all tests
./gradlew build                  # Full build
./gradlew jibDockerBuild         # Build container image locally (Jib, no Dockerfile)
./gradlew jib                    # Build and push to Artifact Registry
```

### Frontend (`frontend/` directory)
```bash
cd frontend
# TODO: Add commands once frontend is scaffolded
npm install                      # Install dependencies
npm run dev                      # Dev server on :5173
npm run build                    # Production build to ./dist/
npm run preview                  # Preview production build
```

### Deployment
```bash
# TODO: Add deployment commands once infrastructure is configured
./gradlew jib                    # Build and push backend container
gcloud run deploy poker-tutor --image <artifact-registry-url> --region us-central1
firebase deploy --only hosting   # Deploy frontend
```

## Architecture

### Request Flow
`Browser` -> `Firebase Hosting (React SPA)` -> `Spring Boot (Cloud Run)` -> `GameController`/`CoachingController` -> `GameService` -> `OpponentAgentService` (Spring AI ChatClient -> Gemini) + `CoachAgentService` (Spring AI ChatClient -> Gemini) + `OddsCalculatorService` (deterministic math) -> `Firebase RTDB` (game state sync) -> `Frontend` (real-time updates)

### Key Design Decisions
- **Multi-agent orchestration:** Two Spring AI ChatClient instances with distinct system prompts. The Opponent Agent receives game state and decides actions with personality. The Coach Agent receives the same game state plus the player's hand and provides mathematical analysis with plain-language explanations.
- **Deterministic + LLM separation:** Pot odds, hand equity, and combinatorics are calculated by pure Java services (`OddsCalculatorService`, `HandEvaluatorService`). The LLM is used only for opponent decision-making and coaching explanations. This ensures mathematical accuracy.
- **Game state machine:** Texas Hold'em phases (PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN) are modeled as a state machine in `GameService`. Each phase enforces valid actions and transitions.
- **Firebase RTDB for real-time sync:** Game state is written to Firebase Realtime Database after each action. The React frontend subscribes to the game path and receives updates in real time without polling.
- **Prompt templates:** Opponent and Coach personas are defined in `src/main/resources/prompts/` as text files loaded at runtime. This allows prompt iteration without code changes.

### API Endpoints
- `POST /api/v1/games` -- Start a new game
- `GET /api/v1/games/{id}` -- Get current game state
- `POST /api/v1/games/{id}/actions` -- Submit player action (bet, call, fold, raise, check)
- `GET /api/v1/games/{id}/coaching` -- Get coaching advice for current hand
- `GET /api/v1/games/{id}/odds` -- Get pot odds and probabilities
- `GET /api/v1/games/{id}/history` -- Get hand history
- `POST /api/v1/games/{id}/next-hand` -- Deal next hand
- `GET /api/v1/health` -- Health check

### Game State Model
The core `Game` object tracks: game ID, players (human + AI), deck, community cards, current phase, pot, betting round state, hand history, and dealer position. Each player has: name, chip stack, hole cards, current bet, and folded status.

## Environment Setup

```bash
# TODO: Add environment setup once project is scaffolded
cp .env.example .env
# Required variables:
#   GCP_PROJECT_ID=your-gcp-project-id
#   GCP_REGION=us-central1 (default)
#   FIREBASE_PROJECT_ID=your-firebase-project-id
```

Backend requires GCP credentials for Vertex AI Gemini and Firebase Admin SDK. For local development, place a service account key at `backend/src/main/resources/gcp-credentials.json` (gitignored). In production, Cloud Run's service account (with "Vertex AI User" and "Firebase Admin" roles) provides implicit auth.

## Project Documentation

Detailed specs live in `docs/`:
- [`docs/README.md`](docs/README.md) -- Documentation index and quick links
- [`docs/architecture.md`](docs/architecture.md) -- System architecture, multi-agent flow, and design decisions
- [`docs/api-contracts.md`](docs/api-contracts.md) -- API endpoint specifications and Java DTO definitions
- [`docs/milestones.md`](docs/milestones.md) -- Four-phase development plan with acceptance criteria
- [`docs/local-dev-guide.md`](docs/local-dev-guide.md) -- Local development setup
- [`docs/local-testing-guide.md`](docs/local-testing-guide.md) -- Testing guide (backend, API, manual)
- [`docs/production-deployment.md`](docs/production-deployment.md) -- GCP deployment guide
