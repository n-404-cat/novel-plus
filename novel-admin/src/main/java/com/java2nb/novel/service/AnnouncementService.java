package com.java2nb.novel.service;

import com.java2nb.novel.domain.AnnouncementDO;

import java.util.List;
import java.util.Map;

/**
 * 公告后台服务。
 */
public interface AnnouncementService {

    AnnouncementDO get(Long id);

    List<AnnouncementDO> list(Map<String, Object> map);

    int count(Map<String, Object> map);

    int save(AnnouncementDO announcement);

    int update(AnnouncementDO announcement);

    int remove(Long id);

    int batchRemove(Long[] ids);
}
