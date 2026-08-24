// POST /api/send-test  { subscription }
import { sendPush, getKV, kvMissingResponse } from "../_shared.js";

export async function onRequestPost(context) {
  const { request, env } = context;
  try {
    if (!getKV(env)) return kvMissingResponse();

    const body = await request.json();
    const { subscription } = body;
    if (!subscription || !subscription.endpoint) {
      return new Response(JSON.stringify({ error: "Missing subscription" }), { status: 400, headers: { "Content-Type": "application/json" } });
    }

    const ok = await sendPush(
      env,
      subscription,
      "Math Master Daily Goal 🎯",
      "You're on a streak! Complete your daily problems today."
    );

    return new Response(JSON.stringify({ ok }), { status: ok ? 200 : 502, headers: { "Content-Type": "application/json" } });
  } catch (e) {
    return new Response(JSON.stringify({ error: String(e) }), { status: 500, headers: { "Content-Type": "application/json" } });
  }
}