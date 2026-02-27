import { useState, useCallback } from 'react';

/**
 * Custom hook for fetching coaching advice and odds data.
 */
export function useCoaching(gameId) {
  // TODO: Implement coaching data fetching:
  //   - State: coaching (object), odds (object), loading (bool), error (string)
  //   - fetchCoaching(): GET /api/v1/games/{id}/coaching
  //   - fetchOdds(): GET /api/v1/games/{id}/odds
  //   - Auto-refresh when game state changes (new phase, new cards)
  //   - Return: { coaching, odds, loading, error, fetchCoaching, fetchOdds }

  const [coaching, setCoaching] = useState(null);
  const [odds, setOdds] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchCoaching = useCallback(async () => {
    // TODO: Call getCoaching API
  }, [gameId]);

  const fetchOdds = useCallback(async () => {
    // TODO: Call getOdds API
  }, [gameId]);

  return { coaching, odds, loading, error, fetchCoaching, fetchOdds };
}
