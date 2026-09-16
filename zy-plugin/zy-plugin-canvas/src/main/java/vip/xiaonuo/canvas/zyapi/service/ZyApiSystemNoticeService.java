package vip.xiaonuo.canvas.zyapi.service;

import java.util.List;
import java.util.Map;

/**
 * C端系统公告/系统消息 Service接口
 *
 * @author xuyuxiang
 * @date 2026/9/10
 **/
public interface ZyApiSystemNoticeService {

    /**
     * 系统公告 feed（影策前端契约）
     *
     * @param userId C端用户id
     * @return { announcements: [...], unreadCount: n }
     * @author xuyuxiang
     **/
    Map<String, Object> announcementFeed(String userId);

    /**
     * 系统公告详情
     *
     * @param userId C端用户id
     * @param id     公告id
     * @return 公告对象
     * @author xuyuxiang
     **/
    Map<String, Object> getAnnouncement(String userId, String id);

    /**
     * 当前用户系统消息（站内信）列表
     *
     * @param userId C端用户id
     * @param limit  条数
     * @return 消息列表 + unreadCount
     * @author xuyuxiang
     **/
    Map<String, Object> messageFeed(String userId, Integer limit);
}
