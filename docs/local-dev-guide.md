# Local Development Guide

How to set up, run, and develop "The Nut" Poker Tutor on your local machine.

---

## Prerequisites

| Tool | Version | Check |
|------|---------|-------|
| Java | 21+ (LTS) | `java --version` |
| Gradle | 8.x (via wrapper) | `./gradlew --version` |
| Node.js | 20+ | `node --version` |
| Firebase CLI | 13+ | `firebase --version` |
| Google Cloud SDK | latest | `gcloud --version` |

---

## 1. Environment Setup

<!-- TODO: Add detailed environment setup steps once the project is scaffolded -->

```bash
# Clone the repository
git clone <repo-url>
cd poker-tutor

# Copy environment template
cp .env.example .env
# Fill in: GCP_PROJECT_ID, GCP_REGION, FIREBASE_PROJECT_ID
```

### GCP Credentials

<!-- TODO: Document credential setup for local Vertex AI and Firebase access -->
<!-- Options: application default credentials or service account key -->
<!-- Service account key goes to backend/src/main/resources/gcp-credentials.json (gitignored) -->

---

## 2. Start the Backend

<!-- TODO: Add backend startup instructions once Spring Boot app is created -->

```bash
cd backend
./gradlew bootRun
# Spring Boot starts on http://localhost:8080 with 'local' profile
```

### Verify Backend

```bash
curl http://localhost:8080/api/v1/health
```

---

## 3. Start the Frontend

<!-- TODO: Add frontend startup instructions once React app is scaffolded -->

```bash
cd frontend
npm install
npm run dev
# Vite dev server starts on http://localhost:5173
```

---

## 4. Quick Verification

<!-- TODO: Add end-to-end verification steps -->

1. Backend health check returns 200
2. Frontend loads in browser at `http://localhost:5173`
3. Start a new game and verify cards are dealt
4. Submit a player action and verify opponent responds
5. Request coaching advice and verify odds are displayed

---

## Troubleshooting

<!-- TODO: Add common issues and solutions -->

| Issue | Solution |
|-------|----------|
| `./gradlew: Permission denied` | Run `chmod +x ./gradlew` |
| Gradle build fails | Ensure Java 21 is installed and `JAVA_HOME` is set |
| CORS errors in browser | Verify backend CORS config includes `http://localhost:5173` |
| Vertex AI authentication failure | Check GCP credentials file at `backend/src/main/resources/gcp-credentials.json` |
| Firebase RTDB connection failure | Verify Firebase project ID and service account permissions |
| Port already in use | Kill the existing process or change the port in `application-local.yml` |
