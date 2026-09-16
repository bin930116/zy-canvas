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
import vip.xiaonuo.canvas.modular.asset.entity.ZyAsset;
import vip.xiaonuo.canvas.modular.project.entity.ZyProject;
import vip.xiaonuo.canvas.zyapi.param.ZyApiAssetCandidateParam;
import vip.xiaonuo.canvas.zyapi.param.ZyApiAssetFolderParam;
import vip.xiaonuo.canvas.zyapi.param.ZyApiAssetParam;

import java.util.List;
import java.util.Map;

/**
 * 短剧项目资产Service接口
 *
 * @author xuyuxiang
 * @date 2026/9/5 21:30
 **/
public interface ZyApiAssetService {

    /**
     * 资产列表(带分页/过滤；无分页参数时返回全部)
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param page      页码
     * @param pageSize  每页条数
     * @param category  分类过滤
     * @param mediaType 介质过滤
     * @param status    状态过滤
     * @param folderId  文件夹过滤
     * @param q         标题关键字
     * @return { assets, categoryCounts, folderCounts, page?, pageSize?, total?, hasMore? }
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     */
    Map<String, Object> listAssets(String userId, String projectId, Integer page, Integer pageSize,
                                   String category, String mediaType, String status, String folderId, String q);

    /**
     * 新建资产
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param param     参数
     * @return { asset }
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     */
    Map<String, Object> createAsset(String userId, String projectId, ZyApiAssetParam param);

    /**
     * 更新资产(category/folderId等)
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param assetId   资产id
     * @param param     参数
     * @return { asset }
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     */
    Map<String, Object> updateAsset(String userId, String projectId, String assetId, ZyApiAssetParam param);

    /**
     * 删除资产
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param assetId   资产id
     * @return { id }
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     */
    Map<String, Object> deleteAsset(String userId, String projectId, String assetId);

    /**
     * 资产文件夹列表
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @return { folders }
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     */
    Map<String, Object> listFolders(String userId, String projectId);

    /**
     * 新建资产文件夹
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param param     参数
     * @return { folder }
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     */
    Map<String, Object> createFolder(String userId, String projectId, ZyApiAssetFolderParam param);

    /**
     * 更新资产文件夹
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param folderId  文件夹id
     * @param param     参数
     * @return { folder }
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     */
    Map<String, Object> updateFolder(String userId, String projectId, String folderId, ZyApiAssetFolderParam param);

    /**
     * 删除资产文件夹
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param folderId  文件夹id
     * @return { id }
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     */
    Map<String, Object> deleteFolder(String userId, String projectId, String folderId);

    /**
     * 资产加版本(暂仅递增版本数)
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param assetId   资产id
     * @return { version }
     * @author xuyuxiang
     * @date 2026/9/5 21:30
     */
    Map<String, Object> createVersion(String userId, String projectId, String assetId);

    /**
     * 资产候选列表(带分页/过滤)
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param page      页码
     * @param pageSize  每页条数
     * @param unitId    章节/剧集id过滤
     * @param status    状态过滤
     * @param category  分类过滤
     * @param query     名称关键字
     * @return { candidates, page, pageSize, total, hasMore }
     * @author xuyuxiang
     * @date 2026/9/5 22:00
     */
    Map<String, Object> listAssetCandidates(String userId, String projectId, Integer page, Integer pageSize,
                                            String unitId, String status, String category, String query);

    /**
     * 创建资产候选
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param params    候选参数列表
     * @param source    来源
     * @return { candidates }
     * @author xuyuxiang
     * @date 2026/9/5 22:00
     */
    Map<String, Object> createAssetCandidates(String userId, String projectId,
                                              java.util.List<ZyApiAssetCandidateParam> params, String source);

    /**
     * 确认资产候选
     *
     * @param userId      用户id
     * @param projectId   项目id
     * @param candidateId 候选id
     * @param assetId     目标资产id（可选，不传则创建新资产）
     * @return { asset }
     * @author xuyuxiang
     * @date 2026/9/5 22:00
     */
    Map<String, Object> confirmAssetCandidate(String userId, String projectId, String candidateId, String assetId);

    /**
     * 存储媒体数据
     *
     * @param userId    用户id
     * @param data      媒体数据
     * @param mediaType 媒体类型：image, video, audio
     * @param mimeType  MIME类型
     * @return 资产实体
     */
    ZyAsset storeMedia(String userId, String projectId, byte[] data, String mediaType, String mimeType);

    /**
     * 存储媒体文件（流式上传，避免大文件全量入内存）
     *
     * @param userId    用户ID（可为空，为空时走系统填充）
     * @param projectId 项目ID（可为空，为空时落个人素材库）
     * @param file      本地临时文件（调用方负责删除）
     * @param mediaType 媒体类型（image/video/audio）
     * @param mimeType  MIME类型（可为空，为空时按 mediaType 兜底并做魔数嗅探）
     * @return 资产实体
     */
    ZyAsset storeMediaFile(String userId, String projectId, java.io.File file, String mediaType, String mimeType);

    /**
     * 存储Base64数据
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @param dataUrl   Base64数据URL
     * @param mediaType 媒体类型
     * @return 资产实体
     */
    ZyAsset storeBase64(String userId, String projectId, String dataUrl, String mediaType);
}
