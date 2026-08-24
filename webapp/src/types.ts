// Core models mirroring the Android app's Kotlin data classes

export type PracticeModeId =
  | "addition"
  | "subtraction"
  | "multiplication"
  | "tables"
  | "factors"
  | "division"
  | "complex"
  | "roots"
  | "grid";

export interface PracticeModeMeta {
  id: PracticeModeId;
  title: string;
  subtitle: string;
  symbol: string;
}

export const PRACTICE_MODES: PracticeModeMeta[] = [
  { id: "addition", title: "Addition Practice", subtitle: "Custom number range & operands", symbol: "+" },
  { id: "subtraction", title: "Subtraction Practice", subtitle: "Mental subtraction drills", symbol: "-" },
  { id: "multiplication", title: "Multiplication Practice", subtitle: "Custom range, factors & count", symbol: "×" },
  { id: "tables", title: "Tables Reverse Practice", subtitle: "Identify factors from product (e.g. 48 -> 24*2)", symbol: "×?" },
  { id: "factors", title: "Factors Practice", subtitle: "Identify factor pairs A × B = N (≤99)", symbol: "➗" },
  { id: "division", title: "Division Practice", subtitle: "Dividend & divisor range (no 0s)", symbol: "÷" },
  { id: "complex", title: "Complex Analysis", subtitle: "Diff between Sum(x,y) & Avg(a,b)", symbol: "∑" },
  { id: "roots", title: "Roots Practice", subtitle: "Square roots (≤100) & Cube roots (≤20)", symbol: "√" },
  { id: "grid", title: "Grid Addition Speed Run", subtitle: "5x5 matrix speed addition + totals", symbol: "▦" },
];

export function modeFromId(id: string): PracticeModeMeta {
  return PRACTICE_MODES.find((m) => m.id === id) ?? PRACTICE_MODES[0];
}

export type TableSelectionMode = "combinations" | "count";
export type FactorsQuestionMode = "enter_pair" | "choose_option";
export type RootMode = "sqroot" | "cbroot" | "both";
export type LearnTableViewMode = "multiplication" | "division";
export type ExponentPowerMode = "power2" | "power3" | "both";
export type ExponentDisplayType = "squares" | "cubes" | "both";
export type LearnRootType = "square" | "cube";
export type RootDisplayType = "sqroot" | "cbroot";

export interface MathQuestion {
  index: number;
  prompt: string;
  answer: string;
  numericAnswer?: number | null;
  type: string; // "standard" | "reverse-table" | "factors"
  targetNumber?: number | null;
  options?: string[];
  allValidAnswers?: string[];
  hint?: string;
}

export interface QuestionResult {
  prompt: string;
  userAnswer: string;
  expectedAnswer: string;
  isCorrect: boolean;
  timeTakenSec: number;
}

export interface GridCellResult {
  key: string;
  label: string;
  value: number;
  timeSec: number;
}

export interface PracticeConfig {
  mode: PracticeModeId;
  minRange: number;
  maxRange: number;
  numsPerQuestion: number;
  totalQuestions: number;
  factorsMin: number;
  factorsMax: number;
  factorsQuestionMode: FactorsQuestionMode;
  dividendMin: number;
  dividendMax: number;
  divisorMin: number;
  divisorMax: number;
  selectedTables: number[];
  tableSelectionMode: TableSelectionMode;
  sqRootMin: number;
  sqRootMax: number;
  cbRootMin: number;
  cbRootMax: number;
  rootMode: RootMode;
}

export function defaultConfig(mode: PracticeModeId = "addition"): PracticeConfig {
  switch (mode) {
    case "grid":
      return { ...baseConfig("grid"), minRange: 1, maxRange: 100 };
    case "factors":
      return {
        ...baseConfig("factors"),
        factorsMin: 100,
        factorsMax: 999,
        factorsQuestionMode: "enter_pair",
        totalQuestions: 5,
      };
    case "division":
      return {
        ...baseConfig("division"),
        dividendMin: 100,
        dividendMax: 999,
        divisorMin: 2,
        divisorMax: 20,
        totalQuestions: 5,
      };
    case "addition":
    case "subtraction":
      return { ...baseConfig(mode), minRange: 100, maxRange: 999, numsPerQuestion: 2, totalQuestions: 5 };
    case "multiplication":
      return { ...baseConfig(mode), minRange: 2, maxRange: 20, numsPerQuestion: 2, totalQuestions: 5 };
    case "tables":
      return {
        ...baseConfig(mode),
        selectedTables: Array.from({ length: 37 - 12 + 1 }, (_, i) => i + 12),
        tableSelectionMode: "combinations",
        totalQuestions: 5,
      };
    case "roots":
      return {
        ...baseConfig(mode),
        sqRootMin: 1,
        sqRootMax: 100,
        cbRootMin: 1,
        cbRootMax: 20,
        rootMode: "sqroot",
        totalQuestions: 10,
      };
    case "complex":
      return { ...baseConfig(mode), totalQuestions: 5 };
    default:
      return baseConfig(mode);
  }
}

function baseConfig(mode: PracticeModeId): PracticeConfig {
  return {
    mode,
    minRange: 100,
    maxRange: 999,
    numsPerQuestion: 2,
    totalQuestions: 5,
    factorsMin: 100,
    factorsMax: 999,
    factorsQuestionMode: "enter_pair",
    dividendMin: 100,
    dividendMax: 999,
    divisorMin: 2,
    divisorMax: 20,
    selectedTables: Array.from({ length: 37 - 12 + 1 }, (_, i) => i + 12),
    tableSelectionMode: "combinations",
    sqRootMin: 1,
    sqRootMax: 100,
    cbRootMin: 1,
    cbRootMax: 20,
    rootMode: "sqroot",
  };
}

export interface GridPlayState {
  rows: number[];
  cols: number[];
  currentStep: number; // 0..24 (cells), 25..29 (row sums), 30..34 (col sums), 35 (grand total)
  userAnswers: Record<string, GridCellResult>;
  stepStartTime: number;
  isError: boolean;
  activePrompt: string;
  expectedAnswer: number;
}

export interface StudyState {
  learnTableNum: number;
  learnTableViewMode: LearnTableViewMode;
  learnTableHideAnswers: boolean;
  revealedTableAnswers: string[];
  learnFactorNumber: number;
  learnFactorHideAnswers: boolean;
  revealedFactors: string[];
  exponentPowerMode: ExponentPowerMode;
  exponentRangeFilter: string;
  exponentHideAnswers: boolean;
  revealedExponents: string[];
  learnRootType: LearnRootType;
  learnRootRangeFilter: string;
  learnRootHideAnswers: boolean;
  revealedRoots: string[];
}

export function defaultStudyState(): StudyState {
  return {
    learnTableNum: 12,
    learnTableViewMode: "multiplication",
    learnTableHideAnswers: false,
    revealedTableAnswers: [],
    learnFactorNumber: 108,
    learnFactorHideAnswers: false,
    revealedFactors: [],
    exponentPowerMode: "power2",
    exponentRangeFilter: "2-10",
    exponentHideAnswers: false,
    revealedExponents: [],
    learnRootType: "square",
    learnRootRangeFilter: "1-20",
    learnRootHideAnswers: false,
    revealedRoots: [],
  };
}

// Database entities
export interface PracticeSessionEntity {
  id: number;
  mode: string;
  totalTimeSec: number;
  timestamp: number;
  totalQuestions: number;
  correctCount: number;
  rangeInfo: string;
  detailsJson: string;
}

export interface UserGoalEntity {
  id: number;
  userName: string;
  dailyTargetQuestions: number;
  reminderHour: number;
  reminderMinute: number;
  reminderEnabled: boolean;
  currentStreak: number;
  lastPracticeDateEpochDay: number;
}

export function defaultUserGoal(): UserGoalEntity {
  return {
    id: 1,
    userName: "",
    dailyTargetQuestions: 20,
    reminderHour: 19,
    reminderMinute: 0,
    reminderEnabled: true,
    currentStreak: 0,
    lastPracticeDateEpochDay: 0,
  };
}

export type ScreenDestination =
  | { type: "home" }
  | { type: "config"; mode: PracticeModeId; backDestination: ScreenDestination }
  | { type: "practice" }
  | { type: "grid" }
  | { type: "result"; mode: PracticeModeId }
  | { type: "analysis"; mode: PracticeModeId; lastCompletionTime?: number | null; backDestination: ScreenDestination }
  | { type: "history" }
  | { type: "dashboard" }
  | { type: "learnTables"; backDestination: ScreenDestination }
  | { type: "learnExponents"; backDestination: ScreenDestination }
  | { type: "learnRoots"; backDestination: ScreenDestination }
  | { type: "learnFactors"; backDestination: ScreenDestination }
  | { type: "privacy"; backDestination: ScreenDestination };
