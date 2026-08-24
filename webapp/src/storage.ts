// localStorage persistence layer (replaces Room database)

import type { PracticeSessionEntity, UserGoalEntity } from "./types";

const SESSIONS_KEY = "math_master_sessions";
const GOAL_KEY = "math_master_goal";

function read<T>(key: string, fallback: T): T {
  try {
    const raw = localStorage.getItem(key);
    if (raw == null) return fallback;
    return JSON.parse(raw) as T;
  } catch {
    return fallback;
  }
}

function write(key: string, value: unknown): void {
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch {
    // storage full or unavailable
  }
}

export const storage = {
  loadSessions(): PracticeSessionEntity[] {
    return read<PracticeSessionEntity[]>(SESSIONS_KEY, []);
  },
  saveSessions(sessions: PracticeSessionEntity[]): void {
    write(SESSIONS_KEY, sessions);
  },
  loadGoal(): UserGoalEntity | null {
    return read<UserGoalEntity | null>(GOAL_KEY, null);
  },
  saveGoal(goal: UserGoalEntity): void {
    write(GOAL_KEY, goal);
  },
  clear(): void {
    localStorage.removeItem(SESSIONS_KEY);
    localStorage.removeItem(GOAL_KEY);
  },
};

export function daysSinceEpoch(ts: number): number {
  return Math.floor(ts / 86400000);
}