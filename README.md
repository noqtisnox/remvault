# RemVault 🎲

> A D&D 5e companion tool for players and dungeon masters.  
> Named after Rem from Re:Zero — organized, reliable, and always ready.

## 🚀 Features

**For Players**
- Character creation with automated 4d6-drop-lowest stat rolling.
- Full character sheet — ability scores, modifiers, skills, saving throws.
- HP tracking, XP & automatic level-up, carrying capacity.
- Spell slot management and inventory.

**For Dungeon Masters**
- Campaign creation and player management.
- Session lifecycle (Planned → Ongoing → Finished).
- Real-time dice rolls visible to all session participants (WebSocket).
- **AI-Assisted (Beta):** Session summaries and homebrew review.

---

## 💻 Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Kotlin · Ktor · Netty |
| **Frontend** | React · Vite · Tailwind CSS |
| **Database** | PostgreSQL + Exposed Framework |
| **Cache** | Redis (Jedis client) |
| **Orchestration** | Docker Compose & Kubernetes (Minikube) |
| **AI Layer** | Python FastAPI + Local LLM (Llama/Mistral) |

---

## 🛠 Infrastructure & Deployment

RemVault is architected for high availability and containerized scaling.

### Option A: Docker Compose (Quick Start)
Best for local feature development.
```bash
# From the project root
docker-compose up --build
```

- Ktor Core: http://localhost:8080
- Postgres: localhost:5433 (mapped to avoid 5432 host conflicts)
- Redis: localhost:6379
- AI Service: http://localhost:8000

### Option B: Kubernetes (Minikube / Production)

Best for testing cluster orchestration and self-healing.

1. Prepare the Environment:

    ```bash
    minikube start --driver=docker
    eval $(minikube docker-env)
    ```

2. Build & Deploy

    ```bash
    # Build internal images
    docker build -t remvault-core-image:latest .
    cd remvault-ai && docker build -t remvault-ai-image:latest . && cd ..

    # Apply manifests
    kubectl apply -f k8s/
    ```

3. Configure Service & Tunnel

    ```bash
    # Register the Redis service in the cluster directory
    kubectl expose deployment remvault-cache --port=6379 --target-port=6379 --name=remvault-cache

    # Open a bridge to the Ktor API (Run in a separate tab)
    kubectl port-forward --address 0.0.0.0 service/remvault-core 8080:8080
    ```

## 📝 Development Notes (Fedora/Linux)

- **DNS Caching:** If Ktor fails to connect to Redis (UnknownHostException), the JVM may have cached a negative DNS result during startup. Restart the pod to clear it:

    ```bash
    kubectl rollout restart deployment remvault-core
    ```

- **Socket Errors:** If Vite throws "Failed to create socket," ensure no "zombie" processes are hogging port 8080:
    ```bash
    sudo lsof -i :8080 followed by kill -9 <PID>
    ```

- **Database Volumes:** PostgreSQL data is persisted via a named volume pgdata. Running docker-compose down will not delete your characters.

## 🗺 Roadmap

|  Phase | Status  | Description  |
|---|---|---|
|  1 — Monolith + In-memory | ✅ Done  | Initial logic and temp UI.  |
|2/3 — Persistence| ✅ Done  | PostgreSQL + Redis integration.  |
| 4 — Dockerization  | ✅ Done  | Multi-stage builds and Compose.  |
| 5 — Kubernetes  | ✅ Done  |  Deployment manifests & Orchestration. |
| 6 — Mobile App  | ⏳ Planned  | Expo / React Native integration.  |

## 📜 License

Personal project — named after Rem (Re:Zero) as a symbol of reliability and care.
Not intended for public distribution.