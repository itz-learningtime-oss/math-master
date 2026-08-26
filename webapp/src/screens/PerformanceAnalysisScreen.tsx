import { useState } from "react";
import { useApp } from "../store";
import { PRACTICE_MODES } from "../types";
import PerformanceChart from "../components/PerformanceChart";

export default function PerformanceAnalysisScreen() {
  const { state, navigate } = useApp();
  const dest = state.destination;
  if (dest.type !== "analysis") return null;

  const [selectedMode, setSelectedMode] = useState(dest.mode);

  const modeSessions = state.sessions
    .filter((s) => s.mode === selectedMode)
    .sort((a, b) => a.timestamp - b.timestamp);

  // Average seconds per question (accounts for different session sizes)
  const avgPerQ = (s: { totalTimeSec: number; totalQuestions: number }) =>
    s.totalTimeSec / Math.max(s.totalQuestions, 1);

  const lastSession = modeSessions.length > 0 ? modeSessions[modeSessions.length - 1] : null;
  const lastTime = lastSession ? avgPerQ(lastSession) : null;

  const bestTime = modeSessions.length > 0 ? Math.min(...modeSessions.map(avgPerQ)) : lastTime;

  const chartData: [number, number][] = modeSessions.slice(-15).map((s) => [s.timestamp, avgPerQ(s)]);

  return (
    <div className="min-h-screen bg-slate-50 px-4 py-3 max-w-lg mx-auto flex flex-col">
      <div className="flex-1">
        {/* Top bar */}
        <div className="flex items-center gap-2 py-1">
          <button onClick={() => navigate(dest.backDestination)} className="text-slate-700 text-2xl p-1 hover:bg-slate-100 rounded-full">←</button>
          <h1 className="text-xl font-black text-slate-900">Performance Analysis</h1>
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

        {/* Last vs Best */}
        <div className="flex gap-2.5 mt-4">
          <div className="flex-1 bg-blue-50 border border-blue-200 rounded-2xl py-4 text-center">
            <p className="text-[10px] font-black text-blue-600 tracking-widest">LAST TIME / Q</p>
            <p className="text-2xl font-black mono text-blue-600 mt-1">{lastTime != null ? `${lastTime.toFixed(2)}s` : "--"}</p>
          </div>
          <div className="flex-1 bg-rose-50 border border-rose-200 rounded-2xl py-4 text-center">
            <p className="text-[10px] font-black text-rose-500 tracking-widest">BEST / Q</p>
            <p className="text-2xl font-black mono text-rose-500 mt-1">{bestTime != null ? `${bestTime.toFixed(2)}s` : "--"}</p>
          </div>
        </div>

        {/* Chart */}
        <p className="text-[11px] font-black text-slate-500 tracking-widest mt-4 mb-2 pl-1">SPEED & TIMING PROGRESSION (s / question)</p>
        <PerformanceChart times={chartData} />
      </div>

      <button
        onClick={() => navigate({ type: "home" })}
        className="w-full h-13 py-3.5 bg-slate-900 text-white rounded-2xl font-bold text-[15px] mt-4 mb-2 hover:bg-slate-800"
      >
        Exit Analysis
      </button>
    </div>
  );
}