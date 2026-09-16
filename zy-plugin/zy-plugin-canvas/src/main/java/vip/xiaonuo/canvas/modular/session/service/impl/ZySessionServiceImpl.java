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
package vip.xiaonuo.canvas.modular.session.service.impl;

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
import vip.xiaonuo.canvas.modular.session.entity.ZySession;
import vip.xiaonuo.canvas.modular.session.mapper.ZySessionMapper;
import vip.xiaonuo.canvas.modular.session.param.ZySessionAddParam;
import vip.xiaonuo.canvas.modular.session.param.ZySessionEditParam;
import vip.xiaonuo.canvas.modular.session.param.ZySessionIdParam;
import vip.xiaonuo.canvas.modular.session.param.ZySessionPageParam;
import vip.xiaonuo.canvas.modular.session.service.ZySessionService;

import vip.xiaonuo.common.util.CommonDownloadUtil;
import vip.xiaonuo.common.util.CommonResponseUtil;

import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.util.List;

/**
 * 会话Service接口实现类
 *
 * @author hanbin
 * @date  2026/09/07 18:57
 **/
@Service
public class ZySessionServiceImpl extends ServiceImpl<ZySessionMapper, ZySession> implements ZySessionService {

    @Override
    public Page<ZySession> page(ZySessionPageParam zySessionPageParam) {
        QueryWrapper<ZySession> queryWrapper = new QueryWrapper<ZySession>().checkSqlInjection();
        if(ObjectUtil.isNotEmpty(zySessionPageParam.getUserId())) {
            queryWrapper.lambda().eq(ZySession::getUserId, zySessionPageParam.getUserId());
        }
        if(ObjectUtil.isNotEmpty(zySessionPageParam.getProjectId())) {
            queryWrapper.lambda().eq(ZySession::getProjectId, zySessionPageParam.getProjectId());
        }
        if(ObjectUtil.isAllNotEmpty(zySessionPageParam.getSortField(), zySessionPageParam.getSortOrder())) {
            CommonSortOrderEnum.validate(zySessionPageParam.getSortOrder());
            queryWrapper.orderBy(true, zySessionPageParam.getSortOrder().equals(CommonSortOrderEnum.ASC.getValue()),
                    StrUtil.toUnderlineCase(zySessionPageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByAsc(ZySession::getId);
        }
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            return new Page<>();
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // CommonSqlUtil.scopeIn(queryWrapper.lambda(), ZySession::getOrgId, StpUtil.getLoginIdAsString(), CommonServletUtil.getRequest().getServletPath());
        }
        return this.page(CommonPageRequest.defaultPage(), queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ZySessionAddParam zySessionAddParam) {
        ZySession zySession = BeanUtil.toBean(zySessionAddParam, ZySession.class);
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限增加会话");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // if(!loginUserDataScope.contains(zySession.getOrgId())) {
            //     throw new CommonException("您没有权限在该机构下增加会话");
            // }
        }

        this.save(zySession);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ZySessionEditParam zySessionEditParam) {
        ZySession zySession = this.queryEntity(zySessionEditParam.getId());
        BeanUtil.copyProperties(zySessionEditParam, zySession);
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限编辑会话");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如：orgId
            // if(!loginUserDataScope.contains(zySession.getOrgId())) {
            //     throw new CommonException("您没有权限编辑该机构下的会话");
            // }
        }

        this.updateById(zySession);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ZySessionIdParam> zySessionIdParamList) {
        // 校验数据范围
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();
        if(loginUserDataScope != null && loginUserDataScope.isEmpty()) {
            throw new CommonException("您没有权限删除会话");
        }
        if(ObjectUtil.isNotEmpty(loginUserDataScope)) {
            // 根据业务需要，取消下面注释并修改为实际的数据范围字段，例如对删除的记录进行数据范围校验
            // Set<String> orgIdSet = this.listByIds(CollStreamUtil.toList(zySessionIdParamList, ZySessionIdParam::getId))
            //         .stream().map(ZySession::getOrgId).collect(Collectors.toSet());
            // if(!new HashSet<>(loginUserDataScope).containsAll(orgIdSet)) {
            //     throw new CommonException("您没有权限删除这些机构下的会话");
            // }
        }
        // 执行删除
        this.removeByIds(CollStreamUtil.toList(zySessionIdParamList, ZySessionIdParam::getId));
    }

    @Override
    public ZySession detail(ZySessionIdParam zySessionIdParam) {
        return this.queryEntity(zySessionIdParam.getId());
    }

    @Override
    public ZySession queryEntity(Serializable id) {
        ZySession zySession = this.getById(id);
        if(ObjectUtil.isEmpty(zySession)) {
            throw new CommonException("会话不存在，id值为：{}", id);
        }
        return zySession;
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
       File tempFile = null;
       try {
         List<ZySessionEditParam> dataList = CollectionUtil.newArrayList();
         String fileName = "会话导入模板_" + DateUtil.format(DateTime.now(), DatePattern.PURE_DATETIME_PATTERN) + ".xlsx";
         tempFile = FileUtil.file(FileUtil.getTmpDir() + FileUtil.FILE_SEPARATOR + fileName);
         EasyExcel.write(tempFile.getPath(), ZySessionEditParam.class).sheet("会话").doWrite(dataList);
         CommonDownloadUtil.download(tempFile, response);
       } catch (Exception e) {
         log.error(">>> 会话导入模板下载失败：", e);
         CommonResponseUtil.renderError(response, "会话导入模板下载失败");
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
                    FileUtil.FILE_SEPARATOR + "zySessionImportTemplate.xlsx"));
            // 读取excel
            List<ZySessionEditParam> zySessionEditParamList =  EasyExcel.read(tempFile).head(ZySessionEditParam.class).sheet()
                    .headRowNumber(1).doReadSync();
            List<ZySession> allDataList = this.list();
            for (int i = 0; i < zySessionEditParamList.size(); i++) {
                JSONObject jsonObject = this.doImport(allDataList, zySessionEditParamList.get(i), i);
                if(jsonObject.getBool("success")) {
                    successCount += 1;
                } else {
                    errorCount += 1;
                    errorDetail.add(jsonObject);
                }
            }
            return JSONUtil.createObj()
                    .set("totalCount", zySessionEditParamList.size())
                    .set("successCount", successCount)
                    .set("errorCount", errorCount)
                    .set("errorDetail", errorDetail);
        } catch (Exception e) {
            log.error(">>> 会话导入失败：", e);
            throw new CommonException("会话导入失败");
        }
    }

    public JSONObject doImport(List<ZySession> allDataList, ZySessionEditParam zySessionEditParam, int i) {
        String id = zySessionEditParam.getId();
        if(ObjectUtil.hasEmpty(id)) {
            return JSONUtil.createObj().set("index", i + 1).set("success", false).set("msg", "必填字段存在空值");
        } else {
            try {
                int index = CollStreamUtil.toList(allDataList, ZySession::getId).indexOf(zySessionEditParam.getId());
                ZySession zySession;
                boolean isAdd = false;
                if(index == -1) {
                    isAdd = true;
                    zySession = new ZySession();
                } else {
                    zySession = allDataList.get(index);
                }
                BeanUtil.copyProperties(zySessionEditParam, zySession);
                if(isAdd) {
                    allDataList.add(zySession);
                } else {
                    allDataList.remove(index);
                    allDataList.add(index, zySession);
                }
                this.saveOrUpdate(zySession);
                return JSONUtil.createObj().set("success", true);
            } catch (Exception e) {
              log.error(">>> 数据导入异常：", e);
              return JSONUtil.createObj().set("success", false).set("index", i + 1).set("msg", "数据导入异常");
            }
        }
    }

    @Override
    public void exportData(List<ZySessionIdParam> zySessionIdParamList, HttpServletResponse response) throws IOException {
       File tempFile = null;
       try {
         List<ZySessionEditParam> dataList;
         if(ObjectUtil.isNotEmpty(zySessionIdParamList)) {
            List<Serializable> idList = CollStreamUtil.toList(zySessionIdParamList, ZySessionIdParam::getId);
            dataList = BeanUtil.copyToList(this.listByIds(idList), ZySessionEditParam.class);
         } else {
            dataList = BeanUtil.copyToList(this.list(), ZySessionEditParam.class);
         }
         String fileName = "会话_" + DateUtil.format(DateTime.now(), DatePattern.PURE_DATETIME_PATTERN) + ".xlsx";
         tempFile = FileUtil.file(FileUtil.getTmpDir() + FileUtil.FILE_SEPARATOR + fileName);
         EasyExcel.write(tempFile.getPath(), ZySessionEditParam.class).sheet("会话").doWrite(dataList);
         CommonDownloadUtil.download(tempFile, response);
       } catch (Exception e) {
         log.error(">>> 会话导出失败：", e);
         CommonResponseUtil.renderError(response, "会话导出失败");
       } finally {
         FileUtil.del(tempFile);
       }
    }
}
