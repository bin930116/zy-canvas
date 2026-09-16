import type { ModelChannel } from "@/stores/use-config-store";
import type { BillingOrder, CreditLedgerEntry } from "@/services/api/wallet";
import type { GenerationTask, TaskStatus } from "@/services/api/task-center";
import type { CanvasDrawingEngineSetting } from "@/lib/canvas/canvas-drawing-engine";
import type { FeatureAvailability } from "@/stores/use-user-store";
import { apiClient, request } from "@/services/api/request";
import type { PublicLogicalModel } from "@/services/api/logical-models";
import type { OSSConnectionTestInput, OSSConnectionTestResult, OSSProvider, S3Preset } from "@/lib/oss-settings";
import { sm2Encrypt } from "@/lib/sm-crypto";

const api = apiClient;

let authSessionRequest: Promise<AuthSessionPayload> | null = null;
let authSessionCache: { payload: AuthSessionPayload; expiresAt: number } | null = null;

function invalidateAuthSessionCache() {
    authSessionCache = null;
}

export type LocalUser = {
    id: string;
    username: string;
    email?: string;
    displayName: string;
    avatarUrl?: string;
    identityProvider?: string;
    identityId?: string;
    identityUsername?: string;
    role: "admin" | "user";
    status: "active" | "disabled";
    lastLoginAt?: string;
    createdAt: string;
    updatedAt: string;
};

export type AdminUser = LocalUser & {
    availableMicrocredits: number;
    reservedMicrocredits: number;
};

export type AuthSessionPayload = {
    user: LocalUser | null;
    logicalModels?: PublicLogicalModel[];
    runtimeLimits?: RuntimeLimits;
    drawingEngine?: CanvasDrawingEngineSetting;
    features?: FeatureAvailability;
};

export type RuntimeLimits = {
    activeTaskLimit: number;
    resourceUploadMB: number;
    sessionUploadMB: number;
    recycleBinRetentionDays?: number;
};

export type ApiCallLog = {
    id: string;
    userId: string;
    userDisplayName?: string;
    userAccount?: string;
    channelId: string;
    channelName: string;
    taskId?: string;
    taskStatus?: TaskStatus;
    billingOrderId?: string;
    billingStatus?: BillingOrder["status"];
    billingAmountMicrocredits: number;
    billingAvailable: boolean;
    source: string;
    capability: "text" | "image" | "video" | "audio" | "";
    operation?: string;
    requestKind: "create" | "poll" | "download" | "repair" | "";
    billable: boolean;
    apiFormat: string;
    method: string;
    path: string;
    model: string;
    status: "succeeded" | "failed";
    statusCode: number;
    durationMs: number;
    pollCount: number;
    providerStatus?: string;
    inputTokens: number;
    outputTokens: number;
    cachedTokens: number;
    usageAvailable: boolean;
    mediaCount: number;
    mediaPreviewUrl?: string;
    mediaPreviewKind?: "image" | "video";
    videoSeconds: number;
    providerRequestId?: string;
    estimatedCostMicros: number;
    costAvailable: boolean;
    currency?: string;
    errorCode?: string;
    error?: string;
    concurrencyLimit: number;
    upstreamUrl: string;
    requestContentType?: string;
    requestBody?: string;
    responseBody?: string;
    startedAt?: string;
    createdAt: string;
};

export type AdminProviderTaskQueryResult = {
    task: GenerationTask;
    providerStatus: string;
    recovered: boolean;
    billingSettled: boolean;
};

export type AdminAuditEvent = {
    id: string;
    actorUserId: string;
    action: string;
    targetType: string;
    targetId: string;
    summary: string;
    metadataJson?: string;
    createdAt: string;
};

export type AdminUserDetail = {
    user: LocalUser;
    account: { userId: string; availableMicrocredits: number; reservedMicrocredits: number; version: number };
    counts: { ledgerEntries: number; tasks: number; apiCalls: number; auditEvents: number };
    storageUsage: {
        assetCount: number;
        assetBytes: number;
        canvasCount: number;
        canvasBytes: number;
        sessionCount: number;
        sessionBytes: number;
        taskCount: number;
        taskBytes: number;
        apiCallCount: number;
    };
    storedFileBytes: number;
    dailyUploadBytes: number;
    quota: RuntimeResourcePolicy;
};

export type AdminUserTask = {
    id: string;
    type: string;
    status: "queued" | "running" | "succeeded" | "failed" | "cancelled";
    stage: string;
    progress: number;
    model?: string;
    providerRequestId?: string;
    createdAt: string;
};

export type AnalyticsFilters = {
    from?: string;
    to?: string;
    userId?: string;
    model?: string;
    channelId?: string;
    capability?: string;
};

export type AdminReferenceData = {
    users: Array<{ id: string; username: string; displayName: string }>;
    channels: Array<{ id: string; name: string; enabled: boolean; models: string[] }>;
};

export type AdminAnalytics = {
    from: string;
    to: string;
    kpi: {
        activeUsers: number;
        dau: number;
        wau: number;
        mau: number;
        generationTasks: number;
        upstreamRequests: number;
        successRate: number;
        p95DurationMs: number;
        currentQueuedTasks: number;
        estimatedCostMicros: number;
        costAvailable: boolean;
        currency?: string;
    };
    trend: Array<{ day: string; tasks: number; requests: number; activeUsers: number; requestSuccessRate: number }>;
    models: Array<{
        model: string;
        capability: string;
        tasks: number;
        requests: number;
        uniqueUsers: number;
        taskSuccessRate: number;
        requestSuccessRate: number;
        p50DurationMs: number;
        p95DurationMs: number;
        inputTokens: number;
        outputTokens: number;
        cachedTokens: number;
        usageAvailable: boolean;
        mediaCount: number;
        videoSeconds: number;
        estimatedCostMicros: number;
        costAvailable: boolean;
        currency?: string;
    }>;
    users: Array<{ userId: string; name: string; activeDays: number; tasks: number; agentMessages: number; canvasDays: number; assets: number; resources: number; commonModel?: string }>;
    failures: Array<{ type: string; model: string; count: number; lastError?: string; lastSeenAt: string }>;
};

export type ModelPricing = {
    id: string;
    channelId?: string;
    model: string;
    capability: "text" | "image" | "video" | "audio";
    currency: string;
    inputPerMillionMicros: number;
    outputPerMillionMicros: number;
    cachedPerMillionMicros: number;
    perRequestMicros: number;
    perMediaMicros: number;
    perVideoSecondMicros: number;
    createdAt: string;
    updatedAt: string;
};

export type PromptTemplate = {
    id: string;
    operation: string;
    name: string;
    version: number;
    content: string;
    outputType: "json" | "text";
    enabled: boolean;
    createdBy?: string;
    createdAt: string;
    updatedAt: string;
};

export type PromptTemplateVariable = {
    label: string;
    placeholder: string;
};

export type PromptOperationDefinition = {
    operation: string;
    label: string;
    category: string;
    description: string;
    outputType: "json" | "text";
    schemaKey?: string;
    variables: PromptTemplateVariable[];
    outputContract: string;
};

export type UserPromptCustomization = {
    id: string;
    operation: string;
    mode: "inherit" | "append" | "rewrite";
    content: string;
    baseTemplateId: string;
    updatedAt: string;
};

export type UserPromptPreference = {
    definition: PromptOperationDefinition;
    template: PromptTemplate | null;
    customization?: UserPromptCustomization;
    outdated: boolean;
};

export type AdminOSSSetting = {
    enabled: boolean;
    provider: OSSProvider;
    s3Preset: S3Preset;
    region: string;
    endpoint: string;
    cdnBaseUrl: string;
    bucket: string;
    accessKeyId: string;
    accessKeySecret?: string;
    hasAccessKeySecret: boolean;
    sessionToken?: string;
    hasSessionToken: boolean;
    pathStyle: boolean;
    allowUserS3: boolean;
    publicBaseUrl: string;
    pathPrefix: string;
    testedAt?: string;
    testedDigest?: string;
    historyCount?: number;
    referencedResourceCount?: number;
    updatedBy?: string;
    createdAt?: string;
    updatedAt?: string;
};

export type AdminArkPrivateAssetSetting = {
    enabled: boolean;
    region: string;
    projectName: string;
    accessKeyId: string;
    accessKeySecret?: string;
    hasAccessKeySecret: boolean;
    updatedBy?: string;
    createdAt?: string;
    updatedAt?: string;
};

export type RuntimeResourcePolicy = {
    resourceUploadMB: number;
    sessionUploadMB: number;
    generatedFileMB: number;
    dailyUploadMB: number;
    storedFileGB: number;
    structuredDataMB: number;
    taskDataGB: number;
    assetCount: number;
    canvasCount: number;
    sessionCount: number;
    taskCount: number;
    apiCallLogCount: number;
    recycleBinRetentionDays?: number;
};

export type RuntimeTaskPolicy = {
    workerConcurrency: number;
    channelConcurrency: number;
    activeTaskLimit: number;
    imageTimeoutMinutes: number;
    textTimeoutMinutes: number;
    audioTimeoutMinutes: number;
    videoTimeoutMinutes: number;
    storyboardTimeoutMinutes: number;
    defaultTimeoutMinutes: number;
};

export type RuntimeRequestPolicy = {
    taskCreatePerMinute: number;
    sessionCreatePerMinute: number;
    resourceUploadPerMinute: number;
    resourceImportPerMinute: number;
    sessionFilePerMinute: number;
    assetWritePerMinute: number;
    canvasWritePerMinute: number;
    registerPerHour: number;
    emailCodePerHour: number;
    loginIPPerTenMinutes: number;
    loginAccountPerTenMinutes: number;
    systemRelayPerMinute: number;
    customRelayPerMinute: number;
    customRelayConcurrency: number;
    customRelayRequestMB: number;
    customRelayResponseMB: number;
    customRelayTimeoutMinutes: number;
    systemRelayRequestMB: number;
    systemRelayResponseMB: number;
    channelCircuitFailureCount: number;
    channelCircuitOpenSeconds: number;
};

export type RuntimePolicySetting = {
    resource: RuntimeResourcePolicy;
    task: RuntimeTaskPolicy;
    request: RuntimeRequestPolicy;
    configured?: boolean;
    updatedBy?: string;
    createdAt?: string;
    updatedAt?: string;
};

export function linuxDOLoginURL(next: string) {
    const base = String(api.defaults.baseURL || "/api").replace(/\/$/, "");
    return `${base}/auth/linuxdo/start?next=${encodeURIComponent(next)}`;
}

export function getAuthSession() {
    const now = Date.now();
    if (authSessionCache && authSessionCache.expiresAt > now) return Promise.resolve(authSessionCache.payload);
    if (authSessionRequest) return authSessionRequest;
    // 直接调用 Snowy C 端获取当前登录用户接口
    authSessionRequest = request<SnowyClientLoginUser>(api.get("/auth/c/getLoginUser"))
        .then((snowyUser) => {
            // 将 Snowy C 端用户格式转换为 Canvas 前端期望的 AuthSessionPayload
            const user: LocalUser = {
                id: snowyUser.id || "",
                username: snowyUser.account || "",
                email: snowyUser.email,
                displayName: snowyUser.name || snowyUser.nickname || snowyUser.account || "",
                avatarUrl: snowyUser.avatar,
                role: "user",
                status: snowyUser.userStatus === "0" ? "active" : "disabled",
                lastLoginAt: snowyUser.lastLoginTime,
                createdAt: snowyUser.createTime || new Date().toISOString(),
                updatedAt: snowyUser.updateTime || new Date().toISOString(),
            };
            const payload: AuthSessionPayload = {
                user,
                logicalModels: [],
                runtimeLimits: { activeTaskLimit: 10, resourceUploadMB: 100, sessionUploadMB: 50 },
                drawingEngine: { defaultEngine: "tldraw" },
                features: {
                    shortDramaEnabled: true,
                    taskCenterEnabled: true,
                    creditsEnabled: false,
                    customChannelsEnabled: true,
                    frontendModelsEnabled: true,
                    pluginCenterEnabled: false,
                    systemPluginsVisibleToUsers: false,
                    desktopLocalChannelsEnabled: false,
                },
            };
            authSessionCache = { payload, expiresAt: Date.now() + 5_000 };
            return payload;
        })
        .catch((err) => {
            // 未登录（401）或网络错误时返回 null user，不中断流程
            const isUnauthenticated =
                err?.code === 401 ||
                err?.status === 401 ||
                err?.response?.status === 401 ||
                err?.response?.data?.code === 401 ||
                err?.name === "AxiosError" ||
                (err instanceof Error && err.message?.includes("Network Error"));
            if (isUnauthenticated) {
                const payload: AuthSessionPayload = { user: null };
                authSessionCache = { payload, expiresAt: Date.now() + 5_000 };
                return payload;
            }
            throw err;
        })
        .finally(() => {
            authSessionRequest = null;
        });
    return authSessionRequest;
}

/** Snowy C 端用户原始结构 */
type SnowyClientLoginUser = {
    id?: string;
    account?: string;
    email?: string;
    name?: string;
    nickname?: string;
    avatar?: string;
    phone?: string;
    gender?: string;
    userStatus?: string;
    lastLoginTime?: string;
    createTime?: string;
    updateTime?: string;
    [key: string]: unknown;
};

export function getSystemChannels() {
    return request<{ channels: ModelChannel[] }>(api.get("/channels/system"));
}

export function getFeatureAvailability() {
    return request<{ features: FeatureAvailability }>(api.get("/features"));
}

export function getAdminFeatureAvailability() {
    return request<{ features: FeatureAvailability }>(api.get("/admin/settings/features"));
}

export function updateAdminFeatureAvailability(features: Pick<FeatureAvailability, "shortDramaEnabled" | "taskCenterEnabled" | "creditsEnabled" | "customChannelsEnabled" | "frontendModelsEnabled" | "pluginCenterEnabled" | "systemPluginsVisibleToUsers">) {
    return request<{ features: FeatureAvailability }>(api.patch("/admin/settings/features", features));
}

export async function login(input: { username: string; password: string }) {
    // 直接调用 Snowy C 端登录接口，密码需 SM2 加密
    const token = await request<string>(api.post("/auth/c/doLogin", {
        account: input.username,
        password: sm2Encrypt(input.password),
    }));
    if (token) {
        localStorage.setItem("snowy_token", token);
    }
    invalidateAuthSessionCache();
    // 登录后立即获取用户信息返回
    const session = await getAuthSession();
    return { user: session.user!, token };
}

export function sendRegistrationEmailCode(email: string) {
    return request<{ sent: boolean }>(api.post("/auth/email-code", { email }));
}

export function sendPasswordResetEmailCode(email: string) {
    return request<{ sent: boolean }>(api.post("/auth/password-reset-code", { email }));
}

export function resetPassword(input: { email: string; emailCode: string; password: string }) {
    return request<{ reset: boolean }>(api.post("/auth/password-reset", input));
}

export function register(input: { username: string; email?: string; emailCode?: string; displayName?: string; password: string }) {
    // 直接调用 Snowy C 端注册接口，密码需 SM2 加密
    return request<string>(api.post("/auth/c/register", {
        account: input.username,
        password: sm2Encrypt(input.password),
    })).then(() => ({ user: null as unknown as LocalUser }));
}

export async function logout() {
    // 直接调用 Snowy C 端登出接口
    const result = await request<string>(api.get("/auth/c/doLogout"));
    localStorage.removeItem("snowy_token");
    invalidateAuthSessionCache();
    return { ok: true };
}

export type AdminListParams = { keyword?: string; status?: string; role?: string; page?: number; limit?: number };

export function listAdminUsers(params: AdminListParams = {}) {
    return request<{ users: AdminUser[]; total: number; page: number; limit: number }>(api.get("/admin/users", { params }));
}

export function createAdminUser(input: { username: string; displayName: string; email?: string; password: string; role: LocalUser["role"]; status: LocalUser["status"] }) {
    return request<{ user: AdminUser }>(api.post("/admin/users", input));
}

export function getAdminReferences() {
    return request<AdminReferenceData>(api.get("/admin/references"));
}

export function getAdminUserDetail(id: string) {
    return request<AdminUserDetail>(api.get(`/admin/users/${encodeURIComponent(id)}/detail`));
}

export function listAdminUserLedger(id: string, params: { page?: number; limit?: number; type?: string } = {}) {
    return request<{ entries: CreditLedgerEntry[]; total: number; page: number; limit: number }>(api.get(`/admin/users/${encodeURIComponent(id)}/ledger`, { params }));
}

export function listAdminUserTasks(id: string, params: { page?: number; limit?: number } = {}) {
    return request<{ tasks: AdminUserTask[]; total: number; page: number; limit: number }>(api.get(`/admin/users/${encodeURIComponent(id)}/tasks`, { params }));
}

export function listAdminUserAuditEvents(id: string, params: { page?: number; limit?: number } = {}) {
    return request<{ events: AdminAuditEvent[]; total: number; page: number; limit: number }>(api.get(`/admin/users/${encodeURIComponent(id)}/audit-events`, { params }));
}

export function updateAdminUser(id: string, input: Partial<Pick<LocalUser, "displayName" | "email" | "role" | "status">> & { password?: string }) {
    return request<{ user: LocalUser }>(api.patch(`/admin/users/${encodeURIComponent(id)}`, input));
}

export function deleteAdminUser(id: string) {
    return request<{ ok: boolean }>(api.delete(`/admin/users/${encodeURIComponent(id)}`));
}

export function bulkDisableAdminUsers(userIds: string[]) {
    return request<{ users: LocalUser[]; disabledCount: number }>(api.post("/admin/users/bulk-disable", { userIds }));
}

export function listAdminChannels(params: AdminListParams = {}) {
    return request<{ channels: ModelChannel[]; total: number; page: number; limit: number }>(api.get("/admin/channels", { params }));
}

export function createAdminChannel(input: Partial<ModelChannel> & { useGlobalConcurrency?: boolean }) {
    return request<{ channel: ModelChannel }>(api.post("/admin/channels", input));
}

export function updateAdminChannel(id: string, input: Partial<ModelChannel> & { useGlobalConcurrency?: boolean }) {
    return request<{ channel: ModelChannel }>(api.patch(`/admin/channels/${encodeURIComponent(id)}`, input));
}

export function deleteAdminChannel(id: string) {
    return request<{ ok: boolean }>(api.delete(`/admin/channels/${encodeURIComponent(id)}`));
}

export function listAdminPromptTemplates() {
    return request<{ templates: PromptTemplate[]; definitions: PromptOperationDefinition[] }>(api.get("/admin/prompt-templates"));
}

export function createAdminPromptTemplate(input: Pick<PromptTemplate, "operation" | "name" | "content"> & { enabled?: boolean }) {
    return request<{ template: PromptTemplate }>(api.post("/admin/prompt-templates", input));
}

export function updateAdminPromptTemplate(id: string, input: Pick<PromptTemplate, "operation" | "name" | "content"> & { enabled?: boolean }) {
    return request<{ template: PromptTemplate }>(api.patch(`/admin/prompt-templates/${encodeURIComponent(id)}`, input));
}

export function deleteAdminPromptTemplate(id: string) {
    return request<{ ok: boolean }>(api.delete(`/admin/prompt-templates/${encodeURIComponent(id)}`));
}

export function listUserPromptPreferences() {
    return request<{ preferences: UserPromptPreference[] }>(api.get("/settings/prompt-templates"));
}

export function updateUserPromptCustomization(operation: string, input: Pick<UserPromptCustomization, "mode" | "content">) {
    return request<{ customization: UserPromptCustomization }>(api.patch(`/settings/prompt-templates/${encodeURIComponent(operation)}`, input));
}

export function resetUserPromptCustomization(operation: string) {
    return request<{ ok: boolean }>(api.delete(`/settings/prompt-templates/${encodeURIComponent(operation)}`));
}

export function getAdminOSSSetting() {
    return request<{ setting: AdminOSSSetting }>(api.get("/admin/settings/oss"));
}

export function updateAdminOSSSetting(input: Partial<AdminOSSSetting>) {
    return request<{ setting: AdminOSSSetting }>(api.patch("/admin/settings/oss", input));
}

export function testAdminOSSConnection(input: OSSConnectionTestInput) {
    return request<OSSConnectionTestResult>(api.post("/admin/settings/oss/test", input));
}

export function getAdminArkPrivateAssetSetting() {
    return request<{ setting: AdminArkPrivateAssetSetting }>(api.get("/admin/settings/ark-private-assets"));
}

export function updateAdminArkPrivateAssetSetting(input: Partial<AdminArkPrivateAssetSetting>) {
    return request<{ setting: AdminArkPrivateAssetSetting }>(api.patch("/admin/settings/ark-private-assets", input));
}

export function getAdminRuntimePolicySetting() {
    return request<{ setting: RuntimePolicySetting }>(api.get("/admin/settings/runtime-policy"));
}

export function getAdminSelfUseRuntimePolicy() {
    return request<{ setting: RuntimePolicySetting }>(api.get("/admin/settings/runtime-policy/self-use"));
}

export function updateAdminRuntimePolicySetting(input: Pick<RuntimePolicySetting, "resource" | "task" | "request">) {
    return request<{ setting: RuntimePolicySetting }>(api.put("/admin/settings/runtime-policy", input));
}

export function resetAdminRuntimePolicySetting() {
    return request<{ setting: RuntimePolicySetting }>(api.delete("/admin/settings/runtime-policy"));
}

export function getAdminDrawingEngineSetting() {
    return request<{ setting: CanvasDrawingEngineSetting }>(api.get("/admin/settings/drawing-engine"));
}

export function updateAdminDrawingEngineSetting(input: Pick<CanvasDrawingEngineSetting, "defaultEngine" | "tldrawLicenseKey">) {
    return request<{ setting: CanvasDrawingEngineSetting }>(api.patch("/admin/settings/drawing-engine", input));
}

export function listAdminApiLogs(params: AdminListParams = {}) {
    return request<{ logs: ApiCallLog[]; total: number; page: number; limit: number }>(api.get("/admin/api-logs", { params }));
}

export function getAdminApiLog(id: string) {
    return request<{ log: ApiCallLog }>(api.get(`/admin/api-logs/${encodeURIComponent(id)}`));
}

export function queryAdminApiLogTask(id: string) {
    return request<AdminProviderTaskQueryResult>(api.post(`/admin/api-logs/${encodeURIComponent(id)}/query-task`));
}

export async function exportAdminApiLogs(params: AdminListParams & { ids?: string[] } = {}) {
    const response = await api.get<Blob>("/admin/api-logs-export.csv", { params: { ...params, ids: params.ids?.join(",") }, responseType: "blob" });
    return response.data;
}

export function getAdminAnalytics(params: AnalyticsFilters) {
    return request<AdminAnalytics>(api.get("/admin/analytics/overview", { params }));
}

export async function exportAdminAnalytics(params: AnalyticsFilters) {
    const response = await api.get<Blob>("/admin/analytics/export.csv", { params, responseType: "blob" });
    return response.data;
}

export function listAdminModelPricings() {
    return request<{ pricings: ModelPricing[] }>(api.get("/admin/model-pricings"));
}

export function createAdminModelPricing(input: Omit<ModelPricing, "id" | "createdAt" | "updatedAt">) {
    return request<{ pricing: ModelPricing }>(api.post("/admin/model-pricings", input));
}

export function updateAdminModelPricing(id: string, input: Omit<ModelPricing, "id" | "createdAt" | "updatedAt">) {
    return request<{ pricing: ModelPricing }>(api.patch(`/admin/model-pricings/${encodeURIComponent(id)}`, input));
}

export function deleteAdminModelPricing(id: string) {
    return request<{ ok: boolean }>(api.delete(`/admin/model-pricings/${encodeURIComponent(id)}`));
}
