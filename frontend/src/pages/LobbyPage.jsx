import React, { useState } from 'react';
import {
  Box, Typography, TextField, Button, MenuItem,
  CircularProgress, Alert, Dialog, DialogTitle, DialogContent,
  DialogContentText, DialogActions, useMediaQuery,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { createGame } from '../api/client';

const CHIP_OPTIONS = [500, 1000, 2000];
const BLIND_OPTIONS = [
  { label: '5 / 10', small: 5, big: 10 },
  { label: '10 / 20', small: 10, big: 20 },
  { label: '25 / 50', small: 25, big: 50 },
];

function LobbyPage() {
  const navigate = useNavigate();
  const [playerName, setPlayerName] = useState('');
  const [startingChips, setStartingChips] = useState(1000);
  const [blindIndex, setBlindIndex] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showDisclaimer, setShowDisclaimer] = useState(false);
  const isMobile = useMediaQuery('(max-width:767px)');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!playerName.trim()) return;
    setShowDisclaimer(true);
  };

  const handleConfirm = async () => {
    setShowDisclaimer(false);
    try {
      setLoading(true);
      setError(null);
      const blind = BLIND_OPTIONS[blindIndex];
      const game = await createGame(playerName.trim(), startingChips, blind.small, blind.big);
      navigate(`/game/${game.gameId}`);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const inputSx = {
    '& .MuiOutlinedInput-root': {
      bgcolor: 'rgba(0,0,0,0.2)',
      '& fieldset': { borderColor: 'rgba(255,255,255,0.08)' },
      '&:hover fieldset': { borderColor: 'rgba(255,255,255,0.15)' },
      '&.Mui-focused fieldset': { borderColor: 'rgba(255,215,0,0.4)' },
    },
    '& .MuiInputLabel-root': { color: '#555', fontSize: '0.85rem' },
    '& .MuiInputLabel-root.Mui-focused': { color: '#ffd700' },
  };

  return (
    <Box sx={{
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      minHeight: 'calc(100vh - 42px)',
      px: isMobile ? 2 : 4,
      mt: isMobile ? 0 : '-100px',
    }}>
      <Box sx={{
        display: 'flex',
        flexDirection: isMobile ? 'column' : 'row',
        alignItems: 'center',
        gap: isMobile ? 3 : 6,
        maxWidth: 820,
        width: '100%',
      }}>
        {/* Logo */}
        <Box sx={{
          flex: '0 0 auto', display: 'flex', flexDirection: 'column', alignItems: 'center',
        }}>
          <Box
            component="img"
            src="/the-nut-logo.png"
            alt="The Nut"
            sx={{
              width: isMobile ? 180 : 340,
              height: 'auto',
              filter: 'drop-shadow(0 4px 24px rgba(255,215,0,0.15))',
            }}
          />
          <Typography sx={{ fontSize: '0.65rem', color: '#333', mt: 1, textAlign: 'center' }}>
            Heads-up No-Limit Texas Hold'em with AI coaching
          </Typography>
        </Box>

        {/* Form */}
        <Box sx={{ flex: '1 1 320px', maxWidth: 360, width: '100%' }}>
          <Typography sx={{
            fontSize: '0.8rem', color: '#555', mb: 2, letterSpacing: '0.1em',
            textAlign: isMobile ? 'center' : 'left',
          }}>
            TEXAS HOLD'EM POKER TUTOR
          </Typography>

          <Box sx={{
            width: '100%', p: isMobile ? 2.5 : 3, borderRadius: '12px',
            bgcolor: 'rgba(19,26,36,0.8)',
            border: '1px solid rgba(255,255,255,0.06)',
            backdropFilter: 'blur(8px)',
          }}>
            {error && <Alert severity="error" sx={{ mb: 2, fontSize: '0.8rem' }}>{error}</Alert>}

            <form onSubmit={handleSubmit}>
              <TextField
                label="Your Name"
                value={playerName}
                onChange={(e) => setPlayerName(e.target.value)}
                fullWidth required size="small"
                sx={{ ...inputSx, mb: 2 }}
              />

              <TextField
                select label="Starting Chips"
                value={startingChips}
                onChange={(e) => setStartingChips(Number(e.target.value))}
                fullWidth size="small"
                sx={{ ...inputSx, mb: 2 }}
              >
                {CHIP_OPTIONS.map((chips) => (
                  <MenuItem key={chips} value={chips}>${chips.toLocaleString()}</MenuItem>
                ))}
              </TextField>

              <TextField
                select label="Blinds"
                value={blindIndex}
                onChange={(e) => setBlindIndex(Number(e.target.value))}
                fullWidth size="small"
                sx={{ ...inputSx, mb: 3 }}
              >
                {BLIND_OPTIONS.map((blind, i) => (
                  <MenuItem key={i} value={i}>{blind.label}</MenuItem>
                ))}
              </TextField>

              <Button
                type="submit"
                fullWidth
                disabled={loading || !playerName.trim()}
                sx={{
                  py: 1.2,
                  background: 'linear-gradient(135deg, #ffd700, #f59e0b)',
                  color: '#1a1a2e',
                  fontWeight: 700, fontSize: '0.9rem',
                  borderRadius: '8px',
                  '&:hover': {
                    background: 'linear-gradient(135deg, #ffe44d, #fbbf24)',
                    boxShadow: '0 4px 20px rgba(255,215,0,0.25)',
                  },
                  '&.Mui-disabled': {
                    background: 'rgba(255,215,0,0.15)',
                    color: 'rgba(26,26,46,0.5)',
                  },
                }}
              >
                {loading ? <CircularProgress size={20} sx={{ color: '#1a1a2e' }} /> : 'Start Game'}
              </Button>
            </form>
          </Box>
        </Box>
      </Box>

      <Dialog
        open={showDisclaimer}
        onClose={() => setShowDisclaimer(false)}
        PaperProps={{
          sx: {
            bgcolor: '#1a2233', border: '1px solid rgba(255,255,255,0.08)',
            borderRadius: '12px', maxWidth: 420,
            mx: isMobile ? 2 : 'auto',
          },
        }}
      >
        <DialogTitle sx={{ color: '#ffd700', fontSize: '1rem', fontWeight: 700, pb: 0.5 }}>
          Before You Play
        </DialogTitle>
        <DialogContent>
          <DialogContentText sx={{ color: '#9ca3af', fontSize: '0.82rem', lineHeight: 1.7 }}>
            This is a <strong style={{ color: '#d1d5db' }}>technology demonstration</strong> of
            AI-powered poker tutoring. The AI opponent and coaching features are powered by
            Google Gemini and are subject to usage limits per session. If limits are reached,
            the opponent will switch to basic play and coaching will be temporarily unavailable.
          </DialogContentText>
          <DialogContentText sx={{ color: '#6b7280', fontSize: '0.75rem', mt: 1.5 }}>
            No real money is involved. Game data is not persisted between sessions.
          </DialogContentText>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setShowDisclaimer(false)} sx={{ color: '#666', fontSize: '0.8rem' }}>
            Cancel
          </Button>
          <Button
            onClick={handleConfirm}
            variant="contained"
            sx={{
              background: 'linear-gradient(135deg, #ffd700, #f59e0b)',
              color: '#1a1a2e', fontWeight: 700, fontSize: '0.8rem',
              '&:hover': { background: 'linear-gradient(135deg, #ffe44d, #fbbf24)' },
            }}
          >
            Got It, Deal Me In
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

export default LobbyPage;
