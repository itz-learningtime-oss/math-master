/* Math Master Service Worker
 * Handles push notifications and PWA install/offline cache.
 */

self.addEventListener("install", (event) => {
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    Promise.all([
      self.clients.claim(),
      // Clean up old cache versions
      caches.keys().then((keys) =>
        Promise.all(keys.filter((k) => k !== CACHE_NAME).map((k) => caches.delete(k)))
      ),
    ])
  );
});

// ---- Push notifications ----
self.addEventListener("push", (event) => {
  let data = {};
  try {
    data = event.data ? event.data.json() : {};
  } catch (e) {
    data = { title: "Math Master", body: "Time for math practice!" };
  }

  const title = data.title || "Math Master - Daily Reminder";
  const body = data.body || "Keep your streak alive! Solve your daily mental math goals.";

  event.waitUntil(
    self.registration.showNotification(title, {
      body: body,
      icon: "/manifest-icon-512.png",
      badge: "/manifest-icon-192.png",
      vibrate: [200, 100, 200],
    })
  );
});

self.addEventListener("notificationclick", (event) => {
  event.notification.close();
  event.waitUntil(
    self.clients.matchAll({ type: "window", includeUncontrolled: true }).then((clientList) => {
      for (const client of clientList) {
        if (client.url === "/" && "focus" in client) {
          return client.focus();
        }
      }
      return self.clients.openWindow("/");
    })
  );
});

// ---- Fetch caching for app shell ----
// Navigation requests use NETWORK-FIRST so new deployments are picked up
// immediately; static assets use cache-first for offline speed.
const CACHE_NAME = "math-master-v3.14";

self.addEventListener("fetch", (event) => {
  const url = new URL(event.request.url);

  // Don't intercept API calls or non-GET
  if (event.request.method !== "GET") return;
  if (url.pathname.startsWith("/api/")) return;
  if (url.origin !== self.location.origin) return;
  // Never cache the service worker or manifest — always fetch fresh
  if (url.pathname.endsWith("/sw.js") || url.pathname.endsWith("/manifest.json")) return;

  // Network-first for page navigations (always get the latest version)
  if (event.request.mode === "navigate") {
    event.respondWith(
      fetch(event.request)
        .then((response) => {
          const clone = response.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put("/index.html", clone));
          return response;
        })
        .catch(() => caches.match("/index.html"))
    );
    return;
  }

  // Cache-first for static assets (hashed bundles change, so stale cache
  // entries are naturally bypassed once index.html points to new hashes).
  event.respondWith(
    caches.match(event.request).then((cached) => {
      if (cached) return cached;
      return fetch(event.request)
        .then((response) => {
          if (response.ok) {
            const clone = response.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put(event.request, clone));
          }
          return response;
        })
        .catch(() => {
          // offline fallback
        });
    })
  );
});