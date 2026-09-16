import { AnimatePresence, motion, useReducedMotion } from "motion/react";
import { Tag } from "antd";
import { Bell } from "lucide-react";
import { lazy, Suspense, useState, type CSSProperties } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";

import { aceternityMotion } from "@/lib/aceternity-motion";
import { getAnnouncementFeed, getSystemMessageFeed } from "@/services/api/announcements";

const NoticeCenterModal = lazy(() => import("@/components/ui/aceternity/announcement-timeline-modal").then((module) => ({ default: module.NoticeCenterModal })));

const FEED_REFRESH_INTERVAL_MS = 5 * 60_000;
const FEED_CACHE_TTL_MS = 60_000;

type SystemAnnouncementCenterProps = {
    userId: string;
    className?: string;
    style?: CSSProperties;
    showLabel?: boolean;
    labelClassName?: string;
    staticMotion?: boolean;
};

/**
 * 顶栏通知入口：铃铛聚合「系统公告 + 提醒消息（站内信/任务完成）」。
 */
export function SystemAnnouncementCenter({ userId, className, style, showLabel = false, labelClassName, staticMotion = false }: SystemAnnouncementCenterProps) {
    const reducedMotion = useReducedMotion();
    const queryClient = useQueryClient();
    const [open, setOpen] = useState(false);

    const announcementKey = ["system-announcements", userId] as const;
    const messageKey = ["system-messages", userId] as const;

    const announcementQuery = useQuery({
        queryKey: announcementKey,
        queryFn: getAnnouncementFeed,
        enabled: Boolean(userId),
        staleTime: FEED_CACHE_TTL_MS,
        refetchInterval: FEED_REFRESH_INTERVAL_MS,
        refetchOnWindowFocus: false,
    });

    const messageQuery = useQuery({
        queryKey: messageKey,
        queryFn: () => getSystemMessageFeed(30),
        enabled: Boolean(userId),
        staleTime: FEED_CACHE_TTL_MS,
        refetchInterval: FEED_REFRESH_INTERVAL_MS,
        refetchOnWindowFocus: false,
    });

    const announcements = announcementQuery.data?.announcements || [];
    const announcementUnread = Math.max(0, announcementQuery.data?.unreadCount || 0);
    const messages = messageQuery.data?.messages || [];
    const messageUnread = Math.max(0, messageQuery.data?.unreadCount || 0);
    const announcementError = announcementQuery.error instanceof Error ? announcementQuery.error.message : announcementQuery.error ? "读取公告失败" : "";
    const messageError = messageQuery.error instanceof Error ? messageQuery.error.message : messageQuery.error ? "读取消息失败" : "";
    const badgeCount = announcementUnread + messageUnread;

    const openCenter = () => {
        setOpen(true);
        void announcementQuery.refetch();
        void messageQuery.refetch();
    };

    return (
        <>
            <motion.button
                type="button"
                className={className}
                style={style}
                whileHover={reducedMotion || staticMotion ? undefined : { y: -1, scale: 1.035 }}
                whileTap={reducedMotion || staticMotion ? undefined : { scale: 0.94 }}
                transition={aceternityMotion.spring.dock}
                onClick={openCenter}
                aria-label={badgeCount ? `通知中心，${badgeCount} 条未读` : "通知中心"}
                title="通知中心（公告与消息）"
            >
                <span className="relative shrink-0">
                    <Bell className="size-4" />
                    <AnimatePresence initial={false}>
                        {badgeCount > 0 ? (
                            <motion.span
                                key="unread-dot"
                                initial={reducedMotion ? false : { opacity: 0, scale: 0.5 }}
                                animate={{ opacity: 1, scale: 1 }}
                                exit={{ opacity: 0, scale: 0.6 }}
                                transition={aceternityMotion.spring.dock}
                                className="absolute -right-1 -top-1 size-2 rounded-full border border-background bg-red-500"
                                aria-hidden
                            />
                        ) : null}
                    </AnimatePresence>
                </span>
                {showLabel ? (
                    <span className={`min-w-0 flex-1 items-center justify-between gap-2 whitespace-nowrap ${labelClassName || ""}`}>
                        <span>通知中心</span>
                        <Tag color={badgeCount > 0 ? "gold" : undefined} className="!m-0 !min-w-6 !px-1.5 !text-center !text-[var(--fs-micro)] !font-medium !leading-[18px] tabular-nums">{announcements.length + messages.length}</Tag>
                    </span>
                ) : null}
            </motion.button>
            {open ? (
                <Suspense fallback={null}>
                    <NoticeCenterModal
                        open
                        announcements={announcements}
                        messages={messages}
                        announcementLoading={announcementQuery.isFetching}
                        messageLoading={messageQuery.isFetching}
                        announcementError={announcements.length ? "" : announcementError}
                        messageError={messages.length ? "" : messageError}
                        announcementUnread={announcementUnread}
                        messageUnread={messageUnread}
                        onClose={() => setOpen(false)}
                        onRetryAnnouncements={() => void announcementQuery.refetch()}
                        onRetryMessages={() => void messageQuery.refetch()}
                        onMarkMessagesRead={() => {
                            // 打开消息页时刷新未读；Snowy 站内信已读后 unreadCount 会下降
                            void messageQuery.refetch();
                            void queryClient.invalidateQueries({ queryKey: messageKey });
                        }}
                    />
                </Suspense>
            ) : null}
        </>
    );
}
