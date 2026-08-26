// Shared config & helpers for Cloudflare Pages Functions (push notifications)
// Uses webpush-webcrypto (WebCrypto API) which works on the Workers runtime —
// unlike the `web-push` npm package which needs Node's crypto.createECDH.
import { generatePushHTTPRequest, ApplicationServerKeys } from "webpush-webcrypto";

const DEFAULT_PUBLIC =
  "BI7Bmi6uZ8eJnKY-YFCtF5FJGs2zPA_D8zYwg6CR2SFJ6qLgmqdnDINTIx-lL_N5J1jJZNdVAnKmjbAXQPxcobc";
// PKCS8-encoded EC private key (base64url), paired with DEFAULT_PUBLIC.
const DEFAULT_PRIVATE =
  "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgbBGim8F-uKOA4bPgEH2wLoGcIC58saAZVIIfy5tbcw6hRANCAASOwZourmfHiZymPmBQrReRSRrNszwPw_M2MIOgkdkhSeqi4JqnZwyDUyMfpS_zeSdYyWTXVQJypo2wF0D8XKG3";
const DEFAULT_SUBJECT = "mailto:www.itzlearningtime@gmail.com";

// Resolve VAPID keys from context.env (dashboard) first, then hardcoded defaults.
export function getVapid(env) {
  return {
    publicKey: (env && env.VAPID_PUBLIC_KEY) || DEFAULT_PUBLIC,
    privateKey: (env && env.VAPID_PRIVATE_KEY) || DEFAULT_PRIVATE,
    subject: (env && env.VAPID_SUBJECT) || DEFAULT_SUBJECT,
  };
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
  try {
    const v = getVapid(env);
    // Try the provided private key; if it's invalid (e.g. old raw format),
    // fall back to the built-in default PKCS8 key.
    let appKeys;
    try {
      appKeys = await ApplicationServerKeys.fromJSON({
        publicKey: v.publicKey,
        privateKey: v.privateKey,
      });
    } catch {
      // Fall back to the default (always valid PKCS8)
      const def = getVapid(undefined);
      appKeys = await ApplicationServerKeys.fromJSON({
        publicKey: def.publicKey,
        privateKey: def.privateKey,
      });
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
      adminContact: v.subject,
      ttl: 300,
    });

    const resp = await fetch(endpoint, { method: "POST", headers, body: encryptedBody });
    if (!resp.ok) {
      const statusCode = resp.status;
      const text = await resp.text();
      // 404/410 means the subscription is no longer valid -> remove it
      if ((statusCode === 404 || statusCode === 410) && getKV(env)) {
        try {
          await getKV(env).delete(subKey(subscription.endpoint));
        } catch {
          // ignore
        }
      }
      return { ok: false, error: `${statusCode} ${text.slice(0, 200)}`, statusCode };
    }
    return { ok: true };
  } catch (err) {
    const reason = err && err.message ? err.message : String(err);
    return { ok: false, error: reason };
  }
}