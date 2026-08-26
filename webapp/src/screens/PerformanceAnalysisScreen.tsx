import { useState } from "react";
import { useApp } from "../store";
import { PRACTICE_MODES } from "../types";
import PerformanceChart from "../components/PerformanceChart";

type Metric = "avg" | "total";

const MONTHS = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

function fmtDate(ts: number): string {
  const d = new Date(ts);
  return `${MONTHS[d.getMonth()]} ${d.getDate()}, ${d.getFullYear()} • ${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
}

export default function PerformanceAnalysisScreen() {
  const { state, navigate } = useApp();
  const dest = state.destination;
  if (dest.type !== "analysis") return null;

  const [selectedMode, setSelectedMode] = useState(dest.mode);
  const [metric, setMetric] = useState<Metric>("avg");

  const modeSessions = state.sessions
    .filter((s) => s.mode === selectedMode)
    .sort((a, b) => a.timestamp - b.timestamp);

  // Last session calculations
  const lastSession = modeSessions.length > 0 ? modeSessions[modeSessions.length - 1] : null;
  const lastQuestionsCount = Math.max(lastSession?.totalQuestions ?? 1, 1);
  const lastTotalTime =
    selectedMode === dest.mode && dest.lastCompletionTime && dest.lastCompletionTime > 0
      ? dest.lastCompletionTime
      : lastSession?.totalTimeSec ?? null;
  const lastAvgTimePerQ = lastTotalTime != null ? lastTotalTime / lastQuestionsCount : null;

  // Personal best calculations
  const bestAvgTimePerQ =
    modeSessions.length > 0
      ? Math.min(...modeSessions.map((s) => s.totalTimeSec / Math.max(s.totalQuestions, 1)))
      : lastAvgTimePerQ;
  const bestTotalTime =
    modeSessions.length > 0 ? Math.min(...modeSessions.map((s) => s.totalTimeSec)) : lastTotalTime;

  const totalQuestionsInMode = modeSessions.reduce((sum, s) => sum + s.totalQuestions, 0);

  // Chart data (last 20 sessions)
  const unit = metric === "avg" ? "s/q" : "s";
  const chartData: [number, number][] = modeSessions.slice(-20).map((s) => [
    s.timestamp,
    metric === "avg" ? s.totalTimeSec / Math.max(s.totalQuestions, 1) : s.totalTimeSec,
  ]);

  const isAvg = metric === "avg";

  return (
    <div className="min-h-screen bg-slate-50 px-4 py-3 max-w-lg mx-auto flex flex-col">
      <div className="flex-1 overflow-y-auto">
        {/* Top bar */}
        <div className="flex items-center gap-2 py-1">
          <button onClick={() => navigate(dest.backDestination)} className="text-slate-700 text-2xl p-1 hover:bg-slate-100 rounded-full">←</button>
          <div>
            <h1 className="text-xl font-black text-slate-900">Performance Analysis</h1>
            <p className="text-[11px] text-slate-500">Normalized speed analysis per question</p>
          </div>
        </div>

        {/* Mode selector */}
        <div className="flex gap-1.5 overflow-x-auto mt-3 pb-1">
          {PRACTICE_MODES.map((m) => (
            <button
              key={m.id}
              onClick={() => setSelectedMode(m.id)}
              className={`whitespace-nowrap rounded-xl px-3 h-9 text-xs font-bold transition-colors ${
                selectedMode === m.id ? "bg-primary-indigo text-white" : "bg-white border border-slate-200 text-slate-700"
              }`}
            >
              {m.title.replace(" Practice", "")}
            </button>
          ))}
        </div>

        {/* Metric toggle: Avg Speed / Question vs Total Duration */}
        <div className="bg-slate-100 rounded-2xl p-1 mt-3 flex">
          <button
            onClick={() => setMetric("avg")}
            className={`flex-1 rounded-xl h-9 flex items-center justify-center gap-1.5 text-xs font-bold transition-colors ${
              isAvg ? "bg-white shadow text-primary-indigo" : "text-slate-600"
            }`}
          >
            <span className={isAvg ? "text-primary-indigo" : "text-slate-400"}>⚡</span>
            Avg Speed / Question
          </button>
          <button
            onClick={() => setMetric("total")}
            className={`flex-1 rounded-xl h-9 flex items-center justify-center gap-1.5 text-xs font-bold transition-colors ${
              !isAvg ? "bg-white shadow text-primary-indigo" : "text-slate-600"
            }`}
          >
            <span className={!isAvg ? "text-primary-indigo" : "text-slate-400"}>⏱</span>
            Total Duration
          </button>
        </div>

        {/* Summary metric cards */}
        <div className="grid grid-cols-3 gap-2 mt-3">
          {/* Last speed / time */}
          <div className="bg-blue-50 border border-blue-200 rounded-2xl py-3 text-center">
            <p className="text-[10px] font-black text-blue-600 tracking-widest">{isAvg ? "LAST SPEED" : "LAST TIME"}</p>
            <p className="text-xl font-black mono text-blue-600 mt-0.5">
              {isAvg
                ? lastAvgTimePerQ != null ? `${lastAvgTimePerQ.toFixed(2)} ${unit}` : "--"
                : lastTotalTime != null ? `${lastTotalTime.toFixed(2)} ${unit}` : "--"}
            </p>
            {lastSession != null && isAvg && (
              <p className="text-[10px] font-bold text-slate-500">{lastSession.totalQuestions} Qs • {lastSession.totalTimeSec.toFixed(1)}s</p>
            )}
          </div>

          {/* Personal best */}
          <div className="bg-rose-50 border border-rose-200 rounded-2xl py-3 text-center">
            <p className="text-[10px] font-black text-rose-500 tracking-widest">{isAvg ? "BEST SPEED" : "BEST TIME"}</p>
            <p className="text-xl font-black mono text-rose-500 mt-0.5">
              {isAvg
                ? bestAvgTimePerQ != null ? `${bestAvgTimePerQ.toFixed(2)} ${unit}` : "--"
                : bestTotalTime != null ? `${bestTotalTime.toFixed(2)} ${unit}` : "--"}
            </p>
            <p className="text-[10px] font-bold text-slate-500">{isAvg ? "Fastest pace" : "Shortest session"}</p>
          </div>

          {/* Total solved */}
          <div className="bg-emerald-50 border border-emerald-200 rounded-2xl py-3 text-center">
            <p className="text-[10px] font-black text-emerald-600 tracking-widest">SOLVED</p>
            <p className="text-xl font-black mono text-emerald-600 mt-0.5">{totalQuestionsInMode}</p>
            <p className="text-[10px] font-bold text-slate-500">{modeSessions.length} sessions</p>
          </div>
        </div>

        {/* Clarification note */}
        {isAvg && (
          <div className="bg-indigo-50 border border-indigo-200 rounded-xl px-3 py-2 mt-3 flex items-start gap-2">
            <span className="text-primary-indigo text-base">⚡</span>
            <p className="text-[11px] font-medium text-primary-indigo leading-snug">
              Fair Speed Tracking: Normalizes time taken by question count (e.g. 20 Qs in 2m and 40 Qs in 4m both reflect 6.00 s/q).
            </p>
          </div>
        )}

        {/* Chart */}
        <p className="text-[11px] font-black text-slate-500 tracking-widest mt-4 mb-2 pl-1">
          {isAvg ? "AVERAGE SPEED PROGRESSION (SECONDS / QUESTION)" : "TOTAL DURATION PROGRESSION (SECONDS)"}
        </p>
        <PerformanceChart times={chartData} unit={unit} />

        {/* Session history */}
        {modeSessions.length > 0 && (
          <>
            <p className="text-[11px] font-black text-slate-500 tracking-widest mt-4 mb-2 pl-1">
              EXAM BREAKDOWN & ATTEMPTS ({modeSessions.length})
            </p>
            <div className="space-y-2">
              {[...modeSessions].reverse().map((session) => {
                const qCount = Math.max(session.totalQuestions, 1);
                const avgSec = session.totalTimeSec / qCount;
                return (
                  <div key={session.id} className="bg-white border border-slate-200 rounded-2xl p-3.5 flex items-center justify-between">
                    <div className="min-w-0">
                      <p className="text-[11px] text-slate-400 font-semibold">{fmtDate(session.timestamp)}</p>
                      <div className="flex items-center gap-1.5 mt-0.5">
                        <span className="text-[13px] font-bold text-slate-800">{qCount} questions</span>
                        <span className="text-slate-400 text-xs">•</span>
                        <span className="text-xs font-semibold text-slate-600">{session.totalTimeSec.toFixed(2)} s total</span>
                      </div>
                    </div>
                    <div className="bg-blue-50 border border-blue-200 rounded-xl px-2.5 py-1.5 text-right shrink-0">
                      <p className="text-sm font-black mono text-primary-indigo">{avgSec.toFixed(2)} s/q</p>
                      <p className="text-[9px] font-bold text-slate-500">avg pace</p>
                    </div>
                  </div>
                );
              })}
            </div>
          </>
        )}
      </div>

      {/* Exit */}
      <button
        onClick={() => navigate({ type: "home" })}
        className="w-full h-13 py-3.5 bg-slate-900 text-white rounded-2xl font-bold text-[15px] mt-4 mb-2 hover:bg-slate-800"
      >
        Back to Home
      </button>
    </div>
  );
}