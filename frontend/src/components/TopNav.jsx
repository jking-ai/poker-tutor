import React from 'react';
import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom';

function TopNav() {
  const navigate = useNavigate();
  const location = useLocation();
  const isInGame = location.pathname.startsWith('/game/');

  return (
    <AppBar
      position="static"
      elevation={0}
      sx={{
        bgcolor: 'rgba(10,14,20,0.92)',
        borderBottom: '1px solid rgba(255,255,255,0.06)',
        backdropFilter: 'blur(12px)',
      }}
    >
      <Toolbar variant="dense" sx={{ minHeight: 42, px: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.8, flexGrow: 1 }}>
          <Box
            component="img"
            src="/the-nut-logo.png"
            alt="The Nut"
            sx={{ height: 30, width: 'auto' }}
          />
          <Typography
            variant="subtitle1"
            sx={{
              fontWeight: 700, fontSize: '0.85rem', letterSpacing: '0.02em',
              color: '#e8eaed',
            }}
          >
            THE NUT
          </Typography>
          <Typography variant="caption" sx={{ color: '#666', ml: 0.5 }}>
            Poker Tutor
          </Typography>
        </Box>
        {isInGame && (
          <Button
            size="small"
            onClick={() => navigate('/')}
            sx={{
              color: '#9aa0a6', fontSize: '0.75rem', fontWeight: 500,
              '&:hover': { color: '#ffd700', bgcolor: 'rgba(255,215,0,0.06)' },
            }}
          >
            New Game
          </Button>
        )}
      </Toolbar>
    </AppBar>
  );
}

export default TopNav;
