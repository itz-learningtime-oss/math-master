import { useEffect, useState } from "react";
import { useApp } from "../store";
import { PRACTICE_MODES } from "../types";
import { daysSinceEpoch } from "../storage";
import ShareAppDialog from "../components/ShareAppDialog";
import { ensurePushSubscribed, sendTestPush, updatePushSchedule, isPushSupported } from "../workers/push";

export default function DashboardScreen() {
  const { state, dispatch, navigate, saveDailyGoal } = useApp();
  const [showClearDialog, setShowClearDialog] = useState(false);
  const [showShareDialog, setShowShareDialog] = useState(false);
  const [showTimeDialog, setShowTimeDialog] = useState(false);
  const [tempHour, setTempHour] = useState(state.goal?.reminderHour ?? 19);
  const [tempMin, setTempMin] = useState(state.goal?.reminderMinute ?? 0);
  const [notifStatus, setNotifStatus] = useState<string>("");

  const sessions = state.sessions;
  const goal = state.goal;
  const userName = goal?.userName ?? "";
  const currentStreak = goal?.currentStreak ?? 0;
  const dailyGoalTarget = goal?.dailyTargetQuestions ?? 20;
  const reminderHour = goal?.reminderHour ?? 19;
  const reminderMinute = goal?.reminderMinute ?? 0;
  const reminderEnabled = goal?.reminderEnabled ?? true;

  const [targetQuestions, setTargetQuestions] = useState(dailyGoalTarget);
  const [isEnabled, setIsEnabled] = useState(reminderEnabled);

  useEffect(() => {
    setTargetQuestions(dailyGoalTarget);
    setIsEnabled(reminderEnabled);
  }, [dailyGoalTarget, reminderEnabled]);

  // Calculate stats
  const totalSessions = sessions.length;
  const totalQuestionsSolved = sessions.reduce((s, x) => s + x.totalQuestions, 0);
  const totalTimeSec = sessions.reduce((s, x) => s + x.totalTimeSec, 0);
  const avgSpeedPerQ = totalQuestionsSolved > 0 ? totalTimeSec / totalQuestionsSolved : 0;

  const todayEpochDay = daysSinceEpoch(Date.now());
  const todayQuestions = sessions.filter((s) => daysSinceEpoch(s.timestamp) === todayEpochDay).reduce((s, x) => s + x.totalQuestions, 0);
  const goalProgress = targetQuestions > 0 ? Math.min(todayQuestions / targetQuestions, 1) : 0;

  const fmtTime = (h: number, m: number) => {
    const ampm = h >= 12 ? "PM" : "AM";
    const hh = h % 12 === 0 ? 12 : h % 12;
    return `${String(hh).padStart(2, "0")}:${String(m).padStart(2, "0")} ${ampm}`;
  };

  const fmtDate = (ts: number) => {
    const d = new Date(ts);
    return `${d.toLocaleDateString("en-US", { month: "short", day: "numeric" })}, ${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
  };

  // Group sessions by mode for breakdown
  const grouped = sessions.reduce<Record<string, typeof sessions>>((acc, s) => {
    if (!acc[s.mode]) acc[s.mode] = [];
    acc[s.mode].push(s);
    return acc;
  }, {});

  // ---- Notification handlers ----
  const handleSendTest = async () => {
    if (!isPushSupported()) {
      setNotifStatus("Push notifications aren't supported in this browser. Use Chrome/Edge on desktop or the Android app.");
      return;
    }
    try {
      // Automatically subscribe if needed (no need to toggle the switch first).
      const subscribed = await ensurePushSubscribed(reminderHour, reminderMinute);
      if (!subscribed) {
        const isIOS = /iPhone|iPad|iPod/i.test(navigator.userAgent);
        setNotifStatus(
          isIOS
            ? "On iPhone/iPad, add this site to your Home Screen (Share → Add to Home Screen) first, then try again."
            : "Could not subscribe to notifications. Allow notification permission in your browser and try again."
        );
        return;
      }
      setIsEnabled(true);
      const result = await sendTestPush();
      if (result.ok) {
        setNotifStatus("Test notification sent! Check your notification shade. ✅");
      } else {
        // Show the actual server error so the user can fix the root cause.
        const hint = /crypto|ECDH|encrypt|not defined|web-push|webpush/i.test(result.error || "")
          ? " (The 'nodejs_compat' compatibility flag is likely missing → Pages Settings → Functions → Compatibility flags → add 'nodejs_compat', then Retry deployment)"
          : /vapid|unauthorized|401|403/i.test(result.error || "")
            ? " (VAPID keys mismatch → make sure VAPID_PRIVATE_KEY / VAPID_PUBLIC_KEY match the values in the README exactly)"
            : " (Check KV binding + VAPID vars in the Pages dashboard, then Retry deployment)";
        setNotifStatus(`Delivery failed: ${result.error}${hint}`);
      }
    } catch (e) {
      setNotifStatus("Error: " + String(e));
    }
  };

  const handleToggleReminder = async (checked: boolean) => {
    setIsEnabled(checked);
    saveDailyGoal(targetQuestions, reminderHour, reminderMinute, checked);
    if (checked) {
      const ok = await ensurePushSubscribed(reminderHour, reminderMinute);
      if (ok) {
        setNotifStatus("Notifications enabled! Daily reminder scheduled at the set time. ✅");
      } else {
        const msg = Notification.permission === "denied" ? "Notification permission denied." : "Could not save the schedule to the server. Make sure the MATH_MASTER_KV binding is configured in the Pages dashboard (Settings → Functions → KV namespace bindings), then try again.";
        setNotifStatus(msg);
        setIsEnabled(false);
      }
    } else {
      await updatePushSchedule(reminderHour, reminderMinute, false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 px-4 py-3 max-w-lg mx-auto space-y-4">
      {/* Developer line */}
      <div
        onClick={() => navigate({ type: "privacy", backDestination: { type: "dashboard" } })}
        className="w-full bg-indigo-50 border border-indigo-200 rounded-xl px-3 py-2 flex items-center justify-between cursor-pointer"
      >
        <div className="flex items-center gap-2 flex-1 min-w-0">
          <div className="w-6 h-6 rounded-full bg-primary-indigo flex items-center justify-center text-white text-xs">◈</div>
          <p className="text-[11px] font-bold text-primary-indigo truncate">Developed by Vishesh Chaturvedi • Version 3.14</p>
        </div>
        <span className="bg-indigo-100 text-primary-indigo text-[10px] font-bold px-1.5 py-0.5 rounded">Privacy</span>
      </div>

      {/* Top bar */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2 flex-1 min-w-0">
          <button onClick={() => navigate({ type: "home" })} className="text-slate-700 text-2xl p-1 hover:bg-slate-100 rounded-full">←</button>
          <div className="min-w-0">
            <h1 className="text-lg font-black text-slate-900 truncate">{userName ? `${userName}'s Dashboard` : "Student Dashboard"}</h1>
            <p className="text-[11px] text-slate-500">Practice Analytics & Goal Tracker</p>
          </div>
        </div>
        <div className="flex items-center gap-1">
          <button onClick={() => dispatch({ type: "OPEN_NAME_PROMPT" })} className="flex items-center gap-1 bg-slate-100 rounded-xl px-2 py-1.5">
            <span className="text-primary-indigo text-sm">👤</span>
            <span className="text-[11px] font-bold text-slate-800">{userName || "Name"}</span>
            <span className="text-slate-500 text-[10px]">✎</span>
          </button>
          <button onClick={() => setShowShareDialog(true)} className="text-primary-indigo text-xl p-1">📤</button>
        </div>
      </div>

      {/* Streak & Daily Goal */}
      <div className="bg-slate-900 rounded-3xl p-5 shadow-md">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-full bg-slate-700 flex items-center justify-center text-amber-500 text-xl">🔥</div>
            <div>
              <p className="text-white text-base font-black">{currentStreak} Day Streak</p>
              <p className="text-[11px] text-slate-400">Practice daily to keep streak</p>
            </div>
          </div>
          <span className={`rounded-xl px-2.5 py-1 text-[11px] font-bold ${goalProgress >= 1 ? "bg-emerald-500/20 text-emerald-300" : "bg-indigo-500/30 text-indigo-200"}`}>
            {goalProgress >= 1 ? "Goal Completed! 🎉" : `${Math.round(goalProgress * 100)}% Done`}
          </span>
        </div>
        <p className="text-white text-[13px] font-bold mt-4">Today's Progress: {todayQuestions} / {targetQuestions} problems</p>
        <div className="h-2 bg-slate-700 rounded-full mt-2 overflow-hidden">
          <div
            className={`h-full rounded-full ${goalProgress >= 1 ? "bg-emerald-500" : "bg-primary-indigo"}`}
            style={{ width: `${goalProgress * 100}%` }}
          />
        </div>
      </div>

      {/* Stats grid */}
      <div className="grid grid-cols-3 gap-2.5">
        <StatCard title="SOLVED" value={String(totalQuestionsSolved)} subtitle="problems" color="text-blue-600" />
        <StatCard title="AVG SPEED" value={`${avgSpeedPerQ.toFixed(2)}s`} subtitle="per problem" color="text-purple-600" />
        <StatCard title="SESSIONS" value={String(totalSessions)} subtitle="completed" color="text-emerald-600" />
      </div>

      {/* Daily Reminder Settings */}
      <div className="bg-white border border-slate-200 rounded-3xl p-4.5">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="text-primary-indigo text-lg">🔔</span>
            <p className="text-[15px] font-bold text-slate-900">Daily Practice Reminder</p>
          </div>
          <button
            onClick={() => handleToggleReminder(!isEnabled)}
            className={`w-12 h-6 rounded-full relative transition-colors ${isEnabled ? "bg-primary-indigo" : "bg-slate-300"}`}
          >
            <span className={`absolute top-0.5 w-5 h-5 rounded-full bg-white shadow transition-all ${isEnabled ? "left-6.5" : "left-0.5"}`} />
          </button>
        </div>

        <div className="flex items-center justify-between mt-3">
          <div>
            <p className="text-[13px] font-bold text-slate-700">Daily Target</p>
            <p className="text-[11px] text-slate-400">Questions to solve per day</p>
          </div>
          <div className="flex items-center gap-2">
            <button onClick={() => { const v = Math.max(targetQuestions - 5, 5); setTargetQuestions(v); saveDailyGoal(v, reminderHour, reminderMinute, isEnabled); }} className="w-8 h-8 bg-slate-100 rounded-lg font-bold">−</button>
            <span className="text-[15px] font-black text-slate-900 w-8 text-center">{targetQuestions}</span>
            <button onClick={() => { const v = Math.min(targetQuestions + 5, 200); setTargetQuestions(v); saveDailyGoal(v, reminderHour, reminderMinute, isEnabled); }} className="w-8 h-8 bg-slate-100 rounded-lg font-bold">+</button>
          </div>
        </div>

        <div className="flex items-center justify-between mt-3">
          <div>
            <p className="text-[13px] font-bold text-slate-700">Reminder Time</p>
            <p className="text-[11px] text-slate-500 font-semibold">{fmtTime(reminderHour, reminderMinute)}</p>
          </div>
          <button onClick={() => { setTempHour(reminderHour); setTempMin(reminderMinute); setShowTimeDialog(true); }} className="border border-slate-300 rounded-xl px-3 py-2 text-xs font-bold text-slate-700">
            Change Time
          </button>
        </div>

        <button
          onClick={handleSendTest}
          className="w-full bg-slate-100 text-slate-800 rounded-xl h-11 mt-3.5 flex items-center justify-center gap-1.5 text-xs font-bold hover:bg-slate-200"
        >
          🔔 Send Test Notification
        </button>
        {notifStatus && <p className="text-[11px] text-center text-slate-500 mt-1.5">{notifStatus}</p>}
      </div>

      {/* Performance by mode */}
      <p className="text-[11px] font-black text-slate-500 tracking-widest pl-1">PERFORMANCE BY MODE</p>
      {PRACTICE_MODES.map((m) => {
        const modeItems = grouped[m.id] ?? [];
        const best = modeItems.length > 0 ? Math.min(...modeItems.map((s) => s.totalTimeSec / Math.max(s.totalQuestions, 1))) : null;
        return (
          <button
            key={m.id}
            onClick={() => navigate({ type: "analysis", mode: m.id, backDestination: { type: "dashboard" } })}
            className="w-full bg-white border border-slate-200 rounded-2xl p-4 flex items-center justify-between hover:border-primary-indigo"
          >
            <div className="text-left">
              <p className="text-sm font-bold text-slate-800">{m.title}</p>
              <p className="text-[11px] text-slate-400">{modeItems.length} sessions completed</p>
            </div>
            <div className="text-right">
              <p className="text-base font-black mono text-primary-indigo">{best != null ? `${best.toFixed(2)}s` : "--"}</p>
              <p className="text-[10px] font-bold text-slate-400">Personal Best</p>
            </div>
          </button>
        );
      })}

      {/* History */}
      <div className="flex items-center justify-between pt-2">
        <p className="text-[11px] font-black text-slate-500 tracking-widest">RECENT PRACTICE SESSIONS</p>
        {sessions.length > 0 && (
          <button onClick={() => setShowClearDialog(true)} className="text-[11px] font-bold text-rose-500">Clear All</button>
        )}
      </div>

      {sessions.length === 0 ? (
        <div className="bg-white border border-slate-200 rounded-2xl py-6 text-center">
          <p className="text-xs text-slate-400">No practice sessions recorded yet. Start training today!</p>
        </div>
      ) : (
        <div className="space-y-2">
          {sessions.slice(0, 15).map((s) => {
            const modeMeta = PRACTICE_MODES.find((m) => m.id === s.mode);
            return (
              <div key={s.id} className="bg-white border border-slate-200 rounded-2xl px-3.5 py-2.5 flex items-center justify-between">
                <div className="min-w-0">
                  <p className="text-[13px] font-bold text-slate-800">{modeMeta?.title ?? s.mode}</p>
                  <p className="text-[10px] text-slate-400 truncate">{fmtDate(s.timestamp)} • {s.rangeInfo}</p>
                </div>
                <div className="flex items-center gap-1.5">
                  <span className="text-sm font-black mono text-slate-900">{s.totalTimeSec.toFixed(2)}s</span>
                  <button onClick={() => dispatch({ type: "DELETE_SESSION", id: s.id })} className="text-slate-400 hover:text-rose-500 text-lg px-1">🗑</button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Share & Privacy cards */}
      <button onClick={() => setShowShareDialog(true)} className="w-full bg-slate-900 rounded-2xl p-4.5 px-4 py-4 flex items-center justify-between">
        <div className="flex items-center gap-3 flex-1 text-left">
          <div className="w-11 h-11 rounded-2xl bg-primary-indigo flex items-center justify-center text-white text-xl">📤</div>
          <div>
            <p className="text-white text-[15px] font-black">Share Math Master Web App</p>
            <p className="text-[11px] text-slate-400">Send app link directly to friends, students & family</p>
          </div>
        </div>
        <span className="bg-cyan-400 text-slate-900 text-xs font-bold rounded-xl px-3 py-2">Share</span>
      </button>

      <button onClick={() => navigate({ type: "privacy", backDestination: { type: "dashboard" } })} className="w-full bg-white border border-slate-200 rounded-2xl p-4 flex items-center justify-between">
        <div className="flex items-center gap-3 flex-1 text-left">
          <div className="w-11 h-11 rounded-2xl bg-emerald-50 flex items-center justify-center text-emerald-600 text-xl">🛡</div>
          <div>
            <p className="text-sm font-black text-slate-900">Privacy Policy & Trust</p>
            <p className="text-[11px] text-slate-500">Developer: Vishesh Chaturvedi • 100% Free & Local</p>
          </div>
        </div>
        <span className="bg-emerald-50 text-emerald-600 text-[11px] font-bold px-2 py-1 rounded">View 📜</span>
      </button>

      {/* Dialogs */}
      {showShareDialog && <ShareAppDialog onDismiss={() => setShowShareDialog(false)} />}

      {showTimeDialog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 animate-fade-in px-4" onClick={() => setShowTimeDialog(false)}>
          <div className="bg-white rounded-3xl p-5 w-full max-w-sm animate-fade-in" onClick={(e) => e.stopPropagation()}>
            <h2 className="text-lg font-black text-slate-900">Set Reminder Time</h2>
            <p className="text-[13px] text-slate-600 mt-1">Select daily notification time:</p>
            <div className="flex items-center justify-center gap-3 my-4">
              {/* Hour */}
              <div className="flex flex-col items-center gap-1">
                <button onClick={() => setTempHour((tempHour + 1) % 24)} className="w-14 h-8 bg-slate-100 rounded-xl text-sm font-bold hover:bg-slate-200">▲</button>
                <span className="text-3xl font-black text-slate-900 w-14 text-center">{String(tempHour).padStart(2, "0")}</span>
                <button onClick={() => setTempHour((tempHour + 23) % 24)} className="w-14 h-8 bg-slate-100 rounded-xl text-sm font-bold hover:bg-slate-200">▼</button>
              </div>
              <span className="text-3xl font-black text-slate-900">:</span>
              {/* Minute */}
              <div className="flex flex-col items-center gap-1">
                <button onClick={() => setTempMin((tempMin + 1) % 60)} className="w-14 h-8 bg-slate-100 rounded-xl text-sm font-bold hover:bg-slate-200">▲</button>
                <span className="text-3xl font-black text-slate-900 w-14 text-center">{String(tempMin).padStart(2, "0")}</span>
                <button onClick={() => setTempMin((tempMin + 59) % 60)} className="w-14 h-8 bg-slate-100 rounded-xl text-sm font-bold hover:bg-slate-200">▼</button>
              </div>
            </div>
            <div className="flex justify-end gap-2">
              <button onClick={() => setShowTimeDialog(false)} className="px-4 py-2 text-slate-500 font-semibold text-sm">Cancel</button>
              <button
                onClick={() => { saveDailyGoal(targetQuestions, tempHour, tempMin, isEnabled); updatePushSchedule(tempHour, tempMin, isEnabled).then((ok) => { if (!ok) setNotifStatus("Subscribed, but the daily schedule couldn't be saved to the server. Add the MATH_MASTER_KV binding in the Pages dashboard, then retry."); }); setShowTimeDialog(false); }}
                className="bg-primary-indigo text-white font-bold rounded-xl px-5 py-2 text-sm"
              >
                Save
              </button>
            </div>
          </div>
        </div>
      )}

      {showClearDialog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 animate-fade-in px-4" onClick={() => setShowClearDialog(false)}>
          <div className="bg-white rounded-3xl p-5 w-full max-w-sm animate-fade-in" onClick={(e) => e.stopPropagation()}>
            <h2 className="text-lg font-black text-slate-900">Clear History?</h2>
            <p className="text-sm text-slate-600 mt-2">Are you sure you want to delete all saved practice sessions? This action cannot be undone.</p>
            <div className="flex justify-end gap-2 mt-4">
              <button onClick={() => setShowClearDialog(false)} className="px-4 py-2 text-slate-500 font-semibold text-sm">Cancel</button>
              <button
                onClick={() => { dispatch({ type: "CLEAR_HISTORY" }); setShowClearDialog(false); }}
                className="bg-rose-500 text-white font-bold rounded-xl px-5 py-2 text-sm"
              >
                Delete All
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function StatCard({ title, value, subtitle, color }: { title: string; value: string; subtitle: string; color: string }) {
  return (
    <div className="bg-white border border-slate-200 rounded-2xl py-3 text-center">
      <p className="text-[9px] font-black text-slate-400 tracking-widest">{title}</p>
      <p className={`text-base font-black mono ${color}`}>{value}</p>
      <p className="text-[10px] text-slate-500">{subtitle}</p>
    </div>
  );
}