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
package vip.xiaonuo.canvas.modular.teammember.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import cn.dev33.satoken.stp.StpUtil;
import vip.xiaonuo.auth.core.util.StpLoginUserUtil;
import vip.xiaonuo.common.util.CommonServletUtil;
import vip.xiaonuo.common.util.CommonSqlUtil;
import vip.xiaonuo.common.enums.CommonSortOrderEnum;
import vip.xiaonuo.common.exception.CommonException;
import vip.xiaonuo.common.page.CommonPageRequest;
import java.math.BigDecimal;
import java.util.Date;
import vip.xiaonuo.canvas.modular.teammember.entity.ZyTeamMember;
import vip.xiaonuo.canvas.modular.teammember.mapper.ZyTeamMemberMapper;
import vip.xiaonuo.canvas.modular.teammember.param.ZyTeamMemberAddParam;
import vip.xiaonuo.canvas.modular.teammember.param.ZyTeamMemberEditParam;
import vip.xiaonuo.canvas.modular.teammember.param.ZyTeamMemberIdParam;
import vip.xiaonuo.canvas.modular.teammember.param.ZyTeamMemberPageParam;
import vip.xiaonuo.canvas.modular.teammember.service.ZyTeamMemberService;

import vip.xiaonuo.common.util.CommonDownloadUtil;
import vip.xiaonuo.common.util.CommonResponseUtil;

import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.util.List;

/**
 * 团队成员关系表Service接口实现类
 *
 * @author hanbin
 * @date  2026/09/07 18:44
 **/
@Service
public class ZyTeamMemberServiceImpl extends ServiceImpl<ZyTeamMemberMapper, ZyTeamMember> implements ZyTeamMemberService {

    @Override
    public Page<ZyTeamMember> page(ZyTeamMemberPageParam zyTeamMemberPageParam) {
        QueryWrapper<ZyTeamMember> queryWrapper = new QueryWrapper<ZyTeamMember>().checkSqlInjection();
        if(ObjectUtil.isNotEmpty(zyTeamMemberPageParam.getTeamId())) {
            queryWrapper.lambda().eq(ZyTeamMember::getTeamId, zyTeamMemberPageParam.getTeamId());
        }
        if(ObjectUtil.isNotEmpty(zyTeamMemberPageParam.getUserId())) {
            queryWrapper.lambda().eq(ZyTeamMember::getUserId, zyTeamMemberPageParam.getUserId());
        }
        if(ObjectUtil.isAllNotEmpty(zyTeamMemberPageParam.getSortField(), zyTeamMemberPageParam.getSortOrder())) {
            CommonSortOrderEnum.validate(zyTeamMemberPageParam.getSortOrder());
            queryWrapper.orderBy(true, zyTeamMemberPageParam.getSortOrder().equals(CommonSortOrderEnum.ASC.getValue()),
                    StrUtil.toUnderlineCase(zyTeamMemberPageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByAsc(ZyTeamMember::getId);
        }
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            return new Page<>();
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // CommonSqlUtil.scopeIn(queryWrapper.lambda(), ZyTeamMember::getOrgId, StpUtil.getLoginIdAsString(), CommonServletUtil.getRequest().getServletPath());
        }
        return this.page(CommonPageRequest.defaultPage(), queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ZyTeamMemberAddParam zyTeamMemberAddParam) {
        ZyTeamMember zyTeamMember = BeanUtil.toBean(zyTeamMemberAddParam, ZyTeamMember.class);
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限增加团队成员关系表");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // if(!loginUserDataScope.contains(zyTeamMember.getOrgId())) {
            //     throw new CommonException("您没有权限在该机构下增加团队成员关系表");
            // }
        }

        this.save(zyTeamMember);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ZyTeamMemberEditParam zyTeamMemberEditParam) {
        ZyTeamMember zyTeamMember = this.queryEntity(zyTeamMemberEditParam.getId());
        BeanUtil.copyProperties(zyTeamMemberEditParam, zyTeamMember);
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限编辑团队成员关系表");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // if(!loginUserDataScope.contains(zyTeamMember.getOrgId())) {
            //     throw new CommonException("您没有权限编辑该机构下的团队成员关系表");
            // }
        }

        this.updateById(zyTeamMember);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ZyTeamMemberIdParam> zyTeamMemberIdParamList) {
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限删除团队成员关系表");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如对删除的记录进行数据范围校验
            // Set<String> orgIdSet = this.listByIds(CollStreamUtil.toList(zyTeamMemberIdParamList, ZyTeamMemberIdParam::getId))
            //         .stream().map(ZyTeamMember::getOrgId).collect(Collectors.toSet());
            // if(!new HashSet<>(loginUserDataScope).containsAll(orgIdSet)) {
            //     throw new CommonException("您没有权限删除这些机构下的团队成员关系表");
            // }
        }
        // 执行删除
        this.removeByIds(CollStreamUtil.toList(zyTeamMemberIdParamList, ZyTeamMemberIdParam::getId));
    }

    @Override
    public ZyTeamMember detail(ZyTeamMemberIdParam zyTeamMemberIdParam) {
        return this.queryEntity(zyTeamMemberIdParam.getId());
    }

    @Override
    public ZyTeamMember queryEntity(Serializable id) {
        ZyTeamMember zyTeamMember = this.getById(id);
        if(ObjectUtil.isEmpty(zyTeamMember)) {
            throw new CommonException("团队成员关系表不存在，id值为：{}", id);
        }
        return zyTeamMember;
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
       File tempFile = null;
       try {
         List<ZyTeamMemberEditParam> dataList = CollectionUtil.newArrayList();
         String fileName = "团队成员关系表导入模板_" + DateUtil.format(DateTime.now(), DatePattern.PURE_DATETIME_PATTERN) + ".xlsx";
         tempFile = FileUtil.file(FileUtil.getTmpDir() + FileUtil.FILE_SEPARATOR + fileName);
         EasyExcel.write(tempFile.getPath(), ZyTeamMemberEditParam.class).sheet("团队成员关系表").doWrite(dataList);
         CommonDownloadUtil.download(tempFile, response);
       } catch (Exception e) {
         log.error(">>> 团队成员关系表导入模板下载失败：", e);
         CommonResponseUtil.renderError(response, "团队成员关系表导入模板下载失败");
       } finally {
         FileUtil.del(tempFile);
       }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JSONObject importData(MultipartFile file) {
        try {
            int successCount = 0;
            int errorCount = 0;
            JSONArray errorDetail = JSONUtil.createArray();
            // 创建临时文件
            File tempFile = FileUtil.writeBytes(file.getBytes(), FileUtil.file(FileUtil.getTmpDir() +
                    FileUtil.FILE_SEPARATOR + "zyTeamMemberImportTemplate.xlsx"));
            // 读取excel
            List<ZyTeamMemberEditParam> zyTeamMemberEditParamList =  EasyExcel.read(tempFile).head(ZyTeamMemberEditParam.class).sheet()
                    .headRowNumber(1).doReadSync();
            List<ZyTeamMember> allDataList = this.list();
            for (int i = 0; i < zyTeamMemberEditParamList.size(); i++) {
                JSONObject jsonObject = this.doImport(allDataList, zyTeamMemberEditParamList.get(i), i);
                if(jsonObject.getBool("success")) {
                    successCount += 1;
                } else {
                    errorCount += 1;
                    errorDetail.add(jsonObject);
                }
            }
            return JSONUtil.createObj()
                    .set("totalCount", zyTeamMemberEditParamList.size())
                    .set("successCount", successCount)
                    .set("errorCount", errorCount)
                    .set("errorDetail", errorDetail);
        } catch (Exception e) {
            log.error(">>> 团队成员关系表导入失败：", e);
            throw new CommonException("团队成员关系表导入失败");
        }
    }

    public JSONObject doImport(List<ZyTeamMember> allDataList, ZyTeamMemberEditParam zyTeamMemberEditParam, int i) {
        String id = zyTeamMemberEditParam.getId();
        String teamId = zyTeamMemberEditParam.getTeamId();
        String userId = zyTeamMemberEditParam.getUserId();
        String role = zyTeamMemberEditParam.getRole();
        String status = zyTeamMemberEditParam.getStatus();
        String invitedBy = zyTeamMemberEditParam.getInvitedBy();
        if(ObjectUtil.hasEmpty(id, teamId, userId, role, status, invitedBy)) {
            return JSONUtil.createObj().set("index", i + 1).set("success", false).set("msg", "必填字段存在空值");
        } else {
            try {
                int index = CollStreamUtil.toList(allDataList, ZyTeamMember::getId).indexOf(zyTeamMemberEditParam.getId());
                ZyTeamMember zyTeamMember;
                boolean isAdd = false;
                if(index == -1) {
                    isAdd = true;
                    zyTeamMember = new ZyTeamMember();
                } else {
                    zyTeamMember = allDataList.get(index);
                }
                BeanUtil.copyProperties(zyTeamMemberEditParam, zyTeamMember);
                if(isAdd) {
                    allDataList.add(zyTeamMember);
                } else {
                    allDataList.remove(index);
                    allDataList.add(index, zyTeamMember);
                }
                this.saveOrUpdate(zyTeamMember);
                return JSONUtil.createObj().set("success", true);
            } catch (Exception e) {
              log.error(">>> 数据导入异常：", e);
              return JSONUtil.createObj().set("success", false).set("index", i + 1).set("msg", "数据导入异常");
            }
        }
    }

    @Override
    public void exportData(List<ZyTeamMemberIdParam> zyTeamMemberIdParamList, HttpServletResponse response) throws IOException {
       File tempFile = null;
       try {
         List<ZyTeamMemberEditParam> dataList;
         if(ObjectUtil.isNotEmpty(zyTeamMemberIdParamList)) {
            List<Serializable> idList = CollStreamUtil.toList(zyTeamMemberIdParamList, ZyTeamMemberIdParam::getId);
            dataList = BeanUtil.copyToList(this.listByIds(idList), ZyTeamMemberEditParam.class);
         } else {
            dataList = BeanUtil.copyToList(this.list(), ZyTeamMemberEditParam.class);
         }
         String fileName = "团队成员关系表_" + DateUtil.format(DateTime.now(), DatePattern.PURE_DATETIME_PATTERN) + ".xlsx";
         tempFile = FileUtil.file(FileUtil.getTmpDir() + FileUtil.FILE_SEPARATOR + fileName);
         EasyExcel.write(tempFile.getPath(), ZyTeamMemberEditParam.class).sheet("团队成员关系表").doWrite(dataList);
         CommonDownloadUtil.download(tempFile, response);
       } catch (Exception e) {
         log.error(">>> 团队成员关系表导出失败：", e);
         CommonResponseUtil.renderError(response, "团队成员关系表导出失败");
       } finally {
         FileUtil.del(tempFile);
       }
    }
}
