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
import vip.xiaonuo.canvas.modular.project.entity.ZyProject;
import vip.xiaonuo.canvas.zyapi.result.ZyApiProjectSummaryResult;

import java.util.List;
import java.util.Map;

/**
 * 画布项目Service接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 18:30
 **/
public interface ZyApiProjectService extends IService<ZyProject> {

    /**
     * 获取用户画布项目摘要列表
     *
     * @param userId 用户id
     * @return 摘要列表
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     */
    List<ZyApiProjectSummaryResult> listSummary(String userId);

    /**
     * 按用户与主键获取项目
     *
     * @param userId 用户id
     * @param id     主键
     * @return 项目
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     */
    ZyProject getById(String userId, String id);

    /**
     * 保存(新增或更新)画布项目
     *
     * @param userId  用户id
     * @param id      主键
     * @param project 画布项目完整对象
     * @return 项目
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     */
    ZyProject upsert(String userId, String id, Map<String, Object> project);

    /**
     * 删除画布项目
     *
     * @param userId 用户id
     * @param id     主键
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     */
    void delete(String userId, String id);
}
