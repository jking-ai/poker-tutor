import React from 'react';
import { Box, Container } from '@mui/material';
import { useParams } from 'react-router-dom';

/**
 * Full hand history page. Shows all completed hands in the game session.
 */
function HistoryPage() {
  const { id: gameId } = useParams();

  // TODO: Fetch hand history from API: GET /api/v1/games/{id}/history
  // TODO: Render list of completed hands with:
  //   - Hand number
  //   - Player and opponent hole cards
  //   - Community cards
  //   - Winner and winning hand rank
  //   - Pot size
  //   - Expandable action timeline for each hand
  //   - Session summary (total hands, chip balance)

  return (
    <Container maxWidth="md">
      <Box sx={{ mt: 4 }}>
        {/* TODO: Implement history page */}
        <h1>Hand History</h1>
        <p>Game: {gameId} - TODO</p>
      </Box>
    </Container>
  );
}

export default HistoryPage;
