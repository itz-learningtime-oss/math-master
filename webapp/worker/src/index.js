// Math Master - Daily Reminder Scheduler Worker
// Runs on a cron trigger and sends push notifications to users whose
// configured reminder time matches the current time.
import webpush from "web-push";

export default {
  async scheduled(controller, env, ctx) {
    webpush.setVapidDetails(
      env.VAPID_SUBJECT || "mailto:www.itzlearningtime@gmail.com",
      env.VAPID_PUBLIC_KEY,
      env.VAPID_PRIVATE_KEY
    );

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
    if (record.hour !== hour) return;
    if (Math.abs(record.minute - minute) > 7) return;

    const payload = JSON.stringify({
      title: "Time for Math Practice! ⚡",
      body: "Keep your streak alive! Solve your daily mental math goals and sharpen your speed.",
    });

    try {
      await webpush.sendNotification(record.subscription, payload, { TTL: 300 });
    } catch (err) {
      const statusCode = err && err.statusCode;
      if (statusCode === 404 || statusCode === 410) {
        await env.MATH_MASTER_KV.delete(key);
      }
    }
  } catch (e) {
    // skip problematic records
  }
}