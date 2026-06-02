"use client";

import { motion } from "framer-motion";
import { BellRingIcon, CheckCircleIcon } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Alert } from "@/types";
import { formatTimestamp, getSeverityColor } from "@/lib/utils";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { alertsApi } from "@/lib/api";
import toast from "react-hot-toast";

interface AlertsListProps {
  alerts: Alert[];
  showActions?: boolean;
}

export function AlertsList({ alerts, showActions = true }: AlertsListProps) {
  const queryClient = useQueryClient();

  const acknowledgeMutation = useMutation({
    mutationFn: (id: string) => alertsApi.acknowledge(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["alerts"] });
      queryClient.invalidateQueries({ queryKey: ["active-alerts"] });
      toast.success("Alert acknowledged");
    },
  });

  if (alerts.length === 0) {
    return (
      <Card>
        <CardContent className="flex flex-col items-center justify-center py-12 text-center">
          <CheckCircleIcon className="w-10 h-10 text-green-500 mb-3" />
          <p className="font-medium">No active alerts</p>
          <p className="text-sm text-muted-foreground mt-1">
            All water quality parameters are within safe limits.
          </p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-4">
        <CardTitle className="text-base flex items-center gap-2">
          <BellRingIcon className="w-4 h-4 text-red-500" />
          Active Alerts
          <Badge variant="danger">{alerts.length}</Badge>
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-3 max-h-96 overflow-y-auto scrollbar-thin">
        {alerts.map((alert, i) => (
          <motion.div
            key={alert.id}
            initial={{ opacity: 0, x: -10 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: i * 0.05 }}
            className="flex items-start gap-3 p-3 rounded-lg border border-border hover:bg-accent/30 transition-colors"
          >
            <div
              className={`w-2 h-2 mt-1.5 rounded-full flex-shrink-0 ${
                alert.severity === "CRITICAL"
                  ? "bg-red-500 animate-pulse"
                  : alert.severity === "HIGH"
                  ? "bg-orange-500"
                  : alert.severity === "MEDIUM"
                  ? "bg-amber-500"
                  : "bg-blue-500"
              }`}
            />
            <div className="flex-1 min-w-0">
              <div className="flex items-start justify-between gap-2">
                <p className="text-sm font-medium truncate">{alert.title}</p>
                <Badge
                  variant="outline"
                  className={`text-xs flex-shrink-0 ${getSeverityColor(alert.severity)}`}
                >
                  {alert.severity}
                </Badge>
              </div>
              <p className="text-xs text-muted-foreground mt-0.5 line-clamp-2">
                {alert.message}
              </p>
              <p className="text-[10px] text-muted-foreground mt-1">
                {alert.villageName} · {formatTimestamp(alert.createdAt)}
              </p>
            </div>
            {showActions && alert.status === "ACTIVE" && (
              <Button
                size="sm"
                variant="outline"
                className="flex-shrink-0 h-7 text-xs"
                onClick={() => acknowledgeMutation.mutate(alert.id)}
                loading={acknowledgeMutation.isPending}
              >
                ACK
              </Button>
            )}
          </motion.div>
        ))}
      </CardContent>
    </Card>
  );
}
