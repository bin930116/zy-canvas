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
package vip.xiaonuo.canvas.modular.canvasunitlink.service.impl;

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
import vip.xiaonuo.canvas.modular.canvasunitlink.entity.ZyCanvasUnitLink;
import vip.xiaonuo.canvas.modular.canvasunitlink.mapper.ZyCanvasUnitLinkMapper;
import vip.xiaonuo.canvas.modular.canvasunitlink.param.ZyCanvasUnitLinkAddParam;
import vip.xiaonuo.canvas.modular.canvasunitlink.param.ZyCanvasUnitLinkEditParam;
import vip.xiaonuo.canvas.modular.canvasunitlink.param.ZyCanvasUnitLinkIdParam;
import vip.xiaonuo.canvas.modular.canvasunitlink.param.ZyCanvasUnitLinkPageParam;
import vip.xiaonuo.canvas.modular.canvasunitlink.service.ZyCanvasUnitLinkService;

import vip.xiaonuo.common.util.CommonDownloadUtil;
import vip.xiaonuo.common.util.CommonResponseUtil;

import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.util.List;

/**
 * 画布与章节关联Service接口实现类
 *
 * @author hanbin
 * @date  2026/09/08 14:31
 **/
@Service
public class ZyCanvasUnitLinkServiceImpl extends ServiceImpl<ZyCanvasUnitLinkMapper, ZyCanvasUnitLink> implements ZyCanvasUnitLinkService {

    @Override
    public Page<ZyCanvasUnitLink> page(ZyCanvasUnitLinkPageParam zyCanvasUnitLinkPageParam) {
        QueryWrapper<ZyCanvasUnitLink> queryWrapper = new QueryWrapper<ZyCanvasUnitLink>().checkSqlInjection();
        if(ObjectUtil.isNotEmpty(zyCanvasUnitLinkPageParam.getProjectId())) {
            queryWrapper.lambda().eq(ZyCanvasUnitLink::getProjectId, zyCanvasUnitLinkPageParam.getProjectId());
        }
        if(ObjectUtil.isNotEmpty(zyCanvasUnitLinkPageParam.getCanvasId())) {
            queryWrapper.lambda().eq(ZyCanvasUnitLink::getCanvasId, zyCanvasUnitLinkPageParam.getCanvasId());
        }
        if(ObjectUtil.isNotEmpty(zyCanvasUnitLinkPageParam.getUnitId())) {
            queryWrapper.lambda().eq(ZyCanvasUnitLink::getUnitId, zyCanvasUnitLinkPageParam.getUnitId());
        }
        if(ObjectUtil.isAllNotEmpty(zyCanvasUnitLinkPageParam.getSortField(), zyCanvasUnitLinkPageParam.getSortOrder())) {
            CommonSortOrderEnum.validate(zyCanvasUnitLinkPageParam.getSortOrder());
            queryWrapper.orderBy(true, zyCanvasUnitLinkPageParam.getSortOrder().equals(CommonSortOrderEnum.ASC.getValue()),
                    StrUtil.toUnderlineCase(zyCanvasUnitLinkPageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByAsc(ZyCanvasUnitLink::getId);
        }
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            return new Page<>();
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // CommonSqlUtil.scopeIn(queryWrapper.lambda(), ZyCanvasUnitLink::getOrgId, StpUtil.getLoginIdAsString(), CommonServletUtil.getRequest().getServletPath());
        }
        return this.page(CommonPageRequest.defaultPage(), queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ZyCanvasUnitLinkAddParam zyCanvasUnitLinkAddParam) {
        ZyCanvasUnitLink zyCanvasUnitLink = BeanUtil.toBean(zyCanvasUnitLinkAddParam, ZyCanvasUnitLink.class);
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限增加画布与章节关联");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // if(!loginUserDataScope.contains(zyCanvasUnitLink.getOrgId())) {
            //     throw new CommonException("您没有权限在该机构下增加画布与章节关联");
            // }
        }

        this.save(zyCanvasUnitLink);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ZyCanvasUnitLinkEditParam zyCanvasUnitLinkEditParam) {
        ZyCanvasUnitLink zyCanvasUnitLink = this.queryEntity(zyCanvasUnitLinkEditParam.getId());
        BeanUtil.copyProperties(zyCanvasUnitLinkEditParam, zyCanvasUnitLink);
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限编辑画布与章节关联");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // if(!loginUserDataScope.contains(zyCanvasUnitLink.getOrgId())) {
            //     throw new CommonException("您没有权限编辑该机构下的画布与章节关联");
            // }
        }

        this.updateById(zyCanvasUnitLink);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ZyCanvasUnitLinkIdParam> zyCanvasUnitLinkIdParamList) {
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限删除画布与章节关联");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如对删除的记录进行数据范围校验
            // Set<String> orgIdSet = this.listByIds(CollStreamUtil.toList(zyCanvasUnitLinkIdParamList, ZyCanvasUnitLinkIdParam::getId))
            //         .stream().map(ZyCanvasUnitLink::getOrgId).collect(Collectors.toSet());
            // if(!new HashSet<>(loginUserDataScope).containsAll(orgIdSet)) {
            //     throw new CommonException("您没有权限删除这些机构下的画布与章节关联");
            // }
        }
        // 执行删除
        this.removeByIds(CollStreamUtil.toList(zyCanvasUnitLinkIdParamList, ZyCanvasUnitLinkIdParam::getId));
    }

    @Override
    public ZyCanvasUnitLink detail(ZyCanvasUnitLinkIdParam zyCanvasUnitLinkIdParam) {
        return this.queryEntity(zyCanvasUnitLinkIdParam.getId());
    }

    @Override
    public ZyCanvasUnitLink queryEntity(Serializable id) {
        ZyCanvasUnitLink zyCanvasUnitLink = this.getById(id);
        if(ObjectUtil.isEmpty(zyCanvasUnitLink)) {
            throw new CommonException("画布与章节关联不存在，id值为：{}", id);
        }
        return zyCanvasUnitLink;
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
       File tempFile = null;
       try {
         List<ZyCanvasUnitLinkEditParam> dataList = CollectionUtil.newArrayList();
         String fileName = "画布与章节关联导入模板_" + DateUtil.format(DateTime.now(), DatePattern.PURE_DATETIME_PATTERN) + ".xlsx";
         tempFile = FileUtil.file(FileUtil.getTmpDir() + FileUtil.FILE_SEPARATOR + fileName);
         EasyExcel.write(tempFile.getPath(), ZyCanvasUnitLinkEditParam.class).sheet("画布与章节关联").doWrite(dataList);
         CommonDownloadUtil.download(tempFile, response);
       } catch (Exception e) {
         log.error(">>> 画布与章节关联导入模板下载失败：", e);
         CommonResponseUtil.renderError(response, "画布与章节关联导入模板下载失败");
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
                    FileUtil.FILE_SEPARATOR + "zyCanvasUnitLinkImportTemplate.xlsx"));
            // 读取excel
            List<ZyCanvasUnitLinkEditParam> zyCanvasUnitLinkEditParamList =  EasyExcel.read(tempFile).head(ZyCanvasUnitLinkEditParam.class).sheet()
                    .headRowNumber(1).doReadSync();
            List<ZyCanvasUnitLink> allDataList = this.list();
            for (int i = 0; i < zyCanvasUnitLinkEditParamList.size(); i++) {
                JSONObject jsonObject = this.doImport(allDataList, zyCanvasUnitLinkEditParamList.get(i), i);
                if(jsonObject.getBool("success")) {
                    successCount += 1;
                } else {
                    errorCount += 1;
                    errorDetail.add(jsonObject);
                }
            }
            return JSONUtil.createObj()
                    .set("totalCount", zyCanvasUnitLinkEditParamList.size())
                    .set("successCount", successCount)
                    .set("errorCount", errorCount)
                    .set("errorDetail", errorDetail);
        } catch (Exception e) {
            log.error(">>> 画布与章节关联导入失败：", e);
            throw new CommonException("画布与章节关联导入失败");
        }
    }

    public JSONObject doImport(List<ZyCanvasUnitLink> allDataList, ZyCanvasUnitLinkEditParam zyCanvasUnitLinkEditParam, int i) {
        String id = zyCanvasUnitLinkEditParam.getId();
        if(ObjectUtil.hasEmpty(id)) {
            return JSONUtil.createObj().set("index", i + 1).set("success", false).set("msg", "必填字段存在空值");
        } else {
            try {
                int index = CollStreamUtil.toList(allDataList, ZyCanvasUnitLink::getId).indexOf(zyCanvasUnitLinkEditParam.getId());
                ZyCanvasUnitLink zyCanvasUnitLink;
                boolean isAdd = false;
                if(index == -1) {
                    isAdd = true;
                    zyCanvasUnitLink = new ZyCanvasUnitLink();
                } else {
                    zyCanvasUnitLink = allDataList.get(index);
                }
                BeanUtil.copyProperties(zyCanvasUnitLinkEditParam, zyCanvasUnitLink);
                if(isAdd) {
                    allDataList.add(zyCanvasUnitLink);
                } else {
                    allDataList.remove(index);
                    allDataList.add(index, zyCanvasUnitLink);
                }
                this.saveOrUpdate(zyCanvasUnitLink);
                return JSONUtil.createObj().set("success", true);
            } catch (Exception e) {
              log.error(">>> 数据导入异常：", e);
              return JSONUtil.createObj().set("success", false).set("index", i + 1).set("msg", "数据导入异常");
            }
        }
    }

    @Override
    public void exportData(List<ZyCanvasUnitLinkIdParam> zyCanvasUnitLinkIdParamList, HttpServletResponse response) throws IOException {
       File tempFile = null;
       try {
         List<ZyCanvasUnitLinkEditParam> dataList;
         if(ObjectUtil.isNotEmpty(zyCanvasUnitLinkIdParamList)) {
            List<Serializable> idList = CollStreamUtil.toList(zyCanvasUnitLinkIdParamList, ZyCanvasUnitLinkIdParam::getId);
            dataList = BeanUtil.copyToList(this.listByIds(idList), ZyCanvasUnitLinkEditParam.class);
         } else {
            dataList = BeanUtil.copyToList(this.list(), ZyCanvasUnitLinkEditParam.class);
         }
         String fileName = "画布与章节关联_" + DateUtil.format(DateTime.now(), DatePattern.PURE_DATETIME_PATTERN) + ".xlsx";
         tempFile = FileUtil.file(FileUtil.getTmpDir() + FileUtil.FILE_SEPARATOR + fileName);
         EasyExcel.write(tempFile.getPath(), ZyCanvasUnitLinkEditParam.class).sheet("画布与章节关联").doWrite(dataList);
         CommonDownloadUtil.download(tempFile, response);
       } catch (Exception e) {
         log.error(">>> 画布与章节关联导出失败：", e);
         CommonResponseUtil.renderError(response, "画布与章节关联导出失败");
       } finally {
         FileUtil.del(tempFile);
       }
    }
}
