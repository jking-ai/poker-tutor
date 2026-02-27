import React from 'react';
import { Box, Button } from '@mui/material';

/**
 * Player action controls: Check, Bet, Call, Raise, Fold, All In.
 * Only valid actions for the current game state are enabled.
 */
function ActionControls({ game, onAction }) {
  // TODO: Determine valid actions from game state:
  //   - CHECK: enabled when no outstanding bet
  //   - BET: enabled when no outstanding bet
  //   - CALL: enabled when there is an outstanding bet
  //   - RAISE: enabled when there is an outstanding bet
  //   - FOLD: always enabled
  //   - ALL_IN: always enabled
  // TODO: Render action buttons with appropriate colors:
  //   - Check/Call: green
  //   - Bet/Raise: gold
  //   - Fold: red
  //   - All In: gold with emphasis
  // TODO: Add bet/raise amount input (slider or number input)
  //   - Min bet = big blind
  //   - Max bet = player's chip stack
  // TODO: Disable all controls when it is not the player's turn

  return (
    <Box>
      {/* TODO: Implement action controls */}
      <Button>Check</Button>
      <Button>Call</Button>
      <Button>Raise</Button>
      <Button>Fold</Button>
    </Box>
  );
}

export default ActionControls;
