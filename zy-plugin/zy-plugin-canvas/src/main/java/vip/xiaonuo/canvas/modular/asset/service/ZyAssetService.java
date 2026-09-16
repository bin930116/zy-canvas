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
package vip.xiaonuo.canvas.modular.asset.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import vip.xiaonuo.canvas.modular.asset.entity.ZyAsset;
import vip.xiaonuo.canvas.modular.asset.param.ZyAssetAddParam;
import vip.xiaonuo.canvas.modular.asset.param.ZyAssetEditParam;
import vip.xiaonuo.canvas.modular.asset.param.ZyAssetIdParam;
import vip.xiaonuo.canvas.modular.asset.param.ZyAssetPageParam;
import java.io.Serializable;
import java.io.IOException;
import java.util.List;

/**
 * 短剧项目资产Service接口
 *
 * @author hanbin
 * @date  2026/09/08 14:46
 **/
public interface ZyAssetService extends IService<ZyAsset> {

    /**
     * 获取短剧项目资产分页
     *
     * @author hanbin
     * @date  2026/09/08 14:46
     */
    Page<ZyAsset> page(ZyAssetPageParam zyAssetPageParam);

    /**
     * 添加短剧项目资产
     *
     * @author hanbin
     * @date  2026/09/08 14:46
     */
    void add(ZyAssetAddParam zyAssetAddParam);

    /**
     * 编辑短剧项目资产
     *
     * @author hanbin
     * @date  2026/09/08 14:46
     */
    void edit(ZyAssetEditParam zyAssetEditParam);

    /**
     * 删除短剧项目资产
     *
     * @author hanbin
     * @date  2026/09/08 14:46
     */
    void delete(List<ZyAssetIdParam> zyAssetIdParamList);

    /**
     * 获取短剧项目资产详情
     *
     * @author hanbin
     * @date  2026/09/08 14:46
     */
    ZyAsset detail(ZyAssetIdParam zyAssetIdParam);

    /**
     * 获取短剧项目资产详情
     *
     * @author hanbin
     * @date  2026/09/08 14:46
     **/
    ZyAsset queryEntity(Serializable id);

    /**
     * 下载短剧项目资产导入模板
     *
     * @author hanbin
     * @date  2026/09/08 14:46
     */
    void downloadImportTemplate(HttpServletResponse response) throws IOException;

    /**
     * 导入短剧项目资产
     *
     * @author hanbin
     * @date  2026/09/08 14:46
     **/
    JSONObject importData(MultipartFile file);

    /**
     * 导出短剧项目资产
     *
     * @author hanbin
     * @date  2026/09/08 14:46
     */
    void exportData(List<ZyAssetIdParam> zyAssetIdParamList, HttpServletResponse response) throws IOException;
}
