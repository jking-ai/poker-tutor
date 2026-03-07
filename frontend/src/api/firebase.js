/**
 * Firebase Realtime Database client for real-time game state synchronization.
 */

// TODO: Import Firebase modules
// import { initializeApp } from 'firebase/app';
// import { getDatabase, ref, onValue, off } from 'firebase/database';

// TODO: Configure Firebase app with project settings
// const firebaseConfig = {
//   apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
//   authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
//   databaseURL: import.meta.env.VITE_FIREBASE_DATABASE_URL,
//   projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
// };

/**
 * Initialize the Firebase app. Call once at app startup.
 */
export function initializeFirebase() {
  // TODO: Initialize Firebase app
  // const app = initializeApp(firebaseConfig);
  // return getDatabase(app);
}

/**
 * Subscribe to real-time game state updates.
 * Returns an unsubscribe function.
 */
export function subscribeToGame(gameId, callback) {
  // TODO: Create a ref to games/{gameId}
  // TODO: Attach onValue listener
  // TODO: Call callback with game state on each update
  // TODO: Return unsubscribe function
}

/**
 * Unsubscribe from game state updates.
 */
export function unsubscribeFromGame(gameId) {
  // TODO: Detach listener for games/{gameId}
}
