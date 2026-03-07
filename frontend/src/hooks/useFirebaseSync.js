import { useState, useEffect } from 'react';

/**
 * Custom hook for subscribing to real-time game state updates via Firebase RTDB.
 */
export function useFirebaseSync(gameId) {
  // TODO: Implement Firebase RTDB subscription:
  //   - State: firebaseState (object), connected (bool)
  //   - Subscribe to games/{gameId} path on mount
  //   - Update firebaseState on each value change
  //   - Track connection status
  //   - Unsubscribe on unmount or gameId change
  //   - Return: { firebaseState, connected }

  const [firebaseState, setFirebaseState] = useState(null);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    if (!gameId) return;

    // TODO: Subscribe to Firebase RTDB at games/{gameId}
    // TODO: Set up connection status listener
    // TODO: Return cleanup function to unsubscribe

    return () => {
      // TODO: Unsubscribe from Firebase
    };
  }, [gameId]);

  return { firebaseState, connected };
}
