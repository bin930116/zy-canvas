export type ResourceStorageLocation = "oss" | "local" | "none";

export function resourceStorageLocation(storageKey?: string): ResourceStorageLocation {
    if (!storageKey) return "none";
    if (storageKey.startsWith("resource:")) return "oss";
    // MinIO/文件引擎直链（生成任务写入 storageKey 的可播 URL）
    if (/^https?:\/\//i.test(storageKey)) return "oss";
    return "local";
}

export function resourceStorageLabel(storageKey?: string) {
    const location = resourceStorageLocation(storageKey);
    if (location === "oss") return "已上传";
    if (location === "local") return "本地";
    return "未同步";
}

export function resourceStorageTitle(storageKey?: string) {
    const location = resourceStorageLocation(storageKey);
    if (storageKey && /^https?:\/\//i.test(storageKey)) return "对象存储/文件引擎直链，可直接播放";
    if (location === "oss") return "已上传到对象存储，并以账号资源同步";
    if (location === "local") return "保存在当前浏览器本地，通常是对象存储未启用或上传失败后的降级";
    return "还没有可同步的资源标识";
}
