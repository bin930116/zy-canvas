import { AnimatePresence, motion, useReducedMotion } from "motion/react";
import { Image as AntImage, Modal, Segmented } from "antd";
import { Bell, BellOff, CheckCheck, ChevronRight, CircleAlert, Clapperboard, Info, Mail, Pin, ShieldAlert, Wrench } from "lucide-react";
import { useMemo, useState, type ReactNode } from "react";

import { aceternityMotion } from "@/lib/aceternity-motion";
import { cn } from "@/lib/utils";
import {
    announcementImageUrl,
    type AnnouncementLevel,
    type SystemAnnouncement,
    type SystemMessage,
} from "@/services/api/announcements";

export type NoticeCenterTab = "announcement" | "message";

type NoticeCenterModalProps = {
    open: boolean;
    announcements: SystemAnnouncement[];
    messages: SystemMessage[];
    announcementLoading?: boolean;
    messageLoading?: boolean;
    announcementError?: string;
    messageError?: string;
    announcementUnread?: number;
    messageUnread?: number;
    initialTab?: NoticeCenterTab;
    onClose: () => void;
    onRetryAnnouncements?: () => void;
    onRetryMessages?: () => void;
    onMarkMessagesRead?: () => void;
};

const levelMeta: Record<AnnouncementLevel, { label: string; dot: string; icon: typeof Info }> = {
    info: { label: "平台通知", dot: "bg-sky-500", icon: Info },
    success: { label: "状态恢复", dot: "bg-emerald-500", icon: Wrench },
    warning: { label: "服务提醒", dot: "bg-amber-500", icon: CircleAlert },
    critical: { label: "重要通知", dot: "bg-red-500", icon: ShieldAlert },
};

/** 提醒消息类型：后续任务完成通知归入 task */
function messageMeta(message: SystemMessage) {
    const category = (message.category || "").toLowerCase();
    if (category.includes("task") || category.includes("任务") || category.includes("generation")) {
        return { label: "任务提醒", icon: Clapperboard, tone: "text-sky-500" };
    }
    if (category.includes("success") || category.includes("成功")) {
        return { label: "成功", icon: CheckCheck, tone: "text-emerald-500" };
    }
    if (category.includes("warn") || category.includes("提醒") || category.includes("警告")) {
        return { label: "提醒", icon: CircleAlert, tone: "text-amber-500" };
    }
    return { label: "系统消息", icon: Mail, tone: "text-foreground/50" };
}

/** 兼容旧导出名 */
export const AnnouncementTimelineModal = NoticeCenterModal;

export function NoticeCenterModal({
    open,
    announcements,
    messages,
    announcementLoading = false,
    messageLoading = false,
    announcementError = "",
    messageError = "",
    announcementUnread = 0,
    messageUnread = 0,
    initialTab = "announcement",
    onClose,
    onRetryAnnouncements,
    onRetryMessages,
    onMarkMessagesRead,
}: NoticeCenterModalProps) {
    const reducedMotion = useReducedMotion();
    const [tab, setTab] = useState<NoticeCenterTab>(initialTab);
    const [detail, setDetail] = useState<SystemAnnouncement | null>(null);
    const [openMessageId, setOpenMessageId] = useState<string | null>(null);

    const tabOptions = useMemo(() => [
        {
            value: "announcement",
            label: (
                <span className="flex items-center gap-1.5">
                    <Bell className="size-3.5" />
                    系统公告
                    {announcementUnread > 0 ? (
                        <span className="rounded-full bg-red-500/15 px-1.5 text-[10px] leading-4 text-red-500">{announcementUnread}</span>
                    ) : announcements.length ? (
                        <span className="text-foreground/40">{announcements.length}</span>
                    ) : null}
                </span>
            ),
        },
        {
            value: "message",
            label: (
                <span className="flex items-center gap-1.5">
                    <Mail className="size-3.5" />
                    提醒消息
                    {messageUnread > 0 ? (
                        <span className="rounded-full bg-amber-500/15 px-1.5 text-[10px] leading-4 text-amber-600 dark:text-amber-400">{messageUnread}</span>
                    ) : messages.length ? (
                        <span className="text-foreground/40">{messages.length}</span>
                    ) : null}
                </span>
            ),
        },
    ], [announcementUnread, announcements.length, messageUnread, messages.length]);

    const handleTabChange = (value: string | number) => {
        const next = value === "message" ? "message" : "announcement";
        setTab(next);
        if (next === "message") onMarkMessagesRead?.();
    };

    return (
        <Modal
            open={open}
            width={560}
            centered
            footer={null}
            onCancel={onClose}
            title={
                <div className="flex min-w-0 items-center gap-3 pr-8">
                    <span className="grid size-9 shrink-0 place-items-center rounded-full border border-border bg-muted/45 text-foreground">
                        <Bell className="size-4.5" />
                    </span>
                    <div className="min-w-0">
                        <div className="text-lg font-semibold tracking-normal text-foreground">通知中心</div>
                        <div className="mt-0.5 text-xs font-normal text-foreground/45">系统公告与任务提醒分开查看</div>
                    </div>
                </div>
            }
            styles={{ body: { paddingTop: 8, maxHeight: "min(72vh, 720px)", overflowY: "auto", overscrollBehavior: "contain" } }}
            modalRender={(node) => (
                <motion.div
                    initial={reducedMotion ? false : { opacity: 0, y: 14, scale: 0.975 }}
                    animate={{ opacity: 1, y: 0, scale: 1 }}
                    transition={{ duration: aceternityMotion.duration.panel, ease: aceternityMotion.easing.enter }}
                >
                    {node}
                </motion.div>
            )}
        >
            <Segmented
                block
                size="middle"
                className="mb-3"
                value={tab}
                options={tabOptions}
                onChange={handleTabChange}
            />

            {tab === "announcement" ? (
                <AnnouncementList
                    announcements={announcements}
                    loading={announcementLoading}
                    error={announcementError}
                    reducedMotion={Boolean(reducedMotion)}
                    onRetry={onRetryAnnouncements}
                    onOpenDetail={setDetail}
                />
            ) : (
                <MessageList
                    messages={messages}
                    loading={messageLoading}
                    error={messageError}
                    reducedMotion={Boolean(reducedMotion)}
                    openMessageId={openMessageId}
                    onToggle={setOpenMessageId}
                    onRetry={onRetryMessages}
                />
            )}

            <Modal
                open={Boolean(detail)}
                title={detail?.title || "公告详情"}
                width={640}
                centered
                footer={null}
                onCancel={() => setDetail(null)}
            >
                {detail ? (
                    <div className="space-y-3">
                        <div className="flex items-center gap-2 text-xs text-foreground/45">
                            <span>{(levelMeta[detail.level] || levelMeta.info).label}</span>
                            {detail.publishedAt ? <time dateTime={detail.publishedAt}>{formatDateTime(detail.publishedAt)}</time> : null}
                        </div>
                        {detail.imageUrl ? <AntImage src={announcementImageUrl(detail)} alt="" preview className="max-h-52 w-full rounded-lg border border-border/70 object-contain" /> : null}
                        {detail.content ? (
                            <AnnouncementHtmlContent content={detail.content} className="text-sm leading-6 text-foreground/80" />
                        ) : (
                            <p className="text-sm text-foreground/45">该公告未填写正文内容。</p>
                        )}
                    </div>
                ) : null}
            </Modal>

        </Modal>
    );
}

function AnnouncementList({
    announcements,
    loading,
    error,
    reducedMotion,
    onRetry,
    onOpenDetail,
}: {
    announcements: SystemAnnouncement[];
    loading: boolean;
    error: string;
    reducedMotion: boolean;
    onRetry?: () => void;
    onOpenDetail: (item: SystemAnnouncement) => void;
}) {
    if (loading) return <EmptyState spin text="正在读取公告" />;
    if (error) return <EmptyState error text="公告读取失败" detail={error} onRetry={onRetry} />;
    if (!announcements.length) return <EmptyState icon={<BellOff className="size-5" />} text="暂无系统公告" detail="有新的服务动态时会在这里展示" />;

    return (
        <motion.div className="py-1" initial="hidden" animate="visible" variants={{ hidden: {}, visible: { transition: { staggerChildren: reducedMotion ? 0 : 0.04 } } }}>
            {announcements.map((announcement, index) => (
                <AnnouncementTimelineItem
                    key={announcement.id}
                    announcement={announcement}
                    last={index === announcements.length - 1}
                    reducedMotion={reducedMotion}
                    onOpenDetail={() => onOpenDetail(announcement)}
                />
            ))}
        </motion.div>
    );
}

function MessageList({
    messages,
    loading,
    error,
    reducedMotion,
    openMessageId,
    onToggle,
    onRetry,
}: {
    messages: SystemMessage[];
    loading: boolean;
    error: string;
    reducedMotion: boolean;
    openMessageId: string | null;
    onToggle: (id: string | null) => void;
    onRetry?: () => void;
}) {
    if (loading) return <EmptyState spin text="正在读取消息" />;
    if (error) return <EmptyState error text="消息读取失败" detail={error} onRetry={onRetry} />;
    if (!messages.length) return <EmptyState icon={<Mail className="size-5" />} text="暂无提醒消息" detail="任务完成、系统提醒会显示在这里" />;

    return (
        <div className="py-1">
            {messages.map((message) => {
                const meta = messageMeta(message);
                const Icon = meta.icon;
                const expanded = openMessageId === message.id;
                return (
                    <button
                        key={message.id}
                        type="button"
                        className={cn(
                            "mb-2 w-full rounded-lg border border-border/60 px-3 py-2.5 text-left transition hover:bg-muted/40 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring",
                            message.read ? "opacity-75" : "border-amber-500/30 bg-amber-500/[0.04]",
                        )}
                        onClick={() => onToggle(expanded ? null : message.id)}
                    >
                        <div className="flex items-start gap-2">
                            <span className={cn("mt-0.5 shrink-0", meta.tone)}><Icon className="size-4" /></span>
                            <div className="min-w-0 flex-1">
                                <div className="flex items-start gap-2">
                                    <span className="min-w-0 flex-1 truncate text-sm font-medium text-foreground">{message.title || "系统消息"}</span>
                                    <ChevronRight className={cn("mt-0.5 size-3.5 shrink-0 text-foreground/30 transition", expanded && "rotate-90")} />
                                </div>
                                <div className="mt-1 flex flex-wrap items-center gap-2 text-[11px] text-foreground/40">
                                    <span className="rounded bg-muted px-1.5 py-0.5">{meta.label}</span>
                                    {message.createdAt ? <time dateTime={message.createdAt}>{relativeTime(message.createdAt)}</time> : null}
                                </div>
                                {expanded && message.content ? (
                                    <div className="mt-2 whitespace-pre-wrap break-words rounded-md bg-muted/50 px-2.5 py-2 text-[13px] leading-5 text-foreground/75">
                                        {message.content}
                                    </div>
                                ) : null}
                            </div>
                        </div>
                    </button>
                );
            })}
        </div>
    );
}

function EmptyState({ icon, spin, error, text, detail, onRetry }: { icon?: ReactNode; spin?: boolean; error?: boolean; text: string; detail?: string; onRetry?: () => void }) {
    return (
        <div className="grid min-h-48 place-items-center px-4 text-center">
            <div>
                {spin ? (
                    <span className="mx-auto block size-7 animate-spin rounded-full border-2 border-foreground/15 border-t-foreground/70" />
                ) : error ? (
                    <CircleAlert className="mx-auto size-6 text-red-500" />
                ) : (
                    <span className="mx-auto grid size-12 place-items-center rounded-full border border-border bg-muted/40 text-foreground/45">{icon}</span>
                )}
                <p className="mt-3 text-sm font-medium text-foreground">{text}</p>
                {detail ? <p className="mt-1 max-w-sm text-xs leading-5 text-foreground/50">{detail}</p> : null}
                {error && onRetry ? (
                    <button type="button" className="mt-4 h-8 rounded-md border border-border px-3 text-xs font-medium text-foreground transition hover:bg-muted" onClick={onRetry}>
                        重新加载
                    </button>
                ) : null}
            </div>
        </div>
    );
}

function AnnouncementTimelineItem({ announcement, last, reducedMotion, onOpenDetail }: { announcement: SystemAnnouncement; last: boolean; reducedMotion: boolean; onOpenDetail: () => void }) {
    const meta = levelMeta[announcement.level] || levelMeta.info;
    const Icon = meta.icon;
    return (
        <motion.article
            variants={{ hidden: { opacity: 0, y: 10 }, visible: { opacity: 1, y: 0, transition: { duration: aceternityMotion.duration.state, ease: aceternityMotion.easing.enter } } }}
            whileHover={reducedMotion ? undefined : { x: 2 }}
            className="relative grid cursor-pointer grid-cols-[26px_minmax(0,1fr)] gap-3 pb-4 last:pb-2 sm:grid-cols-[32px_minmax(0,1fr)] sm:gap-4"
            role="button"
            tabIndex={0}
            onClick={onOpenDetail}
            onKeyDown={(event) => {
                if (event.key === "Enter" || event.key === " ") {
                    event.preventDefault();
                    onOpenDetail();
                }
            }}
            aria-label={`查看公告：${announcement.title}`}
        >
            <div className="relative flex justify-center pt-1.5" aria-hidden>
                {!last ? <span className="absolute left-1/2 top-5 h-[calc(100%+4px)] w-px -translate-x-1/2 bg-border" /> : null}
                <span className={`relative z-10 size-3.5 rounded-full border-[3px] border-background ${meta.dot}`} />
            </div>
            <div className="min-w-0 rounded-lg px-1 py-2 transition hover:bg-muted/40">
                <div className="flex flex-wrap items-center gap-x-2 gap-y-1">
                    <h3 className="text-[var(--fs-body-lg)] font-semibold leading-6 tracking-normal text-foreground sm:text-base">{announcement.title}</h3>
                    <span className="inline-flex items-center gap-1 text-[var(--fs-label)] font-medium text-foreground/45"><Icon className="size-3" />{meta.label}</span>
                    {announcement.pinned ? <span className="inline-flex items-center gap-1 text-[var(--fs-label)] font-medium text-amber-500"><Pin className="size-3" />置顶</span> : null}
                    <ChevronRight className="ml-auto size-3.5 shrink-0 text-foreground/30" />
                </div>
                <time dateTime={announcement.publishedAt} className="mt-1.5 block text-xs tabular-nums text-foreground/40">{relativeTime(announcement.publishedAt)} · {formatDateTime(announcement.publishedAt)}</time>
            </div>
        </motion.article>
    );
}

function AnnouncementHtmlContent({ content, className }: { content: string; className?: string }) {
    const html = sanitizeAnnouncementHtml(content);
    return (
        <div
            className={cn(
                "announcement-rich-body break-words [&_a]:break-all [&_a]:text-blue-600 [&_a]:underline dark:[&_a]:text-blue-400 [&_h1]:text-lg [&_h1]:font-semibold [&_h2]:text-base [&_h2]:font-semibold [&_h3]:font-semibold [&_img]:my-2 [&_img]:max-w-full [&_img]:rounded-md [&_li]:ml-5 [&_li]:list-disc [&_ol_li]:list-decimal [&_p]:my-2 [&_table]:my-2 [&_table]:border-collapse [&_td]:border [&_td]:border-border [&_td]:px-2 [&_td]:py-1 [&_th]:border [&_th]:border-border [&_th]:bg-muted [&_th]:px-2 [&_th]:py-1",
                className,
            )}
            // eslint-disable-next-line react/no-danger -- DOMParser 白名单消毒后的公告富文本
            dangerouslySetInnerHTML={{ __html: html }}
        />
    );
}

function sanitizeAnnouncementHtml(html: string) {
    if (!html) return "";
    if (typeof document === "undefined") return html.replace(/</g, "&lt;");
    const doc = new DOMParser().parseFromString(html, "text/html");
    doc.querySelectorAll("script,style,iframe,object,embed,link,meta,base,form,input,textarea,select,button").forEach((node) => node.remove());
    doc.querySelectorAll("*").forEach((el) => {
        for (const attr of Array.from(el.attributes)) {
            const name = attr.name.toLowerCase();
            if (name.startsWith("on")) {
                el.removeAttribute(attr.name);
                continue;
            }
            if ((name === "href" || name === "src") && !/^(https?:|mailto:|data:image\/)/i.test(attr.value.trim())) {
                el.removeAttribute(attr.name);
            }
        }
        if (el.tagName === "A") {
            el.setAttribute("target", "_blank");
            el.setAttribute("rel", "noopener noreferrer");
        }
    });
    return doc.body.innerHTML;
}

function formatDateTime(value: string) {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "--";
    return new Intl.DateTimeFormat("zh-CN", { year: "numeric", month: "2-digit", day: "2-digit", hour: "2-digit", minute: "2-digit", hour12: false }).format(date).replaceAll("/", "-");
}

function relativeTime(value: string) {
    const timestamp = new Date(value).getTime();
    if (!Number.isFinite(timestamp)) return "刚刚";
    const seconds = Math.max(0, Math.floor((Date.now() - timestamp) / 1000));
    if (seconds < 60) return "刚刚";
    if (seconds < 3600) return `${Math.floor(seconds / 60)} 分钟前`;
    if (seconds < 86400) return `${Math.floor(seconds / 3600)} 小时前`;
    if (seconds < 86400 * 7) return `${Math.floor(seconds / 86400)} 天前`;
    return `${Math.floor(seconds / (86400 * 7))} 周前`;
}
