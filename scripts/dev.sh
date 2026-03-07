#!/usr/bin/env bash
set -e

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
BACKEND_PID_FILE="$PROJECT_DIR/.backend.pid"
FRONTEND_PID_FILE="$PROJECT_DIR/.frontend.pid"

usage() {
  echo "Usage: ./dev.sh <command>"
  echo ""
  echo "Commands:"
  echo "  start           Start both backend and frontend (with AI)"
  echo "  start:noai      Start both without AI (random opponent)"
  echo "  stop            Stop both backend and frontend"
  echo "  start:backend   Start backend only with AI (port 8080)"
  echo "  start:frontend  Start frontend only (port 5173)"
  echo "  stop:backend    Stop backend only"
  echo "  stop:frontend   Stop frontend only"
  echo "  status          Show running status"
  echo "  test            Run backend tests"
  echo "  build           Build both backend and frontend"
  exit 1
}

GCP_CREDS="$HOME/.gcp/poker-tutor-sa.json"

start_backend() {
  if [ -f "$BACKEND_PID_FILE" ] && kill -0 "$(cat "$BACKEND_PID_FILE")" 2>/dev/null; then
    echo "Backend is already running (PID $(cat "$BACKEND_PID_FILE"))"
    return
  fi
  local ai_flag="${1:-true}"
  cd "$PROJECT_DIR/backend"
  if [ "$ai_flag" = "true" ] && [ -f "$GCP_CREDS" ]; then
    echo "Starting backend on port 8080 (AI enabled)..."
    GOOGLE_APPLICATION_CREDENTIALS="$GCP_CREDS" APP_AI_ENABLED=true ./gradlew bootRun &>/dev/null &
  else
    echo "Starting backend on port 8080 (random opponent)..."
    ./gradlew bootRun &>/dev/null &
  fi
  echo $! > "$BACKEND_PID_FILE"
  echo "Backend started (PID $!)"
}

stop_backend() {
  if [ -f "$BACKEND_PID_FILE" ]; then
    PID=$(cat "$BACKEND_PID_FILE")
    if kill -0 "$PID" 2>/dev/null; then
      echo "Stopping backend (PID $PID)..."
      kill -- -"$(ps -o pgid= -p "$PID" | tr -d ' ')" 2>/dev/null || kill "$PID" 2>/dev/null
      echo "Backend stopped"
    else
      echo "Backend is not running"
    fi
    rm -f "$BACKEND_PID_FILE"
  else
    echo "Backend is not running"
  fi
}

start_frontend() {
  if [ -f "$FRONTEND_PID_FILE" ] && kill -0 "$(cat "$FRONTEND_PID_FILE")" 2>/dev/null; then
    echo "Frontend is already running (PID $(cat "$FRONTEND_PID_FILE"))"
    return
  fi
  echo "Starting frontend on port 5173..."
  cd "$PROJECT_DIR/frontend"
  npm run dev &>/dev/null &
  echo $! > "$FRONTEND_PID_FILE"
  echo "Frontend started (PID $!)"
}

stop_frontend() {
  if [ -f "$FRONTEND_PID_FILE" ]; then
    PID=$(cat "$FRONTEND_PID_FILE")
    if kill -0 "$PID" 2>/dev/null; then
      echo "Stopping frontend (PID $PID)..."
      kill -- -"$(ps -o pgid= -p "$PID" | tr -d ' ')" 2>/dev/null || kill "$PID" 2>/dev/null
      echo "Frontend stopped"
    else
      echo "Frontend is not running"
    fi
    rm -f "$FRONTEND_PID_FILE"
  else
    echo "Frontend is not running"
  fi
}

show_status() {
  if [ -f "$BACKEND_PID_FILE" ] && kill -0 "$(cat "$BACKEND_PID_FILE")" 2>/dev/null; then
    echo "Backend:  RUNNING (PID $(cat "$BACKEND_PID_FILE")) - http://localhost:8080"
  else
    echo "Backend:  STOPPED"
    rm -f "$BACKEND_PID_FILE"
  fi
  if [ -f "$FRONTEND_PID_FILE" ] && kill -0 "$(cat "$FRONTEND_PID_FILE")" 2>/dev/null; then
    echo "Frontend: RUNNING (PID $(cat "$FRONTEND_PID_FILE")) - http://localhost:5173"
  else
    echo "Frontend: STOPPED"
    rm -f "$FRONTEND_PID_FILE"
  fi
}

run_tests() {
  echo "Running backend tests..."
  cd "$PROJECT_DIR/backend"
  ./gradlew test
}

run_build() {
  echo "Building backend..."
  cd "$PROJECT_DIR/backend"
  ./gradlew build
  echo ""
  echo "Building frontend..."
  cd "$PROJECT_DIR/frontend"
  npm run build
}

case "${1:-}" in
  start)          start_backend true; start_frontend ;;
  start:noai)     start_backend false; start_frontend ;;
  stop)           stop_backend; stop_frontend ;;
  start:backend)  start_backend true ;;
  start:frontend) start_frontend ;;
  stop:backend)   stop_backend ;;
  stop:frontend)  stop_frontend ;;
  status)         show_status ;;
  test)           run_tests ;;
  build)          run_build ;;
  *)              usage ;;
esac
