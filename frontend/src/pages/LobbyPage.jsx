import React from 'react';
import { Box, Container } from '@mui/material';

/**
 * Game lobby page. Start a new game with configuration options.
 */
function LobbyPage() {
  // TODO: Render lobby with:
  //   - Welcome message and brief instructions
  //   - "New Game" form:
  //     - Player name input
  //     - Starting chips selector (500, 1000, 2000)
  //     - Blind level selector (5/10, 10/20, 25/50)
  //   - "Start Game" button
  //   - On submit: call createGame API, navigate to /game/{id}
  //   - Loading state while creating game

  return (
    <Container maxWidth="md">
      <Box sx={{ mt: 4 }}>
        {/* TODO: Implement lobby page */}
        <h1>Welcome to The Nut</h1>
        <p>TODO: Implement game lobby</p>
      </Box>
    </Container>
  );
}

export default LobbyPage;
