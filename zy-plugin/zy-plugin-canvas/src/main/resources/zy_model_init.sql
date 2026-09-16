-- ========================================
-- 模型配置初始化脚本（PostgreSQL）
-- 合并Provider和Config为一个表
-- ========================================

-- 模型配置表
CREATE TABLE IF NOT EXISTS zy_model (
    id                  varchar(64) not null primary key,
    model_key           varchar(128) not null unique,
    model_name          varchar(256) not null,
    provider_name       varchar(128),
    capability          varchar(32) not null,
    protocol            varchar(64),
    base_url            varchar(512),
    api_key             varchar(512),
    max_concurrency     int default 10,
    timeout_seconds     int default 300,
    capability_spec_json text,
    status              varchar(32) default 'active',
    is_default          varchar(10) default 'N',
    sort_code           int default 0,
    delete_flag         varchar(32) default 'NOT_DELETE',
    create_user         varchar(64),
    create_time         timestamp default current_timestamp,
    update_user         varchar(64),
    update_time         timestamp
);

COMMENT ON TABLE zy_model IS '模型配置';
COMMENT ON COLUMN zy_model.id IS '主键';
COMMENT ON COLUMN zy_model.model_key IS '模型标识（如：gpt-4o, kling-v1）';
COMMENT ON COLUMN zy_model.model_name IS '模型名称';
COMMENT ON COLUMN zy_model.provider_name IS '提供商名称（如：OpenAI, 可灵）';
COMMENT ON COLUMN zy_model.capability IS '能力类型：text-文本，image-图片，video-视频，audio-音频';
COMMENT ON COLUMN zy_model.protocol IS '协议类型：newapi(NewAPI官方/video-generations), newapi-channel-1, newapi-channel-2, openai, gemini等';
COMMENT ON COLUMN zy_model.base_url IS 'API基础URL';
COMMENT ON COLUMN zy_model.api_key IS 'API密钥（SM4加密存储）';
COMMENT ON COLUMN zy_model.max_concurrency IS '最大并发数';
COMMENT ON COLUMN zy_model.timeout_seconds IS '超时时间（秒）';
COMMENT ON COLUMN zy_model.capability_spec_json IS '能力规格JSON';
COMMENT ON COLUMN zy_model.status IS '状态：active-启用，disabled-禁用';
COMMENT ON COLUMN zy_model.is_default IS '是否默认：Y-是，N-否';
COMMENT ON COLUMN zy_model.sort_code IS '排序码';
COMMENT ON COLUMN zy_model.delete_flag IS '删除标志';
COMMENT ON COLUMN zy_model.create_user IS '创建人';
COMMENT ON COLUMN zy_model.create_time IS '创建时间';
COMMENT ON COLUMN zy_model.update_user IS '更新人';
COMMENT ON COLUMN zy_model.update_time IS '更新时间';

CREATE INDEX idx_zy_model_key ON zy_model(model_key);
CREATE INDEX idx_zy_model_capability ON zy_model(capability);
CREATE INDEX idx_zy_model_status ON zy_model(status);
CREATE INDEX idx_zy_model_default ON zy_model(is_default);

-- 插入默认模型数据

-- 文本模型
INSERT INTO zy_model (id, model_key, model_name, provider_name, capability, protocol, status, is_default, sort_code, capability_spec_json) VALUES
('model_gpt4o', 'gpt-4o', 'GPT-4o', 'OpenAI', 'text', 'newapi-channel-2', 'active', 'Y', 1, '{"maxTokens": 128000}'),
('model_gpt4o_mini', 'gpt-4o-mini', 'GPT-4o Mini', 'OpenAI', 'text', 'newapi-channel-2', 'active', 'N', 2, '{"maxTokens": 128000}'),
('model_gemini_flash', 'gemini-1.5-flash', 'Gemini 1.5 Flash', 'Google', 'text', 'newapi-channel-2', 'active', 'N', 10, '{"maxTokens": 1000000}'),
('model_claude_35_sonnet', 'claude-3-5-sonnet', 'Claude 3.5 Sonnet', 'Anthropic', 'text', 'newapi-channel-2', 'active', 'N', 20, '{"maxTokens": 200000}');

-- 图片模型
INSERT INTO zy_model (id, model_key, model_name, provider_name, capability, protocol, status, is_default, sort_code, capability_spec_json) VALUES
('model_dall_e_3', 'dall-e-3', 'DALL·E 3', 'OpenAI', 'image', 'newapi-channel-2', 'active', 'Y', 100, '{"sizes": ["1024x1024", "1024x1792", "1792x1024"], "quality": ["standard", "hd"]}'),
('model_flux_dev', 'flux-dev', 'Flux Dev', 'Flux', 'image', 'newapi-channel-2', 'active', 'N', 101, '{"sizes": ["1024x1024"]}'),
('model_sd_xl', 'stable-diffusion-xl', 'Stable Diffusion XL', 'Stability', 'image', 'newapi-channel-2', 'active', 'N', 102, '{"sizes": ["1024x1024"]}');

-- 视频模型
INSERT INTO zy_model (id, model_key, model_name, provider_name, capability, protocol, status, is_default, sort_code, capability_spec_json, max_concurrency, timeout_seconds) VALUES
('model_kling_v1', 'kling-v1', '可灵视频 V1', '可灵', 'video', 'newapi-channel-2', 'active', 'Y', 200, '{"durations": [5, 10], "ratios": ["16:9", "9:16", "1:1"]}', 5, 600),
('model_kling_v1_5', 'kling-v1-5', '可灵视频 V1.5', '可灵', 'video', 'newapi-channel-2', 'active', 'N', 201, '{"durations": [5, 10], "ratios": ["16:9", "9:16", "1:1"]}', 5, 600),
('model_runway_gen3', 'runway-gen3', 'Runway Gen-3', 'Runway', 'video', 'newapi-channel-2', 'active', 'N', 210, '{"durations": [5, 10], "ratios": ["16:9", "9:16"]}', 3, 600),
('model_sora', 'sora', 'Sora', 'OpenAI', 'video', 'newapi-channel-2', 'disabled', 'N', 220, '{"durations": [5, 10, 15, 20], "ratios": ["16:9", "9:16", "1:1"]}', 3, 600),
-- wan2.7-r2v：万相参考生视频，经 NewAPI 网关；协议 newapi，后端自动走 input.media
('model_wan27_r2v', 'wan2.7-r2v', '万相2.7 参考生视频', '阿里云', 'video', 'newapi', 'active', 'N', 230, '{"durations": [2, 5, 10], "ratios": ["16:9", "9:16", "1:1", "4:3", "3:4"], "resolutions": ["720P", "1080P"], "supportsReferenceVideo": true, "supportsReferenceVoice": true}', 3, 600);

-- 音频模型
INSERT INTO zy_model (id, model_key, model_name, provider_name, capability, protocol, status, is_default, sort_code, capability_spec_json) VALUES
('model_tts_1', 'tts-1', 'TTS-1', 'OpenAI', 'audio', 'newapi-channel-2', 'active', 'Y', 300, '{"voices": ["alloy", "echo", "fable", "onyx", "nova", "shimmer"]}'),
('model_tts_1_hd', 'tts-1-hd', 'TTS-1 HD', 'OpenAI', 'audio', 'newapi-channel-2', 'active', 'N', 301, '{"voices": ["alloy", "echo", "fable", "onyx", "nova", "shimmer"]}');