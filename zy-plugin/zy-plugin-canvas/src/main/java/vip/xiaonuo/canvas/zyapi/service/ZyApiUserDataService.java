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
 * 平台用户数据Service接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 18:40
 **/
public interface ZyApiUserDataService {

    /**
     * 资产列表(带分页/过滤；无分页参数时返回摘要)
     *
     * @param userId        用户id
     * @param page          页码
     * @param pageSize      每页条数
     * @param kind          介质类型过滤
     * @param category      分类过滤
     * @param folderId      文件夹过滤
     * @param uncategorized 未分类过滤
     * @param status        状态过滤
     * @param query         标题关键字
     * @return { assets, kindCounts?, categoryCounts?, folderCounts?, page?, pageSize?, total?, hasMore? }
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     */
    Map<String, Object> listAssets(String userId, Integer page, Integer pageSize,
                                   String kind, String category, String folderId,
                                   Boolean uncategorized, String status, String query);

    /**
     * 批量获取资产详情
     *
     * @param userId 用户id
     * @param ids    资产id列表
     * @return { assets }
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     */
    Map<String, Object> listAssetsByIds(String userId, List<String> ids);

    /**
     * 获取单个资产详情
     *
     * @param userId  用户id
     * @param assetId 资产id
     * @return { asset }
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     */
    Map<String, Object> getAsset(String userId, String assetId);

    /**
     * 创建或更新资产
     *
     * @param userId 用户id
     * @param asset  资产数据
     * @return { asset }
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     */
    Map<String, Object> upsertAsset(String userId, Object asset);

    /**
     * 删除资产
     *
     * @param userId  用户id
     * @param assetId 资产id
     * @return { id }
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     */
    Map<String, Object> deleteAsset(String userId, String assetId);

    /**
     * 资产文件夹列表
     *
     * @param userId 用户id
     * @return { folders }
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     */
    Map<String, Object> listFolders(String userId);

    /**
     * 新建资产文件夹
     *
     * @param userId 用户id
     * @param name   文件夹名称
     * @return { folder }
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     */
    Map<String, Object> createFolder(String userId, String name);

    /**
     * 更新资产文件夹
     *
     * @param userId   用户id
     * @param folderId 文件夹id
     * @param name     文件夹名称
     * @return { folder }
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     */
    Map<String, Object> updateFolder(String userId, String folderId, String name);

    /**
     * 删除资产文件夹
     *
     * @param userId   用户id
     * @param folderId 文件夹id
     * @return { id }
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     */
    Map<String, Object> deleteFolder(String userId, String folderId);

    /**
     * 移动资产到文件夹（暂未实现）
     *
     * @param userId   用户id
     * @param assetIds 资产id列表
     * @param folderId 文件夹id(空字符串表示移出文件夹)
     * @return { assetIds, folderId }
     * @author xuyuxiang
     * @date 2026/9/5 18:40
     */
    Map<String, Object> moveAssetsToFolder(String userId, List<String> assetIds, String folderId);
}