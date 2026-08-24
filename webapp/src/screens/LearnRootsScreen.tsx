import { useApp } from "../store";
import type { RootDisplayType } from "../types";

export default function LearnRootsScreen() {
  const { state, dispatch, navigate, startPractice } = useApp();
  const dest = state.destination;
  if (dest.type !== "learnRoots") return null;

  const study = state.study;
  const displayType: RootDisplayType = study.learnRootType === "square" ? "sqroot" : "cbroot";
  const sqRanges = ["1-25", "26-50", "51-75", "76-100", "All (1-100)"];
  const cbRanges = ["1-10", "11-20", "All (1-20)"];
  const ranges = displayType === "sqroot" ? sqRanges : cbRanges;

  const numbers = (() => {
    if (displayType === "sqroot") {
      switch (study.learnRootRangeFilter) {
        case "1-25": return Array.from({ length: 25 }, (_, i) => i + 1);
        case "26-50": return Array.from({ length: 25 }, (_, i) => i + 26);
        case "51-75": return Array.from({ length: 25 }, (_, i) => i + 51);
        case "76-100": return Array.from({ length: 25 }, (_, i) => i + 76);
        default: return Array.from({ length: 100 }, (_, i) => i + 1);
      }
    }
    switch (study.learnRootRangeFilter) {
      case "1-10": return Array.from({ length: 10 }, (_, i) => i + 1);
      case "11-20": return Array.from({ length: 10 }, (_, i) => i + 11);
      default: return Array.from({ length: 20 }, (_, i) => i + 1);
    }
  })();

  const setType = (t: RootDisplayType) => dispatch({ type: "STUDY_SET_ROOT_TYPE", rootType: t === "sqroot" ? "square" : "cube" });
  const setRange = (r: string) => dispatch({ type: "STUDY_SET_ROOT_RANGE", range: r });
  const toggleFlashcard = () => dispatch({ type: "STUDY_TOGGLE_ROOT_HIDE" });
  const toggleReveal = (key: string) => dispatch({ type: "STUDY_TOGGLE_ROOT_REVEAL", key });

  return (
    <div className="min-h-screen bg-slate-50 px-4 py-3 max-w-lg mx-auto flex flex-col">
      <div className="flex-1 flex flex-col">
        {/* Header */}
        <div className="flex items-center gap-2 py-1">
          <button onClick={() => navigate(dest.backDestination)} className="text-slate-700 text-2xl p-1 hover:bg-slate-100 rounded-full">←</button>
          <h1 className="text-xl font-black text-slate-900">Learn Roots (√ & ∛)</h1>
        </div>

        {/* Type toggle */}
        <div className="flex gap-2 mt-3">
          <button
            onClick={() => { setType("sqroot"); setRange("1-25"); }}
            className={`flex-1 h-10 rounded-xl text-xs font-bold ${displayType === "sqroot" ? "bg-primary-indigo text-white" : "bg-white border border-slate-200 text-slate-700"}`}
          >
            Square Roots (√ ≤ 100)
          </button>
          <button
            onClick={() => { setType("cbroot"); setRange("1-10"); }}
            className={`flex-1 h-10 rounded-xl text-xs font-bold ${displayType === "cbroot" ? "bg-primary-indigo text-white" : "bg-white border border-slate-200 text-slate-700"}`}
          >
            Cube Roots (∛ ≤ 20)
          </button>
        </div>

        {/* Range selector */}
        <div className="flex gap-1.5 overflow-x-auto mt-2.5 pb-1">
          {ranges.map((r) => (
            <button
              key={r}
              onClick={() => setRange(r)}
              className={`h-8 rounded-xl px-2.5 text-[11px] font-bold whitespace-nowrap ${r === study.learnRootRangeFilter ? "bg-slate-900 text-white" : "bg-white border border-slate-200 text-slate-700"}`}
            >
              {r}
            </button>
          ))}
        </div>

        {/* Flashcard toggle */}
        <div className="flex items-center justify-between mt-2.5">
          <p className="text-[10px] font-black text-slate-400 tracking-widest">{numbers.length} ROOTS IN LIST</p>
          <button
            onClick={toggleFlashcard}
            className="border border-slate-300 rounded-xl px-3 py-2 text-[11px] font-bold text-slate-700"
          >
            {study.learnRootHideAnswers ? "👁 Show All" : "🙈 Flashcard Mode"}
          </button>
        </div>

        {/* Roots list */}
        <div className="flex-1 overflow-y-auto mt-2.5 space-y-2 mb-3">
          {numbers.map((n) => {
            const isSq = displayType === "sqroot";
            const powerVal = isSq ? n * n : n * n * n;
            const symbol = isSq ? "√" : "∛";
            const key = `${displayType}_${n}`;
            const isRevealed = !study.learnRootHideAnswers || study.revealedRoots.includes(key);

            return (
              <button
                key={key}
                onClick={() => study.learnRootHideAnswers && toggleReveal(key)}
                className="w-full bg-white border border-slate-200 rounded-2xl px-4 py-3.5 flex items-center justify-between"
              >
                <span className="text-[17px] font-black mono text-slate-900">{symbol}{powerVal}</span>
                {isRevealed ? (
                  <span className="text-lg font-black mono text-emerald-600">= {n}</span>
                ) : (
                  <span className="bg-emerald-100 text-emerald-800 text-[11px] font-bold rounded-lg px-2 py-1">Tap to reveal</span>
                )}
              </button>
            );
          })}
        </div>
      </div>

      {/* Practice button */}
      <button
        onClick={() => startPractice()}
        className="w-full h-13 py-3.5 bg-primary-indigo text-white rounded-2xl flex items-center justify-center gap-2 font-bold text-[15px] hover:bg-indigo-600 mb-2"
      >
        ⚡ Practice Roots Drills
      </button>
    </div>
  );
}