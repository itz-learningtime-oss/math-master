import { useApp } from "../store";

export default function PrivacyPolicyScreen() {
  const { state, navigate } = useApp();
  const dest = state.destination;
  if (dest.type !== "privacy") return null;

  return (
    <div className="min-h-screen bg-slate-50 px-4 py-3 max-w-lg mx-auto space-y-4">
      {/* Top bar */}
      <div className="flex items-center gap-2 py-1">
        <button onClick={() => navigate(dest.backDestination)} className="text-slate-700 text-2xl p-1 hover:bg-slate-100 rounded-full">←</button>
        <div>
          <h1 className="text-xl font-black text-slate-900">Privacy Policy</h1>
          <p className="text-[11px] text-slate-500">Math Master • Last updated: August 24, 2026</p>
        </div>
      </div>

      {/* Developer banner */}
      <div className="bg-slate-900 rounded-2xl p-4 flex items-center gap-3">
        <div className="w-11 h-11 rounded-full bg-primary-indigo flex items-center justify-center text-white text-xl">◈</div>
        <div>
          <p className="text-white text-[15px] font-black">Developed by Vishesh Chaturvedi</p>
          <p className="text-cyan-400 text-xs">Version 3.14 • 100% Free & Local Math App</p>
        </div>
      </div>

      <PolicySection n="1" title="About the App & Developer" icon="👤">
        <p className="text-[13px] text-slate-700 leading-relaxed">
          Math Master is an educational application developed by Vishesh Chaturvedi. The app is specifically designed to help students master mental math calculation speed and accuracy for competitive exams and academic success.
        </p>
      </PolicySection>

      <PolicySection n="2" title="Free Usage Commitment" icon="💰">
        <p className="text-[13px] text-slate-700 leading-relaxed">
          Math Master is 100% completely free forever. There are no subscription fees, hidden charges, or in-app purchases required to access any features.
        </p>
      </PolicySection>

      <PolicySection n="3" title="Information Collection & Storage" icon="💾">
        <div className="space-y-2.5">
          <p className="text-[13px] text-slate-700 leading-relaxed">
            We strongly value your privacy. Math Master operates purely as a local practice utility:
          </p>
          <Bullet heading="No Server Data Transfer" detail="The app does not collect, track, or share any personal data with the developer or any third parties." />
          <Bullet heading="Local Storage Only" detail="All progress, score history, and user settings are stored exclusively in your browser's local storage and are never transmitted externally." />
          <Bullet heading="No Account Required" detail="The app does not require logins, accounts, or registration." />
        </div>
      </PolicySection>

      <PolicySection n="4" title="Permissions" icon="✅">
        <p className="text-[13px] text-slate-700 leading-relaxed">
          Math Master requires no special or sensitive permissions (such as location, camera, or contacts) to run. Push notifications for daily practice reminders are optional and opt-in only.
        </p>
      </PolicySection>

      <PolicySection n="5" title="Suggestions, Feedback, and Support" icon="✉️">
        <div className="space-y-3">
          <p className="text-[13px] text-slate-700 leading-relaxed">
            We welcome your feedback to help us continuously improve the app. If you have suggestions, reviews, or feature requests, feel free to contact the developer directly:
          </p>
          <div className="bg-blue-50 border border-blue-200 rounded-xl px-3 py-2.5 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="text-primary-indigo text-lg">✉️</span>
              <span className="text-xs font-bold text-primary-indigo">www.itzlearningtime@gmail.com</span>
            </div>
          </div>
          <a
            href="mailto:www.itzlearningtime@gmail.com?subject=Math%20Master%20-%20Feedback%20%26%20Suggestions"
            className="block w-full bg-primary-indigo text-white rounded-xl py-3 text-center text-[13px] font-bold hover:bg-indigo-600"
          >
            ✉️ Email Developer
          </a>
          <p className="text-[13px] font-semibold text-primary-indigo">Enjoy the app, keep learning, and keep exploring!</p>
        </div>
      </PolicySection>
    </div>
  );
}

function PolicySection({ n, title, icon, children }: { n: string; title: string; icon: string; children: React.ReactNode }) {
  return (
    <div className="bg-white border border-slate-200 rounded-2xl p-4.5 p-4">
      <div className="flex items-center gap-2.5">
        <div className="w-9 h-9 rounded-full bg-indigo-50 flex items-center justify-center text-primary-indigo text-lg">{icon}</div>
        <p className="text-[15px] font-black text-slate-900">{n}. {title}</p>
      </div>
      <div className="mt-3">{children}</div>
    </div>
  );
}

function Bullet({ heading, detail }: { heading: string; detail: string }) {
  return (
    <div className="flex gap-2">
      <span className="w-1.5 h-1.5 rounded-full bg-primary-indigo mt-2 shrink-0" />
      <div>
        <p className="text-[13px] font-bold text-slate-900">{heading}</p>
        <p className="text-xs text-slate-600 leading-relaxed">{detail}</p>
      </div>
    </div>
  );
}