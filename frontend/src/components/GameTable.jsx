import React from 'react';
import { Box, Typography, Button } from '@mui/material';
import CardDisplay from './CardDisplay';
import PlayerPanel from './PlayerPanel';
import ActionControls from './ActionControls';

function GameTable({ game, onAction, onDealNextHand, disabled }) {
  if (!game) return null;

  const opponent = game.players[1];
  const player = game.players[0];
  const isShowdown = game.phase === 'SHOWDOWN';

  // Get AI's latest thought (table talk during play, reasoning after showdown)
  const aiThought = game.lastAction?.player === opponent.name
    ? game.lastAction.reasoning : null;

  return (
    <Box sx={{
      display: 'flex', flexDirection: 'column',
      alignItems: 'center', gap: 1,
      width: '100%', maxWidth: 640,
    }}>
      {/* Opponent */}
      <PlayerPanel
        player={opponent}
        isCurrentTurn={game.currentPlayerIndex === 1}
        showCards={isShowdown}
        position="top"
        isAiThinking={game.currentPlayerIndex === 1 && !isShowdown && !game.gameOver}
        thought={aiThought}
      />

      {/* ---- Poker Table ---- */}
      <Box sx={{
        position: 'relative',
        width: '100%', maxWidth: 560,
        aspectRatio: '2.2 / 1',
      }}>
        {/* Outer rail */}
        <Box sx={{
          position: 'absolute', inset: 0,
          borderRadius: '50%',
          background: 'linear-gradient(160deg, #5c3310 0%, #2a1505 40%, #4a2810 100%)',
          boxShadow: '0 8px 32px rgba(0,0,0,0.7), inset 0 1px 3px rgba(255,200,100,0.08)',
        }} />

        {/* Inner felt */}
        <Box sx={{
          position: 'absolute', inset: '12px',
          borderRadius: '50%',
          background: 'radial-gradient(ellipse at 50% 40%, #2a7a35 0%, #1a5c25 40%, #0d3d15 100%)',
          boxShadow: 'inset 0 4px 20px rgba(0,0,0,0.5)',
          display: 'flex', flexDirection: 'column',
          alignItems: 'center', justifyContent: 'center',
          gap: 0.75,
        }}>
          {/* Phase label */}
          <Typography sx={{
            fontSize: '0.55rem', fontWeight: 600,
            color: 'rgba(255,215,0,0.5)', letterSpacing: '0.2em',
            textTransform: 'uppercase',
          }}>
            {game.phase.replace('_', ' ')}
          </Typography>

          {/* Community cards */}
          <Box sx={{ display: 'flex', gap: '5px', alignItems: 'center', minHeight: 74 }}>
            {game.communityCards.length > 0 ? (
              game.communityCards.map((card, i) => (
                <CardDisplay key={i} card={card} />
              ))
            ) : (
              [0, 1, 2, 3, 4].map(i => (
                <Box key={i} sx={{
                  width: 52, height: 74, borderRadius: '6px',
                  border: '1px dashed rgba(255,255,255,0.08)',
                }} />
              ))
            )}
          </Box>

          {/* Pot */}
          {game.pot > 0 && (
            <Box sx={{
              px: 1.5, py: 0.3, borderRadius: '14px',
              background: 'rgba(0,0,0,0.4)',
              border: '1px solid rgba(255,215,0,0.2)',
            }}>
              <Typography sx={{
                fontSize: '0.8rem', fontWeight: 700,
                color: '#ffd700',
                fontFamily: '"JetBrains Mono", monospace',
              }}>
                ${game.pot.toLocaleString()}
              </Typography>
            </Box>
          )}
        </Box>
      </Box>
      {/* ---- End Table ---- */}

      {/* Winner / Game Over banner */}
      {game.winnerMessage && (
        <Box sx={{
          px: 2, py: 0.5, borderRadius: '6px',
          background: game.gameOver
            ? 'linear-gradient(135deg, rgba(255,215,0,0.15), rgba(245,158,11,0.08))'
            : 'rgba(255,215,0,0.08)',
          border: '1px solid rgba(255,215,0,0.2)',
        }}>
          <Typography sx={{
            fontSize: '0.8rem', fontWeight: 600,
            color: '#ffd700', textAlign: 'center',
          }}>
            {game.gameOver ? 'GAME OVER — ' : ''}{game.winnerMessage}
          </Typography>
        </Box>
      )}

      {/* Player area */}
      <Box sx={{
        display: 'flex', alignItems: 'center', gap: 2,
        justifyContent: 'center',
      }}>
        <PlayerPanel
          player={player}
          isCurrentTurn={game.currentPlayerIndex === 0}
          showCards={true}
          position="bottom"
        />

        {game.gameOver ? (
          <Box /> /* empty — game over message shown above */
        ) : isShowdown ? (
          <Button
            variant="contained"
            onClick={onDealNextHand}
            size="small"
            sx={{
              bgcolor: 'rgba(255,215,0,0.12)',
              color: '#ffd700',
              border: '1px solid rgba(255,215,0,0.3)',
              fontWeight: 600, fontSize: '0.8rem',
              px: 2.5, py: 0.7,
              '&:hover': {
                bgcolor: 'rgba(255,215,0,0.2)',
                border: '1px solid rgba(255,215,0,0.5)',
              },
            }}
          >
            Deal Next Hand
          </Button>
        ) : (
          <ActionControls game={game} onAction={onAction} disabled={disabled} />
        )}
      </Box>
    </Box>
  );
}

export default GameTable;
