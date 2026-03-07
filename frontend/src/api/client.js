const BASE_URL = import.meta.env.VITE_API_URL || '';

function getCsrfToken() {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/);
  return match ? decodeURIComponent(match[1]) : null;
}

async function request(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...options.headers };
  const csrfToken = getCsrfToken();
  if (csrfToken) {
    headers['X-XSRF-TOKEN'] = csrfToken;
  }
  const res = await fetch(`${BASE_URL}${path}`, {
    headers,
    ...options,
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({ error: { message: res.statusText } }));
    const err = new Error(body.error?.message || `HTTP ${res.status}`);
    err.status = res.status;
    err.code = body.error?.code;
    throw err;
  }
  return res.json();
}

export async function healthCheck() {
  return request('/api/v1/games/health');
}

export async function createGame(playerName, startingChips, smallBlind, bigBlind) {
  return request('/api/v1/games', {
    method: 'POST',
    body: JSON.stringify({ playerName, startingChips, smallBlind, bigBlind }),
  });
}

export async function getGame(gameId) {
  return request(`/api/v1/games/${gameId}`);
}

export async function submitAction(gameId, action, amount) {
  return request(`/api/v1/games/${gameId}/actions`, {
    method: 'POST',
    body: JSON.stringify({ action, amount: amount || null }),
  });
}

export async function getCoaching(gameId) {
  return request(`/api/v1/games/${gameId}/coaching`);
}

export async function getOdds(gameId) {
  return request(`/api/v1/games/${gameId}/odds`);
}

export async function getHistory(gameId) {
  return request(`/api/v1/games/${gameId}/history`);
}

export async function dealNextHand(gameId) {
  return request(`/api/v1/games/${gameId}/next-hand`, { method: 'POST' });
}
