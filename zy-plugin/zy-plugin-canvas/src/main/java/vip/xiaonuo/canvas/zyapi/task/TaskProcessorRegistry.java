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

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务处理器注册表
 *
 * @author Your Name
 * @date 2026/09/06
 **/
@Component
public class TaskProcessorRegistry {

    private final Map<String, TaskProcessor> processorMap = new ConcurrentHashMap<>();

    @Resource
    public void setProcessors(List<TaskProcessor> processors) {
        for (TaskProcessor processor : processors) {
            register(processor);
        }
    }

    /**
     * 注册任务处理器
     *
     * @param processor 任务处理器
     */
    public void register(TaskProcessor processor) {
        processorMap.put(processor.getTaskType(), processor);
    }

    /**
     * 获取任务处理器
     * 先按注册 key 精确匹配，未命中时遍历所有处理器用 supports 兜底（支持一个处理器声明多个任务类型）
     *
     * @param taskType 任务类型
     * @return 任务处理器
     */
    public TaskProcessor getProcessor(String taskType) {
        TaskProcessor exact = processorMap.get(taskType);
        if (exact != null) return exact;
        for (TaskProcessor processor : processorMap.values()) {
            if (processor.supports(taskType)) return processor;
        }
        return null;
    }

    /**
     * 判断是否支持该任务类型
     *
     * @param taskType 任务类型
     * @return 是否支持
     */
    public boolean hasProcessor(String taskType) {
        if (processorMap.containsKey(taskType)) return true;
        for (TaskProcessor processor : processorMap.values()) {
            if (processor.supports(taskType)) return true;
        }
        return false;
    }

    /**
     * 获取所有支持的任务类型
     *
     * @return 任务类型列表
     */
    public java.util.Set<String> getSupportedTaskTypes() {
        return processorMap.keySet();
    }
}