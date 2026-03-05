import React from 'react';
import { Box, Typography } from '@mui/material';
import PlayerPanel from './PlayerPanel';
import CardDisplay from './CardDisplay';
import ActionControls from './ActionControls';

/**
 * Main poker table layout component.
 * Renders an oval poker table graphic with opponent at top, community cards
 * and pot in the center felt, and the player at the bottom.
 */
function GameTable({ game, onAction }) {
  const opponent = game?.players?.find(p => p.type === 'AI') ?? game?.opponent;
  const player = game?.players?.find(p => p.type === 'HUMAN') ?? game?.player;
  const communityCards = game?.communityCards ?? [];
  const pot = game?.pot ?? 0;
  const phase = game?.phase ?? '';

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', py: 2 }}>

      {/* Opponent seat — overlaps the top rail */}
      <Box sx={{ zIndex: 2, mb: '-36px' }}>
        <PlayerPanel
          player={opponent}
          isCurrentTurn={game?.currentTurn === 'AI'}
          showCards={game?.phase === 'SHOWDOWN'}
        />
      </Box>

      {/* ── Poker table ── */}
      <Box
        sx={{
          position: 'relative',
          width: { xs: '320px', sm: '560px', md: '720px' },
          height: { xs: '190px', sm: '310px', md: '390px' },
        }}
      >
        {/* Outer rail — dark wood */}
        <Box
          sx={{
            position: 'absolute',
            inset: 0,
            borderRadius: '50%',
            background: 'linear-gradient(160deg, #6b3a18 0%, #3b1f08 50%, #5a2e0d 100%)',
            boxShadow: '0 10px 40px rgba(0,0,0,0.8), inset 0 2px 6px rgba(255,200,100,0.12)',
          }}
        />

        {/* Inner felt */}
        <Box
          sx={{
            position: 'absolute',
            inset: '18px',
            borderRadius: '50%',
            background: 'radial-gradient(ellipse at 50% 40%, #3d7020 0%, #2d5016 55%, #1e3a0d 100%)',
            boxShadow: 'inset 0 6px 24px rgba(0,0,0,0.6)',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            gap: { xs: 0.75, sm: 1.25 },
          }}
        >
          {/* Phase label */}
          {phase && (
            <Typography
              variant="caption"
              sx={{
                color: '#ffd700',
                letterSpacing: '0.2em',
                textTransform: 'uppercase',
                fontSize: { xs: '0.55rem', sm: '0.65rem' },
                opacity: 0.85,
              }}
            >
              {phase.replace('_', ' ')}
            </Typography>
          )}

          {/* Community cards — 5 placeholders */}
          <Box sx={{ display: 'flex', gap: { xs: 0.5, sm: 1 }, alignItems: 'center' }}>
            {[0, 1, 2, 3, 4].map(i => (
              <CardDisplay
                key={i}
                card={communityCards[i] ?? null}
                faceDown={!communityCards[i]}
              />
            ))}
          </Box>

          {/* Pot display */}
          <Box
            sx={{
              px: { xs: 1.5, sm: 2 },
              py: 0.5,
              borderRadius: '12px',
              background: 'rgba(0,0,0,0.45)',
              border: '1px solid rgba(255,215,0,0.35)',
            }}
          >
            <Typography
              variant="body2"
              sx={{
                color: '#ffd700',
                fontWeight: 700,
                fontSize: { xs: '0.7rem', sm: '0.85rem' },
                letterSpacing: '0.05em',
              }}
            >
              POT: ${pot.toLocaleString()}
            </Typography>
          </Box>
        </Box>
      </Box>
      {/* ── End poker table ── */}

      {/* Player seat — overlaps the bottom rail */}
      <Box sx={{ zIndex: 2, mt: '-36px' }}>
        <PlayerPanel
          player={player}
          isCurrentTurn={game?.currentTurn === 'HUMAN'}
          showCards
        />
      </Box>

      {/* Action controls below the table */}
      <Box sx={{ mt: 2 }}>
        <ActionControls game={game} onAction={onAction} />
      </Box>
    </Box>
  );
}

export default GameTable;
