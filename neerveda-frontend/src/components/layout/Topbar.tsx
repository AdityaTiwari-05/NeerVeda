"use client";

import { useTheme } from "next-themes";
import { useRouter } from "next/navigation";
import { BellIcon, SunIcon, MoonIcon, LogOutIcon, UserCircleIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { useAuthStore } from "@/store/authStore";
import { useQuery } from "@tanstack/react-query";
import { alertsApi } from "@/lib/api";
import toast from "react-hot-toast";

export function Topbar({ title }: { title?: string }) {
  const { theme, setTheme } = useTheme();
  const router = useRouter();
  const { user, clearAuth } = useAuthStore();

  const { data: alertsData } = useQuery({
    queryKey: ["active-alerts"],
    queryFn: () => alertsApi.getActive().then((r) => r.data),
    refetchInterval: 30000,
  });

  const activeAlertCount = alertsData?.data?.length ?? 0;

  const handleLogout = () => {
    clearAuth();
    toast.success("Logged out successfully");
    router.push("/");
  };

  return (
    <header className="h-16 border-b border-border bg-card flex items-center justify-between px-6 sticky top-0 z-10">
      {/* Page title */}
      <div>
        <h1 className="text-lg font-semibold text-foreground">{title || "Dashboard"}</h1>
        <p className="text-xs text-muted-foreground">
          NeerVeda · SIH25001 · Team CORE_401
        </p>
      </div>

      {/* Right actions */}
      <div className="flex items-center gap-2">
        {/* Alerts bell */}
        <Button
          variant="ghost"
          size="icon"
          className="relative"
          onClick={() => router.push("/dashboard/alerts")}
          aria-label="View alerts"
        >
          <BellIcon className="w-4 h-4" />
          {activeAlertCount > 0 && (
            <span className="absolute -top-0.5 -right-0.5 w-4 h-4 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center">
              {activeAlertCount > 9 ? "9+" : activeAlertCount}
            </span>
          )}
        </Button>

        {/* Theme toggle */}
        <Button
          variant="ghost"
          size="icon"
          onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
          aria-label="Toggle theme"
        >
          {theme === "dark" ? (
            <SunIcon className="w-4 h-4" />
          ) : (
            <MoonIcon className="w-4 h-4" />
          )}
        </Button>

        {/* User info */}
        {user && (
          <div className="flex items-center gap-2 pl-2 border-l border-border">
            <UserCircleIcon className="w-5 h-5 text-muted-foreground" />
            <div className="hidden md:block">
              <p className="text-sm font-medium leading-none">{user.name}</p>
              <Badge variant="info" className="text-[10px] mt-0.5">
                {user.role.replace(/_/g, " ")}
              </Badge>
            </div>
            <Button
              variant="ghost"
              size="icon"
              onClick={handleLogout}
              aria-label="Logout"
            >
              <LogOutIcon className="w-4 h-4" />
            </Button>
          </div>
        )}
      </div>
    </header>
  );
}
