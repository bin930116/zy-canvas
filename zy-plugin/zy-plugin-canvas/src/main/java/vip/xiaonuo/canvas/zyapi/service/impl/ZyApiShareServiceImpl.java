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
package vip.xiaonuo.canvas.zyapi.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import vip.xiaonuo.canvas.modular.project.entity.ZyProject;
import vip.xiaonuo.canvas.modular.share.entity.ZyShare;
import vip.xiaonuo.canvas.modular.share.mapper.ZyShareMapper;
import vip.xiaonuo.canvas.zyapi.result.ZyApiShareStatusResult;
import vip.xiaonuo.canvas.zyapi.service.ZyApiProjectService;
import vip.xiaonuo.canvas.zyapi.service.ZyApiShareService;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 画布项目分享Service实现
 *
 * @author xuyuxiang
 * @date 2026/9/5 18:30
 **/
@Service
public class ZyApiShareServiceImpl extends ServiceImpl<ZyShareMapper, ZyShare> implements ZyApiShareService {

    @Resource
    private ZyShareMapper zyShareMapper;

    @Resource
    private ZyApiProjectService zyProjectService;

    @Override
    public ZyApiShareStatusResult getStatus(String userId, String projectId) {
        ZyShare share = findLatestByProjectId(projectId);
        return toStatus(share);
    }

    @Override
    public ZyApiShareStatusResult create(String userId, String projectId, Integer expiresDays, Boolean rotate) {
        ZyShare share = findLatestByProjectId(projectId);
        Date expiresAt = null;
        if (ObjectUtil.isNotEmpty(expiresDays)) {
            expiresAt = DateUtil.offsetDay(new Date(), expiresDays);
        }
        String token;
        if (ObjectUtil.isEmpty(share)) {
            share = new ZyShare();
            share.setId(IdUtil.fastSimpleUUID());
            share.setProjectId(projectId);
            share.setToken(IdUtil.fastSimpleUUID());
            share.setEnabled("true");
            share.setExpiresAt(expiresAt);
            zyShareMapper.insert(share);
        } else {
            token = Boolean.TRUE.equals(rotate) ? IdUtil.fastSimpleUUID() : share.getToken();
            share.setToken(token);
            share.setEnabled("true");
            if (ObjectUtil.isNotEmpty(expiresDays)) {
                share.setExpiresAt(expiresAt);
            }
            zyShareMapper.updateById(share);
        }
        return toStatus(share);
    }

    @Override
    public void delete(String userId, String projectId) {
        zyShareMapper.delete(new QueryWrapper<ZyShare>().eq("project_id", projectId));
    }

    @Override
    public Map<String, Object> getPublicByToken(String token) {
        ZyShare share = zyShareMapper.selectOne(new QueryWrapper<ZyShare>()
                .eq("token", token)
                .eq("enabled", "true")
                .and(w -> w.isNull("expires_at").or().gt("expires_at", new Date())));
        if (ObjectUtil.isEmpty(share)) {
            return null;
        }
        ZyProject project = zyProjectService.getOne(new QueryWrapper<ZyProject>().eq("id", share.getProjectId()));
        if (ObjectUtil.isEmpty(project) || ObjectUtil.isEmpty(project.getDataJson())) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("project", JSONUtil.parseObj(project.getDataJson()));
        result.put("expiresAt", share.getExpiresAt() != null ? DateUtil.formatDateTime(share.getExpiresAt()) : null);
        return result;
    }

    /**
     * 查询项目最新的分享记录
     *
     * @param projectId 项目id
     * @return 分享记录
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     */
    private ZyShare findLatestByProjectId(String projectId) {
        List<ZyShare> list = this.page(new Page<>(1, 1, false), new QueryWrapper<ZyShare>()
                .eq("project_id", projectId)
                .orderByDesc("create_time")).getRecords();
        return ObjectUtil.isEmpty(list) ? null : list.get(0);
    }

    /**
     * 实体转分享状态结果
     *
     * @param share 分享实体
     * @return 分享状态结果
     * @author xuyuxiang
     * @date 2026/9/5 18:30
     */
    private ZyApiShareStatusResult toStatus(ZyShare share) {
        ZyApiShareStatusResult result = new ZyApiShareStatusResult();
        if (ObjectUtil.isEmpty(share)) {
            result.setEnabled(false);
            return result;
        }
        result.setEnabled("true".equals(share.getEnabled()));
        result.setToken(share.getToken());
        result.setExpiresAt(share.getExpiresAt() != null ? DateUtil.formatDateTime(share.getExpiresAt()) : null);
        result.setCreatedAt(share.getCreateTime() != null ? DateUtil.formatDateTime(share.getCreateTime()) : null);
        return result;
    }
}
