// Math engine: question generation, validation, streak logic

import type { MathQuestion, PracticeConfig, PracticeModeId, RootMode } from "./types";

// ---- Random Helpers ----
function randomInt(min: number, max: number): number {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function getRandomUnique(min: number, max: number, existing: number[]): number {
  let attempts = 0;
  let num: number;
  do {
    num = randomInt(min, max);
    attempts++;
    if (attempts > 500) break;
  } while (existing.includes(num) || num % 10 === 0);
  return num;
}

// ---- Factor Pairs ----
export function calculateValidFactorPairs(n: number, maxFactor: number = 99): [number, number][] {
  const pairs: [number, number][] = [];
  const limit = Math.min(maxFactor, Math.floor(Math.sqrt(n)));
  for (let a = 2; a <= limit; a++) {
    if (n % a === 0) {
      const b = n / a;
      if (b <= maxFactor && b >= 2) {
        pairs.push([a, b]);
      }
    }
  }
  return pairs.sort((a, b) => b[1] - a[1]);
}

// ---- Question Generation ----
export function generateQuestions(config: PracticeConfig): MathQuestion[] {
  const questions: MathQuestion[] = [];
  const count = config.totalQuestions;

  switch (config.mode) {
    case "addition": {
      for (let i = 0; i < count; i++) {
        const nums: number[] = [];
        for (let j = 0; j < config.numsPerQuestion; j++) {
          nums.push(getRandomUnique(config.minRange, config.maxRange, []));
        }
        const sum = nums.reduce((a, b) => a + b, 0);
        questions.push({ index: i, prompt: nums.join(" + "), answer: sum.toString(), numericAnswer: sum, type: "standard" });
      }
      break;
    }

    case "subtraction": {
      for (let i = 0; i < count; i++) {
        const nums: number[] = [];
        for (let j = 0; j < config.numsPerQuestion; j++) {
          nums.push(getRandomUnique(config.minRange, config.maxRange, []));
        }
        nums.sort((a, b) => b - a);
        let result = nums[0];
        for (let j = 1; j < nums.length; j++) result -= nums[j];
        questions.push({ index: i, prompt: nums.join(" - "), answer: result.toString(), numericAnswer: result, type: "standard" });
      }
      break;
    }

    case "multiplication": {
      for (let i = 0; i < count; i++) {
        const nums: number[] = [];
        for (let j = 0; j < config.numsPerQuestion; j++) {
          nums.push(getRandomUnique(config.minRange, config.maxRange, []));
        }
        let product = 1;
        nums.forEach((n) => (product *= n));
        questions.push({ index: i, prompt: nums.join(" × "), answer: product.toString(), numericAnswer: product, type: "standard" });
      }
      break;
    }

    case "tables": {
      const tables = config.selectedTables.length > 0 ? config.selectedTables : [12];
      if (config.tableSelectionMode === "combinations") {
        let idx = 0;
        tables.forEach((t) => {
          for (let factor = 2; factor <= 9; factor++) {
            const prod = t * factor;
            questions.push({ index: idx++, prompt: prod.toString(), answer: `${t}*${factor}`, type: "reverse-table" });
          }
        });
        questions.sort(() => Math.random() - 0.5);
      } else {
        for (let i = 0; i < count; i++) {
          const t = tables[Math.floor(Math.random() * tables.length)];
          const factor = randomInt(2, 9);
          const prod = t * factor;
          questions.push({ index: i, prompt: prod.toString(), answer: `${t}*${factor}`, type: "reverse-table" });
        }
      }
      break;
    }

    case "division": {
      for (let i = 0; i < count; i++) {
        const divisor = getRandomUnique(config.divisorMin, config.divisorMax, []);
        const maxQuotient = Math.max(Math.floor(config.dividendMax / divisor), 1);
        const minQuotient = Math.max(Math.floor(config.dividendMin / divisor), 1);
        const quotient = maxQuotient > minQuotient ? randomInt(minQuotient, maxQuotient) : minQuotient;
        const dividend = divisor * quotient;
        questions.push({ index: i, prompt: `${dividend} ÷ ${divisor}`, answer: quotient.toString(), numericAnswer: quotient, type: "standard" });
      }
      break;
    }

    case "complex": {
      for (let i = 0; i < count; i++) {
        const x = randomInt(10, 99);
        const y = randomInt(10, 99);
        const a = randomInt(10, 99);
        const b = randomInt(10, 99);
        const sum = x + y;
        const avg = (a + b) / 2;
        const diff = Math.abs(sum - avg);
        const formattedDiff = diff % 1 === 0 ? diff.toFixed(0) : diff.toFixed(1);
        questions.push({
          index: i,
          prompt: `Difference between Sum(${x}, ${y}) and Average(${a}, ${b})`,
          answer: formattedDiff,
          numericAnswer: diff,
          type: "standard",
        });
      }
      break;
    }

    case "roots": {
      for (let i = 0; i < count; i++) {
        const isSquare =
          config.rootMode === "sqroot" ? true : config.rootMode === "cbroot" ? false : Math.random() > 0.5;
        if (isSquare) {
          const base = randomInt(config.sqRootMin, config.sqRootMax);
          const square = base * base;
          questions.push({ index: i, prompt: `√${square}`, answer: base.toString(), numericAnswer: base, type: "standard" });
        } else {
          const base = randomInt(config.cbRootMin, config.cbRootMax);
          const cube = base * base * base;
          questions.push({ index: i, prompt: `∛${cube}`, answer: base.toString(), numericAnswer: base, type: "standard" });
        }
      }
      break;
    }

    case "factors": {
      const existingNums = new Set<number>();
      for (let i = 0; i < count; i++) {
        let targetNum = 0;
        let pairs: [number, number][] = [];
        let attempts = 0;
        while (attempts < 500) {
          attempts++;
          const candidate = randomInt(config.factorsMin, config.factorsMax);
          if (existingNums.has(candidate)) continue;
          const validPairs = calculateValidFactorPairs(candidate, 99);
          if (validPairs.length > 0) {
            targetNum = candidate;
            pairs = validPairs;
            existingNums.add(candidate);
            break;
          }
        }
        if (targetNum === 0) {
          targetNum = i % 2 === 0 ? 252 : 108;
          pairs = calculateValidFactorPairs(targetNum, 99);
        }

        const allValidList = pairs.flatMap(([a, b]) => [
          `${b} × ${a}`,
          `${a} × ${b}`,
          `${b}*${a}`,
          `${a}*${b}`,
        ]);
        const canonicalAnswer = `${pairs[0][1]} × ${pairs[0][0]}`;
        const pairsDisplay = pairs.map(([a, b]) => `${b} × ${a}`).join(", ");

        const correctPair = pairs[Math.floor(Math.random() * pairs.length)];
        const correctOption = `${correctPair[1]} × ${correctPair[0]}`;
        const distractors = new Set<string>();
        const offsetList = [-3, -2, -1, 1, 2, 3, 4, -4, 5, -5];
        let distAttempts = 0;
        while (distractors.size < 3 && distAttempts < 80) {
          distAttempts++;
          const da = Math.min(Math.max(correctPair[1] + offsetList[Math.floor(Math.random() * offsetList.length)], 2), 99);
          const db = Math.min(Math.max(correctPair[0] + offsetList[Math.floor(Math.random() * offsetList.length)], 2), 99);
          if (da * db !== targetNum && !distractors.has(`${da} × ${db}`) && !distractors.has(`${db} × ${da}`)) {
            distractors.add(`${da} × ${db}`);
          }
        }
        const options = ([...distractors].slice(0, 3).concat(correctOption)).sort(() => Math.random() - 0.5);

        questions.push({
          index: i,
          prompt: targetNum.toString(),
          answer: canonicalAnswer,
          type: "factors",
          targetNumber: targetNum,
          options,
          allValidAnswers: allValidList,
          hint: `Factor pairs: ${pairsDisplay}`,
        });
      }
      break;
    }

    case "grid":
      break;
  }

  return questions;
}

// ---- Answer Validation ----
export function checkAnswer(
  input: string,
  question: MathQuestion,
  config: PracticeConfig
): boolean {
  const inputVal = input.trim().replace(/\s/g, "").replace(/x/g, "*").replace(/X/g, "*");

  if (question.type === "reverse-table") {
    const parts = question.answer.split("*");
    if (parts.length === 2) {
      const [p1, p2] = parts;
      return inputVal === `${p1}*${p2}` || inputVal === `${p2}*${p1}`;
    }
    return inputVal === question.answer;
  }

  if (question.type === "factors" || config.mode === "factors") {
    const tokens = inputVal.split(/[*xX×, ]+/).filter(Boolean);
    if (tokens.length === 2) {
      const a = parseInt(tokens[0], 10);
      const b = parseInt(tokens[1], 10);
      const target = question.targetNumber ?? parseInt(question.prompt, 10) ?? 0;
      if (!isNaN(a) && !isNaN(b) && a >= 2 && b >= 2 && a <= 99 && b <= 99 && a * b === target) {
        return true;
      }
    }
    return question.allValidAnswers?.some((v) => v.replace(/\s/g, "").toLowerCase() === inputVal.toLowerCase()) ?? false;
  }

  const numericInput = parseFloat(inputVal);
  if (!isNaN(numericInput) && question.numericAnswer != null) {
    return Math.abs(numericInput - question.numericAnswer) < 0.01;
  }
  return inputVal.toLowerCase() === question.answer.toLowerCase();
}

// ---- Streak ----
export function updateStreakOnPractice(
  currentStreak: number,
  lastPracticeDateEpochDay: number
): { currentStreak: number; lastPracticeDateEpochDay: number } {
  const todayEpochDay = Math.floor(Date.now() / 86400000);
  if (lastPracticeDateEpochDay === 0) return { currentStreak: 1, lastPracticeDateEpochDay: todayEpochDay };
  if (lastPracticeDateEpochDay === todayEpochDay) return { currentStreak, lastPracticeDateEpochDay: todayEpochDay };
  if (lastPracticeDateEpochDay === todayEpochDay - 1) return { currentStreak: currentStreak + 1, lastPracticeDateEpochDay: todayEpochDay };
  return { currentStreak: 1, lastPracticeDateEpochDay: todayEpochDay };
}