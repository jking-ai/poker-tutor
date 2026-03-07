# Local Testing Guide

How to run tests, test the API manually, and verify frontend behavior.

---

## 1. Backend Tests

<!-- TODO: Add test commands once test suite is created -->

```bash
cd backend

# Run all tests
./gradlew test

# Run with verbose output
./gradlew test --info

# Run a specific test class
./gradlew test --tests "com.jkingai.pokertutor.service.HandEvaluatorServiceTest"

# Run tests with coverage (requires JaCoCo plugin)
./gradlew test jacocoTestReport
```

### Test Structure

<!-- TODO: Document test classes and what they cover -->
<!-- Expected test areas:
  - HandEvaluatorServiceTest: hand ranking for all 10 ranks, comparison between hands
  - OddsCalculatorServiceTest: pot odds, outs counting, equity estimation
  - GameServiceTest: game creation, action processing, phase transitions, showdown
  - GameControllerTest: REST endpoint integration tests
  - DeckTest: shuffle, deal, reset
-->

---

## 2. API Testing (curl examples)

<!-- TODO: Update curl examples once endpoints are implemented -->

### Health Check

```bash
curl http://localhost:8080/api/v1/health
```

### Start a New Game

```bash
curl -X POST http://localhost:8080/api/v1/games \
  -H "Content-Type: application/json" \
  -d '{
    "playerName": "TestPlayer",
    "startingChips": 1000,
    "smallBlind": 5,
    "bigBlind": 10
  }'
```

### Get Game State

```bash
curl http://localhost:8080/api/v1/games/<game-id>
```

### Submit Player Action

```bash
curl -X POST http://localhost:8080/api/v1/games/<game-id>/actions \
  -H "Content-Type: application/json" \
  -d '{
    "action": "CALL",
    "amount": null
  }'
```

### Get Coaching Advice

```bash
curl http://localhost:8080/api/v1/games/<game-id>/coaching
```

### Get Odds

```bash
curl http://localhost:8080/api/v1/games/<game-id>/odds
```

### Get Hand History

```bash
curl http://localhost:8080/api/v1/games/<game-id>/history
```

### Deal Next Hand

```bash
curl -X POST http://localhost:8080/api/v1/games/<game-id>/next-hand
```

---

## 3. Frontend Testing

<!-- TODO: Add frontend test commands once test framework is configured -->

```bash
cd frontend

# Run tests (Vitest)
npm test

# Run with watch mode
npm run test:watch
```

---

## 4. Manual Testing

<!-- TODO: Add manual testing checklist -->

### Game Flow Validation

- [ ] Start a new game and verify hole cards are dealt
- [ ] Verify blinds are posted correctly (dealer = small blind in heads-up)
- [ ] Play a complete hand through all phases (pre-flop, flop, turn, river, showdown)
- [ ] Verify community cards are dealt at correct phases (3 on flop, 1 on turn, 1 on river)
- [ ] Verify pot calculation is correct after each action
- [ ] Verify showdown correctly evaluates hands and awards pot
- [ ] Deal next hand and verify dealer position alternates

### Opponent Agent Validation

- [ ] Opponent makes contextually appropriate decisions (not random)
- [ ] Opponent occasionally folds weak hands
- [ ] Opponent occasionally bluffs (raises with a weak hand)
- [ ] Opponent reasoning text makes sense for the action taken

### Coach Agent Validation

- [ ] Pot odds match manual calculation
- [ ] Outs count is correct for common draws (flush draw = 9, OESD = 8)
- [ ] Coaching advice changes based on hand strength and pot odds
- [ ] Recommended action aligns with mathematical analysis
- [ ] Explanation is clear enough for a beginner to understand

### Frontend Validation

- [ ] Game table renders correctly with all components
- [ ] Cards display with correct rank/suit symbols
- [ ] Action buttons enable/disable based on valid actions
- [ ] Real-time updates appear within 1 second via Firebase
- [ ] Coaching panel updates when game state changes
- [ ] Hand history displays correctly after completed hands
