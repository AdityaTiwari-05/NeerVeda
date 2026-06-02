import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { Providers } from "./providers";

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
  display: "swap",
});

export const metadata: Metadata = {
  title: {
    default: "NeerVeda — Smart Water Safety Platform",
    template: "%s | NeerVeda",
  },
  description:
    "NeerVeda: AI-powered smart water safety and disease prevention system. " +
    "Real-time water quality monitoring, outbreak prediction, and alerting for rural India. " +
    "Team CORE_401 | Smart India Hackathon 2025 | Problem ID: SIH25001",
  keywords: [
    "water quality", "IoT", "disease prevention", "Smart India Hackathon",
    "rural India", "water safety", "NeerVeda", "CORE_401",
  ],
  authors: [{ name: "Team CORE_401" }],
  openGraph: {
    title: "NeerVeda — Smart Water Safety Platform",
    description:
      "AI-powered water quality monitoring and disease outbreak prevention for rural India",
    type: "website",
  },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className={`${inter.variable} font-sans antialiased`}>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
