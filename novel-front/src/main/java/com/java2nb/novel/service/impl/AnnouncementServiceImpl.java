package com.java2nb.novel.service.impl;

import com.github.pagehelper.PageHelper;
import com.java2nb.novel.entity.Announcement;
import com.java2nb.novel.mapper.FrontAnnouncementMapper;
import com.java2nb.novel.service.AnnouncementService;
import com.java2nb.novel.service.SensitiveWordFilterService;
import io.github.xxyopen.model.page.PageBean;
import io.github.xxyopen.model.page.builder.pagehelper.PageBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 前台公告服务实现
 */
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private static final int ROLLING_LIMIT = 5;

    private final FrontAnnouncementMapper announcementMapper;

    private final SensitiveWordFilterService sensitiveWordFilterService;

    @Override
    public List<Announcement> listRollingAnnouncements() {
        List<Announcement> rollingList = announcementMapper.listActiveByType(2, ROLLING_LIMIT);
        if (rollingList == null || rollingList.isEmpty()) {
            // 没有专门配置首页滚动公告时，回退使用文章公告，保证首页公告区域不会空置。
            rollingList = announcementMapper.listActiveByType(1, ROLLING_LIMIT);
        }
        return rollingList;
    }

    @Override
    public Announcement getPopupAnnouncement() {
        return firstAnnouncement(announcementMapper.listActiveByType(3, 1));
    }

    @Override
    public Announcement getCornerAnnouncement() {
        return firstAnnouncement(announcementMapper.listActiveByType(4, 1));
    }

    @Override
    public Announcement queryAnnouncementInfo(Long announcementId) {
        Announcement announcement = announcementMapper.getActiveById(announcementId);
        if (announcement != null && announcement.getContent() != null) {
            announcement.setContent(sensitiveWordFilterService.filterNewsContent(announcement.getContent()));
        }
        return announcement;
    }

    @Override
    public PageBean<Announcement> listByPage(int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        // 公告中心承接“更多”入口，需要同时展示文章公告和首页滚动公告，避免滚动公告只能在首页看到。
        List<Announcement> announcements = announcementMapper.listDisplayable();
        return PageBuilder.build(announcements);
    }

    private Announcement firstAnnouncement(List<Announcement> announcements) {
        if (announcements == null || announcements.isEmpty()) {
            return null;
        }
        Announcement announcement = announcements.get(0);
        if (announcement.getContent() != null) {
            announcement.setContent(sensitiveWordFilterService.filterNewsContent(announcement.getContent()));
        }
        return announcement;
    }
}
