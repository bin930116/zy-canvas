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
package vip.xiaonuo.canvas.modular.share.service.impl;

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
import vip.xiaonuo.canvas.modular.share.entity.ZyShare;
import vip.xiaonuo.canvas.modular.share.mapper.ZyShareMapper;
import vip.xiaonuo.canvas.modular.share.param.ZyShareAddParam;
import vip.xiaonuo.canvas.modular.share.param.ZyShareEditParam;
import vip.xiaonuo.canvas.modular.share.param.ZyShareIdParam;
import vip.xiaonuo.canvas.modular.share.param.ZySharePageParam;
import vip.xiaonuo.canvas.modular.share.service.ZyShareService;

import vip.xiaonuo.common.util.CommonDownloadUtil;
import vip.xiaonuo.common.util.CommonResponseUtil;

import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.util.List;

/**
 * 画布项目分享Service接口实现类
 *
 * @author hanbin
 * @date  2026/09/07 18:56
 **/
@Service
public class ZyShareServiceImpl extends ServiceImpl<ZyShareMapper, ZyShare> implements ZyShareService {

    @Override
    public Page<ZyShare> page(ZySharePageParam zySharePageParam) {
        QueryWrapper<ZyShare> queryWrapper = new QueryWrapper<ZyShare>().checkSqlInjection();
        if(ObjectUtil.isNotEmpty(zySharePageParam.getProjectId())) {
            queryWrapper.lambda().eq(ZyShare::getProjectId, zySharePageParam.getProjectId());
        }
        if(ObjectUtil.isNotEmpty(zySharePageParam.getEnabled())) {
            queryWrapper.lambda().eq(ZyShare::getEnabled, zySharePageParam.getEnabled());
        }
        if(ObjectUtil.isAllNotEmpty(zySharePageParam.getSortField(), zySharePageParam.getSortOrder())) {
            CommonSortOrderEnum.validate(zySharePageParam.getSortOrder());
            queryWrapper.orderBy(true, zySharePageParam.getSortOrder().equals(CommonSortOrderEnum.ASC.getValue()),
                    StrUtil.toUnderlineCase(zySharePageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByAsc(ZyShare::getId);
        }
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            return new Page<>();
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // CommonSqlUtil.scopeIn(queryWrapper.lambda(), ZyShare::getOrgId, StpUtil.getLoginIdAsString(), CommonServletUtil.getRequest().getServletPath());
        }
        return this.page(CommonPageRequest.defaultPage(), queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ZyShareAddParam zyShareAddParam) {
        ZyShare zyShare = BeanUtil.toBean(zyShareAddParam, ZyShare.class);
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限增加画布项目分享");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // if(!loginUserDataScope.contains(zyShare.getOrgId())) {
            //     throw new CommonException("您没有权限在该机构下增加画布项目分享");
            // }
        }

        this.save(zyShare);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ZyShareEditParam zyShareEditParam) {
        ZyShare zyShare = this.queryEntity(zyShareEditParam.getId());
        BeanUtil.copyProperties(zyShareEditParam, zyShare);
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限编辑画布项目分享");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // if(!loginUserDataScope.contains(zyShare.getOrgId())) {
            //     throw new CommonException("您没有权限编辑该机构下的画布项目分享");
            // }
        }

        this.updateById(zyShare);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ZyShareIdParam> zyShareIdParamList) {
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限删除画布项目分享");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如对删除的记录进行数据范围校验
            // Set<String> orgIdSet = this.listByIds(CollStreamUtil.toList(zyShareIdParamList, ZyShareIdParam::getId))
            //         .stream().map(ZyShare::getOrgId).collect(Collectors.toSet());
            // if(!new HashSet<>(loginUserDataScope).containsAll(orgIdSet)) {
            //     throw new CommonException("您没有权限删除这些机构下的画布项目分享");
            // }
        }
        // 执行删除
        this.removeByIds(CollStreamUtil.toList(zyShareIdParamList, ZyShareIdParam::getId));
    }

    @Override
    public ZyShare detail(ZyShareIdParam zyShareIdParam) {
        return this.queryEntity(zyShareIdParam.getId());
    }

    @Override
    public ZyShare queryEntity(Serializable id) {
        ZyShare zyShare = this.getById(id);
        if(ObjectUtil.isEmpty(zyShare)) {
            throw new CommonException("画布项目分享不存在，id值为：{}", id);
        }
        return zyShare;
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
       File tempFile = null;
       try {
         List<ZyShareEditParam> dataList = CollectionUtil.newArrayList();
         String fileName = "画布项目分享导入模板_" + DateUtil.format(DateTime.now(), DatePattern.PURE_DATETIME_PATTERN) + ".xlsx";
         tempFile = FileUtil.file(FileUtil.getTmpDir() + FileUtil.FILE_SEPARATOR + fileName);
         EasyExcel.write(tempFile.getPath(), ZyShareEditParam.class).sheet("画布项目分享").doWrite(dataList);
         CommonDownloadUtil.download(tempFile, response);
       } catch (Exception e) {
         log.error(">>> 画布项目分享导入模板下载失败：", e);
         CommonResponseUtil.renderError(response, "画布项目分享导入模板下载失败");
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
                    FileUtil.FILE_SEPARATOR + "zyShareImportTemplate.xlsx"));
            // 读取excel
            List<ZyShareEditParam> zyShareEditParamList =  EasyExcel.read(tempFile).head(ZyShareEditParam.class).sheet()
                    .headRowNumber(1).doReadSync();
            List<ZyShare> allDataList = this.list();
            for (int i = 0; i < zyShareEditParamList.size(); i++) {
                JSONObject jsonObject = this.doImport(allDataList, zyShareEditParamList.get(i), i);
                if(jsonObject.getBool("success")) {
                    successCount += 1;
                } else {
                    errorCount += 1;
                    errorDetail.add(jsonObject);
                }
            }
            return JSONUtil.createObj()
                    .set("totalCount", zyShareEditParamList.size())
                    .set("successCount", successCount)
                    .set("errorCount", errorCount)
                    .set("errorDetail", errorDetail);
        } catch (Exception e) {
            log.error(">>> 画布项目分享导入失败：", e);
            throw new CommonException("画布项目分享导入失败");
        }
    }

    public JSONObject doImport(List<ZyShare> allDataList, ZyShareEditParam zyShareEditParam, int i) {
        String id = zyShareEditParam.getId();
        if(ObjectUtil.hasEmpty(id)) {
            return JSONUtil.createObj().set("index", i + 1).set("success", false).set("msg", "必填字段存在空值");
        } else {
            try {
                int index = CollStreamUtil.toList(allDataList, ZyShare::getId).indexOf(zyShareEditParam.getId());
                ZyShare zyShare;
                boolean isAdd = false;
                if(index == -1) {
                    isAdd = true;
                    zyShare = new ZyShare();
                } else {
                    zyShare = allDataList.get(index);
                }
                BeanUtil.copyProperties(zyShareEditParam, zyShare);
                if(isAdd) {
                    allDataList.add(zyShare);
                } else {
                    allDataList.remove(index);
                    allDataList.add(index, zyShare);
                }
                this.saveOrUpdate(zyShare);
                return JSONUtil.createObj().set("success", true);
            } catch (Exception e) {
              log.error(">>> 数据导入异常：", e);
              return JSONUtil.createObj().set("success", false).set("index", i + 1).set("msg", "数据导入异常");
            }
        }
    }

    @Override
    public void exportData(List<ZyShareIdParam> zyShareIdParamList, HttpServletResponse response) throws IOException {
       File tempFile = null;
       try {
         List<ZyShareEditParam> dataList;
         if(ObjectUtil.isNotEmpty(zyShareIdParamList)) {
            List<Serializable> idList = CollStreamUtil.toList(zyShareIdParamList, ZyShareIdParam::getId);
            dataList = BeanUtil.copyToList(this.listByIds(idList), ZyShareEditParam.class);
         } else {
            dataList = BeanUtil.copyToList(this.list(), ZyShareEditParam.class);
         }
         String fileName = "画布项目分享_" + DateUtil.format(DateTime.now(), DatePattern.PURE_DATETIME_PATTERN) + ".xlsx";
         tempFile = FileUtil.file(FileUtil.getTmpDir() + FileUtil.FILE_SEPARATOR + fileName);
         EasyExcel.write(tempFile.getPath(), ZyShareEditParam.class).sheet("画布项目分享").doWrite(dataList);
         CommonDownloadUtil.download(tempFile, response);
       } catch (Exception e) {
         log.error(">>> 画布项目分享导出失败：", e);
         CommonResponseUtil.renderError(response, "画布项目分享导出失败");
       } finally {
         FileUtil.del(tempFile);
       }
    }
}
