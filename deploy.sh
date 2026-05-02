#!/usr/bin/env bash
set -euo pipefail

# ── Configuration ──────────────────────────────────────────────
# Source .env if present (contains deployment-specific defaults)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ -f "$SCRIPT_DIR/.env" ]; then
  # shellcheck disable=SC1091
  source "$SCRIPT_DIR/.env"
fi

REMOTE_HOST="${REMOTE_HOST:?Set REMOTE_HOST in .env or environment (e.g. user@hostname)}"
REMOTE_APP_DIR="${REMOTE_APP_DIR:-/home/${REMOTE_HOST%%@*}/apps/poker-tutor}"
IMAGE_NAME="poker-tutor"
IMAGE_TAG="${IMAGE_TAG:-latest}"
HOST_PORT="${HOST_PORT:-8080}"
CONTAINER_NAME="poker-tutor"
GCP_CREDS_LOCAL="${GCP_CREDENTIALS_PATH:?Set GCP_CREDENTIALS_PATH in .env or environment}"

# AI defaults (override with env vars)
APP_AI_ENABLED="${APP_AI_ENABLED:-true}"
GCP_PROJECT_ID="${GCP_PROJECT_ID:?Set GCP_PROJECT_ID in .env or environment}"
GCP_REGION="${GCP_REGION:-us-central1}"
GEMINI_LOCATION="${GEMINI_LOCATION:-global}"
GEMINI_API_ENDPOINT="${GEMINI_API_ENDPOINT:-aiplatform.googleapis.com}"
AI_MODEL="${AI_MODEL:-gemini-3.1-flash-lite-preview}"
MAX_AI_CALLS_PER_GAME="${MAX_AI_CALLS_PER_GAME:-200}"
MAX_CONCURRENT_GAMES="${MAX_CONCURRENT_GAMES:-15}"
MAX_COACHING_PER_HAND="${MAX_COACHING_PER_HAND:-3}"

# ── Sync source to server ─────────────────────────────────────
echo "==> Syncing source to ${REMOTE_HOST}:${REMOTE_APP_DIR}..."
ssh "$REMOTE_HOST" "mkdir -p ${REMOTE_APP_DIR}"
rsync -az --delete \
  --exclude '.git' \
  --exclude 'backend/build' \
  --exclude 'backend/bin' \
  --exclude 'backend/.gradle' \
  --exclude 'frontend/node_modules' \
  --exclude 'frontend/dist' \
  --exclude '*.md' \
  "$SCRIPT_DIR/" "${REMOTE_HOST}:${REMOTE_APP_DIR}/"

# ── Copy GCP credentials ──────────────────────────────────────
echo "==> Copying GCP credentials..."
scp -q "$GCP_CREDS_LOCAL" "${REMOTE_HOST}:${REMOTE_APP_DIR}/gcp-credentials.json"
ssh "$REMOTE_HOST" "chmod 600 ${REMOTE_APP_DIR}/gcp-credentials.json"

# ── Build and deploy on server ─────────────────────────────────
echo "==> Building image and deploying on server..."
ssh "$REMOTE_HOST" bash <<EOF
  set -euo pipefail
  cd ${REMOTE_APP_DIR}

  echo "--- Building Docker image ---"
  docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .

  echo "--- Stopping existing container ---"
  docker rm -f ${CONTAINER_NAME} 2>/dev/null || true

  echo "--- Starting container ---"
  docker run -d \
    --name ${CONTAINER_NAME} \
    --restart unless-stopped \
    -p ${HOST_PORT}:8080 \
    -v ${REMOTE_APP_DIR}/gcp-credentials.json:/gcp/credentials.json:ro \
    -e GOOGLE_APPLICATION_CREDENTIALS=/gcp/credentials.json \
    -e APP_AI_ENABLED=${APP_AI_ENABLED} \
    -e GCP_PROJECT_ID=${GCP_PROJECT_ID} \
    -e GCP_REGION=${GCP_REGION} \
    -e GEMINI_LOCATION=${GEMINI_LOCATION} \
    -e GEMINI_API_ENDPOINT=${GEMINI_API_ENDPOINT} \
    -e AI_MODEL=${AI_MODEL} \
    -e CORS_ALLOWED_ORIGINS= \
    -e MAX_AI_CALLS_PER_GAME=${MAX_AI_CALLS_PER_GAME} \
    -e MAX_CONCURRENT_GAMES=${MAX_CONCURRENT_GAMES} \
    -e MAX_COACHING_PER_HAND=${MAX_COACHING_PER_HAND} \
    ${IMAGE_NAME}:${IMAGE_TAG}
EOF

# ── Health check ───────────────────────────────────────────────
echo "==> Waiting for health check..."
for i in $(seq 1 30); do
  if ssh "$REMOTE_HOST" "curl -sf http://localhost:${HOST_PORT}/api/v1/games/health" >/dev/null 2>&1; then
    echo "==> Healthy!"
    ssh "$REMOTE_HOST" "curl -s http://localhost:${HOST_PORT}/api/v1/games/health"
    echo ""
    DEPLOY_IP="${DEPLOY_IP:-$(echo "$REMOTE_HOST" | cut -d@ -f2)}"
    echo "==> Deployed at http://${DEPLOY_IP}:${HOST_PORT}"
    exit 0
  fi
  sleep 2
done

echo "==> WARNING: Health check did not pass within 60s. Check logs:"
echo "    ssh ${REMOTE_HOST} docker logs ${CONTAINER_NAME}"
exit 1
