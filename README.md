# "The Nut" Poker Tutor

**One-line summary:** An interactive Texas Hold'em game where an LLM opponent plays against you while a coaching agent provides real-time pot odds, probabilities, and GTO-based advice to help you identify "the nut."

---

## Problem Statement

Learning poker strategy requires understanding two fundamentally different skill sets: the mathematical foundations (pot odds, expected value, hand probabilities) and the psychological dimensions (reading opponents, bluffing, table image). Most learning resources teach these in isolation -- probability calculators lack game context, and play-money apps offer no coaching. New players need a safe environment where they can play hands against a realistic opponent while receiving real-time mathematical coaching that explains *why* a decision is correct, not just *what* the correct decision is.

## Target User Persona

**Name:** Marcus, aspiring recreational poker player

- Has watched poker on TV and plays casually with friends
- Understands the basic rules of Texas Hold'em but struggles with bet sizing and knowing when to fold
- Knows pot odds exist but cannot calculate them in real time during a hand
- Wants to understand *why* a call is profitable, not just be told "call here"
- Gets frustrated by poker training apps that assume advanced knowledge or charge monthly subscriptions
- Wants to practice heads-up play against a challenging but beatable opponent

## Skills and Engineering Patterns Showcased

| Pattern | Description |
|---------|-------------|
| **Multi-Agent Orchestration** | Two specialized LLM agents (Opponent + Coach) operating on the same game state with distinct personas, goals, and prompt strategies via Spring AI |
| **Deterministic + Non-Deterministic Integration** | Combining exact mathematical calculations (pot odds, hand equity, combinatorics) with LLM-driven reasoning (opponent strategy, coaching explanations) |
| **Spring AI Chat Client & Advisors** | Leveraging Spring AI's ChatClient with advisor chains for structured multi-agent communication with Vertex AI Gemini |
| **Game State Machine** | Implementing a finite state machine for Texas Hold'em phases (pre-flop, flop, turn, river, showdown) with enforced betting rules |
| **Real-Time Sync with Firebase RTDB** | Using Firebase Realtime Database to synchronize game state between backend and frontend with sub-second latency |
| **Spring Boot REST API** | Clean controller/service architecture with DTOs, global exception handling, and profile-based configuration |
| **React + MUI Frontend** | Dark-themed poker table UI with Material UI components, real-time state updates, and responsive layout |
| **Vertex AI Integration** | Using Gemini via Spring AI for both creative (opponent personality) and analytical (coaching) LLM tasks |

## Success Criteria

1. **Functional:** A user can start a heads-up Texas Hold'em game, play complete hands through all betting rounds, and see a showdown result with correct hand evaluation.
2. **Opponent Quality:** The LLM opponent makes contextually appropriate decisions (folds weak hands, raises strong hands, occasionally bluffs) rather than playing randomly.
3. **Coaching Accuracy:** Pot odds and hand probabilities displayed by the Coach agent are mathematically correct and match a reference poker calculator within 2% margin.
4. **Real-Time:** Game state updates appear in the frontend within 1 second of a backend state change via Firebase RTDB.
5. **Educational:** The coaching panel explains recommendations in plain language that a beginner can understand (e.g., "You need 25% equity to call this bet. Your flush draw gives you ~35% equity, so calling is profitable.").
6. **Deployable:** Backend runs on Cloud Run, frontend is hosted on Firebase Hosting, both accessible via public URLs.
7. **Portfolio-Ready:** The project README, architecture docs, and live demo clearly communicate the multi-agent orchestration pattern and deterministic/LLM integration to a technical reviewer.

## Documentation

| Document | Description |
|----------|-------------|
| [Architecture](docs/architecture.md) | System design, multi-agent flow, tech stack, data flow, and design decisions |
| [API Contracts](docs/api-contracts.md) | Endpoint specs, request/response examples, Java DTOs and model definitions |
| [Milestones](docs/milestones.md) | Four-phase development plan with acceptance criteria |
| [Local Development Guide](docs/local-dev-guide.md) | Prerequisites, environment setup, running backend and frontend locally |
| [Local Testing Guide](docs/local-testing-guide.md) | Backend tests, API testing, manual testing checklists |
| [Production Deployment](docs/production-deployment.md) | GCP deployment, Cloud Run, Firebase Hosting, verification |

## Level of Effort

**High** -- Estimated 6-8 focused implementation sessions.

- Game Engine: ~8 hours (deck, hand evaluation, Texas Hold'em round flow, betting logic, pot management)
- AI Agents: ~6 hours (Spring AI setup, opponent agent with personality/bluffing, coach agent with GTO/odds)
- Backend API: ~4 hours (REST controllers, DTOs, Firebase RTDB integration, exception handling)
- Frontend: ~8 hours (MUI poker table UI, card display, action controls, coaching panel, real-time sync)
- Integration & Polish: ~4 hours (end-to-end testing, prompt refinement, UI polish, deployment)
