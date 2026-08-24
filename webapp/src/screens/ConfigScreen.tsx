import { useApp } from "../store";
import { PRACTICE_MODES, type PracticeConfig, type PracticeModeId, type RootMode } from "../types";
import type { ScreenDestination } from "../types";

export default function ConfigScreen() {
  const { state, dispatch, navigate, startPractice } = useApp();
  const dest = state.destination;
  if (dest.type !== "config") return null;
  const mode = dest.mode;
  const config = state.config;
  const modeMeta = PRACTICE_MODES.find((m) => m.id === mode)!;

  const update = (fn: (c: PracticeConfig) => PracticeConfig) => {
    dispatch({ type: "UPDATE_CONFIG", config: fn(config) });
  };

  const goBack = () => navigate(dest.backDestination);
  const goStudy = (sd: ScreenDestination) => navigate(sd);

  return (
    <div className="min-h-screen bg-slate-50 px-4 py-3 max-w-lg mx-auto flex flex-col">
      {/* Header */}
      <div className="flex items-center gap-2 py-1">
        <button onClick={goBack} className="text-slate-700 text-2xl p-1 hover:bg-slate-100 rounded-full">←</button>
        <h1 className="text-xl font-black text-slate-900">{modeMeta.title} Settings</h1>
      </div>

      <div className="flex-1 space-y-4 mt-3 pb-4">
        {/* ---- Addition / Subtraction / Multiplication ---- */}
        {(mode === "addition" || mode === "subtraction" || mode === "multiplication") && (
          <>
            <ConfigRangeInput
              title="Number Range"
              minVal={config.minRange}
              maxVal={config.maxRange}
              onMinChange={(v) => update((c) => ({ ...c, minRange: v }))}
              onMaxChange={(v) => update((c) => ({ ...c, maxRange: v }))}
              presets={[
                ["1-Digit", 1, 9],
                ["2-Digit", 10, 99],
                ["3-Digit", 100, 999],
                ["4-Digit", 1000, 9999],
              ]}
            />
            <ConfigStepper
              title="Numbers per Question"
              value={config.numsPerQuestion}
              min={2}
              max={5}
              onChange={(v) => update((c) => ({ ...c, numsPerQuestion: v }))}
            />
            <ConfigCountPills
              title="Number of Questions"
              selectedCount={config.totalQuestions}
              counts={[5, 10, 20, 30]}
              onSelect={(v) => update((c) => ({ ...c, totalQuestions: v }))}
            />
            {mode === "multiplication" && (
              <StudyBanner
                title="Need to review exponents or tables?"
                subtitle="Study 12-37 tables or x² / x³ powers"
                buttonText="Exponents ⚡"
                onClick={() => goStudy({ type: "learnExponents", backDestination: { type: "config", mode, backDestination: dest.backDestination } })}
              />
            )}
          </>
        )}

        {/* ---- Division ---- */}
        {mode === "division" && (
          <>
            <StudyBanner
              title="Review Division & Tables"
              subtitle="Study tables 12 to 37 before practicing"
              buttonText="Learn Tables 📖"
              onClick={() => goStudy({ type: "learnTables", backDestination: { type: "config", mode, backDestination: dest.backDestination } })}
            />
            <InfoBanner color="emerald">
              Numbers ending in 0 are automatically excluded for clean calculation drills.
            </InfoBanner>
            <ConfigRangeInput
              title="Dividend Range (Greater Number)"
              minVal={config.dividendMin}
              maxVal={config.dividendMax}
              onMinChange={(v) => update((c) => ({ ...c, dividendMin: v }))}
              onMaxChange={(v) => update((c) => ({ ...c, dividendMax: v }))}
              presets={[
                ["2-Digit", 10, 99],
                ["3-Digit", 100, 999],
                ["4-Digit", 1000, 9999],
              ]}
            />
            <ConfigRangeInput
              title="Divisor Range (Smaller Number)"
              minVal={config.divisorMin}
              maxVal={config.divisorMax}
              onMinChange={(v) => update((c) => ({ ...c, divisorMin: v }))}
              onMaxChange={(v) => update((c) => ({ ...c, divisorMax: v }))}
              presets={[
                ["Single", 2, 9],
                ["2-Digit", 10, 99],
                ["12-37", 12, 37],
              ]}
            />
            <ConfigCountPills
              title="Number of Questions"
              selectedCount={config.totalQuestions}
              counts={[5, 10, 20, 30]}
              onSelect={(v) => update((c) => ({ ...c, totalQuestions: v }))}
            />
          </>
        )}

        {/* ---- Tables ---- */}
        {mode === "tables" && (
          <>
            <div>
              <p className="text-[13px] font-bold text-slate-700 mb-2">Select Tables to Practice (12 to 37)</p>
              <div className="grid grid-cols-6 gap-1.5">
                {Array.from({ length: 36 }, (_, i) => i + 2).map((n) => {
                  const isSelected = config.selectedTables.includes(n);
                  return (
                    <button
                      key={n}
                      onClick={() => dispatch({ type: "TOGGLE_TABLE", table: n })}
                      className={`h-11 rounded-xl text-[13px] font-bold transition-colors ${
                        isSelected ? "bg-indigo-50 border border-primary-indigo text-primary-indigo" : "bg-white border border-slate-200 text-slate-700"
                      }`}
                    >
                      {n}
                    </button>
                  );
                })}
              </div>
            </div>
            <div>
              <p className="text-[13px] font-bold text-slate-700 mb-2">Question Selection Mode</p>
              <div className="flex gap-2">
                <PillOption
                  label="All Combinations (2-9)"
                  isSelected={config.tableSelectionMode === "combinations"}
                  onClick={() => update((c) => ({ ...c, tableSelectionMode: "combinations" as const }))}
                />
                <PillOption
                  label="Specific Count"
                  isSelected={config.tableSelectionMode === "count"}
                  onClick={() => update((c) => ({ ...c, tableSelectionMode: "count" as const }))}
                />
              </div>
            </div>
            {config.tableSelectionMode === "count" && (
              <ConfigCountPills
                title="Number of Questions"
                selectedCount={config.totalQuestions}
                counts={[5, 10, 20, 30]}
                onSelect={(v) => update((c) => ({ ...c, totalQuestions: v }))}
              />
            )}
          </>
        )}

        {/* ---- Roots ---- */}
        {mode === "roots" && (
          <>
            <StudyBanner
              title="Need to review roots first?"
              subtitle="Study √1-100 & ∛1-20 rules"
              buttonText="Learn Roots 🌱"
              onClick={() => goStudy({ type: "learnRoots", backDestination: { type: "config", mode, backDestination: dest.backDestination } })}
            />
            <div>
              <p className="text-[13px] font-bold text-slate-700 mb-2">Practice Type</p>
              <div className="flex gap-1.5">
                <PillOption label="Square Root (√)" isSelected={config.rootMode === "sqroot"} onClick={() => update((c) => ({ ...c, rootMode: "sqroot" as RootMode }))} />
                <PillOption label="Cube Root (∛)" isSelected={config.rootMode === "cbroot"} onClick={() => update((c) => ({ ...c, rootMode: "cbroot" as RootMode }))} />
                <PillOption label="Mixed (√ & ∛)" isSelected={config.rootMode === "both"} onClick={() => update((c) => ({ ...c, rootMode: "both" as RootMode }))} />
              </div>
            </div>
            {(config.rootMode === "sqroot" || config.rootMode === "both") && (
              <ConfigRangeInput
                title="Square Root Base Range (up to 100)"
                minVal={config.sqRootMin}
                maxVal={config.sqRootMax}
                onMinChange={(v) => update((c) => ({ ...c, sqRootMin: Math.min(Math.max(v, 1), 100) }))}
                onMaxChange={(v) => update((c) => ({ ...c, sqRootMax: Math.min(Math.max(v, 1), 100) }))}
                presets={[
                  ["1 - 25", 1, 25],
                  ["1 - 50", 1, 50],
                  ["1 - 100", 1, 100],
                ]}
              />
            )}
            {(config.rootMode === "cbroot" || config.rootMode === "both") && (
              <ConfigRangeInput
                title="Cube Root Base Range (up to 20)"
                minVal={config.cbRootMin}
                maxVal={config.cbRootMax}
                onMinChange={(v) => update((c) => ({ ...c, cbRootMin: Math.min(Math.max(v, 1), 20) }))}
                onMaxChange={(v) => update((c) => ({ ...c, cbRootMax: Math.min(Math.max(v, 1), 20) }))}
                presets={[
                  ["1 - 10", 1, 10],
                  ["1 - 15", 1, 15],
                  ["1 - 20", 1, 20],
                ]}
              />
            )}
            <ConfigCountPills
              title="Number of Questions"
              selectedCount={config.totalQuestions}
              counts={[5, 10, 20, 30]}
              onSelect={(v) => update((c) => ({ ...c, totalQuestions: v }))}
            />
          </>
        )}

        {/* ---- Factors ---- */}
        {mode === "factors" && (
          <>
            <StudyBanner
              title="Need to explore factor pairs?"
              subtitle="Interactive factor explorer for any 3-digit number"
              buttonText="Factors Explorer 🔍"
              onClick={() => goStudy({ type: "learnFactors", backDestination: { type: "config", mode, backDestination: dest.backDestination } })}
            />
            <InfoBanner color="indigo">
              <p className="font-bold text-slate-900 mb-1">3-Digit Factors Rule (A × B = N, A, B ≤ 99)</p>
              <p className="text-[11px] leading-relaxed">
                • Presents a random 3-digit non-prime number (e.g. 252 or 108).<br />
                • Enter or select any valid factor pair where both factors are ≤ 99.<br />
                • Example: 108 = 54×2, 36×3, 27×4, 18×6, 12×9
              </p>
            </InfoBanner>
            <ConfigRangeInput
              title="3-Digit Number Range (Non-Prime)"
              minVal={config.factorsMin}
              maxVal={config.factorsMax}
              onMinChange={(v) => update((c) => ({ ...c, factorsMin: Math.min(Math.max(v, 100), 999) }))}
              onMaxChange={(v) => update((c) => ({ ...c, factorsMax: Math.min(Math.max(v, 100), 999) }))}
              presets={[
                ["100 - 300", 100, 300],
                ["100 - 500", 100, 500],
                ["100 - 999", 100, 999],
              ]}
            />
            <ConfigCountPills
              title="Number of Questions"
              selectedCount={config.totalQuestions}
              counts={[5, 10, 15, 20]}
              onSelect={(v) => update((c) => ({ ...c, totalQuestions: v }))}
            />
          </>
        )}

        {/* ---- Complex ---- */}
        {mode === "complex" && (
          <>
            <InfoBanner color="purple">
              <p className="font-bold text-purple-600 mb-1">Format: Difference between Sum(x, y) and Average(a, b)</p>
              <p className="text-[11px] text-slate-600">Tests dual-path mental arithmetic and instant mental subtraction.</p>
            </InfoBanner>
            <ConfigCountPills
              title="Number of Questions"
              selectedCount={config.totalQuestions}
              counts={[5, 10, 20]}
              onSelect={(v) => update((c) => ({ ...c, totalQuestions: v }))}
            />
          </>
        )}

        {/* ---- Grid ---- */}
        {mode === "grid" && (
          <>
            <ConfigRangeInput
              title="Matrix Number Range (Multiples of 10 Excluded)"
              minVal={config.minRange}
              maxVal={config.maxRange}
              onMinChange={(v) => update((c) => ({ ...c, minRange: v }))}
              onMaxChange={(v) => update((c) => ({ ...c, maxRange: v }))}
              presets={[
                ["1-Digit", 1, 9],
                ["2-Digit", 10, 99],
                ["3-Digit", 100, 999],
              ]}
            />
            <div className="bg-slate-900 rounded-2xl p-3.5">
              <p className="text-[13px] font-black text-white">5×5 Speed Matrix Run</p>
              <p className="text-[11px] text-slate-400 mt-1">
                Includes 25 cell additions + 5 row sums + 5 column sums + 1 grand total with per-step timing analysis.
              </p>
            </div>
          </>
        )}
      </div>

      {/* Start button */}
      <button
        onClick={startPractice}
        className="w-full h-14 bg-primary-indigo text-white rounded-2xl flex items-center justify-center gap-2 font-black text-base hover:bg-indigo-600 mb-4"
      >
        ▶ Start Practice
      </button>
    </div>
  );
}

/* ---- Shared config components ---- */

function ConfigRangeInput({
  title,
  minVal,
  maxVal,
  onMinChange,
  onMaxChange,
  presets,
}: {
  title: string;
  minVal: number;
  maxVal: number;
  onMinChange: (v: number) => void;
  onMaxChange: (v: number) => void;
  presets: [string, number, number][];
}) {
  return (
    <div className="bg-white border border-slate-200 rounded-2xl p-4">
      <div className="flex items-center justify-between mb-2.5">
        <p className="text-[13px] font-bold text-slate-700">{title}</p>
        <span className="bg-indigo-50 text-primary-indigo text-xs font-black px-2 py-1 rounded">{minVal} to {maxVal}</span>
      </div>
      <div className="flex gap-1.5 mb-3">
        {presets.map(([label, min, max]) => {
          const isSelected = minVal === min && maxVal === max;
          return (
            <button
              key={label}
              onClick={() => {
                onMinChange(min);
                onMaxChange(max);
              }}
              className={`flex-1 h-8 rounded-lg text-[11px] font-bold transition-colors ${
                isSelected ? "bg-primary-indigo text-white" : "bg-slate-100 border border-slate-200 text-slate-700"
              }`}
            >
              {label}
            </button>
          );
        })}
      </div>
      <div className="flex items-center gap-2">
        <NumberField label="Min" value={minVal} onChange={onMinChange} />
        <span className="text-[13px] font-bold text-slate-500">to</span>
        <NumberField label="Max" value={maxVal} onChange={onMaxChange} />
      </div>
    </div>
  );
}

function NumberField({ label, value, onChange }: { label: string; value: number; onChange: (v: number) => void }) {
  return (
    <div className="flex-1">
      <label className="block text-[11px] font-semibold text-slate-600 mb-1">{label}</label>
      <input
        type="number"
        value={value}
        onChange={(e) => {
          const v = parseInt(e.target.value, 10);
          if (!isNaN(v)) onChange(v);
        }}
        className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2.5 font-bold text-slate-900 text-base outline-none focus:border-primary-indigo"
      />
    </div>
  );
}

function ConfigStepper({ title, value, min, max, onChange }: { title: string; value: number; min: number; max: number; onChange: (v: number) => void }) {
  return (
    <div className="bg-white border border-slate-200 rounded-2xl p-4 flex items-center justify-between">
      <p className="text-[13px] font-bold text-slate-700">{title}</p>
      <div className="flex items-center gap-2">
        <button
          onClick={() => onChange(Math.max(value - 1, min))}
          className="w-9 h-9 rounded-xl bg-slate-100 text-slate-700 text-lg font-bold hover:bg-slate-200"
        >
          −
        </button>
        <span className="text-base font-black text-slate-900 px-2">{value}</span>
        <button
          onClick={() => onChange(Math.min(value + 1, max))}
          className="w-9 h-9 rounded-xl bg-slate-100 text-slate-700 text-lg font-bold hover:bg-slate-200"
        >
          +
        </button>
      </div>
    </div>
  );
}

function ConfigCountPills({ title, selectedCount, counts, onSelect }: { title: string; selectedCount: number; counts: number[]; onSelect: (v: number) => void }) {
  return (
    <div>
      <p className="text-[13px] font-bold text-slate-700 mb-2">{title}</p>
      <div className="flex gap-2">
        {counts.map((c) => {
          const isSelected = c === selectedCount;
          return (
            <button
              key={c}
              onClick={() => onSelect(c)}
              className={`flex-1 h-11 rounded-xl text-sm font-bold transition-colors ${
                isSelected ? "bg-primary-indigo text-white" : "bg-white border border-slate-200 text-slate-700"
              }`}
            >
              {c}
            </button>
          );
        })}
      </div>
    </div>
  );
}

function PillOption({ label, isSelected, onClick }: { label: string; isSelected: boolean; onClick: () => void }) {
  return (
    <button
      onClick={onClick}
      className={`flex-1 h-11 rounded-xl text-xs font-bold px-2 transition-colors ${
        isSelected ? "bg-primary-indigo text-white" : "bg-white border border-slate-200 text-slate-700"
      }`}
    >
      {label}
    </button>
  );
}

function StudyBanner({ title, subtitle, buttonText, onClick }: { title: string; subtitle: string; buttonText: string; onClick: () => void }) {
  return (
    <div className="bg-white border border-slate-200 rounded-2xl p-3.5 flex items-center justify-between gap-2">
      <div className="flex-1 min-w-0">
        <p className="text-xs font-bold text-slate-900">{title}</p>
        <p className="text-[10px] text-slate-500 font-medium">{subtitle}</p>
      </div>
      <button onClick={onClick} className="bg-primary-indigo text-white text-[11px] font-bold rounded-xl px-3 py-2 whitespace-nowrap">
        {buttonText}
      </button>
    </div>
  );
}

function InfoBanner({ color, children }: { color: "emerald" | "indigo" | "purple"; children: React.ReactNode }) {
  const styles = {
    emerald: "bg-emerald-50 border-emerald-200 text-emerald-800",
    indigo: "bg-indigo-50 border-indigo-200 text-indigo-800",
    purple: "bg-purple-50 border-purple-200 text-purple-800",
  };
  return (
    <div className={`${styles[color]} border rounded-2xl px-3.5 py-3 text-xs font-bold`}>
      {children}
    </div>
  );
}