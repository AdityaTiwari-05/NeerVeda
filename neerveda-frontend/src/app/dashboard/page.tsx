"use client";

import { useQuery } from "@tanstack/react-query";
import {
  DropletIcon,
  BellRingIcon,
  ActivityIcon,
  ShieldCheckIcon,
  RadioIcon,
  AlertTriangleIcon,
  TrendingUpIcon,
  UsersIcon,
} from "lucide-react";
import { StatCard } from "@/components/dashboard/StatCard";
import { WaterQualityChart } from "@/components/dashboard/WaterQualityChart";
import { DiseaseTrendChart } from "@/components/dashboard/DiseaseTrendChart";
import { AlertsList } from "@/components/dashboard/AlertsList";
import { Topbar } from "@/components/layout/Topbar";
import { dashboardApi, alertsApi } from "@/lib/api";
import { Alert } from "@/types";

export default function DashboardPage() {
  const { data: statsData, isLoading: statsLoading } = useQuery({
    queryKey: ["dashboard-stats"],
    queryFn: () => dashboardApi.getStats().then((r) => r.data.data),
    refetchInterval: 60000,
  });

  const { data: alertsData } = useQuery({
    queryKey: ["active-alerts"],
    queryFn: () => alertsApi.getActive().then((r) => r.data.data),
    refetchInterval: 30000,
  });

  const stats = statsData;
  const alerts: Alert[] = alertsData || [];

  return (
    <>
      <Topbar title="Dashboard Overview" />
      <div className="max-w-7xl mx-auto space-y-6 animate-fade-in">

        {/* Summary Stats */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <StatCard
            title="Total Villages"
            value={stats?.totalVillages ?? "—"}
            subtitle="Monitored locations"
            icon={DropletIcon}
            iconColor="text-nv-blue-600"
            index={0}
          />
          <StatCard
            title="Active Alerts"
            value={stats?.activeAlerts ?? "—"}
            subtitle={`${stats?.totalAlerts ?? 0} total alerts`}
            icon={BellRingIcon}
            iconColor="text-red-600"
            index={1}
          />
          <StatCard
            title="Active Sensors"
            value={stats?.activeSensors ?? "—"}
            subtitle="IoT devices online"
            icon={RadioIcon}
            iconColor="text-nv-teal-600"
            index={2}
          />
          <StatCard
            title="Safety Index"
            value={stats ? `${stats.waterSafetyIndex}%` : "—"}
            subtitle="Villages with safe water"
            icon={ShieldCheckIcon}
            iconColor="text-green-600"
            index={3}
          />
        </div>

        {/* Second row stats */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <StatCard
            title="Safe Villages"
            value={stats?.safeVillages ?? "—"}
            icon={ShieldCheckIcon}
            iconColor="text-green-600"
            index={4}
          />
          <StatCard
            title="Warning Villages"
            value={stats?.warningVillages ?? "—"}
            icon={AlertTriangleIcon}
            iconColor="text-amber-600"
            index={5}
          />
          <StatCard
            title="Danger Villages"
            value={stats?.dangerVillages ?? "—"}
            icon={AlertTriangleIcon}
            iconColor="text-red-600"
            index={6}
          />
          <StatCard
            title="Symptom Reports"
            value={stats?.totalSymptomReports ?? "—"}
            subtitle={`${stats?.pendingReviews ?? 0} pending`}
            icon={ActivityIcon}
            iconColor="text-purple-600"
            index={7}
          />
        </div>

        {/* Charts */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <WaterQualityChart
            data={stats?.monthlyWaterQuality ?? []}
          />
          <DiseaseTrendChart
            data={stats?.diseaseTrends ?? []}
          />
        </div>

        {/* Alerts */}
        <AlertsList alerts={alerts} />

      </div>
    </>
  );
}
