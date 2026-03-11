import React, { useState, useCallback, useMemo } from 'react';
import { Box, Typography, CircularProgress, Alert, useMediaQuery } from '@mui/material';
import { useParams } from 'react-router-dom';
import { useGame } from '../hooks/useGame';
import { useCoaching } from '../hooks/useCoaching';
import GameTable from '../components/GameTable';
import CoachingPanel from '../components/CoachingPanel';
import ActionLog from '../components/ActionLog';

const MOBILE_TABS = [
  { key: 'table', label: 'Table' },
  { key: 'log', label: 'Log' },
  { key: 'coach', label: 'Coach' },
];

function GamePage() {
  const { id: gameId } = useParams();
  const { game, loading, error, submitAction, dealNextHand } = useGame(gameId);
  const { coaching, loading: coachingLoading, error: coachingError, fetchCoaching, clearCoaching } = useCoaching(gameId);
  const [coachLogEntries, setCoachLogEntries] = useState([]);
  const [mobileTab, setMobileTab] = useState('table');
  const isMobile = useMediaQuery('(max-width:767px)');

  const handleRequestCoaching = useCallback(async () => {
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

  const fullActionLog = useMemo(() => {
    if (!game?.actionLog) return [];
    if (coachLogEntries.length === 0) return game.actionLog;

    const result = [...game.actionLog];
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

  /* ---- Mobile layout: single column with tab switcher ---- */
  if (isMobile) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 42px)', overflow: 'hidden' }}>
        {/* Tab bar */}
        <Box sx={{
          display: 'flex',
          borderBottom: '1px solid rgba(255,255,255,0.06)',
          bgcolor: 'rgba(10,14,20,0.9)',
          flexShrink: 0,
        }}>
          {MOBILE_TABS.map(tab => (
            <Box
              key={tab.key}
              onClick={() => setMobileTab(tab.key)}
              sx={{
                flex: 1, py: 1, textAlign: 'center', cursor: 'pointer',
                borderBottom: mobileTab === tab.key
                  ? '2px solid #ffd700' : '2px solid transparent',
                color: mobileTab === tab.key ? '#ffd700' : '#666',
                fontSize: '0.72rem', fontWeight: 600,
                letterSpacing: '0.08em', textTransform: 'uppercase',
                transition: 'color 0.15s, border-color 0.15s',
                userSelect: 'none',
                WebkitTapHighlightColor: 'transparent',
              }}
            >
              {tab.label}
            </Box>
          ))}
        </Box>

        {/* Tab content */}
        <Box sx={{ flex: 1, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
          {mobileTab === 'table' && (
            <Box sx={{
              flex: 1, display: 'flex', flexDirection: 'column',
              alignItems: 'center', justifyContent: 'center',
              overflow: 'auto', p: 1,
            }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                <Typography sx={{
                  fontSize: '0.6rem', color: '#444',
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
                    fontSize: '0.5rem', fontWeight: 600, letterSpacing: '0.05em',
                    color: game?.aiEnabled ? '#22c55e' : '#444',
                  }}>
                    {game?.aiEnabled ? 'AI' : 'RANDOM'}
                  </Typography>
                </Box>
              </Box>
              {error && <Alert severity="error" sx={{ mb: 1, width: '100%' }}>{error}</Alert>}
              <GameTable
                game={game}
                onAction={handleAction}
                onDealNextHand={handleDealNextHand}
                disabled={loading}
                compact
              />
            </Box>
          )}

          {mobileTab === 'log' && (
            <Box sx={{ flex: 1, overflow: 'hidden', p: 1 }}>
              <ActionLog actionLog={fullActionLog} />
            </Box>
          )}

          {mobileTab === 'coach' && (
            <Box sx={{ flex: 1, overflow: 'hidden', p: 1 }}>
              <CoachingPanel
                coaching={coaching}
                loading={coachingLoading}
                error={coachingError}
                onRequestCoaching={handleRequestCoaching}
              />
            </Box>
          )}
        </Box>
      </Box>
    );
  }

  /* ---- Desktop layout: three columns ---- */
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
