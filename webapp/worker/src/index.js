// Math Master - Daily Reminder Scheduler Worker
// Runs on a cron trigger and sends push notifications to users whose
// configured reminder time matches the current time.
// Uses webpush-webcrypto (WebCrypto API) — works on the Workers runtime,
// unlike `web-push` which requires Node's crypto.createECDH.
import { generatePushHTTPRequest, ApplicationServerKeys } from "webpush-webcrypto";

const DEFAULT_PUBLIC =
  "BI7Bmi6uZ8eJnKY-YFCtF5FJGs2zPA_D8zYwg6CR2SFJ6qLgmqdnDINTIx-lL_N5J1jJZNdVAnKmjbAXQPxcobc";
const DEFAULT_PRIVATE =
  "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgbBGim8F-uKOA4bPgEH2wLoGcIC58saAZVIIfy5tbcw6hRANCAASOwZourmfHiZymPmBQrReRSRrNszwPw_M2MIOgkdkhSeqi4JqnZwyDUyMfpS_zeSdYyWTXVQJypo2wF0D8XKG3";
const DEFAULT_SUBJECT = "mailto:www.itzlearningtime@gmail.com";

export default {
  async scheduled(controller, env, ctx) {
    const now = new Date();
    const hour = now.getHours();
    const minute = now.getMinutes();

    const keys = await env.MATH_MASTER_KV.list({ prefix: "sub:" });
    const tasks = [];

    for (const key of keys.keys) {
      tasks.push(processRecord(env, key.name, hour, minute));
    }

    await Promise.allSettled(tasks);
  },

  // Optional: a simple health-check endpoint when invoked via HTTP
  async fetch(request, env) {
    const url = new URL(request.url);
    if (url.pathname === "/debug") {
      const now = new Date();
      const keys = await env.MATH_MASTER_KV.list({ prefix: "sub:" });
      return new Response(
        JSON.stringify(
          {
            nowUTC: now.toISOString(),
            utcHour: now.getHours(),
            utcMinute: now.getMinutes(),
            subscriptionCount: keys.keys.length,
          },
          null,
          2
        ),
        { headers: { "Content-Type": "application/json" } }
      );
    }
    return new Response("Math Master reminder scheduler is running.", {
      headers: { "Content-Type": "text/plain" },
    });
  },
};

async function processRecord(env, key, hour, minute) {
  try {
    const raw = await env.MATH_MASTER_KV.get(key);
    if (!raw) return;
    const record = JSON.parse(raw);

    if (!record.enabled) return;

    // Use minute-of-day with circular distance to handle the cross-hour
    // boundary (e.g. reminder 1:58, cron runs at 2:00).
    const currentMod = hour * 60 + minute;
    const reminderMod = record.hour * 60 + record.minute;
    let diff = Math.abs(currentMod - reminderMod);
    if (diff > 720) diff = 1440 - diff; // wrap around midnight
    if (diff > 7) return;

    const result = await sendPush(
      env,
      record.subscription,
      "Time for Math Practice! ⚡",
      "Keep your streak alive! Solve your daily mental math goals and sharpen your speed."
    );
    if (!result.ok && (result.statusCode === 404 || result.statusCode === 410)) {
      await env.MATH_MASTER_KV.delete(key);
    }
  } catch (e) {
    // skip problematic records
  }
}

async function sendPush(env, subscription, title, body) {
  const publicKey = (env && env.VAPID_PUBLIC_KEY) || DEFAULT_PUBLIC;
  const privateKey = (env && env.VAPID_PRIVATE_KEY) || DEFAULT_PRIVATE;
  const subject = (env && env.VAPID_SUBJECT) || DEFAULT_SUBJECT;

  let appKeys;
  try {
    appKeys = await ApplicationServerKeys.fromJSON({ publicKey, privateKey });
  } catch {
    // Fall back to the built-in default (always valid PKCS8)
    appKeys = await ApplicationServerKeys.fromJSON({ publicKey: DEFAULT_PUBLIC, privateKey: DEFAULT_PRIVATE });
  }

  const { headers, body: encryptedBody, endpoint } = await generatePushHTTPRequest({
    payload: JSON.stringify({ title, body }),
    applicationServerKeys: appKeys,
    target: {
      endpoint: subscription.endpoint,
      keys: {
        p256dh: subscription.keys.p256dh,
        auth: subscription.keys.auth,
      },
    },
    adminContact: subject,
    ttl: 300,
  });

  const resp = await fetch(endpoint, { method: "POST", headers, body: encryptedBody });
  if (!resp.ok) {
    return { ok: false, statusCode: resp.status, error: await resp.text() };
  }
  return { ok: true };
}