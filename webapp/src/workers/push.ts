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

export async function saveSubscriptionToBackend(sub: PushSubscription, hour: number, minute: number, enabled: boolean): Promise<boolean> {
  try {
    const resp = await fetch("/api/subscribe", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ subscription: sub, hour, minute, enabled }),
    });
    return resp.ok;
  } catch {
    return false;
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

export async function ensurePushSubscribed(hour: number, minute: number): Promise<boolean> {
  if (!isPushSupported()) return false;
  if (Notification.permission !== "granted") {
    const ok = await requestPermission();
    if (!ok) return false;
  }
  const sub = await subscribeToPush();
  if (!sub) return false;
  await saveSubscriptionToBackend(sub, hour, minute, true);
  return true;
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

export async function updatePushSchedule(hour: number, minute: number, enabled: boolean): Promise<void> {
  if (!isPushSupported()) return;
  if (enabled && Notification.permission === "granted") {
    const sub = await getSubscription();
    if (sub) await saveSubscriptionToBackend(sub, hour, minute, true);
  } else {
    const sub = await getSubscription();
    if (sub) await saveSubscriptionToBackend(sub, hour, minute, false);
  }
}

export function showLocalNotification(title: string, body: string): void {
  if (!("Notification" in window) || Notification.permission !== "granted") return;
  try {
    new Notification(title, { body, icon: "/manifest-icon-512.png" });
  } catch {
    // fallback
  }
}