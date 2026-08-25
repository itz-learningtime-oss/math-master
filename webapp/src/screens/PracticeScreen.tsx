import { useEffect, useState } from "react";
import { useApp } from "../store";
import MathKeypad from "../components/MathKeypad";
import { modeFromId } from "../types";

export default function PracticeScreen() {
  const { state, dispatch, submitAnswer, togglePause, continueToNext, revealFactorOptions } = useApp();
  const {
    questions,
    currentQuestionIndex,
    currentInput,
    elapsedSeconds,
    isPaused,
    isAnswerError,
    config,
    tablesMultiPairPrompt,
    tablesWarningMessage,
    canContinueNext,
    showFactorOptions,
  } = state;
  const [shakeKey, setShakeKey] = useState(0);

  useEffect(() => {
    if (isAnswerError) setShakeKey((k) => k + 1);
  }, [isAnswerError]);

  if (!questions || questions.length === 0) return null;

  const currentQ = questions[currentQuestionIndex] ?? questions[0];
  const isReverseTable = currentQ.type === "reverse-table";
  const isFactors = config.mode === "factors" || currentQ.type === "factors";
  const isComplex = config.mode === "complex";
  const multiPair = isReverseTable && (currentQ.allValidTablePairs?.length ?? 0) > 1;
  const modeMeta = modeFromId(config.mode);

  const updateInput = (input: string) => dispatch({ type: "UPDATE_INPUT", input });

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col max-w-lg mx-auto relative">
      {/* Top bar */}
      <div className="flex items-center justify-between px-4 pt-4">
        <span className="bg-slate-100 text-slate-700 text-xs font-bold rounded-xl px-3 py-1.5">
          Q {currentQuestionIndex + 1} / {questions.length}
        </span>
        <div className="flex items-center gap-2">
          <button
            onClick={togglePause}
            className="w-9 h-9 rounded-full bg-slate-100 flex items-center justify-center text-slate-700 text-sm"
          >
            {isPaused ? "▶" : "⏸"}
          </button>
          <span className="bg-amber-50 border border-amber-200 text-amber-600 text-[13px] font-black mono rounded-xl px-3 py-1.5">
            ⏱ {elapsedSeconds.toFixed(2)} s
          </span>
        </div>
      </div>

      {/* Problem area */}
      <div className="flex-1 flex flex-col px-5 mt-4" key={shakeKey}>
        <p className="text-center text-[11px] font-black text-slate-400 tracking-widest">
          {isFactors
            ? "FIND ANY FACTOR PAIR (A × B = N, A,B ≤ 99)"
            : isReverseTable
              ? multiPair
                ? `ENTER FACTOR PAIRS (${currentQ.allValidTablePairs!.length} IN SELECTED TABLES)`
                : "ENTER FACTOR PAIR (e.g. 24*2)"
              : "SOLVE THIS"}
        </p>

        {/* Prompt card */}
        <div
          className={`w-full bg-white rounded-3xl border-2 shadow-sm p-5 mt-3 transition-colors ${
            isAnswerError ? "border-rose-500 animate-shake" : "border-slate-200"
          }`}
        >
          {isFactors && (
            <p className="text-center text-[11px] font-bold text-slate-500">Target Number (3-Digit Non-Prime)</p>
          )}
          <p className={`text-center font-black text-slate-900 mono ${isComplex ? "text-[22px] leading-7" : "text-[38px] leading-11"}`}>
            {currentQ.prompt}
          </p>
          {isFactors && (
            <span className="block mx-auto w-fit mt-1 bg-blue-50 text-primary-indigo text-[11px] font-bold rounded-xl px-2.5 py-1">
              Type factor pair: A × B = {currentQ.prompt}
            </span>
          )}
          {multiPair && (
            <span className="block mx-auto w-fit mt-1 bg-indigo-50 text-primary-indigo text-[11px] font-bold rounded-lg px-2 py-0.5">
              {currentQ.allValidTablePairs!.length} valid pairs in your selected tables
            </span>
          )}
        </div>

        {/* Answer display */}
        <div
          className={`w-full h-15 rounded-2xl border-2 mt-3.5 flex items-center justify-center transition-colors ${
            isAnswerError ? "bg-rose-50 border-rose-500" : "bg-slate-100 border-primary-indigo/50"
          }`}
        >
          {currentInput ? (
            <span className={`text-2xl font-black mono truncate max-w-full px-3 ${isAnswerError ? "text-rose-500" : "text-primary-indigo"}`}>
              {currentInput}
            </span>
          ) : (
            <span className="text-sm font-bold text-slate-400">
              {isFactors
                ? "Type factor pair (e.g. 36×7)"
                : isReverseTable
                  ? multiPair
                    ? "e.g. 21*4, 12*7 or 21*4"
                    : "Tap keypad (e.g. 12*2)"
                  : "Enter answer..."}
            </span>
          )}
        </div>

        {/* Tables warning banner */}
        {tablesWarningMessage && (
          <div className="w-full bg-amber-50 border border-amber-200 rounded-xl px-3 py-2 mt-2.5 flex items-start gap-2">
            <span className="text-amber-600 text-base">⚠️</span>
            <p className="text-[11px] font-bold text-amber-900 leading-snug flex-1">{tablesWarningMessage}</p>
          </div>
        )}

        {/* Tables multi-pair encouragement */}
        {tablesMultiPairPrompt && (
          <div className="w-full bg-emerald-50 border border-emerald-200 rounded-xl px-3 py-2 mt-2.5 flex items-start gap-2">
            <span className="text-emerald-600 text-base">✓</span>
            <p className="text-[11px] font-bold text-emerald-900 leading-snug flex-1">{tablesMultiPairPrompt}</p>
          </div>
        )}

        {/* Continue to next */}
        {canContinueNext && (
          <button
            onClick={continueToNext}
            className="w-full bg-emerald-500 text-white font-bold rounded-xl h-10 mt-2.5 flex items-center justify-center gap-1.5 text-xs hover:bg-emerald-600"
          >
            Don't remember more? Continue to Next →
          </button>
        )}

        {/* Factors: hidden options with reveal button */}
        {isFactors && currentQ.options && currentQ.options.length > 0 && (
          <>
            {!showFactorOptions ? (
              <button
                onClick={revealFactorOptions}
                className="w-full bg-blue-50 border border-blue-200 text-primary-indigo font-bold rounded-xl py-2.5 mt-2.5 flex items-center justify-center gap-1.5 text-[11px]"
              >
                💡 Unable to remember the pair? Check Options
              </button>
            ) : (
              <div className="mt-2.5">
                <p className="text-center text-[10px] font-black text-slate-400 tracking-widest">CHOOSE A VALID FACTOR PAIR:</p>
                <div className="flex gap-1.5 mt-1.5">
                  {currentQ.options.map((opt) => {
                    const isSelected = currentInput === opt;
                    return (
                      <button
                        key={opt}
                        onClick={() => updateInput(opt)}
                        className={`flex-1 rounded-xl border py-2 text-xs font-bold mono transition-colors ${
                          isSelected ? "bg-primary-indigo border-primary-indigo text-white" : "bg-white border-slate-200 text-slate-800"
                        }`}
                      >
                        {opt}
                      </button>
                    );
                  })}
                </div>
              </div>
            )}
          </>
        )}
      </div>

      {/* Keypad */}
      <MathKeypad
        onDigit={(d) => updateInput(currentInput + d)}
        onBackspace={() => updateInput(currentInput.slice(0, -1))}
        onClear={() => updateInput("")}
        onSubmit={submitAnswer}
        showMultiplySymbol={isReverseTable || isFactors}
        showMinusSymbol={isComplex || config.mode === "subtraction"}
        showDecimal={isComplex}
        showComma={isReverseTable || isFactors}
      />

      {/* Pause overlay */}
      {isPaused && (
        <div className="fixed inset-0 z-40 bg-black/65 flex flex-col items-center justify-center">
          <p className="text-white text-[28px] font-black">Session Paused</p>
          <button
            onClick={togglePause}
            className="mt-5 bg-primary-indigo text-white font-bold rounded-2xl px-8 py-3.5 text-base"
          >
            ▶ Resume Practice
          </button>
        </div>
      )}

      <p className="hidden">{modeMeta.id}</p>
    </div>
  );
}