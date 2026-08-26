// Math Master - Daily Reminder Scheduler Worker
// Runs on a cron trigger and sends push notifications to users whose
// configured reminder time matches the current time (in UTC).
// Uses webpush-webcrypto (WebCrypto API) — works on the Workers runtime.
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

    // Diagnostics: track when the cron last ran and how many times.
    await env.MATH_MASTER_KV.put("meta:lastRanAt", now.toISOString());
    const prevCount = parseInt((await env.MATH_MASTER_KV.get("meta:runCount")) || "0", 10);
    await env.MATH_MASTER_KV.put("meta:runCount", String(prevCount + 1));

    const keys = await env.MATH_MASTER_KV.list({ prefix: "sub:" });
    const tasks = [];
    for (const key of keys.keys) {
      if (key.name.startsWith("meta:")) continue;
      tasks.push(processRecord(env, key.name, hour, minute));
    }
    await Promise.allSettled(tasks);
  },

  // Health-check + diagnostics + manual test-send
  async fetch(request, env) {
    const url = new URL(request.url);
    if (url.pathname === "/debug") {
      const now = new Date();
      const keys = await env.MATH_MASTER_KV.list({ prefix: "sub:" });
      const subs = [];
      for (const k of keys.keys) {
        if (k.name.startsWith("meta:")) continue;
        const raw = await env.MATH_MASTER_KV.get(k.name);
        if (!raw) continue;
        try {
          const r = JSON.parse(raw);
          subs.push({ hour: r.hour, minute: r.minute, enabled: r.enabled, updatedAt: r.updatedAt });
        } catch {}
      }
      return new Response(
        JSON.stringify(
          {
            nowUTC: now.toISOString(),
            utcHour: now.getHours(),
            utcMinute: now.getMinutes(),
            subscriptionCount: subs.length,
            subscriptions: subs,
            lastRanAt: await env.MATH_MASTER_KV.get("meta:lastRanAt"),
            runCount: await env.MATH_MASTER_KV.get("meta:runCount"),
            lastError: await env.MATH_MASTER_KV.get("meta:lastError"),
          },
          null,
          2
        ),
        { headers: { "Content-Type": "application/json" } }
      );
    }

    if (url.pathname === "/test-send") {
      const keys = await env.MATH_MASTER_KV.list({ prefix: "sub:" });
      let ok = 0;
      let fail = 0;
      const errors = [];
      for (const k of keys.keys) {
        if (k.name.startsWith("meta:")) continue;
        const raw = await env.MATH_MASTER_KV.get(k.name);
        if (!raw) continue;
        try {
          const record = JSON.parse(raw);
          const res = await sendPush(
            env,
            record.subscription,
            "Math Master Worker Test ⏰",
            "This push was sent directly from the reminder Worker."
          );
          if (res.ok) ok++;
          else {
            fail++;
            errors.push(res.error || "unknown error");
          }
        } catch (e) {
          fail++;
          errors.push(String(e));
        }
      }
      return new Response(
        JSON.stringify({ ok, fail, errors: errors.slice(0, 5) }, null, 2),
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

    // Minute-of-day circular matching, but only fire AT or AFTER the set time.
    const currentMod = hour * 60 + minute;
    const reminderMod = record.hour * 60 + record.minute;
    let diff = currentMod - reminderMod;
    if (diff < 0) diff += 1440; // not reached yet today
    // Fire only within 5 min after the set time (5-min cron), and not >5 min late.
    if (diff > 5) return;

    // Prevent duplicate sends within the same day.
    const today = new Date().toISOString().slice(0, 10);
    const firedKey = key + ":fired:" + today;
    if (await env.MATH_MASTER_KV.get(firedKey)) return;

    const result = await sendPush(
      env,
      record.subscription,
      "Time for Math Practice! ⚡",
      "Keep your streak alive! Solve your daily mental math goals and sharpen your speed."
    );

    if (result.ok) {
      await env.MATH_MASTER_KV.put(firedKey, "1");
    } else {
      await env.MATH_MASTER_KV.put("meta:lastError", result.error || "send failed");
      if (result.statusCode === 404 || result.statusCode === 410) {
        await env.MATH_MASTER_KV.delete(key);
      }
    }
  } catch (e) {
    await env.MATH_MASTER_KV.put("meta:lastError", String(e));
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
    return { ok: false, statusCode: resp.status, error: `${resp.status} ${(await resp.text()).slice(0, 200)}` };
  }
  return { ok: true };
}