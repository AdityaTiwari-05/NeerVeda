"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { motion } from "framer-motion";
import { ShieldCheckIcon, UserXIcon } from "lucide-react";
import { adminApi } from "@/lib/api";
import { User } from "@/types";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Topbar } from "@/components/layout/Topbar";
import { formatTimestamp } from "@/lib/utils";
import { useAuthStore } from "@/store/authStore";
import { useEffect } from "react";
import { useRouter } from "next/navigation";
import toast from "react-hot-toast";

const roleBadgeVariant = (role: string) => {
  const map: Record<string, "default" | "info" | "warning" | "danger" | "safe"> = {
    ADMIN: "danger",
    GOVERNMENT_OFFICER: "info",
    HEALTH_WORKER: "safe",
    WATER_INSPECTOR: "warning",
    PUBLIC_VIEWER: "default",
  };
  return map[role] || "default";
};

export default function AdminPage() {
  const { isAdmin } = useAuthStore();
  const router = useRouter();
  const queryClient = useQueryClient();

  useEffect(() => {
    if (!isAdmin()) router.replace("/dashboard");
  }, [isAdmin, router]);

  const { data, isLoading } = useQuery({
    queryKey: ["admin-users"],
    queryFn: () => adminApi.getAllUsers().then((r) => r.data.data),
  });

  const { data: auditData } = useQuery({
    queryKey: ["audit-logs"],
    queryFn: () => adminApi.getAuditLogs().then((r) => r.data.data),
  });

  const deactivateMutation = useMutation({
    mutationFn: (id: string) => adminApi.deactivateUser(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-users"] });
      toast.success("User deactivated");
    },
    onError: () => toast.error("Failed to deactivate user"),
  });

  const users: User[] = data || [];
  const auditLogs: Record<string, string>[] = auditData || [];

  return (
    <>
      <Topbar title="Admin Panel" />
      <div className="max-w-7xl mx-auto space-y-6 animate-fade-in">
        <div className="flex items-center gap-2">
          <ShieldCheckIcon className="w-6 h-6 text-red-600" />
          <h2 className="text-2xl font-bold">Admin Control Panel</h2>
        </div>

        {/* User Management */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">User Management</CardTitle>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="space-y-3">
                {[...Array(5)].map((_, i) => (
                  <div key={i} className="h-14 rounded-lg bg-muted animate-pulse" />
                ))}
              </div>
            ) : (
              <div className="divide-y divide-border">
                {users.map((user, i) => (
                  <motion.div
                    key={user.id}
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ delay: i * 0.04 }}
                    className="flex items-center justify-between py-3 gap-4"
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-nv-blue-100 dark:bg-nv-blue-900 flex items-center justify-center text-xs font-bold text-nv-blue-700">
                        {user.name?.charAt(0).toUpperCase()}
                      </div>
                      <div>
                        <p className="text-sm font-medium">{user.name}</p>
                        <p className="text-xs text-muted-foreground">{user.email}</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-3">
                      <Badge variant={roleBadgeVariant(user.role)}>
                        {user.role.replace(/_/g, " ")}
                      </Badge>
                      <Badge variant={user.active ? "safe" : "danger"}>
                        {user.active ? "Active" : "Inactive"}
                      </Badge>
                      {user.active && (
                        <Button
                          size="sm"
                          variant="outline"
                          className="h-7 text-xs gap-1"
                          onClick={() => deactivateMutation.mutate(user.id)}
                          loading={deactivateMutation.isPending}
                        >
                          <UserXIcon className="w-3 h-3" />
                          Deactivate
                        </Button>
                      )}
                    </div>
                  </motion.div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Audit Logs */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Recent Audit Logs</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="max-h-64 overflow-y-auto scrollbar-thin space-y-2">
              {auditLogs.slice(0, 50).map((log, i) => (
                <div
                  key={i}
                  className="flex items-center justify-between text-xs py-2 border-b border-border last:border-0"
                >
                  <div className="flex items-center gap-3">
                    <span
                      className={`w-1.5 h-1.5 rounded-full flex-shrink-0 ${
                        log.success === "true" ? "bg-green-500" : "bg-red-500"
                      }`}
                    />
                    <span className="font-medium">{log.eventType}</span>
                    <span className="text-muted-foreground">{log.userEmail}</span>
                    <span className="text-muted-foreground hidden md:inline">{log.description}</span>
                  </div>
                  <span className="text-muted-foreground flex-shrink-0">
                    {formatTimestamp(log.timestamp as string)}
                  </span>
                </div>
              ))}
              {auditLogs.length === 0 && (
                <p className="text-center text-muted-foreground py-6">
                  No audit logs yet.
                </p>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
