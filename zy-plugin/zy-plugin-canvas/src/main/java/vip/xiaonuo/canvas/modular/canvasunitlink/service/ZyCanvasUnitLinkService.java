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
package vip.xiaonuo.canvas.modular.canvasunitlink.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import vip.xiaonuo.canvas.modular.canvasunitlink.entity.ZyCanvasUnitLink;
import vip.xiaonuo.canvas.modular.canvasunitlink.param.ZyCanvasUnitLinkAddParam;
import vip.xiaonuo.canvas.modular.canvasunitlink.param.ZyCanvasUnitLinkEditParam;
import vip.xiaonuo.canvas.modular.canvasunitlink.param.ZyCanvasUnitLinkIdParam;
import vip.xiaonuo.canvas.modular.canvasunitlink.param.ZyCanvasUnitLinkPageParam;
import java.io.Serializable;
import java.io.IOException;
import java.util.List;

/**
 * 画布与章节关联Service接口
 *
 * @author hanbin
 * @date  2026/09/08 14:31
 **/
public interface ZyCanvasUnitLinkService extends IService<ZyCanvasUnitLink> {

    /**
     * 获取画布与章节关联分页
     *
     * @author hanbin
     * @date  2026/09/08 14:31
     */
    Page<ZyCanvasUnitLink> page(ZyCanvasUnitLinkPageParam zyCanvasUnitLinkPageParam);

    /**
     * 添加画布与章节关联
     *
     * @author hanbin
     * @date  2026/09/08 14:31
     */
    void add(ZyCanvasUnitLinkAddParam zyCanvasUnitLinkAddParam);

    /**
     * 编辑画布与章节关联
     *
     * @author hanbin
     * @date  2026/09/08 14:31
     */
    void edit(ZyCanvasUnitLinkEditParam zyCanvasUnitLinkEditParam);

    /**
     * 删除画布与章节关联
     *
     * @author hanbin
     * @date  2026/09/08 14:31
     */
    void delete(List<ZyCanvasUnitLinkIdParam> zyCanvasUnitLinkIdParamList);

    /**
     * 获取画布与章节关联详情
     *
     * @author hanbin
     * @date  2026/09/08 14:31
     */
    ZyCanvasUnitLink detail(ZyCanvasUnitLinkIdParam zyCanvasUnitLinkIdParam);

    /**
     * 获取画布与章节关联详情
     *
     * @author hanbin
     * @date  2026/09/08 14:31
     **/
    ZyCanvasUnitLink queryEntity(Serializable id);

    /**
     * 下载画布与章节关联导入模板
     *
     * @author hanbin
     * @date  2026/09/08 14:31
     */
    void downloadImportTemplate(HttpServletResponse response) throws IOException;

    /**
     * 导入画布与章节关联
     *
     * @author hanbin
     * @date  2026/09/08 14:31
     **/
    JSONObject importData(MultipartFile file);

    /**
     * 导出画布与章节关联
     *
     * @author hanbin
     * @date  2026/09/08 14:31
     */
    void exportData(List<ZyCanvasUnitLinkIdParam> zyCanvasUnitLinkIdParamList, HttpServletResponse response) throws IOException;
}
