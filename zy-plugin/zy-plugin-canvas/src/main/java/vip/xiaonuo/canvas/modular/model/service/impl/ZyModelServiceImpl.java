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
package vip.xiaonuo.canvas.modular.model.service.impl;

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
import vip.xiaonuo.canvas.modular.model.entity.ZyModel;
import vip.xiaonuo.canvas.modular.model.mapper.ZyModelMapper;
import vip.xiaonuo.canvas.modular.model.param.ZyModelAddParam;
import vip.xiaonuo.canvas.modular.model.param.ZyModelEditParam;
import vip.xiaonuo.canvas.modular.model.param.ZyModelIdParam;
import vip.xiaonuo.canvas.modular.model.param.ZyModelPageParam;
import vip.xiaonuo.canvas.modular.model.service.ZyModelService;

import vip.xiaonuo.common.util.CommonDownloadUtil;
import vip.xiaonuo.common.util.CommonResponseUtil;

import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.util.List;

/**
 * 模型配置Service接口实现类
 *
 * @author hanbin
 * @date  2026/09/07 18:27
 **/
@Service
public class ZyModelServiceImpl extends ServiceImpl<ZyModelMapper, ZyModel> implements ZyModelService {

    @Override
    public Page<ZyModel> page(ZyModelPageParam zyModelPageParam) {
        QueryWrapper<ZyModel> queryWrapper = new QueryWrapper<ZyModel>().checkSqlInjection();
        if(ObjectUtil.isNotEmpty(zyModelPageParam.getModelKey())) {
            queryWrapper.lambda().like(ZyModel::getModelKey, zyModelPageParam.getModelKey());
        }
        if(ObjectUtil.isNotEmpty(zyModelPageParam.getModelName())) {
            queryWrapper.lambda().like(ZyModel::getModelName, zyModelPageParam.getModelName());
        }
        if(ObjectUtil.isAllNotEmpty(zyModelPageParam.getSortField(), zyModelPageParam.getSortOrder())) {
            CommonSortOrderEnum.validate(zyModelPageParam.getSortOrder());
            queryWrapper.orderBy(true, zyModelPageParam.getSortOrder().equals(CommonSortOrderEnum.ASC.getValue()),
                    StrUtil.toUnderlineCase(zyModelPageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByAsc(ZyModel::getSortCode);
        }
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            return new Page<>();
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // CommonSqlUtil.scopeIn(queryWrapper.lambda(), ZyModel::getOrgId, StpUtil.getLoginIdAsString(), CommonServletUtil.getRequest().getServletPath());
        }
        return this.page(CommonPageRequest.defaultPage(), queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ZyModelAddParam zyModelAddParam) {
        ZyModel zyModel = BeanUtil.toBean(zyModelAddParam, ZyModel.class);
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限增加模型配置");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // if(!loginUserDataScope.contains(zyModel.getOrgId())) {
            //     throw new CommonException("您没有权限在该机构下增加模型配置");
            // }
        }

        this.save(zyModel);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ZyModelEditParam zyModelEditParam) {
        ZyModel zyModel = this.queryEntity(zyModelEditParam.getId());
        BeanUtil.copyProperties(zyModelEditParam, zyModel);
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限编辑模型配置");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // if(!loginUserDataScope.contains(zyModel.getOrgId())) {
            //     throw new CommonException("您没有权限编辑该机构下的模型配置");
            // }
        }

        this.updateById(zyModel);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ZyModelIdParam> zyModelIdParamList) {
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限删除模型配置");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如对删除的记录进行数据范围校验
            // Set<String> orgIdSet = this.listByIds(CollStreamUtil.toList(zyModelIdParamList, ZyModelIdParam::getId))
            //         .stream().map(ZyModel::getOrgId).collect(Collectors.toSet());
            // if(!new HashSet<>(loginUserDataScope).containsAll(orgIdSet)) {
            //     throw new CommonException("您没有权限删除这些机构下的模型配置");
            // }
        }
        // 执行删除
        this.removeByIds(CollStreamUtil.toList(zyModelIdParamList, ZyModelIdParam::getId));
    }

    @Override
    public ZyModel detail(ZyModelIdParam zyModelIdParam) {
        return this.queryEntity(zyModelIdParam.getId());
    }

    @Override
    public ZyModel queryEntity(Serializable id) {
        ZyModel zyModel = this.getById(id);
        if(ObjectUtil.isEmpty(zyModel)) {
            throw new CommonException("模型配置不存在，id值为：{}", id);
        }
        return zyModel;
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
       File tempFile = null;
       try {
         List<ZyModelEditParam> dataList = CollectionUtil.newArrayList();
         String fileName = "模型配置导入模板_" + DateUtil.format(DateTime.now(), DatePattern.PURE_DATETIME_PATTERN) + ".xlsx";
         tempFile = FileUtil.file(FileUtil.getTmpDir() + FileUtil.FILE_SEPARATOR + fileName);
         EasyExcel.write(tempFile.getPath(), ZyModelEditParam.class).sheet("模型配置").doWrite(dataList);
         CommonDownloadUtil.download(tempFile, response);
       } catch (Exception e) {
         log.error(">>> 模型配置导入模板下载失败：", e);
         CommonResponseUtil.renderError(response, "模型配置导入模板下载失败");
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
                    FileUtil.FILE_SEPARATOR + "zyModelImportTemplate.xlsx"));
            // 读取excel
            List<ZyModelEditParam> zyModelEditParamList =  EasyExcel.read(tempFile).head(ZyModelEditParam.class).sheet()
                    .headRowNumber(1).doReadSync();
            List<ZyModel> allDataList = this.list();
            for (int i = 0; i < zyModelEditParamList.size(); i++) {
                JSONObject jsonObject = this.doImport(allDataList, zyModelEditParamList.get(i), i);
                if(jsonObject.getBool("success")) {
                    successCount += 1;
                } else {
                    errorCount += 1;
                    errorDetail.add(jsonObject);
                }
            }
            return JSONUtil.createObj()
                    .set("totalCount", zyModelEditParamList.size())
                    .set("successCount", successCount)
                    .set("errorCount", errorCount)
                    .set("errorDetail", errorDetail);
        } catch (Exception e) {
            log.error(">>> 模型配置导入失败：", e);
            throw new CommonException("模型配置导入失败");
        }
    }

    public JSONObject doImport(List<ZyModel> allDataList, ZyModelEditParam zyModelEditParam, int i) {
        String id = zyModelEditParam.getId();
        String modelKey = zyModelEditParam.getModelKey();
        String modelName = zyModelEditParam.getModelName();
        String capability = zyModelEditParam.getCapability();
        String protocol = zyModelEditParam.getProtocol();
        String baseUrl = zyModelEditParam.getBaseUrl();
        String apiKey = zyModelEditParam.getApiKey();
        Integer maxConcurrency = zyModelEditParam.getMaxConcurrency();
        Integer timeoutSeconds = zyModelEditParam.getTimeoutSeconds();
        String status = zyModelEditParam.getStatus();
        String isDefault = zyModelEditParam.getIsDefault();
        if(ObjectUtil.hasEmpty(id, modelKey, modelName, capability, protocol, baseUrl, apiKey, maxConcurrency, timeoutSeconds, status, isDefault)) {
            return JSONUtil.createObj().set("index", i + 1).set("success", false).set("msg", "必填字段存在空值");
        } else {
            try {
                int index = CollStreamUtil.toList(allDataList, ZyModel::getId).indexOf(zyModelEditParam.getId());
                ZyModel zyModel;
                boolean isAdd = false;
                if(index == -1) {
                    isAdd = true;
                    zyModel = new ZyModel();
                } else {
                    zyModel = allDataList.get(index);
                }
                BeanUtil.copyProperties(zyModelEditParam, zyModel);
                if(isAdd) {
                    allDataList.add(zyModel);
                } else {
                    allDataList.remove(index);
                    allDataList.add(index, zyModel);
                }
                this.saveOrUpdate(zyModel);
                return JSONUtil.createObj().set("success", true);
            } catch (Exception e) {
              log.error(">>> 数据导入异常：", e);
              return JSONUtil.createObj().set("success", false).set("index", i + 1).set("msg", "数据导入异常");
            }
        }
    }

    @Override
    public void exportData(List<ZyModelIdParam> zyModelIdParamList, HttpServletResponse response) throws IOException {
       File tempFile = null;
       try {
         List<ZyModelEditParam> dataList;
         if(ObjectUtil.isNotEmpty(zyModelIdParamList)) {
            List<Serializable> idList = CollStreamUtil.toList(zyModelIdParamList, ZyModelIdParam::getId);
            dataList = BeanUtil.copyToList(this.listByIds(idList), ZyModelEditParam.class);
         } else {
            dataList = BeanUtil.copyToList(this.list(), ZyModelEditParam.class);
         }
         String fileName = "模型配置_" + DateUtil.format(DateTime.now(), DatePattern.PURE_DATETIME_PATTERN) + ".xlsx";
         tempFile = FileUtil.file(FileUtil.getTmpDir() + FileUtil.FILE_SEPARATOR + fileName);
         EasyExcel.write(tempFile.getPath(), ZyModelEditParam.class).sheet("模型配置").doWrite(dataList);
         CommonDownloadUtil.download(tempFile, response);
       } catch (Exception e) {
         log.error(">>> 模型配置导出失败：", e);
         CommonResponseUtil.renderError(response, "模型配置导出失败");
       } finally {
         FileUtil.del(tempFile);
       }
    }
}
