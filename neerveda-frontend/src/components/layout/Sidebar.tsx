"use client";

import { useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { motion, AnimatePresence } from "framer-motion";
import {
  DropletIcon,
  LayoutDashboardIcon,
  ActivityIcon,
  BellRingIcon,
  UsersIcon,
  SettingsIcon,
  MapPinIcon,
  BrainCircuitIcon,
  FileTextIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  ShieldCheckIcon,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { useAuthStore } from "@/store/authStore";

const navItems = [
  {
    label: "Dashboard",
    href: "/dashboard",
    icon: LayoutDashboardIcon,
    roles: ["ADMIN", "GOVERNMENT_OFFICER", "HEALTH_WORKER", "WATER_INSPECTOR", "PUBLIC_VIEWER"],
  },
  {
    label: "Water Quality",
    href: "/dashboard/water",
    icon: DropletIcon,
    roles: ["ADMIN", "GOVERNMENT_OFFICER", "WATER_INSPECTOR", "PUBLIC_VIEWER"],
  },
  {
    label: "Disease Monitor",
    href: "/dashboard/disease",
    icon: ActivityIcon,
    roles: ["ADMIN", "GOVERNMENT_OFFICER", "HEALTH_WORKER"],
  },
  {
    label: "Alerts",
    href: "/dashboard/alerts",
    icon: BellRingIcon,
    roles: ["ADMIN", "GOVERNMENT_OFFICER", "HEALTH_WORKER", "WATER_INSPECTOR"],
  },
  {
    label: "AI Predictions",
    href: "/dashboard/predictions",
    icon: BrainCircuitIcon,
    roles: ["ADMIN", "GOVERNMENT_OFFICER", "HEALTH_WORKER"],
  },
  {
    label: "Map View",
    href: "/dashboard/map",
    icon: MapPinIcon,
    roles: ["ADMIN", "GOVERNMENT_OFFICER", "HEALTH_WORKER", "WATER_INSPECTOR", "PUBLIC_VIEWER"],
  },
  {
    label: "Reports",
    href: "/dashboard/reports",
    icon: FileTextIcon,
    roles: ["ADMIN", "GOVERNMENT_OFFICER", "HEALTH_WORKER"],
  },
  {
    label: "Admin Panel",
    href: "/dashboard/admin",
    icon: ShieldCheckIcon,
    roles: ["ADMIN"],
  },
  {
    label: "Users",
    href: "/dashboard/admin/users",
    icon: UsersIcon,
    roles: ["ADMIN"],
  },
  {
    label: "Settings",
    href: "/dashboard/settings",
    icon: SettingsIcon,
    roles: ["ADMIN", "GOVERNMENT_OFFICER"],
  },
];

export function Sidebar() {
  const [collapsed, setCollapsed] = useState(false);
  const pathname = usePathname();
  const { user } = useAuthStore();

  const visibleItems = navItems.filter(
    (item) => user && item.roles.includes(user.role)
  );

  return (
    <motion.aside
      animate={{ width: collapsed ? 72 : 256 }}
      transition={{ duration: 0.2, ease: "easeInOut" }}
      className="relative flex flex-col h-full bg-card border-r border-border overflow-hidden"
    >
      {/* Logo */}
      <div className="flex items-center gap-3 px-4 py-5 border-b border-border">
        <div className="flex-shrink-0 w-8 h-8 rounded-lg bg-nv-blue-600 flex items-center justify-center">
          <DropletIcon className="w-4 h-4 text-white" />
        </div>
        <AnimatePresence>
          {!collapsed && (
            <motion.div
              initial={{ opacity: 0, x: -10 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -10 }}
              transition={{ duration: 0.15 }}
            >
              <p className="font-bold text-base text-foreground">NeerVeda</p>
              <p className="text-[10px] text-muted-foreground uppercase tracking-widest">
                CORE_401
              </p>
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* Navigation */}
      <nav className="flex-1 py-4 space-y-1 overflow-y-auto scrollbar-thin px-2">
        {visibleItems.map((item) => {
          const Icon = item.icon;
          const isActive = pathname === item.href ||
            (item.href !== "/dashboard" && pathname.startsWith(item.href));

          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors",
                "hover:bg-accent hover:text-accent-foreground",
                isActive
                  ? "bg-nv-blue-600 text-white shadow-sm"
                  : "text-muted-foreground"
              )}
              title={collapsed ? item.label : undefined}
            >
              <Icon className="w-4 h-4 flex-shrink-0" />
              <AnimatePresence>
                {!collapsed && (
                  <motion.span
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    transition={{ duration: 0.1 }}
                  >
                    {item.label}
                  </motion.span>
                )}
              </AnimatePresence>
            </Link>
          );
        })}
      </nav>

      {/* Role badge */}
      {!collapsed && user && (
        <div className="px-4 py-3 border-t border-border">
          <div className="flex items-center gap-2">
            <div className="w-7 h-7 rounded-full bg-nv-blue-100 dark:bg-nv-blue-900 flex items-center justify-center text-xs font-bold text-nv-blue-700 dark:text-nv-blue-300">
              {user.name.charAt(0).toUpperCase()}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-xs font-medium truncate">{user.name}</p>
              <p className="text-[10px] text-muted-foreground truncate">
                {user.role.replace(/_/g, " ")}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Collapse toggle */}
      <button
        onClick={() => setCollapsed(!collapsed)}
        className="absolute top-5 -right-3 w-6 h-6 rounded-full border border-border bg-card flex items-center justify-center shadow-sm hover:bg-accent transition-colors z-10"
        aria-label={collapsed ? "Expand sidebar" : "Collapse sidebar"}
      >
        {collapsed ? (
          <ChevronRightIcon className="w-3 h-3 text-muted-foreground" />
        ) : (
          <ChevronLeftIcon className="w-3 h-3 text-muted-foreground" />
        )}
      </button>
    </motion.aside>
  );
}
