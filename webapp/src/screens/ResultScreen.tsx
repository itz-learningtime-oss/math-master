import { useApp } from "../store";
import { modeFromId } from "../types";

export default function ResultScreen() {
  const { state, navigate } = useApp();
  const dest = state.destination;
  if (dest.type !== "result") return null;

  const mode = dest.mode;
  const isGrid = mode === "grid";
  const totalTimeSec = state.finalCompletionTime;
  const count = isGrid ? 36 : state.results.length;
  const avgPerQ = count > 0 ? totalTimeSec / count : 0;

  const gridItems = isGrid ? Object.values(state.grid.userAnswers) : [];

  return (
    <div className="min-h-screen bg-slate-50 px-4 py-5 max-w-lg mx-auto flex flex-col">
      {/* Celebration header */}
      <div className="text-center">
        <div className="w-16 h-16 rounded-full bg-emerald-50 flex items-center justify-center text-emerald-500 text-4xl mx-auto">🏆</div>
        <h1 className="text-2xl font-black text-slate-900 mt-2">Practice Complete!</h1>
        <p className="text-[44px] font-black mono text-primary-indigo mt-1">{totalTimeSec.toFixed(2)} s</p>
        <p className="text-[11px] font-bold text-slate-400 tracking-widest">TOTAL COMPLETION TIME</p>

        <div className="flex gap-2 mt-3">
          <div className="flex-1 bg-white border border-slate-200 rounded-2xl py-3 text-center">
            <p className="text-[10px] font-black text-slate-400">PROBLEMS</p>
            <p className="text-lg font-black text-slate-900">{count}</p>
          </div>
          <div className="flex-1 bg-white border border-slate-200 rounded-2xl py-3 text-center">
            <p className="text-[10px] font-black text-slate-400">AVG SPEED</p>
            <p className="text-lg font-black text-slate-900">{avgPerQ.toFixed(2)} s/q</p>
          </div>
        </div>
      </div>

      {/* Breakdown table */}
      <div className="flex-1 bg-white border border-slate-200 rounded-2xl p-3 mt-4 overflow-y-auto">
        <p className="text-[11px] font-black text-slate-500 tracking-widest pl-1 pb-2">ITEMIZED TIME REPORT</p>
        {isGrid ? (
          <div className="space-y-1.5">
            {gridItems.map((item, idx) => (
              <div
                key={item.key}
                className={`flex items-center justify-between rounded-xl px-2.5 py-2 ${idx % 2 === 0 ? "bg-slate-50" : "bg-white"}`}
              >
                <span className="text-xs font-bold text-slate-800 mono">{item.label}</span>
                <span className="flex items-center gap-3">
                  <span className="text-xs font-black text-primary-indigo mono">{item.value}</span>
                  <span className="text-xs text-slate-500 mono">{item.timeSec.toFixed(2)}s</span>
                </span>
              </div>
            ))}
          </div>
        ) : (
          <div className="space-y-1.5">
            {state.results.map((item, idx) => (
              <div
                key={idx}
                className={`flex items-center justify-between rounded-xl px-2.5 py-2 ${idx % 2 === 0 ? "bg-slate-50" : "bg-white"}`}
              >
                <div className="flex-1 min-w-0 mr-2">
                  <p className="text-xs font-bold text-slate-800 mono truncate">{item.prompt}</p>
                  <p className="text-[11px] font-black text-primary-indigo mono">= {item.userAnswer}</p>
                </div>
                <span className="text-xs font-bold text-slate-500 mono">{item.timeTakenSec.toFixed(2)}s</span>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Actions */}
      <div className="flex gap-2.5 mt-4 mb-2">
        <button
          onClick={() => navigate({ type: "home" })}
          className="flex-1 h-13 py-3.5 bg-slate-100 text-slate-700 rounded-2xl flex items-center justify-center gap-1.5 font-bold text-sm hover:bg-slate-200"
        >
          🏠 Home
        </button>
        <button
          onClick={() => navigate({ type: "analysis", mode, lastCompletionTime: totalTimeSec, backDestination: { type: "home" } })}
          className="flex-1 h-13 py-3.5 bg-primary-indigo text-white rounded-2xl flex items-center justify-center gap-1.5 font-bold text-sm hover:bg-indigo-600"
        >
          📈 Analysis
        </button>
      </div>

      <p className="hidden">{modeFromId(mode).id}</p>
    </div>
  );
}