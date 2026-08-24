// POST /api/subscribe  { subscription, hour, minute, enabled }
import { saveSubscription, getKV, kvMissingResponse, getVapid } from "../_shared.js";

export async function onRequestPost(context) {
  const { request, env } = context;
  try {
    if (!getKV(env)) return kvMissingResponse();

    const body = await request.json();
    const { subscription, hour, minute, enabled } = body;

    if (!subscription || !subscription.endpoint) {
      return new Response(JSON.stringify({ error: "Missing subscription" }), { status: 400, headers: { "Content-Type": "application/json" } });
    }

    await saveSubscription(env, subscription, hour ?? 19, minute ?? 0, enabled !== false);

    return new Response(
      JSON.stringify({ ok: true, publicKey: getVapid(env).publicKey }),
      { status: 200, headers: { "Content-Type": "application/json" } }
    );
  } catch (e) {
    return new Response(JSON.stringify({ error: String(e) }), { status: 500, headers: { "Content-Type": "application/json" } });
  }
}