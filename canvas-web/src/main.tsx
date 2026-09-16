import { runLocalRuntimeBootstrap } from "@/services/local-runtime-bootstrap";
import { bootstrapAppearance } from "@/services/appearance-bootstrap";

// 清理历史遗留的 Service Worker（sw-cache.js 已从项目中移除，残留注册会拦截 SSE 流式请求导致失败）
if ("serviceWorker" in navigator) {
    void navigator.serviceWorker.getRegistrations().then((registrations) => {
        for (const registration of registrations) {
            void registration.unregister();
        }
    });
}

runLocalRuntimeBootstrap(
    {
        get href() {
            return window.location.href;
        },
        replaceUrl(url) {
            window.history.replaceState(window.history.state, "", url);
        },
        removeStorageItem(key) {
            window.localStorage.removeItem(key);
        },
    },
    () => {
        void bootstrapAppearance().finally(() => import("./application"));
    },
);
