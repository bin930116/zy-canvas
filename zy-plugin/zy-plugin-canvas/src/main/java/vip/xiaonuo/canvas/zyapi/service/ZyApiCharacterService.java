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
 * 短剧项目角色Service接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 21:00
 **/
public interface ZyApiCharacterService {

    /**
     * 创建角色
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param name      角色名称
     * @param definition 角色定义
     * @return { asset, character }
     * @author xuyuxiang
     * @date 2026/9/5 21:00
     */
    Map<String, Object> create(String userId, String projectId, String name, Map<String, Object> definition);

    /**
     * 获取角色
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param assetId   角色id
     * @return { asset, character }
     * @author xuyuxiang
     * @date 2026/9/5 21:00
     */
    Map<String, Object> get(String userId, String projectId, String assetId);

    /**
     * 更新角色
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param assetId   角色id
     * @param name      角色名称
     * @param definition 角色定义
     * @return { asset, character }
     * @author xuyuxiang
     * @date 2026/9/5 21:00
     */
    Map<String, Object> update(String userId, String projectId, String assetId, String name, Map<String, Object> definition);

    /**
     * 更新角色形象(representations)
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param assetId   角色id
     * @param representations 形象列表
     * @return { asset, character }
     * @author xuyuxiang
     * @date 2026/9/5 21:00
     */
    Map<String, Object> putRepresentations(String userId, String projectId, String assetId, List<Map<String, Object>> representations);

    /**
     * 更新角色音色(voice)
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param assetId   角色id
     * @param voice     音色数据
     * @return { asset, character }
     * @author xuyuxiang
     * @date 2026/9/5 21:00
     */
    Map<String, Object> putVoice(String userId, String projectId, String assetId, Map<String, Object> voice);

    /**
     * 删除角色音色
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param assetId   角色id
     * @return { asset, character }
     * @author xuyuxiang
     * @date 2026/9/5 21:00
     */
    Map<String, Object> deleteVoice(String userId, String projectId, String assetId);
}
