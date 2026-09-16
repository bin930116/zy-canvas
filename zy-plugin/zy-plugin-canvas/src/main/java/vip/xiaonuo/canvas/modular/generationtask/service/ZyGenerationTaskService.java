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
package vip.xiaonuo.canvas.modular.generationtask.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import vip.xiaonuo.canvas.modular.generationtask.entity.ZyGenerationTask;
import vip.xiaonuo.canvas.modular.generationtask.param.ZyGenerationTaskAddParam;
import vip.xiaonuo.canvas.modular.generationtask.param.ZyGenerationTaskEditParam;
import vip.xiaonuo.canvas.modular.generationtask.param.ZyGenerationTaskIdParam;
import vip.xiaonuo.canvas.modular.generationtask.param.ZyGenerationTaskPageParam;
import java.io.Serializable;
import java.io.IOException;
import java.util.List;

/**
 * 任务队列信息Service接口
 *
 * @author hanbin
 * @date  2026/09/08 14:24
 **/
public interface ZyGenerationTaskService extends IService<ZyGenerationTask> {

    /**
     * 获取任务队列信息分页
     *
     * @author hanbin
     * @date  2026/09/08 14:24
     */
    Page<ZyGenerationTask> page(ZyGenerationTaskPageParam zyGenerationTaskPageParam);

    /**
     * 添加任务队列信息
     *
     * @author hanbin
     * @date  2026/09/08 14:24
     */
    void add(ZyGenerationTaskAddParam zyGenerationTaskAddParam);

    /**
     * 编辑任务队列信息
     *
     * @author hanbin
     * @date  2026/09/08 14:24
     */
    void edit(ZyGenerationTaskEditParam zyGenerationTaskEditParam);

    /**
     * 删除任务队列信息
     *
     * @author hanbin
     * @date  2026/09/08 14:24
     */
    void delete(List<ZyGenerationTaskIdParam> zyGenerationTaskIdParamList);

    /**
     * 获取任务队列信息详情
     *
     * @author hanbin
     * @date  2026/09/08 14:24
     */
    ZyGenerationTask detail(ZyGenerationTaskIdParam zyGenerationTaskIdParam);

    /**
     * 获取任务队列信息详情
     *
     * @author hanbin
     * @date  2026/09/08 14:24
     **/
    ZyGenerationTask queryEntity(Serializable id);

    /**
     * 下载任务队列信息导入模板
     *
     * @author hanbin
     * @date  2026/09/08 14:24
     */
    void downloadImportTemplate(HttpServletResponse response) throws IOException;

    /**
     * 导入任务队列信息
     *
     * @author hanbin
     * @date  2026/09/08 14:24
     **/
    JSONObject importData(MultipartFile file);

    /**
     * 导出任务队列信息
     *
     * @author hanbin
     * @date  2026/09/08 14:24
     */
    void exportData(List<ZyGenerationTaskIdParam> zyGenerationTaskIdParamList, HttpServletResponse response) throws IOException;
}
