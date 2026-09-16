import { useEffect, useMemo, useState } from "react";
import { App, Button, Input, Modal, Select, Space, Tabs, Tag, Typography } from "antd";
import { BookOpenText, Copy, Sparkles, Wand2, Clapperboard } from "lucide-react";

import { optimizePrompt } from "@/services/api/prompt-optimizer";
import { apiClient, request } from "@/services/api/request";
import {
    PROMPT_AGENT_TEMPLATES,
    PROMPT_TEMPLATES,
    PROMPT_TEMPLATE_CATEGORIES,
    PROMPT_TEMPLATE_CATEGORY_LABEL,
    PromptAgentTemplate,
    PromptTemplate,
    renderPromptTemplate,
    templateDefaultValues,
} from "@/lib/prompt-templates";

const { Text } = Typography;

type TextModel = {
    id: string;
    modelKey: string;
    modelName: string;
    providerName?: string;
    isDefault?: boolean;
};

export type PromptStudioContext = {
    /** 镜头标题 */
    title?: string;
    /** 画面内容 / 表演动作 */
    action?: string;
    /** 对白旁白 */
    dialogue?: string;
    /** 景别 */
    shotSize?: string;
    /** 机位角度 */
    cameraAngle?: string;
    /** 运镜方式 */
    cameraMovement?: string;
    /** 镜头时长（秒） */
    duration?: number;
    /** 题材/风格提示 */
    styleHint?: string;
};

type PromptStudioModalProps = {
    open: boolean;
    onClose: () => void;
    /** 应用生成的提示词（回填视频提示词/图片提示词） */
    onApply: (prompt: string) => void;
    mode: "image" | "video";
    /** 当前输入框已有内容（作为生成的基础描述） */
    initialPrompt?: string;
    /** 当前镜头的上下文（标题/动作/对白/景别/运镜等），自动拼入生成请求 */
    context?: PromptStudioContext;
};

/**
 * 专业生成提示词工作台
 * 三个能力：
 * 1. 专业生成：输入简单描述 + 镜头上下文 → LLM 按影视方法论生成专业提示词
 * 2. 提示词模板：内置结构化模板（变量槽位），选择填充后可直接生成/应用
 * 3. 智能体模板：内置导演方法论智能体，按多步流程生成（参考即梦 Agent 技能）
 */
export function PromptStudioModal({ open, onClose, onApply, mode, initialPrompt = "", context }: PromptStudioModalProps) {
    const { message } = App.useApp();
    const [tab, setTab] = useState("generate");

    // 专业生成
    const [inputPrompt, setInputPrompt] = useState(initialPrompt);
    const [optimizedPrompt, setOptimizedPrompt] = useState("");
    const [loading, setLoading] = useState(false);
    const [textModels, setTextModels] = useState<TextModel[]>([]);
    const [selectedModel, setSelectedModel] = useState("");

    // 提示词模板
    const [activeCategory, setActiveCategory] = useState<string>("camera");
    const [templateValues, setTemplateValues] = useState<Record<string, string>>({});
    const [activeTemplate, setActiveTemplate] = useState<PromptTemplate | null>(null);

    // 智能体模板
    const [activeAgent, setActiveAgent] = useState<PromptAgentTemplate | null>(null);
    const [agentInput, setAgentInput] = useState("");

    // 打开时初始化
    useEffect(() => {
        if (!open) return;
        setInputPrompt(initialPrompt);
        setOptimizedPrompt("");
        setTab("generate");
        setActiveTemplate(null);
        setActiveAgent(null);
        setAgentInput("");
    }, [open, initialPrompt]);

    // 加载文本模型列表
    useEffect(() => {
        if (!open) return;
        let cancelled = false;
        void (async () => {
            try {
                const data = await request<{ providers?: Array<{ name?: string; models?: Array<{ id: string; modelKey: string; modelName: string; capability?: string; isDefault?: boolean }> }> }>(
                    apiClient.get("/api/model-catalog")
                );
                if (cancelled) return;
                const models: TextModel[] = [];
                for (const provider of data.providers || []) {
                    for (const m of provider.models || []) {
                        if (!m.modelKey) continue;
                        const cap = String(m.capability || "").toLowerCase().trim();
                        if (cap !== "text") continue;
                        models.push({ id: m.modelKey, modelKey: m.modelKey, modelName: m.modelName || m.modelKey, providerName: provider.name, isDefault: m.isDefault });
                    }
                }
                setTextModels(models);
                const defaultModel = models.find((m) => m.isDefault === true);
                const targetModel = defaultModel || models[0];
                if (targetModel) setSelectedModel(targetModel.modelKey);
            } catch (e) {
                console.error("[PromptStudio] 加载模型失败:", e);
            }
        })();
        return () => { cancelled = true; };
    }, [open]);

    // 按 mode 过滤模板
    const filteredTemplates = useMemo(() => PROMPT_TEMPLATES.filter((t) => t.applicable === "both" || t.applicable === mode), [mode]);
    const filteredAgents = useMemo(() => PROMPT_AGENT_TEMPLATES.filter((a) => a.applicable === mode), [mode]);

    /** 拼装镜头上下文摘要（用于生成请求的 styleHint 增强） */
    const buildContextHint = () => {
        if (!context) return undefined;
        const parts: string[] = [];
        if (context.title) parts.push(`镜头标题：${context.title}`);
        if (context.action) parts.push(`画面/表演：${context.action}`);
        if (context.dialogue) parts.push(`对白：${context.dialogue}`);
        const camera = [context.shotSize, context.cameraAngle, context.cameraMovement].filter(Boolean).join("·");
        if (camera) parts.push(`镜头语言：${camera}`);
        if (context.duration) parts.push(`时长：${context.duration}秒`);
        return parts.join("\n");
    };

    const buildStyleHint = () => {
        const hints: string[] = [];
        const ctxHint = buildContextHint();
        if (ctxHint) hints.push(ctxHint);
        if (context?.styleHint) hints.push(context.styleHint);
        return hints.length ? hints.join("\n") : undefined;
    };

    const callGenerate = async (prompt: string, extraStyleHint?: string) => {
        if (!prompt.trim()) {
            message.warning("请输入要生成的内容描述");
            return false;
        }
        if (!selectedModel) {
            message.warning("请选择文本模型");
            return false;
        }
        setLoading(true);
        try {
            const result = await optimizePrompt({
                prompt: prompt.trim(),
                mode,
                styleHint: [buildStyleHint(), extraStyleHint].filter(Boolean).join("\n") || undefined,
                model: selectedModel,
            });
            setOptimizedPrompt(result.optimized);
            message.success("生成完成");
            return true;
        } catch (error) {
            message.error(error instanceof Error ? error.message : "生成失败，请重试");
            return false;
        } finally {
            setLoading(false);
        }
    };

    const handleGenerate = async () => {
        await callGenerate(inputPrompt);
    };

    const handleTemplateSelect = (template: PromptTemplate) => {
        setActiveTemplate(template);
        setTemplateValues(templateDefaultValues(template));
    };

    const handleTemplateRender = () => {
        if (!activeTemplate) return;
        const rendered = renderPromptTemplate(activeTemplate, templateValues);
        setInputPrompt(rendered);
        message.success("模板已填入输入框，可微调后生成");
    };

    const handleTemplateGenerate = async () => {
        if (!activeTemplate) return;
        const rendered = renderPromptTemplate(activeTemplate, templateValues);
        await callGenerate(rendered, `模板：${activeTemplate.name}`);
    };

    const handleAgentGenerate = async () => {
        if (!activeAgent) return;
        await callGenerate(agentInput || inputPrompt, activeAgent.systemPrompt);
    };

    const handleApply = () => {
        if (!optimizedPrompt.trim()) {
            message.warning("请先生成提示词");
            return;
        }
        onApply(optimizedPrompt.trim());
        onClose();
    };

    const handleCopy = async () => {
        if (!optimizedPrompt.trim()) return;
        try {
            await navigator.clipboard.writeText(optimizedPrompt.trim());
            message.success("已复制到剪贴板");
        } catch {
            message.error("复制失败");
        }
    };

    // 按 provider 分组模型选项
    const modelOptions = textModels
        .slice()
        .sort((a, b) => (b.isDefault ? 1 : 0) - (a.isDefault ? 1 : 0))
        .reduce<Array<{ label: string; options: Array<{ label: string; value: string }> }>>((groups, m) => {
            const provider = m.providerName || "默认";
            let group = groups.find((g) => g.label === provider);
            if (!group) {
                group = { label: provider, options: [] };
                groups.push(group);
            }
            group.options.push({ label: m.isDefault ? `${m.modelName}（默认）` : m.modelName, value: m.modelKey });
            return groups;
        }, []);

    const contextSummary = buildContextHint();

    return (
        <Modal
            title={
                <span className="flex items-center gap-2">
                    <Sparkles className="size-4 text-purple-500" />
                    专业生成{mode === "video" ? "视频" : "图片"}提示词
                </span>
            }
            open={open}
            onCancel={onClose}
            width={720}
            footer={
                <div className="flex items-center justify-between">
                    <Button icon={<Copy className="size-3.5" />} disabled={!optimizedPrompt} onClick={handleCopy}>
                        复制结果
                    </Button>
                    <Space>
                        <Button onClick={onClose}>取消</Button>
                        <Button type="primary" disabled={!optimizedPrompt} onClick={handleApply}>
                            应用提示词
                        </Button>
                    </Space>
                </div>
            }
        >
            <Tabs
                activeKey={tab}
                onChange={setTab}
                items={[
                    {
                        key: "generate",
                        label: <span className="flex items-center gap-1.5"><Wand2 className="size-3.5" />专业生成</span>,
                        children: (
                            <div className="space-y-4">
                                {contextSummary ? (
                                    <div className="rounded-lg border border-border/60 bg-muted/40 p-3 text-xs leading-relaxed text-foreground/65">
                                        <div className="mb-1 font-semibold text-foreground/80">已自动带入镜头上下文</div>
                                        <pre className="whitespace-pre-wrap font-sans">{contextSummary}</pre>
                                    </div>
                                ) : null}
                                <div>
                                    <div className="mb-2 flex items-center justify-between">
                                        <Text strong>输入简单描述</Text>
                                        <span className="flex items-center gap-2">
                                            <Text type="secondary" className="text-xs">模型</Text>
                                            <Select
                                                size="small"
                                                value={selectedModel || undefined}
                                                onChange={setSelectedModel}
                                                options={modelOptions}
                                                placeholder="选择模型"
                                                className="min-w-[160px]"
                                                loading={loading && !textModels.length}
                                            />
                                        </span>
                                    </div>
                                    <Input.TextArea
                                        value={inputPrompt}
                                        onChange={(e) => setInputPrompt(e.target.value)}
                                        placeholder={mode === "video" ? "例如：一个女孩在海边散步，镜头缓慢推进" : "例如：一个未来科幻城市"}
                                        autoSize={{ minRows: 3, maxRows: 6 }}
                                        disabled={loading}
                                    />
                                </div>
                                <div className="flex justify-center">
                                    <Button
                                        type="primary"
                                        icon={<Wand2 className="size-4" />}
                                        loading={loading}
                                        onClick={handleGenerate}
                                        className="px-8"
                                    >
                                        {loading ? "生成中..." : "生成专业提示词"}
                                    </Button>
                                </div>
                            </div>
                        ),
                    },
                    {
                        key: "templates",
                        label: <span className="flex items-center gap-1.5"><BookOpenText className="size-3.5" />提示词模板</span>,
                        children: (
                            <div className="space-y-3">
                                <Select
                                    size="small"
                                    className="w-full"
                                    value={activeCategory}
                                    onChange={setActiveCategory}
                                    options={PROMPT_TEMPLATE_CATEGORIES.map((c) => ({ value: c, label: PROMPT_TEMPLATE_CATEGORY_LABEL[c] }))}
                                />
                                <div className="grid grid-cols-2 gap-2">
                                    {filteredTemplates
                                        .filter((t) => t.category === activeCategory)
                                        .map((template) => (
                                            <button
                                                key={template.id}
                                                type="button"
                                                onClick={() => handleTemplateSelect(template)}
                                                className={`rounded-lg border p-3 text-left transition-colors ${activeTemplate?.id === template.id ? "border-purple-400 bg-purple-50/60" : "border-border/60 bg-muted/20 hover:border-purple-300"}`}
                                            >
                                                <div className="flex items-center gap-1.5 font-medium">{template.name}{template.applicable === "both" ? <Tag className="!m-0 !text-[10px]" color="default">通用</Tag> : null}</div>
                                                <div className="mt-1 text-xs text-foreground/55">{template.description}</div>
                                                <div className="mt-1.5 flex flex-wrap gap-1">{template.tags?.map((tag) => <span key={tag} className="rounded bg-foreground/5 px-1.5 py-0.5 text-[10px] text-foreground/50">{tag}</span>)}</div>
                                            </button>
                                        ))}
                                </div>
                                {activeTemplate ? (
                                    <div className="rounded-lg border border-border/60 bg-muted/30 p-3">
                                        <div className="mb-2 flex items-center justify-between">
                                            <Text strong>填充变量：{activeTemplate.name}</Text>
                                            <Button size="small" type="link" onClick={() => setActiveTemplate(null)}>收起</Button>
                                        </div>
                                        <div className="space-y-2">
                                            {activeTemplate.variables.map((v) => (
                                                <div key={v.key} className="flex items-center gap-2">
                                                    <span className="w-16 shrink-0 text-xs text-foreground/60">{v.label}{v.required ? <span className="text-red-500">*</span> : null}</span>
                                                    <Input
                                                        size="small"
                                                        value={templateValues[v.key] || ""}
                                                        onChange={(e) => setTemplateValues((prev) => ({ ...prev, [v.key]: e.target.value }))}
                                                        placeholder={v.placeholder}
                                                    />
                                                </div>
                                            ))}
                                        </div>
                                        <div className="mt-3 flex justify-end gap-2">
                                            <Button size="small" onClick={handleTemplateRender}>填入输入框</Button>
                                            <Button size="small" type="primary" loading={loading} onClick={handleTemplateGenerate}>生成提示词</Button>
                                        </div>
                                    </div>
                                ) : null}
                            </div>
                        ),
                    },
                    {
                        key: "agents",
                        label: <span className="flex items-center gap-1.5"><Clapperboard className="size-3.5" />智能体模板</span>,
                        children: (
                            <div className="space-y-3">
                                <div className="grid grid-cols-2 gap-2">
                                    {filteredAgents.map((agent) => (
                                        <button
                                            key={agent.id}
                                            type="button"
                                            onClick={() => { setActiveAgent(agent); setAgentInput(inputPrompt); }}
                                            className={`rounded-lg border p-3 text-left transition-colors ${activeAgent?.id === agent.id ? "border-purple-400 bg-purple-50/60" : "border-border/60 bg-muted/20 hover:border-purple-300"}`}
                                        >
                                            <div className="flex items-center gap-1.5 font-medium">{agent.icon} {agent.name}</div>
                                            <div className="mt-1 text-xs text-foreground/55">{agent.description}</div>
                                        </button>
                                    ))}
                                </div>
                                {activeAgent ? (
                                    <div className="rounded-lg border border-border/60 bg-muted/30 p-3">
                                        <div className="mb-1 flex items-center justify-between">
                                            <Text strong>{activeAgent.name}</Text>
                                            <Button size="small" type="link" onClick={() => setActiveAgent(null)}>收起</Button>
                                        </div>
                                        <div className="mb-2 text-xs text-foreground/55">{activeAgent.prompts[0]}</div>
                                        <Input.TextArea
                                            value={agentInput}
                                            onChange={(e) => setAgentInput(e.target.value)}
                                            placeholder="输入你的剧情/画面想法，智能体将按导演方法论拆解生成"
                                            autoSize={{ minRows: 3, maxRows: 5 }}
                                            disabled={loading}
                                        />
                                        <div className="mt-3 flex justify-end">
                                            <Button type="primary" icon={<Clapperboard className="size-3.5" />} loading={loading} onClick={handleAgentGenerate}>
                                                {loading ? "导演工作中..." : "按导演方法论生成"}
                                            </Button>
                                        </div>
                                    </div>
                                ) : null}
                            </div>
                        ),
                    },
                ]}
            />

            {optimizedPrompt ? (
                <div className="mt-4">
                    <div className="mb-2 flex items-center justify-between">
                        <Text strong>生成结果</Text>
                        <Text type="secondary" className="text-xs">{mode === "image" ? "英文（图片模型更准确）" : "中文"}</Text>
                    </div>
                    <div className="rounded-lg border border-border/60 bg-muted/30 p-3">
                        <Input.TextArea
                            value={optimizedPrompt}
                            onChange={(e) => setOptimizedPrompt(e.target.value)}
                            autoSize={{ minRows: 4, maxRows: 10 }}
                            className="!border-0 !bg-transparent !p-0 !shadow-none"
                        />
                    </div>
                </div>
            ) : null}
        </Modal>
    );
}
