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
package vip.xiaonuo.canvas.modular.assetcandidate.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import vip.xiaonuo.canvas.modular.assetcandidate.entity.ZyAssetCandidate;
import vip.xiaonuo.canvas.modular.assetcandidate.param.ZyAssetCandidateAddParam;
import vip.xiaonuo.canvas.modular.assetcandidate.param.ZyAssetCandidateEditParam;
import vip.xiaonuo.canvas.modular.assetcandidate.param.ZyAssetCandidateIdParam;
import vip.xiaonuo.canvas.modular.assetcandidate.param.ZyAssetCandidatePageParam;
import java.io.Serializable;
import java.io.IOException;
import java.util.List;

/**
 * 短剧项目资产候选Service接口
 *
 * @author hanbin
 * @date  2026/09/08 14:40
 **/
public interface ZyAssetCandidateService extends IService<ZyAssetCandidate> {

    /**
     * 获取短剧项目资产候选分页
     *
     * @author hanbin
     * @date  2026/09/08 14:40
     */
    Page<ZyAssetCandidate> page(ZyAssetCandidatePageParam zyAssetCandidatePageParam);

    /**
     * 添加短剧项目资产候选
     *
     * @author hanbin
     * @date  2026/09/08 14:40
     */
    void add(ZyAssetCandidateAddParam zyAssetCandidateAddParam);

    /**
     * 编辑短剧项目资产候选
     *
     * @author hanbin
     * @date  2026/09/08 14:40
     */
    void edit(ZyAssetCandidateEditParam zyAssetCandidateEditParam);

    /**
     * 删除短剧项目资产候选
     *
     * @author hanbin
     * @date  2026/09/08 14:40
     */
    void delete(List<ZyAssetCandidateIdParam> zyAssetCandidateIdParamList);

    /**
     * 获取短剧项目资产候选详情
     *
     * @author hanbin
     * @date  2026/09/08 14:40
     */
    ZyAssetCandidate detail(ZyAssetCandidateIdParam zyAssetCandidateIdParam);

    /**
     * 获取短剧项目资产候选详情
     *
     * @author hanbin
     * @date  2026/09/08 14:40
     **/
    ZyAssetCandidate queryEntity(Serializable id);

    /**
     * 下载短剧项目资产候选导入模板
     *
     * @author hanbin
     * @date  2026/09/08 14:40
     */
    void downloadImportTemplate(HttpServletResponse response) throws IOException;

    /**
     * 导入短剧项目资产候选
     *
     * @author hanbin
     * @date  2026/09/08 14:40
     **/
    JSONObject importData(MultipartFile file);

    /**
     * 导出短剧项目资产候选
     *
     * @author hanbin
     * @date  2026/09/08 14:40
     */
    void exportData(List<ZyAssetCandidateIdParam> zyAssetCandidateIdParamList, HttpServletResponse response) throws IOException;
}
