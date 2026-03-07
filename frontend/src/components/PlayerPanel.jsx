import React from 'react';
import { Box, Typography } from '@mui/material';
import CardDisplay from './CardDisplay';

function PlayerPanel({ player, isCurrentTurn, showCards = true, position = 'bottom', isAiThinking = false, thought = null }) {
  if (!player) return null;

  const isTop = position === 'top';

  const showBubble = isTop && thought;

  return (
    <Box sx={{
      display: 'flex', flexDirection: 'column', alignItems: 'center',
      opacity: player.folded ? 0.45 : 1,
      transition: 'opacity 0.3s',
    }}>
      {/* Thought bubble — fixed height reservation so layout never shifts */}
      {isTop && (
        <Box sx={{
          height: 44, width: 240,
          display: 'flex', alignItems: 'flex-end', justifyContent: 'center',
          mb: 0.3,
        }}>
          {showBubble && (
            <Box sx={{
              position: 'relative',
              maxWidth: 230, px: 1.2, py: 0.4,
              bgcolor: 'rgba(245,158,11,0.08)',
              border: '1px solid rgba(245,158,11,0.2)',
              borderRadius: '10px',
              '&::after': {
                content: '""',
                position: 'absolute',
                bottom: -5,
                left: '50%', transform: 'translateX(-50%)',
                width: 0, height: 0,
                borderLeft: '5px solid transparent',
                borderRight: '5px solid transparent',
                borderTop: '5px solid rgba(245,158,11,0.2)',
              },
            }}>
              <Typography sx={{
                fontSize: '0.58rem', color: '#f59e0b', opacity: 0.8,
                fontStyle: 'italic', lineHeight: 1.35, textAlign: 'center',
                display: '-webkit-box',
                WebkitLineClamp: 2,
                WebkitBoxOrient: 'vertical',
                overflow: 'hidden',
              }}>
                &ldquo;{thought}&rdquo;
              </Typography>
            </Box>
          )}
        </Box>
      )}

      {/* Cards */}
      <Box sx={{
        display: 'flex', gap: '4px',
        order: isTop ? 0 : 1,
        filter: player.folded ? 'grayscale(0.8)' : 'none',
      }}>
        {player.holeCards ? (
          player.holeCards.map((card, i) => (
            <CardDisplay key={i} card={card} size="small" />
          ))
        ) : (
          <>
            <CardDisplay faceDown size="small" />
            <CardDisplay faceDown size="small" />
          </>
        )}
      </Box>

      {/* Info bar */}
      <Box sx={{
        display: 'flex', alignItems: 'center', gap: 1,
        px: 1.5, py: 0.4, mt: 0.5,
        borderRadius: '20px',
        background: isCurrentTurn
          ? 'linear-gradient(135deg, rgba(255,215,0,0.15), rgba(255,215,0,0.05))'
          : 'rgba(255,255,255,0.04)',
        border: isCurrentTurn
          ? '1px solid rgba(255,215,0,0.35)'
          : '1px solid rgba(255,255,255,0.06)',
        animation: isCurrentTurn ? 'turnGlow 2s ease-in-out infinite' : 'none',
        order: isTop ? 1 : 0,
      }}>
        <Typography sx={{
          fontSize: '0.75rem', fontWeight: 600, color: '#e8eaed',
          maxWidth: 100, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
        }}>
          {player.name}
        </Typography>

        {player.isDealer && (
          <Box sx={{
            width: 16, height: 16, borderRadius: '50%',
            background: 'linear-gradient(135deg, #ffd700, #f59e0b)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: 9, fontWeight: 800, color: '#1a1a2e', flexShrink: 0,
          }}>
            D
          </Box>
        )}

        <Box sx={{
          px: 0.8, py: 0.15, borderRadius: '10px',
          bgcolor: 'rgba(0,0,0,0.3)',
        }}>
          <Typography sx={{
            fontSize: '0.7rem', fontWeight: 700,
            color: '#ffd700',
            fontFamily: '"JetBrains Mono", monospace',
          }}>
            ${player.chips.toLocaleString()}
          </Typography>
        </Box>

        {player.currentBet > 0 && (
          <Box sx={{
            px: 0.8, py: 0.15, borderRadius: '10px',
            bgcolor: 'rgba(34,197,94,0.12)',
            border: '1px solid rgba(34,197,94,0.25)',
          }}>
            <Typography sx={{
              fontSize: '0.65rem', fontWeight: 600, color: '#22c55e',
              fontFamily: '"JetBrains Mono", monospace',
            }}>
              ${player.currentBet}
            </Typography>
          </Box>
        )}

        {player.folded && (
          <Typography sx={{
            fontSize: '0.6rem', fontWeight: 700, color: '#ef4444',
            letterSpacing: '0.05em', textTransform: 'uppercase',
          }}>
            Fold
          </Typography>
        )}

        {isAiThinking && (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.4 }}>
            {[0, 1, 2].map(i => (
              <Box key={i} sx={{
                width: 4, height: 4, borderRadius: '50%',
                bgcolor: '#f59e0b',
                animation: 'thinkingDot 1.2s ease-in-out infinite',
                animationDelay: `${i * 0.2}s`,
                '@keyframes thinkingDot': {
                  '0%, 80%, 100%': { opacity: 0.25, transform: 'scale(0.8)' },
                  '40%': { opacity: 1, transform: 'scale(1.2)' },
                },
              }} />
            ))}
          </Box>
        )}
      </Box>
    </Box>
  );
}

export default PlayerPanel;
