import { useEffect, useState } from "react";
import { App, Button, Input, Modal, Select, Space, Typography } from "antd";
import { Copy, Sparkles, Wand2 } from "lucide-react";

import { optimizePrompt } from "@/services/api/prompt-optimizer";
import { apiClient, request } from "@/services/api/request";

const { Text } = Typography;

type TextModel = {
    id: string;
    modelKey: string;
    modelName: string;
    providerName?: string;
    isDefault?: boolean;
};

type PromptOptimizerModalProps = {
    open: boolean;
    onClose: () => void;
    onApply: (optimizedPrompt: string) => void;
    mode: "image" | "video";
    initialPrompt?: string;
    styleHint?: string;
};

export function PromptOptimizerModal({ open, onClose, onApply, mode, initialPrompt = "", styleHint }: PromptOptimizerModalProps) {
    const { message } = App.useApp();
    const [inputPrompt, setInputPrompt] = useState(initialPrompt);
    const [optimizedPrompt, setOptimizedPrompt] = useState("");
    const [loading, setLoading] = useState(false);
    const [textModels, setTextModels] = useState<TextModel[]>([]);
    const [selectedModel, setSelectedModel] = useState("");

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
                // 严格筛选：capability 必须是 text
                const models: TextModel[] = [];
                for (const provider of data.providers || []) {
                    for (const m of provider.models || []) {
                        if (!m.modelKey) continue;
                        const cap = String(m.capability || "").toLowerCase().trim();
                        if (cap !== "text") continue; // 严格匹配 text
                        models.push({ id: m.modelKey, modelKey: m.modelKey, modelName: m.modelName || m.modelKey, providerName: provider.name, isDefault: m.isDefault });
                    }
                }
                console.log("[PromptOptimizer] 文本模型列表:", models);
                setTextModels(models);
                // 从文本模型里找 isDefault=true 的
                const defaultModel = models.find((m) => m.isDefault === true);
                const fallbackModel = models[0];
                const targetModel = defaultModel || fallbackModel;
                console.log("[PromptOptimizer] 默认模型:", defaultModel?.modelKey, "兜底:", fallbackModel?.modelKey);
                if (targetModel) {
                    setSelectedModel(targetModel.modelKey);
                }
            } catch (e) {
                console.error("[PromptOptimizer] 加载模型失败:", e);
            }
        })();
        return () => { cancelled = true; };
    }, [open]);

    const handleOptimize = async () => {
        if (!inputPrompt.trim()) {
            message.warning("请输入要优化的提示词");
            return;
        }
        if (!selectedModel) {
            message.warning("请选择文本模型");
            return;
        }
        setLoading(true);
        try {
            const result = await optimizePrompt({
                prompt: inputPrompt.trim(),
                mode,
                styleHint,
                model: selectedModel,
            });
            setOptimizedPrompt(result.optimized);
            message.success("提示词优化完成");
        } catch (error) {
            message.error(error instanceof Error ? error.message : "优化失败，请重试");
        } finally {
            setLoading(false);
        }
    };

    const handleApply = () => {
        if (!optimizedPrompt.trim()) {
            message.warning("请先优化提示词");
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

    // 按 provider 分组，默认模型排最前
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

    return (
        <Modal
            title={
                <span className="flex items-center gap-2">
                    <Sparkles className="size-4 text-purple-500" />
                    优化{mode === "video" ? "视频" : "图片"}提示词
                </span>
            }
            open={open}
            onCancel={onClose}
            width={640}
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
            <div className="space-y-4">
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
                        placeholder={mode === "video" ? "例如：一个女孩在海边散步" : "例如：一个未来科幻城市"}
                        autoSize={{ minRows: 3, maxRows: 6 }}
                        disabled={loading}
                    />
                </div>

                <div className="flex justify-center">
                    <Button
                        type="primary"
                        icon={<Wand2 className="size-4" />}
                        loading={loading}
                        onClick={handleOptimize}
                        className="px-8"
                    >
                        {loading ? "优化中..." : "生成专业提示词"}
                    </Button>
                </div>

                {optimizedPrompt && (
                    <div>
                        <div className="mb-2 flex items-center justify-between">
                            <Text strong>优化结果</Text>
                            <Text type="secondary" className="text-xs">
                                {mode === "image" ? "英文（图片模型更准确）" : "中文"}
                            </Text>
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
                )}
            </div>
        </Modal>
    );
}
