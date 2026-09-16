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
package vip.xiaonuo.canvas.zyapi.task;

import vip.xiaonuo.canvas.modular.generationtask.entity.ZyGenerationTask;

/**
 * 任务处理器接口
 *
 * @author Your Name
 * @date 2026/09/06
 **/
public interface TaskProcessor {

    /**
     * 处理任务
     *
     * @param task 任务实体
     * @throws Exception 处理异常
     */
    void process(ZyGenerationTask task) throws Exception;

    /**
     * 判断是否支持该任务类型
     *
     * @param taskType 任务类型
     * @return 是否支持
     */
    boolean supports(String taskType);

    /**
     * 获取任务类型
     *
     * @return 任务类型
     */
    String getTaskType();
}