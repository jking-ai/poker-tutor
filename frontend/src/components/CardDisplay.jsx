import React from 'react';
import { Box, Typography } from '@mui/material';

const SUIT_SYMBOLS = {
  HEARTS: '\u2665',
  DIAMONDS: '\u2666',
  CLUBS: '\u2663',
  SPADES: '\u2660',
};

const RANK_DISPLAY = {
  TWO: '2', THREE: '3', FOUR: '4', FIVE: '5', SIX: '6',
  SEVEN: '7', EIGHT: '8', NINE: '9', TEN: '10',
  JACK: 'J', QUEEN: 'Q', KING: 'K', ACE: 'A',
};

const SIZES = {
  normal:  { w: 52, h: 74, rank: 15, suit: 13, center: 22 },
  small:   { w: 44, h: 64, rank: 13, suit: 11, center: 18 },
  compact: { w: 36, h: 52, rank: 11, suit: 9,  center: 15 },
};

function CardDisplay({ card, faceDown = false, size = 'normal' }) {
  const s = SIZES[size] || SIZES.normal;
  const w = s.w;
  const h = s.h;
  const rankSize = s.rank;
  const suitSize = s.suit;
  const centerSize = s.center;
  const borderR = size === 'compact' ? '4px' : '6px';

  if (faceDown || !card) {
    return (
      <Box className="card-deal" sx={{
        width: w, height: h, borderRadius: borderR,
        background: 'linear-gradient(145deg, #1a3a5c, #0f2440)',
        border: '1.5px solid rgba(93,173,226,0.3)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        boxShadow: '0 2px 8px rgba(0,0,0,0.4)',
        position: 'relative', overflow: 'hidden',
      }}>
        <Box sx={{
          position: 'absolute', inset: size === 'compact' ? 3 : 4, borderRadius: size === 'compact' ? '3px' : '4px',
          border: '1px solid rgba(93,173,226,0.15)',
        }} />
        <Typography sx={{ fontSize: centerSize - 2, color: 'rgba(93,173,226,0.4)', fontWeight: 700 }}>?</Typography>
      </Box>
    );
  }

  const isRed = card.suit === 'HEARTS' || card.suit === 'DIAMONDS';
  const color = isRed ? '#dc2626' : '#1e293b';
  const rank = RANK_DISPLAY[card.rank] || card.rank;
  const suit = SUIT_SYMBOLS[card.suit] || card.suit;

  return (
    <Box className="card-deal" sx={{
      width: w, height: h, borderRadius: borderR,
      background: 'linear-gradient(170deg, #ffffff 0%, #f1f0eb 100%)',
      boxShadow: '0 2px 8px rgba(0,0,0,0.35), inset 0 1px 0 rgba(255,255,255,0.8)',
      position: 'relative', overflow: 'hidden', cursor: 'default',
      transition: 'transform 0.15s',
      '&:hover': { transform: 'translateY(-2px)' },
    }}>
      {/* Top-left pip */}
      <Box sx={{ position: 'absolute', top: size === 'compact' ? 2 : 3, left: size === 'compact' ? 3 : 4, lineHeight: 1, textAlign: 'center' }}>
        <Typography sx={{ fontSize: rankSize, fontWeight: 700, color, lineHeight: 1, fontFamily: '"Inter"' }}>
          {rank}
        </Typography>
        <Typography sx={{ fontSize: suitSize, color, lineHeight: 1, mt: '-1px' }}>
          {suit}
        </Typography>
      </Box>
      {/* Center suit */}
      <Box sx={{
        position: 'absolute', top: '50%', left: '50%',
        transform: 'translate(-50%, -50%)',
      }}>
        <Typography sx={{ fontSize: centerSize, color, lineHeight: 1, opacity: 0.85 }}>
          {suit}
        </Typography>
      </Box>
      {/* Bottom-right pip */}
      <Box sx={{
        position: 'absolute', bottom: size === 'compact' ? 2 : 3, right: size === 'compact' ? 3 : 4,
        lineHeight: 1, textAlign: 'center',
        transform: 'rotate(180deg)',
      }}>
        <Typography sx={{ fontSize: rankSize, fontWeight: 700, color, lineHeight: 1, fontFamily: '"Inter"' }}>
          {rank}
        </Typography>
        <Typography sx={{ fontSize: suitSize, color, lineHeight: 1, mt: '-1px' }}>
          {suit}
        </Typography>
      </Box>
    </Box>
  );
}

export default CardDisplay;
