import { useEffect, useRef, useState, type ImgHTMLAttributes, type ReactNode } from "react";

import { resourceIdFromStorageKey } from "@/services/api/resources";
import { cacheResourceObjectUrl } from "@/services/resource-blob-cache";
import { resolveImageUrl } from "@/services/image-storage";

type CachedResourceImageProps = Omit<ImgHTMLAttributes<HTMLImageElement>, "src"> & {
    storageKey?: string;
    src?: string;
    fallback?: ReactNode;
    eager?: boolean;
};

/** 远程缓存链路（IndexedDB/fetch）异常挂起时的兜底等待时长 */
const REMOTE_CACHE_TIMEOUT_MS = 8000;

/**
 * 资源图片优先读取按用户隔离的本地 Blob 缓存，避免刷新后再次从对象存储下载。
 * 本地 image: 类型的 storageKey 也会自动从 LocalForage 恢复有效的 Object URL。
 * 缓存链路失败或挂起时，一律回退到原始 src 直链，保证图片始终可显示。
 */
export function CachedResourceImage({ storageKey, src = "", fallback = null, eager = false, onError, ...props }: CachedResourceImageProps) {
    const remoteResource = Boolean(resourceIdFromStorageKey(storageKey)) || /^https?:\/\//i.test(storageKey || "");
    const localImageResource = Boolean(storageKey && (storageKey.startsWith("image:") || storageKey.startsWith("generation-image:")));
    const targetRef = useRef<HTMLSpanElement>(null);
    const [nearViewport, setNearViewport] = useState(eager || !remoteResource);
    const [cachedSrc, setCachedSrc] = useState(remoteResource ? "" : src);
    const [cacheFailed, setCacheFailed] = useState(false);

    useEffect(() => {
        if (!remoteResource || eager) {
            setNearViewport(true);
            return;
        }
        const image = targetRef.current;
        if (!image || typeof IntersectionObserver === "undefined") {
            setNearViewport(true);
            return;
        }
        const observer = new IntersectionObserver(
            (entries) => {
                if (entries.some((entry) => entry.isIntersecting)) {
                    setNearViewport(true);
                    observer.disconnect();
                }
            },
            { rootMargin: "240px" },
        );
        observer.observe(image);
        return () => observer.disconnect();
    }, [eager, remoteResource]);

    useEffect(() => {
        let cancelled = false;
        let timeoutId: number | undefined;
        setCacheFailed(false);

        if (remoteResource && storageKey) {
            if (!nearViewport) {
                setCachedSrc("");
                return () => {
                    cancelled = true;
                };
            }
            setCachedSrc("");
            const resolve = cacheResourceObjectUrl(storageKey);
            timeoutId = window.setTimeout(() => {
                // 缓存链路（IndexedDB/fetch）异常挂起时兜底直链，避免图片区域永远空白
                if (!cancelled) {
                    setCacheFailed(true);
                    setCachedSrc(src);
                }
            }, REMOTE_CACHE_TIMEOUT_MS);
            void resolve
                .then((url) => {
                    if (!cancelled) {
                        window.clearTimeout(timeoutId);
                        setCachedSrc(url || src);
                    }
                })
                .catch(() => {
                    if (!cancelled) {
                        window.clearTimeout(timeoutId);
                        setCacheFailed(true);
                        setCachedSrc(src);
                    }
                });
            return () => {
                cancelled = true;
                window.clearTimeout(timeoutId);
            };
        }

        if (localImageResource && storageKey) {
            void resolveImageUrl(storageKey, src)
                .then((url) => {
                    if (!cancelled) setCachedSrc(url || src);
                })
                .catch(() => {
                    if (!cancelled) setCachedSrc(src);
                });
            return () => {
                cancelled = true;
            };
        }

        setCachedSrc(src);
        return () => {
            cancelled = true;
        };
    }, [localImageResource, nearViewport, remoteResource, src, storageKey]);

    const handleImgError = (e: React.SyntheticEvent<HTMLImageElement, Event>) => {
        if (localImageResource && storageKey && cachedSrc.startsWith("blob:")) {
            void resolveImageUrl(storageKey)
                .then((url) => {
                    if (url && url !== cachedSrc) {
                        setCachedSrc(url);
                        return;
                    }
                    setCacheFailed(true);
                    onError?.(e);
                })
                .catch(() => {
                    setCacheFailed(true);
                    onError?.(e);
                });
            return;
        }
        if (remoteResource && src && cachedSrc !== src) {
            // 缓存内容损坏/失效时回退直链，避免坏 blob 导致图片永久空白
            setCachedSrc(src);
            return;
        }
        setCacheFailed(true);
        onError?.(e);
    };

    if (!remoteResource) {
        if (cacheFailed && fallback) return <>{fallback}</>;
        return <img {...props} src={cachedSrc} onError={handleImgError} />;
    }
    return (
        <span ref={targetRef} className="cached-resource-image-shell" style={{ display: "block", width: "100%", height: "100%" }}>
            {cachedSrc && !(cacheFailed && !src) ? <img {...props} src={cachedSrc} onError={handleImgError} /> : fallback}
        </span>
    );
}
