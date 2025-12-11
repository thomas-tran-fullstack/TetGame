Render deployment notes

1) Preconditions
- Create a Render account and connect the GitHub/GitLab repo for this project.

2) Using the provided `render.yaml`
- The repository contains `render.yaml` which defines two web services (`tetgame-backend`, `tetgame-frontend`), a managed Postgres (`tetgame-db`) and a Redis addon (`tetgame-redis`).
- Push this branch to `main` and in Render choose "Create from Render.yaml" or link the repo and use the YAML manifest.

3) Environment variables
- Render will create the Postgres and Redis instances; the `render.yaml` references them. You should ensure secrets are set in the Render dashboard for any OAuth keys (Google/Facebook), JWT secrets, and any other external integration.

4) Backend build
- The backend uses Maven wrapper (`mvnw`). The Dockerfile in `backend/` builds the Spring Boot jar.

5) Frontend build
- The frontend uses Vite. The Dockerfile in `frontend/` builds and runs `vite preview`.

6) Post-deploy checks
- Visit the frontend service URL and login. Ensure WebSocket endpoint URL (VITE_WS_URL) is set to the backend service URL + `/ws`.

7) CI / Auto-deploy via GitHub Actions
- The repo contains `.github/workflows/ci-deploy.yml` which builds backend and frontend and then triggers deploys on Render via API.
- To enable: add these GitHub Secrets in repository settings:
	- `RENDER_API_KEY` — a Render API key with deploy permission
	- `RENDER_BACKEND_SERVICE_ID` — the Render service id for `tetgame-backend`
	- `RENDER_FRONTEND_SERVICE_ID` — the Render service id for `tetgame-frontend`

8) Local development quick run
- Backend (requires Postgres and Redis running locally). Example using Docker Compose (not included here): set env vars then:

```bash
# backend
cd backend
./mvnw spring-boot:run

# frontend
cd ../frontend
npm ci
npm run dev
```

Notes
- For WebRTC testing locally, use HTTPS (e.g., `ngrok` or `localhost` with proper cert) and set a STUN/TURN server in environment for better connectivity.

Notes
- Render may require you to supply explicit environment variables like `SPRING_DATASOURCE_USERNAME`/`PASSWORD` — check the managed DB creation output.
- For production WebRTC, add a TURN server and set `RELAY` config.
