import React, { useState, useCallback, useMemo } from 'react';
import { Box, Typography, CircularProgress, Alert } from '@mui/material';
import { useParams } from 'react-router-dom';
import { useGame } from '../hooks/useGame';
import { useCoaching } from '../hooks/useCoaching';
import GameTable from '../components/GameTable';
import CoachingPanel from '../components/CoachingPanel';
import ActionLog from '../components/ActionLog';

function GamePage() {
  const { id: gameId } = useParams();
  const { game, loading, error, submitAction, dealNextHand } = useGame(gameId);
  const { coaching, loading: coachingLoading, error: coachingError, fetchCoaching, clearCoaching } = useCoaching(gameId);
  const [coachLogEntries, setCoachLogEntries] = useState([]);

  const handleRequestCoaching = useCallback(async () => {
    // Capture insert position now so it appears inline with current log state
    const insertAt = game?.actionLog?.length || 0;
    const result = await fetchCoaching();
    if (result) {
      const advice = result.aiCoachAdvice || result.advice;
      setCoachLogEntries(prev => [...prev, {
        player: 'Coach',
        action: result.recommendedAction,
        amount: 0,
        message: advice,
        phase: game?.phase,
        _insertAt: insertAt,
      }]);
    }
  }, [fetchCoaching, game?.phase, game?.actionLog?.length]);

  const handleAction = async (action, amount) => {
    clearCoaching();
    await submitAction(action, amount);
  };

  const handleDealNextHand = async () => {
    clearCoaching();
    setCoachLogEntries([]);
    await dealNextHand();
  };

  // Merge server action log with client-side coach entries at their original positions
  const fullActionLog = useMemo(() => {
    if (!game?.actionLog) return [];
    if (coachLogEntries.length === 0) return game.actionLog;

    const result = [...game.actionLog];
    // Insert coach entries at their captured positions (offset for prior insertions)
    const sorted = [...coachLogEntries].sort((a, b) => a._insertAt - b._insertAt);
    sorted.forEach((entry, i) => {
      const pos = Math.min(entry._insertAt + i, result.length);
      result.splice(pos, 0, entry);
    });
    return result;
  }, [game?.actionLog, coachLogEntries]);

  if (loading && !game) {
    return (
      <Box sx={{ mt: 8, textAlign: 'center' }}>
        <CircularProgress size={28} sx={{ color: '#ffd700' }} />
        <Typography sx={{ mt: 1.5, color: '#666', fontSize: '0.8rem' }}>Loading game...</Typography>
      </Box>
    );
  }

  if (error && !game) {
    return (
      <Box sx={{ mt: 4, px: 2 }}>
        <Alert severity="error">{error}</Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ display: 'flex', height: 'calc(100vh - 42px)', overflow: 'hidden' }}>
      {/* Left column - Game Log */}
      <Box sx={{
        width: 260, minWidth: 220, p: 1,
        borderRight: '1px solid rgba(255,255,255,0.04)',
        overflow: 'hidden', display: 'flex', flexDirection: 'column',
      }}>
        <ActionLog actionLog={fullActionLog} />
      </Box>

      {/* Center column - Table */}
      <Box sx={{
        flex: 1, p: 1,
        display: 'flex', flexDirection: 'column', alignItems: 'center',
        justifyContent: 'center',
        overflow: 'hidden',
      }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
          <Typography sx={{
            fontSize: '0.65rem', color: '#444',
            fontWeight: 600, letterSpacing: '0.05em',
          }}>
            HAND #{game?.handNumber}
          </Typography>
          <Box sx={{
            px: 0.8, py: 0.15, borderRadius: '4px',
            bgcolor: game?.aiEnabled
              ? 'rgba(34,197,94,0.1)' : 'rgba(255,255,255,0.04)',
            border: game?.aiEnabled
              ? '1px solid rgba(34,197,94,0.25)' : '1px solid rgba(255,255,255,0.06)',
          }}>
            <Typography sx={{
              fontSize: '0.55rem', fontWeight: 600, letterSpacing: '0.05em',
              color: game?.aiEnabled ? '#22c55e' : '#444',
            }}>
              {game?.aiEnabled ? 'AI' : 'RANDOM'}
            </Typography>
          </Box>
        </Box>

        {error && <Alert severity="error" sx={{ mb: 1, width: '100%', maxWidth: 500 }}>{error}</Alert>}

        <GameTable
          game={game}
          onAction={handleAction}
          onDealNextHand={handleDealNextHand}
          disabled={loading}
        />
      </Box>

      {/* Right column - Coach */}
      <Box sx={{
        width: 260, minWidth: 220, p: 1,
        borderLeft: '1px solid rgba(255,255,255,0.04)',
        overflow: 'hidden', display: 'flex', flexDirection: 'column',
      }}>
        <CoachingPanel
          coaching={coaching}
          loading={coachingLoading}
          error={coachingError}
          onRequestCoaching={handleRequestCoaching}
        />
      </Box>
    </Box>
  );
}

export default GamePage;
