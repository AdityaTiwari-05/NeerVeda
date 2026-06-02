# 💧 NeerVeda — Smart Water Safety & Disease Prevention System

> **Neer** = Water | **Veda** = Knowledge/Wisdom

**Team:** CORE_401 | **Problem ID:** SIH25001 | **Smart India Hackathon 2025**

---

## 🏆 Overview

NeerVeda is an enterprise-grade, AI-powered smart water safety and disease prevention platform built for rural India. It combines IoT sensor networks, machine learning, and real-time alerting to protect communities from waterborne diseases before they strike.

---

## 🏗️ Architecture

```
neerveda-frontend/          Next.js 15 + React 19 + TypeScript + Tailwind CSS
neerveda-backend/           Spring Boot 3.2 + Java 21 + Firebase Firestore
.github/workflows/          CI/CD GitHub Actions Pipeline
docker-compose.yml          Local Docker orchestration
```

### System Architecture
```
[ESP32 IoT Sensors] → POST /api/v1/water/reading
                          ↓
                    [Spring Boot Backend]
                    ┌────────────────────┐
                    │  Security Layer    │ ← JWT + RBAC + Rate Limiting
                    │  Controller Layer  │
                    │  Service Layer     │ ← Water Analysis + AI Prediction
                    │  Repository Layer  │ ← Firestore Repository
                    └────────────────────┘
                          ↓           ↓
                   [Firebase       [Twilio]
                   Firestore]      SMS Alerts
                          ↑
              [Next.js Dashboard]
```

---

## 🛠️ Technology Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Spring Boot | 3.2.5 | REST API Framework |
| Java | 21 | Language |
| Firebase Admin SDK | 9.2.0 | Firestore Database |
| Spring Security | 6.x | Auth + RBAC |
| JJWT | 0.12.5 | JWT Tokens |
| Bucket4j | 8.7.0 | Rate Limiting |
| Twilio | 9.14.0 | SMS Alerts |
| SpringDoc OpenAPI | 2.3.0 | Swagger UI |
| JUnit 5 + Mockito | — | Testing |

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| Next.js | 15 | React Framework + SSR |
| React | 19 | UI Library |
| TypeScript | 5.6 | Type Safety |
| Tailwind CSS | 3.4 | Utility-First Styling |
| Framer Motion | 11 | Animations |
| Recharts | 2.12 | Data Visualization |
| TanStack Query | 5 | Server State Management |
| Zustand | 5 | Client State Management |
| Leaflet | 1.9 | Map Integration |
| Axios | 1.7 | HTTP Client |
| Zod | 3.23 | Schema Validation |

---

## 🗄️ Firebase Collections

### `water_readings`
```
id, villageId, villageName, district, state
latitude, longitude
ph, tds, turbidity, temperature
status (SAFE/WARNING/DANGER)
alertParameter, alertMessage, deviceId, timestamp
```

### `symptom_reports`
```
id, reportedBy, reporterRole, reporterPhone
patientAge, patientGender
villageId, villageName, district, state, latitude, longitude
symptoms[], suspectedDisease, severity, affectedCount, waterSource
status (PENDING/REVIEWED/RESOLVED), timestamp, reviewNotes
```

### `alerts`
```
id, alertType (WATER_QUALITY/DISEASE_OUTBREAK/SYSTEM)
severity (LOW/MEDIUM/HIGH/CRITICAL)
villageId, villageName, district, state, latitude, longitude
title, message, affectedParameter, status (ACTIVE/ACKNOWLEDGED/RESOLVED)
createdAt, resolvedAt, resolvedBy
```

### `users`
```
id, name, email, passwordHash, role, active
phone, district, state, createdAt, lastLogin
```

### `audit_logs`
```
id, userId, userEmail, eventType, description
ipAddress, userAgent, timestamp, success
```

---

## 👥 User Roles & Permissions

| Role | Submit Readings | View Alerts | Submit Reports | Admin Panel |
|---|:---:|:---:|:---:|:---:|
| ADMIN | ✅ | ✅ | ✅ | ✅ |
| GOVERNMENT_OFFICER | ❌ | ✅ | ❌ | ❌ |
| HEALTH_WORKER | ❌ | ✅ | ✅ | ❌ |
| WATER_INSPECTOR | ✅ | ✅ | ❌ | ❌ |
| PUBLIC_VIEWER | ❌ | ❌ | ❌ | ❌ |

---

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven 3.9+
- Node.js 20+
- Firebase project with Firestore enabled
- Docker (optional)

### 1. Clone & Configure Firebase

```bash
git clone https://github.com/your-org/NeerVeda.git
cd NeerVeda
```

Download your Firebase service account JSON and place it at:
```
neerveda-backend/src/main/resources/firebase-service-account.json
```

### 2. Backend Setup

```bash
cd neerveda-backend
# Update application.properties or set env variables:
# FIREBASE_PROJECT_ID=your-project-id
# JWT_SECRET=your-256-bit-secret

mvn spring-boot:run
```

Backend starts at: `http://localhost:8080`
Swagger UI: `http://localhost:8080/swagger-ui`

### 3. Frontend Setup

```bash
cd neerveda-frontend
cp .env.local .env.local   # already configured for localhost:8080
npm install
npm run dev
```

Frontend starts at: `http://localhost:3000`

### 4. Docker (Full Stack)

```bash
# Set required environment variables
export FIREBASE_PROJECT_ID=your-project-id
export JWT_SECRET=your-secret

docker-compose up --build
```

---

## 🔌 API Reference

Full interactive API docs: `http://localhost:8080/swagger-ui`

### Authentication
```
POST /api/v1/auth/login           Login (returns JWT)
POST /api/v1/auth/register        Register new user
POST /api/v1/auth/refresh         Refresh access token
```

### Water Quality (IoT Ingestion)
```
GET  /api/v1/water/health         Health check (public)
POST /api/v1/water/reading        Submit IoT reading
GET  /api/v1/water/readings       Get all readings
GET  /api/v1/water/village/{id}   Readings by village
GET  /api/v1/water/alerts         Get DANGER readings
```

### Disease Monitoring
```
POST /api/v1/symptoms/report             Submit symptom report
GET  /api/v1/symptoms/reports            All reports
GET  /api/v1/symptoms/reports/village/{id}  By village
PUT  /api/v1/symptoms/report/{id}/review  Review report
```

### Alerts
```
GET  /api/v1/alerts              All alerts
GET  /api/v1/alerts/active       Active alerts only
PUT  /api/v1/alerts/{id}/acknowledge
PUT  /api/v1/alerts/{id}/resolve
```

### AI Predictions
```
GET  /api/v1/predictions/village/{id}    AI prediction for village
```

### Dashboard
```
GET  /api/v1/dashboard/stats     Aggregated statistics
```

### Admin
```
GET  /api/v1/admin/users         All users
PUT  /api/v1/admin/users/{id}/deactivate
GET  /api/v1/admin/audit-logs    Audit trail
```

---

## 🤖 AI/ML Module

Three prediction models run on every village query:

| Model | Algorithm | Output |
|---|---|---|
| Disease Outbreak | Random Forest heuristic | Probability (0–1) |
| Water Quality Trend | ARIMA-like | IMPROVING/STABLE/DEGRADING |
| Anomaly Detection | Isolation Forest heuristic | Boolean + description |

**Risk Levels:**
- `LOW` (0–25%) — Standard monitoring
- `MEDIUM` (25–50%) — Increase monitoring frequency
- `HIGH` (50–75%) — Immediate inspection required
- `CRITICAL` (75–100%) — Emergency response team deploy

---

## 🔒 Security Features

- **JWT** — Access token (15 min) + Refresh token (7 days)
- **RBAC** — Spring Security `@PreAuthorize` on every endpoint
- **BCrypt** — Password encoding (strength: 12)
- **Rate Limiting** — Bucket4j: 20 req/60s on auth endpoints
- **CORS** — Configurable allowed origins
- **HSTS** — `max-age=31536000; includeSubDomains; preload`
- **CSP** — Content-Security-Policy header
- **X-Frame-Options** — DENY (clickjacking protection)
- **Audit Logging** — All security events tracked in Firestore
- **Input Validation** — Bean Validation on all DTOs

---

## 📱 SMS Alerts (Twilio)

Set environment variables to activate:
```
TWILIO_ACCOUNT_SID=ACxxxxxxxx
TWILIO_AUTH_TOKEN=your-auth-token
TWILIO_FROM=+15005550006
TWILIO_ENABLED=true
```

Alerts trigger when:
- pH outside 6.5–8.5
- TDS > 500 ppm
- Turbidity > 5 NTU
- Disease outbreak threshold (≥3 reports in 48h) reached

---

## 🧪 Testing

### Backend
```bash
cd neerveda-backend
mvn test
# Coverage report: target/site/jacoco/index.html
```

### Frontend
```bash
cd neerveda-frontend
npm test
npm run test:coverage
```

---

## 🚀 Deployment

### Frontend → Vercel
1. Import repo in Vercel dashboard
2. Set `NEXT_PUBLIC_API_URL` environment variable
3. Deploy

### Backend → Render
1. Connect GitHub repo
2. Set environment variables in Render dashboard
3. Deploy as Web Service (Docker or Maven)

---

## 📂 Project Structure

```
NeerVeda-1/
├── neerveda-backend/
│   ├── src/main/java/com/neerveda/
│   │   ├── config/          FirebaseConfig, SecurityConfig, OpenApiConfig
│   │   ├── controller/      Auth, Water, Symptoms, Alerts, Dashboard, Prediction, Admin
│   │   ├── dto/             Request/Response DTOs with validation
│   │   ├── exception/       Global exception handler
│   │   ├── model/           User, WaterQualityData, SymptomReport, Alert, AuditLog
│   │   ├── repository/      Generic Firestore repository
│   │   ├── security/        JWT, UserPrincipal, AuthFilter
│   │   └── service/         Water, Auth, User, Alert, Symptoms, SMS, AI, Dashboard
│   └── src/test/            Unit tests
├── neerveda-frontend/
│   └── src/
│       ├── app/             Next.js pages (landing, auth, dashboard)
│       ├── components/      UI components, layout, dashboard widgets
│       ├── lib/             Axios client, utils
│       ├── store/           Zustand auth store
│       └── types/           TypeScript interfaces
├── .github/workflows/       CI/CD pipeline
├── docker-compose.yml
└── README.md
```

---

## 🏅 SIH Judging Criteria Coverage

| Criterion | Implementation |
|---|---|
| **Innovation** | AI outbreak prediction, IoT + ML pipeline, real-time alerting |
| **Scalability** | Firebase Firestore, stateless JWT, Docker microservices |
| **Security** | RBAC, JWT, BCrypt, rate limiting, audit logs, CSP |
| **Usability** | SaaS-grade UI, dark mode, responsive, role-based views |
| **Technical Depth** | Java 21, Spring Security, Next.js 15, Zustand, Recharts |
| **Impact** | 2M+ potential lives protected across rural India |

---

## 👥 Team CORE_401

Built with ❤️ for Smart India Hackathon 2025

---

*Neer (नीर) = Water | Veda (वेद) = Knowledge/Wisdom*
*"Knowledge of Water — For the Safety of All"*
