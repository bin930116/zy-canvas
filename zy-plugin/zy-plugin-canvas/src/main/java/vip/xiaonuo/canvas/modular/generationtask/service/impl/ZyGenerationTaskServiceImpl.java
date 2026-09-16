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
package vip.xiaonuo.canvas.modular.generationtask.service.impl;

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
import vip.xiaonuo.canvas.modular.generationtask.entity.ZyGenerationTask;
import vip.xiaonuo.canvas.modular.generationtask.mapper.ZyGenerationTaskMapper;
import vip.xiaonuo.canvas.modular.generationtask.param.ZyGenerationTaskAddParam;
import vip.xiaonuo.canvas.modular.generationtask.param.ZyGenerationTaskEditParam;
import vip.xiaonuo.canvas.modular.generationtask.param.ZyGenerationTaskIdParam;
import vip.xiaonuo.canvas.modular.generationtask.param.ZyGenerationTaskPageParam;
import vip.xiaonuo.canvas.modular.generationtask.service.ZyGenerationTaskService;

import vip.xiaonuo.common.util.CommonDownloadUtil;
import vip.xiaonuo.common.util.CommonResponseUtil;

import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.util.List;

/**
 * 任务队列信息Service接口实现类
 *
 * @author hanbin
 * @date  2026/09/08 14:24
 **/
@Service
public class ZyGenerationTaskServiceImpl extends ServiceImpl<ZyGenerationTaskMapper, ZyGenerationTask> implements ZyGenerationTaskService {

    @Override
    public Page<ZyGenerationTask> page(ZyGenerationTaskPageParam zyGenerationTaskPageParam) {
        QueryWrapper<ZyGenerationTask> queryWrapper = new QueryWrapper<ZyGenerationTask>().checkSqlInjection();
        if(ObjectUtil.isNotEmpty(zyGenerationTaskPageParam.getUserId())) {
            queryWrapper.lambda().eq(ZyGenerationTask::getUserId, zyGenerationTaskPageParam.getUserId());
        }
        if(ObjectUtil.isNotEmpty(zyGenerationTaskPageParam.getProjectId())) {
            queryWrapper.lambda().eq(ZyGenerationTask::getProjectId, zyGenerationTaskPageParam.getProjectId());
        }
        if(ObjectUtil.isNotEmpty(zyGenerationTaskPageParam.getType())) {
            queryWrapper.lambda().eq(ZyGenerationTask::getType, zyGenerationTaskPageParam.getType());
        }
        if(ObjectUtil.isNotEmpty(zyGenerationTaskPageParam.getStatus())) {
            queryWrapper.lambda().eq(ZyGenerationTask::getStatus, zyGenerationTaskPageParam.getStatus());
        }
        if(ObjectUtil.isAllNotEmpty(zyGenerationTaskPageParam.getSortField(), zyGenerationTaskPageParam.getSortOrder())) {
            CommonSortOrderEnum.validate(zyGenerationTaskPageParam.getSortOrder());
            queryWrapper.orderBy(true, zyGenerationTaskPageParam.getSortOrder().equals(CommonSortOrderEnum.ASC.getValue()),
                    StrUtil.toUnderlineCase(zyGenerationTaskPageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByAsc(ZyGenerationTask::getId);
        }
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            return new Page<>();
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // CommonSqlUtil.scopeIn(queryWrapper.lambda(), ZyGenerationTask::getOrgId, StpUtil.getLoginIdAsString(), CommonServletUtil.getRequest().getServletPath());
        }
        return this.page(CommonPageRequest.defaultPage(), queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ZyGenerationTaskAddParam zyGenerationTaskAddParam) {
        ZyGenerationTask zyGenerationTask = BeanUtil.toBean(zyGenerationTaskAddParam, ZyGenerationTask.class);
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限增加任务队列信息");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // if(!loginUserDataScope.contains(zyGenerationTask.getOrgId())) {
            //     throw new CommonException("您没有权限在该机构下增加任务队列信息");
            // }
        }

        this.save(zyGenerationTask);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ZyGenerationTaskEditParam zyGenerationTaskEditParam) {
        ZyGenerationTask zyGenerationTask = this.queryEntity(zyGenerationTaskEditParam.getId());
        BeanUtil.copyProperties(zyGenerationTaskEditParam, zyGenerationTask);
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限编辑任务队列信息");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // if(!loginUserDataScope.contains(zyGenerationTask.getOrgId())) {
            //     throw new CommonException("您没有权限编辑该机构下的任务队列信息");
            // }
        }

        this.updateById(zyGenerationTask);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ZyGenerationTaskIdParam> zyGenerationTaskIdParamList) {
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限删除任务队列信息");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如对删除的记录进行数据范围校验
            // Set<String> orgIdSet = this.listByIds(CollStreamUtil.toList(zyGenerationTaskIdParamList, ZyGenerationTaskIdParam::getId))
            //         .stream().map(ZyGenerationTask::getOrgId).collect(Collectors.toSet());
            // if(!new HashSet<>(loginUserDataScope).containsAll(orgIdSet)) {
            //     throw new CommonException("您没有权限删除这些机构下的任务队列信息");
            // }
        }
        // 执行删除
        this.removeByIds(CollStreamUtil.toList(zyGenerationTaskIdParamList, ZyGenerationTaskIdParam::getId));
    }

    @Override
    public ZyGenerationTask detail(ZyGenerationTaskIdParam zyGenerationTaskIdParam) {
        return this.queryEntity(zyGenerationTaskIdParam.getId());
    }

    @Override
    public ZyGenerationTask queryEntity(Serializable id) {
        ZyGenerationTask zyGenerationTask = this.getById(id);
        if(ObjectUtil.isEmpty(zyGenerationTask)) {
            throw new CommonException("任务队列信息不存在，id值为：{}", id);
        }
        return zyGenerationTask;
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
       File tempFile = null;
       try {
         List<ZyGenerationTaskEditParam> dataList = CollectionUtil.newArrayList();
         String fileName = "任务队列信息导入模板_" + DateUtil.format(DateTime.now(), DatePattern.PURE_DATETIME_PATTERN) + ".xlsx";
         tempFile = FileUtil.file(FileUtil.getTmpDir() + FileUtil.FILE_SEPARATOR + fileName);
         EasyExcel.write(tempFile.getPath(), ZyGenerationTaskEditParam.class).sheet("任务队列信息").doWrite(dataList);
         CommonDownloadUtil.download(tempFile, response);
       } catch (Exception e) {
         log.error(">>> 任务队列信息导入模板下载失败：", e);
         CommonResponseUtil.renderError(response, "任务队列信息导入模板下载失败");
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
                    FileUtil.FILE_SEPARATOR + "zyGenerationTaskImportTemplate.xlsx"));
            // 读取excel
            List<ZyGenerationTaskEditParam> zyGenerationTaskEditParamList =  EasyExcel.read(tempFile).head(ZyGenerationTaskEditParam.class).sheet()
                    .headRowNumber(1).doReadSync();
            List<ZyGenerationTask> allDataList = this.list();
            for (int i = 0; i < zyGenerationTaskEditParamList.size(); i++) {
                JSONObject jsonObject = this.doImport(allDataList, zyGenerationTaskEditParamList.get(i), i);
                if(jsonObject.getBool("success")) {
                    successCount += 1;
                } else {
                    errorCount += 1;
                    errorDetail.add(jsonObject);
                }
            }
            return JSONUtil.createObj()
                    .set("totalCount", zyGenerationTaskEditParamList.size())
                    .set("successCount", successCount)
                    .set("errorCount", errorCount)
                    .set("errorDetail", errorDetail);
        } catch (Exception e) {
            log.error(">>> 任务队列信息导入失败：", e);
            throw new CommonException("任务队列信息导入失败");
        }
    }

    public JSONObject doImport(List<ZyGenerationTask> allDataList, ZyGenerationTaskEditParam zyGenerationTaskEditParam, int i) {
        String id = zyGenerationTaskEditParam.getId();
        if(ObjectUtil.hasEmpty(id)) {
            return JSONUtil.createObj().set("index", i + 1).set("success", false).set("msg", "必填字段存在空值");
        } else {
            try {
                int index = CollStreamUtil.toList(allDataList, ZyGenerationTask::getId).indexOf(zyGenerationTaskEditParam.getId());
                ZyGenerationTask zyGenerationTask;
                boolean isAdd = false;
                if(index == -1) {
                    isAdd = true;
                    zyGenerationTask = new ZyGenerationTask();
                } else {
                    zyGenerationTask = allDataList.get(index);
                }
                BeanUtil.copyProperties(zyGenerationTaskEditParam, zyGenerationTask);
                if(isAdd) {
                    allDataList.add(zyGenerationTask);
                } else {
                    allDataList.remove(index);
                    allDataList.add(index, zyGenerationTask);
                }
                this.saveOrUpdate(zyGenerationTask);
                return JSONUtil.createObj().set("success", true);
            } catch (Exception e) {
              log.error(">>> 数据导入异常：", e);
              return JSONUtil.createObj().set("success", false).set("index", i + 1).set("msg", "数据导入异常");
            }
        }
    }

    @Override
    public void exportData(List<ZyGenerationTaskIdParam> zyGenerationTaskIdParamList, HttpServletResponse response) throws IOException {
       File tempFile = null;
       try {
         List<ZyGenerationTaskEditParam> dataList;
         if(ObjectUtil.isNotEmpty(zyGenerationTaskIdParamList)) {
            List<Serializable> idList = CollStreamUtil.toList(zyGenerationTaskIdParamList, ZyGenerationTaskIdParam::getId);
            dataList = BeanUtil.copyToList(this.listByIds(idList), ZyGenerationTaskEditParam.class);
         } else {
            dataList = BeanUtil.copyToList(this.list(), ZyGenerationTaskEditParam.class);
         }
         String fileName = "任务队列信息_" + DateUtil.format(DateTime.now(), DatePattern.PURE_DATETIME_PATTERN) + ".xlsx";
         tempFile = FileUtil.file(FileUtil.getTmpDir() + FileUtil.FILE_SEPARATOR + fileName);
         EasyExcel.write(tempFile.getPath(), ZyGenerationTaskEditParam.class).sheet("任务队列信息").doWrite(dataList);
         CommonDownloadUtil.download(tempFile, response);
       } catch (Exception e) {
         log.error(">>> 任务队列信息导出失败：", e);
         CommonResponseUtil.renderError(response, "任务队列信息导出失败");
       } finally {
         FileUtil.del(tempFile);
       }
    }
}
