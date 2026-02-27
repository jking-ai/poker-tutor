import React from 'react';
import { Box } from '@mui/material';

/**
 * Displays the action timeline for the current hand or a completed hand.
 * Shows each action with player name, action type, amount, and phase.
 */
function HandHistory({ history }) {
  // TODO: Render hand history as a timeline or list:
  //   - Each action entry: player name, action (e.g., "RAISE $50"), phase
  //   - Phase dividers (PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN)
  //   - Community cards dealt at each phase
  //   - Winner and winning hand at showdown
  //   - Pot size at each step
  //   - Scrollable if history is long

  return (
    <Box>
      {/* TODO: Implement hand history display */}
      <p>HandHistory - TODO</p>
    </Box>
  );
}

export default HandHistory;
