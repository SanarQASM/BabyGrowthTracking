// firebase-messaging-sw.js
// ─────────────────────────────────────────────────────────────────────────────
// Place this file at the ROOT of your web output directory.
// For Kotlin/JS: composeApp/src/jsMain/resources/firebase-messaging-sw.js
// For Kotlin/Wasm: composeApp/src/wasmJsMain/resources/firebase-messaging-sw.js
//
// This service worker handles push notifications when:
//  • The web tab is closed
//  • The web tab is in the background
//  • The device screen is off
//
// Without this file, Firebase Web Push only works when the tab is open and
// the foreground onMessage() handler fires. Background delivery requires a
// registered service worker.
// ─────────────────────────────────────────────────────────────────────────────
importScripts('https://www.gstatic.com/firebasejs/10.7.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.7.0/firebase-messaging-compat.js');

// ✅ Must exactly match your index.html config
firebase.initializeApp({
    apiKey           : "AIzaSyAIUFHXzCKuOVN23DjYALmNT17Yyt5s4a4",
    authDomain       : "babygrowthtracking-cfd66.firebaseapp.com",
    projectId        : "babygrowthtracking-cfd66",
    storageBucket    : "babygrowthtracking-cfd66.firebasestorage.app",
    messagingSenderId: "887110744705",
    appId            : "1:887110744705:web:6372e4d47c402ac5062b80"
});

const messaging = firebase.messaging();

// Handle background messages
messaging.onBackgroundMessage(payload => {
  console.log('[SW] Background message received:', payload);

  const notificationTitle = payload.notification?.title ?? 'BabyGrowth';
  const notificationBody  = payload.notification?.body  ?? '';
  const data              = payload.data ?? {};

  const notificationOptions = {
    body : notificationBody,
    icon : '/icon-192.png',
    badge: '/badge-72.png',
    data : {
      deepLinkRoute: data.deepLinkRoute ?? null,
      category     : data.category ?? 'GENERAL',
      priority     : data.priority ?? 'MEDIUM'
    },
    // Show an action button if the notification carries one
    actions: data.actionLabel ? [
      { action: 'primary', title: data.actionLabel }
    ] : []
  };

  return self.registration.showNotification(notificationTitle, notificationOptions);
});

// Handle notification click — open the app and navigate to the deep-link route
self.addEventListener('notificationclick', event => {
  event.notification.close();

  const route = event.notification.data?.deepLinkRoute;
  const url   = route ? `/?notification_route=${encodeURIComponent(route)}` : '/';

  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then(clientList => {
      // If a tab is already open, focus it and send a message
      for (const client of clientList) {
        if ('focus' in client) {
          client.focus();
          if (route) {
            client.postMessage({ type: 'NOTIFICATION_DEEP_LINK', route });
          }
          return;
        }
      }
      // No open tab — open a new one
      if (clients.openWindow) {
        return clients.openWindow(url);
      }
    })
  );
});