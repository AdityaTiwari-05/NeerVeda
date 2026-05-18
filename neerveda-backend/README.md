# 💧 NeerVeda Backend
### Smart Water Safety & Disease Prevention System
> *Neer = Water | Veda = Knowledge/Wisdom*

Built for **Smart India Hackathon 2025** | Team CORE_401 | Problem ID: SIH25001

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend Framework | Spring Boot 3.2.5 |
| Language | Java 21 |
| Database | Firebase Firestore |
| IoT Integration | ESP32 + REST API |
| Notifications | Twilio SMS |
| Deployment | AWS / Netlify |
| AI/ML | Python (scikit-learn, ARIMA) |

---

## 📁 Project Structure

```
neerveda-backend/
├── src/
│   ├── main/
│   │   ├── java/com/neerveda/
│   │   │   ├── NeerVedaApplication.java    ← Main entry point
│   │   │   ├── controller/
│   │   │   │   ├── WaterQualityController.java  ← Water sensor APIs
│   │   │   │   └── SymptomReportController.java ← ASHA worker APIs
│   │   │   ├── model/
│   │   │   │   ├── WaterQualityData.java   ← Sensor reading model
│   │   │   │   ├── SymptomReport.java      ← Disease report model
│   │   │   │   └── Alert.java              ← Alert model
│   │   │   ├── service/
│   │   │   │   └── WaterQualityService.java ← Business logic
│   │   │   ├── dto/
│   │   │   │   └── ApiResponse.java        ← Standard API response
│   │   │   └── config/
│   │   │       └── FirebaseConfig.java     ← Firebase setup
│   │   └── resources/
│   │       └── application.properties      ← App configuration
│   └── test/
│       └── java/com/neerveda/
│           └── WaterQualityServiceTest.java ← Unit tests
└── pom.xml                                 ← Dependencies
```

---

## 🚀 How to Run

### Prerequisites
- Java 21
- Maven 3.8+
- VS Code (with Java Extension Pack)

### Steps
```bash
# 1. Clone the repo
git clone https://github.com/YOUR_USERNAME/neerveda-backend.git
cd neerveda-backend

# 2. Run the project
mvn spring-boot:run

# 3. Server starts at:
# http://localhost:8080
```

---

## 🌊 API Endpoints

### Water Quality APIs
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/v1/water/health` | Check if API is running |
| POST | `/api/v1/water/reading` | Submit IoT sensor reading |
| GET | `/api/v1/water/readings` | Get all readings |
| GET | `/api/v1/water/reading/{id}` | Get reading by ID |
| GET | `/api/v1/water/status/{villageId}` | Get village water status |
| GET | `/api/v1/water/alerts` | Get all dangerous readings |

### Symptom Report APIs
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/v1/symptoms/report` | Submit symptom report |
| GET | `/api/v1/symptoms/reports` | Get all reports |
| GET | `/api/v1/symptoms/reports/{villageId}` | Get village reports |
| PUT | `/api/v1/symptoms/report/{id}/review` | Review a report |

---

## 🧪 Sample API Test (use Postman or curl)

### Submit a water reading:
```json
POST http://localhost:8080/api/v1/water/reading
Content-Type: application/json

{
  "villageId": "VIL001",
  "villageName": "Dimapur Village",
  "district": "Dimapur",
  "state": "Nagaland",
  "latitude": 25.9022,
  "longitude": 93.7234,
  "ph": 5.8,
  "tds": 620,
  "turbidity": 8.5,
  "temperature": 28.0,
  "deviceId": "ESP32-001"
}
```

### Expected Response:
```json
{
  "success": true,
  "message": "Water quality reading recorded. Status: DANGER",
  "data": {
    "id": "...",
    "villageName": "Dimapur Village",
    "ph": 5.8,
    "status": "DANGER",
    "alertParameter": "pH, TDS, Turbidity",
    "alertMessage": "⚠️ pH level (5.8) is outside safe range..."
  }
}
```

---

## 🔒 Water Quality Thresholds (WHO / Jal Jeevan Mission)

| Parameter | Safe Range | Risk if Exceeded |
|-----------|-----------|-----------------|
| pH | 6.5 – 8.5 | Gastroenteritis, Skin Irritation |
| TDS | < 500 ppm | Kidney Problems |
| Turbidity | < 5 NTU | Cholera, Typhoid, Diarrhea |
| Temperature | < 35°C | Bacterial Growth, Hepatitis A |

---

## 🔥 Firebase Setup (Phase 2)
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create project `neerveda`
3. Enable Firestore Database
4. Go to Project Settings → Service Accounts → Generate Key
5. Save as `firebase-service-account.json` in `src/main/resources/`
6. ⚠️ This file is in `.gitignore` — NEVER commit it!

---

## 👥 Team CORE_401
Built with ❤️ for rural Northeast India communities.
