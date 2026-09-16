/*
 * Copyright [2022] [https://www.xiaonuo.vip]
 *
 * Snowy采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改Snowy源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 https://www.xiaonuo.vip
 * 5.不可二次分发开源参与同类竞品，如有想法可联系团队xiaonuobase@qq.com商议合作。
 * 6.若您的项目无法满足以上几点，需要更多功能代码，获取Snowy商业授权许可，请在官网购买授权，地址为 https://www.xiaonuo.vip
 */
package vip.xiaonuo.canvas.zyapi.service;

import java.util.List;
import java.util.Map;

/**
 * 生成任务 Service 接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 23:00
 **/
public interface ZyApiGenerationTaskService {

    /**
     * 获取生成任务列表
     *
     * @param userId     用户id
     * @param projectId  项目id（可选）
     * @param activeOnly 是否只显示活跃任务
     * @param limit      数量限制
     * @return 任务列表
     * @author xuyuxiang
     * @date 2026/9/5 23:00
     */
    List<Map<String, Object>> listTasks(String userId, String projectId, Boolean activeOnly, Integer limit);

    /**
     * 创建生成任务
     *
     * @param userId 用户id
     * @param input  任务输入
     * @return 任务对象
     * @author xuyuxiang
     * @date 2026/9/5 23:00
     */
    Map<String, Object> createTask(String userId, Map<String, Object> input);

    /**
     * 获取生成任务详情
     *
     * @param userId  用户id
     * @param taskId  任务id
     * @return 任务对象
     * @author xuyuxiang
     * @date 2026/9/5 23:00
     */
    Map<String, Object> getTask(String userId, String taskId);

    /**
     * 重试任务
     *
     * @param userId  用户id
     * @param taskId  任务id
     * @return 任务对象
     * @author xuyuxiang
     * @date 2026/9/5 23:00
     */
    Map<String, Object> retryTask(String userId, String taskId);

    /**
     * 取消任务
     *
     * @param userId  用户id
     * @param taskId  任务id
     * @return 任务对象
     * @author xuyuxiang
     * @date 2026/9/5 23:00
     */
    Map<String, Object> cancelTask(String userId, String taskId);

    /**
     * 查询上游渠道状态（恢复失败任务）
     *
     * @param userId  用户id
     * @param taskId  任务id
     * @return 查询结果（含 task/recovered/providerStatus/billingSettled）
     * @author xuyuxiang
     * @date 2026/9/5 23:00
     */
    Map<String, Object> queryProviderTask(String userId, String taskId);

    /**
     * 更新任务状态
     *
     * @param taskId  任务id
     * @param status  状态
     * @param progress 进度
     * @param error   错误信息
     * @author xuyuxiang
     * @date 2026/9/5 23:00
     */
    void updateTaskStatus(String taskId, String status, Integer progress, String error);
}