import { useState } from "react";
import { useApp } from "../store";
import { PRACTICE_MODES } from "../types";
import { daysSinceEpoch } from "../storage";
import ShareAppDialog from "../components/ShareAppDialog";

export default function HomeScreen() {
  const { state, navigate, dispatch } = useApp();
  const [showShareDialog, setShowShareDialog] = useState(false);

  const todayEpochDay = daysSinceEpoch(Date.now());
  const todayQuestions = state.sessions
    .filter((s) => daysSinceEpoch(s.timestamp) === todayEpochDay)
    .reduce((sum, s) => sum + s.totalQuestions, 0);

  const gridSessions = state.sessions.filter((s) => s.mode === "grid");
  const bestGridTime = gridSessions.length > 0 ? Math.min(...gridSessions.map((s) => s.totalTimeSec / Math.max(s.totalQuestions, 1))) : null;

  const userName = state.goal?.userName ?? "";
  const dailyGoalTarget = state.goal?.dailyTargetQuestions ?? 20;
  const currentStreak = state.goal?.currentStreak ?? 0;

  const goalProgress = dailyGoalTarget > 0 ? Math.min(todayQuestions / dailyGoalTarget, 1) : 0;

  const modeColors: Record<string, { color: string; bg: string }> = {
    addition: { color: "text-blue-600", bg: "bg-blue-50" },
    subtraction: { color: "text-rose-600", bg: "bg-rose-50" },
    multiplication: { color: "text-amber-600", bg: "bg-amber-50" },
    tables: { color: "text-primary-indigo", bg: "bg-indigo-50" },
    factors: { color: "text-cyan-700", bg: "bg-cyan-50" },
    division: { color: "text-emerald-600", bg: "bg-emerald-50" },
    complex: { color: "text-purple-600", bg: "bg-purple-50" },
    roots: { color: "text-emerald-600", bg: "bg-emerald-50" },
    grid: { color: "text-rose-600", bg: "bg-slate-700" },
  };

  return (
    <div className="min-h-screen bg-slate-50 px-4 py-3.5 space-y-3.5 max-w-lg mx-auto">
      {/* Developer attribution line */}
      <div
        onClick={() => navigate({ type: "privacy", backDestination: { type: "home" } })}
        className="w-full bg-indigo-50 border border-indigo-200 rounded-xl px-3 py-2 flex items-center justify-between cursor-pointer"
      >
        <div className="flex items-center gap-2 flex-1 min-w-0">
          <div className="w-6 h-6 rounded-full bg-primary-indigo flex items-center justify-center text-white text-xs">◈</div>
          <p className="text-[11px] font-bold text-primary-indigo truncate">
            This app is developed by Vishesh Chaturvedi • Version 3.14
          </p>
        </div>
        <span className="bg-indigo-100 text-primary-indigo text-[10px] font-bold px-1.5 py-0.5 rounded">Policy 📜</span>
      </div>

      {/* Hero card */}
      <div className="w-full bg-slate-900 rounded-3xl p-5 shadow-lg">
        <div className="flex items-center justify-between">
          <button
            onClick={() => dispatch({ type: "OPEN_NAME_PROMPT" })}
            className="flex items-center gap-1.5 bg-slate-800 border border-slate-700 rounded-xl px-2.5 py-1.5"
          >
            <span className="text-cyan-400 text-sm">👤</span>
            <span className="text-white text-xs font-bold">{userName || "Tap to add your Name"}</span>
            <span className="text-slate-400 text-[10px]">✎</span>
          </button>
          {userName && <p className="text-emerald-400 text-[11px] font-bold">Ready to train! ⚡</p>}
        </div>

        <div className="flex items-center justify-between mt-4">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-2xl bg-primary-indigo flex items-center justify-center text-white text-2xl">⚡</div>
            <div>
              <h1 className="text-xl font-black text-white">{userName ? `Hi, ${userName}!` : "Math Master"}</h1>
              <p className="text-xs text-slate-400 font-medium">Speed & Accuracy Trainer</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setShowShareDialog(true)}
              className="flex items-center gap-1 bg-slate-700 rounded-2xl px-2.5 py-1.5"
            >
              <span className="text-cyan-400 text-sm">📤</span>
              <span className="text-white text-xs font-bold">Share</span>
            </button>
            <button
              onClick={() => navigate({ type: "dashboard" })}
              className="flex items-center gap-1 bg-slate-700 rounded-2xl px-2.5 py-1.5"
            >
              <span className="text-amber-400 text-sm">🔥</span>
              <span className="text-white text-xs font-black">{currentStreak}d</span>
            </button>
          </div>
        </div>

        <div className="mt-5 bg-slate-800 rounded-2xl p-3.5 flex items-center justify-between cursor-pointer" onClick={() => navigate({ type: "dashboard" })}>
          <div className="flex-1">
            <div className="flex items-center gap-1.5">
              <p className="text-slate-400 text-xs font-bold">Today's Practice Goal</p>
              {todayQuestions >= dailyGoalTarget && dailyGoalTarget > 0 && <span className="text-emerald-400 text-sm">✓</span>}
            </div>
            <p className="text-white text-sm font-black mt-1">{todayQuestions} / {dailyGoalTarget} problems solved</p>
          </div>
          <div className="relative w-10 h-10">
            <div className="w-10 h-10 rounded-full" style={{ background: `conic-gradient(${goalProgress >= 1 ? "#059669" : "#4F46E5"} ${goalProgress * 360}deg, #334155 0deg)` }} />
            <div className="absolute inset-1.5 bg-slate-800 rounded-full flex items-center justify-center">
              <span className="text-white text-[10px] font-bold">{Math.round(goalProgress * 100)}%</span>
            </div>
          </div>
        </div>
      </div>

      {/* Practice modes */}
      <p className="text-[11px] font-black text-slate-500 tracking-wider pl-1 pt-2">PRACTICE MODES</p>

      {PRACTICE_MODES.filter((m) => m.id !== "grid").map((m) => {
        const mc = modeColors[m.id];
        return (
          <button
            key={m.id}
            onClick={() => dispatch({ type: "SELECT_MODE", mode: m.id })}
            className="w-full bg-white border border-slate-200 rounded-2xl p-4 flex items-center justify-between hover:border-primary-indigo transition-colors shadow-sm"
          >
            <div className="text-left flex-1">
              <p className="text-[15px] font-bold text-slate-800">{m.title}</p>
              <p className="text-xs text-slate-400 font-medium">{m.subtitle}</p>
            </div>
            <div className={`w-10 h-10 rounded-xl ${mc.bg} flex items-center justify-center text-lg font-black ${mc.color}`}>
              {m.symbol}
            </div>
          </button>
        );
      })}

      {/* Study guides */}
      <div className="pt-2">
        <p className="text-[11px] font-black text-slate-500 tracking-wider pl-1 mb-2">STUDY GUIDES & FLASHCARDS</p>
        <div className="grid grid-cols-4 gap-2">
          <StudyCard
            title="Tables"
            subtitle="12 to 37"
            badge="Study 📖"
            color="text-primary-indigo"
            bg="bg-indigo-50"
            border="border-indigo-200"
            onClick={() => navigate({ type: "learnTables", backDestination: { type: "home" } })}
          />
          <StudyCard
            title="Factors"
            subtitle="A × B = N"
            badge="Study 🔍"
            color="text-cyan-700"
            bg="bg-cyan-50"
            border="border-cyan-200"
            onClick={() => navigate({ type: "learnFactors", backDestination: { type: "home" } })}
          />
          <StudyCard
            title="Exponents"
            subtitle="x² & x³"
            badge="Study ⚡"
            color="text-amber-600"
            bg="bg-amber-50"
            border="border-amber-200"
            onClick={() => navigate({ type: "learnExponents", backDestination: { type: "home" } })}
          />
          <StudyCard
            title="Roots"
            subtitle="√100 & ∛20"
            badge="Study 🌱"
            color="text-emerald-600"
            bg="bg-emerald-50"
            border="border-emerald-200"
            onClick={() => navigate({ type: "learnRoots", backDestination: { type: "home" } })}
          />
        </div>
      </div>

      {/* Complex & Roots & Grid */}
      {PRACTICE_MODES.filter((m) => m.id === "complex" || m.id === "roots").map((m) => {
        const mc = modeColors[m.id];
        return (
          <button
            key={m.id}
            onClick={() => dispatch({ type: "SELECT_MODE", mode: m.id })}
            className="w-full bg-white border border-slate-200 rounded-2xl p-4 flex items-center justify-between hover:border-primary-indigo transition-colors shadow-sm"
          >
            <div className="text-left flex-1">
              <p className="text-[15px] font-bold text-slate-800">{m.title}</p>
              <p className="text-xs text-slate-400 font-medium">{m.subtitle}</p>
            </div>
            <div className={`w-10 h-10 rounded-xl ${mc.bg} flex items-center justify-center text-lg font-black ${mc.color}`}>
              {m.symbol}
            </div>
          </button>
        );
      })}

      {/* Grid card */}
      <button
        onClick={() => dispatch({ type: "SELECT_MODE", mode: "grid" })}
        className="w-full bg-slate-900 rounded-3xl p-4.5 px-4 py-4 flex items-center justify-between shadow-md"
      >
        <div className="text-left flex-1">
          <p className="text-base font-bold text-white">Grid Addition Speed Run</p>
          {bestGridTime != null ? (
            <p className="text-[11px] font-black text-rose-400 mt-0.5">BEST: {bestGridTime.toFixed(2)}s</p>
          ) : (
            <p className="text-xs text-slate-400 mt-0.5">5x5 matrix + row & col totals speed run</p>
          )}
        </div>
        <div className="w-11 h-11 rounded-2xl bg-slate-700 flex items-center justify-center text-rose-400 text-2xl">▦</div>
      </button>

      {/* Dashboard button */}
      <button
        onClick={() => navigate({ type: "dashboard" })}
        className="w-full bg-slate-100 text-slate-700 rounded-2xl py-3.5 flex items-center justify-center gap-2 font-bold text-sm hover:bg-slate-200"
      >
        📊 View Student Progress Dashboard
      </button>

      {/* Share card */}
      <button
        onClick={() => setShowShareDialog(true)}
        className="w-full bg-white border border-slate-200 rounded-2xl p-4 flex items-center justify-between shadow-sm"
      >
        <div className="flex items-center gap-3 flex-1 text-left">
          <div className="w-11 h-11 rounded-2xl bg-indigo-50 flex items-center justify-center text-primary-indigo text-xl">📤</div>
          <div>
            <p className="text-sm font-black text-slate-900">Share App with Anyone</p>
            <p className="text-[11px] text-slate-500">Send website link or invite with friends</p>
          </div>
        </div>
        <span className="bg-blue-50 text-primary-indigo text-[11px] font-bold px-2 py-1 rounded">Link 🌐</span>
      </button>

      {/* Privacy card */}
      <button
        onClick={() => navigate({ type: "privacy", backDestination: { type: "home" } })}
        className="w-full bg-white border border-slate-200 rounded-2xl p-4 flex items-center justify-between shadow-sm"
      >
        <div className="flex items-center gap-3 flex-1 text-left">
          <div className="w-11 h-11 rounded-2xl bg-emerald-50 flex items-center justify-center text-emerald-600 text-xl">🛡</div>
          <div>
            <p className="text-sm font-black text-slate-900">Privacy Policy & Developer</p>
            <p className="text-[11px] text-slate-500">By Vishesh Chaturvedi • 100% Free & Local Practice</p>
          </div>
        </div>
        <span className="bg-emerald-50 text-emerald-600 text-[11px] font-bold px-2 py-1 rounded">Read 📜</span>
      </button>

      {/* Footer */}
      <div className="text-center py-2">
        <p className="text-xs font-bold text-slate-500">Math Master • Version 3.14</p>
        <p className="text-[11px] text-slate-400">Developed by Vishesh Chaturvedi</p>
      </div>

      {showShareDialog && <ShareAppDialog onDismiss={() => setShowShareDialog(false)} />}
    </div>
  );
}

function StudyCard({
  title,
  subtitle,
  badge,
  color,
  bg,
  border,
  onClick,
}: {
  title: string;
  subtitle: string;
  badge: string;
  color: string;
  bg: string;
  border: string;
  onClick: () => void;
}) {
  return (
    <button
      onClick={onClick}
      className={`${bg} ${border} border rounded-2xl p-3 text-left flex flex-col justify-between min-h-[88px] hover:shadow-sm transition-shadow`}
    >
      <div>
        <p className="text-[13px] font-black text-slate-900">{title}</p>
        <p className={`text-[10px] font-bold ${color}`}>{subtitle}</p>
      </div>
      <p className={`text-[10px] font-black mt-2 ${color}`}>{badge}</p>
    </button>
  );
}