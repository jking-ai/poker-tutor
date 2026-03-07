# API Contracts -- "The Nut" Poker Tutor

## Base URL

- **Local development:** `http://localhost:8080`
- **Production:** `https://poker-tutor-<hash>-uc.a.run.app`

All endpoints are prefixed with `/api/v1`.

---

## Endpoints Overview

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/games` | Start a new heads-up Texas Hold'em game |
| `GET` | `/api/v1/games/{id}` | Get current game state |
| `POST` | `/api/v1/games/{id}/actions` | Submit a player action (bet, call, fold, raise, check) |
| `GET` | `/api/v1/games/{id}/coaching` | Get coaching advice for the current hand |
| `GET` | `/api/v1/games/{id}/odds` | Get pot odds and hand probabilities |
| `GET` | `/api/v1/games/{id}/history` | Get hand history for the game |
| `POST` | `/api/v1/games/{id}/next-hand` | Deal the next hand |
| `GET` | `/api/v1/health` | Health check and service metadata |

---

## Error Response Format

All error responses follow a consistent format:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable description of what went wrong.",
    "details": {}
  }
}
```

### Error Codes

| HTTP Status | Error Code | Description |
|-------------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Request body failed validation |
| 400 | `INVALID_ACTION` | The player action is not valid for the current game state |
| 404 | `GAME_NOT_FOUND` | No game exists with the specified ID |
| 500 | `AGENT_ERROR` | The LLM agent call failed |
| 500 | `INTERNAL_ERROR` | Unexpected server error |

---

## Endpoint Details

### GET /api/v1/health

Returns service status and metadata. Used by Cloud Run health checks.

**Request:** No parameters.

**curl:**
```bash
curl http://localhost:8080/api/v1/health
```

**Response (200 OK):**

```json
{
  "status": "healthy",
  "service": "poker-tutor",
  "version": "1.0.0",
  "model": "gemini-2.0-flash"
}
```

---

### POST /api/v1/games

Start a new heads-up Texas Hold'em game. Creates the game, shuffles the deck, and deals hole cards to both players.

**Request Headers:**
- `Content-Type: application/json`

**Request Body:**

```json
{
  "playerName": "Marcus",
  "startingChips": 1000,
  "smallBlind": 5,
  "bigBlind": 10
}
```

**curl:**
```bash
curl -X POST http://localhost:8080/api/v1/games \
  -H "Content-Type: application/json" \
  -d '{
    "playerName": "Marcus",
    "startingChips": 1000,
    "smallBlind": 5,
    "bigBlind": 10
  }'
```

**Response (201 Created):**

```json
{
  "gameId": "game_a1b2c3d4",
  "phase": "PRE_FLOP",
  "pot": 15,
  "communityCards": [],
  "players": [
    {
      "name": "Marcus",
      "chips": 990,
      "holeCards": [
        { "rank": "ACE", "suit": "SPADES" },
        { "rank": "KING", "suit": "HEARTS" }
      ],
      "currentBet": 10,
      "folded": false,
      "isDealer": true
    },
    {
      "name": "The House",
      "chips": 995,
      "holeCards": null,
      "currentBet": 5,
      "folded": false,
      "isDealer": false
    }
  ],
  "currentPlayerIndex": 0,
  "handNumber": 1,
  "smallBlind": 5,
  "bigBlind": 10
}
```

**Notes:**
- The opponent's `holeCards` are always `null` in the response (hidden from the player) until SHOWDOWN phase.
- Blinds are posted automatically. In heads-up, the dealer posts the small blind and acts first pre-flop.

---

### GET /api/v1/games/{id}

Get the current state of a game.

**curl:**
```bash
curl http://localhost:8080/api/v1/games/game_a1b2c3d4
```

**Response (200 OK):**

Same structure as the POST /api/v1/games response, reflecting the current game state.

**Response (404 Not Found):**

```json
{
  "error": {
    "code": "GAME_NOT_FOUND",
    "message": "No game found with ID 'game_invalid'.",
    "details": {}
  }
}
```

---

### POST /api/v1/games/{id}/actions

Submit a player action. After processing the player's action, the backend automatically processes the opponent's response (via the Opponent Agent) if it is the opponent's turn.

**Request Body:**

```json
{
  "action": "RAISE",
  "amount": 50
}
```

**Valid actions by phase:**
- `CHECK` -- Pass (only if no outstanding bet)
- `BET` -- Place a bet (only if no outstanding bet)
- `CALL` -- Match the current bet
- `RAISE` -- Increase the current bet (requires `amount`)
- `FOLD` -- Surrender the hand
- `ALL_IN` -- Bet all remaining chips

**curl:**
```bash
curl -X POST http://localhost:8080/api/v1/games/game_a1b2c3d4/actions \
  -H "Content-Type: application/json" \
  -d '{
    "action": "RAISE",
    "amount": 50
  }'
```

**Response (200 OK):**

```json
{
  "gameId": "game_a1b2c3d4",
  "phase": "PRE_FLOP",
  "pot": 115,
  "communityCards": [],
  "players": [
    {
      "name": "Marcus",
      "chips": 940,
      "holeCards": [
        { "rank": "ACE", "suit": "SPADES" },
        { "rank": "KING", "suit": "HEARTS" }
      ],
      "currentBet": 60,
      "folded": false,
      "isDealer": true
    },
    {
      "name": "The House",
      "chips": 945,
      "holeCards": null,
      "currentBet": 55,
      "folded": false,
      "isDealer": false
    }
  ],
  "currentPlayerIndex": 0,
  "handNumber": 1,
  "lastAction": {
    "player": "The House",
    "action": "CALL",
    "amount": 50,
    "reasoning": "Strong hand pre-flop, let's see a flop."
  },
  "smallBlind": 5,
  "bigBlind": 10
}
```

**Response (400 Bad Request -- invalid action):**

```json
{
  "error": {
    "code": "INVALID_ACTION",
    "message": "Cannot CHECK when there is an outstanding bet of 50. Valid actions: CALL, RAISE, FOLD, ALL_IN.",
    "details": {
      "currentBet": 50,
      "validActions": ["CALL", "RAISE", "FOLD", "ALL_IN"]
    }
  }
}
```

---

### GET /api/v1/games/{id}/coaching

Get coaching advice from the Coach Agent for the current hand. Returns mathematical analysis (pot odds, equity, outs) combined with plain-language strategic advice.

**curl:**
```bash
curl http://localhost:8080/api/v1/games/game_a1b2c3d4/coaching
```

**Response (200 OK):**

```json
{
  "advice": "You're holding Ace-King suited, which is a premium starting hand. The pot is $115 and you need to call $50. Your pot odds are about 2.3:1 (30%). With AKs, your equity against a typical opponent range is around 65%. This is a clear call -- you're getting great odds with a strong hand. Consider raising to put pressure on your opponent.",
  "recommendedAction": "CALL",
  "confidence": "HIGH",
  "handStrength": {
    "currentRank": "HIGH_CARD",
    "bestHand": "Ace high",
    "nutDistance": 8
  },
  "odds": {
    "potOdds": 0.30,
    "potOddsRatio": "2.3:1",
    "handEquity": 0.65,
    "outs": 6,
    "outsDescription": "6 outs to top pair (3 Aces + 3 Kings)"
  },
  "explanation": {
    "situation": "Pre-flop with AKs facing a raise",
    "mathSummary": "You need 30% equity to call. You have ~65% equity.",
    "recommendation": "Calling is profitable. You could also 3-bet to isolate."
  }
}
```

---

### GET /api/v1/games/{id}/odds

Get raw pot odds and probability calculations for the current hand (without coaching narrative).

**curl:**
```bash
curl http://localhost:8080/api/v1/games/game_a1b2c3d4/odds
```

**Response (200 OK):**

```json
{
  "potSize": 115,
  "costToCall": 50,
  "potOdds": 0.30,
  "potOddsRatio": "2.3:1",
  "handEquity": 0.65,
  "outs": 6,
  "outsDetails": [
    { "card": "ACE", "count": 3, "description": "Top pair (Aces)" },
    { "card": "KING", "count": 3, "description": "Top pair (Kings)" }
  ],
  "winProbability": 0.65,
  "tieProbability": 0.02,
  "loseProbability": 0.33,
  "isNut": false,
  "nutHand": "Pocket Aces (AA)"
}
```

---

### GET /api/v1/games/{id}/history

Get the hand history for the current game session.

**curl:**
```bash
curl http://localhost:8080/api/v1/games/game_a1b2c3d4/history
```

**Response (200 OK):**

```json
{
  "gameId": "game_a1b2c3d4",
  "totalHands": 3,
  "hands": [
    {
      "handNumber": 1,
      "playerHoleCards": [
        { "rank": "ACE", "suit": "SPADES" },
        { "rank": "KING", "suit": "HEARTS" }
      ],
      "opponentHoleCards": [
        { "rank": "QUEEN", "suit": "DIAMONDS" },
        { "rank": "JACK", "suit": "DIAMONDS" }
      ],
      "communityCards": [
        { "rank": "ACE", "suit": "DIAMONDS" },
        { "rank": "SEVEN", "suit": "CLUBS" },
        { "rank": "TWO", "suit": "HEARTS" },
        { "rank": "NINE", "suit": "SPADES" },
        { "rank": "THREE", "suit": "CLUBS" }
      ],
      "winner": "Marcus",
      "winningHand": "PAIR",
      "potSize": 200,
      "actions": [
        { "phase": "PRE_FLOP", "player": "Marcus", "action": "RAISE", "amount": 30 },
        { "phase": "PRE_FLOP", "player": "The House", "action": "CALL", "amount": 30 },
        { "phase": "FLOP", "player": "The House", "action": "CHECK", "amount": 0 },
        { "phase": "FLOP", "player": "Marcus", "action": "BET", "amount": 40 },
        { "phase": "FLOP", "player": "The House", "action": "CALL", "amount": 40 },
        { "phase": "TURN", "player": "The House", "action": "CHECK", "amount": 0 },
        { "phase": "TURN", "player": "Marcus", "action": "CHECK", "amount": 0 },
        { "phase": "RIVER", "player": "The House", "action": "CHECK", "amount": 0 },
        { "phase": "RIVER", "player": "Marcus", "action": "BET", "amount": 60 },
        { "phase": "RIVER", "player": "The House", "action": "FOLD", "amount": 0 }
      ]
    }
  ]
}
```

---

### POST /api/v1/games/{id}/next-hand

Deal the next hand in the current game. Resets the deck, shuffles, deals new hole cards, posts blinds, and alternates the dealer button.

**curl:**
```bash
curl -X POST http://localhost:8080/api/v1/games/game_a1b2c3d4/next-hand
```

**Response (200 OK):**

Same structure as POST /api/v1/games response, with incremented `handNumber` and swapped dealer position.

---

## Data Models (Java DTOs)

### Enums

```java
public enum GamePhase {
    PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN
}

public enum PlayerAction {
    BET, CALL, FOLD, RAISE, CHECK, ALL_IN
}

public enum HandRank {
    ROYAL_FLUSH,
    STRAIGHT_FLUSH,
    FOUR_OF_A_KIND,
    FULL_HOUSE,
    FLUSH,
    STRAIGHT,
    THREE_OF_A_KIND,
    TWO_PAIR,
    PAIR,
    HIGH_CARD
}

public enum Suit {
    HEARTS, DIAMONDS, CLUBS, SPADES
}

public enum Rank {
    TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN,
    JACK, QUEEN, KING, ACE
}
```

### Request DTOs

```java
public record GameRequest(
    String playerName,
    int startingChips,
    int smallBlind,
    int bigBlind
) {}

public record ActionRequest(
    PlayerAction action,
    Integer amount  // Required for BET, RAISE; ignored for others
) {}
```

### Response DTOs

```java
public record GameResponse(
    String gameId,
    GamePhase phase,
    int pot,
    List<CardDto> communityCards,
    List<PlayerDto> players,
    int currentPlayerIndex,
    int handNumber,
    ActionDetail lastAction,
    int smallBlind,
    int bigBlind
) {}

public record PlayerDto(
    String name,
    int chips,
    List<CardDto> holeCards,  // null for opponent until showdown
    int currentBet,
    boolean folded,
    boolean isDealer
) {}

public record CardDto(
    String rank,
    String suit
) {}

public record ActionDetail(
    String player,
    PlayerAction action,
    int amount,
    String reasoning  // Opponent's reasoning (from LLM)
) {}

public record CoachingResponse(
    String advice,
    PlayerAction recommendedAction,
    String confidence,
    HandStrengthDto handStrength,
    OddsDto odds,
    ExplanationDto explanation
) {}

public record HandStrengthDto(
    HandRank currentRank,
    String bestHand,
    int nutDistance  // How many ranks away from the nut
) {}

public record OddsDto(
    double potOdds,
    String potOddsRatio,
    double handEquity,
    int outs,
    String outsDescription
) {}

public record OddsResponse(
    int potSize,
    int costToCall,
    double potOdds,
    String potOddsRatio,
    double handEquity,
    int outs,
    List<OutDetail> outsDetails,
    double winProbability,
    double tieProbability,
    double loseProbability,
    boolean isNut,
    String nutHand
) {}

public record OutDetail(
    String card,
    int count,
    String description
) {}

public record ExplanationDto(
    String situation,
    String mathSummary,
    String recommendation
) {}
```

### Error Response

```java
public record ErrorResponse(
    ErrorBody error
) {}

public record ErrorBody(
    String code,
    String message,
    Object details
) {}
```

---

## Card Representation

Cards are represented as objects with `rank` and `suit` string fields:

```json
{ "rank": "ACE", "suit": "SPADES" }
```

**Ranks:** TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE

**Suits:** HEARTS, DIAMONDS, CLUBS, SPADES

---

## Notes for Implementation

1. **Opponent cards hidden:** The opponent's `holeCards` field must be `null` in all responses until the game phase is `SHOWDOWN`. At showdown, both players' cards are revealed.

2. **Auto-advance opponent turn:** When the player submits an action via POST `/actions`, the backend should automatically process the opponent's turn (if applicable) before returning the response. The response includes the opponent's `lastAction` with reasoning.

3. **Coaching is read-only:** GET `/coaching` and GET `/odds` do not modify game state. They are safe to call at any time during a hand.

4. **CORS:** The Spring Boot app must configure CORS to allow requests from the Firebase Hosting domain and `localhost:5173` (Vite dev server).

5. **Firebase RTDB path:** Game state is written to `games/{gameId}` in Firebase RTDB. The frontend subscribes to this path for real-time updates. The REST API responses and RTDB state should be eventually consistent.
