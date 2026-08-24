import { useState } from "react";
import { useApp } from "../store";
import { calculateValidFactorPairs } from "../engine";

export default function LearnFactorsScreen() {
  const { state, dispatch, navigate, startPractice } = useApp();
  const dest = state.destination;
  if (dest.type !== "learnFactors") return null;

  const study = state.study;
  const [searchInput, setSearchInput] = useState(String(study.learnFactorNumber));

  const validFactorPairs = calculateValidFactorPairs(study.learnFactorNumber, 99);
  const sampleNumbers = [108, 144, 180, 216, 252, 288, 360, 420, 504, 576, 720, 840];

  const selectNumber = (num: number) => {
    dispatch({ type: "STUDY_SET_FACTOR_NUM", num });
    setSearchInput(String(num));
  };
  const toggleFlashcard = () => dispatch({ type: "STUDY_TOGGLE_FACTOR_HIDE" });
  const toggleReveal = (key: string) => dispatch({ type: "STUDY_TOGGLE_FACTOR_REVEAL", key });

  return (
    <div className="min-h-screen bg-slate-50 max-w-lg mx-auto flex flex-col">
      {/* Top bar */}
      <div className="bg-white shadow-sm px-4 py-2">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <button onClick={() => navigate(dest.backDestination)} className="text-slate-700 text-2xl p-1 hover:bg-slate-100 rounded-full">←</button>
            <div>
              <h1 className="text-lg font-black text-slate-900">Factors Explorer 🔍</h1>
              <p className="text-[11px] text-slate-500">Dynamic factor pairs: A × B = N (≤99)</p>
            </div>
          </div>
          <button
            onClick={() => startPractice()}
            className="bg-primary-indigo text-white text-xs font-bold rounded-xl px-3 py-2 flex items-center gap-1"
          >
            ▶ Practice
          </button>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto px-4 py-3 space-y-3.5">
        {/* Number input card */}
        <div className="bg-white border border-slate-200 rounded-2xl p-4">
          <p className="text-[10px] font-black text-slate-500 tracking-widest">SELECT OR ENTER A 3-DIGIT NUMBER</p>
          <div className="flex items-center gap-2 mt-2.5">
            <div className="flex-1 flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-2xl px-3 py-2.5 focus-within:border-primary-indigo">
              <span className="text-primary-indigo text-lg">🔍</span>
              <input
                value={searchInput}
                onChange={(e) => {
                  const s = e.target.value.replace(/\D/g, "");
                  setSearchInput(s);
                  const num = parseInt(s, 10);
                  if (!isNaN(num) && num >= 10 && num <= 9999) selectNumber(num);
                }}
                placeholder="e.g. 252 or 108"
                className="flex-1 bg-transparent outline-none font-bold text-sm text-slate-900 placeholder:text-slate-400"
              />
              {searchInput && (
                <button onClick={() => { setSearchInput(""); dispatch({ type: "STUDY_SET_FACTOR_NUM", num: 108 }); }} className="text-slate-400 text-sm">✕</button>
              )}
            </div>
            <button
              onClick={() => { const r = sampleNumbers[Math.floor(Math.random() * sampleNumbers.length)]; setSearchInput(String(r)); dispatch({ type: "STUDY_SET_FACTOR_NUM", num: r }); }}
              className="bg-slate-100 text-slate-700 font-bold rounded-2xl px-3.5 py-3 text-sm"
            >
              🔄
            </button>
          </div>

          <p className="text-[11px] font-bold text-slate-600 mt-3">Popular Non-Prime 3-Digit Numbers:</p>
          <div className="flex gap-1.5 mt-1.5 overflow-x-auto pb-1">
            {sampleNumbers.map((n) => (
              <button
                key={n}
                onClick={() => selectNumber(n)}
                className={`px-2.5 py-1.5 rounded-xl text-xs font-bold shrink-0 ${n === study.learnFactorNumber ? "bg-primary-indigo text-white" : "bg-slate-100 text-slate-700"}`}
              >
                {n}
              </button>
            ))}
          </div>
        </div>

        {/* Flashcard toggle */}
        <div className="bg-white border border-slate-200 rounded-2xl px-4 py-2.5 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <span className={`text-xl ${study.learnFactorHideAnswers ? "text-primary-indigo" : "text-slate-500"}`}>
              {study.learnFactorHideAnswers ? "🙈" : "👁"}
            </span>
            <div>
              <p className="text-[13px] font-bold text-slate-900">Flashcard / Hide Mode</p>
              <p className="text-[11px] text-slate-500">{study.learnFactorHideAnswers ? "Tap cards to reveal factor pairs" : "All factor pairs visible"}</p>
            </div>
          </div>
          <button
            onClick={toggleFlashcard}
            className={`w-12 h-6 rounded-full relative transition-colors ${study.learnFactorHideAnswers ? "bg-primary-indigo" : "bg-slate-300"}`}
          >
            <span className={`absolute top-0.5 w-5 h-5 rounded-full bg-white shadow transition-all ${study.learnFactorHideAnswers ? "left-6.5" : "left-0.5"}`} />
          </button>
        </div>

        {/* Target number header */}
        <div className="bg-slate-900 rounded-3xl p-5 text-center shadow-md">
          <p className="text-[11px] font-black text-slate-400 tracking-widest">FACTORS OF</p>
          <p className="text-[44px] font-black mono text-white">{study.learnFactorNumber}</p>
          <span className="inline-block bg-slate-700 text-cyan-400 text-xs font-bold rounded-xl px-3 py-1.5 mt-1">
            {validFactorPairs.length > 0 ? `${validFactorPairs.length} valid factor pairs (≤99)` : "Prime or no 2-digit factor pairs"}
          </span>
        </div>

        {/* Factor pairs */}
        {validFactorPairs.length === 0 ? (
          <div className="bg-rose-50 border border-rose-200 rounded-2xl py-5 px-4 text-center">
            <p className="text-[13px] font-bold text-rose-800">{study.learnFactorNumber} has no factor pair where both factors are between 2 and 99.</p>
            <p className="text-[11px] text-slate-600 mt-1">Try selecting another composite number like 108, 144, or 252.</p>
          </div>
        ) : (
          validFactorPairs.map(([a, b]) => {
            const key = `factor_${study.learnFactorNumber}_${a}_${b}`;
            const isRevealed = !study.learnFactorHideAnswers || study.revealedFactors.includes(key);
            const formula = `${b} × ${a}`;
            return (
              <button
                key={key}
                onClick={() => study.learnFactorHideAnswers && toggleReveal(key)}
                className={`w-full rounded-2xl border p-4 flex items-center justify-between transition-shadow ${
                  isRevealed ? "bg-white border-primary-indigo/30 shadow-sm" : "bg-slate-50 border-slate-300"
                }`}
              >
                <div className="flex items-center gap-3.5">
                  <div className={`w-10 h-10 rounded-xl flex items-center justify-center text-sm font-black ${isRevealed ? "bg-indigo-50 text-primary-indigo" : "bg-slate-200 text-slate-500"}`}>
                    {a}
                  </div>
                  <div className="text-left">
                    {isRevealed ? (
                      <>
                        <p className="text-xl font-black mono text-slate-900">{formula}</p>
                        <p className="text-[11px] font-bold text-emerald-600">{formula} = {study.learnFactorNumber}</p>
                      </>
                    ) : (
                      <>
                        <p className="text-sm font-bold text-slate-500">Tap to reveal factor pair</p>
                        <p className="text-xs mono text-primary-indigo">A × {a} = {study.learnFactorNumber}</p>
                      </>
                    )}
                  </div>
                </div>
                {isRevealed ? (
                  <span className="text-emerald-500 text-xl">✓</span>
                ) : (
                  <span className="bg-slate-200 text-slate-600 text-xs font-bold rounded-lg px-2.5 py-1">???</span>
                )}
              </button>
            );
          })
        )}
      </div>
    </div>
  );
}