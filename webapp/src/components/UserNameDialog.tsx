import { useState } from "react";
import { useApp } from "../store";

export default function UserNameDialog() {
  const { state, dispatch } = useApp();
  const currentName = state.goal?.userName ?? "";
  const [nameInput, setNameInput] = useState(currentName);
  const [isError, setIsError] = useState(false);

  const handleSave = () => {
    const trimmed = nameInput.trim();
    if (trimmed) {
      dispatch({ type: "SET_NAME", name: trimmed });
    } else {
      setIsError(true);
    }
  };

  const handleDismiss = () => dispatch({ type: "CLOSE_NAME_PROMPT" });

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 animate-fade-in px-4" onClick={handleDismiss}>
      <div
        className="w-full max-w-md bg-white rounded-3xl p-6 shadow-2xl animate-fade-in"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-full bg-primary-indigo flex items-center justify-center text-white text-2xl">👤</div>
            <div>
              <h2 className="text-lg font-black text-slate-900">
                {currentName ? "Edit Your Profile Name" : "Welcome to Math Master!"}
              </h2>
              <p className="text-xs text-slate-500">Personalize your math journey</p>
            </div>
          </div>
          <button onClick={handleDismiss} className="text-slate-400 hover:text-slate-600 text-xl">✕</button>
        </div>

        <div className="mt-5">
          <p className="text-sm font-bold text-slate-900">What is your name?</p>
          <p className="text-xs text-slate-600 mt-1 leading-relaxed">
            Your name will be displayed on the app header and progress reports every time you open Math Master.
          </p>
        </div>

        <div className="mt-4">
          <div className={`flex items-center gap-2 rounded-2xl border-2 px-4 py-3 ${isError ? "border-red-500 bg-slate-100" : "border-slate-300 bg-slate-50 focus-within:border-primary-indigo"}`}>
            <span className="text-primary-indigo text-lg">🏆</span>
            <input
              value={nameInput}
              onChange={(e) => {
                setNameInput(e.target.value);
                if (e.target.value.trim()) setIsError(false);
              }}
              placeholder="Enter your name (e.g., Alex, Sarah)"
              onKeyDown={(e) => e.key === "Enter" && handleSave()}
              className="flex-1 bg-transparent outline-none font-bold text-slate-900 placeholder:text-slate-400 placeholder:font-normal text-sm"
            />
          </div>
          {isError && <p className="text-red-600 text-xs mt-1">Please enter a name</p>}
        </div>

        <div className="mt-5 flex justify-end items-center gap-2">
          <button onClick={handleDismiss} className="px-4 py-2.5 text-slate-500 font-semibold text-sm rounded-xl hover:bg-slate-100">
            Cancel
          </button>
          <button
            onClick={handleSave}
            className="flex items-center gap-2 bg-primary-indigo text-white font-bold rounded-xl px-5 py-3 text-sm hover:bg-indigo-600"
          >
            ✓ {currentName ? "Save Name" : "Save & Start Training"}
          </button>
        </div>
      </div>
    </div>
  );
}