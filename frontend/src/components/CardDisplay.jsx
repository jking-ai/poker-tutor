import React from 'react';
import { Box } from '@mui/material';

/**
 * Renders a playing card with rank and suit.
 * Supports face-up (showing rank/suit) and face-down (card back) states.
 */
function CardDisplay({ card, faceDown = false }) {
  // TODO: Render card with:
  //   - Face-up: rank symbol + suit symbol, colored (red for hearts/diamonds, black for clubs/spades)
  //   - Face-down: card back pattern
  //   - Rounded corners, white background for face-up, dark pattern for face-down
  //   - Suit symbols: Hearts, Diamonds, Clubs, Spades
  //   - Rank display: A, K, Q, J, 10, 9, 8, 7, 6, 5, 4, 3, 2

  return (
    <Box>
      {/* TODO: Implement card display */}
      <p>{faceDown ? '??' : `${card?.rank} ${card?.suit}`}</p>
    </Box>
  );
}

export default CardDisplay;
