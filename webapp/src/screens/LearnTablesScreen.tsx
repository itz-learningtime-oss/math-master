import { useApp } from "../store";

export default function LearnTablesScreen() {
  const { state, dispatch, navigate, startPractice } = useApp();
  const dest = state.destination;
  if (dest.type !== "learnTables") return null;

  const study = state.study;
  const tableNumbers = Array.from({ length: 26 }, (_, i) => i + 12); // 12 to 37

  const selectTable = (n: number) => dispatch({ type: "STUDY_SET_TABLE", num: n });
  const toggleViewMode = (mode: "multiplication" | "division") => dispatch({ type: "STUDY_SET_TABLE_VIEW", mode });
  const toggleFlashcard = () => dispatch({ type: "STUDY_TOGGLE_TABLE_HIDE" });
  const toggleReveal = (key: string) => dispatch({ type: "STUDY_TOGGLE_TABLE_REVEAL", key });

  const practiceTable = (n: number) => {
    dispatch({ type: "UPDATE_CONFIG", config: { ...state.config, mode: "tables", selectedTables: [n], totalQuestions: 5 } });
    startPractice();
  };

  return (
    <div className="min-h-screen bg-slate-50 px-4 py-3 max-w-lg mx-auto flex flex-col">
      <div className="flex-1 flex flex-col">
        {/* Header */}
        <div className="flex items-center gap-2 py-1">
          <button onClick={() => navigate(dest.backDestination)} className="text-slate-700 text-2xl p-1 hover:bg-slate-100 rounded-full">←</button>
          <h1 className="text-xl font-black text-slate-900">Table of {study.learnTableNum}</h1>
        </div>

        {/* Select table */}
        <p className="text-[10px] font-black text-slate-400 tracking-widest mt-2 mb-1.5 pl-1">SELECT TABLE (12 TO 37)</p>
        <div className="flex gap-1.5 overflow-x-auto pb-1">
          {tableNumbers.map((n) => (
            <button
              key={n}
              onClick={() => selectTable(n)}
              className={`w-11 h-9 rounded-xl text-[13px] font-bold shrink-0 ${n === study.learnTableNum ? "bg-primary-indigo text-white" : "bg-white border border-slate-200 text-slate-700"}`}
            >
              {n}
            </button>
          ))}
        </div>

        {/* View mode + flashcard */}
        <div className="flex items-center gap-2 mt-3">
          <div className="flex flex-1 bg-slate-100 rounded-xl p-1">
            <button
              onClick={() => toggleViewMode("multiplication")}
              className={`flex-1 h-8 rounded-lg text-[10px] font-bold ${study.learnTableViewMode === "multiplication" ? "bg-white shadow text-primary-indigo" : "text-slate-500"}`}
            >
              Multiplication (×)
            </button>
            <button
              onClick={() => toggleViewMode("division")}
              className={`flex-1 h-8 rounded-lg text-[10px] font-bold ${study.learnTableViewMode === "division" ? "bg-white shadow text-primary-indigo" : "text-slate-500"}`}
            >
              Division (÷)
            </button>
          </div>
          <button
            onClick={toggleFlashcard}
            className="border border-slate-300 rounded-xl px-3 py-2 text-[11px] font-bold text-slate-700"
          >
            {study.learnTableHideAnswers ? "👁 Show All" : "🙈 Flashcard"}
          </button>
        </div>

        {/* Table rows */}
        <div className="flex-1 overflow-y-auto mt-3 space-y-2 mb-3">
          {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((multiplier) => {
            const product = study.learnTableNum * multiplier;
            const key = `${study.learnTableNum}_${multiplier}`;
            const isRevealed = !study.learnTableHideAnswers || study.revealedTableAnswers.includes(key);
            const leftExpression = study.learnTableViewMode === "multiplication" ? `${study.learnTableNum} × ${multiplier}` : `${product} ÷ ${study.learnTableNum}`;
            const rightAnswer = study.learnTableViewMode === "multiplication" ? `${product}` : `${multiplier}`;

            return (
              <button
                key={key}
                onClick={() => study.learnTableHideAnswers && toggleReveal(key)}
                className={`w-full rounded-2xl border p-3.5 flex items-center justify-between ${isRevealed ? "bg-white border-slate-200" : "bg-indigo-50 border-indigo-200"}`}
              >
                <div className="flex items-center gap-3">
                  <span className="w-7 h-7 rounded-full bg-slate-100 flex items-center justify-center text-[11px] font-bold text-slate-500">{multiplier}</span>
                  <span className="text-base font-bold mono text-slate-900">{leftExpression}</span>
                </div>
                {isRevealed ? (
                  <span className="text-lg font-black mono text-primary-indigo">= {rightAnswer}</span>
                ) : (
                  <span className="bg-indigo-200/50 text-primary-indigo text-[11px] font-bold rounded-lg px-2 py-1">Tap to reveal</span>
                )}
              </button>
            );
          })}
        </div>
      </div>

      {/* Practice button */}
      <button
        onClick={() => practiceTable(study.learnTableNum)}
        className="w-full h-13 py-3.5 bg-primary-indigo text-white rounded-2xl flex items-center justify-center gap-2 font-bold text-[15px] hover:bg-indigo-600 mb-2"
      >
        ⚡ Practice Table {study.learnTableNum} Drills
      </button>
    </div>
  );
}