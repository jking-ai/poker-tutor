import { useState, useEffect, useCallback, useRef } from 'react';
import { getGame, submitAction as submitActionApi, dealNextHand as dealNextHandApi } from '../api/client';

export function useGame(gameId) {
  const [game, setGame] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const pollingRef = useRef(null);

  const refreshGame = useCallback(async () => {
    if (!gameId) return;
    try {
      setLoading(true);
      const data = await getGame(gameId);
      setGame(data);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [gameId]);

  useEffect(() => {
    refreshGame();
  }, [refreshGame]);

  // Poll when it's AI's turn — the GET triggers the backend to auto-process
  useEffect(() => {
    if (pollingRef.current) {
      clearInterval(pollingRef.current);
      pollingRef.current = null;
    }

    const isAiTurn = game
      && game.phase !== 'SHOWDOWN'
      && game.currentPlayerIndex === 1
      && !game.gameOver;

    if (isAiTurn) {
      pollingRef.current = setInterval(async () => {
        try {
          const data = await getGame(gameId);
          setGame(data);
          // Stop polling once it's no longer AI's turn
          if (data.currentPlayerIndex !== 1 || data.phase === 'SHOWDOWN') {
            clearInterval(pollingRef.current);
            pollingRef.current = null;
          }
        } catch (_) { /* ignore polling errors */ }
      }, 2000);
    }

    return () => {
      if (pollingRef.current) {
        clearInterval(pollingRef.current);
        pollingRef.current = null;
      }
    };
  }, [game?.currentPlayerIndex, game?.phase, game?.gameOver, gameId]);

  const submitAction = useCallback(async (action, amount) => {
    try {
      setLoading(true);
      const data = await submitActionApi(gameId, action, amount);
      setGame(data);
      setError(null);
      return data;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [gameId]);

  const dealNextHand = useCallback(async () => {
    try {
      setLoading(true);
      const data = await dealNextHandApi(gameId);
      setGame(data);
      setError(null);
      return data;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [gameId]);

  return { game, loading, error, refreshGame, submitAction, dealNextHand };
}
