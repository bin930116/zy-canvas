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
package vip.xiaonuo.canvas.modular.model.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import vip.xiaonuo.canvas.modular.model.entity.ZyModel;
import vip.xiaonuo.canvas.modular.model.param.ZyModelAddParam;
import vip.xiaonuo.canvas.modular.model.param.ZyModelEditParam;
import vip.xiaonuo.canvas.modular.model.param.ZyModelIdParam;
import vip.xiaonuo.canvas.modular.model.param.ZyModelPageParam;
import java.io.Serializable;
import java.io.IOException;
import java.util.List;

/**
 * 模型配置Service接口
 *
 * @author hanbin
 * @date  2026/09/07 18:27
 **/
public interface ZyModelService extends IService<ZyModel> {

    /**
     * 获取模型配置分页
     *
     * @author hanbin
     * @date  2026/09/07 18:27
     */
    Page<ZyModel> page(ZyModelPageParam zyModelPageParam);

    /**
     * 添加模型配置
     *
     * @author hanbin
     * @date  2026/09/07 18:27
     */
    void add(ZyModelAddParam zyModelAddParam);

    /**
     * 编辑模型配置
     *
     * @author hanbin
     * @date  2026/09/07 18:27
     */
    void edit(ZyModelEditParam zyModelEditParam);

    /**
     * 删除模型配置
     *
     * @author hanbin
     * @date  2026/09/07 18:27
     */
    void delete(List<ZyModelIdParam> zyModelIdParamList);

    /**
     * 获取模型配置详情
     *
     * @author hanbin
     * @date  2026/09/07 18:27
     */
    ZyModel detail(ZyModelIdParam zyModelIdParam);

    /**
     * 获取模型配置详情
     *
     * @author hanbin
     * @date  2026/09/07 18:27
     **/
    ZyModel queryEntity(Serializable id);

    /**
     * 下载模型配置导入模板
     *
     * @author hanbin
     * @date  2026/09/07 18:27
     */
    void downloadImportTemplate(HttpServletResponse response) throws IOException;

    /**
     * 导入模型配置
     *
     * @author hanbin
     * @date  2026/09/07 18:27
     **/
    JSONObject importData(MultipartFile file);

    /**
     * 导出模型配置
     *
     * @author hanbin
     * @date  2026/09/07 18:27
     */
    void exportData(List<ZyModelIdParam> zyModelIdParamList, HttpServletResponse response) throws IOException;
}
