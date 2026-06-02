"use client";

import Link from "next/link";
import { motion } from "framer-motion";
import {
  DropletIcon,
  ShieldCheckIcon,
  BrainCircuitIcon,
  BellRingIcon,
  MapPinIcon,
  ActivityIcon,
  ArrowRightIcon,
  CheckCircleIcon,
  RadioIcon,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";

const features = [
  {
    icon: DropletIcon,
    color: "text-nv-blue-600",
    bg: "bg-nv-blue-50 dark:bg-nv-blue-950",
    title: "Real-Time IoT Monitoring",
    description:
      "ESP32 sensors transmit pH, TDS, turbidity and temperature data every 15 minutes from 500+ villages across India.",
  },
  {
    icon: BrainCircuitIcon,
    color: "text-purple-600",
    bg: "bg-purple-50 dark:bg-purple-950",
    title: "AI Outbreak Prediction",
    description:
      "Machine learning models (Random Forest, ARIMA, Isolation Forest) predict disease outbreaks 7 days before they occur.",
  },
  {
    icon: BellRingIcon,
    color: "text-red-600",
    bg: "bg-red-50 dark:bg-red-950",
    title: "Instant SMS Alerts",
    description:
      "Twilio-powered SMS alerts reach health workers, ASHA workers and government officers within seconds of a safety breach.",
  },
  {
    icon: MapPinIcon,
    color: "text-nv-teal-600",
    bg: "bg-nv-teal-50 dark:bg-nv-teal-950",
    title: "Interactive Village Map",
    description:
      "Leaflet + OpenStreetMap heatmap shows contamination zones, safe villages and active alerts across the country.",
  },
  {
    icon: ShieldCheckIcon,
    color: "text-green-600",
    bg: "bg-green-50 dark:bg-green-950",
    title: "Enterprise-Grade Security",
    description:
      "JWT + RBAC with BCrypt, rate limiting (Bucket4j), HSTS, CSP headers, and complete audit logging.",
  },
  {
    icon: ActivityIcon,
    color: "text-amber-600",
    bg: "bg-amber-50 dark:bg-amber-950",
    title: "Disease Surveillance",
    description:
      "ASHA workers submit symptom reports from the field. Cluster analysis automatically triggers outbreak alerts.",
  },
];

const stats = [
  { value: "500+", label: "Villages Monitored" },
  { value: "10K+", label: "Daily Sensor Readings" },
  { value: "<30s", label: "Alert Response Time" },
  { value: "99.9%", label: "System Uptime" },
];

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-background">
      {/* ---- NAVBAR ---- */}
      <nav className="sticky top-0 z-50 bg-background/80 backdrop-blur-sm border-b border-border">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-nv-blue-600 flex items-center justify-center">
              <DropletIcon className="w-4 h-4 text-white" />
            </div>
            <div>
              <span className="font-bold text-base">NeerVeda</span>
              <Badge variant="info" className="ml-2 text-[10px]">
                SIH25001
              </Badge>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <Link href="/auth/login">
              <Button variant="ghost" size="sm">Sign In</Button>
            </Link>
            <Link href="/auth/register">
              <Button size="sm">Get Access</Button>
            </Link>
          </div>
        </div>
      </nav>

      {/* ---- HERO ---- */}
      <section className="relative overflow-hidden py-24 px-6">
        {/* Background */}
        <div className="absolute inset-0 -z-10">
          <div className="absolute top-0 left-1/4 w-96 h-96 bg-nv-blue-400/10 rounded-full blur-3xl" />
          <div className="absolute bottom-0 right-1/4 w-96 h-96 bg-nv-teal-400/10 rounded-full blur-3xl" />
        </div>

        <div className="max-w-5xl mx-auto text-center">
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
          >
            <Badge variant="info" className="mb-6 text-sm px-4 py-1.5">
              🏆 Smart India Hackathon 2025 · Team CORE_401
            </Badge>

            <h1 className="text-5xl md:text-7xl font-extrabold tracking-tight leading-none mb-6">
              <span className="text-nv-blue-700 dark:text-nv-blue-400">Neer</span>
              <span>Veda</span>
            </h1>
            <p className="text-xl md:text-2xl font-medium text-muted-foreground mb-4">
              Smart Water Safety & Disease Prevention System
            </p>
            <p className="text-base text-muted-foreground max-w-2xl mx-auto mb-10 leading-relaxed">
              AI-powered real-time water quality monitoring for rural India.
              Protect communities from waterborne diseases before they strike.
              Built on IoT sensors, machine learning, and instant alerting.
            </p>

            <div className="flex flex-wrap items-center justify-center gap-4">
              <Link href="/auth/login">
                <Button size="xl" className="gap-2">
                  Access Platform
                  <ArrowRightIcon className="w-4 h-4" />
                </Button>
              </Link>
              <Link href="#features">
                <Button size="xl" variant="outline">
                  See Features
                </Button>
              </Link>
            </div>
          </motion.div>
        </div>
      </section>

      {/* ---- STATS ---- */}
      <section className="py-16 px-6 border-y border-border bg-muted/30">
        <div className="max-w-4xl mx-auto grid grid-cols-2 md:grid-cols-4 gap-8">
          {stats.map((stat, i) => (
            <motion.div
              key={stat.label}
              initial={{ opacity: 0, scale: 0.9 }}
              whileInView={{ opacity: 1, scale: 1 }}
              transition={{ delay: i * 0.1 }}
              viewport={{ once: true }}
              className="text-center"
            >
              <p className="text-4xl font-extrabold text-nv-blue-600">{stat.value}</p>
              <p className="text-sm text-muted-foreground mt-1">{stat.label}</p>
            </motion.div>
          ))}
        </div>
      </section>

      {/* ---- FEATURES ---- */}
      <section id="features" className="py-24 px-6">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold mb-4">Built for India's Water Crisis</h2>
            <p className="text-muted-foreground text-lg max-w-2xl mx-auto">
              Every feature designed around the Jal Jeevan Mission and WHO water safety guidelines.
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {features.map((f, i) => (
              <motion.div
                key={f.title}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.1 }}
                viewport={{ once: true }}
                className="p-6 rounded-2xl border border-border bg-card hover:shadow-md transition-shadow"
              >
                <div className={`w-12 h-12 rounded-xl ${f.bg} flex items-center justify-center mb-4`}>
                  <f.icon className={`w-5 h-5 ${f.color}`} />
                </div>
                <h3 className="font-semibold text-base mb-2">{f.title}</h3>
                <p className="text-sm text-muted-foreground leading-relaxed">
                  {f.description}
                </p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* ---- HOW IT WORKS ---- */}
      <section className="py-24 px-6 bg-muted/30 border-y border-border">
        <div className="max-w-5xl mx-auto">
          <h2 className="text-4xl font-bold text-center mb-16">How NeerVeda Works</h2>
          <div className="grid md:grid-cols-4 gap-8">
            {[
              { icon: RadioIcon, step: "1", title: "Sensors Detect", desc: "ESP32 IoT sensors measure pH, TDS, turbidity & temperature every 15 min." },
              { icon: DropletIcon, step: "2", title: "AI Analyses", desc: "Spring Boot backend + ML models evaluate data against WHO thresholds." },
              { icon: BellRingIcon, step: "3", title: "Alerts Fire", desc: "Twilio SMS and in-app notifications reach responders within 30 seconds." },
              { icon: CheckCircleIcon, step: "4", title: "Action Taken", desc: "Health workers get recommendations. Lives are protected." },
            ].map((item) => (
              <div key={item.step} className="flex flex-col items-center text-center">
                <div className="w-14 h-14 rounded-full bg-nv-blue-600 text-white flex items-center justify-center text-xl font-bold mb-4 shadow-lg">
                  {item.step}
                </div>
                <h3 className="font-semibold mb-2">{item.title}</h3>
                <p className="text-sm text-muted-foreground">{item.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ---- CTA ---- */}
      <section className="py-24 px-6">
        <div className="max-w-2xl mx-auto text-center">
          <h2 className="text-4xl font-bold mb-4">Ready to Protect Your District?</h2>
          <p className="text-muted-foreground mb-8">
            Join government officers, health workers and water inspectors already using NeerVeda.
          </p>
          <Link href="/auth/register">
            <Button size="xl" className="gap-2">
              Request Access
              <ArrowRightIcon className="w-4 h-4" />
            </Button>
          </Link>
        </div>
      </section>

      {/* ---- FOOTER ---- */}
      <footer className="border-t border-border py-10 px-6">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4 text-sm text-muted-foreground">
          <div className="flex items-center gap-2">
            <DropletIcon className="w-4 h-4 text-nv-blue-600" />
            <span>NeerVeda · Neer + Veda = Water + Knowledge</span>
          </div>
          <p>Smart India Hackathon 2025 · Problem ID: SIH25001 · Team CORE_401</p>
          <p>Built with Spring Boot · Next.js · Firebase · Twilio</p>
        </div>
      </footer>
    </div>
  );
}
