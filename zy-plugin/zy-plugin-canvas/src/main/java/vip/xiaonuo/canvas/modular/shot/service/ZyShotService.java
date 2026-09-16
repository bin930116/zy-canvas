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
package vip.xiaonuo.canvas.modular.shot.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import vip.xiaonuo.canvas.modular.shot.entity.ZyShot;
import vip.xiaonuo.canvas.modular.shot.param.ZyShotAddParam;
import vip.xiaonuo.canvas.modular.shot.param.ZyShotEditParam;
import vip.xiaonuo.canvas.modular.shot.param.ZyShotIdParam;
import vip.xiaonuo.canvas.modular.shot.param.ZyShotPageParam;
import java.io.Serializable;
import java.io.IOException;
import java.util.List;

/**
 * 短剧项目分镜Service接口
 *
 * @author hanbin
 * @date  2026/09/07 18:55
 **/
public interface ZyShotService extends IService<ZyShot> {

    /**
     * 获取短剧项目分镜分页
     *
     * @author hanbin
     * @date  2026/09/07 18:55
     */
    Page<ZyShot> page(ZyShotPageParam zyShotPageParam);

    /**
     * 添加短剧项目分镜
     *
     * @author hanbin
     * @date  2026/09/07 18:55
     */
    void add(ZyShotAddParam zyShotAddParam);

    /**
     * 编辑短剧项目分镜
     *
     * @author hanbin
     * @date  2026/09/07 18:55
     */
    void edit(ZyShotEditParam zyShotEditParam);

    /**
     * 删除短剧项目分镜
     *
     * @author hanbin
     * @date  2026/09/07 18:55
     */
    void delete(List<ZyShotIdParam> zyShotIdParamList);

    /**
     * 获取短剧项目分镜详情
     *
     * @author hanbin
     * @date  2026/09/07 18:55
     */
    ZyShot detail(ZyShotIdParam zyShotIdParam);

    /**
     * 获取短剧项目分镜详情
     *
     * @author hanbin
     * @date  2026/09/07 18:55
     **/
    ZyShot queryEntity(Serializable id);

    /**
     * 下载短剧项目分镜导入模板
     *
     * @author hanbin
     * @date  2026/09/07 18:55
     */
    void downloadImportTemplate(HttpServletResponse response) throws IOException;

    /**
     * 导入短剧项目分镜
     *
     * @author hanbin
     * @date  2026/09/07 18:55
     **/
    JSONObject importData(MultipartFile file);

    /**
     * 导出短剧项目分镜
     *
     * @author hanbin
     * @date  2026/09/07 18:55
     */
    void exportData(List<ZyShotIdParam> zyShotIdParamList, HttpServletResponse response) throws IOException;
}
