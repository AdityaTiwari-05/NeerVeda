// ============================================================
// NeerVeda — TypeScript Types
// ============================================================

export type Role =
  | "ADMIN"
  | "GOVERNMENT_OFFICER"
  | "HEALTH_WORKER"
  | "WATER_INSPECTOR"
  | "PUBLIC_VIEWER";

export type WaterStatus = "SAFE" | "WARNING" | "DANGER";
export type AlertSeverity = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
export type AlertType = "WATER_QUALITY" | "DISEASE_OUTBREAK" | "SYSTEM";
export type AlertStatus = "ACTIVE" | "ACKNOWLEDGED" | "RESOLVED";
export type RiskLevel = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

// -------------------------------------------------------
// AUTH
// -------------------------------------------------------

export interface User {
  id: string;
  name: string;
  email: string;
  role: Role;
  phone?: string;
  district?: string;
  state?: string;
  active: boolean;
  createdAt?: string;
  lastLogin?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  userId: string;
  name: string;
  email: string;
  role: Role;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: Role;
  phone?: string;
  district?: string;
  state?: string;
}

// -------------------------------------------------------
// WATER QUALITY
// -------------------------------------------------------

export interface WaterReading {
  id: string;
  villageId: string;
  villageName: string;
  district: string;
  state: string;
  latitude: number;
  longitude: number;
  ph: number;
  tds: number;
  turbidity: number;
  temperature: number;
  status: WaterStatus;
  alertParameter?: string;
  alertMessage: string;
  deviceId: string;
  timestamp: string;
}

export interface WaterReadingRequest {
  villageId: string;
  villageName: string;
  district?: string;
  state?: string;
  latitude?: number;
  longitude?: number;
  ph: number;
  tds: number;
  turbidity: number;
  temperature: number;
  deviceId: string;
}

// -------------------------------------------------------
// SYMPTOM REPORTS
// -------------------------------------------------------

export interface SymptomReport {
  id: string;
  reportedBy: string;
  reporterRole: string;
  reporterPhone?: string;
  patientAge: number;
  patientGender: string;
  villageId: string;
  villageName: string;
  district: string;
  state: string;
  latitude?: number;
  longitude?: number;
  symptoms: string[];
  suspectedDisease?: string;
  severity: "MILD" | "MODERATE" | "SEVERE";
  affectedCount: number;
  waterSource?: string;
  status: "PENDING" | "REVIEWED" | "RESOLVED";
  timestamp: string;
  reviewNotes?: string;
}

// -------------------------------------------------------
// ALERTS
// -------------------------------------------------------

export interface Alert {
  id: string;
  alertType: AlertType;
  severity: AlertSeverity;
  villageId: string;
  villageName: string;
  district: string;
  state: string;
  latitude: number;
  longitude: number;
  title: string;
  message: string;
  affectedParameter?: string;
  status: AlertStatus;
  createdAt: string;
  resolvedAt?: string;
  resolvedBy?: string;
}

// -------------------------------------------------------
// PREDICTIONS
// -------------------------------------------------------

export interface PredictionResponse {
  villageId: string;
  villageName: string;
  district: string;
  state: string;
  overallRiskLevel: RiskLevel;
  overallRiskScore: number;
  outbreakProbability: number;
  predictedDiseases: string[];
  outbreakTimeframe: string;
  waterQualityTrend: "IMPROVING" | "STABLE" | "DEGRADING";
  anomalyDetected: boolean;
  anomalyDescription?: string;
  recommendations: string[];
  immediateAction?: string;
  generatedAt: string;
  modelVersion: string;
}

// -------------------------------------------------------
// DASHBOARD
// -------------------------------------------------------

export interface DashboardStats {
  totalVillages: number;
  totalAlerts: number;
  activeAlerts: number;
  activeSensors: number;
  waterSafetyIndex: number;
  safeVillages: number;
  warningVillages: number;
  dangerVillages: number;
  totalSymptomReports: number;
  pendingReviews: number;
  outbreakRiskVillages: number;
  monthlyWaterQuality: Array<{ date: string; safe: number; danger: number }>;
  diseaseTrends: Array<{ disease: string; count: number }>;
  sensorActivity: Array<{ deviceId: string; readings: number }>;
  generatedAt: string;
}

// -------------------------------------------------------
// API RESPONSE WRAPPER
// -------------------------------------------------------

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
  statusCode: number;
}
