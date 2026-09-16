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
package vip.xiaonuo.biz.api;

import cn.hutool.json.JSONObject;

import java.util.List;

/**
 * 通知公告API（C端/跨模块只读查询）
 *
 * @author xuyuxiang
 * @date 2026/9/10
 **/
public interface BizNoticeApi {

    /**
     * 查询启用中的系统公告列表
     *
     * @param limit 返回条数上限，空则默认20
     * @return 公告JSON列表
     * @author xuyuxiang
     **/
    List<JSONObject> listEnabledNotices(Integer limit);

    /**
     * 查询公告详情
     *
     * @param id 公告id
     * @return 公告JSON，不存在返回null
     * @author xuyuxiang
     **/
    JSONObject getNoticeById(String id);
}
