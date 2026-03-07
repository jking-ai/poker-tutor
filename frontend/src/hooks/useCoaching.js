import { useState, useCallback } from 'react';
import { getCoaching } from '../api/client';

export function useCoaching(gameId) {
  const [coaching, setCoaching] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchCoaching = useCallback(async () => {
    if (!gameId) return null;
    try {
      setLoading(true);
      const data = await getCoaching(gameId);
      setCoaching(data);
      setError(null);
      return data;
    } catch (err) {
      setError({ message: err.message, code: err.code, status: err.status });
      return null;
    } finally {
      setLoading(false);
    }
  }, [gameId]);

  const clearCoaching = useCallback(() => {
    setCoaching(null);
    setError(null);
  }, []);

  return { coaching, loading, error, fetchCoaching, clearCoaching };
}
