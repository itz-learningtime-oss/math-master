# 🧮 Math Master — Web App

The official web version of the **Math Master** Android app (v3.14), rebuilt with React + Vite + TypeScript + Tailwind CSS. Every feature from the Android app is preserved:

- **9 Practice Modes**: Addition, Subtraction, Multiplication, Tables Reverse (12–37), Factors (A×B=N ≤99), Division, Complex Analysis (Sum vs Avg), Roots (√≤100, ∛≤20), and the 5×5 Grid Speed Run.
- **4 Study Guides**: Tables, Factors Explorer, Exponents (x²/x³), Roots — all with Flashcard / hide-and-reveal mode.
- **Dashboard & Analytics**: Daily goal, streak counter, total problems, average speed, session history, per-mode best times, and a speed-progression line chart.
- **User Profile**: Name dialog, daily reminder settings, test notification, privacy policy, share app.
- **Push Notifications**: Web Push daily reminder via a Cloudflare Worker + Pages Functions backend.
- **PWA**: Installable, offline app shell.

Data is stored 100% locally in the browser (`localStorage`), just like the Android app's Room database — no tracking, no accounts.

---

## 📁 Project Structure

```
math-webapp/
├── src/                    # React app
│   ├── screens/            # All UI screens (mirror the Android Compose screens)
│   ├── components/         # Reusable UI (keypad, dialogs, chart)
│   ├── workers/push.ts     # Client-side Web Push helper
│   ├── engine.ts           # Question generation & validation
│   ├── store.tsx           # Global state (mirrors MathViewModel)
│   ├── storage.ts          # localStorage persistence (mirrors Room)
│   └── types.ts            # Data models (mirrors Kotlin models)
├── functions/api/          # Cloudflare Pages Functions (push subscribe + test)
├── worker/                 # Cloudflare Worker (daily reminder cron)
├── public/
│   ├── sw.js               # Service worker (push + PWA cache)
│   └── manifest.json       # PWA manifest
├── wrangler.toml           # Pages project config (KV binding)
└── vite.config.ts
```

---

## 🚀 Deploy to Cloudflare Pages (via GitHub — recommended)

This project lives in the `webapp/` folder of the repo
[**itz-learningtime-oss/math-master**](https://github.com/itz-learningtime-oss/math-master).
Cloudflare Pages connects directly to the GitHub repo, so **no Wrangler CLI is needed** for the main site.

### 1. Connect the repo in Cloudflare
1. [Cloudflare Dashboard](https://dash.cloudflare.com/) → **Workers & Pages** → **Create** → **Pages** → **Connect to Git**.
2. Select **itz-learningtime-oss/math-master** → **Begin setup**.

### 2. Configure the build
| Setting | Value |
|---------|-------|
| Production branch | `main` |
| Build command | `npm ci && npm run build` |
| Build output directory | `dist` |
| **Root directory** | **`webapp`** ⚠️ |

Click **Save and Deploy**. Every push to `main` auto-deploys.

### 3. Enable push notifications
> The site works without this step. Notifications are configured via the Cloudflare dashboard — no CLI / file edits needed.
> The `wrangler.toml` intentionally has no KV id so deployment never fails on that.

1. **Create KV namespace** `MATH_MASTER_KV` (Dashboard → Workers & Pages → KV → Create).
2. **Bind it** to the Pages project: *Settings → Functions → KV namespace bindings* → add `MATH_MASTER_KV`.
3. **Compatibility flag**: *Settings → Functions* → add `nodejs_compat`.
4. **Environment variables**:
   - `VAPID_PUBLIC_KEY` = `BI7Bmi6uZ8eJnKY-YFCtF5FJGs2zPA_D8zYwg6CR2SFJ6qLgmqdnDINTIx-lL_N5J1jJZNdVAnKmjbAXQPxcobc`
   - `VAPID_PRIVATE_KEY` = `bBGim8F-uKOA4bPgEH2wLoGcIC58saAZVIIfy5tbcw4`
5. **Reminder cron Worker**: Dashboard → Create → Worker → paste `worker/src/index.js`, cron `*/15 * * * *`, bind the same KV + VAPID vars.

---

## 🚀 Alternative: Deploy with Wrangler CLI

### Step 1 — Build the site

```bash
npm install
npm run build        # outputs to dist/
```

### Step 2 — Create the KV namespace

```bash
npx wrangler kv namespace create MATH_MASTER_KV
```

Copy the printed **id** and paste it into **both**:
- `wrangler.toml` → `[[kv_namespaces]] id =`
- `worker/wrangler.toml` → `[[kv_namespaces]] id =`

### Step 3 — Set the VAPID private key secret

```bash
npx wrangler pages secret put VAPID_PRIVATE_KEY --project-name math-master
# value: bBGim8F-uKOA4bPgEH2wLoGcIC58saAZVIIfy5tbcw4
```

> If you generate your own keys (`npx web-push generate-vapid-keys`), update the public key in **both** `src/workers/push.ts` and `wrangler.toml`.

### Step 4 — Deploy the Pages project

```bash
npx wrangler pages deploy dist --project-name math-master
```

This uploads the static site **and** the `functions/` directory (Pages Functions are deployed automatically).

### Step 5 — Deploy the reminder Worker

```bash
cd worker
npm install
npx wrangler kv namespace create MATH_MASTER_KV   # (or reuse the id above)
# edit worker/wrangler.toml with the KV id
npx wrangler secret put VAPID_PRIVATE_KEY          # same private key
npx wrangler deploy
```

The Worker's cron trigger (`*/15 * * * *`) checks KV every 15 minutes and pushes a reminder to every user whose reminder time matches.

---

## 🔔 How Push Notifications Work

1. User enables **Daily Practice Reminder** on the Dashboard.
2. The browser asks for notification permission and subscribes via the service worker.
3. The subscription (with reminder time) is stored in **Cloudflare KV** through `POST /api/subscribe`.
4. The **reminder Worker** (cron) checks KV every 15 minutes and sends the push.
5. **Send Test Notification** calls `POST /api/send-test` for an immediate push.

> Notes: Push notifications require HTTPS (Cloudflare Pages provides it automatically) and only work when the site is *installed or open in a supported browser* (Chrome, Edge, Firefox). On mobile, add the site to your home screen for the best experience.

---

## 🛠 Local Development

```bash
npm run dev      # Vite dev server
npm run build    # production build
npm run preview  # preview the production build
```

## 📜 License & Credits

- **Developer**: Vishesh Chaturvedi — www.itzlearningtime@gmail.com
- 100% free, no tracking, no accounts.
