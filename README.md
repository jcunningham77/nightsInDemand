# Nights in Demand

Aggregates sports and concert schedules to identify high-demand nights in a given city — useful for resellers (hotels, rideshare, tickets, etc.).

## Live Environments

| | URL |
|---|---|
| **Frontend** | https://nightsindemand-frontend.vercel.app |
| **Backend API** | https://nights-in-demand-backend.up.railway.app |

### Example API calls
```
GET /api/health
GET /api/events/New%20York/highdemand?from=2026-06-01&to=2026-06-30
GET /api/events/New%20York/highdemand?on=2026-06-05
GET /api/events/New%20York?from=2026-06-01&to=2026-06-07
DELETE /api/cache/New%20York
```

## Stack

| Layer | Tech |
|---|---|
| Backend | Kotlin + Ktor + Exposed + SQLite |
| Frontend | Next.js 16 + Tailwind + shadcn/ui + Recharts |
| Backend hosting | Railway |
| Frontend hosting | Vercel |

## Data Sources

- **ESPN** (no key required) — NBA, NFL, MLB, NHL, MLS schedules
- **Ticketmaster** Discovery API — concerts and events

## Local Development

### Backend
```bash
cd backend
# Add your keys to .env (see .env.example)
./gradlew run
# Runs at http://localhost:8080
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# Runs at http://localhost:3000
```

## Demand Score

Each night is scored 1–10 based on significant events and attendance. Click the **ⓘ** button next to any demand badge in the UI for a full explanation.
