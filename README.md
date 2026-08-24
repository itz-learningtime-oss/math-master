<div align="center">
<h1>🧮 Math Master</h1>
<p><b>Speed Math &amp; Mental Arithmetic Trainer</b> — Android app + Web app</p>
<p>Developed by <b>Vishesh Chaturvedi</b> • Version 3.14 • 100% Free</p>
</div>

---

## 📱 What's in this repository

This monorepo contains **two implementations** of the Math Master speed-math trainer:

| Directory | Platform | Description |
|-----------|----------|-------------|
| `webapp/` | 🌐 **Web (React + Vite + TS + Tailwind)** | The website, deployable to Cloudflare Pages via GitHub integration |
| (root) | 🤖 **Android (Kotlin / Jetpack Compose)** | The native Android app (AI Studio template) |

Both apps share the same features and design.

---

## 🚀 Deploy the Web App to Cloudflare Pages (via GitHub)

Cloudflare Pages connects directly to this GitHub repository — **no Wrangler CLI needed** for the main site.

### 1. Connect the repo in Cloudflare

1. Go to the [Cloudflare Dashboard](https://dash.cloudflare.com/) → **Workers & Pages** → **Create** → **Pages** → **Connect to Git**.
2. Choose **itz-learningtime-oss/math-master** and click **Begin setup**.

### 2. Configure the build

| Setting | Value |
|---------|-------|
| Production branch | `main` |
| Build command | `npm ci && npm run build` |
| Build output directory | `dist` |
| **Root directory** | **`webapp`** ⚠️ |

> ⚠️ Set **Root directory** to `webapp` — the React app lives in the `webapp/` subfolder, not the repo root.

Click **Save and Deploy**. Every push to `main` now auto-deploys.

### 3. Enable push notifications (optional)

> The site deploys and works fine without this step. Push notifications need a **KV namespace** and a **VAPID secret**, configured purely in the dashboard (no CLI, no file edits — `webapp/wrangler.toml` intentionally contains **no** KV id so deployment never fails on it).

**a. Create the KV namespace**
- Dashboard → **Workers & Pages** → **KV** → **Create a namespace**, name it `MATH_MASTER_KV`.

**b. Bind KV to the Pages project**
- Pages → **math-master** → **Settings** → **Functions** → **KV namespace bindings** → **Add binding**
- Variable name: `MATH_MASTER_KV`, choose your namespace.

**c. Add secrets / vars**
- Under **Settings → Functions → Compatibility flags**, add `nodejs_compat`.
- Under **Settings → Environment variables**, add:
  - `VAPID_PUBLIC_KEY` = `BI7Bmi6uZ8eJnKY-YFCtF5FJGs2zPA_D8zYwg6CR2SFJ6qLgmqdnDINTIx-lL_N5J1jJZNdVAnKmjbAXQPxcobc`
  - `VAPID_PRIVATE_KEY` = `bBGim8F-uKOA4bPgEH2wLoGcIC58saAZVIIfy5tbcw4`

**d. Deploy the reminder cron Worker** (for automatic daily reminders)
- Dashboard → **Workers & Pages** → **Create** → **Worker** → paste the contents of `webapp/worker/src/index.js`
- Set **Cron Triggers** → `*/15 * * * *`
- Bind the same `MATH_MASTER_KV` KV namespace (Variable name: `MATH_MASTER_KV`)
- Add `VAPID_PUBLIC_KEY` and `VAPID_PRIVATE_KEY` environment variables.

Done — the site auto-deploys on every `git push`, and daily reminders fire from the cron Worker.

---

## 🧪 Features (identical in both apps)

- **9 Practice Modes**: Addition, Subtraction, Multiplication, Tables Reverse (12–37), Factors (A×B=N ≤99), Division, Complex Analysis (Sum vs Avg), Roots (√≤100, ∛≤20), 5×5 Grid Speed Run.
- **4 Study Guides**: Tables, Factors Explorer, Exponents (x²/x³), Roots — all with Flashcard hide/reveal.
- **Dashboard & Analytics**: Daily goal, streak, total problems, average speed, session history, per-mode best times, speed-progression line chart.
- **User Profile**: Name dialog, daily reminder, test notification, privacy policy, share app.

---

## 🛠 Local development (web app)

```bash
cd webapp
npm install
npm run dev      # dev server
npm run build    # production build → webapp/dist
```

## 📜 License & Credits

- **Developer**: Vishesh Chaturvedi — www.itzlearningtime@gmail.com
- 100% free, no tracking, no accounts.
