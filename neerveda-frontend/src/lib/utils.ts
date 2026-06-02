import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";
import { WaterStatus, AlertSeverity, RiskLevel } from "@/types";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// -------------------------------------------------------
// STATUS COLOR HELPERS
// -------------------------------------------------------

export function getStatusColor(status: WaterStatus): string {
  return {
    SAFE:    "text-green-500 bg-green-50 dark:bg-green-950 border-green-200",
    WARNING: "text-amber-500 bg-amber-50 dark:bg-amber-950 border-amber-200",
    DANGER:  "text-red-500 bg-red-50 dark:bg-red-950 border-red-200",
  }[status] ?? "text-gray-500 bg-gray-50";
}

export function getStatusDot(status: WaterStatus): string {
  return {
    SAFE:    "bg-green-500",
    WARNING: "bg-amber-500",
    DANGER:  "bg-red-500",
  }[status] ?? "bg-gray-400";
}

export function getSeverityColor(severity: AlertSeverity): string {
  return {
    LOW:      "text-blue-600 bg-blue-50 border-blue-200",
    MEDIUM:   "text-amber-600 bg-amber-50 border-amber-200",
    HIGH:     "text-orange-600 bg-orange-50 border-orange-200",
    CRITICAL: "text-red-600 bg-red-50 border-red-200",
  }[severity] ?? "text-gray-600 bg-gray-50";
}

export function getRiskColor(level: RiskLevel): string {
  return {
    LOW:      "text-green-600",
    MEDIUM:   "text-amber-600",
    HIGH:     "text-orange-600",
    CRITICAL: "text-red-600",
  }[level] ?? "text-gray-600";
}

// -------------------------------------------------------
// FORMATTING
// -------------------------------------------------------

export function formatTimestamp(ts: string): string {
  if (!ts) return "—";
  try {
    return new Intl.DateTimeFormat("en-IN", {
      dateStyle: "medium",
      timeStyle: "short",
    }).format(new Date(ts));
  } catch {
    return ts;
  }
}

export function formatNumber(n: number, decimals = 1): string {
  return n.toFixed(decimals);
}

export function formatPercentage(n: number): string {
  return (n * 100).toFixed(1) + "%";
}

// -------------------------------------------------------
// SENSOR VALUE HELPERS
// -------------------------------------------------------

export const thresholds = {
  ph:          { min: 6.5, max: 8.5, unit: "" },
  tds:         { min: 0,   max: 500, unit: "ppm" },
  turbidity:   { min: 0,   max: 5,   unit: "NTU" },
  temperature: { min: 0,   max: 35,  unit: "°C" },
};

export function getParameterStatus(
  param: keyof typeof thresholds,
  value: number
): WaterStatus {
  const t = thresholds[param];
  if (param === "ph") {
    return value < t.min || value > t.max ? "DANGER" : "SAFE";
  }
  return value > t.max ? "DANGER" : "SAFE";
}
