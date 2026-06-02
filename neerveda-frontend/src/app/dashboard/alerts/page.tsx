"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { motion } from "framer-motion";
import { BellRingIcon, CheckCheckIcon, XCircleIcon } from "lucide-react";
import { alertsApi } from "@/lib/api";
import { Alert } from "@/types";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Topbar } from "@/components/layout/Topbar";
import { formatTimestamp, getSeverityColor } from "@/lib/utils";
import { useAuthStore } from "@/store/authStore";
import toast from "react-hot-toast";

export default function AlertsPage() {
  const queryClient = useQueryClient();
  const { hasRole } = useAuthStore();

  const { data, isLoading } = useQuery({
    queryKey: ["alerts"],
    queryFn: () => alertsApi.getAll().then((r) => r.data.data),
    refetchInterval: 30000,
  });

  const alerts: Alert[] = data || [];

  const acknowledgeMutation = useMutation({
    mutationFn: (id: string) => alertsApi.acknowledge(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["alerts"] });
      queryClient.invalidateQueries({ queryKey: ["active-alerts"] });
      toast.success("Alert acknowledged");
    },
    onError: () => toast.error("Failed to acknowledge alert"),
  });

  const resolveMutation = useMutation({
    mutationFn: (id: string) => alertsApi.resolve(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["alerts"] });
      toast.success("Alert resolved");
    },
    onError: () => toast.error("Failed to resolve alert"),
  });

  const activeAlerts = alerts.filter((a) => a.status === "ACTIVE");
  const ackAlerts = alerts.filter((a) => a.status === "ACKNOWLEDGED");
  const resolvedAlerts = alerts.filter((a) => a.status === "RESOLVED");

  return (
    <>
      <Topbar title="Alerts Center" />
      <div className="max-w-6xl mx-auto space-y-6 animate-fade-in">

        {/* Summary */}
        <div className="grid grid-cols-3 gap-4">
          <Card>
            <CardContent className="p-4 flex items-center gap-3">
              <div className="w-10 h-10 rounded-lg bg-red-50 dark:bg-red-950 flex items-center justify-center">
                <BellRingIcon className="w-5 h-5 text-red-600" />
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Active</p>
                <p className="text-2xl font-bold text-red-600">{activeAlerts.length}</p>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-4 flex items-center gap-3">
              <div className="w-10 h-10 rounded-lg bg-amber-50 dark:bg-amber-950 flex items-center justify-center">
                <CheckCheckIcon className="w-5 h-5 text-amber-600" />
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Acknowledged</p>
                <p className="text-2xl font-bold text-amber-600">{ackAlerts.length}</p>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-4 flex items-center gap-3">
              <div className="w-10 h-10 rounded-lg bg-green-50 dark:bg-green-950 flex items-center justify-center">
                <XCircleIcon className="w-5 h-5 text-green-600" />
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Resolved</p>
                <p className="text-2xl font-bold text-green-600">{resolvedAlerts.length}</p>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Alert table */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">All Alerts</CardTitle>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="space-y-3">
                {[...Array(5)].map((_, i) => (
                  <div key={i} className="h-16 rounded-lg bg-muted animate-pulse" />
                ))}
              </div>
            ) : alerts.length === 0 ? (
              <div className="text-center py-12 text-muted-foreground">
                <BellRingIcon className="w-10 h-10 mx-auto mb-3 opacity-40" />
                <p>No alerts recorded yet.</p>
              </div>
            ) : (
              <div className="space-y-2">
                {alerts.map((alert, i) => (
                  <motion.div
                    key={alert.id}
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: i * 0.03 }}
                    className="flex items-start gap-4 p-4 rounded-xl border border-border hover:bg-accent/20 transition-colors"
                  >
                    <div
                      className={`w-2 h-2 mt-2 rounded-full flex-shrink-0 ${
                        alert.status === "ACTIVE"
                          ? "bg-red-500 animate-pulse"
                          : alert.status === "ACKNOWLEDGED"
                          ? "bg-amber-500"
                          : "bg-green-500"
                      }`}
                    />
                    <div className="flex-1 min-w-0 grid grid-cols-1 md:grid-cols-4 gap-2">
                      <div className="md:col-span-2">
                        <p className="text-sm font-medium">{alert.title}</p>
                        <p className="text-xs text-muted-foreground mt-0.5 line-clamp-1">
                          {alert.message}
                        </p>
                      </div>
                      <div className="flex items-center gap-2">
                        <Badge
                          variant="outline"
                          className={`text-xs ${getSeverityColor(alert.severity)}`}
                        >
                          {alert.severity}
                        </Badge>
                        <Badge variant="outline" className="text-xs">
                          {alert.alertType.replace("_", " ")}
                        </Badge>
                      </div>
                      <div className="text-xs text-muted-foreground">
                        <p>{alert.villageName}</p>
                        <p>{formatTimestamp(alert.createdAt)}</p>
                      </div>
                    </div>
                    {hasRole("ADMIN", "GOVERNMENT_OFFICER", "HEALTH_WORKER") && (
                      <div className="flex gap-2 flex-shrink-0">
                        {alert.status === "ACTIVE" && (
                          <Button
                            size="sm"
                            variant="outline"
                            className="h-8 text-xs"
                            onClick={() => acknowledgeMutation.mutate(alert.id)}
                            loading={acknowledgeMutation.isPending}
                          >
                            ACK
                          </Button>
                        )}
                        {(alert.status === "ACTIVE" || alert.status === "ACKNOWLEDGED") &&
                          hasRole("ADMIN", "GOVERNMENT_OFFICER") && (
                            <Button
                              size="sm"
                              variant="success"
                              className="h-8 text-xs"
                              onClick={() => resolveMutation.mutate(alert.id)}
                              loading={resolveMutation.isPending}
                            >
                              Resolve
                            </Button>
                          )}
                      </div>
                    )}
                  </motion.div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </>
  );
}
