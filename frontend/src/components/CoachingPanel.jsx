import React from 'react';
import { Box, Button, Typography, CircularProgress } from '@mui/material';
import SportsIcon from '@mui/icons-material/Sports';
import CasinoIcon from '@mui/icons-material/Casino';

const CONFIDENCE_COLORS = {
  HIGH: { bg: 'rgba(34,197,94,0.1)', border: 'rgba(34,197,94,0.3)', text: '#22c55e' },
  MEDIUM: { bg: 'rgba(245,158,11,0.1)', border: 'rgba(245,158,11,0.3)', text: '#f59e0b' },
  LOW: { bg: 'rgba(239,68,68,0.1)', border: 'rgba(239,68,68,0.3)', text: '#ef4444' },
};

function CoachingPanel({ coaching, loading, onRequestCoaching, error }) {
  const isLimited = error?.code === 'COACHING_LIMIT';
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
        display: 'flex', alignItems: 'center', gap: 0.6,
      }}>
        <SportsIcon sx={{ fontSize: 14, color: '#22c55e', opacity: 0.7 }} />
        <Typography sx={{
          fontSize: '0.65rem', fontWeight: 700,
          color: '#666', letterSpacing: '0.1em',
          textTransform: 'uppercase',
        }}>
          Coach
        </Typography>
      </Box>

      <Box sx={{ p: 1.5, flex: 1, overflowY: 'auto' }}>
        <Button
          onClick={onRequestCoaching}
          disabled={loading || isLimited}
          fullWidth
          size="small"
          startIcon={loading ? null : <CasinoIcon sx={{ fontSize: '16px !important' }} />}
          sx={{
            bgcolor: isLimited ? 'rgba(255,255,255,0.03)' : 'rgba(34,197,94,0.1)',
            color: isLimited ? '#555' : '#22c55e',
            border: isLimited ? '1px solid rgba(255,255,255,0.06)' : '1px solid rgba(34,197,94,0.25)',
            fontWeight: 600, fontSize: '0.75rem',
            py: 0.7, mb: 1.5,
            gap: 0.5,
            '&:hover': {
              bgcolor: isLimited ? 'rgba(255,255,255,0.03)' : 'rgba(34,197,94,0.18)',
              border: isLimited ? '1px solid rgba(255,255,255,0.06)' : '1px solid rgba(34,197,94,0.4)',
              boxShadow: isLimited ? 'none' : '0 0 12px rgba(34,197,94,0.15)',
            },
          }}
        >
          {loading ? <CircularProgress size={16} sx={{ color: '#22c55e' }} /> : isLimited ? 'Limit Reached' : 'Ask Coach'}
        </Button>

        {isLimited && (
          <Typography sx={{ fontSize: '0.65rem', color: '#f59e0b', mb: 1, textAlign: 'center' }}>
            {error.message}
          </Typography>
        )}

        {coaching && (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
            {/* Recommendation */}
            <Box sx={{ display: 'flex', gap: 0.8, alignItems: 'center' }}>
              <Box sx={{
                px: 1, py: 0.2, borderRadius: '4px',
                bgcolor: 'rgba(255,215,0,0.12)',
                border: '1px solid rgba(255,215,0,0.3)',
              }}>
                <Typography sx={{ fontSize: '0.7rem', fontWeight: 700, color: '#ffd700' }}>
                  {coaching.recommendedAction}
                </Typography>
              </Box>
              {coaching.confidence && (() => {
                const c = CONFIDENCE_COLORS[coaching.confidence] || CONFIDENCE_COLORS.MEDIUM;
                return (
                  <Box sx={{
                    px: 0.8, py: 0.15, borderRadius: '4px',
                    bgcolor: c.bg, border: `1px solid ${c.border}`,
                  }}>
                    <Typography sx={{ fontSize: '0.6rem', fontWeight: 600, color: c.text }}>
                      {coaching.confidence}
                    </Typography>
                  </Box>
                );
              })()}
            </Box>

            {/* Advice */}
            <Typography sx={{ fontSize: '0.75rem', color: '#9ca3af', lineHeight: 1.5 }}>
              {coaching.advice}
            </Typography>

            {/* Stats */}
            {(coaching.handStrength || coaching.odds) && (
              <Box sx={{
                p: 1, borderRadius: '6px',
                bgcolor: 'rgba(0,0,0,0.2)',
                border: '1px solid rgba(255,255,255,0.04)',
                display: 'flex', flexDirection: 'column', gap: 0.5,
              }}>
                {coaching.handStrength && (
                  <Box>
                    <Typography sx={{ fontSize: '0.6rem', color: '#555', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                      Hand
                    </Typography>
                    <Typography sx={{ fontSize: '0.72rem', color: '#d1d5db' }}>
                      {coaching.handStrength.bestHand}
                      {coaching.handStrength.nutDistance >= 0 &&
                        <span style={{ color: '#f59e0b' }}>{` (${coaching.handStrength.nutDistance} from nut)`}</span>}
                    </Typography>
                  </Box>
                )}
                {coaching.odds && (
                  <Box>
                    <Typography sx={{ fontSize: '0.6rem', color: '#555', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                      Math
                    </Typography>
                    <Typography sx={{
                      fontSize: '0.72rem', color: '#d1d5db',
                      fontFamily: '"JetBrains Mono", monospace',
                    }}>
                      {(coaching.odds.handEquity * 100).toFixed(0)}% equity
                      {coaching.odds.potOddsRatio !== 'N/A' && ` | ${coaching.odds.potOddsRatio} pot`}
                      {coaching.odds.outs > 0 && ` | ${coaching.odds.outs} outs`}
                    </Typography>
                  </Box>
                )}
              </Box>
            )}

            {coaching.explanation && (
              <Typography sx={{ fontSize: '0.7rem', color: '#6b7280', lineHeight: 1.5, fontStyle: 'italic' }}>
                {coaching.explanation.mathSummary}
              </Typography>
            )}

            {/* AI Coach personal advice */}
            {coaching.aiCoachAdvice && (
              <Box sx={{
                p: 1, borderRadius: '6px',
                bgcolor: 'rgba(99,102,241,0.06)',
                border: '1px solid rgba(99,102,241,0.15)',
              }}>
                <Typography sx={{
                  fontSize: '0.6rem', color: 'rgba(129,140,248,0.6)', fontWeight: 600,
                  textTransform: 'uppercase', letterSpacing: '0.05em', mb: 0.3,
                }}>
                  Coach Says
                </Typography>
                <Typography sx={{
                  fontSize: '0.72rem', color: '#c4b5fd', lineHeight: 1.6,
                  fontStyle: 'italic',
                }}>
                  &ldquo;{coaching.aiCoachAdvice}&rdquo;
                </Typography>
              </Box>
            )}
          </Box>
        )}
      </Box>
    </Box>
  );
}

export default CoachingPanel;
