import React from 'react';
import { AppBar, Toolbar, Typography } from '@mui/material';

/**
 * Top navigation bar with app title and navigation links.
 */
function TopNav() {
  // TODO: Render AppBar with:
  //   - App title: "The Nut" with poker chip icon
  //   - Navigation links: Lobby (home), Game (if active), History
  //   - Responsive: hamburger menu on small screens

  return (
    <AppBar position="static">
      <Toolbar>
        <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
          {/* TODO: Style the title with poker theme */}
          The Nut - Poker Tutor
        </Typography>
        {/* TODO: Add navigation links */}
      </Toolbar>
    </AppBar>
  );
}

export default TopNav;
