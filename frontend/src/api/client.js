/**
 * REST API client for the poker-tutor backend.
 * Base URL is configurable via VITE_API_URL environment variable.
 */

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// TODO: Implement API client methods:

/**
 * Health check.
 * GET /api/v1/health
 */
export async function healthCheck() {
  // TODO: Implement fetch call
}

/**
 * Start a new game.
 * POST /api/v1/games
 */
export async function createGame(playerName, startingChips, smallBlind, bigBlind) {
  // TODO: Implement fetch call with JSON body
}

/**
 * Get current game state.
 * GET /api/v1/games/{id}
 */
export async function getGame(gameId) {
  // TODO: Implement fetch call
}

/**
 * Submit a player action.
 * POST /api/v1/games/{id}/actions
 */
export async function submitAction(gameId, action, amount) {
  // TODO: Implement fetch call with JSON body
}

/**
 * Get coaching advice.
 * GET /api/v1/games/{id}/coaching
 */
export async function getCoaching(gameId) {
  // TODO: Implement fetch call
}

/**
 * Get pot odds and probabilities.
 * GET /api/v1/games/{id}/odds
 */
export async function getOdds(gameId) {
  // TODO: Implement fetch call
}

/**
 * Get hand history.
 * GET /api/v1/games/{id}/history
 */
export async function getHistory(gameId) {
  // TODO: Implement fetch call
}

/**
 * Deal next hand.
 * POST /api/v1/games/{id}/next-hand
 */
export async function dealNextHand(gameId) {
  // TODO: Implement fetch call
}
