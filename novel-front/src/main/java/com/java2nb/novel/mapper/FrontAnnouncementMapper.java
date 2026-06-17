package com.java2nb.novel.mapper;

import com.java2nb.novel.entity.Announcement;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 前台公告查询
 */
public interface FrontAnnouncementMapper {

    /**
     * 按类型查询有效公告
     *
     * @param type 公告类型
     * @param limit 返回数量
     * @return 有效公告列表
     */
    List<Announcement> listActiveByType(@Param("type") Integer type, @Param("limit") int limit);

    /**
     * 查询公告中心列表。
     *
     * @return 文章公告和首页滚动公告列表
     */
    List<Announcement> listDisplayable();

    /**
     * 查询有效公告详情
     *
     * @param id 公告ID
     * @return 公告详情
     */
    Announcement getActiveById(@Param("id") Long id);
}
