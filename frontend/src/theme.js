import { createTheme } from '@mui/material/styles';

/**
 * MUI dark theme with poker table aesthetic.
 * Green felt, dark background, gold accents, card-friendly typography.
 */
const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#4caf50', // Poker table green
    },
    secondary: {
      main: '#ffd700', // Gold accent
    },
    background: {
      default: '#1a1a2e', // Deep dark blue-black
      paper: '#16213e',   // Slightly lighter for cards/panels
    },
    // TODO: Add custom colors for:
    //   - card suits (red for hearts/diamonds, white for clubs/spades)
    //   - chip denominations
    //   - action buttons (green for check/call, red for fold, gold for raise)
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    // TODO: Configure typography scale for card labels, chip counts, etc.
  },
  components: {
    // TODO: Override MUI component defaults for poker-themed styling
    //   - MuiButton: rounded, poker-chip style
    //   - MuiCard: subtle border, felt-like background
    //   - MuiChip: chip-count display styling
  },
});

export default theme;
