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

import java.util.Map;

/**
 * 画布项目 Service 接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 22:30
 **/
public interface ZyApiProjectCanvasService {

    /**
     * 获取项目画布列表
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param page      页码
     * @param pageSize  每页数量
     * @return 画布列表
     * @author xuyuxiang
     * @date 2026/9/5 22:30
     */
    Map<String, Object> listCanvases(String userId, String projectId, Integer page, Integer pageSize);

    /**
     * 关联画布与章节
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param canvasId  画布id
     * @param unitId    章节id
     * @param role      角色
     * @return 关联信息
     * @author xuyuxiang
     * @date 2026/9/5 22:30
     */
    Map<String, Object> linkCanvasUnit(String userId, String projectId, String canvasId, String unitId, String role);

    /**
     * 解除画布与章节的关联
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param canvasId  画布id
     * @param unitId    章节id
     * @author xuyuxiang
     * @date 2026/9/5 22:30
     */
    void unlinkCanvasUnit(String userId, String projectId, String canvasId, String unitId);

    /**
     * 解除画布与项目的关联
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param canvasId  画布id
     * @author xuyuxiang
     * @date 2026/9/5 22:30
     */
    void unlinkCanvasProject(String userId, String projectId, String canvasId);
}