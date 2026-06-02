"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { motion } from "framer-motion";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { DropletIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useAuthStore } from "@/store/authStore";
import { authApi } from "@/lib/api";
import toast from "react-hot-toast";
import { Role } from "@/types";

const schema = z.object({
  name: z.string().min(2, "Name must be at least 2 characters"),
  email: z.string().email("Invalid email"),
  password: z
    .string()
    .min(8, "Password must be at least 8 characters")
    .regex(
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/,
      "Must contain uppercase, lowercase, number, and special character"
    ),
  role: z.enum([
    "ADMIN",
    "GOVERNMENT_OFFICER",
    "HEALTH_WORKER",
    "WATER_INSPECTOR",
    "PUBLIC_VIEWER",
  ]),
  phone: z.string().optional(),
  district: z.string().optional(),
  state: z.string().optional(),
});

type RegisterForm = z.infer<typeof schema>;

const roles: { value: Role; label: string; description: string }[] = [
  { value: "GOVERNMENT_OFFICER", label: "Government Officer", description: "View all data, manage alerts and reports" },
  { value: "HEALTH_WORKER", label: "Health Worker / ASHA", description: "Submit and review symptom reports" },
  { value: "WATER_INSPECTOR", label: "Water Inspector", description: "Submit sensor readings and monitor quality" },
  { value: "PUBLIC_VIEWER", label: "Public Viewer", description: "Read-only access to public dashboard" },
];

export default function RegisterPage() {
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();
  const { setAuth } = useAuthStore();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterForm>({ resolver: zodResolver(schema) });

  const onSubmit = async (data: RegisterForm) => {
    setIsLoading(true);
    try {
      const res = await authApi.register(data);
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
      toast.success("Account created successfully!");
      router.push("/dashboard");
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      toast.error(error?.response?.data?.message || "Registration failed.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-nv-blue-50 to-white dark:from-gray-950 dark:to-gray-900 p-6">
      <motion.div
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.3 }}
        className="w-full max-w-lg"
      >
        <div className="bg-card rounded-2xl border border-border p-8 shadow-sm">
          <div className="flex items-center gap-2 mb-8">
            <div className="w-8 h-8 rounded-lg bg-nv-blue-600 flex items-center justify-center">
              <DropletIcon className="w-4 h-4 text-white" />
            </div>
            <span className="font-bold">NeerVeda</span>
          </div>

          <h2 className="text-2xl font-bold mb-1">Request Access</h2>
          <p className="text-muted-foreground text-sm mb-8">
            Create your NeerVeda account. Admin approval may be required.
          </p>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="col-span-2 space-y-1.5">
                <label className="text-sm font-medium">Full Name</label>
                <Input placeholder="Dr. Priya Sharma" {...register("name")} error={errors.name?.message} />
              </div>

              <div className="col-span-2 space-y-1.5">
                <label className="text-sm font-medium">Work Email</label>
                <Input type="email" placeholder="priya@health.gov.in" {...register("email")} error={errors.email?.message} />
              </div>

              <div className="col-span-2 space-y-1.5">
                <label className="text-sm font-medium">Password</label>
                <Input type="password" placeholder="Min 8 chars, incl. A–Z, 0–9, symbol" {...register("password")} error={errors.password?.message} />
              </div>

              <div className="space-y-1.5">
                <label className="text-sm font-medium">Phone</label>
                <Input placeholder="+91 98765 43210" {...register("phone")} />
              </div>

              <div className="space-y-1.5">
                <label className="text-sm font-medium">District</label>
                <Input placeholder="Dimapur" {...register("district")} />
              </div>

              <div className="col-span-2 space-y-1.5">
                <label className="text-sm font-medium">Role</label>
                <select
                  {...register("role")}
                  className="w-full h-10 rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                >
                  <option value="">Select your role</option>
                  {roles.map((r) => (
                    <option key={r.value} value={r.value}>
                      {r.label}
                    </option>
                  ))}
                </select>
                {errors.role && (
                  <p className="text-xs text-destructive">{errors.role.message}</p>
                )}
              </div>
            </div>

            <Button type="submit" className="w-full" size="lg" loading={isLoading}>
              Create Account
            </Button>
          </form>

          <p className="text-center text-sm text-muted-foreground mt-6">
            Already have access?{" "}
            <Link href="/auth/login" className="text-nv-blue-600 font-medium hover:underline">
              Sign in
            </Link>
          </p>
        </div>
      </motion.div>
    </div>
  );
}
