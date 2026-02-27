import React from 'react';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import theme from './theme';
import TopNav from './components/TopNav';
import LobbyPage from './pages/LobbyPage';
import GamePage from './pages/GamePage';
import HistoryPage from './pages/HistoryPage';

/**
 * Root application component.
 * Provides MUI dark theme, React Router, and top-level layout.
 */
function App() {
  // TODO: Add global state provider if needed (React Context or Zustand)

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <TopNav />
        <Routes>
          <Route path="/" element={<LobbyPage />} />
          <Route path="/game/:id" element={<GamePage />} />
          <Route path="/history/:id" element={<HistoryPage />} />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;
