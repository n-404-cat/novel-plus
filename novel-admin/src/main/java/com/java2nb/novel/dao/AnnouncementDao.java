package com.java2nb.novel.dao;

import com.java2nb.common.annotation.SanitizeMap;
import com.java2nb.novel.domain.AnnouncementDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 公告后台 DAO。
 */
@Mapper
public interface AnnouncementDao {

    AnnouncementDO get(Long id);

    List<AnnouncementDO> list(@SanitizeMap Map<String, Object> map);

    int count(Map<String, Object> map);

    int save(AnnouncementDO announcement);

    int update(AnnouncementDO announcement);

    int remove(Long id);

    int batchRemove(Long[] ids);
}
