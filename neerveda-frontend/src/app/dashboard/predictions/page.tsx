"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { motion } from "framer-motion";
import {
  BrainCircuitIcon,
  SearchIcon,
  TrendingUpIcon,
  TrendingDownIcon,
  MinusIcon,
  AlertTriangleIcon,
  CheckCircleIcon,
} from "lucide-react";
import { predictionsApi } from "@/lib/api";
import { PredictionResponse } from "@/types";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Topbar } from "@/components/layout/Topbar";
import { formatTimestamp, getRiskColor, formatPercentage } from "@/lib/utils";

export default function PredictionsPage() {
  const [villageId, setVillageId] = useState("");
  const [searchId, setSearchId] = useState("");

  const { data, isLoading, isError } = useQuery({
    queryKey: ["prediction", searchId],
    queryFn: () =>
      predictionsApi.getForVillage(searchId).then((r) => r.data.data as PredictionResponse),
    enabled: searchId.length > 0,
  });

  const handleSearch = () => {
    if (villageId.trim()) setSearchId(villageId.trim());
  };

  const getTrendIcon = (trend: string) => {
    if (trend === "IMPROVING") return <TrendingUpIcon className="w-4 h-4 text-green-500" />;
    if (trend === "DEGRADING") return <TrendingDownIcon className="w-4 h-4 text-red-500" />;
    return <MinusIcon className="w-4 h-4 text-amber-500" />;
  };

  return (
    <>
      <Topbar title="AI Predictions" />
      <div className="max-w-5xl mx-auto space-y-6 animate-fade-in">

        <div>
          <h2 className="text-2xl font-bold flex items-center gap-2">
            <BrainCircuitIcon className="w-6 h-6 text-purple-600" />
            Disease Outbreak Prediction
          </h2>
          <p className="text-muted-foreground text-sm mt-1">
            AI models (Random Forest · ARIMA · Isolation Forest) predict outbreak risks up to 7 days ahead.
          </p>
        </div>

        {/* Search */}
        <Card>
          <CardContent className="p-4">
            <div className="flex gap-3">
              <Input
                placeholder="Enter Village ID (e.g., VIL001)"
                value={villageId}
                onChange={(e) => setVillageId(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                className="flex-1"
              />
              <Button onClick={handleSearch} className="gap-2" disabled={!villageId.trim()}>
                <SearchIcon className="w-4 h-4" />
                Predict
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Results */}
        {isLoading && (
          <div className="space-y-4">
            <div className="h-32 rounded-xl bg-muted animate-pulse" />
            <div className="grid md:grid-cols-3 gap-4">
              {[...Array(3)].map((_, i) => (
                <div key={i} className="h-32 rounded-xl bg-muted animate-pulse" />
              ))}
            </div>
          </div>
        )}

        {isError && (
          <Card>
            <CardContent className="py-8 text-center text-muted-foreground">
              <AlertTriangleIcon className="w-8 h-8 mx-auto mb-3 text-amber-500" />
              <p>No data found for village ID: {searchId}</p>
            </CardContent>
          </Card>
        )}

        {data && (
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className="space-y-6"
          >
            {/* Risk Overview */}
            <Card className={`border-2 ${
              data.overallRiskLevel === "CRITICAL"
                ? "border-red-500"
                : data.overallRiskLevel === "HIGH"
                ? "border-orange-500"
                : data.overallRiskLevel === "MEDIUM"
                ? "border-amber-400"
                : "border-green-400"
            }`}>
              <CardContent className="p-6">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                  <div>
                    <p className="text-sm text-muted-foreground">Overall Risk Assessment</p>
                    <h3 className="text-3xl font-extrabold mt-1">
                      <span className={getRiskColor(data.overallRiskLevel)}>
                        {data.overallRiskLevel}
                      </span>
                      <span className="text-lg font-normal text-muted-foreground ml-2">
                        ({(data.overallRiskScore * 100).toFixed(0)}% risk score)
                      </span>
                    </h3>
                    <p className="text-base font-medium mt-1">
                      {data.villageName} · {data.district}, {data.state}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-xs text-muted-foreground">Model: {data.modelVersion}</p>
                    <p className="text-xs text-muted-foreground">
                      Generated: {formatTimestamp(data.generatedAt)}
                    </p>
                    {data.immediateAction && (
                      <div className="mt-2 p-2 rounded-lg bg-red-50 dark:bg-red-950 border border-red-200">
                        <p className="text-xs font-medium text-red-700 dark:text-red-400">
                          ⚠️ {data.immediateAction}
                        </p>
                      </div>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Stats Grid */}
            <div className="grid md:grid-cols-3 gap-4">
              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm">Outbreak Probability</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className={`text-3xl font-bold ${getRiskColor(data.overallRiskLevel)}`}>
                    {formatPercentage(data.outbreakProbability)}
                  </p>
                  <p className="text-xs text-muted-foreground mt-1">
                    Timeframe: {data.outbreakTimeframe}
                  </p>
                  {data.predictedDiseases.length > 0 && (
                    <div className="flex flex-wrap gap-1 mt-2">
                      {data.predictedDiseases.map((d) => (
                        <Badge key={d} variant="warning" className="text-xs">{d}</Badge>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm">Water Quality Trend</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2 mt-1">
                    {getTrendIcon(data.waterQualityTrend)}
                    <span className="text-lg font-semibold">{data.waterQualityTrend}</span>
                  </div>
                  {data.anomalyDetected && (
                    <div className="mt-3 p-2 rounded-lg bg-amber-50 dark:bg-amber-950">
                      <p className="text-xs text-amber-700 dark:text-amber-400">
                        ⚠️ Anomaly: {data.anomalyDescription}
                      </p>
                    </div>
                  )}
                  {!data.anomalyDetected && (
                    <p className="text-xs text-green-600 mt-2 flex items-center gap-1">
                      <CheckCircleIcon className="w-3 h-3" /> No anomalies detected
                    </p>
                  )}
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm">Recommendations</CardTitle>
                </CardHeader>
                <CardContent>
                  <ul className="space-y-1.5">
                    {data.recommendations.slice(0, 4).map((rec, i) => (
                      <li key={i} className="text-xs text-muted-foreground flex gap-1.5">
                        <span className="text-nv-blue-600 font-bold flex-shrink-0">•</span>
                        {rec}
                      </li>
                    ))}
                  </ul>
                </CardContent>
              </Card>
            </div>
          </motion.div>
        )}

        {/* Empty state */}
        {!searchId && (
          <Card>
            <CardContent className="py-16 text-center">
              <BrainCircuitIcon className="w-12 h-12 mx-auto text-purple-400 mb-4" />
              <p className="font-medium text-lg">Enter a Village ID to Run Prediction</p>
              <p className="text-muted-foreground text-sm mt-2">
                The AI will analyse sensor data and symptom reports to forecast outbreak risks.
              </p>
            </CardContent>
          </Card>
        )}
      </div>
    </>
  );
}
