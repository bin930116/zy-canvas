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
import vip.xiaonuo.canvas.modular.share.entity.ZyShare;
import vip.xiaonuo.canvas.zyapi.result.ZyApiShareStatusResult;

import java.util.Map;

/**
 * 画布项目分享Service接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 18:30
 **/
public interface ZyApiShareService extends IService<ZyShare> {

    /**
     * 获取项目分享状态
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @return 分享状态
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     */
    ZyApiShareStatusResult getStatus(String userId, String projectId);

    /**
     * 创建(或轮换)分享
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param expiresDays 有效天数
     * @param rotate    是否轮换token
     * @return 分享状态
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     */
    ZyApiShareStatusResult create(String userId, String projectId, Integer expiresDays, Boolean rotate);

    /**
     * 删除分享
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     */
    void delete(String userId, String projectId);

    /**
     * 按token获取公开分享(含项目数据)
     *
     * @param token 分享token
     * @return { project, expiresAt }
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     */
    Map<String, Object> getPublicByToken(String token);
}
