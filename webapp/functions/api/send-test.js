// POST /api/send-test  { subscription }
// Sends an immediate push. Does NOT require the KV binding — it delivers
// straight to the subscription provided by the client. (Subscribe still uses KV.)
import { sendPush } from "../_shared.js";

export async function onRequestPost(context) {
  const { request, env } = context;
  try {
    const body = await request.json();
    const { subscription } = body;
    if (!subscription || !subscription.endpoint) {
      return new Response(JSON.stringify({ error: "Missing subscription" }), { status: 400, headers: { "Content-Type": "application/json" } });
    }

    const result = await sendPush(
      env,
      subscription,
      "Math Master Daily Goal 🎯",
      "You're on a streak! Complete your daily problems today."
    );

    if (result.ok) {
      return new Response(JSON.stringify({ ok: true }), { status: 200, headers: { "Content-Type": "application/json" } });
    }
    return new Response(
      JSON.stringify({ ok: false, error: result.error || "Delivery failed", statusCode: result.statusCode || null }),
      { status: 502, headers: { "Content-Type": "application/json" } }
    );
  } catch (e) {
    return new Response(JSON.stringify({ error: String(e) }), { status: 500, headers: { "Content-Type": "application/json" } });
  }
}