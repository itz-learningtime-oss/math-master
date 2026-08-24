import { useState } from "react";

interface ShareAppDialogProps {
  onDismiss: () => void;
}

const WEBSITE_URL = window.location.origin;

export default function ShareAppDialog({ onDismiss }: ShareAppDialogProps) {
  const [selectedTab, setSelectedTab] = useState(0); // 0 = Web, 1 = Multi-platform, 2 = Invite
  const [copied, setCopied] = useState(false);

  const shareLink = async () => {
    try {
      if (navigator.share) {
        await navigator.share({
          title: "Math Master - Speed Math Trainer",
          text: "Math Master - Speed Math & Mental Arithmetic Trainer. Practice addition, tables, factors, roots & 5x5 grid!",
          url: WEBSITE_URL,
        });
      } else {
        await copyLink();
      }
    } catch {
      // user cancelled
    }
  };

  const copyLink = async () => {
    try {
      await navigator.clipboard.writeText(WEBSITE_URL);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // fallback
      const ta = document.createElement("textarea");
      ta.value = WEBSITE_URL;
      document.body.appendChild(ta);
      ta.select();
      document.execCommand("copy");
      document.body.removeChild(ta);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const copyInvite = async () => {
    const text = `🧮 Math Master - Speed Math Trainer App\nBoost your calculation speed with tailored practice modes:\n• Addition, Subtraction, Multiplication (2 to 5 operands)\n• Tables Reverse Practice (12 to 37)\n• Factors: Find factor pairs A × B = N (≤99)\n• Division with Exact Quotients\n• Squares (up to 50²) & Cubes (up to 20³)\n• Square Roots (√100) & Cube Roots (∛20)\n• Complex Analysis: Sum vs Average\n• 5×5 Matrix Grid Addition Speed Runs\n• Daily Goals, Streaks & Performance Analytics\n\nTry it now: ${WEBSITE_URL}`;
    try {
      await navigator.clipboard.writeText(text);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // fallback
    }
  };

  const shareInvite = async () => {
    try {
      if (navigator.share) {
        await navigator.share({
          title: "Math Master - Speed Math Trainer App",
          text: `🧮 Math Master - Speed Math Trainer App\nBoost your calculation speed with tailored practice modes!\nTry it now: ${WEBSITE_URL}`,
          url: WEBSITE_URL,
        });
      } else {
        await copyInvite();
      }
    } catch {
      // user cancelled
    }
  };

  const tabs = [
    { label: "🌐 Website", content: renderWebTab() },
    { label: "📱 Multi-Platform", content: renderPlatformTab() },
    { label: "💬 Invite", content: renderInviteTab() },
  ];

  function renderWebTab() {
    return (
      <div className="space-y-3">
        <div className="bg-slate-50 rounded-2xl border border-slate-200 p-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center text-primary-indigo text-xl">🌐</div>
            <div>
              <p className="text-sm font-black text-slate-900">Share Math Master Website</p>
              <p className="text-xs text-slate-500 mt-0.5">Send the web app link to anyone on any device</p>
            </div>
          </div>
          <div className="mt-4 flex items-center gap-2">
            <button
              onClick={shareLink}
              className="flex-1 bg-primary-indigo text-white font-bold rounded-xl py-3 text-sm hover:bg-indigo-600"
            >
              📤 Share Link
            </button>
            <button
              onClick={copyLink}
              className="flex-1 border border-slate-300 text-slate-700 font-bold rounded-xl py-3 text-sm hover:bg-slate-100"
            >
              {copied ? "✓ Copied!" : "📋 Copy Link"}
            </button>
          </div>
          <p className="text-[10px] text-slate-400 mt-3">No installation needed. Works on any phone, tablet, or computer browser.</p>
        </div>
      </div>
    );
  }

  function renderPlatformTab() {
    return (
      <div className="space-y-3">
        <div className="bg-slate-900 rounded-2xl p-4 text-white">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-slate-700 flex items-center justify-center text-xl">📱</div>
            <div>
              <p className="text-sm font-black">Works on Every Platform</p>
              <p className="text-[11px] text-slate-400 mt-0.5">Web app runs in any modern browser</p>
            </div>
          </div>
          <p className="text-[11px] text-slate-400 mt-3 leading-relaxed">
            Math Master Web runs on Android, iOS, Mac, Windows & Linux — no install needed.
          </p>
        </div>
        <div className="bg-blue-50 border border-blue-200 rounded-2xl p-4">
          <p className="text-xs font-black text-blue-800 mb-2">How to run on any device:</p>
          <div className="space-y-1.5">
            <p className="text-[11px] text-blue-700 leading-relaxed">💻 <b>Desktop/Laptop:</b> Open the link in Chrome, Edge, Firefox or Safari.</p>
            <p className="text-[11px] text-blue-700 leading-relaxed">📱 <b>Phone/Tablet:</b> Open the link in any browser. Install as PWA via "Add to Home Screen" for an app-like experience.</p>
            <p className="text-[11px] text-blue-700 leading-relaxed">🔔 <b>Notifications:</b> Enable push notifications for daily practice reminders.</p>
          </div>
        </div>
      </div>
    );
  }

  function renderInviteTab() {
    return (
      <div className="space-y-3">
        <div className="bg-slate-50 rounded-2xl border border-slate-200 p-4">
          <p className="text-sm font-black text-slate-900">Share App Features & Curriculum</p>
          <p className="text-[11px] text-slate-500 mt-1">Send full speed math curriculum overview (Tables 12-37, Factors, Squares, Roots, 5x5 Grid) to study groups.</p>
          <div className="mt-4 flex gap-2">
            <button
              onClick={copyInvite}
              className="flex-1 border border-slate-300 text-slate-700 font-bold rounded-xl py-2.5 text-xs hover:bg-slate-100"
            >
              {copied ? "✓ Copied!" : "📋 Copy"}
            </button>
            <button
              onClick={shareInvite}
              className="flex-1 bg-primary-indigo text-white font-bold rounded-xl py-2.5 text-xs hover:bg-indigo-600"
            >
              📤 Share
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 animate-fade-in px-4" onClick={onDismiss}>
      <div
        className="w-full max-w-lg bg-white rounded-3xl p-5 shadow-2xl animate-fade-in max-h-[90vh] overflow-y-auto"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-gradient-to-br from-indigo-600 to-indigo-500 flex items-center justify-center text-white text-xl">⚡</div>
            <div>
              <h2 className="text-lg font-black text-slate-900">Share Math Master</h2>
              <p className="text-[11px] text-slate-500 font-semibold">Web App & Multi-Platform Info</p>
            </div>
          </div>
          <button onClick={onDismiss} className="text-slate-400 hover:text-slate-600 text-xl">✕</button>
        </div>

        <div className="mt-4 flex gap-1 bg-slate-100 rounded-xl p-1">
          {tabs.map((tab, i) => (
            <button
              key={i}
              onClick={() => setSelectedTab(i)}
              className={`flex-1 rounded-lg py-2 text-xs font-bold transition-all ${selectedTab === i ? "bg-white shadow text-slate-900" : "text-slate-500"}`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div className="mt-4">{tabs[selectedTab].content}</div>
      </div>
    </div>
  );
}