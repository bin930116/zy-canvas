-- =========================================================
-- 修复 zy_asset 表：添加缺失字段
-- =========================================================

-- 添加封面图片 URL 字段
ALTER TABLE zy_asset ADD COLUMN IF NOT EXISTS cover_url varchar(512);

-- 添加标签字段（JSON 数组）
ALTER TABLE zy_asset ADD COLUMN IF NOT EXISTS tags text;

-- 添加备注信息字段
ALTER TABLE zy_asset ADD COLUMN IF NOT EXISTS note text;

-- 添加字段注释
COMMENT ON COLUMN zy_asset.cover_url IS '封面图片URL';
COMMENT ON COLUMN zy_asset.tags IS '标签(JSON数组)';
COMMENT ON COLUMN zy_asset.note IS '备注信息';

-- 更新已有的注释（补充 media_type 和 category 的枚举值）
COMMENT ON COLUMN zy_asset.media_type IS '介质类型：image|video|audio|text|model';
COMMENT ON COLUMN zy_asset.category IS '分类：character|material|other';
COMMENT ON COLUMN zy_asset.status IS '状态：active|archived|deleted';
COMMENT ON COLUMN zy_asset.usages IS '用途(JSON数组)';