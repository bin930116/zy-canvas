package vip.xiaonuo.canvas.zyapi.service;

import java.util.List;
import java.util.Map;

/**
 * 短剧项目工作流Service
 *
 * @author hanbin
 * @date 2026/09/09 16:50
 **/
public interface ZyApiWorkflowService {

    /**
     * 创建（或复用）章节工作流
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param unitId    章节id
     * @return 工作流对象
     * @author hanbin
     * @date 2026/09/09 16:50
     */
    Map<String, Object> createWorkflow(String userId, String projectId, String unitId);

    /**
     * 获取项目下所有工作流
     *
     * @param projectId 项目id
     * @return 工作流列表
     * @author hanbin
     * @date 2026/09/09 16:50
     */
    List<Map<String, Object>> listWorkflowsByProject(String projectId);
}
