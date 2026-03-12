# RemVault 🎲

> A D&D 5e companion tool for players and dungeon masters.  
> Named after Rem from Re:Zero — organized, reliable, and always ready.

## Features

**For Players**
- Character creation with automated 4d6-drop-lowest stat rolling
- Full character sheet — ability scores, modifiers, skills, saving throws
- HP tracking, XP & automatic level-up, carrying capacity
- Spell slot management, inventory

**For Dungeon Masters**
- Campaign creation and player management
- Session lifecycle (Planned → Ongoing → Finished)
- Session notes per session
- NPC and loot generation *(coming soon)*
- AI-assisted session summaries and homebrew review *(coming soon)*

**Shared**
- Real-time dice rolls visible to all session participants (WebSocket)
- Canon 5e SRD content import *(Phase 2)*
- Structured homebrew templates with balance review *(Phase 2)*

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Kotlin · Ktor · Netty |
| Shared logic | Kotlin module (RulesEngine, domain models) |
| Frontend (temp) | React · Vite · Plain CSS |
| Frontend (final) | Compose Multiplatform *(planned)* |
| Database | In-memory (Phase 1) · PostgreSQL + Exposed *(Phase 2)* |
| Auth | JWT |
| Real-time | WebSockets |
| LLM | OpenAI API *(Phase 3+)* |

---

## Project Structure
```
remvault/
├── shared/                  # Domain models, enums, RulesEngine
│   └── src/main/kotlin/dev/remvault/shared/
│       ├── models/          # User, Character, Campaign, Session, Roll…
│       ├── enums/           # UserRole, CharacterStatus, Origin…
│       └── rules/           # RulesEngine — pure 5e logic
├── server/                  # Ktor backend
│   └── src/main/kotlin/dev/remvault/
│       ├── plugins/         # Serialization, Auth, WebSockets, Routing…
│       ├── routing/         # AuthRoutes, CharacterRoutes, CampaignRoutes
│       ├── services/        # AuthService, CharacterService, CampaignService, DiceService
│       └── dto/             # Request/response data classes
└── web/                     # Temporary React frontend
    └── src/
        ├── api/             # API client (all fetch calls)
        ├── components/      # Navbar
        ├── pages/           # Login, Register, Dashboard, CharacterSheet, Campaign
        └── store/           # AuthContext (token + user state)
```

---

## Getting Started

### Prerequisites
- JDK 21
- Node.js 22+
- Git

### Run the backend
```bash
cd remvault
./gradlew run
# Server starts at http://localhost:8080
```

### Run the frontend
```bash
cd remvault/web
npm install
npm run dev
# Opens at http://localhost:5173
```

> Start Ktor before Vite — the dev proxy forwards `/api` requests to `localhost:8080`.

### Health check
```bash
curl http://localhost:8080/health
# {"status":"ok","version":"0.1.0"}
```

---

## API Overview

### Auth
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/auth/register` | — | Register a new account |
| POST | `/api/v1/auth/login` | — | Login, receive JWT |
| GET | `/api/v1/auth/me` | ✓ | Current user info |

### Characters
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/characters` | ✓ | Create character (rolls stats) |
| GET | `/api/v1/characters` | ✓ | List your characters |
| GET | `/api/v1/characters/{id}` | ✓ | Get full character sheet |
| PATCH | `/api/v1/characters/{id}` | ✓ | Update character |
| PATCH | `/api/v1/characters/{id}/hp` | ✓ | Update hit points |
| DELETE | `/api/v1/characters/{id}` | ✓ | Delete character |

### Campaigns
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/campaigns` | ✓ | Create campaign (DM only) |
| GET | `/api/v1/campaigns` | ✓ | List your campaigns |
| GET | `/api/v1/campaigns/{id}` | ✓ | Get campaign details |
| POST | `/api/v1/campaigns/{id}/archive` | ✓ | Archive campaign (DM only) |
| GET | `/api/v1/campaigns/{id}/members` | ✓ | List members |
| POST | `/api/v1/campaigns/{id}/members` | ✓ | Add member (DM only) |
| DELETE | `/api/v1/campaigns/{id}/members/{userId}` | ✓ | Remove member |
| GET | `/api/v1/campaigns/{id}/sessions` | ✓ | List sessions |
| POST | `/api/v1/campaigns/{id}/sessions` | ✓ | Create session (DM only) |
| GET | `/api/v1/campaigns/{id}/sessions/{sid}` | ✓ | Get session |
| PATCH | `/api/v1/campaigns/{id}/sessions/{sid}/status` | ✓ | Update session status |
| PATCH | `/api/v1/campaigns/{id}/sessions/{sid}/notes` | ✓ | Update session notes |

---

## Development Phases

| Phase | Status | Description |
|-------|--------|-------------|
| 1 — Monolith + In-memory | ✅ Current | Working backend + temp React UI |
| 2 — Monolith + PostgreSQL | 🔜 Next | Persistence, full auth, SRD import |
| 3 — Microservices + REST | ⏳ Planned | Split into independent services |
| 4 — Docker | ⏳ Planned | Containerized, one-command startup |
| 5 — Kubernetes | ⏳ Planned | Cluster deployment, autoscaling |

---

## License

Personal project — named after Rem (Re:Zero) as a symbol of reliability and care.  
Not intended for public distribution.