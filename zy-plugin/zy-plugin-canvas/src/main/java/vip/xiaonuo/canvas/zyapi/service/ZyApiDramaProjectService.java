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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import vip.xiaonuo.canvas.modular.dramaproject.entity.ZyDramaProject;
import vip.xiaonuo.canvas.zyapi.param.ZyApiDramaProjectParam;

/**
 * 短剧项目Service接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 19:00
 **/
public interface ZyApiDramaProjectService extends IService<ZyDramaProject> {

    /**
     * 分页/列表查询短剧项目
     *
     * @param userId   用户id
     * @param page     页码(为空则取前1000)
     * @param pageSize 每页条数
     * @return 分页结果
     * @author xuyuxiang
     * @date 2026/9/5 19:00
     */
    Page<ZyDramaProject> listPage(String userId, Integer page, Integer pageSize);

    /**
     * 按用户与主键获取项目(包含团队成员权限)
     *
     * @param userId 用户id
     * @param id     主键
     * @return 项目(无权限返回null)
     * @author xuyuxiang
     * @date 2026/9/5 19:00
     */
    ZyDramaProject getById(String userId, String id);

    /**
     * 按用户与主键获取项目(仅限创建者，用于项目管理操作)
     *
     * @param userId 用户id
     * @param id     主键
     * @return 项目(无权限返回null)
     * @author xuyuxiang
     * @date 2026/9/5 19:00
     */
    ZyDramaProject getByIdForOwner(String userId, String id);

    /**
     * 新建短剧项目
     *
     * @param userId 用户id
     * @param param  参数
     * @return 项目
     * @author xuyuxiang
     * @date 2026/9/5 19:00
     */
    ZyDramaProject create(String userId, ZyApiDramaProjectParam param);

    /**
     * 更新短剧项目
     *
     * @param userId 用户id
     * @param id     主键
     * @param param  参数
     * @return 项目
     * @author xuyuxiang
     * @date 2026/9/5 19:00
     */
    ZyDramaProject update(String userId, String id, ZyApiDramaProjectParam param);

    /**
     * 删除短剧项目
     *
     * @param userId 用户id
     * @param id     主键
     * @author xuyuxiang
     * @date 2026/9/5 19:00
     */
    void delete(String userId, String id);

    /**
     * 关联/更换/取消关联团队(teamId为空时取消关联)
     *
     * @param userId   用户id
     * @param id       主键
     * @param teamId   团队id(为空取消关联)
     * @return 更新后的项目(项目不存在返回null)
     */
    ZyDramaProject updateTeam(String userId, String id, String teamId);
}
