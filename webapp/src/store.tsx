// Global state management (mirrors MathViewModel)

import React, { createContext, useContext, useReducer, useMemo, useEffect, useCallback, useRef } from "react";
import type {
  MathQuestion,
  PracticeConfig,
  PracticeModeId,
  PracticeSessionEntity,
  QuestionResult,
  ScreenDestination,
  GridPlayState,
  StudyState,
  UserGoalEntity,
  GridCellResult,
} from "./types";
import { defaultConfig, defaultStudyState, defaultUserGoal } from "./types";
import { generateQuestions, checkAnswer, updateStreakOnPractice, parseFactorPairs } from "./engine";
import { storage } from "./storage";

// ---- State shape ----
export interface AppState {
  destination: ScreenDestination;
  previousDestination: ScreenDestination;
  config: PracticeConfig;
  questions: MathQuestion[];
  currentQuestionIndex: number;
  currentInput: string;
  isAnswerError: boolean;
  isPaused: boolean;
  elapsedSeconds: number;
  results: QuestionResult[];
  finalCompletionTime: number;
  grid: GridPlayState;
  study: StudyState;
  sessions: PracticeSessionEntity[];
  goal: UserGoalEntity | null;
  showNamePromptDialog: boolean;
  // Tables reverse-practice: multi-pair state
  tablesMultiPairPrompt: string | null;
  tablesWarningMessage: string | null;
  foundPairsForCurrentQuestion: [number, number][];
  canContinueNext: boolean;
  // Factors practice: hidden options reveal
  showFactorOptions: boolean;
}

const initialGoal = storage.loadGoal() ?? defaultUserGoal();

const initialState: AppState = {
  destination: { type: "home" },
  previousDestination: { type: "home" },
  config: defaultConfig("addition"),
  questions: [],
  currentQuestionIndex: 0,
  currentInput: "",
  isAnswerError: false,
  isPaused: false,
  elapsedSeconds: 0,
  results: [],
  finalCompletionTime: 0,
  grid: { rows: [], cols: [], currentStep: 0, userAnswers: {}, stepStartTime: 0, isError: false, activePrompt: "", expectedAnswer: 0 },
  study: defaultStudyState(),
  sessions: storage.loadSessions(),
  goal: initialGoal,
  // On first launch (no saved name), auto-show the "What is your name?" dialog.
  showNamePromptDialog: initialGoal.userName.trim() === "",
  tablesMultiPairPrompt: null,
  tablesWarningMessage: null,
  foundPairsForCurrentQuestion: [],
  canContinueNext: false,
  showFactorOptions: false,
};

// ---- Actions ----
type Action =
  | { type: "NAVIGATE"; dest: ScreenDestination }
  | { type: "OPEN_NAME_PROMPT" }
  | { type: "CLOSE_NAME_PROMPT" }
  | { type: "SET_NAME"; name: string }
  | { type: "SELECT_MODE"; mode: PracticeModeId; backDest?: ScreenDestination }
  | { type: "UPDATE_CONFIG"; config: PracticeConfig }
  | { type: "TOGGLE_TABLE"; table: number }
  | { type: "START_PRACTICE" }
  | { type: "START_GRID" }
  | { type: "UPDATE_INPUT"; input: string }
  | { type: "SET_ERROR"; value: boolean }
  | { type: "TICK"; elapsed: number }
  | { type: "SET_PAUSED"; value: boolean }
  | { type: "SUBMIT_CORRECT"; result: QuestionResult }
  | { type: "ADVANCE_QUESTION" }
  | { type: "FINISH"; mode: PracticeModeId }
  | { type: "TABLES_PROGRESS"; foundPairs: [number, number][]; prompt: string | null; warning: string | null; canContinue: boolean }
  | { type: "REVEAL_FACTOR_OPTIONS" }
  | { type: "GRID_SUBMIT"; input: string }
  | { type: "GRID_FINISH"; totalTime: number }
  | { type: "SAVE_GOAL"; target: number; hour: number; minute: number; enabled: boolean }
  | { type: "DELETE_SESSION"; id: number }
  | { type: "CLEAR_HISTORY" }
  | { type: "STUDY_SET_TABLE"; num: number }
  | { type: "STUDY_SET_TABLE_VIEW"; mode: StudyState["learnTableViewMode"] }
  | { type: "STUDY_TOGGLE_TABLE_HIDE" }
  | { type: "STUDY_TOGGLE_TABLE_REVEAL"; key: string }
  | { type: "STUDY_SET_EXPONENT_MODE"; mode: StudyState["exponentPowerMode"] }
  | { type: "STUDY_SET_EXPONENT_RANGE"; range: string }
  | { type: "STUDY_TOGGLE_EXPONENT_HIDE" }
  | { type: "STUDY_TOGGLE_EXPONENT_REVEAL"; key: string }
  | { type: "STUDY_SET_ROOT_TYPE"; rootType: StudyState["learnRootType"] }
  | { type: "STUDY_SET_ROOT_RANGE"; range: string }
  | { type: "STUDY_TOGGLE_ROOT_HIDE" }
  | { type: "STUDY_TOGGLE_ROOT_REVEAL"; key: string }
  | { type: "STUDY_SET_FACTOR_NUM"; num: number }
  | { type: "STUDY_TOGGLE_FACTOR_HIDE" }
  | { type: "STUDY_TOGGLE_FACTOR_REVEAL"; key: string };

function toggleInSet(set: string[], key: string): string[] {
  return set.includes(key) ? set.filter((k) => k !== key) : [...set, key];
}

function getRandomUnique(min: number, max: number, existing: number[]): number {
  let attempts = 0;
  let num: number;
  do {
    num = Math.floor(Math.random() * (max - min + 1)) + min;
    attempts++;
    if (attempts > 500) break;
  } while (existing.includes(num) || num % 10 === 0);
  return num;
}

function buildRangeInfo(state: AppState, mode: PracticeModeId): string {
  const c = state.config;
  switch (mode) {
    case "addition":
    case "subtraction":
    case "multiplication":
      return `${c.minRange}-${c.maxRange}`;
    case "division":
      return `${c.dividendMin}..${c.dividendMax} ÷ ${c.divisorMin}..${c.divisorMax}`;
    case "tables":
      return `Tables: ${[...c.selectedTables].sort((a, b) => a - b).join(",")}`;
    case "roots":
      return `Roots: ${c.rootMode}`;
    case "factors":
      return `Factors: ${c.factorsMin}-${c.factorsMax}`;
    case "grid":
      return `Grid: ${c.minRange}-${c.maxRange}`;
    case "complex":
      return "Complex";
    default:
      return "";
  }
}

function reducer(state: AppState, action: Action): AppState {
  switch (action.type) {
    case "NAVIGATE": {
      return { ...state, previousDestination: state.destination, destination: action.dest };
    }
    case "OPEN_NAME_PROMPT":
      return { ...state, showNamePromptDialog: true };
    case "CLOSE_NAME_PROMPT":
      return { ...state, showNamePromptDialog: false };
    case "SET_NAME": {
      const goal = { ...(state.goal ?? defaultUserGoal()), userName: action.name.trim() };
      storage.saveGoal(goal);
      return { ...state, goal, showNamePromptDialog: false };
    }
    case "SELECT_MODE": {
      const backDest = action.backDest ?? ({ type: "home" } as const);
      return {
        ...state,
        config: defaultConfig(action.mode),
        destination: { type: "config", mode: action.mode, backDestination: backDest },
        previousDestination: backDest,
      };
    }
    case "UPDATE_CONFIG":
      return { ...state, config: action.config };
    case "TOGGLE_TABLE": {
      const sel = state.config.selectedTables;
      const next = sel.includes(action.table) ? sel.filter((t) => t !== action.table) : [...sel, action.table];
      return { ...state, config: { ...state.config, selectedTables: next } };
    }
    case "START_PRACTICE": {
      const questions = generateQuestions(state.config);
      return {
        ...state,
        destination: { type: "practice" },
        questions,
        currentQuestionIndex: 0,
        currentInput: "",
        isAnswerError: false,
        isPaused: false,
        elapsedSeconds: 0,
        results: [],
        tablesMultiPairPrompt: null,
        tablesWarningMessage: null,
        foundPairsForCurrentQuestion: [],
        canContinueNext: false,
        showFactorOptions: false,
      };
    }
    case "START_GRID": {
      const existing: number[] = [];
      const rows: number[] = [];
      const cols: number[] = [];
      for (let i = 0; i < 5; i++) {
        const r = getRandomUnique(state.config.minRange, state.config.maxRange, existing);
        rows.push(r);
        existing.push(r);
        const c = getRandomUnique(state.config.minRange, state.config.maxRange, existing);
        cols.push(c);
        existing.push(c);
      }
      const grid: GridPlayState = {
        rows,
        cols,
        currentStep: 0,
        userAnswers: {},
        stepStartTime: Date.now(),
        isError: false,
        activePrompt: `${rows[0]} + ${cols[0]}`,
        expectedAnswer: rows[0] + cols[0],
      };
      return {
        ...state,
        destination: { type: "grid" },
        grid,
        isPaused: false,
        elapsedSeconds: 0,
        currentInput: "",
      };
    }
    case "UPDATE_INPUT":
      return { ...state, currentInput: action.input, isAnswerError: false, tablesWarningMessage: null };
    case "SET_ERROR":
      return { ...state, isAnswerError: action.value };
    case "TICK":
      return { ...state, elapsedSeconds: action.elapsed };
    case "SET_PAUSED":
      return { ...state, isPaused: action.value };
    case "SUBMIT_CORRECT":
      return { ...state, results: [...state.results, action.result], currentInput: "" };
    case "ADVANCE_QUESTION":
      return {
        ...state,
        currentQuestionIndex: state.currentQuestionIndex + 1,
        currentInput: "",
        isAnswerError: false,
        tablesMultiPairPrompt: null,
        tablesWarningMessage: null,
        foundPairsForCurrentQuestion: [],
        canContinueNext: false,
        showFactorOptions: false,
      };
    case "TABLES_PROGRESS":
      return {
        ...state,
        currentInput: "",
        isAnswerError: false,
        foundPairsForCurrentQuestion: action.foundPairs,
        tablesMultiPairPrompt: action.prompt,
        tablesWarningMessage: action.warning,
        canContinueNext: action.canContinue,
      };
    case "REVEAL_FACTOR_OPTIONS":
      return { ...state, showFactorOptions: true };
    case "FINISH": {
      const results = state.results;
      const totalTime = state.elapsedSeconds;
      const detailsJson = JSON.stringify(results.map((r) => ({ prompt: r.prompt, userAnswer: r.userAnswer, expected: r.expectedAnswer, time: r.timeTakenSec })));
      const rangeInfo = buildRangeInfo(state, action.mode);
      const timestamp = Date.now();
      const session: PracticeSessionEntity = {
        id: timestamp,
        mode: action.mode,
        totalTimeSec: totalTime,
        timestamp,
        totalQuestions: results.length,
        correctCount: results.filter((r) => r.isCorrect).length,
        rangeInfo,
        detailsJson,
      };
      const sessions = [session, ...state.sessions];
      storage.saveSessions(sessions);
      const goal = state.goal ?? defaultUserGoal();
      const streak = updateStreakOnPractice(goal.currentStreak, goal.lastPracticeDateEpochDay);
      const newGoal = { ...goal, currentStreak: streak.currentStreak, lastPracticeDateEpochDay: streak.lastPracticeDateEpochDay };
      storage.saveGoal(newGoal);
      return {
        ...state,
        sessions,
        goal: newGoal,
        destination: { type: "result", mode: action.mode },
        finalCompletionTime: totalTime,
      };
    }
    case "GRID_SUBMIT": {
      const grid = state.grid;
      const valInt = parseInt(action.input.trim(), 10);
      if (isNaN(valInt)) return state;
      const stepTime = (Date.now() - grid.stepStartTime) / 1000;
      const currentStep = grid.currentStep;
      const key =
        currentStep < 25
          ? `r${Math.floor(currentStep / 5)}c${currentStep % 5}`
          : currentStep < 30
            ? `rowSum${currentStep - 25}`
            : currentStep < 35
              ? `colSum${currentStep - 30}`
              : "grand";
      const label = grid.activePrompt;
      const newAnswers = { ...grid.userAnswers, [key]: { key, label, value: valInt, timeSec: stepTime } as GridCellResult };

      if (currentStep < 35) {
        const nextStep = currentStep + 1;
        let nextPrompt = "";
        let nextExpected = 0;
        if (nextStep < 25) {
          const r = Math.floor(nextStep / 5);
          const c = nextStep % 5;
          nextPrompt = `${grid.rows[r]} + ${grid.cols[c]}`;
          nextExpected = grid.rows[r] + grid.cols[c];
        } else if (nextStep < 30) {
          const r = nextStep - 25;
          nextPrompt = `Row ${r + 1} Total`;
          for (let c = 0; c < 5; c++) nextExpected += newAnswers[`r${r}c${c}`]?.value ?? 0;
        } else if (nextStep < 35) {
          const c = nextStep - 30;
          nextPrompt = `Col ${c + 1} Total`;
          for (let r = 0; r < 5; r++) nextExpected += newAnswers[`r${r}c${c}`]?.value ?? 0;
        } else {
          nextPrompt = "GRAND TOTAL";
          for (let r = 0; r < 5; r++) nextExpected += newAnswers[`rowSum${r}`]?.value ?? 0;
        }
        const nextGrid: GridPlayState = {
          ...grid,
          currentStep: nextStep,
          userAnswers: newAnswers,
          stepStartTime: Date.now(),
          isError: false,
          activePrompt: nextPrompt,
          expectedAnswer: nextExpected,
        };
        return { ...state, currentInput: "", grid: nextGrid };
      }
      // finish grid
      const totalTime = state.elapsedSeconds;
      const detailsJson = JSON.stringify(Object.values(newAnswers).map((a) => ({ key: a.key, label: a.label, val: a.value, time: a.timeSec })));
      const timestamp = Date.now();
      const session: PracticeSessionEntity = {
        id: timestamp,
        mode: "grid",
        totalTimeSec: totalTime,
        timestamp,
        totalQuestions: 36,
        correctCount: 36,
        rangeInfo: `${state.config.minRange}-${state.config.maxRange}`,
        detailsJson,
      };
      const sessions = [session, ...state.sessions];
      storage.saveSessions(sessions);
      const goal = state.goal ?? defaultUserGoal();
      const streak = updateStreakOnPractice(goal.currentStreak, goal.lastPracticeDateEpochDay);
      const newGoal = { ...goal, currentStreak: streak.currentStreak, lastPracticeDateEpochDay: streak.lastPracticeDateEpochDay };
      storage.saveGoal(newGoal);
      return {
        ...state,
        sessions,
        goal: newGoal,
        destination: { type: "result", mode: "grid" },
        finalCompletionTime: totalTime,
        grid: { ...grid, userAnswers: newAnswers },
      };
    }
    case "GRID_FINISH": {
      // Handled by GRID_SUBMIT for the last step; kept for completeness
      return state;
    }
    case "SAVE_GOAL": {
      const goal = { ...(state.goal ?? defaultUserGoal()), dailyTargetQuestions: action.target, reminderHour: action.hour, reminderMinute: action.minute, reminderEnabled: action.enabled };
      storage.saveGoal(goal);
      return { ...state, goal };
    }
    case "DELETE_SESSION": {
      const sessions = state.sessions.filter((s) => s.id !== action.id);
      storage.saveSessions(sessions);
      return { ...state, sessions };
    }
    case "CLEAR_HISTORY": {
      storage.saveSessions([]);
      return { ...state, sessions: [] };
    }
    case "STUDY_SET_TABLE":
      return { ...state, study: { ...state.study, learnTableNum: action.num, revealedTableAnswers: [] } };
    case "STUDY_SET_TABLE_VIEW":
      return { ...state, study: { ...state.study, learnTableViewMode: action.mode } };
    case "STUDY_TOGGLE_TABLE_HIDE":
      return { ...state, study: { ...state.study, learnTableHideAnswers: !state.study.learnTableHideAnswers, revealedTableAnswers: [] } };
    case "STUDY_TOGGLE_TABLE_REVEAL":
      return { ...state, study: { ...state.study, revealedTableAnswers: toggleInSet(state.study.revealedTableAnswers, action.key) } };
    case "STUDY_SET_EXPONENT_MODE":
      return { ...state, study: { ...state.study, exponentPowerMode: action.mode } };
    case "STUDY_SET_EXPONENT_RANGE":
      return { ...state, study: { ...state.study, exponentRangeFilter: action.range } };
    case "STUDY_TOGGLE_EXPONENT_HIDE":
      return { ...state, study: { ...state.study, exponentHideAnswers: !state.study.exponentHideAnswers, revealedExponents: [] } };
    case "STUDY_TOGGLE_EXPONENT_REVEAL":
      return { ...state, study: { ...state.study, revealedExponents: toggleInSet(state.study.revealedExponents, action.key) } };
    case "STUDY_SET_ROOT_TYPE": {
      const defaultRange = action.rootType === "square" ? "1-20" : "1-10";
      return { ...state, study: { ...state.study, learnRootType: action.rootType, learnRootRangeFilter: defaultRange, revealedRoots: [] } };
    }
    case "STUDY_SET_ROOT_RANGE":
      return { ...state, study: { ...state.study, learnRootRangeFilter: action.range } };
    case "STUDY_TOGGLE_ROOT_HIDE":
      return { ...state, study: { ...state.study, learnRootHideAnswers: !state.study.learnRootHideAnswers, revealedRoots: [] } };
    case "STUDY_TOGGLE_ROOT_REVEAL":
      return { ...state, study: { ...state.study, revealedRoots: toggleInSet(state.study.revealedRoots, action.key) } };
    case "STUDY_SET_FACTOR_NUM":
      return { ...state, study: { ...state.study, learnFactorNumber: Math.min(Math.max(action.num, 10), 9999), revealedFactors: [] } };
    case "STUDY_TOGGLE_FACTOR_HIDE":
      return { ...state, study: { ...state.study, learnFactorHideAnswers: !state.study.learnFactorHideAnswers, revealedFactors: [] } };
    case "STUDY_TOGGLE_FACTOR_REVEAL":
      return { ...state, study: { ...state.study, revealedFactors: toggleInSet(state.study.revealedFactors, action.key) } };
    default:
      return state;
  }
}

// ---- Timer hook ----
function useSessionTimer(active: boolean, paused: boolean, onTick: (elapsed: number) => void) {
  const pausedRef = useRef(paused);
  const accumulatedRef = useRef(0);
  const startRef = useRef(Date.now());

  pausedRef.current = paused;

  useEffect(() => {
    if (!active) {
      accumulatedRef.current = 0;
      startRef.current = Date.now();
      return;
    }
    startRef.current = Date.now();
    const interval = window.setInterval(() => {
      if (!pausedRef.current) {
        const totalMs = accumulatedRef.current + (Date.now() - startRef.current);
        onTick(totalMs / 1000);
      }
    }, 50);
    return () => window.clearInterval(interval);
  }, [active, onTick]);
}

// ---- Context ----
interface AppContextValue {
  state: AppState;
  dispatch: React.Dispatch<Action>;
  navigate: (dest: ScreenDestination) => void;
  startPractice: () => void;
  submitAnswer: () => void;
  togglePause: () => void;
  submitGridAnswer: (input: string) => void;
  saveDailyGoal: (target: number, hour: number, minute: number, enabled: boolean) => void;
  goBack: () => void;
  continueToNext: () => void;
  revealFactorOptions: () => void;
}

const AppContext = createContext<AppContextValue | null>(null);

export function AppProvider({ children }: { children: React.ReactNode }) {
  const [state, dispatch] = useReducer(reducer, initialState);

  const navigate = useCallback((dest: ScreenDestination) => {
    // Push browser history so the back button can navigate back in-app.
    try {
      window.history.pushState({ dest: JSON.stringify(dest) }, "");
    } catch {
      // ignore
    }
    dispatch({ type: "NAVIGATE", dest });
  }, []);

  const startPractice = useCallback(() => {
    dispatch({ type: state.config.mode === "grid" ? "START_GRID" : "START_PRACTICE" });
  }, [state.config.mode]);

  const submitAnswer = useCallback(() => {
    const { questions, currentQuestionIndex, currentInput, config, elapsedSeconds, foundPairsForCurrentQuestion, canContinueNext } = state;
    if (currentQuestionIndex >= questions.length) return;
    const q = questions[currentQuestionIndex];
    const rawInput = currentInput.trim();

    // ---- Tables reverse-practice: multi-pair flow ----
    if (q.type === "reverse-table") {
      const targetProd = q.targetNumber ?? parseInt(q.prompt, 10) ?? 0;
      const validPairsInSelected: [number, number][] =
        q.allValidTablePairs && q.allValidTablePairs.length > 0
          ? q.allValidTablePairs
          : (q.selectedTablesForQuestion && q.selectedTablesForQuestion.length > 0
              ? q.selectedTablesForQuestion
              : config.selectedTables
            )
              .filter((t) => targetProd % t === 0 && targetProd / t >= 2 && targetProd / t <= 99)
              .map((t) => [t, targetProd / t] as [number, number]);
      const selectedTables =
        q.selectedTablesForQuestion && q.selectedTablesForQuestion.length > 0
          ? q.selectedTablesForQuestion
          : config.selectedTables;

      const parsedPairs = parseFactorPairs(rawInput);
      if (parsedPairs.length === 0) {
        dispatch({ type: "SET_ERROR", value: true });
        window.setTimeout(() => dispatch({ type: "SET_ERROR", value: false }), 500);
        return;
      }

      let hasInvalidProduct = false;
      let outsideTableWarning: string | null = null;
      const matchedCanonicalPairs: [number, number][] = [];

      for (const [a, b] of parsedPairs) {
        if (a * b !== targetProd) {
          hasInvalidProduct = true;
          break;
        }
        const matched = validPairsInSelected.find(
          ([x, y]) => (x === a && y === b) || (x === b && y === a)
        );
        if (matched) {
          matchedCanonicalPairs.push(matched);
        } else {
          const selListStr = [...selectedTables].sort((x, y) => x - y).join(", ");
          outsideTableWarning = `⚠️ ${a} × ${b} = ${targetProd} is valid, but is outside your selected tables (${selListStr}). Please choose within your selected table numbers!`;
        }
      }

      if (hasInvalidProduct) {
        dispatch({ type: "SET_ERROR", value: true });
        window.setTimeout(() => dispatch({ type: "SET_ERROR", value: false }), 500);
        return;
      }

      if (outsideTableWarning != null && matchedCanonicalPairs.length === 0) {
        dispatch({
          type: "TABLES_PROGRESS",
          foundPairs: foundPairsForCurrentQuestion,
          prompt: null,
          warning: outsideTableWarning,
          canContinue: canContinueNext,
        });
        return;
      }

      const currentFound = [...foundPairsForCurrentQuestion];
      matchedCanonicalPairs.forEach((cp) => {
        if (!currentFound.some(([x, y]) => (x === cp[0] && y === cp[1]) || (x === cp[1] && y === cp[0]))) {
          currentFound.push(cp);
        }
      });

      const totalPossible = validPairsInSelected.length;

      if (currentFound.length >= totalPossible || totalPossible <= 1) {
        // All pairs found (or only one possible) — record the result and move on.
        const formatted = currentFound.map(([a, b]) => `${a} × ${b}`).join(", ");
        const expected = validPairsInSelected.map(([a, b]) => `${a} × ${b}`).join(", ");
        const result: QuestionResult = {
          prompt: `Table factor pairs of ${targetProd}`,
          userAnswer: formatted,
          expectedAnswer: expected || q.answer,
          isCorrect: true,
          timeTakenSec: elapsedSeconds,
        };
        dispatch({ type: "SUBMIT_CORRECT", result });
        if (currentQuestionIndex + 1 < questions.length) {
          dispatch({ type: "ADVANCE_QUESTION" });
        } else {
          dispatch({ type: "FINISH", mode: config.mode });
        }
      } else {
        const foundStr = currentFound.map(([a, b]) => `${a}×${b}`).join(", ");
        const promptMsg = `Think of other pairs if you remember (${currentFound.length} of ${totalPossible} found: ${foundStr})`;
        dispatch({
          type: "TABLES_PROGRESS",
          foundPairs: currentFound,
          prompt: promptMsg,
          warning: outsideTableWarning,
          canContinue: true,
        });
      }
      return;
    }

    // ---- Factors practice ----
    if (q.type === "factors" || config.mode === "factors") {
      const inputVal = rawInput.replace(/\s/g, "").replace(/x/g, "*").replace(/X/g, "*").replace(/×/g, "*");
      const tokens = inputVal.split(/[*xX×, ]+/).filter(Boolean);
      let isCorrect = false;
      if (tokens.length === 2) {
        const a = parseInt(tokens[0], 10);
        const b = parseInt(tokens[1], 10);
        const target = q.targetNumber ?? parseInt(q.prompt, 10) ?? 0;
        if (!isNaN(a) && !isNaN(b) && a >= 2 && b >= 2 && a <= 99 && b <= 99 && a * b === target) {
          isCorrect = true;
        }
      } else {
        isCorrect = q.allValidAnswers?.some((v) => v.replace(/\s/g, "").toLowerCase() === inputVal.toLowerCase()) ?? false;
      }
      if (!isCorrect) {
        dispatch({ type: "SET_ERROR", value: true });
        window.setTimeout(() => dispatch({ type: "SET_ERROR", value: false }), 500);
        return;
      }
      const result: QuestionResult = {
        prompt: `Factors of ${q.targetNumber ?? q.prompt}`,
        userAnswer: tokens.length === 2 ? `${tokens[0]} × ${tokens[1]}` : inputVal,
        expectedAnswer: q.hint ? q.hint : q.answer,
        isCorrect: true,
        timeTakenSec: elapsedSeconds,
      };
      dispatch({ type: "SUBMIT_CORRECT", result });
      if (currentQuestionIndex + 1 < questions.length) {
        dispatch({ type: "ADVANCE_QUESTION" });
      } else {
        dispatch({ type: "FINISH", mode: config.mode });
      }
      return;
    }

    // ---- Standard numeric / string questions ----
    const isCorrect = checkAnswer(currentInput, q, config);
    if (!isCorrect) {
      dispatch({ type: "SET_ERROR", value: true });
      window.setTimeout(() => dispatch({ type: "SET_ERROR", value: false }), 500);
      return;
    }
    const inputVal = currentInput.trim().replace(/\s/g, "").replace(/x/g, "*").replace(/X/g, "*");
    const result: QuestionResult = {
      prompt: q.prompt,
      userAnswer: inputVal,
      expectedAnswer: q.answer,
      isCorrect: true,
      timeTakenSec: elapsedSeconds,
    };
    dispatch({ type: "SUBMIT_CORRECT", result });
    if (currentQuestionIndex + 1 < questions.length) {
      dispatch({ type: "ADVANCE_QUESTION" });
    } else {
      dispatch({ type: "FINISH", mode: config.mode });
    }
  }, [state]);

  const continueToNext = useCallback(() => {
    const { questions, currentQuestionIndex, config, elapsedSeconds, foundPairsForCurrentQuestion } = state;
    if (currentQuestionIndex >= questions.length) return;
    const q = questions[currentQuestionIndex];
    const target = q.targetNumber ?? parseInt(q.prompt, 10) ?? 0;
    const validPairs = q.allValidTablePairs ?? [];
    const formattedUserAnswer =
      foundPairsForCurrentQuestion.length > 0
        ? foundPairsForCurrentQuestion.map(([a, b]) => `${a} × ${b}`).join(", ") +
          (validPairs.length > 1 ? ` (${foundPairsForCurrentQuestion.length}/${validPairs.length} found)` : "")
        : "Skipped";
    const expected = validPairs.length > 0 ? validPairs.map(([a, b]) => `${a} × ${b}`).join(", ") : q.answer;
    const result: QuestionResult = {
      prompt: `Table factor pairs of ${target}`,
      userAnswer: formattedUserAnswer,
      expectedAnswer: expected,
      isCorrect: foundPairsForCurrentQuestion.length > 0,
      timeTakenSec: elapsedSeconds,
    };
    dispatch({ type: "SUBMIT_CORRECT", result });
    if (currentQuestionIndex + 1 < questions.length) {
      dispatch({ type: "ADVANCE_QUESTION" });
    } else {
      dispatch({ type: "FINISH", mode: config.mode });
    }
  }, [state]);

  const revealFactorOptions = useCallback(() => {
    dispatch({ type: "REVEAL_FACTOR_OPTIONS" });
  }, []);

  const submitGridAnswer = useCallback(
    (input: string) => {
      const grid = state.grid;
      if (!grid || grid.rows.length === 0) return;
      const valInt = parseInt(input.trim(), 10);
      if (isNaN(valInt)) return;
      if (valInt !== grid.expectedAnswer) {
        dispatch({ type: "SET_ERROR", value: true });
        window.setTimeout(() => dispatch({ type: "SET_ERROR", value: false }), 500);
        return;
      }
      dispatch({ type: "GRID_SUBMIT", input });
    },
    [state.grid]
  );

  const togglePause = useCallback(() => {
    dispatch({ type: "SET_PAUSED", value: !state.isPaused });
  }, [state.isPaused]);

  const saveDailyGoal = useCallback((target: number, hour: number, minute: number, enabled: boolean) => {
    dispatch({ type: "SAVE_GOAL", target, hour, minute, enabled });
  }, []);

  const goBack = useCallback(() => {
    const dest = state.destination;
    switch (dest.type) {
      case "config":
        navigate(dest.backDestination);
        break;
      case "practice":
        navigate({ type: "config", mode: state.config.mode, backDestination: { type: "home" } });
        break;
      case "grid":
        navigate({ type: "config", mode: "grid", backDestination: { type: "home" } });
        break;
      case "result":
        navigate({ type: "home" });
        break;
      case "analysis":
        navigate(dest.backDestination);
        break;
      case "history":
        navigate({ type: "dashboard" });
        break;
      case "dashboard":
        navigate({ type: "home" });
        break;
      case "learnTables":
      case "learnFactors":
      case "learnExponents":
      case "learnRoots":
      case "privacy":
        navigate(dest.backDestination);
        break;
      default:
        break;
    }
  }, [state.destination, state.config.mode, navigate]);

  // Session timer: active on practice/grid screens
  const isSessionActive = state.destination.type === "practice" || state.destination.type === "grid";
  const handleTick = useCallback((elapsed: number) => {
    dispatch({ type: "TICK", elapsed });
  }, []);
  useSessionTimer(isSessionActive, state.isPaused, handleTick);

  // Browser back button
  useEffect(() => {
    const handler = () => {
      if (state.destination.type !== "home") {
        goBack();
      }
    };
    window.addEventListener("popstate", handler);
    return () => window.removeEventListener("popstate", handler);
  }, [state.destination, goBack]);

  // ---- Automatic physical keyboard input (desktop) ----
  // Active only during Practice / Grid sessions so users can type answers
  // directly instead of clicking the on-screen keypad.
  const stateRef = useRef(state);
  stateRef.current = state;
  const submitAnswerRef = useRef(submitAnswer);
  submitAnswerRef.current = submitAnswer;
  const submitGridAnswerRef = useRef(submitGridAnswer);
  submitGridAnswerRef.current = submitGridAnswer;

  const destType = state.destination.type;
  useEffect(() => {
    if (destType !== "practice" && destType !== "grid") return;

    const onKeyDown = (e: KeyboardEvent) => {
      if (e.metaKey || e.ctrlKey || e.altKey) return;
      const el = document.activeElement as HTMLElement | null;
      if (el && (el.tagName === "INPUT" || el.tagName === "TEXTAREA" || el.isContentEditable)) return;
      if (el && el.tagName === "BUTTON") el.blur();

      const s = stateRef.current;
      if (s.isPaused) return;

      const mode = s.config.mode;
      const q = s.questions[s.currentQuestionIndex];
      const isReverseTable = q?.type === "reverse-table";
      const isFactors = mode === "factors" || q?.type === "factors";
      const isComplex = mode === "complex";
      const isSub = mode === "subtraction";
      const isMul = mode === "multiplication" || isReverseTable || isFactors;
      const current = s.currentInput;
      const append = (str: string) => {
        e.preventDefault();
        dispatch({ type: "UPDATE_INPUT", input: current + str });
      };

      if (/^[0-9]$/.test(e.key)) {
        append(e.key);
        return;
      }
      if (e.key === "Backspace") {
        e.preventDefault();
        if (current.length > 0) dispatch({ type: "UPDATE_INPUT", input: current.slice(0, -1) });
        return;
      }
      if (e.key === "Enter") {
        e.preventDefault();
        if (s.destination.type === "grid") submitGridAnswerRef.current(current);
        else submitAnswerRef.current();
        return;
      }
      if (e.key === "," || e.key === ";") {
        if (isReverseTable || isFactors) append(", ");
        return;
      }
      if (e.key === "-" && (isSub || isComplex)) {
        append("-");
        return;
      }
      if (e.key === "." && isComplex) {
        append(".");
        return;
      }
      if ((e.key === "*" || e.key === "x" || e.key === "X") && isMul) {
        append("*");
        return;
      }
    };

    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [destType, dispatch]);

  const value = useMemo<AppContextValue>(
    () => ({
      state,
      dispatch,
      navigate,
      startPractice,
      submitAnswer,
      togglePause,
      submitGridAnswer,
      saveDailyGoal,
      goBack,
      continueToNext,
      revealFactorOptions,
    }),
    [state, navigate, startPractice, submitAnswer, togglePause, submitGridAnswer, saveDailyGoal, goBack, continueToNext, revealFactorOptions]
  );

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useApp(): AppContextValue {
  const ctx = useContext(AppContext);
  if (!ctx) throw new Error("useApp must be used within AppProvider");
  return ctx;
}