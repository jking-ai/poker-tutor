# Milestones -- "The Nut" Poker Tutor

## Phase 1: Foundation

**Goal:** Establish the project skeleton, build the core game engine (deck, cards, hand evaluation), and verify Spring Boot + Spring AI integration.

**Estimated effort:** 2 sessions (~6 hours)

### Deliverables

#### 1.1 Backend Project Setup

**Acceptance criteria:**
- [ ] Spring Boot 3.4+ project initialized with Gradle, Java 21, and dependencies: Spring Web, Spring AI (Vertex AI), Firebase Admin SDK
- [ ] `build.gradle` includes Jib plugin for containerization
- [ ] `PokerTutorApplication.java` runs without errors
- [ ] `application.yml` and `application-local.yml` configure Spring profiles, server port (8080), and placeholder Vertex AI settings
- [ ] `GET /api/v1/health` returns `{ "status": "healthy", "service": "poker-tutor" }`
- [ ] Running `./gradlew bootRun` starts the server on port 8080

#### 1.2 Card and Deck Model

**Acceptance criteria:**
- [ ] `Card.java` defines a card with `Rank` and `Suit` enums (standard 52-card deck)
- [ ] `Deck.java` implements: create full deck, shuffle, deal (remove top card), reset
- [ ] `Hand.java` represents a player's hand (hole cards + optional community cards for evaluation)
- [ ] Unit tests verify: deck has 52 cards after creation, shuffle produces different order, dealing removes cards from deck
- [ ] `Rank` enum supports comparison (ACE > KING > ... > TWO)

#### 1.3 Hand Evaluator

**Acceptance criteria:**
- [ ] `HandEvaluatorService.java` evaluates the best 5-card hand from 7 cards (2 hole + 5 community)
- [ ] Correctly identifies all hand ranks: Royal Flush, Straight Flush, Four of a Kind, Full House, Flush, Straight, Three of a Kind, Two Pair, Pair, High Card
- [ ] Handles edge cases: Ace-low straight (A-2-3-4-5), wheel, Broadway
- [ ] Returns a `HandRank` enum and the best 5-card combination
- [ ] Supports comparison between two evaluated hands to determine the winner
- [ ] Unit tests cover at least one example of each hand rank and multiple comparison scenarios

#### 1.4 Spring AI Integration Proof of Concept

**Acceptance criteria:**
- [ ] `VertexAiConfig.java` creates a Spring AI `ChatClient` bean configured for Vertex AI Gemini
- [ ] A test endpoint or script sends a simple prompt to Gemini and returns the response
- [ ] Authentication works locally using a service account key
- [ ] Error handling wraps Spring AI exceptions into application-specific errors

**Dependencies:** 1.1 must be complete before 1.4.

---

## Phase 2: Game Logic

**Goal:** Implement the full Texas Hold'em round flow including betting rounds, pot management, phase transitions, and the opponent agent.

**Estimated effort:** 2 sessions (~6 hours)

### Deliverables

#### 2.1 Game State Machine

**Acceptance criteria:**
- [ ] `Game.java` tracks: game ID, players, deck, community cards, pot, current phase, dealer position, hand number, hand history
- [ ] `Player.java` tracks: name, chip stack, hole cards, current bet, folded status, dealer flag
- [ ] `GamePhase` enum: PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN
- [ ] `GameService.java` implements `createGame()`: initializes game, shuffles deck, deals 2 hole cards to each player, posts blinds
- [ ] Heads-up blind posting: dealer posts small blind, other player posts big blind
- [ ] Game state is stored in-memory (ConcurrentHashMap keyed by game ID)

#### 2.2 Betting Round Logic

**Acceptance criteria:**
- [ ] `GameService.processAction()` validates and processes: BET, CALL, FOLD, RAISE, CHECK, ALL_IN
- [ ] Action validation enforces rules: cannot CHECK with outstanding bet, RAISE must be >= current bet, BET amount must be >= big blind
- [ ] Tracks current bet per player, detects when betting round is complete (both players acted, bets equalized)
- [ ] On betting round complete: transitions to next phase, deals community cards (3 for FLOP, 1 for TURN, 1 for RIVER)
- [ ] FOLD immediately awards pot to the other player and transitions to hand-complete state
- [ ] ALL_IN handles side pot logic for unequal stacks
- [ ] Unit tests verify: valid action sequences through a complete hand, invalid action rejection, pot calculation

#### 2.3 Showdown and Pot Resolution

**Acceptance criteria:**
- [ ] When RIVER betting completes, phase transitions to SHOWDOWN
- [ ] At SHOWDOWN: both players' hands are evaluated using `HandEvaluatorService`
- [ ] Winner is determined, pot is awarded to the winner's chip stack
- [ ] Tie (split pot) is handled correctly
- [ ] Opponent's hole cards are revealed (included in response)
- [ ] Hand is recorded in the game's hand history
- [ ] `POST /api/v1/games/{id}/next-hand` resets for the next hand: shuffles deck, deals cards, alternates dealer, posts blinds

#### 2.4 Game REST Endpoints

**Acceptance criteria:**
- [ ] `POST /api/v1/games` -- Creates game, returns initial state
- [ ] `GET /api/v1/games/{id}` -- Returns current game state (opponent cards hidden)
- [ ] `POST /api/v1/games/{id}/actions` -- Processes player action, returns updated state
- [ ] `GET /api/v1/games/{id}/history` -- Returns hand history
- [ ] `POST /api/v1/games/{id}/next-hand` -- Deals next hand
- [ ] All endpoints return DTOs matching the api-contracts.md specification
- [ ] `GlobalExceptionHandler` handles `GameNotFoundException` (404) and `InvalidActionException` (400)

**Dependencies:** 2.1 must be complete before 2.2. 2.2 and 2.3 must be complete before 2.4.

---

## Phase 3: AI Agents

**Goal:** Integrate the Opponent Agent and Coach Agent using Spring AI, implement odds calculation, and connect to Firebase RTDB.

**Estimated effort:** 2 sessions (~6 hours)

### Deliverables

#### 3.1 Opponent Agent

**Acceptance criteria:**
- [ ] `OpponentAgentService.java` uses Spring AI `ChatClient` with the opponent persona prompt
- [ ] Opponent persona prompt (`prompts/opponent_persona.txt`) instructs the model to: play poker as a skilled but beatable opponent, vary play style, occasionally bluff, provide brief reasoning for decisions
- [ ] Agent receives: current game state (phase, pot, community cards, opponent's hole cards, chip stacks, action history)
- [ ] Agent returns a structured response: action (BET/CALL/FOLD/RAISE/CHECK), amount (if applicable), reasoning (1-2 sentences)
- [ ] Temperature is set higher (0.8) for varied, unpredictable play
- [ ] Integration with `GameService`: after player acts, if it is the opponent's turn, `OpponentAgentService` is called to determine the opponent's action
- [ ] Fallback: if LLM call fails, opponent defaults to CHECK or CALL (safe fallback)

#### 3.2 Odds Calculator

**Acceptance criteria:**
- [ ] `OddsCalculatorService.java` implements:
  - Pot odds calculation: cost-to-call / (pot + cost-to-call)
  - Pot odds ratio: pot / cost-to-call formatted as "X:1"
  - Outs counting: enumerate cards that improve the hand to a higher rank
  - Hand equity estimation: Monte Carlo simulation (1000+ iterations) or combinatorial calculation
  - Win/tie/loss probability against a random opponent range
- [ ] Identifies "the nut" (best possible hand given the community cards)
- [ ] Calculates "nut distance" (how many hand ranks away the player is from the nut)
- [ ] `GET /api/v1/games/{id}/odds` returns the `OddsResponse` DTO
- [ ] Unit tests verify pot odds calculation, outs counting for common draws (flush draw = 9 outs, open-ended straight draw = 8 outs)

#### 3.3 Coach Agent

**Acceptance criteria:**
- [ ] `CoachAgentService.java` uses Spring AI `ChatClient` with the coach persona prompt
- [ ] Coach persona prompt (`prompts/coach_persona.txt`) instructs the model to: explain poker math in simple terms, provide actionable advice, reference the calculated odds/equity, identify the nut hand
- [ ] Agent receives: game state, pre-calculated odds (from `OddsCalculatorService`), hand evaluation (from `HandEvaluatorService`)
- [ ] Agent returns a structured response: advice text, recommended action, confidence level (HIGH/MEDIUM/LOW)
- [ ] Temperature is set lower (0.3) for consistent, reliable advice
- [ ] `GET /api/v1/games/{id}/coaching` returns the `CoachingResponse` DTO
- [ ] Advice is contextual: different recommendations for different phases (pre-flop range advice vs. post-flop pot odds)

#### 3.4 Firebase Realtime Database Integration

**Acceptance criteria:**
- [ ] `FirebaseConfig.java` initializes Firebase Admin SDK with service account credentials
- [ ] After each game state change (action processed, next hand dealt), the game state is written to Firebase RTDB at path `games/{gameId}`
- [ ] The written state matches the `GameResponse` DTO structure (with opponent cards hidden)
- [ ] At SHOWDOWN, the written state includes both players' hole cards
- [ ] Old game data is cleaned up (TTL or manual cleanup for games older than 24 hours)

**Dependencies:** 3.1 depends on Phase 2 completion. 3.2 depends on 1.3 (HandEvaluator). 3.3 depends on 3.2. 3.4 can run in parallel with 3.1-3.3.

---

## Phase 4: Frontend & Polish

**Goal:** Build the React + MUI frontend with poker table UI, real-time Firebase sync, coaching overlay, and deploy to GCP.

**Estimated effort:** 2 sessions (~8 hours)

### Deliverables

#### 4.1 Frontend Project Setup

**Acceptance criteria:**
- [ ] React 19 project initialized with Vite and MUI dependencies
- [ ] Dark theme configured in `theme.js` (poker table green, dark background, gold accents)
- [ ] React Router configured with routes: `/` (lobby), `/game/:id` (game), `/history/:id` (history)
- [ ] `TopNav.jsx` renders navigation bar with app title and links
- [ ] `client.js` API client configured with base URL from `VITE_API_URL` environment variable
- [ ] `firebase.js` initializes Firebase app and RTDB reference

#### 4.2 Game Table UI

**Acceptance criteria:**
- [ ] `GameTable.jsx` renders the poker table layout: community cards in center, player panel at bottom, opponent panel at top, pot display, action controls
- [ ] `CardDisplay.jsx` renders playing cards with rank and suit symbols, face-down state for hidden cards, and visual distinction between suits (red/black)
- [ ] `PlayerPanel.jsx` shows: player name, chip count, hole cards, current bet, dealer chip indicator
- [ ] `ActionControls.jsx` renders action buttons (Check, Bet, Call, Raise, Fold, All In) with only valid actions enabled based on game state
- [ ] Raise control includes a slider or input for bet amount with min/max bounds

#### 4.3 Real-Time Firebase Sync

**Acceptance criteria:**
- [ ] `useFirebaseSync.js` hook subscribes to `games/{gameId}` in Firebase RTDB
- [ ] Game state updates from Firebase trigger React re-renders within 1 second
- [ ] `useGame.js` hook manages local game state and merges Firebase updates
- [ ] `useGameActions.js` hook dispatches player actions to the REST API
- [ ] Connection status indicator shows when Firebase is connected/disconnected

#### 4.4 Coaching Panel

**Acceptance criteria:**
- [ ] `CoachingPanel.jsx` renders coaching advice in a collapsible side panel
- [ ] `useCoaching.js` hook fetches coaching data from `GET /api/v1/games/{id}/coaching`
- [ ] Coaching panel shows: recommended action (highlighted), confidence level, plain-language explanation
- [ ] `OddsDisplay.jsx` renders: pot odds, hand equity as a progress bar, outs count and description, nut hand identification
- [ ] Coaching refreshes automatically when game state changes (new cards dealt, betting round advances)

#### 4.5 Hand History and Polish

**Acceptance criteria:**
- [ ] `HandHistory.jsx` renders a timeline of actions for the current hand
- [ ] `HistoryPage.jsx` shows full hand history for all completed hands in the session
- [ ] Game over state shows final chip counts and session summary
- [ ] Loading states and error handling throughout the UI
- [ ] Responsive layout works on desktop (1200px+) and tablet (768px+)

#### 4.6 Deployment

**Acceptance criteria:**
- [ ] Backend deploys to Cloud Run via Jib (`./gradlew jib`)
- [ ] Cloud Run service has `GCP_PROJECT_ID`, `FIREBASE_PROJECT_ID` environment variables configured
- [ ] Cloud Run service account has Vertex AI User and Firebase Admin roles
- [ ] Frontend builds with `VITE_API_URL` set to production Cloud Run URL
- [ ] Frontend deploys to Firebase Hosting (`firebase deploy --only hosting`)
- [ ] Firebase RTDB security rules restrict writes to the backend service account
- [ ] `GET /api/v1/health` returns 200 from production URL
- [ ] End-to-end: start a game, play a hand, receive coaching, see showdown result

#### 4.7 Prompt Refinement and Quality Validation

**Acceptance criteria:**
- [ ] Play 10+ hands against the Opponent Agent and verify: opponent makes contextually appropriate decisions, does not always fold or always call, occasionally bluffs
- [ ] Request coaching during 5+ different hand situations and verify: pot odds match manual calculation, advice is actionable and clear, recommended action changes based on hand strength
- [ ] Adjust opponent persona prompt for better play variety if needed
- [ ] Adjust coach persona prompt for clearer explanations if needed
- [ ] Document final prompt templates

**Dependencies:** 4.1 must be complete before 4.2-4.5. 4.3 depends on 3.4 (Firebase RTDB). 4.4 depends on 3.3 (Coach Agent). 4.6 depends on all previous deliverables. 4.7 depends on 4.6.
