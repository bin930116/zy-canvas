import { useEffect, useRef, useState, type VideoHTMLAttributes } from "react";

import { resourceIdFromStorageKey } from "@/services/api/resources";
import { cacheResourceObjectUrl, scheduleResourceBlobCache } from "@/services/resource-blob-cache";

type CachedResourceVideoProps = Omit<VideoHTMLAttributes<HTMLVideoElement>, "src"> & {
    storageKey?: string;
    src?: string;
};

/**
 * 视频资源优先读取本地 Blob 缓存，避免每次打开都从 MinIO 重新下载。
 * 播放时先用直链起播（支持 Range 边下边播），后台异步缓存完整 blob。
 * 下次打开直接用本地 blob，秒开。
 */
export function CachedResourceVideo({ storageKey, src = "", ...props }: CachedResourceVideoProps) {
    const remoteResource = Boolean(resourceIdFromStorageKey(storageKey)) || /^https?:\/\//i.test(storageKey || "");
    const [cachedSrc, setCachedSrc] = useState(remoteResource ? "" : src);

    useEffect(() => {
        if (!remoteResource || !storageKey) {
            setCachedSrc(src);
            return;
        }
        let cancelled = false;
        // 先读缓存，有就直接用
        void cacheResourceObjectUrl(storageKey)
            .then((url) => {
                if (!cancelled && url) {
                    setCachedSrc(url);
                }
            })
            .catch(() => {
                // 缓存失败，用直链
            });
        // 同时后台调度完整 blob 缓存（如果还没缓存的话）
        scheduleResourceBlobCache(storageKey, 2_000);
        return () => {
            cancelled = true;
        };
    }, [remoteResource, src, storageKey]);

    // 如果缓存没命中，用直链起播（video 标签支持 Range，边下边播）
    const finalSrc = cachedSrc || src;

    return <video {...props} src={finalSrc} />;
}
