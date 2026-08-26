import { useEffect, useState } from "react";
import { useApp } from "../store";
import MathKeypad from "../components/MathKeypad";

export default function GridScreen() {
  const { state, dispatch, submitGridAnswer, togglePause } = useApp();
  const { grid, currentInput, elapsedSeconds, isPaused, isAnswerError } = state;
  const [shakeKey, setShakeKey] = useState(0);

  useEffect(() => {
    if (isAnswerError) setShakeKey((k) => k + 1);
  }, [isAnswerError]);

  if (!grid || grid.rows.length < 5 || grid.cols.length < 5) return null;

  const { rows, cols, currentStep, userAnswers } = grid;
  const isError = isAnswerError;
  const updateInput = (input: string) => dispatch({ type: "UPDATE_INPUT", input });

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col max-w-lg mx-auto relative">
      {/* Header */}
      <div className="flex items-center justify-between px-4 pt-3">
        <span className="bg-slate-900 text-white text-[11px] font-black rounded-xl px-2.5 py-1.5">
          GRID SPEED RUN ({currentStep + 1}/36)
        </span>
        <div className="flex items-center gap-2">
          <button
            onClick={togglePause}
            className="w-9 h-9 rounded-full bg-slate-100 flex items-center justify-center text-slate-700 text-sm"
          >
            {isPaused ? "▶" : "⏸"}
          </button>
          <span className="bg-amber-50 border border-amber-200 text-amber-600 text-[13px] font-black mono rounded-xl px-2.5 py-1.5">
            ⏱ {elapsedSeconds.toFixed(2)} s
          </span>
        </div>
      </div>

      {/* Matrix table */}
      <div className="flex justify-center px-2 mt-2 overflow-x-auto" key={shakeKey}>
        <div className="bg-white border border-slate-200 rounded-2xl p-1 inline-block">
          {/* Header row */}
          <div className="flex gap-0.5 mb-0.5">
            <GridCell text="" bg="bg-slate-100" textColor="text-slate-700" />
            {cols.map((c, i) => (
              <GridCell key={`h${i}`} text={String(c)} bg="bg-yellow-200" textColor="text-yellow-900" bold />
            ))}
            <GridCell text="TOT" bg="bg-green-200" textColor="text-green-800" bold />
          </div>

          {/* Data rows */}
          {rows.map((rVal, rIdx) => (
            <div key={rIdx} className="flex gap-0.5 mb-0.5">
              <GridCell text={String(rVal)} bg="bg-yellow-200" textColor="text-yellow-900" bold />
              {[0, 1, 2, 3, 4].map((cIdx) => {
                const cellStep = rIdx * 5 + cIdx;
                const cellKey = `r${rIdx}c${cIdx}`;
                const cellData = userAnswers[cellKey];
                const isActive = currentStep === cellStep;
                const cellBg = isActive && isError ? "bg-red-100" : isActive ? "bg-blue-100" : cellData ? "bg-white" : "bg-slate-100/50";
                const cellBorder = isActive ? "border-blue-500" : "border-slate-200";
                const cellText = cellData ? String(cellData.value) : isActive ? currentInput : "";
                return (
                  <GridCell
                    key={cellKey}
                    text={cellText}
                    bg={cellBg}
                    textColor={isActive && isError ? "text-rose-500" : "text-slate-900"}
                    border={cellBorder}
                    bold
                  />
                );
              })}
              <GridCell
                text={String(userAnswers[`rowSum${rIdx}`]?.value ?? "")}
                bg="bg-green-50"
                textColor="text-green-800"
                border="border-green-200"
                bold
              />
            </div>
          ))}

          {/* Bottom total row */}
          <div className="flex gap-0.5">
            <GridCell text="TOT" bg="bg-green-200" textColor="text-green-800" bold />
            {[0, 1, 2, 3, 4].map((cIdx) => (
              <GridCell
                key={`c${cIdx}`}
                text={String(userAnswers[`colSum${cIdx}`]?.value ?? "")}
                bg="bg-green-50"
                textColor="text-green-800"
                border="border-green-200"
                bold
              />
            ))}
            <GridCell
              text={String(userAnswers["grand"]?.value ?? "")}
              bg="bg-blue-50"
              textColor="text-primary-indigo"
              border="border-blue-200"
              bold
            />
          </div>
        </div>
      </div>

      {/* Target prompt */}
      <div
        className="bg-slate-900 rounded-2xl mx-4 mt-3 p-3.5 text-center shadow-md"
        key={`prompt-${shakeKey}`}
      >
        <p className="text-[10px] font-black text-slate-400 tracking-widest">CURRENT TARGET</p>
        <p className="text-2xl font-black mono text-white mt-0.5">{grid.activePrompt}</p>
      </div>

      <div className="flex-1" />

      {/* Keypad */}
      <p className="text-center text-[10px] text-slate-400 font-medium pb-1">⌨️ Tip: you can also type your answer on a physical keyboard (Enter to submit)</p>
      <MathKeypad
        onDigit={(d) => updateInput(currentInput + d)}
        onBackspace={() => updateInput(currentInput.slice(0, -1))}
        onClear={() => updateInput("")}
        onSubmit={() => submitGridAnswer(currentInput)}
      />

      {/* Pause overlay */}
      {isPaused && (
        <div className="fixed inset-0 z-40 bg-black/65 flex flex-col items-center justify-center">
          <p className="text-white text-2xl font-black">Grid Speed Run Paused</p>
          <button
            onClick={togglePause}
            className="mt-4 bg-primary-indigo text-white font-bold rounded-2xl px-8 py-3.5 text-[15px]"
          >
            Resume Speed Run
          </button>
        </div>
      )}
    </div>
  );
}

function GridCell({
  text,
  bg,
  textColor,
  border = "border-slate-200",
  bold = false,
}: {
  text: string;
  bg: string;
  textColor: string;
  border?: string;
  bold?: boolean;
}) {
  return (
    <div
      className={`w-11 h-9 rounded-md ${bg} ${border} border flex items-center justify-center mono text-[11px] ${bold ? "font-black" : "font-medium"} ${textColor}`}
    >
      {text}
    </div>
  );
}