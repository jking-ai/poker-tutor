import { useState, useCallback } from 'react';

/**
 * Custom hook for dispatching game actions (bet, call, fold, raise, check).
 * Handles API calls and loading states for each action.
 */
export function useGameActions(gameId) {
  // TODO: Implement action dispatch:
  //   - State: submitting (bool), actionError (string)
  //   - submitAction(action, amount): POST /api/v1/games/{id}/actions
  //   - dealNextHand(): POST /api/v1/games/{id}/next-hand
  //   - Handle loading state during API call
  //   - Handle error responses (invalid action, game not found)
  //   - Return: { submitAction, dealNextHand, submitting, actionError }

  const [submitting, setSubmitting] = useState(false);
  const [actionError, setActionError] = useState(null);

  const submitAction = useCallback(async (action, amount) => {
    // TODO: Call submitAction API
  }, [gameId]);

  const dealNextHand = useCallback(async () => {
    // TODO: Call dealNextHand API
  }, [gameId]);

  return { submitAction, dealNextHand, submitting, actionError };
}
