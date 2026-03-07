import React, { useEffect, useRef } from 'react';
import { Box, Typography } from '@mui/material';

function formatAction(action, amount) {
  switch (action) {
    case 'FOLD': return 'folds.';
    case 'CHECK': return 'checks.';
    case 'CALL': return `calls $${amount}.`;
    case 'BET': return `bets $${amount}.`;
    case 'RAISE': return `raises to $${amount}.`;
    case 'ALL_IN': return `goes all-in for $${amount}!`;
    default: return action;
  }
}

function ActionLog({ actionLog }) {
  const scrollRef = useRef(null);

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [actionLog]);

  return (
    <Box sx={{
      display: 'flex', flexDirection: 'column', height: '100%',
      bgcolor: 'rgba(0,0,0,0.25)',
      borderRadius: '8px',
      border: '1px solid rgba(255,255,255,0.04)',
      overflow: 'hidden',
    }}>
      <Box sx={{
        px: 1.5, py: 0.8,
        borderBottom: '1px solid rgba(255,255,255,0.04)',
      }}>
        <Typography sx={{
          fontSize: '0.65rem', fontWeight: 700,
          color: '#666', letterSpacing: '0.1em',
          textTransform: 'uppercase',
        }}>
          Game Log
        </Typography>
      </Box>
      <Box
        ref={scrollRef}
        sx={{ flex: 1, overflowY: 'auto', px: 1.5, py: 0.5 }}
      >
        {actionLog && actionLog.map((entry, i) => {
          const isSystem = entry.player === 'System';
          const isCoach = entry.player === 'Coach';
          const isPlayerAction = !isSystem && !isCoach && entry.action;
          // System entries use message field; player actions build from action data
          const text = isSystem
            ? (entry.message || '')
            : isCoach
              ? `Coach recommends: ${entry.action}`
              : `${entry.player} ${formatAction(entry.action, entry.amount)}`;
          const isHandHeader = isSystem && text.startsWith('---');
          const isPhaseHeader = isSystem && text.startsWith('***');
          const isGameOver = isSystem && text.startsWith('===');
          const isAiAction = entry.player === 'The House' && entry.action;
          const hasReasoning = (isAiAction || isCoach) && entry.message && entry.message.length > 0;

          return (
            <Box key={i}>
              <Typography
                sx={{
                  fontSize: '0.7rem',
                  lineHeight: 1.6,
                  fontFamily: '"JetBrains Mono", monospace',
                  color: isGameOver ? '#ffd700'
                    : isHandHeader ? '#f59e0b'
                    : isPhaseHeader ? '#22c55e'
                    : isCoach ? '#818cf8'
                    : isAiAction ? '#f59e0b'
                    : isPlayerAction ? '#60a5fa'
                    : isSystem ? '#6b7280'
                    : '#9ca3af',
                  fontWeight: (isHandHeader || isPhaseHeader || isGameOver || isCoach) ? 600 : 400,
                  mt: (isHandHeader || isCoach) ? 0.8 : 0,
                }}
              >
                {text}
              </Typography>
              {hasReasoning && (
                <Typography sx={{
                  fontSize: '0.6rem', lineHeight: 1.4, pl: 1.5,
                  fontStyle: 'italic',
                  color: isCoach ? '#a5b4fc' : '#f59e0b',
                  opacity: 0.7,
                  fontFamily: '"Inter", sans-serif',
                }}>
                  &ldquo;{entry.message}&rdquo;
                </Typography>
              )}
            </Box>
          );
        })}
      </Box>
    </Box>
  );
}

export default ActionLog;
