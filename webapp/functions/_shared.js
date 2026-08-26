// Shared config & helpers for Cloudflare Pages Functions (push notifications)
import webpush from "web-push";

const DEFAULT_PUBLIC =
  "BI7Bmi6uZ8eJnKY-YFCtF5FJGs2zPA_D8zYwg6CR2SFJ6qLgmqdnDINTIx-lL_N5J1jJZNdVAnKmjbAXQPxcobc";
const DEFAULT_PRIVATE = "bBGim8F-uKOA4bPgEH2wLoGcIC58saAZVIIfy5tbcw4";
const DEFAULT_SUBJECT = "mailto:www.itzlearningtime@gmail.com";

// Resolve VAPID keys from context.env (dashboard) first, then hardcoded defaults.
export function getVapid(env) {
  return {
    publicKey: (env && env.VAPID_PUBLIC_KEY) || DEFAULT_PUBLIC,
    privateKey: (env && env.VAPID_PRIVATE_KEY) || DEFAULT_PRIVATE,
    subject: (env && env.VAPID_SUBJECT) || DEFAULT_SUBJECT,
  };
}

export function initWebPush(env) {
  const v = getVapid(env);
  webpush.setVapidDetails(v.subject, v.publicKey, v.privateKey);
  return v;
}

// KV keys
export function subKey(endpoint) {
  return `sub:${endpoint}`;
}

// Returns the KV binding or null if it isn't configured (dashboard binding missing).
export function getKV(env) {
  return env && env.MATH_MASTER_KV ? env.MATH_MASTER_KV : null;
}

export function kvMissingResponse() {
  return new Response(
    JSON.stringify({
      ok: false,
      error: "KV namespace binding 'MATH_MASTER_KV' is not configured. Add it in the Cloudflare Pages dashboard: Settings → Functions → KV namespace bindings.",
    }),
    { status: 500, headers: { "Content-Type": "application/json" } }
  );
}

export async function saveSubscription(env, subscription, hour, minute, enabled) {
  const kv = getKV(env);
  if (!kv) throw new Error("KV binding not configured");
  const record = {
    subscription,
    hour,
    minute,
    enabled: enabled !== false,
    updatedAt: Date.now(),
  };
  await kv.put(subKey(subscription.endpoint), JSON.stringify(record));
}

export async function listSubscriptions(env) {
  const kv = getKV(env);
  if (!kv) return [];
  const keys = await kv.list({ prefix: "sub:" });
  const records = [];
  for (const key of keys.keys) {
    const raw = await kv.get(key.name);
    if (raw) {
      try {
        records.push(JSON.parse(raw));
      } catch {
        // skip corrupt records
      }
    }
  }
  return records;
}

export async function sendPush(env, subscription, title, body) {
  initWebPush(env);
  const payload = JSON.stringify({ title, body });
  try {
    await webpush.sendNotification(subscription, payload, { TTL: 300 });
    return { ok: true };
  } catch (err) {
    const statusCode = err && err.statusCode;
    // 404/410 means the subscription is no longer valid -> remove it
    if ((statusCode === 404 || statusCode === 410) && getKV(env)) {
      try {
        await getKV(env).delete(subKey(subscription.endpoint));
      } catch {
        // ignore
      }
    }
    const reason = err && err.message ? err.message : String(err);
    return { ok: false, error: reason, statusCode };
  }
}