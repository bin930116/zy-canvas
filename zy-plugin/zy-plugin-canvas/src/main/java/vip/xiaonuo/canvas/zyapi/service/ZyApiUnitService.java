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
import vip.xiaonuo.canvas.modular.unit.entity.ZyUnit;
import vip.xiaonuo.canvas.zyapi.param.ZyApiUnitParam;

import java.util.List;

/**
 * 短剧项目章节Service接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 19:30
 **/
public interface ZyApiUnitService extends IService<ZyUnit> {

    /**
     * 获取项目章节列表
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @return 章节列表
     * @author xuyuxiang
     * @date 2026/9/5 19:30
     */
    List<ZyUnit> listByProject(String userId, String projectId);

    /**
     * 获取章节详情
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param unitId    章节id
     * @return 章节
     * @author xuyuxiang
     * @date 2026/9/5 19:30
     */
    ZyUnit get(String userId, String projectId, String unitId);

    /**
     * 新建章节
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param param     参数
     * @return 章节
     * @author xuyuxiang
     * @date 2026/9/5 19:30
     */
    ZyUnit create(String userId, String projectId, ZyApiUnitParam param);

    /**
     * 批量导入章节
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param params    章节列表
     * @return 章节列表
     * @author xuyuxiang
     * @date 2026/9/5 19:30
     */
    List<ZyUnit> importUnits(String userId, String projectId, List<ZyApiUnitParam> params);

    /**
     * 更新章节
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param unitId    章节id
     * @param param     参数
     * @return 章节
     * @author xuyuxiang
     * @date 2026/9/5 19:30
     */
    ZyUnit update(String userId, String projectId, String unitId, ZyApiUnitParam param);

    /**
     * 删除章节
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param unitId    章节id
     * @author xuyuxiang
     * @date 2026/9/5 19:30
     */
    void delete(String userId, String projectId, String unitId);

    /**
     * 章节排序
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param unitIds   排序后的章节id列表
     * @return 排序后的章节id列表
     * @author xuyuxiang
     * @date 2026/9/5 19:30
     */
    List<String> reorder(String userId, String projectId, List<String> unitIds);
}
