package vip.xiaonuo.canvas.zyapi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import vip.xiaonuo.canvas.modular.model.entity.ZyModel;

import java.util.List;
import java.util.Map;

/**
 * 模型配置Service接口
 *
 * @author Your Name
 * @date 2026/09/06
 **/
public interface ZyApiModelService extends IService<ZyModel> {

    /**
     * 获取模型列表（按能力分类）
     *
     * @param capability 能力类型（可选）：text、image、video、audio
     * @return 模型列表
     */
    Map<String, Object> listModels(String capability);

    /**
     * 获取默认模型列表
     *
     * @return 默认模型列表
     */
    Map<String, Object> getDefaultModels();

    /**
     * 根据模型标识获取模型配置
     *
     * @param modelKey 模型标识
     * @return 模型配置
     */
    ZyModel getByModelKey(String modelKey);

    /**
     * 根据能力类型获取可用模型列表
     *
     * @param capability 能力类型
     * @return 模型列表
     */
    List<ZyModel> listByCapability(String capability);

    /**
     * 获取默认模型
     *
     * @param capability 能力类型
     * @return 默认模型
     */
    ZyModel getDefaultModel(String capability);

    /**
     * 获取可用模型列表（用于前端模型选择）
     *
     * @param capability 能力类型（可选）
     * @return 可用模型列表
     */
    List<Map<String, Object>> getAvailableModels(String capability);
}