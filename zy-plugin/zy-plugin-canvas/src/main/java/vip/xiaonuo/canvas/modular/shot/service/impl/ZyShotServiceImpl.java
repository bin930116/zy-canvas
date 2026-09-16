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
package vip.xiaonuo.canvas.modular.shot.service.impl;

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
import vip.xiaonuo.canvas.modular.shot.entity.ZyShot;
import vip.xiaonuo.canvas.modular.shot.mapper.ZyShotMapper;
import vip.xiaonuo.canvas.modular.shot.param.ZyShotAddParam;
import vip.xiaonuo.canvas.modular.shot.param.ZyShotEditParam;
import vip.xiaonuo.canvas.modular.shot.param.ZyShotIdParam;
import vip.xiaonuo.canvas.modular.shot.param.ZyShotPageParam;
import vip.xiaonuo.canvas.modular.shot.service.ZyShotService;

import vip.xiaonuo.common.util.CommonDownloadUtil;
import vip.xiaonuo.common.util.CommonResponseUtil;

import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.util.List;

/**
 * 短剧项目分镜Service接口实现类
 *
 * @author hanbin
 * @date  2026/09/07 18:55
 **/
@Service
public class ZyShotServiceImpl extends ServiceImpl<ZyShotMapper, ZyShot> implements ZyShotService {

    @Override
    public Page<ZyShot> page(ZyShotPageParam zyShotPageParam) {
        QueryWrapper<ZyShot> queryWrapper = new QueryWrapper<ZyShot>().checkSqlInjection();
        if(ObjectUtil.isNotEmpty(zyShotPageParam.getProjectId())) {
            queryWrapper.lambda().eq(ZyShot::getProjectId, zyShotPageParam.getProjectId());
        }
        if(ObjectUtil.isNotEmpty(zyShotPageParam.getUnitId())) {
            queryWrapper.lambda().eq(ZyShot::getUnitId, zyShotPageParam.getUnitId());
        }
        if(ObjectUtil.isAllNotEmpty(zyShotPageParam.getSortField(), zyShotPageParam.getSortOrder())) {
            CommonSortOrderEnum.validate(zyShotPageParam.getSortOrder());
            queryWrapper.orderBy(true, zyShotPageParam.getSortOrder().equals(CommonSortOrderEnum.ASC.getValue()),
                    StrUtil.toUnderlineCase(zyShotPageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByAsc(ZyShot::getId);
        }
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            return new Page<>();
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // CommonSqlUtil.scopeIn(queryWrapper.lambda(), ZyShot::getOrgId, StpUtil.getLoginIdAsString(), CommonServletUtil.getRequest().getServletPath());
        }
        return this.page(CommonPageRequest.defaultPage(), queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ZyShotAddParam zyShotAddParam) {
        ZyShot zyShot = BeanUtil.toBean(zyShotAddParam, ZyShot.class);
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限增加短剧项目分镜");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // if(!loginUserDataScope.contains(zyShot.getOrgId())) {
            //     throw new CommonException("您没有权限在该机构下增加短剧项目分镜");
            // }
        }

        this.save(zyShot);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ZyShotEditParam zyShotEditParam) {
        ZyShot zyShot = this.queryEntity(zyShotEditParam.getId());
        BeanUtil.copyProperties(zyShotEditParam, zyShot);
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限编辑短剧项目分镜");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // if(!loginUserDataScope.contains(zyShot.getOrgId())) {
            //     throw new CommonException("您没有权限编辑该机构下的短剧项目分镜");
            // }
        }

        this.updateById(zyShot);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ZyShotIdParam> zyShotIdParamList) {
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限删除短剧项目分镜");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如对删除的记录进行数据范围校验
            // Set<String> orgIdSet = this.listByIds(CollStreamUtil.toList(zyShotIdParamList, ZyShotIdParam::getId))
            //         .stream().map(ZyShot::getOrgId).collect(Collectors.toSet());
            // if(!new HashSet<>(loginUserDataScope).containsAll(orgIdSet)) {
            //     throw new CommonException("您没有权限删除这些机构下的短剧项目分镜");
            // }
        }
        // 执行删除
        this.removeByIds(CollStreamUtil.toList(zyShotIdParamList, ZyShotIdParam::getId));
    }

    @Override
    public ZyShot detail(ZyShotIdParam zyShotIdParam) {
        return this.queryEntity(zyShotIdParam.getId());
    }

    @Override
    public ZyShot queryEntity(Serializable id) {
        ZyShot zyShot = this.getById(id);
        if(ObjectUtil.isEmpty(zyShot)) {
            throw new CommonException("短剧项目分镜不存在，id值为：{}", id);
        }
        return zyShot;
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
       File tempFile = null;
       try {
         List<ZyShotEditParam> dataList = CollectionUtil.newArrayList();
         String fileName = "短剧项目分镜导入模板_" + DateUtil.format(DateTime.now(), DatePattern.PURE_DATETIME_PATTERN) + ".xlsx";
         tempFile = FileUtil.file(FileUtil.getTmpDir() + FileUtil.FILE_SEPARATOR + fileName);
         EasyExcel.write(tempFile.getPath(), ZyShotEditParam.class).sheet("短剧项目分镜").doWrite(dataList);
         CommonDownloadUtil.download(tempFile, response);
       } catch (Exception e) {
         log.error(">>> 短剧项目分镜导入模板下载失败：", e);
         CommonResponseUtil.renderError(response, "短剧项目分镜导入模板下载失败");
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
                    FileUtil.FILE_SEPARATOR + "zyShotImportTemplate.xlsx"));
            // 读取excel
            List<ZyShotEditParam> zyShotEditParamList =  EasyExcel.read(tempFile).head(ZyShotEditParam.class).sheet()
                    .headRowNumber(1).doReadSync();
            List<ZyShot> allDataList = this.list();
            for (int i = 0; i < zyShotEditParamList.size(); i++) {
                JSONObject jsonObject = this.doImport(allDataList, zyShotEditParamList.get(i), i);
                if(jsonObject.getBool("success")) {
                    successCount += 1;
                } else {
                    errorCount += 1;
                    errorDetail.add(jsonObject);
                }
            }
            return JSONUtil.createObj()
                    .set("totalCount", zyShotEditParamList.size())
                    .set("successCount", successCount)
                    .set("errorCount", errorCount)
                    .set("errorDetail", errorDetail);
        } catch (Exception e) {
            log.error(">>> 短剧项目分镜导入失败：", e);
            throw new CommonException("短剧项目分镜导入失败");
        }
    }

    public JSONObject doImport(List<ZyShot> allDataList, ZyShotEditParam zyShotEditParam, int i) {
        String id = zyShotEditParam.getId();
        String projectId = zyShotEditParam.getProjectId();
        String unitId = zyShotEditParam.getUnitId();
        if(ObjectUtil.hasEmpty(id, projectId, unitId)) {
            return JSONUtil.createObj().set("index", i + 1).set("success", false).set("msg", "必填字段存在空值");
        } else {
            try {
                int index = CollStreamUtil.toList(allDataList, ZyShot::getId).indexOf(zyShotEditParam.getId());
                ZyShot zyShot;
                boolean isAdd = false;
                if(index == -1) {
                    isAdd = true;
                    zyShot = new ZyShot();
                } else {
                    zyShot = allDataList.get(index);
                }
                BeanUtil.copyProperties(zyShotEditParam, zyShot);
                if(isAdd) {
                    allDataList.add(zyShot);
                } else {
                    allDataList.remove(index);
                    allDataList.add(index, zyShot);
                }
                this.saveOrUpdate(zyShot);
                return JSONUtil.createObj().set("success", true);
            } catch (Exception e) {
              log.error(">>> 数据导入异常：", e);
              return JSONUtil.createObj().set("success", false).set("index", i + 1).set("msg", "数据导入异常");
            }
        }
    }

    @Override
    public void exportData(List<ZyShotIdParam> zyShotIdParamList, HttpServletResponse response) throws IOException {
       File tempFile = null;
       try {
         List<ZyShotEditParam> dataList;
         if(ObjectUtil.isNotEmpty(zyShotIdParamList)) {
            List<Serializable> idList = CollStreamUtil.toList(zyShotIdParamList, ZyShotIdParam::getId);
            dataList = BeanUtil.copyToList(this.listByIds(idList), ZyShotEditParam.class);
         } else {
            dataList = BeanUtil.copyToList(this.list(), ZyShotEditParam.class);
         }
         String fileName = "短剧项目分镜_" + DateUtil.format(DateTime.now(), DatePattern.PURE_DATETIME_PATTERN) + ".xlsx";
         tempFile = FileUtil.file(FileUtil.getTmpDir() + FileUtil.FILE_SEPARATOR + fileName);
         EasyExcel.write(tempFile.getPath(), ZyShotEditParam.class).sheet("短剧项目分镜").doWrite(dataList);
         CommonDownloadUtil.download(tempFile, response);
       } catch (Exception e) {
         log.error(">>> 短剧项目分镜导出失败：", e);
         CommonResponseUtil.renderError(response, "短剧项目分镜导出失败");
       } finally {
         FileUtil.del(tempFile);
       }
    }
}
