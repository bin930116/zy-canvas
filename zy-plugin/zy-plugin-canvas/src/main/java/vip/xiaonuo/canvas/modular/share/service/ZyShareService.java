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
package vip.xiaonuo.canvas.modular.share.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import vip.xiaonuo.canvas.modular.share.entity.ZyShare;
import vip.xiaonuo.canvas.modular.share.param.ZyShareAddParam;
import vip.xiaonuo.canvas.modular.share.param.ZyShareEditParam;
import vip.xiaonuo.canvas.modular.share.param.ZyShareIdParam;
import vip.xiaonuo.canvas.modular.share.param.ZySharePageParam;
import java.io.Serializable;
import java.io.IOException;
import java.util.List;

/**
 * 画布项目分享Service接口
 *
 * @author hanbin
 * @date  2026/09/07 18:56
 **/
public interface ZyShareService extends IService<ZyShare> {

    /**
     * 获取画布项目分享分页
     *
     * @author hanbin
     * @date  2026/09/07 18:56
     */
    Page<ZyShare> page(ZySharePageParam zySharePageParam);

    /**
     * 添加画布项目分享
     *
     * @author hanbin
     * @date  2026/09/07 18:56
     */
    void add(ZyShareAddParam zyShareAddParam);

    /**
     * 编辑画布项目分享
     *
     * @author hanbin
     * @date  2026/09/07 18:56
     */
    void edit(ZyShareEditParam zyShareEditParam);

    /**
     * 删除画布项目分享
     *
     * @author hanbin
     * @date  2026/09/07 18:56
     */
    void delete(List<ZyShareIdParam> zyShareIdParamList);

    /**
     * 获取画布项目分享详情
     *
     * @author hanbin
     * @date  2026/09/07 18:56
     */
    ZyShare detail(ZyShareIdParam zyShareIdParam);

    /**
     * 获取画布项目分享详情
     *
     * @author hanbin
     * @date  2026/09/07 18:56
     **/
    ZyShare queryEntity(Serializable id);

    /**
     * 下载画布项目分享导入模板
     *
     * @author hanbin
     * @date  2026/09/07 18:56
     */
    void downloadImportTemplate(HttpServletResponse response) throws IOException;

    /**
     * 导入画布项目分享
     *
     * @author hanbin
     * @date  2026/09/07 18:56
     **/
    JSONObject importData(MultipartFile file);

    /**
     * 导出画布项目分享
     *
     * @author hanbin
     * @date  2026/09/07 18:56
     */
    void exportData(List<ZyShareIdParam> zyShareIdParamList, HttpServletResponse response) throws IOException;
}
