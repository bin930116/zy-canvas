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

import com.baomidou.mybatisplus.extension.service.IService;
import vip.xiaonuo.canvas.modular.shot.entity.ZyShot;
import vip.xiaonuo.canvas.modular.shotrevision.entity.ZyShotRevision;
import vip.xiaonuo.canvas.zyapi.param.ZyApiShotParam;
import vip.xiaonuo.canvas.zyapi.param.ZyApiShotRevisionInputParam;

import java.util.List;
import java.util.Map;

/**
 * 短剧项目分镜Service接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 20:00
 **/
public interface ZyApiShotService extends IService<ZyShot> {

    /**
     * 获取章节下的分镜列表
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param unitId    章节id
     * @return 分镜列表
     * @author xuyuxiang
     * @date 2026/9/5 20:00
     */
    List<ZyShot> listByUnit(String userId, String projectId, String unitId);

    /**
     * 保存(新增/更新)分镜
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param param     参数
     * @return 分镜
     * @author xuyuxiang
     * @date 2026/9/5 20:00
     */
    ZyShot save(String userId, String projectId, ZyApiShotParam param);

    /**
     * 删除分镜
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param shotId    分镜id
     * @author xuyuxiang
     * @date 2026/9/5 20:00
     */
    void delete(String userId, String projectId, String shotId);

    /**
     * 按章节批量替换分镜
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param unitId    章节id
     * @param params    分镜列表
     * @return 分镜列表
     * @author xuyuxiang
     * @date 2026/9/5 20:00
     */
    List<ZyShot> replaceByUnit(String userId, String projectId, String unitId, List<ZyApiShotParam> params);

    /**
     * 创建分镜版本
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param shotId    分镜id
     * @param input     版本输入
     * @return { shot, revision }
     * @author xuyuxiang
     * @date 2026/9/5 20:00
     */
    Map<String, Object> createRevision(String userId, String projectId, String shotId, ZyApiShotRevisionInputParam input);

    /**
     * 获取分镜的所有版本
     *
     * @param shotId 分镜id
     * @return 版本列表
     * @author xuyuxiang
     * @date 2026/9/5 20:00
     */
    List<ZyShotRevision> listRevisionsByShot(String shotId);

    /**
     * 绑定资产到镜头
     *
     * @param userId         用户id
     * @param projectId      项目id
     * @param shotId         分镜id
     * @param assetVersionId 资产版本id
     * @param role           引用角色
     * @return 绑定引用
     * @author hanbin
     * @date 2026/09/09 16:20
     */
    Map<String, Object> linkAsset(String userId, String projectId, String shotId, String assetVersionId, String role);

    /**
     * 解绑镜头资产
     *
     * @param userId      用户id
     * @param projectId   项目id
     * @param shotId      分镜id
     * @param referenceId 绑定引用id
     * @return 解绑结果
     * @author hanbin
     * @date 2026/09/09 16:20
     */
    Map<String, Object> unlinkAsset(String userId, String projectId, String shotId, String referenceId);

    /**
     * 获取项目下所有镜头资产绑定
     *
     * @param projectId 项目id
     * @return 绑定引用列表
     * @author hanbin
     * @date 2026/09/09 16:20
     */
    List<Map<String, Object>> listAssetsByProject(String projectId);
}
