/**
 * 提示词模板库（专业生成提示词）
 *
 * 形态参考：
 * - 提示词模板：即梦/可灵"主体+动作+环境+光线+镜头+风格"公式的结构化卡片（变量槽位）
 * - 智能体模板：即梦 Agent 技能（叙事短片导演分镜 / 名导风格大师）的多步方法论流程
 * 方法论参考：seedance-prompt-skill、lanshu-awesome-ai-video-kit、ai-shortfilm-prompts
 */

export type PromptTemplateCategory = "camera" | "style" | "genre" | "mood";

export type PromptTemplateVariable = {
    key: string;
    label: string;
    placeholder: string;
    required?: boolean;
};

export type PromptTemplate = {
    id: string;
    name: string;
    category: PromptTemplateCategory;
    applicable: "video" | "image" | "both";
    /** 一句话说明适用场景 */
    description: string;
    tags?: string[];
    /** 变量槽位 */
    variables: PromptTemplateVariable[];
    /** 模板正文，{key} 会被变量替换 */
    template: string;
};

export type PromptAgentTemplate = {
    id: string;
    name: string;
    /** 一句话定位（技能描述） */
    description: string;
    /** 适用目标：video 视频提示词 / image 图片提示词 */
    applicable: "video" | "image";
    /** 该智能体的方法论系统提示词：注入 LLM，指导生成 */
    systemPrompt: string;
    /** 需用户补充的关键信息提示 */
    prompts: string[];
    icon?: string;
};

/** 模板分类展示名 */
export const PROMPT_TEMPLATE_CATEGORY_LABEL: Record<PromptTemplateCategory, string> = {
    camera: "运镜与镜头",
    style: "视觉风格",
    genre: "类型片",
    mood: "氛围情绪",
};

export const PROMPT_TEMPLATE_CATEGORIES: PromptTemplateCategory[] = ["camera", "style", "genre", "mood"];

/**
 * 内置提示词模板（MVP：12 条，覆盖运镜/风格/类型片/氛围）
 * 模板统一按"主体+动作+场景+光线+镜头+风格+节奏"公式组织，
 * {key} 为变量槽位，点击"使用模板"后由用户填充。
 */
export const PROMPT_TEMPLATES: PromptTemplate[] = [
    // ============ 运镜与镜头 ============
    {
        id: "camera-long-take",
        name: "电影级长镜头",
        category: "camera",
        applicable: "video",
        description: "一镜到底式运镜，缓慢推进/跟拍，适合建立空间关系与情绪铺垫",
        tags: ["推镜", "一镜到底", "史诗感"],
        variables: [
            { key: "subject", label: "主体", placeholder: "例如：穿风衣的主角走过废墟", required: true },
            { key: "scene", label: "场景", placeholder: "例如：末日废城街道", required: true },
            { key: "mood", label: "氛围", placeholder: "例如：冷寂、肃穆", required: false },
        ],
        template:
            "电影级一镜到底长镜头。镜头自 {scene} 远景缓慢匀速向前推进，{subject} 始终处于画面视觉中心。{mood} 的氛围贯穿全片，光线层次分明，景深由浅入深逐步聚焦主体。运动克制平滑，无跳切，画面节奏沉缓，营造身临其境的沉浸感与时间流逝感。",
    },
    {
        id: "camera-aerial",
        name: "航拍大远景",
        category: "camera",
        applicable: "both",
        description: "上帝视角俯拍，展现宏大空间尺度，适合开场定场镜头",
        tags: ["俯拍", "定场", "壮阔"],
        variables: [
            { key: "scene", label: "场景", placeholder: "例如：被云海环绕的雪山城市", required: true },
            { key: "movement", label: "运镜方向", placeholder: "例如：从高空垂直下降并缓慢旋转", required: false },
            { key: "light", label: "光线", placeholder: "例如：金色晨光斜射", required: false },
        ],
        template:
            "航拍大远景镜头，{movement}。画面覆盖 {scene} 的全貌，展现极致空间尺度与地理纵深。{light} 洒落画面，明暗层次丰富，色彩通透。镜头运动缓慢稳定，节奏庄重，传递宏大、渺小与敬畏的情绪基调，适合作为定场或转场镜头。",
    },
    {
        id: "camera-macro",
        name: "微距特写",
        category: "camera",
        applicable: "both",
        description: "浅景深微距，聚焦细节质感，适合强调道具、眼神、纹理",
        tags: ["特写", "浅景深", "细节"],
        variables: [
            { key: "detail", label: "细节对象", placeholder: "例如：一滴雨珠沿叶脉滑落", required: true },
            { key: "texture", label: "质感关键词", placeholder: "例如：湿润、晶莹、缓慢", required: false },
        ],
        template:
            "微距特写镜头，浅景深极致虚化背景，焦点牢牢锁定 {detail}。{texture} 的细节纤毫毕现，光影在表面流动，画面充满呼吸感与质感张力，营造沉浸、私密或惊悚的凝视感。",
    },
    {
        id: "camera-chase",
        name: "动感追逐",
        category: "camera",
        applicable: "video",
        description: "快速跟拍+手持晃动，适合追逐、奔跑、打斗等强节奏戏份",
        tags: ["跟拍", "手持", "快节奏"],
        variables: [
            { key: "subject", label: "追逐对象", placeholder: "例如：两名黑衣人在楼顶奔跑", required: true },
            { key: "scene", label: "场景", placeholder: "例如：霓虹闪烁的楼顶", required: true },
            { key: "pace", label: "节奏强度", placeholder: "例如：急促、紧张、喘不过气", required: false },
        ],
        template:
            "动感追逐戏。手持跟拍镜头紧贴 {subject}，在 {scene} 中高速穿行，画面轻微晃动制造临场感。{pace} 的节奏由运镜速度与景别切换共同驱动，镜头快速推拉，运动模糊强化速度感，音画同步的急促感贯穿始终。",
    },
    // ============ 视觉风格 ============
    {
        id: "style-noir",
        name: "暗黑童话",
        category: "style",
        applicable: "both",
        description: "低饱和高对比、雾气弥漫、哥特感，适合悬疑/奇幻/暗黑题材",
        tags: ["哥特", "高对比", "低饱和"],
        variables: [
            { key: "subject", label: "主体", placeholder: "例如：穿黑裙的少女提着灯笼", required: true },
            { key: "scene", label: "场景", placeholder: "例如：枯萎的黑森林", required: true },
            { key: "light", label: "光线设计", placeholder: "例如：灯笼暖光与冷蓝月光对冲", required: false },
        ],
        template:
            "暗黑童话视觉风格。{subject} 行走在 {scene} 中，整体低饱和、高对比，阴影浓重而富有层次。{light} 形成冷暖对冲，雾气弥漫营造神秘与不安。画面带有哥特式美感与宿命感，仿佛一帧被遗忘的童话插画。",
    },
    {
        id: "style-cyberpunk",
        name: "赛博朋克夜城",
        category: "style",
        applicable: "both",
        description: "霓虹、雨夜、反射、高密度城市，适合科幻/犯罪题材",
        tags: ["霓虹", "雨夜", "未来城市"],
        variables: [
            { key: "subject", label: "主体", placeholder: "例如：机械义肢的赏金猎人", required: true },
            { key: "scene", label: "场景", placeholder: "例如：摩天大楼间的潮湿巷道", required: true },
            { key: "detail", label: "细节元素", placeholder: "例如：全息广告牌、积水倒影、雾气", required: false },
        ],
        template:
            "赛博朋克夜城。{subject} 穿行于 {scene}，霓虹灯牌在雨夜中晕染，{detail} 铺满画面。青紫与品红的电子色光主导色调，金属与玻璃材质反射高光，空气潮湿带雾气。景深压缩街道纵深，营造繁华而孤独、科技而颓废的未来感。",
    },
    {
        id: "style-ink-wuxia",
        name: "水墨古风",
        category: "style",
        applicable: "both",
        description: "水墨质感、飘逸留白，适合古风/仙侠/武侠题材",
        tags: ["水墨", "留白", "仙侠"],
        variables: [
            { key: "subject", label: "主体", placeholder: "例如：白衣剑客踏竹而行", required: true },
            { key: "scene", label: "场景", placeholder: "例如：云雾缭绕的绝壁山巅", required: true },
            { key: "element", label: "水墨元素", placeholder: "例如：墨色晕染、飞鸟、飘落的桃花", required: false },
        ],
        template:
            "水墨古风视觉。{subject} 置身 {scene}，画面如水墨晕染，浓淡干湿层次分明，大量留白营造意境。{element} 点缀其间，衣袂与云雾飘动缓慢优雅，色调以青灰素白为主，透出东方美学的空灵与侠气。",
    },
    {
        id: "style-sci-fi-hud",
        name: "科技感 HUD",
        category: "style",
        applicable: "video",
        description: "发光线条、数据界面、冷白蓝光，适合科幻装备/实验室/飞船",
        tags: ["科幻", "HUD", "发光"],
        variables: [
            { key: "subject", label: "主体", placeholder: "例如：驾驶舱内的宇航员", required: true },
            { key: "device", label: "科技装置", placeholder: "例如：环绕的全息控制台", required: true },
            { key: "color", label: "主色调", placeholder: "例如：冷白与幽蓝", required: false },
        ],
        template:
            "科技感 HUD 视觉。{subject} 面对 {device}，半透明全息界面悬浮环绕，数据流与发光线条实时流动。{color} 为主色调，金属冷光与屏幕辉光交织，界面投影在面部与舱壁上。镜头平稳，细节精密，营造先进、冷静、未来的科技氛围。",
    },
    // ============ 类型片 ============
    {
        id: "genre-action",
        name: "动作打斗",
        category: "genre",
        applicable: "video",
        description: "快速剪辑感、冲击力、力量爆发，适合打斗/追车/枪战",
        tags: ["打斗", "冲击", "快剪"],
        variables: [
            { key: "fighters", label: "打斗双方", placeholder: "例如：蒙面刺客与持刀保镖", required: true },
            { key: "scene", label: "场景", placeholder: "例如：昏暗的地下拳场", required: true },
            { key: "style", label: "动作风格", placeholder: "例如：凌厉、凶狠、拳拳到肉", required: false },
        ],
        template:
            "高燃动作打斗。{fighters} 在 {scene} 中激烈交锋，动作凌厉凶狠、拳拳到肉。镜头以快节奏剪辑感呈现，运动镜头紧随发力瞬间，慢动作与实时速度交替突出力量冲击。尘埃与汗珠飞溅，光影切割画面，压迫感与肾上腺素感拉满。",
    },
    {
        id: "genre-horror",
        name: "悬疑惊悚",
        category: "genre",
        applicable: "both",
        description: "压迫光影、不安构图、悬念氛围，适合恐怖/悬疑/犯罪",
        tags: ["惊悚", "压迫", "悬念"],
        variables: [
            { key: "subject", label: "主体", placeholder: "例如：独自搜索旧宅的女人", required: true },
            { key: "scene", label: "场景", placeholder: "例如：走廊尽头半掩的门", required: true },
            { key: "tension", label: "惊悚元素", placeholder: "例如：忽明忽暗的灯、门后阴影、镜中倒影", required: false },
        ],
        template:
            "悬疑惊悚氛围。{subject} 身处 {scene}，画面运用大量阴影与低光环境，光源闪烁不定。{tension} 不断制造不安，构图刻意留出画面外的未知空间。镜头缓慢、克制，呼吸声般的节奏压迫感持续累积，让观众始终预感危险将至。",
    },
    {
        id: "genre-romance",
        name: "浪漫氛围",
        category: "genre",
        applicable: "both",
        description: "柔光暖调、慢镜、情绪流动，适合爱情/治愈/回忆戏",
        tags: ["浪漫", "柔光", "慢镜"],
        variables: [
            { key: "couple", label: "人物", placeholder: "例如：在雨中相拥的恋人", required: true },
            { key: "scene", label: "场景", placeholder: "例如：黄昏的海边灯塔", required: true },
            { key: "detail", label: "情绪细节", placeholder: "例如：风吹起的发丝、泛光的海面、眼神交汇", required: false },
        ],
        template:
            "浪漫氛围镜头。{couple} 在 {scene} 中，柔光与暖色调笼罩画面，光线边缘柔和如梦境。{detail} 强化情绪流动，镜头缓慢推进，慢动作放大瞬间的情感浓度。画面温暖、克制而充满张力，带出怦然心动或追忆往昔的抒情质感。",
    },
    {
        id: "genre-war-epic",
        name: "史诗战争",
        category: "genre",
        applicable: "both",
        description: "宏大场面、尘土烽烟、悲壮基调，适合战争/灾难/史诗",
        tags: ["史诗", "战争", "悲壮"],
        variables: [
            { key: "armies", label: "对峙双方", placeholder: "例如：铁甲军团与山城守军", required: true },
            { key: "scene", label: "战场", placeholder: "例如：硝烟弥漫的平原要塞", required: true },
            { key: "tone", label: "基调", placeholder: "例如：悲壮、肃杀、末路", required: false },
        ],
        template:
            "史诗战争大场面。{armies} 在 {scene} 中对峙冲锋，尘土与烽烟遮蔽天日，旗帜猎猎。镜头从高空俯瞰切换至贴地跟拍，展现战场的浩瀚与个体的渺小。光线穿透烟尘形成丁达尔效应，整体色调苍凉，{tone} 的基调贯穿，节奏沉重大气。",
    },
    // ============ 氛围情绪 ============
    {
        id: "mood-dream",
        name: "梦境虚幻",
        category: "mood",
        applicable: "both",
        description: "超现实、漂浮感、光影迷离，适合梦境/回忆/意识流",
        tags: ["超现实", "漂浮", "迷离"],
        variables: [
            { key: "subject", label: "主体", placeholder: "例如：悬浮在空中的少年", required: true },
            { key: "scene", label: "场景", placeholder: "例如：颠倒漂浮的旧图书馆", required: true },
            { key: "effect", label: "虚幻效果", placeholder: "例如：粒子光尘、物体失重、时间凝滞", required: false },
        ],
        template:
            "梦境虚幻氛围。{subject} 漂浮于 {scene}，物理规则被打破，{effect} 弥漫画面。光线柔和而迷离，边缘泛着微光，时间仿佛凝滞。镜头缓慢漂移，画面如记忆般模糊又清晰，情绪似梦似醒，带有超现实主义的诗意与不安。",
    },
    {
        id: "mood-lonely",
        name: "孤独疏离",
        category: "mood",
        applicable: "both",
        description: "大环境小人物的构图、冷调、留白，适合孤独/疏离/都市感",
        tags: ["孤独", "留白", "冷调"],
        variables: [
            { key: "subject", label: "主体", placeholder: "例如：独自坐在长椅上的老人", required: true },
            { key: "scene", label: "场景", placeholder: "例如：空旷的站台", required: true },
            { key: "weather", label: "环境细节", placeholder: "例如：细雨中、孤灯下、落叶飘过", required: false },
        ],
        template:
            "孤独疏离氛围。{subject} 被放置在 {scene} 的广阔空间里，人物占比极小，环境大面积留白。{weather} 加重清冷感，冷色调主导，光线平淡而真实。镜头静止或极缓移动，构图强化个体与环境的巨大落差，情绪克制、沉默、疏离。",
    },
];

/**
 * 内置智能体模板（MVP：2 个，参考即梦 Agent 技能形态）
 * 选中后按 systemPrompt 方法论多步引导生成专业提示词
 */
export const PROMPT_AGENT_TEMPLATES: PromptAgentTemplate[] = [
    {
        id: "agent-short-drama-director",
        name: "叙事短片导演分镜",
        description: "像导演一样拆解镜头：钩子、节奏、视听与反转，输出可执行的分镜视频提示词",
        applicable: "video",
        icon: "🎬",
        prompts: ["补充一句话剧情或题材，我会按导演方法论为你拆解成专业视频提示词"],
        systemPrompt:
            "你是一位资深的叙事短片导演，擅长将剧情意图转化为可直接用于视频生成模型（Seedance 类）的高质量镜头提示词。\n" +
            "方法论（严格遵守）：\n" +
            "1. 先拆解镜头任务：明确这个镜头的叙事目的（建立/推进/揭示/强调/过渡/情绪）。\n" +
            "2. 提示词结构按序组织：主体与动作 → 场景环境 → 光线与氛围 → 镜头运动（运镜方向+景别）→ 视觉风格 → 节奏与时长感。\n" +
            "3. 悬念与反转：若镜头包含信息揭示，写明'观众知道什么、角色不知道什么'；反转要具体到画面元素。\n" +
            "4. 语言要求：使用中文，画面描述具体、可拍摄（避免抽象词汇），一个镜头只表达一个核心动作，不要跨场景。\n" +
            "5. 输出格式：直接输出一段 80~150 字的专业视频提示词，不要解释过程。",
    },
    {
        id: "agent-master-style",
        name: "名导风格大师",
        description: "把任意题材套用指定导演的视听语言风格，生成风格化视频提示词",
        applicable: "video",
        icon: "🎞️",
        prompts: ["输入你的题材或画面想法，我会用指定导演的风格重新演绎成视频提示词"],
        systemPrompt:
            "你是一位深谙电影视听语言的风格化专家，能够将任意题材改写成特定导演风格的视频生成提示词。\n" +
            "导演风格库（用户提及或默认选择其一）：\n" +
            "- 王家卫：抽帧感、慢门拖影、浓烈色彩、都市孤独、暧昧光影\n" +
            "- 克里斯托弗·诺兰：IMAX 实拍感、冷峻、宏大空间、时间叙事、高对比\n" +
            "- 韦斯·安德森：对称构图、糖果色、平面感、居中调度、轻快节奏\n" +
            "- 宫崎骏：手绘质感、通透天空、细腻自然、温暖治愈\n" +
            "- 姜文：粗粝胶片、浓烈红色、男性荷尔蒙、荒诞张力\n" +
            "方法：\n" +
            "1. 理解用户题材与画面意图。\n" +
            "2. 用指定导演的标志性视听手法（运镜、构图、色彩、光线、节奏）重构画面描述。\n" +
            "3. 输出 80~150 字中文专业视频提示词，直接可用，不解释。",
    },
];

/**
 * 渲染模板：{key} 替换为变量值，未填变量留空或替换为空
 */
export function renderPromptTemplate(template: PromptTemplate, values: Record<string, string>): string {
    let result = template.template;
    for (const v of template.variables) {
        result = result.split(`{${v.key}}`).join((values[v.key] || "").trim());
    }
    return result.replace(/\s+/g, " ").trim();
}

/** 变量默认值（预填示例，方便快速出效果） */
export function templateDefaultValues(template: PromptTemplate): Record<string, string> {
    const values: Record<string, string> = {};
    for (const v of template.variables) {
        values[v.key] = "";
    }
    return values;
}
