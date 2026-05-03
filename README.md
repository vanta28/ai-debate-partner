# ⚖️ AI Debate Partner — Production

Full-stack Spring Boot app with JWT auth, PostgreSQL persistence, rate limiting, and one-click Railway deploy.

## What's included
- **JWT Auth** — register/login, BCrypt passwords, stateless tokens
- **PostgreSQL** — sessions and users persisted across restarts
- **Rate Limiting** — 20 requests/min per user via Bucket4j
- **Groq AI** — llama-3.3-70b-versatile as the debate opponent
- **Docker** — containerized, ready to deploy anywhere

---

## Deploy to Railway (free)

### 1. Push to GitHub
```bash
git init
git add .
git commit -m "initial commit"
git remote add origin https://github.com/YOUR_USERNAME/ai-debate-partner.git
git push -u origin main
```

### 2. Create Railway project
1. Go to [railway.app](https://railway.app) → **New Project**
2. Click **Deploy from GitHub repo** → select your repo
3. Railway auto-detects the Dockerfile and builds it

### 3. Add PostgreSQL
1. In your Railway project → click **+ New** → **Database** → **PostgreSQL**
2. Railway automatically injects `DATABASE_URL` into your app ✅

### 4. Set environment variables
In Railway → your service → **Variables**, add:

| Variable | Value |
|----------|-------|
| `GROQ_API_KEY` | `gsk_your_key_here` |
| `JWT_SECRET` | Any long random string (min 32 chars) e.g. `myS3cr3tK3yTh4tIsV3ryL0ngAndS3cur3!!` |

`DATABASE_URL` and `PORT` are injected by Railway automatically.

### 5. Deploy
Railway deploys automatically on every push to main. Your app will be live at:
```
https://your-app-name.up.railway.app
```

---

## Run locally

You'll need PostgreSQL running locally. Easiest way:
```bash
docker run -d \
  --name debate-db \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=debate \
  -p 5432:5432 \
  postgres:15
```

Then set env vars and run:
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/debate?user=postgres&password=postgres
export GROQ_API_KEY=gsk_your_key
export JWT_SECRET=myS3cr3tK3yTh4tIsV3ryL0ngAndS3cur3!!
mvn spring-boot:run
```

Open → **http://localhost:8080**

---

## API Reference

### Auth (public)
```
POST /api/auth/register   { email, password, username }
POST /api/auth/login      { email, password }
```
Both return: `{ token, username, email }`

### Debate (requires Authorization: Bearer <token>)
```
POST   /api/debate/start           { topic, userSide }
POST   /api/debate/message         { sessionId, message }
GET    /api/debate/sessions        → list of past debates
DELETE /api/debate/session/{id}    → delete a session
```

---

## Project structure
```
src/main/java/com/debate/
├── DebateApplication.java
├── config/
│   └── SecurityConfig.java         # Spring Security + CORS
├── controller/
│   ├── AuthController.java         # register + login
│   └── DebateController.java       # debate endpoints
├── filter/
│   ├── JwtAuthFilter.java          # validates JWT on every request
│   └── RateLimitFilter.java        # Bucket4j rate limiter
├── model/
│   ├── entity/
│   │   ├── User.java               # JPA entity
│   │   ├── DebateSession.java      # JPA entity
│   │   └── ChatMessage.java        # POJO (stored as JSON in DB)
│   └── dto/
│       └── Dtos.java               # all request/response DTOs
├── repository/
│   ├── UserRepository.java
│   └── DebateSessionRepository.java
├── security/
│   └── JwtUtil.java                # generate + validate tokens
└── service/
    ├── AuthService.java            # register + login logic
    └── DebateService.java          # Groq API + session logic
```
