import { apiClient, request } from "@/services/api/request";

export type OptimizePromptRequest = {
    prompt: string;
    mode: "image" | "video";
    model?: string;
    styleHint?: string;
};

export type OptimizePromptResponse = {
    original: string;
    optimized: string;
    mode: string;
};

/**
 * 优化提示词：用户输入简单描述 → LLM 生成专业影视提示词
 */
export function optimizePrompt(input: OptimizePromptRequest) {
    return request<OptimizePromptResponse>(apiClient.post("/api/agent/optimize-prompt", input));
}
