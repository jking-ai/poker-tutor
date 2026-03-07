# Production Deployment -- "The Nut" Poker Tutor

How to deploy and operate the application on Google Cloud Platform.

---

## GCP Resources

<!-- TODO: Fill in once infrastructure is provisioned -->

| Resource | Service | Details |
|----------|---------|---------|
| Backend API | Cloud Run | `poker-tutor` in `us-central1` |
| Frontend | Firebase Hosting | Static React SPA served via CDN |
| Real-Time Database | Firebase RTDB | Game state sync |
| LLM | Vertex AI | Gemini via Spring AI |
| Container Registry | Artifact Registry | Docker images for Cloud Run (built with Jib) |

### Required IAM Roles

<!-- TODO: Document service account and IAM configuration -->

- Cloud Run service account needs **Vertex AI User** role for Gemini access
- Cloud Run service account needs **Firebase Admin** role for RTDB writes

---

## Backend Deployment (Cloud Run)

<!-- TODO: Add deployment commands once infrastructure is configured -->

### Build and Push Container (Jib)

```bash
cd backend

# Build and push to Artifact Registry
./gradlew jib \
  -Djib.to.image=us-central1-docker.pkg.dev/<project>/poker-tutor/backend:latest

# Or build locally for testing
./gradlew jibDockerBuild
docker run -p 8080:8080 poker-tutor-backend
```

### Deploy to Cloud Run

```bash
gcloud run deploy poker-tutor \
  --image us-central1-docker.pkg.dev/<project>/poker-tutor/backend:latest \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars "GCP_PROJECT_ID=<project>,GCP_REGION=us-central1,FIREBASE_PROJECT_ID=<project>,SPRING_PROFILES_ACTIVE=prod"

# Verify deployment
curl https://poker-tutor-<hash>-uc.a.run.app/api/v1/health
```

---

## Frontend Deployment (Firebase Hosting)

<!-- TODO: Add Firebase hosting setup and deploy commands -->

```bash
cd frontend

# Build the production frontend
VITE_API_URL=https://poker-tutor-<hash>-uc.a.run.app npm run build

# Deploy to Firebase Hosting
firebase deploy --only hosting
```

### Firebase Configuration

<!-- TODO: Document firebase.json and hosting config -->
<!-- The frontend/firebase.json should configure dist/ as the public directory with SPA rewrite rules -->

---

## Firebase Realtime Database Setup

<!-- TODO: Document RTDB setup and security rules -->

```bash
# Deploy security rules
firebase deploy --only database
```

### Security Rules

<!-- TODO: Define RTDB security rules -->
<!-- Rules should allow:
  - Backend service account: read/write to games/{gameId}
  - Frontend (authenticated or public): read-only to games/{gameId}
  - No direct writes from the frontend
-->

---

## Verification

<!-- TODO: Add post-deployment verification checklist -->

1. **Backend health:** `curl https://<cloud-run-url>/api/v1/health` returns 200
2. **Start game:** POST to `/api/v1/games` returns game state with dealt cards
3. **Player action:** POST to `/api/v1/games/{id}/actions` processes action and returns opponent response
4. **Coaching:** GET `/api/v1/games/{id}/coaching` returns advice with odds
5. **Frontend:** Firebase Hosting URL loads the React app
6. **Real-time sync:** Game state updates appear in the frontend via Firebase RTDB
7. **End-to-end:** Play a complete hand through the UI

---

## Troubleshooting

<!-- TODO: Add production troubleshooting steps -->

| Issue | Solution |
|-------|----------|
| Cloud Run returns 500 | Check Cloud Run logs: `gcloud run services logs read poker-tutor` |
| CORS errors | Verify backend CORS config includes the Firebase Hosting domain |
| Vertex AI auth failure | Confirm service account has Vertex AI User role |
| Firebase RTDB write fails | Confirm service account has Firebase Admin role |
| Firebase deploy fails | Run `firebase use <project-id>` to set the active project |
| Opponent agent timeout | Check Vertex AI quotas and increase Cloud Run request timeout |
