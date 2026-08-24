import React from "react";
import ReactDOM from "react-dom/client";
import { AppProvider } from "./store";
import App from "./App";
import { registerServiceWorker } from "./workers/push";
import "./index.css";

// Register service worker for PWA + push notifications
registerServiceWorker();

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <AppProvider>
      <App />
    </AppProvider>
  </React.StrictMode>
);