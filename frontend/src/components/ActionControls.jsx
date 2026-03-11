import React, { useState } from 'react';
import { Box, Button, Slider, Typography } from '@mui/material';

const ACTION_CONFIG = {
  CHECK:  { label: 'Check',  bg: 'rgba(34,197,94,0.1)',  border: 'rgba(34,197,94,0.3)',  color: '#22c55e', hoverBg: 'rgba(34,197,94,0.18)' },
  CALL:   { label: 'Call',   bg: 'rgba(34,197,94,0.1)',  border: 'rgba(34,197,94,0.3)',  color: '#22c55e', hoverBg: 'rgba(34,197,94,0.18)' },
  BET:    { label: 'Bet',    bg: 'rgba(245,158,11,0.1)', border: 'rgba(245,158,11,0.3)', color: '#f59e0b', hoverBg: 'rgba(245,158,11,0.18)' },
  RAISE:  { label: 'Raise',  bg: 'rgba(245,158,11,0.1)', border: 'rgba(245,158,11,0.3)', color: '#f59e0b', hoverBg: 'rgba(245,158,11,0.18)' },
  FOLD:   { label: 'Fold',   bg: 'rgba(239,68,68,0.08)', border: 'rgba(239,68,68,0.25)', color: '#ef4444', hoverBg: 'rgba(239,68,68,0.15)' },
  ALL_IN: { label: 'All In', bg: 'rgba(245,158,11,0.15)', border: 'rgba(245,158,11,0.4)', color: '#fbbf24', hoverBg: 'rgba(245,158,11,0.25)' },
};

function ActionControls({ game, onAction, disabled, compact = false }) {
  const [betAmount, setBetAmount] = useState(0);
  const [allInConfirm, setAllInConfirm] = useState(false);

  if (!game || game.phase === 'SHOWDOWN') return null;

  const validActions = game.validActions || [];
  const isPlayerTurn = game.currentPlayerIndex === 0;
  if (!isPlayerTurn) return null;

  const showBetSlider = validActions.includes('BET') || validActions.includes('RAISE');
  const minBet = game.bigBlind;
  const player = game.players[0];
  const maxBet = player.chips;

  const handleAction = (action) => {
    if (action === 'ALL_IN' && !allInConfirm) {
      setAllInConfirm(true);
      return;
    }
    const amount = (action === 'BET' || action === 'RAISE')
      ? (betAmount > 0 ? betAmount : minBet)
      : null;
    onAction(action, amount);
    setBetAmount(0);
    setAllInConfirm(false);
  };

  /* Mobile compact: horizontal row of buttons */
  if (compact) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5, width: '100%', maxWidth: 380 }}>
        {/* Action buttons in a horizontal wrap */}
        <Box sx={{ display: 'flex', gap: '6px', justifyContent: 'center', flexWrap: 'wrap' }}>
          {validActions.map((action) => {
            const cfg = ACTION_CONFIG[action] || ACTION_CONFIG.CHECK;
            const isAllIn = action === 'ALL_IN';
            const showConfirm = isAllIn && allInConfirm;
            return (
              <Button
                key={action}
                onClick={() => handleAction(action)}
                onBlur={() => { if (isAllIn) setAllInConfirm(false); }}
                disabled={disabled}
                size="small"
                sx={{
                  bgcolor: showConfirm ? 'rgba(239,68,68,0.15)' : cfg.bg,
                  color: showConfirm ? '#fbbf24' : cfg.color,
                  border: showConfirm
                    ? '1px solid rgba(239,68,68,0.5)'
                    : `1px solid ${cfg.border}`,
                  fontWeight: 600, fontSize: '0.75rem',
                  py: 0.8, px: 2,
                  minHeight: 38,
                  minWidth: 72,
                  justifyContent: 'center',
                  animation: showConfirm ? 'pulse 1.5s ease-in-out infinite' : 'none',
                  '&:hover': {
                    bgcolor: showConfirm ? 'rgba(239,68,68,0.25)' : cfg.hoverBg,
                    border: showConfirm
                      ? '1px solid rgba(239,68,68,0.7)'
                      : `1px solid ${cfg.color}`,
                  },
                  '&.Mui-disabled': { opacity: 0.4 },
                }}
              >
                {showConfirm
                  ? `All In $${player.chips.toLocaleString()}`
                  : cfg.label}
              </Button>
            );
          })}
        </Box>

        {/* Bet slider */}
        {showBetSlider && maxBet > minBet && (
          <Box sx={{ px: 1, pt: 0.3 }}>
            <Slider
              size="small"
              min={minBet}
              max={maxBet}
              step={minBet}
              value={betAmount || minBet}
              onChange={(_, val) => setBetAmount(val)}
              sx={{
                color: '#f59e0b',
                height: 6,
                '& .MuiSlider-thumb': {
                  width: 20, height: 20,
                  bgcolor: '#fbbf24',
                  '&:hover': { boxShadow: '0 0 8px rgba(251,191,36,0.4)' },
                },
                '& .MuiSlider-track': { border: 'none' },
                '& .MuiSlider-rail': { bgcolor: 'rgba(255,255,255,0.08)' },
              }}
            />
            <Typography sx={{
              fontSize: '0.7rem', textAlign: 'center',
              color: '#f59e0b', fontWeight: 600,
              fontFamily: '"JetBrains Mono", monospace',
            }}>
              ${(betAmount || minBet).toLocaleString()}
            </Typography>
          </Box>
        )}
      </Box>
    );
  }

  /* Desktop: vertical column layout */
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5, minWidth: 110 }}>
      {/* Action buttons */}
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
        {validActions.map((action) => {
          const cfg = ACTION_CONFIG[action] || ACTION_CONFIG.CHECK;
          const isAllIn = action === 'ALL_IN';
          const showConfirm = isAllIn && allInConfirm;
          return (
            <Button
              key={action}
              onClick={() => handleAction(action)}
              onBlur={() => { if (isAllIn) setAllInConfirm(false); }}
              disabled={disabled}
              size="small"
              sx={{
                bgcolor: showConfirm ? 'rgba(239,68,68,0.15)' : cfg.bg,
                color: showConfirm ? '#fbbf24' : cfg.color,
                border: showConfirm
                  ? '1px solid rgba(239,68,68,0.5)'
                  : `1px solid ${cfg.border}`,
                fontWeight: 600, fontSize: '0.75rem',
                py: 0.4, px: 1.5,
                justifyContent: 'center',
                animation: showConfirm ? 'pulse 1.5s ease-in-out infinite' : 'none',
                '&:hover': {
                  bgcolor: showConfirm ? 'rgba(239,68,68,0.25)' : cfg.hoverBg,
                  border: showConfirm
                    ? '1px solid rgba(239,68,68,0.7)'
                    : `1px solid ${cfg.color}`,
                },
                '&.Mui-disabled': { opacity: 0.4 },
              }}
            >
              {showConfirm
                ? `Confirm All In — $${player.chips.toLocaleString()}`
                : cfg.label}
            </Button>
          );
        })}
      </Box>

      {/* Bet slider */}
      {showBetSlider && maxBet > minBet && (
        <Box sx={{ px: 0.5, pt: 0.5 }}>
          <Slider
            size="small"
            min={minBet}
            max={maxBet}
            step={minBet}
            value={betAmount || minBet}
            onChange={(_, val) => setBetAmount(val)}
            sx={{
              color: '#f59e0b',
              height: 4,
              '& .MuiSlider-thumb': {
                width: 14, height: 14,
                bgcolor: '#fbbf24',
                '&:hover': { boxShadow: '0 0 8px rgba(251,191,36,0.4)' },
              },
              '& .MuiSlider-track': { border: 'none' },
              '& .MuiSlider-rail': { bgcolor: 'rgba(255,255,255,0.08)' },
            }}
          />
          <Typography sx={{
            fontSize: '0.65rem', textAlign: 'center',
            color: '#f59e0b', fontWeight: 600,
            fontFamily: '"JetBrains Mono", monospace',
          }}>
            ${(betAmount || minBet).toLocaleString()}
          </Typography>
        </Box>
      )}
    </Box>
  );
}

export default ActionControls;
