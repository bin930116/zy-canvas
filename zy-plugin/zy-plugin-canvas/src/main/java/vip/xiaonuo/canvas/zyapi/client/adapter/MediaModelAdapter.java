package vip.xiaonuo.canvas.zyapi.client.adapter;

import vip.xiaonuo.canvas.modular.model.entity.ZyModel;
import vip.xiaonuo.canvas.zyapi.client.request.NewApiImageRequest;
import vip.xiaonuo.canvas.zyapi.client.request.NewApiVideoRequest;
import vip.xiaonuo.canvas.zyapi.client.response.NewApiStatusResponse;
import vip.xiaonuo.canvas.zyapi.client.response.NewApiTaskResponse;

/**
 * 模型渠道适配器：屏蔽不同模型/网关的协议差异
 * <p>
 * 业务层（任务处理器/轮询服务）只依赖 NewApiClient 门面；
 * 新增一个模型渠道 = 新增一个 Adapter 实现 + 注册为 Spring Bean，业务层零改动。
 * <p>
 * 各适配器内部消化：URL 拼法、参数名（size/resolution/ratio）、鉴权头、
 * 同步/异步、参考图传法（img_urls/media/messages）、任务状态字段等差异。
 *
 * @author Your Name
 * @date 2026/09/09
 **/
public interface MediaModelAdapter {

    /**
     * 是否处理该模型（按 baseUrl 域名 / protocol 判断）
     */
    boolean supports(ZyModel model);

    /**
     * 创建视频生成任务（异步任务返回 provider 侧任务 ID）
     */
    NewApiTaskResponse createVideoTask(ZyModel model, NewApiVideoRequest request);

    /**
     * 创建图片生成任务
     */
    NewApiTaskResponse createImageTask(ZyModel model, NewApiImageRequest request);

    /**
     * 查询上游任务状态
     */
    NewApiStatusResponse getStatus(ZyModel model, String taskId);

    /**
     * 取消任务
     */
    void cancelTask(ZyModel model, String taskId);
}
