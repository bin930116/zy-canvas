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
package vip.xiaonuo.canvas.modular.teammember.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import vip.xiaonuo.canvas.modular.teammember.entity.ZyTeamMember;
import vip.xiaonuo.canvas.modular.teammember.param.ZyTeamMemberAddParam;
import vip.xiaonuo.canvas.modular.teammember.param.ZyTeamMemberEditParam;
import vip.xiaonuo.canvas.modular.teammember.param.ZyTeamMemberIdParam;
import vip.xiaonuo.canvas.modular.teammember.param.ZyTeamMemberPageParam;
import java.io.Serializable;
import java.io.IOException;
import java.util.List;

/**
 * 团队成员关系表Service接口
 *
 * @author hanbin
 * @date  2026/09/07 18:44
 **/
public interface ZyTeamMemberService extends IService<ZyTeamMember> {

    /**
     * 获取团队成员关系表分页
     *
     * @author hanbin
     * @date  2026/09/07 18:44
     */
    Page<ZyTeamMember> page(ZyTeamMemberPageParam zyTeamMemberPageParam);

    /**
     * 添加团队成员关系表
     *
     * @author hanbin
     * @date  2026/09/07 18:44
     */
    void add(ZyTeamMemberAddParam zyTeamMemberAddParam);

    /**
     * 编辑团队成员关系表
     *
     * @author hanbin
     * @date  2026/09/07 18:44
     */
    void edit(ZyTeamMemberEditParam zyTeamMemberEditParam);

    /**
     * 删除团队成员关系表
     *
     * @author hanbin
     * @date  2026/09/07 18:44
     */
    void delete(List<ZyTeamMemberIdParam> zyTeamMemberIdParamList);

    /**
     * 获取团队成员关系表详情
     *
     * @author hanbin
     * @date  2026/09/07 18:44
     */
    ZyTeamMember detail(ZyTeamMemberIdParam zyTeamMemberIdParam);

    /**
     * 获取团队成员关系表详情
     *
     * @author hanbin
     * @date  2026/09/07 18:44
     **/
    ZyTeamMember queryEntity(Serializable id);

    /**
     * 下载团队成员关系表导入模板
     *
     * @author hanbin
     * @date  2026/09/07 18:44
     */
    void downloadImportTemplate(HttpServletResponse response) throws IOException;

    /**
     * 导入团队成员关系表
     *
     * @author hanbin
     * @date  2026/09/07 18:44
     **/
    JSONObject importData(MultipartFile file);

    /**
     * 导出团队成员关系表
     *
     * @author hanbin
     * @date  2026/09/07 18:44
     */
    void exportData(List<ZyTeamMemberIdParam> zyTeamMemberIdParamList, HttpServletResponse response) throws IOException;
}
