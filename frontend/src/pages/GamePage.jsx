import React from 'react';
import { Box, Container, Grid } from '@mui/material';
import { useParams } from 'react-router-dom';

/**
 * Main game page. Renders the poker table, coaching panel, and hand history.
 */
function GamePage() {
  const { id: gameId } = useParams();

  // TODO: Use hooks:
  //   - useGame(gameId) for game state
  //   - useFirebaseSync(gameId) for real-time updates
  //   - useGameActions(gameId) for dispatching actions
  //   - useCoaching(gameId) for coaching data

  // TODO: Render layout:
  //   - Left/center: GameTable (main game area)
  //   - Right: CoachingPanel (collapsible sidebar)
  //   - Bottom: HandHistory (current hand actions)
  //   - Handle loading, error, and game-not-found states

  return (
    <Container maxWidth="xl">
      <Box sx={{ mt: 2 }}>
        {/* TODO: Implement game page layout */}
        <p>Game: {gameId} - TODO</p>
      </Box>
    </Container>
  );
}

export default GamePage;
