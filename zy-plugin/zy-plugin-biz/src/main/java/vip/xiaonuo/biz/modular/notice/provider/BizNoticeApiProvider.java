/*
 * Copyright [2022] [https://www.xiaonuo.vip]
 *
 * Snowy采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改Snowy源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时，请注明软件出处 https://www.xiaonuo.vip
 * 5.不可二次分发开源参与同类竞品，如有想法可联系团队xiaonuobase@qq.com商议合作。
 * 6.若您的项目无法满足以上几点，需要更多功能代码，获取Snowy商业授权许可，请在官网购买授权，地址为 https://www.xiaonuo.vip
 */
package vip.xiaonuo.biz.modular.notice.provider;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import vip.xiaonuo.biz.api.BizNoticeApi;
import vip.xiaonuo.biz.modular.notice.entity.BizNotice;
import vip.xiaonuo.biz.modular.notice.enums.BizNoticeStatusEnum;
import vip.xiaonuo.biz.modular.notice.service.BizNoticeService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知公告API接口提供者（供 C 端 zyapi 等跨模块查询）
 *
 * @author xuyuxiang
 * @date 2026/9/10
 **/
@Service
public class BizNoticeApiProvider implements BizNoticeApi {

    @Resource
    private BizNoticeService bizNoticeService;

    @Override
    public List<JSONObject> listEnabledNotices(Integer limit) {
        int size = ObjectUtil.isNotEmpty(limit) && limit > 0 ? Math.min(limit, 100) : 20;
        return bizNoticeService.list(new LambdaQueryWrapper<BizNotice>()
                        .eq(BizNotice::getStatus, BizNoticeStatusEnum.ENABLE.getValue())
                        .orderByDesc(BizNotice::getSortCode)
                        .orderByDesc(BizNotice::getCreateTime))
                .stream()
                .limit(size)
                .map(notice -> JSONUtil.parseObj(JSONUtil.toJsonStr(notice)))
                .collect(Collectors.toList());
    }

    @Override
    public JSONObject getNoticeById(String id) {
        BizNotice notice = bizNoticeService.getById(id);
        if (notice == null) {
            return null;
        }
        if (!BizNoticeStatusEnum.ENABLE.getValue().equals(notice.getStatus())) {
            return null;
        }
        return JSONUtil.parseObj(JSONUtil.toJsonStr(notice));
    }
}
