import { dirname, resolve } from "node:path";
import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

const webDir = dirname(fileURLToPath(import.meta.url));
const appVersion = process.env.CANVAS_BUILD_VERSION?.trim() || readFileSync(resolve(webDir, "VERSION"), "utf8").trim();
let appChangelog = "";
try {
    appChangelog = readFileSync(resolve(webDir, "CHANGELOG.md"), "utf8");
} catch {
    // CHANGELOG.md 缺失时降级为空字符串，避免构建失败
}
const apiProxyTarget = process.env.VITE_API_PROXY_TARGET?.trim() || "http://192.168.10.97:82";

export default defineConfig({
    plugins: [react()],
    define: {
        __APP_VERSION__: JSON.stringify(appVersion),
        __APP_CHANGELOG__: JSON.stringify(appChangelog),
        "import.meta.env.VITE_APP_VERSION": JSON.stringify(appVersion),
    },
    server: {
        proxy: {
            "/auth": {
                target: apiProxyTarget,
                changeOrigin: true,
                xfwd: true,
            },
            "/client": {
                target: apiProxyTarget,
                changeOrigin: true,
                xfwd: true,
            },
            "/api": {
                target: apiProxyTarget,
                changeOrigin: true,
                xfwd: true,
            },
            // 画布上传图片接口（Snowy /files）
            "/files": {
                target: apiProxyTarget,
                changeOrigin: true,
                xfwd: true,
            },
            // 素材接口走 VITE_CANVAS_BACKEND_URL 直连后端，不再代理 /assets：
            // SPA 页面路由也是 /assets，整页刷新会被代理成 API 返回 JSON。
            "/asset-folders": {
                target: apiProxyTarget,
                changeOrigin: true,
                xfwd: true,
            },
            "/oauth/linuxdo/callback": {
                target: apiProxyTarget,
                changeOrigin: true,
                xfwd: true,
            },
        },
    },
    resolve: {
        alias: {
            "@": resolve(webDir, "src"),
        },
    },
    build: {
        rolldownOptions: {
            output: {
                strictExecutionOrder: true,
                codeSplitting: {
                    includeDependenciesRecursively: false,
                    minSize: 20 * 1024,
                    groups: [
                        {
                            name: "vendor-react",
                            test: /node_modules[\\/](?:react(?:-dom|-router|-router-dom)?|scheduler|zustand|use-sync-external-store|@tanstack[\\/](?:query-core|react-query))[\\/]/,
                            priority: 30,
                        },
                        {
                            name: "vendor-icons",
                            test: /node_modules[\\/](?:lucide-react|@ant-design[\\/]icons)[\\/]/,
                            priority: 20,
                            entriesAware: true,
                            entriesAwareMergeThreshold: 48 * 1024,
                        },
                        {
                            name: "vendor-antd",
                            test: /node_modules[\\/](?:antd|@ant-design|@rc-component|rc-[^\\/]+|dayjs)[\\/]/,
                            priority: 10,
                            entriesAware: true,
                            entriesAwareMergeThreshold: 80 * 1024,
                        },
                        {
                            name: "app-shared",
                            test: /[\\/]src[\\/]/,
                            priority: 5,
                            minShareCount: 2,
                            entriesAware: true,
                            entriesAwareMergeThreshold: 48 * 1024,
                        },
                    ],
                },
            },
        },
    },
});
