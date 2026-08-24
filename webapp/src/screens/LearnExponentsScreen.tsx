import { useApp } from "../store";
import type { ExponentDisplayType, ExponentPowerMode } from "../types";

export default function LearnExponentsScreen() {
  const { state, dispatch, navigate, startPractice } = useApp();
  const dest = state.destination;
  if (dest.type !== "learnExponents") return null;

  const study = state.study;
  const ranges = ["2-10", "11-20", "21-30", "31-40", "41-50", "All (2-50)"];

  const displayType: ExponentDisplayType =
    study.exponentPowerMode === "power2" ? "squares" : study.exponentPowerMode === "power3" ? "cubes" : "both";

  const filteredNumbers = (() => {
    switch (study.exponentRangeFilter) {
      case "2-10": return Array.from({ length: 9 }, (_, i) => i + 2);
      case "11-20": return Array.from({ length: 10 }, (_, i) => i + 11);
      case "21-30": return Array.from({ length: 10 }, (_, i) => i + 21);
      case "31-40": return Array.from({ length: 10 }, (_, i) => i + 31);
      case "41-50": return Array.from({ length: 10 }, (_, i) => i + 41);
      default: return Array.from({ length: 49 }, (_, i) => i + 2);
    }
  })();

  const setType = (t: ExponentDisplayType) => {
    const mode: ExponentPowerMode = t === "squares" ? "power2" : t === "cubes" ? "power3" : "both";
    dispatch({ type: "STUDY_SET_EXPONENT_MODE", mode });
  };
  const setRange = (r: string) => dispatch({ type: "STUDY_SET_EXPONENT_RANGE", range: r });
  const toggleFlashcard = () => dispatch({ type: "STUDY_TOGGLE_EXPONENT_HIDE" });
  const toggleReveal = (key: string) => dispatch({ type: "STUDY_TOGGLE_EXPONENT_REVEAL", key });

  return (
    <div className="min-h-screen bg-slate-50 px-4 py-3 max-w-lg mx-auto flex flex-col">
      <div className="flex-1 flex flex-col">
        {/* Header */}
        <div className="flex items-center gap-2 py-1">
          <button onClick={() => navigate(dest.backDestination)} className="text-slate-700 text-2xl p-1 hover:bg-slate-100 rounded-full">←</button>
          <h1 className="text-xl font-black text-slate-900">Learn Exponents (x² & x³)</h1>
        </div>

        {/* Type toggle */}
        <div className="flex gap-1.5 mt-3">
          {(["squares", "cubes", "both"] as ExponentDisplayType[]).map((t) => (
            <button
              key={t}
              onClick={() => setType(t)}
              className={`flex-1 h-10 rounded-xl text-[11px] font-bold ${displayType === t ? "bg-primary-indigo text-white" : "bg-white border border-slate-200 text-slate-700"}`}
            >
              {t === "squares" ? "Squares (x²)" : t === "cubes" ? "Cubes (x³)" : "Both"}
            </button>
          ))}
        </div>

        {/* Range selector */}
        <div className="flex gap-1.5 overflow-x-auto mt-2.5 pb-1">
          {ranges.map((r) => (
            <button
              key={r}
              onClick={() => setRange(r)}
              className={`h-8 rounded-xl px-2.5 text-[11px] font-bold whitespace-nowrap ${r === study.exponentRangeFilter ? "bg-slate-900 text-white" : "bg-white border border-slate-200 text-slate-700"}`}
            >
              {r}
            </button>
          ))}
        </div>

        {/* Flashcard toggle */}
        <div className="flex items-center justify-between mt-2.5">
          <p className="text-[10px] font-black text-slate-400 tracking-widest">{filteredNumbers.length} NUMBERS IN LIST</p>
          <button
            onClick={toggleFlashcard}
            className="border border-slate-300 rounded-xl px-3 py-2 text-[11px] font-bold text-slate-700"
          >
            {study.exponentHideAnswers ? "👁 Show All" : "🙈 Flashcard Mode"}
          </button>
        </div>

        {/* Exponents list */}
        <div className="flex-1 overflow-y-auto mt-2.5 space-y-2 mb-3">
          {filteredNumbers.map((n) => {
            const showSquare = displayType === "squares" || displayType === "both";
            const showCube = displayType === "cubes" || displayType === "both";
            const sqKey = `sq_${n}`;
            const cbKey = `cb_${n}`;
            const isSqRevealed = !study.exponentHideAnswers || study.revealedExponents.includes(sqKey);
            const isCbRevealed = !study.exponentHideAnswers || study.revealedExponents.includes(cbKey);

            return (
              <div key={n} className="w-full bg-white border border-slate-200 rounded-2xl p-3 flex items-center justify-between">
                <span className="text-base font-black mono text-slate-900">n = {n}</span>
                <div className="flex gap-2">
                  {showSquare && (
                    <button
                      onClick={() => study.exponentHideAnswers && toggleReveal(sqKey)}
                      className={`rounded-xl border px-2.5 py-1.5 text-[13px] font-bold mono ${isSqRevealed ? "bg-amber-50 border-amber-200 text-amber-800" : "bg-slate-100 border-slate-200 text-slate-500"}`}
                    >
                      {isSqRevealed ? `${n}² = ${n * n}` : `${n}² = ?`}
                    </button>
                  )}
                  {showCube && (
                    <button
                      onClick={() => study.exponentHideAnswers && toggleReveal(cbKey)}
                      className={`rounded-xl border px-2.5 py-1.5 text-[13px] font-bold mono ${isCbRevealed ? "bg-indigo-50 border-indigo-200 text-primary-indigo" : "bg-slate-100 border-slate-200 text-slate-500"}`}
                    >
                      {isCbRevealed ? `${n}³ = ${n * n * n}` : `${n}³ = ?`}
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Practice button */}
      <button
        onClick={() => startPractice()}
        className="w-full h-13 py-3.5 bg-primary-indigo text-white rounded-2xl flex items-center justify-center gap-2 font-bold text-[15px] hover:bg-indigo-600 mb-2"
      >
        ⚡ Practice Multiplication Drills
      </button>
    </div>
  );
}