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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import vip.xiaonuo.canvas.modular.asset.entity.ZyAsset;
import vip.xiaonuo.canvas.modular.asset.mapper.ZyAssetMapper;
import vip.xiaonuo.canvas.zyapi.service.ZyApiResourceService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 资源 Service 实现类
 *
 * @author xuyuxiang
 * @date 2026/9/5 23:30
 **/
@Service
public class ZyApiResourceServiceImpl implements ZyApiResourceService {

    @Resource
    private ZyAssetMapper zyAssetMapper;

    @Override
    public Map<String, Object> getStorageUsage(String userId) {
        // 获取用户所有项目的资产
        List<ZyAsset> allAssets = zyAssetMapper.selectList(
            new QueryWrapper<ZyAsset>().isNotNull("project_id")
        );

        // 计算已用空间（暂时使用一个默认值，实际需要从资源表或文件大小计算）
        long usedBytes = 0L;
        int resourceCount = 0;

        // TODO: 实际项目中需要从资源表中统计文件大小
        // 这里暂时返回默认值

        // 总容量：100GB (100 * 1024 * 1024 * 1024 bytes)
        long totalBytes = 100L * 1024 * 1024 * 1024;

        Map<String, Object> usage = new LinkedHashMap<>();
        usage.put("totalBytes", totalBytes);
        usage.put("usedBytes", usedBytes);
        usage.put("resourceCount", resourceCount);
        usage.put("lastCalculatedAt", System.currentTimeMillis());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("usage", usage);

        return result;
    }
}