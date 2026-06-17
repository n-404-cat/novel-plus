package com.java2nb.novel.service;

import com.java2nb.novel.entity.Announcement;
import io.github.xxyopen.model.page.PageBean;

import java.util.List;

/**
 * 前台公告服务
 */
public interface AnnouncementService {

    /**
     * 查询首页滚动公告
     *
     * @return 滚动公告列表
     */
    List<Announcement> listRollingAnnouncements();

    /**
     * 查询弹窗公告
     *
     * @return 弹窗公告
     */
    Announcement getPopupAnnouncement();

    /**
     * 查询右下角公告
     *
     * @return 右下角公告
     */
    Announcement getCornerAnnouncement();

    /**
     * 查询公告详情
     *
     * @param announcementId 公告ID
     * @return 公告详情
     */
    Announcement queryAnnouncementInfo(Long announcementId);

    /**
     * 分页查询文章公告
     *
     * @param page 页码
     * @param pageSize 分页大小
     * @return 公告分页数据
     */
    PageBean<Announcement> listByPage(int page, int pageSize);
}
