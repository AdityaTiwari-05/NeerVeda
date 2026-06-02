import { create } from "zustand";
import { persist } from "zustand/middleware";
import Cookies from "js-cookie";
import { User, Role } from "@/types";

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;

  setAuth: (user: User, accessToken: string, refreshToken: string) => void;
  clearAuth: () => void;
  setLoading: (loading: boolean) => void;

  // Permission helpers
  hasRole: (...roles: Role[]) => boolean;
  isAdmin: () => boolean;
  canViewAlerts: () => boolean;
  canSubmitReadings: () => boolean;
  canSubmitReports: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      isAuthenticated: false,
      isLoading: false,

      setAuth: (user, accessToken, refreshToken) => {
        // Store tokens in httpOnly-style cookies (secure + sameSite)
        Cookies.set("accessToken", accessToken, {
          expires: 1 / 96, // 15 min
          secure: process.env.NODE_ENV === "production",
          sameSite: "strict",
        });
        Cookies.set("refreshToken", refreshToken, {
          expires: 7,
          secure: process.env.NODE_ENV === "production",
          sameSite: "strict",
        });
        set({ user, isAuthenticated: true, isLoading: false });
      },

      clearAuth: () => {
        Cookies.remove("accessToken");
        Cookies.remove("refreshToken");
        set({ user: null, isAuthenticated: false });
      },

      setLoading: (loading) => set({ isLoading: loading }),

      // -------------------------------------------------------
      // PERMISSION HELPERS
      // -------------------------------------------------------

      hasRole: (...roles: Role[]) => {
        const { user } = get();
        return user ? roles.includes(user.role) : false;
      },

      isAdmin: () => get().user?.role === "ADMIN",

      canViewAlerts: () =>
        get().hasRole("ADMIN", "GOVERNMENT_OFFICER", "HEALTH_WORKER"),

      canSubmitReadings: () =>
        get().hasRole("ADMIN", "WATER_INSPECTOR"),

      canSubmitReports: () =>
        get().hasRole("ADMIN", "HEALTH_WORKER"),
    }),
    {
      name: "neerveda-auth",
      partialize: (state) => ({ user: state.user, isAuthenticated: state.isAuthenticated }),
    }
  )
);
