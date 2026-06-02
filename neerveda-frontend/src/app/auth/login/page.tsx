"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { motion } from "framer-motion";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { DropletIcon, EyeIcon, EyeOffIcon, LockIcon, MailIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useAuthStore } from "@/store/authStore";
import { authApi } from "@/lib/api";
import toast from "react-hot-toast";

const loginSchema = z.object({
  email: z.string().email("Invalid email"),
  password: z.string().min(6, "Password must be at least 6 characters"),
});

type LoginForm = z.infer<typeof loginSchema>;

export default function LoginPage() {
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();
  const { setAuth } = useAuthStore();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginForm>({ resolver: zodResolver(loginSchema) });

  const onSubmit = async (data: LoginForm) => {
    setIsLoading(true);
    try {
      const res = await authApi.login(data.email, data.password);
      const authData = res.data.data;
      setAuth(
        {
          id: authData.userId,
          name: authData.name,
          email: authData.email,
          role: authData.role,
          active: true,
        },
        authData.accessToken,
        authData.refreshToken
      );
      toast.success(`Welcome back, ${authData.name}!`);
      router.push("/dashboard");
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      toast.error(error?.response?.data?.message || "Login failed. Check your credentials.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex bg-gradient-to-br from-nv-blue-50 to-white dark:from-gray-950 dark:to-gray-900">

      {/* Left — Branding Panel */}
      <div className="hidden lg:flex w-1/2 bg-nv-blue-700 p-12 flex-col justify-between relative overflow-hidden">
        {/* Background pattern */}
        <div className="absolute inset-0 opacity-10">
          {[...Array(6)].map((_, i) => (
            <div
              key={i}
              className="absolute rounded-full border border-white"
              style={{
                width: `${(i + 1) * 120}px`,
                height: `${(i + 1) * 120}px`,
                top: "50%",
                left: "50%",
                transform: "translate(-50%, -50%)",
              }}
            />
          ))}
        </div>

        <div className="relative">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-10 h-10 rounded-xl bg-white/20 flex items-center justify-center">
              <DropletIcon className="w-5 h-5 text-white" />
            </div>
            <span className="text-white font-bold text-xl">NeerVeda</span>
          </div>
          <p className="text-nv-blue-200 text-sm">
            Smart Water Safety & Disease Prevention
          </p>
        </div>

        <div className="relative space-y-6">
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
          >
            <h1 className="text-4xl font-bold text-white leading-tight">
              Protecting Lives
              <br />
              Through Water Safety
            </h1>
            <p className="text-nv-blue-200 mt-4 text-lg leading-relaxed">
              AI-powered real-time monitoring of water quality across rural India.
              Early detection. Instant alerts. Zero compromise.
            </p>
          </motion.div>

          <div className="grid grid-cols-3 gap-4">
            {[
              { label: "Villages", value: "500+" },
              { label: "Daily Readings", value: "10K+" },
              { label: "Lives Protected", value: "2M+" },
            ].map((stat) => (
              <div key={stat.label} className="bg-white/10 rounded-xl p-4 text-center">
                <p className="text-white font-bold text-xl">{stat.value}</p>
                <p className="text-nv-blue-200 text-xs mt-1">{stat.label}</p>
              </div>
            ))}
          </div>
        </div>

        <div className="relative">
          <p className="text-nv-blue-300 text-xs">
            Smart India Hackathon 2025 · Problem ID: SIH25001 · Team CORE_401
          </p>
        </div>
      </div>

      {/* Right — Login Form */}
      <div className="flex-1 flex items-center justify-center p-8">
        <motion.div
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.3 }}
          className="w-full max-w-md"
        >
          {/* Mobile logo */}
          <div className="lg:hidden flex items-center gap-2 mb-8">
            <DropletIcon className="w-6 h-6 text-nv-blue-600" />
            <span className="font-bold text-lg">NeerVeda</span>
          </div>

          <div className="bg-card rounded-2xl border border-border p-8 shadow-sm">
            <div className="mb-8">
              <h2 className="text-2xl font-bold">Sign in</h2>
              <p className="text-muted-foreground text-sm mt-1">
                Access the NeerVeda monitoring platform
              </p>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              {/* Email */}
              <div className="space-y-1.5">
                <label className="text-sm font-medium" htmlFor="email">
                  Email address
                </label>
                <div className="relative">
                  <MailIcon className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                  <Input
                    id="email"
                    type="email"
                    placeholder="officer@example.gov.in"
                    className="pl-10"
                    autoComplete="email"
                    {...register("email")}
                    error={errors.email?.message}
                  />
                </div>
              </div>

              {/* Password */}
              <div className="space-y-1.5">
                <div className="flex items-center justify-between">
                  <label className="text-sm font-medium" htmlFor="password">
                    Password
                  </label>
                  <Link
                    href="/auth/forgot-password"
                    className="text-xs text-nv-blue-600 hover:underline"
                  >
                    Forgot password?
                  </Link>
                </div>
                <div className="relative">
                  <LockIcon className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                  <Input
                    id="password"
                    type={showPassword ? "text" : "password"}
                    placeholder="Enter your password"
                    className="pl-10 pr-10"
                    autoComplete="current-password"
                    {...register("password")}
                    error={errors.password?.message}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                    aria-label="Toggle password visibility"
                  >
                    {showPassword ? (
                      <EyeOffIcon className="w-4 h-4" />
                    ) : (
                      <EyeIcon className="w-4 h-4" />
                    )}
                  </button>
                </div>
              </div>

              <Button
                type="submit"
                className="w-full"
                size="lg"
                loading={isLoading}
              >
                Sign in
              </Button>
            </form>

            <div className="mt-6 text-center">
              <p className="text-sm text-muted-foreground">
                Don&apos;t have an account?{" "}
                <Link
                  href="/auth/register"
                  className="text-nv-blue-600 font-medium hover:underline"
                >
                  Request access
                </Link>
              </p>
            </div>

            {/* Demo credentials */}
            <div className="mt-6 p-3 rounded-lg bg-muted text-xs text-muted-foreground">
              <p className="font-medium text-foreground mb-1">Demo Credentials</p>
              <p>admin@neerveda.gov.in / Admin@1234</p>
            </div>
          </div>
        </motion.div>
      </div>
    </div>
  );
}
