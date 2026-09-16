import { App, Button, Input, Modal, Select, Tag, Typography } from "antd";
import { BookOpenText, Clapperboard, Copy, Sparkles, Wand2 } from "lucide-react";
import { useMemo, useState } from "react";

import { PromptStudioModal } from "@/components/canvas/prompt-studio-modal";
import {
    PROMPT_AGENT_TEMPLATES,
    PROMPT_TEMPLATES,
    PROMPT_TEMPLATE_CATEGORIES,
    PROMPT_TEMPLATE_CATEGORY_LABEL,
    PromptTemplate,
    renderPromptTemplate,
    templateDefaultValues,
} from "@/lib/prompt-templates";

const { Text } = Typography;

/**
 * 技能库 · 提示词模板面板
 * 内置提示词模板（变量槽位）+ 智能体模板（导演方法论），
 * 支持浏览/筛选/填变量/复制/一键专业生成。
 */
export function PromptTemplatePanel() {
    const { message } = App.useApp();
    const [activeCategory, setActiveCategory] = useState<string>("camera");
    const [activeTemplate, setActiveTemplate] = useState<PromptTemplate | null>(null);
    const [templateValues, setTemplateValues] = useState<Record<string, string>>({});
    const [studioOpen, setStudioOpen] = useState(false);
    const [studioPrompt, setStudioPrompt] = useState("");
    const [detailOpen, setDetailOpen] = useState(false);

    const filteredTemplates = useMemo(() => PROMPT_TEMPLATES.filter((t) => t.category === activeCategory), [activeCategory]);

    const handleCardClick = (template: PromptTemplate) => {
        setActiveTemplate(template);
        setTemplateValues(templateDefaultValues(template));
        setDetailOpen(true);
    };

    const rendered = activeTemplate ? renderPromptTemplate(activeTemplate, templateValues) : "";

    const handleCopy = async (text: string) => {
        try {
            await navigator.clipboard.writeText(text);
            message.success("已复制到剪贴板");
        } catch {
            message.error("复制失败");
        }
    };

    const openStudio = (initial: string) => {
        setStudioPrompt(initial);
        setStudioOpen(true);
    };

    return (
        <div className="space-y-8 py-6">
            <section aria-labelledby="prompt-template-heading">
                <div className="mb-3 flex items-center justify-between">
                    <h2 id="prompt-template-heading" className="flex items-center gap-2 text-base font-semibold text-foreground/75">
                        <span className="skill-group-icon"><BookOpenText className="size-4" /></span>
                        提示词模板
                        <span className="text-[var(--fs-label)] font-normal text-foreground/32">{PROMPT_TEMPLATES.length} 个内置</span>
                    </h2>
                    <Select
                        size="small"
                        className="w-36"
                        value={activeCategory}
                        onChange={setActiveCategory}
                        options={PROMPT_TEMPLATE_CATEGORIES.map((c) => ({ value: c, label: PROMPT_TEMPLATE_CATEGORY_LABEL[c] }))}
                    />
                </div>
                <p className="mb-4 text-[var(--fs-label)] text-foreground/45">按"主体+动作+场景+光线+镜头+风格+节奏"公式组织，点卡片填变量即可生成专业提示词。</p>
                <div className="library-grid skill-library-grid">
                    {filteredTemplates.map((template) => (
                        <button key={template.id} type="button" className="library-card library-card-surface skill-library-card group text-left" onClick={() => handleCardClick(template)}>
                            <span className="library-icon-tile skill-card-icon" aria-hidden="true"><Clapperboard className="size-4" /></span>
                            <div className="skill-card-top"><h3>{template.name}</h3></div>
                            <div className="skill-card-description"><p>{template.description}</p></div>
                            <div className="skill-card-footer">
                                <span className="skill-card-tag">{PROMPT_TEMPLATE_CATEGORY_LABEL[template.category]}</span>
                                <span className="skill-card-tag">{template.applicable === "both" ? "图文通用" : template.applicable === "video" ? "视频" : "图片"}</span>
                            </div>
                            <div className="mt-3 flex flex-wrap gap-1">{template.tags?.map((tag) => <span key={tag} className="rounded bg-foreground/5 px-1.5 py-0.5 text-[10px] text-foreground/50">{tag}</span>)}</div>
                            <div className="skill-card-action"><span className="skill-card-added-count">点击填写变量并生成</span></div>
                        </button>
                    ))}
                </div>
            </section>

            <section aria-labelledby="agent-template-heading">
                <div className="mb-3 flex items-center justify-between">
                    <h2 id="agent-template-heading" className="flex items-center gap-2 text-base font-semibold text-foreground/75">
                        <span className="skill-group-icon"><Sparkles className="size-4" /></span>
                        智能体模板
                        <span className="text-[var(--fs-label)] font-normal text-foreground/32">{PROMPT_AGENT_TEMPLATES.length} 个内置</span>
                    </h2>
                </div>
                <p className="mb-4 text-[var(--fs-label)] text-foreground/45">像导演一样工作：按方法论多步拆解，输出可直接使用的专业提示词。</p>
                <div className="library-grid skill-library-grid">
                    {PROMPT_AGENT_TEMPLATES.map((agent) => (
                        <div key={agent.id} className="library-card library-card-surface skill-library-card group">
                            <span className="library-icon-tile skill-card-icon" aria-hidden="true"><Sparkles className="size-4" /></span>
                            <div className="skill-card-top"><h3>{agent.icon} {agent.name}</h3></div>
                            <div className="skill-card-description"><p>{agent.description}</p></div>
                            <div className="skill-card-footer"><span className="skill-card-tag">{agent.applicable === "video" ? "视频" : "图片"}</span><span className="skill-card-tag">方法论引导</span></div>
                            <div className="skill-card-action">
                                <button type="button" className="skill-card-join" onClick={() => openStudio("")}>
                                    <Wand2 className="size-3.5" />
                                    <span>打开专业生成</span>
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            </section>

            {/* 模板详情：填变量 */}
            <Modal
                title={activeTemplate ? `提示词模板 · ${activeTemplate.name}` : ""}
                open={detailOpen}
                onCancel={() => setDetailOpen(false)}
                width={620}
                footer={
                    <div className="flex items-center justify-between">
                        <Button icon={<Copy className="size-3.5" />} disabled={!rendered} onClick={() => void handleCopy(rendered)}>复制模板</Button>
                        <div className="flex gap-2">
                            <Button onClick={() => setDetailOpen(false)}>取消</Button>
                            <Button type="primary" icon={<Wand2 className="size-3.5" />} onClick={() => { setDetailOpen(false); openStudio(rendered); }}>用模板专业生成</Button>
                        </div>
                    </div>
                }
            >
                {activeTemplate ? (
                    <div className="space-y-4">
                        <div>
                            <Text type="secondary" className="text-xs">{activeTemplate.description}</Text>
                            <div className="mt-1 flex flex-wrap gap-1">{activeTemplate.tags?.map((tag) => <Tag key={tag} className="!m-0">{tag}</Tag>)}</div>
                        </div>
                        <div className="space-y-2">
                            {activeTemplate.variables.map((v) => (
                                <div key={v.key} className="flex items-center gap-2">
                                    <span className="w-16 shrink-0 text-xs text-foreground/60">{v.label}{v.required ? <span className="text-red-500">*</span> : null}</span>
                                    <Input
                                        value={templateValues[v.key] || ""}
                                        onChange={(e) => setTemplateValues((prev) => ({ ...prev, [v.key]: e.target.value }))}
                                        placeholder={v.placeholder}
                                    />
                                </div>
                            ))}
                        </div>
                        <div>
                            <div className="mb-1 text-xs text-foreground/60">模板预览</div>
                            <div className="whitespace-pre-wrap rounded-lg border border-border/60 bg-muted/30 p-3 text-sm leading-relaxed text-foreground/80">{rendered || "填写变量后这里会生成完整提示词…"}</div>
                        </div>
                    </div>
                ) : null}
            </Modal>

            <PromptStudioModal
                open={studioOpen}
                onClose={() => setStudioOpen(false)}
                onApply={(prompt) => {
                    setStudioOpen(false);
                    void handleCopy(prompt);
                }}
                mode="video"
                initialPrompt={studioPrompt}
            />
        </div>
    );
}
