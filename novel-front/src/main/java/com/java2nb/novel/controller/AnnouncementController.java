package com.java2nb.novel.controller;

import com.java2nb.novel.entity.Announcement;
import com.java2nb.novel.service.AnnouncementService;
import io.github.xxyopen.model.page.PageBean;
import io.github.xxyopen.model.resp.RestResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台公告接口
 */
@RequestMapping("announcement")
@RestController
@Slf4j
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 首页滚动公告，优先取滚动类型，未配置时由服务层回退文章公告。
     */
    @GetMapping("rolling")
    public RestResult<List<Announcement>> rolling() {
        return RestResult.ok(announcementService.listRollingAnnouncements());
    }

    @GetMapping("popup")
    public RestResult<Announcement> popup() {
        return RestResult.ok(announcementService.getPopupAnnouncement());
    }

    @GetMapping("corner")
    public RestResult<Announcement> corner() {
        return RestResult.ok(announcementService.getCornerAnnouncement());
    }

    @GetMapping("listByPage")
    public RestResult<PageBean<Announcement>> listByPage(@RequestParam(value = "curr", defaultValue = "1") int page,
        @RequestParam(value = "limit", defaultValue = "10") int pageSize) {
        return RestResult.ok(announcementService.listByPage(page, pageSize));
    }
}
