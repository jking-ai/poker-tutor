import { useState, useEffect } from 'react';

/**
 * Custom hook for managing game state.
 * Fetches initial game state from the API and merges real-time Firebase updates.
 */
export function useGame(gameId) {
  // TODO: Implement game state management:
  //   - State: game (object), loading (bool), error (string)
  //   - Fetch initial game state from GET /api/v1/games/{gameId}
  //   - Merge updates from useFirebaseSync
  //   - Handle loading and error states
  //   - Return: { game, loading, error, refreshGame }

  const [game, setGame] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    // TODO: Fetch initial game state
    // TODO: Set up Firebase subscription for real-time updates
    // TODO: Cleanup on unmount
  }, [gameId]);

  return { game, loading, error };
}
