"use client";

import { useQuery } from "@tanstack/react-query";
import { motion } from "framer-motion";
import {
  DropletIcon,
  ThermometerIcon,
  FlaskConicalIcon,
  WavesIcon,
  RefreshCwIcon,
} from "lucide-react";
import { waterApi } from "@/lib/api";
import { WaterReading } from "@/types";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Topbar } from "@/components/layout/Topbar";
import { formatTimestamp, getStatusColor, thresholds, formatNumber } from "@/lib/utils";

function ParameterGauge({
  label,
  value,
  unit,
  min,
  max,
  icon: Icon,
}: {
  label: string;
  value: number;
  unit: string;
  min?: number;
  max: number;
  icon: React.ElementType;
}) {
  const isViolation = min
    ? value < min || value > max
    : value > max;
  const percentage = min
    ? Math.max(0, Math.min(100, ((value - min) / (max - min)) * 100))
    : Math.max(0, Math.min(100, (value / max) * 100));

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between text-sm">
        <div className="flex items-center gap-1.5">
          <Icon className="w-3.5 h-3.5 text-muted-foreground" />
          <span className="font-medium">{label}</span>
        </div>
        <span className={isViolation ? "text-red-500 font-bold" : "text-foreground"}>
          {formatNumber(value)}{unit}
        </span>
      </div>
      <div className="h-2 bg-muted rounded-full overflow-hidden">
        <motion.div
          className={`h-full rounded-full ${isViolation ? "bg-red-500" : "bg-green-500"}`}
          initial={{ width: 0 }}
          animate={{ width: `${percentage}%` }}
          transition={{ duration: 0.8, ease: "easeOut" }}
        />
      </div>
      <p className="text-xs text-muted-foreground">
        Safe: {min ? `${min}–${max}` : `≤${max}`}{unit}
      </p>
    </div>
  );
}

export default function WaterPage() {
  const {
    data: readingsData,
    isLoading,
    refetch,
    isFetching,
  } = useQuery({
    queryKey: ["water-readings"],
    queryFn: () => waterApi.getAllReadings().then((r) => r.data.data),
    refetchInterval: 60000,
  });

  const readings: WaterReading[] = readingsData || [];

  return (
    <>
      <Topbar title="Water Quality Monitoring" />
      <div className="max-w-7xl mx-auto space-y-6 animate-fade-in">

        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold">Live Sensor Readings</h2>
            <p className="text-muted-foreground text-sm mt-0.5">
              {readings.length} readings from IoT sensors · Auto-refresh every 60s
            </p>
          </div>
          <Button
            variant="outline"
            size="sm"
            onClick={() => refetch()}
            disabled={isFetching}
            className="gap-2"
          >
            <RefreshCwIcon className={`w-3.5 h-3.5 ${isFetching ? "animate-spin" : ""}`} />
            Refresh
          </Button>
        </div>

        {isLoading ? (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
            {[...Array(6)].map((_, i) => (
              <div key={i} className="h-64 rounded-xl bg-muted animate-pulse" />
            ))}
          </div>
        ) : readings.length === 0 ? (
          <Card>
            <CardContent className="flex flex-col items-center justify-center py-16 text-center">
              <DropletIcon className="w-12 h-12 text-muted-foreground mb-4" />
              <p className="font-medium text-lg">No readings yet</p>
              <p className="text-muted-foreground text-sm mt-2">
                Connect IoT sensors to start monitoring water quality.
              </p>
            </CardContent>
          </Card>
        ) : (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
            {readings.map((reading: WaterReading, i) => (
              <motion.div
                key={reading.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.05 }}
              >
                <Card className={`border-l-4 ${
                  reading.status === "DANGER"
                    ? "border-l-red-500"
                    : reading.status === "WARNING"
                    ? "border-l-amber-500"
                    : "border-l-green-500"
                }`}>
                  <CardHeader className="pb-3">
                    <div className="flex items-start justify-between">
                      <div>
                        <CardTitle className="text-base">{reading.villageName}</CardTitle>
                        <p className="text-xs text-muted-foreground mt-0.5">
                          {reading.district}, {reading.state}
                        </p>
                      </div>
                      <Badge
                        className={getStatusColor(reading.status)}
                        variant="outline"
                      >
                        {reading.status}
                      </Badge>
                    </div>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <ParameterGauge
                      label="pH"
                      value={reading.ph}
                      unit=""
                      min={thresholds.ph.min}
                      max={thresholds.ph.max}
                      icon={FlaskConicalIcon}
                    />
                    <ParameterGauge
                      label="TDS"
                      value={reading.tds}
                      unit=" ppm"
                      max={thresholds.tds.max}
                      icon={WavesIcon}
                    />
                    <ParameterGauge
                      label="Turbidity"
                      value={reading.turbidity}
                      unit=" NTU"
                      max={thresholds.turbidity.max}
                      icon={DropletIcon}
                    />
                    <ParameterGauge
                      label="Temperature"
                      value={reading.temperature}
                      unit="°C"
                      max={thresholds.temperature.max}
                      icon={ThermometerIcon}
                    />

                    <div className="pt-2 border-t border-border">
                      <p className="text-xs text-muted-foreground">
                        Device: {reading.deviceId}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        Last reading: {formatTimestamp(reading.timestamp)}
                      </p>
                    </div>
                  </CardContent>
                </Card>
              </motion.div>
            ))}
          </div>
        )}
      </div>
    </>
  );
}
