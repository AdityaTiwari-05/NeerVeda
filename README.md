# 💧 NeerVeda Frontend
### Water Safety & Disease Prevention — Auth + Dashboard

## 🚀 How to Run

Just open `index.html` in your browser — no build step needed!

```
neerveda-frontend/
└── index.html   ← Complete app (auth + dashboard)
```

## 🔗 Connect to Backend

In `index.html`, find this line and update if needed:
```js
const API_BASE = 'http://localhost:8080/api/v1';
```

**Start your Spring Boot backend first:**
```bash
cd neerveda-backend
mvn spring-boot:run
```
Then open `index.html` — the dashboard will show **"Backend API online"** ✅

## 🔐 Security Features Implemented

| Feature | Details |
|---|---|
| Input Sanitization | All user input sanitized to prevent XSS |
| Rate Limiting | 5 login attempts → 15 min lockout |
| Password Strength | 4-bar indicator, enforces strong passwords |
| Session Management | Token stored in sessionStorage (clears on tab close) |
| CSRF Token | Generated per session, sent with every API call |
| Form Validation | Client-side + server-side (Spring Boot) |
| Password Hashing | bcrypt on backend (simulated on frontend demo) |

## 📦 Pages / Sections

- **Login** — Email + password, rate limiting, OAuth buttons
- **Register** — Full form with role selection (ASHA Worker, Volunteer, Clinic, Health Officer)
- **Dashboard** — Submit sensor readings, view live water quality data
- **Forgot Password** — Modal with email reset flow

## 🔮 Next Steps (Phase 2)
- [ ] Connect OAuth to `/api/v1/auth/google`
- [ ] Add map view with Leaflet.js
- [ ] Add Chart.js for water quality trends
- [ ] Add symptom report submission form
- [ ] PWA support (offline-first)
