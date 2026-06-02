import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from "axios";
import Cookies from "js-cookie";

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

// -------------------------------------------------------
// AXIOS INSTANCE
// -------------------------------------------------------

const api: AxiosInstance = axios.create({
  baseURL: API_URL,
  timeout: 15000,
  headers: {
    "Content-Type": "application/json",
  },
});

// -------------------------------------------------------
// REQUEST INTERCEPTOR — Attach JWT
// -------------------------------------------------------

api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = Cookies.get("accessToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// -------------------------------------------------------
// RESPONSE INTERCEPTOR — Handle 401, refresh token
// -------------------------------------------------------

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: AxiosError | null, token: string | null = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error);
    else resolve(token);
  });
  failedQueue = [];
};

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return api(originalRequest);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = Cookies.get("refreshToken");
      if (!refreshToken) {
        Cookies.remove("accessToken");
        Cookies.remove("refreshToken");
        if (typeof window !== "undefined") window.location.href = "/auth/login";
        return Promise.reject(error);
      }

      try {
        const response = await axios.post(
          `${API_URL}/api/v1/auth/refresh?refreshToken=${refreshToken}`
        );
        const { accessToken, refreshToken: newRefresh } = response.data.data;
        Cookies.set("accessToken", accessToken, { secure: true, sameSite: "strict" });
        Cookies.set("refreshToken", newRefresh, { secure: true, sameSite: "strict" });
        processQueue(null, accessToken);
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError as AxiosError, null);
        Cookies.remove("accessToken");
        Cookies.remove("refreshToken");
        if (typeof window !== "undefined") window.location.href = "/auth/login";
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default api;

// -------------------------------------------------------
// API FUNCTIONS
// -------------------------------------------------------

// Auth
export const authApi = {
  login: (email: string, password: string) =>
    api.post("/api/v1/auth/login", { email, password }),
  register: (data: object) => api.post("/api/v1/auth/register", data),
  refresh: (token: string) =>
    api.post(`/api/v1/auth/refresh?refreshToken=${token}`),
};

// Water Quality
export const waterApi = {
  submitReading: (data: object) => api.post("/api/v1/water/reading", data),
  getAllReadings: () => api.get("/api/v1/water/readings"),
  getReadingById: (id: string) => api.get(`/api/v1/water/reading/${id}`),
  getByVillage: (villageId: string) => api.get(`/api/v1/water/village/${villageId}`),
  getDangerousReadings: () => api.get("/api/v1/water/alerts"),
};

// Symptom Reports
export const symptomsApi = {
  submitReport: (data: object) => api.post("/api/v1/symptoms/report", data),
  getAllReports: () => api.get("/api/v1/symptoms/reports"),
  getByVillage: (villageId: string) =>
    api.get(`/api/v1/symptoms/reports/village/${villageId}`),
  reviewReport: (id: string, status: string, notes?: string) =>
    api.put(`/api/v1/symptoms/report/${id}/review`, null, {
      params: { status, notes },
    }),
};

// Alerts
export const alertsApi = {
  getAll: () => api.get("/api/v1/alerts"),
  getActive: () => api.get("/api/v1/alerts/active"),
  getByVillage: (villageId: string) => api.get(`/api/v1/alerts/village/${villageId}`),
  acknowledge: (id: string) => api.put(`/api/v1/alerts/${id}/acknowledge`),
  resolve: (id: string) => api.put(`/api/v1/alerts/${id}/resolve`),
};

// Dashboard
export const dashboardApi = {
  getStats: () => api.get("/api/v1/dashboard/stats"),
};

// Predictions
export const predictionsApi = {
  getForVillage: (villageId: string) =>
    api.get(`/api/v1/predictions/village/${villageId}`),
};

// Admin
export const adminApi = {
  getAllUsers: () => api.get("/api/v1/admin/users"),
  getUserById: (id: string) => api.get(`/api/v1/admin/users/${id}`),
  deactivateUser: (id: string) => api.put(`/api/v1/admin/users/${id}/deactivate`),
  getAuditLogs: () => api.get("/api/v1/admin/audit-logs"),
};
