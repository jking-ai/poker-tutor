import React from 'react';
import { Box } from '@mui/material';

/**
 * Displays player information: name, chip count, hole cards, current bet, and dealer status.
 */
function PlayerPanel({ player, isCurrentTurn, showCards = true }) {
  // TODO: Render player panel with:
  //   - Player name
  //   - Chip count (formatted with chip icon)
  //   - Hole cards (2 CardDisplay components, face-down if !showCards)
  //   - Current bet amount (if any)
  //   - Dealer button indicator (D chip)
  //   - Visual highlight if it is this player's turn
  //   - Folded state (greyed out cards)

  return (
    <Box>
      {/* TODO: Implement player panel */}
      <p>{player?.name} - Chips: {player?.chips}</p>
    </Box>
  );
}

export default PlayerPanel;
