// Client-side Web Push notification helper (paired with Cloudflare Worker backend)

const SW_PATH = "/sw.js";
const VAPID_PUBLIC_KEY = "BI7Bmi6uZ8eJnKY-YFCtF5FJGs2zPA_D8zYwg6CR2SFJ6qLgmqdnDINTIx-lL_N5J1jJZNdVAnKmjbAXQPxcobc";

let cachedRegistration: ServiceWorkerRegistration | null = null;

function urlBase64ToUint8Array(base64String: string): Uint8Array<ArrayBuffer> {
  const padding = "=".repeat((4 - (base64String.length % 4)) % 4);
  const base64 = (base64String + padding).replace(/-/g, "+").replace(/_/g, "/");
  const rawData = window.atob(base64);
  const outputArray = new Uint8Array(new ArrayBuffer(rawData.length));
  for (let i = 0; i < rawData.length; ++i) {
    outputArray[i] = rawData.charCodeAt(i);
  }
  return outputArray;
}

async function getReg(): Promise<ServiceWorkerRegistration | null> {
  if (!("serviceWorker" in navigator)) return null;
  if (cachedRegistration) return cachedRegistration;
  try {
    cachedRegistration = await navigator.serviceWorker.ready;
    return cachedRegistration;
  } catch {
    return null;
  }
}

export async function registerServiceWorker(): Promise<ServiceWorkerRegistration | null> {
  if (!("serviceWorker" in navigator)) return null;
  try {
    cachedRegistration = await navigator.serviceWorker.register(SW_PATH);
    return cachedRegistration;
  } catch {
    return null;
  }
}

export function isPushSupported(): boolean {
  return "serviceWorker" in navigator && "PushManager" in window && "Notification" in window;
}

export async function requestPermission(): Promise<boolean> {
  if (!isPushSupported()) return false;
  const result = await Notification.requestPermission();
  return result === "granted";
}

export async function subscribeToPush(): Promise<PushSubscription | null> {
  if (!isPushSupported()) return null;
  const reg = await getReg();
  if (!reg) return null;
  try {
    let sub = await reg.pushManager.getSubscription();
    if (!sub) {
      sub = await reg.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: urlBase64ToUint8Array(VAPID_PUBLIC_KEY),
      });
    }
    // Also save subscription reference for later use
    return sub;
  } catch {
    return null;
  }
}

export async function getSubscription(): Promise<PushSubscription | null> {
  if (!isPushSupported()) return null;
  const reg = await getReg();
  if (!reg) return null;
  try {
    return await reg.pushManager.getSubscription();
  } catch {
    return null;
  }
}

export async function saveSubscriptionToBackend(
  sub: PushSubscription,
  hour: number,
  minute: number,
  enabled: boolean
): Promise<{ ok: boolean; error?: string }> {
  try {
    const resp = await fetch("/api/subscribe", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ subscription: sub, hour, minute, enabled }),
    });
    if (!resp.ok) {
      let msg = `Server responded with ${resp.status}`;
      try {
        const body = await resp.json();
        if (body && body.error) msg = body.error;
      } catch {
        // ignore parse failure
      }
      return { ok: false, error: msg };
    }
    return { ok: true };
  } catch (e) {
    return { ok: false, error: String(e) };
  }
}

export async function sendTestPush(): Promise<{ ok: boolean; error?: string }> {
  try {
    const sub = await getSubscription();
    if (!sub) return { ok: false, error: "No push subscription found." };
    const resp = await fetch("/api/send-test", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ subscription: sub }),
    });
    if (!resp.ok) {
      let errMsg = `Server responded with ${resp.status}`;
      try {
        const body = await resp.json();
        if (body && body.error) errMsg = body.error;
      } catch {
        // ignore parse failure
      }
      return { ok: false, error: errMsg };
    }
    return { ok: true };
  } catch (e) {
    return { ok: false, error: String(e) };
  }
}

// Just create (or reuse) the browser push subscription — no backend call.
export async function getOrCreateSubscription(): Promise<PushSubscription | null> {
  if (!isPushSupported()) return null;
  if (Notification.permission !== "granted") {
    const ok = await requestPermission();
    if (!ok) return null;
  }
  return subscribeToPush();
}

export async function ensurePushSubscribed(
  hour: number,
  minute: number
): Promise<{ ok: boolean; error?: string }> {
  const sub = await getOrCreateSubscription();
  if (!sub) {
    return { ok: false, error: "Notification permission denied or push not supported in this browser." };
  }
  const saved = await saveSubscriptionToBackend(sub, hour, minute, true);
  if (!saved.ok) {
    return {
      ok: false,
      error: `Subscribed in the browser, but the schedule couldn't be saved to the server: ${saved.error}`,
    };
  }
  return { ok: true };
}

export async function unsubscribeFromPush(): Promise<boolean> {
  if (!isPushSupported()) return false;
  const reg = await getReg();
  if (!reg) return false;
  try {
    const sub = await reg.pushManager.getSubscription();
    if (sub) await sub.unsubscribe();
    return true;
  } catch {
    return false;
  }
}

export async function updatePushSchedule(
  hour: number,
  minute: number,
  enabled: boolean
): Promise<{ ok: boolean; error?: string }> {
  if (!isPushSupported()) return { ok: false, error: "Push not supported in this browser." };
  const sub = await getSubscription();
  if (!sub) return { ok: false, error: "No push subscription found. Enable notifications first." };
  return await saveSubscriptionToBackend(sub, hour, minute, enabled);
}

export function showLocalNotification(title: string, body: string): void {
  if (!("Notification" in window) || Notification.permission !== "granted") return;
  try {
    new Notification(title, { body, icon: "/manifest-icon-512.png" });
  } catch {
    // fallback
  }
}