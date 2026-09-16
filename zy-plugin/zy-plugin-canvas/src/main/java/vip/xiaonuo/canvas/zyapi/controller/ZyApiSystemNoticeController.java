package vip.xiaonuo.canvas.zyapi.controller;

import cn.hutool.core.util.ObjectUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.canvas.zyapi.service.ZyApiSystemNoticeService;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.Map;

/**
 * C端系统公告/系统消息（影策创作工作台）
 * 公告：读 B 端 BizNotice；消息：读站内信 DevMessage。
 *
 * @author xuyuxiang
 * @date 2026/9/10
 **/
@Tag(name = "C端系统公告/消息")
@RestController
@Validated
public class ZyApiSystemNoticeController {

    @Resource
    private ZyApiSystemNoticeService zyApiSystemNoticeService;

    /**
     * 系统公告 feed（前端 GET /announcements）
     */
    @Operation(summary = "系统公告列表")
    @GetMapping("/announcements")
    public CommonResult<Map<String, Object>> announcementFeed() {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyApiSystemNoticeService.announcementFeed(userId));
    }

    /**
     * 系统公告详情
     */
    @Operation(summary = "系统公告详情")
    @GetMapping("/announcements/detail")
    public CommonResult<Map<String, Object>> announcementDetail(@RequestParam String id) {
        String userId = StpClientUtil.getLoginIdAsString();
        return CommonResult.data(zyApiSystemNoticeService.getAnnouncement(userId, id));
    }

    /**
     * 系统消息（站内信）feed
     */
    @Operation(summary = "系统消息列表")
    @GetMapping("/announcements/messages")
    public CommonResult<Map<String, Object>> messageFeed(@RequestParam(required = false) Integer limit) {
        String userId = StpClientUtil.getLoginIdAsString();
        Integer safeLimit = ObjectUtil.isNotEmpty(limit) ? limit : 20;
        return CommonResult.data(zyApiSystemNoticeService.messageFeed(userId, safeLimit));
    }
}
